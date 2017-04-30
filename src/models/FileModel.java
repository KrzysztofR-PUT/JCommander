package models;

import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;

/**
 * Created by Krzys on 2017-04-20.
 */
public class FileModel {
    private File file;

    private SimpleStringProperty fileName;
    private SimpleStringProperty size;
    private SimpleStringProperty creationTime;
    private final FileTime creationFileTime;

    public FileModel(File file) throws IOException {
        this.file = file;
//        this.fileName.set(file.getName());
        this.fileName = new SimpleStringProperty(file.getName());
        if (file.isDirectory()) {
            this.size = new SimpleStringProperty("<DIR>");
        } else {
            this.size = new SimpleStringProperty(String.valueOf(file.length()));
        }
        BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        this.creationFileTime = attr.creationTime();

        DateFormat df = DateFormat.getDateInstance();
        this.creationTime = new SimpleStringProperty(df.format(this.creationFileTime.toMillis()));
    }

    public FileModel(File file, String fileName) {
        this.file = file;
//        this.fileName.set(file.getName());
        this.fileName = new SimpleStringProperty(fileName);

        this.size = new SimpleStringProperty("<DIR>");


        this.creationFileTime = null;
        this.creationTime = new SimpleStringProperty("");
    }

    public File getFile() {
        return file;
    }
    public String getFileName() {
        return fileName.get();
    }

    public SimpleStringProperty fileNameProperty() {
        return fileName;
    }
    public SimpleStringProperty creationTimeProperty() {
        return creationTime;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getCreationTime() {
        return creationTime.get();
    }

    public String getSize() {
        return size.get();
    }

    public SimpleStringProperty sizeProperty() {
        return size;
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    @Override
    public String toString() {
        return getFileName();
    }
}
