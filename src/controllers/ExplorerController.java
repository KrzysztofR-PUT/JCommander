package controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.util.Callback;
import models.FileModel;
import helpers.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by krzysztof on 24/04/2017.
 */
public class ExplorerController extends Observable implements Observer {

    private Path currentPath;
    private ObservableList<FileModel> files;

    @FXML
    private ComboBox<FileModel> driveComboBox;
    @FXML
    public TableView<FileModel> filesTableView;
    @FXML
    private TableColumn<FileModel, String> nameTableColumn;
    @FXML
    private TableColumn<FileModel, String> sizeTableColumn;
    @FXML
    private TableColumn<FileModel, String> dateTableColumn;
    @FXML
    private ProgressIndicator progressIndicator;

    private Alert replaceAlert = new Alert(Alert.AlertType.CONFIRMATION);
    private Alert deleteAlert = new Alert(Alert.AlertType.CONFIRMATION);
    private ButtonType buttonTypeReplace = new ButtonType("Replace");
    private ButtonType buttonTypeDelete = new ButtonType("Delete");
    private ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);


    @FXML
    protected void initialize() {
        progressIndicator.setVisible(false);
        setupObservableTableView();
        populateComboBox();
        goToDesktop();
        changeLanguage(new Locale("en"));
    }


    public void copySelectedFileToClipboard() {
        if (filesTableView.getSelectionModel().getSelectedItem() != null) {
            List<File> selectedFiles = new ArrayList<File>();
            selectedFiles.add(filesTableView.getSelectionModel().getSelectedItem().getFile());
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putFiles(selectedFiles);
            clipboard.setContent(content);
        }
    }
    public void pasteFileFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasFiles()) {
            List<File> pastedFiles = clipboard.getFiles();
            for (File pastedFile : pastedFiles) {
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(true);
                            }
                        });
                        //Thread.sleep(3000); //EXAMPLE FOR MULTITHRD.
                        try {
                            FileManager fileManager = new FileManager();
                            fileManager.copyFileToDir(pastedFile.toPath(), currentPath, false);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    progressIndicator.setVisible(false);
                                    refreshFiles();
                                }
                            });
                        } catch (IOException e) {
//                    e.printStackTrace();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    progressIndicator.setVisible(false);
                                    pasteAndReplaceFileFromClipboard();
                                }
                            });
                        }
                        return null;
                    }
                };
                new Thread(task).start();
            }

        }

    }
    public void pasteAndReplaceFileFromClipboard() {
        Optional<ButtonType> result = getReplaceAlertBox().showAndWait();
        if (result.get() == buttonTypeReplace) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasFiles()) {

                List<File> pastedFiles = clipboard.getFiles();
                for (File pastedFile : pastedFiles) {
                    Task task = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            try {
                                FileManager fileManager = new FileManager();
                                fileManager.copyFileToDir(pastedFile.toPath(), currentPath, true);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshFiles();
                                    }
                                });
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            return null;
                        }
                    };
                    new Thread(task).start();
                }
            }
        }
    }
    public void duplicateSelectedFile() {
        if (filesTableView.getSelectionModel().getSelectedItem() != null) {
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    FileManager fileManager = new FileManager();
                    try {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(true);
                            }
                        });
                        fileManager.duplicateFile(filesTableView.getSelectionModel().getSelectedItem().getFile());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(false);
                                refreshFiles();
                            }
                        });
                    } catch (IOException e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(false);
                            }
                        });
                    }
                    //populateTableView(currentPath);
                    return null;
                }
            };
            new Thread(task).start();
        }
    }
    public void deleteSelectedFile() {
        if (filesTableView.getSelectionModel().getSelectedItem() != null) {
            Optional<ButtonType> result = getDeleteAlertBox().showAndWait();

            if (result.get() == buttonTypeDelete) {
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(true);
                            }
                        });
                        FileManager fileManager = new FileManager();
                        fileManager.deleteFile(filesTableView.getSelectionModel().getSelectedItem().getFile());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(false);
                                refreshFiles();
                            }
                        });
                        return null;
                    }
                };
                new Thread(task).start();
            }
        }
    }
    public void refreshFiles() {
        populateTableView(currentPath);
    }

    public void onDragDetected(MouseEvent mouseEvent) {
        File file = filesTableView.getSelectionModel().getSelectedItem().getFile();
        List<File> files = new ArrayList<File>();
        files.add(file);

        if (file != null) {
            Dragboard db = filesTableView.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putFiles(files);
            db.setContent(content);
        }
        mouseEvent.consume();
    }
    public void onDragOver(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            dragEvent.acceptTransferModes(TransferMode.COPY);
        }
        dragEvent.consume();
    }
    public void onDragDropped(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            List<File> receivedFiles = db.getFiles();
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            progressIndicator.setVisible(true);
                        }
                    });
                    FileManager fileManager = new FileManager();
                    try {
                        fileManager.copyFileToDir(receivedFiles.get(0).toPath(), currentPath, false);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    files.add(new FileModel(receivedFiles.get(0)));
                                    //fileManager.deleteFile(receivedFiles.get(0));
                                    filesTableView.requestFocus();
                                    dragEvent.setDropCompleted(true);
                                    dragEvent.consume();
                                    progressIndicator.setVisible(false);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    progressIndicator.setVisible(false);
                                }
                            }
                        });
                    } catch (IOException e) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(false);
                                onDragDroppedReplaceFile(dragEvent);
                            }
                        });
                    }

                    return null;
                }
            };
            new Thread(task).start();
        }
    }
    public void onDragDroppedReplaceFile(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            List<File> receivedFiles = db.getFiles();
            Optional<ButtonType> result = getReplaceAlertBox().showAndWait();
            if (result.get() == buttonTypeReplace) {
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressIndicator.setVisible(true);
                            }
                        });
                        try {
                            FileManager fileManager = new FileManager();
                            fileManager.copyFileToDir(receivedFiles.get(0).toPath(), currentPath, true);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    //fileManager.deleteFile(receivedFiles.get(0));
                                    filesTableView.requestFocus();
                                    dragEvent.setDropCompleted(true);
                                    dragEvent.consume();
                                    progressIndicator.setVisible(false);
                                }
                            });
                        } catch (IOException e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    //onDragDroppedReplaceFile(dragEvent);
                                    progressIndicator.setVisible(false);
                                }
                            });
                        }
                        return null;
                    }
                };
                new Thread(task).start();
            }
        }
    }

    public void moveUpDirButtonClicked(MouseEvent mouseEvent) {
        currentPath = currentPath.toFile().getParentFile().toPath();
        populateTableView(currentPath);
    }
    public void moveToRootButtonClicked(MouseEvent mouseEvent) {
        currentPath = driveComboBox.getSelectionModel().getSelectedItem().getFile().toPath();
        populateTableView(currentPath);
    }



    private void populateComboBox() {
        FileManager fileManager = new FileManager();

        ObservableList<FileModel> drives;
        drives = fileManager.getAllDrives();

        driveComboBox.setItems(drives);
        driveComboBox.setCellFactory(new Callback<ListView<FileModel>, ListCell<FileModel>>() {
            @Override
            public ListCell<FileModel> call(ListView<FileModel> param) {
                return new ListCell<FileModel>() {
                    @Override
                    public void updateItem(FileModel item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setText(item.getFileName());
                            setGraphic(null);
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
        driveComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FileModel>() {
            @Override
            public void changed(ObservableValue<? extends FileModel> observable,
                                FileModel oldValue, FileModel newValue) {
                populateTableView(newValue.getFile().toPath());
            }
        });
        driveComboBox.getSelectionModel().selectFirst();
    }
    private void populateTableView(Path path) {
        FileManager fileManager = new FileManager();
        currentPath = path;


        System.out.println("Called with: " + path);
        try {
            files = fileManager.getAllFilesFromPath(path);
            filesTableView.setItems(files);
        } catch (IOException e) {
            e.printStackTrace();
        }

        filesTableView.setRowFactory(tv -> {
            TableRow<FileModel> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    FileModel rowData = row.getItem();
                    if (rowData.getFile().isDirectory()) {
                        populateTableView(rowData.getFile().toPath());
                    }
                }
            });
            return row;
        });
    }
    private void setupObservableTableView() {
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<FileModel, String>("fileName"));
        sizeTableColumn.setCellValueFactory(new PropertyValueFactory<FileModel, String>("size"));
        dateTableColumn.setCellValueFactory(new PropertyValueFactory<FileModel, String>("creationTime"));

        filesTableView.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    notifyForFocus();
                }
            }
        });
    }

    private Alert getReplaceAlertBox() {
        replaceAlert.getButtonTypes().setAll(buttonTypeReplace, buttonTypeCancel);
        return replaceAlert;
    }
    private Alert getDeleteAlertBox() {
        deleteAlert.getButtonTypes().setAll(buttonTypeDelete, buttonTypeCancel);
        return deleteAlert;
    }


    private void notifyForFocus() {
        this.setChanged();
        this.notifyObservers();
    }
    private void changeLanguage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("resources/lang", locale);
        nameTableColumn.getColumns();
        nameTableColumn.setText(bundle.getString("table.name"));
        sizeTableColumn.setText(bundle.getString("table.size"));
        dateTableColumn.setText(bundle.getString("table.date"));

        replaceAlert.setTitle(bundle.getString("alert.title"));
        replaceAlert.setHeaderText(bundle.getString("alert.header"));
        replaceAlert.setContentText(bundle.getString("alert.content"));
        deleteAlert.setTitle(bundle.getString("alert.title"));
        deleteAlert.setHeaderText(bundle.getString("alert.deleteheader"));
        deleteAlert.setContentText(bundle.getString("alert.deletecontent"));

        buttonTypeReplace = new ButtonType(bundle.getString("alert.replace"));
        buttonTypeDelete = new ButtonType(bundle.getString("alert.replace"));
        buttonTypeCancel = new ButtonType(bundle.getString("alert.cancel"));
        buttonTypeDelete = new ButtonType(bundle.getString("delete"));
    }




    @Override
    public void update(Observable o, Object arg) {
        changeLanguage((Locale) arg);
    }

    //TODO: TMP HELPER -> DELETE
    private void goToDesktop() {
        populateTableView(Paths.get("/Users/krzysztof/Desktop"));
    }
}
