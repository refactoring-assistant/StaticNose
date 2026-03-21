package edu.northeastern.reporting;

import java.io.File;

public class FileUtils {

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
