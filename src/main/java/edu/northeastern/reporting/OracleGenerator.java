package edu.northeastern.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class used to generate an oracle from a valid java project.
 */
public class OracleGenerator {

    private final File sourceFolder;

    public OracleGenerator(File sourceFolder) {
        this.sourceFolder = sourceFolder;
    }

    /**
     * Generates the oracle file for a valid java project.
     * Since files are named after the public class in a file,
     * the public class is used to name the file name column.
     */
    public void generate() {
        List<File> javaFiles = findJavaFiles(sourceFolder);

        String folderName = sourceFolder.getName();
        String outputFileName = folderName + "-oracle.csv";

        System.out.println("Found " + javaFiles.size() + " Java files. Generating " + outputFileName + "...");

        Set<String> uniqueClassNames = new HashSet<>();

        try (FileWriter writer = new FileWriter(outputFileName)) {
            writer.append("File Name,Expected Code Smells\n");

            for (File file : javaFiles) {
                String className = file.getName().replace(".java", "");

                if (!uniqueClassNames.add(className)) {
                    throw new IllegalStateException("Error: Duplicate class name found across directories: '" + className + "'. Oracle generation aborted.");
                }

                writer.append(className)
                        .append(",")
                        .append("[]\n");
            }

            System.out.println("Successfully generated oracle template: " + outputFileName);

        } catch (IOException e) {
            System.err.println("Failed to write oracle: " + e.getMessage());
        }
    }

    /**
     * Recursively walks the source directory to find all .java files.
     */
    private List<File> findJavaFiles(File folder) {
        try (Stream<Path> paths = Files.walk(Paths.get(folder.getAbsolutePath()))) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error scanning for Java files in directory: " + folder.getAbsolutePath(), e);
        }
    }
}