package de.rogovskyy.reorderpdf;

import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;

import de.rogovskyy.reorderpdf.model.DocumentPage;
import de.rogovskyy.reorderpdf.model.PagesManager;
import de.rogovskyy.reorderpdf.model.WholeDocumentPage;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {
	private final PagesManager mgr = new PagesManager();

	@FXML
	public Label infoTxt;
	
	@FXML
	public ListView<DocumentPage> pagesView;

	@FXML
	public Pane previewImg;

	@FXML
	public Button addBtn;

	@FXML
	public Button extractBtn;

	@FXML
	public Button saveBtn;

	@FXML
	public Button removeBtn;

	@FXML
	public Button upBtn;

	@FXML
	public Button downBtn;

	@FXML
	public void initialize() {
		pagesView.itemsProperty().bind(mgr.documentPagesProperty());
		previewImg.backgroundProperty().bind(Bindings.createObjectBinding(this::getBackgroundFromSelectedItm,
				pagesView.getSelectionModel().selectedItemProperty()));

		extractBtn.disableProperty().bind(dependsOnSelected((i, s) -> !(s instanceof WholeDocumentPage)));
		upBtn.disableProperty().bind(dependsOnSelected((i, s) -> i <= 0));
		downBtn.disableProperty().bind(dependsOnSelected((i, s) -> i == -1 || i == mgr.getDocumentPages().size() - 1));
		removeBtn.disableProperty().bind(dependsOnSelected((i, s) -> s == null));
		saveBtn.disableProperty().bind(Bindings.equal(Bindings.size(mgr.documentPagesProperty()), 0));
		infoTxt.visibleProperty().bind(Bindings.equal(Bindings.size(mgr.documentPagesProperty()), 0));
		
		addBtn.setOnAction((ae) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF File", "*.pdf"),
					new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
			File selectedFile = fileChooser.showOpenDialog(addBtn.getScene().getWindow());
			if (selectedFile == null)
				return; // user clicked cancel
			try {
				mgr.loadFile(selectedFile);
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
				alert.showAndWait();
			}
		});

		saveBtn.setOnAction((ae) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new ExtensionFilter("PDF File", "*.pdf"));
			File selectedFile = fileChooser.showSaveDialog(addBtn.getScene().getWindow());
			if (selectedFile == null)
				return; // user clicked cancel
			try {
				mgr.save(selectedFile);
				Alert alert = new Alert(AlertType.INFORMATION, "File saved successfully.", ButtonType.CLOSE);
				alert.showAndWait();
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
				alert.showAndWait();
			}
		});

		extractBtn.setOnAction((ae) -> {
			DocumentPage page = pagesView.getSelectionModel().getSelectedItem();
			if (page instanceof WholeDocumentPage) {
				mgr.unpackPDF(pagesView.getSelectionModel().getSelectedIndex());
			}
		});

		upBtn.setOnAction((ae) -> {
			int i = pagesView.getSelectionModel().getSelectedIndex();
			if (i == 0)
				return;
			DocumentPage itm = mgr.getDocumentPages().get(i);
			mgr.getDocumentPages().remove(i);
			mgr.getDocumentPages().add(i - 1, itm);
			pagesView.getSelectionModel().select(i - 1);
			pagesView.requestFocus();
		});

		downBtn.setOnAction((ae) -> {
			int i = pagesView.getSelectionModel().getSelectedIndex();
			if (i == mgr.getDocumentPages().size() - 1)
				return;
			DocumentPage itm = mgr.getDocumentPages().get(i);
			mgr.getDocumentPages().remove(i);
			mgr.getDocumentPages().add(i + 1, itm);
			pagesView.getSelectionModel().select(i + 1);
			pagesView.requestFocus();
		});

		removeBtn.setOnAction((ae) -> {
			int i = pagesView.getSelectionModel().getSelectedIndex();
			mgr.remove(i);
			if (mgr.getDocumentPages().size() > 0) {
				pagesView.getSelectionModel().select(i);
				pagesView.requestFocus();
			}
		});
	}

	private <T> ObjectBinding<T> dependsOnSelected(BiFunction<Integer, DocumentPage, T> c) {
		return Bindings.createObjectBinding(
				() -> c.apply(pagesView.getSelectionModel().getSelectedIndex(),
						pagesView.getSelectionModel().getSelectedItem()),
				pagesView.getSelectionModel().selectedItemProperty());
	}

	private Background getBackgroundFromSelectedItm() {
		DocumentPage selected = pagesView.getSelectionModel().getSelectedItem();
		if (selected == null) {
			return null;
		}
		return new Background(
				new BackgroundImage(selected.getImage(), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
						BackgroundPosition.CENTER, new BackgroundSize(0, 0, false, false, true, false)));
	}

	public void close() {
		mgr.close();
	}
}
