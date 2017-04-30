package controllers;

import helpers.BundleManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.*;

/**
 * Created by krzysztof on 11/04/2017.
 */
public class RootController implements Observer {

    private BundleManager bundleManager = new BundleManager(new Locale("en"));

    private ExplorerController activeExplorerController;
    private ExplorerController leftExplorerController;
    private ExplorerController rightExplorerController;

    public SplitPane splitPane;

    public Menu fileMenu;
    public Menu languageMenu;
    public MenuItem closeMenuItem;
    public MenuItem englishMenuItem;
    public MenuItem polishMenuItem;

    public Button deleteButton;
    public Button duplicateButton;
    public Button refreshButton;
    public Button copyButton;
    public Button pasteButton;

    @FXML
    protected void initialize() {
        bundleManager.addObserver(this);
        addSubViews();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof BundleManager)
            changeLanguage((Locale) arg);
        else if (o == leftExplorerController) {
            activeExplorerController = leftExplorerController;
            rightExplorerController.refreshFiles();
        }
        else if (o == rightExplorerController) {
            activeExplorerController = rightExplorerController;
            leftExplorerController.refreshFiles();
        }
    }

    public void langChangedToEng(ActionEvent actionEvent) {
        bundleManager.changeAndNotify(new Locale("en"));
    }
    public void langChangedToPol(ActionEvent actionEvent) {
        bundleManager.changeAndNotify(new Locale("pl"));
    }
    public void closeApplication(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }

    public void duplicateButtonClicked(ActionEvent actionEvent) {
        if (activeExplorerController != null) {
            activeExplorerController.duplicateSelectedFile();
        }
    }
    public void refreshButtonClicked(ActionEvent actionEvent) {
        leftExplorerController.refreshFiles();
        rightExplorerController.refreshFiles();
    }
    public void copyButtonClicked(ActionEvent actionEvent) {
        if (activeExplorerController != null) {
            activeExplorerController.copySelectedFileToClipboard();
        }
    }
    public void pasteButtonClicked(ActionEvent actionEvent) {
        if (activeExplorerController != null) {
            activeExplorerController.pasteFileFromClipboard();
        }
    }
    public void deleteButtonClicked(ActionEvent actionEvent) {
        if (activeExplorerController != null) {
            activeExplorerController.deleteSelectedFile();
        }
    }

    private void changeLanguage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("resources/lang", locale);
        fileMenu.setText(bundle.getString("file"));
        closeMenuItem.setText(bundle.getString("file.close"));
        languageMenu.setText(bundle.getString("language"));
        englishMenuItem.setText(bundle.getString("language.english"));
        polishMenuItem.setText(bundle.getString("language.polish"));
        deleteButton.setText(bundle.getString("delete"));
        duplicateButton.setText(bundle.getString("duplicate"));
        refreshButton.setText(bundle.getString("refresh"));
        copyButton.setText(bundle.getString("copy"));
        pasteButton.setText(bundle.getString("paste"));
    }
    private void addSubViews(){
        FXMLLoader loader1 = new FXMLLoader(getClass().getResource("/resources/views/explorer.fxml"));
        ResourceBundle bundle1 = ResourceBundle.getBundle("resources/lang", new Locale("en"));
        loader1.setResources(bundle1);

        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/resources/views/explorer.fxml"));
        ResourceBundle bundle2 = ResourceBundle.getBundle("resources/lang", new Locale("en"));
        loader2.setResources(bundle2);

        try {
            Pane pane1 = loader1.load();
            Pane pane2 = loader2.load();
            splitPane.getItems().add(pane1);
            splitPane.getItems().add(pane2);

            leftExplorerController = loader1.getController();
            rightExplorerController = loader2.getController();

            bundleManager.addObserver(leftExplorerController);
            bundleManager.addObserver(rightExplorerController);
            leftExplorerController.addObserver(this);
            rightExplorerController.addObserver(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
