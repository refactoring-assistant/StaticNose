package edu.northeastern.reporting;

import java.io.File;

public class FileUtils {

    /**
     * Function to safely create a directory. It only creates a directory
     * if it does not already exist at the directory path.
     * @param dirPath the filepath to create a directory at
     * @return the directory File object
     */
    public static File safeCreateDir(String dirPath) {
        File dir = new File(dirPath);

        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                throw new RuntimeException("Failed to create directory: " + dirPath);
            }
        }
        return dir;
    }

}
