package com.starterkit.javafx.controller;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import com.starterkit.javafx.dataprovider.DataProvider;
import com.starterkit.javafx.dataprovider.data.ImageVO;
import com.starterkit.javafx.model.FileModel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

public class ImagesViewerController {

	private final DataProvider dataProvider = DataProvider.INSTANCE;
	private FileModel model = new FileModel();
	private Timeline timeline;
	private boolean slideShowOn = false;
	private DoubleProperty zoomProperty;

	@FXML
	private ResourceBundle resources;

	@FXML
	private ImageView imageView;

	@FXML
	private Button previousButton;

	@FXML
	private Button playButton;

	@FXML
	private Button nextButton;

	@FXML
	private Button browseButton;

	@FXML
	private Label directoryLabel;

	@FXML
	private ListView<ImageVO> imagesList;

	@FXML
	private GridPane gridPane;

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private StackPane imageHolder;

	@FXML
	private void initialize() {
		initializeImagesList();
		imagesList.itemsProperty().bind(model.resultProperty());
	}

	@FXML
	public void browseButtonAction() throws IOException {
		imageView.setImage(null);
		zoomProperty = null;
		selectDirectory();
		if (model.getDirectory() != "") {
			updateDirectoryLabel();
			updateImagesList();
			enableButtons();
		}
	}

	@FXML
	public void nextButtonAction() {
		imagesList.getSelectionModel().selectNext();
	}

	@FXML
	public void previousButtonAction() {
		imagesList.getSelectionModel().selectPrevious();
	}

	@FXML
	public void playButtonAction() {
		if (!slideShowOn) {
			slideShowOn = true;
			timeline = new Timeline();
			timeline.setCycleCount(Timeline.INDEFINITE);
			timeline.setAutoReverse(true);

			Duration duration = Duration.millis(1000);

			EventHandler<ActionEvent> onFinished = new EventHandler<ActionEvent>() {
				public void handle(ActionEvent t) {
					if (imagesList.getSelectionModel().getSelectedItem() != null
							&& !imagesList.getSelectionModel().isSelected(imagesList.getItems().size() - 1)) {
						imagesList.getSelectionModel().selectNext();
					} else {
						imagesList.getSelectionModel().selectFirst();
					}
				}
			};
			KeyFrame keyFrame = new KeyFrame(duration, onFinished);
			timeline.getKeyFrames().add(keyFrame);

			timeline.play();
			playButton.setText(resources.getString("button.stop"));
		} else {
			slideShowOn = false;
			timeline.stop();
			playButton.setText(resources.getString("button.play"));
		}
	}

	private void initializeZoom() {
		if (zoomProperty == null) {
			zoomProperty = new SimpleDoubleProperty(200);
			zoomProperty.addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable arg0) {
					imageView.setFitWidth(zoomProperty.get() * 4);
					imageView.setFitHeight(zoomProperty.get() * 3);
				}
			});

			scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
				@Override
				public void handle(ScrollEvent event) {
					if (event.getDeltaY() > 0) {
						zoomProperty.set(zoomProperty.get() * 1.1);
					} else if (event.getDeltaY() < 0) {
						zoomProperty.set(zoomProperty.get() / 1.1);
					}

				}
			});
		}
	}

	private void enableButtons() {
		previousButton.setDisable(false);
		nextButton.setDisable(false);
		playButton.setDisable(false);
	}

	private void initializeImagesList() {
		imagesList.setCellFactory(listView -> new ListCell<ImageVO>() {
			private final ImageView imageView = new ImageView();
			{
				imageView.setFitHeight(100);
				imageView.setFitWidth(200);
				imageView.setPreserveRatio(true);
			}

			@Override
			public void updateItem(ImageVO image, boolean empty) {
				super.updateItem(image, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
				} else {
					imageView.setImage(new Image("file:" + File.separator + image.getFullPath()));
					setGraphic(imageView);
				}
			}
		});
		imagesList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ImageVO>() {
			@Override
			public void changed(ObservableValue<? extends ImageVO> observable, ImageVO oldValue, ImageVO newValue) {
				if (newValue == null) {
					return;
				}
				Image image = new Image("file:" + File.separator + newValue.getFullPath());
				imageView.setImage(image);
				imageView.setFitWidth(image.getWidth());
				imageView.setFitHeight(image.getHeight());
				initializeZoom();
			}
		});
	}

	private void updateImagesList() {
		Task<Collection<ImageVO>> getImagesTask = new Task<Collection<ImageVO>>() {

			@Override
			protected Collection<ImageVO> call() throws Exception {
				return dataProvider.getImages(model.getDirectory());
			}

			@Override
			protected void succeeded() {
				model.setResult(getValue());
				imagesList.setVisible(true);
			}
		};
		new Thread(getImagesTask).start();
	}

	private void updateDirectoryLabel() {
		directoryLabel.setText(model.getDirectory());
		directoryLabel.setTooltip(new Tooltip(model.getDirectory()));
		directoryLabel.setVisible(true);
	}

	private void selectDirectory() throws IOException {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File(".").getCanonicalFile());
		model.setDirectory(directoryChooser.showDialog(gridPane.getScene().getWindow()).getAbsolutePath());

	}
}
