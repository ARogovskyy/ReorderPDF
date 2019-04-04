package de.rogovskyy.reorderpdf.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class ImagePage implements DocumentPage {
	private final Image img;
	private final String name;
	private PDRectangle targetRect;

	public ImagePage(File imageFile) throws MalformedURLException {
		img = new Image(imageFile.toURI().toURL().toString());
		name = imageFile.getName();
	}

	@Override
	public Image getImage() {
		return img;
	}

	@Override
	public String toString() {
		return name;
	}

	public PDRectangle getTargetRect() {
		return targetRect;
	}

	public void setTargetRect(PDRectangle targetRect) {
		this.targetRect = targetRect;
	}

	@Override
	public void addToPdf(PDDocument doc) throws IOException {
		PDImageXObject pdfImage = LosslessFactory.createFromImage(doc, SwingFXUtils.fromFXImage(img, null));
		PDPage newPage = new PDPage(getTargetRect() == null ? PDRectangle.A4 : getTargetRect());
		try (PDPageContentStream contentStream = new PDPageContentStream(doc, newPage, AppendMode.OVERWRITE, true,
				true)) {
			contentStream.drawImage(pdfImage, 0, 0, pdfImage.getWidth(), pdfImage.getHeight());
		}
		doc.addPage(newPage);
	}

}
