package de.rogovskyy.reorderpdf.model;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class WholeDocumentPage implements DocumentPage, PDFReference{
	private static final Log log = LogFactory.getLog(WholeDocumentPage.class);
	
	private final String docName;
	private final PDDocument pdf;
	private final Image rendered;

	public WholeDocumentPage(String docName, PDDocument pdf) {
		this.docName = docName;
		this.pdf = pdf;
		Image rendered = null;
		try {
			rendered = SwingFXUtils.toFXImage(new PDFRenderer(pdf).renderImage(0), null);
		} catch (IOException e) {
			log.warn("Could not render first page for preview", e);
		}
		this.rendered = rendered;
	}

	public PDDocument getPdf() {
		return pdf;
	}

	@Override
	public Image getImage() {
		return rendered;
	}
	
	@Override
	public String toString() {
		return docName;
	}

	@Override
	public void addToPdf(PDDocument doc) throws IOException {
		new PDFMergerUtility().appendDocument(doc, pdf);
	}
}
