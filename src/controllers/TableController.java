package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * Created by Krzys on 2017-04-19.
 */
public class TableController implements Observer {

    @FXML
    private TableColumn nameTableColumn;
    @FXML
    private TableColumn extensionTableColumn;
    @FXML
    private TableColumn sizeTableColumn;
    @FXML
    private TableColumn dateTableColumn;

    @FXML
    protected void initialize() {
        changeLanguage(new Locale("en"));
    }

    @Override
    public void update(Observable o, Object arg) {
        //ResourceBundle bundle = ResourceBundle.getBundle("resources/lang", locale);
        changeLanguage((Locale) arg);
    }

    private void changeLanguage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("resources/lang", locale);
        nameTableColumn.getColumns();
        nameTableColumn.setText(bundle.getString("table.name"));
        extensionTableColumn.setText(bundle.getString("table.ext"));
        sizeTableColumn.setText(bundle.getString("table.size"));
        dateTableColumn.setText(bundle.getString("table.date"));
    }
}
