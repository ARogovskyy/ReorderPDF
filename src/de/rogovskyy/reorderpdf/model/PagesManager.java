package de.rogovskyy.reorderpdf.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PagesManager {
	private static final Log log = LogFactory.getLog(PagesManager.class);
	public static final String[] SUPPORTED_IMG_EXTS = { ".jpg", ".jpeg", ".png" };

	private final SimpleListProperty<DocumentPage> documentPages = new SimpleListProperty<>(
			FXCollections.observableArrayList());

	public final SimpleListProperty<DocumentPage> documentPagesProperty() {
		return this.documentPages;
	}

	public final ObservableList<DocumentPage> getDocumentPages() {
		return this.documentPagesProperty().get();
	}

	public final void setDocumentPages(final ObservableList<DocumentPage> documentPages) {
		this.documentPagesProperty().set(documentPages);
	}

	public void loadFile(File selectedFile) throws IOException {
		String path = selectedFile.getName();
		int dotIndex = path.lastIndexOf('.');
		if (dotIndex == -1) {
			throw new IOException("The selected file type is not supported.");
		}
		String ext = path.substring(dotIndex);
		if (Arrays.stream(SUPPORTED_IMG_EXTS).anyMatch((e) -> e.equalsIgnoreCase(ext))) {
			loadImage(selectedFile);
		} else if (ext.equalsIgnoreCase(".pdf")) {
			loadPDF(selectedFile);
		} else {
			throw new IOException("The selected file type is not supported.");
		}
	}

	private void loadPDF(File selectedFile) throws IOException {
		PDDocument pdf;
		pdf = PDDocument.load(selectedFile);
		documentPages.add(new WholeDocumentPage(selectedFile.getName(), pdf));
	}

	private void loadImage(File selectedFile) throws MalformedURLException {
		documentPages.add(new ImagePage(selectedFile));
	}

	public void unpackPDF(int itemIndex) {
		DocumentPage page = documentPages.get(itemIndex);
		if (!(page instanceof WholeDocumentPage))
			return;

		// convert them first and add all at once to update UI only once
		WholeDocumentPage pdfPage = ((WholeDocumentPage) page);
		PDDocument pdf = pdfPage.getPdf();
		List<SinglePDFPage> newPages = IntStream.range(0, pdf.getNumberOfPages())
				.mapToObj((i) -> new SinglePDFPage(pdfPage.toString(), pdf, i)).collect(Collectors.toList());
		documentPages.remove(itemIndex);
		documentPages.addAll(itemIndex, newPages);
	}

	public void save(File selectedFile) throws IOException {
		if (documentPages.size() == 0)
			throw new IOException("You need to add at least one page");
		PDDocument pdf = new PDDocument();
		for (DocumentPage documentPage : documentPages) {
			documentPage.addToPdf(pdf);
		}
		pdf.save(selectedFile);
	}

	public void remove(int i) {
		if (i < 0 || i >= documentPages.size())
			throw new IndexOutOfBoundsException();
		PDDocument referencedDoc;
		DocumentPage page = documentPages.get(i);
		if (page instanceof PDFReference)
			referencedDoc = ((PDFReference) page).getPdf();
		else
			referencedDoc = null;

		documentPages.remove(i);

		// if the removed page referenced a PDDocument, we need to make sure to close it if no references
		// to it are left
		if (referencedDoc != null && !documentPages.stream()
				.anyMatch((p) -> p instanceof PDFReference && ((PDFReference) p).getPdf() == referencedDoc)) {
			try {
				referencedDoc.close();
			} catch (IOException e) {
				log.debug("Error while closing PDF document", e);
			}
		}
	}
	
	public void close() {
		for (DocumentPage page : documentPages) {
			if(page instanceof PDFReference) {
				try {
					((PDFReference) page).getPdf().close();
				} catch (IOException e) {
					log.debug("Error while closing PDF document", e);
				}
			}
		}
	}

}
