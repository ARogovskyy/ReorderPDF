package de.rogovskyy.reorderpdf.model;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

import javafx.scene.image.Image;

public interface DocumentPage {
	Image getImage();

	void addToPdf(PDDocument doc) throws IOException;
}
