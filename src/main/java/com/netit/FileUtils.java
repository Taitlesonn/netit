package com.netit;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import javafx.stage.DirectoryChooser;

public class FileUtils {

    public static void copyDirectory(String sourceDir, String targetDir) throws IOException {
        Path sourcePath = Paths.get(sourceDir);
        Path targetPath = Paths.get(targetDir);

        Files.walk(sourcePath).forEach(source -> {
            try {
                Path target = targetPath.resolve(sourcePath.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error copying: " + source, e);
            }
        });
    }
}
