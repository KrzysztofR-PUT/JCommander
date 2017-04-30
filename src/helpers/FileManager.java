package helpers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.FileModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by Krzys on 2017-04-20.
 */
public class FileManager {

    public FileManager() {

    }

    public ObservableList<FileModel> getAllDrives() {
        ObservableList<FileModel> drives = FXCollections.observableArrayList();
        File[] roots = File.listRoots();
        for (File drive : roots) {
            drives.add(new FileModel(drive, drive.toString()));
        }
        return drives;
    }

    public ObservableList<FileModel> getAllFilesFromPath(Path path) throws IOException {
        ObservableList<FileModel> files = FXCollections.observableArrayList();

        File file = path.toFile();
        for (File entry : file.listFiles()) {
            files.add(new FileModel(entry));
        }

        return files;
    }

    synchronized public void copyFileToDir (Path source, Path target, Boolean canReplace) throws IOException {
        Path targetFile = Paths.get(target.toString() + "/" + source.getFileName());
        if (canReplace) {
            Files.copy(source, targetFile, REPLACE_EXISTING);
        } else {
            Files.copy(source.toAbsolutePath(), targetFile.toAbsolutePath());
        }
    }

    synchronized public void duplicateFile(File file) throws IOException {
//        Path targetPath = Paths.get(file.getPath().toString() + "Copy");
        String fileName = file.getPath().toString();
        String filenameWithoutExt = fileName.replaceFirst("[.][^.]+$", "");
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        Path targetPath = Paths.get(filenameWithoutExt + "-Copy." + extension);
        Files.copy(file.toPath(), targetPath);

    }

    synchronized public void deleteFile(File file) {
        try {
            File binFile = new File("src/xbin");
            copyFileToDir(file.toPath(), binFile.toPath(), true);
            Files.delete(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printAllFilesForPath(String path) {
        File file = new File(path);

        for (File entry : file.listFiles()) {
            if (entry.isDirectory()) {
                System.out.println(entry.getName());
            } else {
                System.out.println(entry.getName());
            }
        }
    }
}
