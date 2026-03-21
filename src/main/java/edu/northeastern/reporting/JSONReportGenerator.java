package edu.northeastern.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JSONReportGenerator extends AbstractReportGenerator{

    public JSONReportGenerator(String outputPath) {
        super(outputPath);
    }

    @Override
    protected String getFileExtension() {
        return ".json";
    }

    @Override
    public void generate(List<ReportStruct> reportStructList) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();

        if (parentDir != null) {
            try {
                FileUtils.safeCreateDir(parentDir.getAbsolutePath());
            } catch (RuntimeException e) {
                System.err.println("Aborting report generation: " + e.getMessage());
                return; // Stop execution if we can't create the directory
            }
        }

        try {
            mapper.writeValue(new File(outputPath), reportStructList);

            System.out.println("JSON Report generated at: " + outputPath);

        } catch (IOException e) {
            System.err.println("Failed to write JSON: " + e.getMessage());
        }
    }
}
