package de.rogovskyy.reorderpdf.model;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class PDFPage implements DocumentPage {
	private static final Log log = LogFactory.getLog(PDFPage.class);

	private final String name;
	private final PDDocument pdf;
	private final int pageNum;
	private final PDPage page;
	private final Image rendered;

	public PDFPage(String name, PDDocument pdf, int pageNum) {
		this.name = name;
		this.pdf = pdf;
		this.pageNum = pageNum;
		this.page = pdf.getPage(pageNum);
		Image rendered = null;
		try {
			rendered = SwingFXUtils.toFXImage(new PDFRenderer(pdf).renderImage(pageNum), null);
		} catch (IOException e) {
			log.warn("Could not render page " + pageNum + " for preview", e);
		}
		this.rendered = rendered;
	}

	public PDDocument getPdf() {
		return pdf;
	}

	public int getPageNum() {
		return pageNum;
	}

	@Override
	public Image getImage() {
		return rendered;
	}

	@Override
	public String toString() {
		return name + " -  page " + pageNum;
	}

	@Override
	public void addToPdf(PDDocument doc) throws IOException {
		doc.addPage(page);
	}

}
