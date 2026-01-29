package edu.northeastern.reporting;

import java.io.File;

public class FileUtils {

    public static File safeCreateDir(String dirPath) {
        File dir = new File(dirPath);

        if(!dir.exists()){
            dir.mkdir();
        }

        return dir;
    }

}
