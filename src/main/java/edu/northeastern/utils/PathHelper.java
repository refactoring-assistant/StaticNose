package edu.northeastern.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathHelper {

    public static String getPathFromInputFolder(String fileFullPath, String inputDirFullPath) {
        try {
            Path inputPath = Paths.get(inputDirFullPath).toAbsolutePath().normalize();
            Path filePath = Paths.get(fileFullPath).toAbsolutePath().normalize();

            Path base = inputPath.getParent();

            if(base == null) return filePath.toString().replace("\\", "/");

            return base.relativize(filePath).toString().replace("\\", "/");
        } catch(Exception e) {
            return fileFullPath;
        }
    }
}
