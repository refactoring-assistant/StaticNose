package edu.northeastern.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class that contains helper functions for path operations.
 */
public class PathHelper {

    /**
     * Function to obtain the relative path of a file to the input project path.
     * @param fileFullPath the full file path of the file to get the relative path for
     * @param inputDirFullPath the input directory
     * @return the relative path of the file
     */
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
