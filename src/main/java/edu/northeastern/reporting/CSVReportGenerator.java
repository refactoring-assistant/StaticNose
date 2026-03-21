package edu.northeastern.reporting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVReportGenerator extends AbstractReportGenerator {

    public CSVReportGenerator(String outputPath) {
        super(outputPath);
    }

    @Override
    protected String getFileExtension() {
        return ".csv";
    }

    @Override
    public void generate(List<ReportStruct> reportStructList) {
        // 1. Updated headers: Dropped "Class Name" for a purely file-based report
        String[] HEADERS = { "File Path", "Detected Smells" };

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

        // 2. Group strictly by File Path
        Map<String, List<ReportStruct>> groupedReports = reportStructList.stream()
                .collect(Collectors.groupingBy(ReportStruct::getFilePath));

        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();

        // Ensure directories exist
        if (parentDir != null) {
            try {
                FileUtils.safeCreateDir(parentDir.getAbsolutePath());
            } catch (RuntimeException e) {
                System.err.println("Aborting report generation: " + e.getMessage());
                return;
            }
        }

        try (FileWriter out = new FileWriter(outputFile);
             CSVPrinter printer = new CSVPrinter(out, csvFormat)) {

            // Iterate through the grouped file map
            for (Map.Entry<String, List<ReportStruct>> entry : groupedReports.entrySet()) {

                String filePath = entry.getKey();
                List<ReportStruct> fileReports = entry.getValue();

                // 3. Extract unique smells for the ENTIRE file and format them
                String smellsFormatted = fileReports.stream()
                        .map(ReportStruct::getSmellName)
                        .distinct()
                        .map(smell -> "'" + smell + "'")
                        .collect(Collectors.joining(", ", "[", "]"));

                System.out.println(filePath + " -> " + smellsFormatted);

                // 4. Print the single file-level record
                printer.printRecord(
                        filePath,
                        smellsFormatted
                );
            }

            System.out.println("CSV Report generated at: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
        }
    }
}