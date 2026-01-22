package edu.northeastern.reporting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CSVReportGenerator implements IReportGenerator{

    @Override
    public void generate(List<ReportStruct> reportStructList) {
        String[] HEADERS = { "File Path", "Class Name", "Has Code Smell", "Line Numbers" };

        File resultDir = new File("results");

        if(!resultDir.exists()){
            resultDir.mkdir();
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = now.format(dtf);
        String outputPath = "results/results-"+timestamp+".csv";

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

        try (FileWriter out = new FileWriter(outputPath);
             CSVPrinter printer = new CSVPrinter(out, csvFormat)) {

            for(ReportStruct report : reportStructList) {

                String lineNumbersString = report.getLineNumbers().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("; "));

                System.out.println(report.getFilePath() + " | " +  report.getClassName());

                printer.printRecord(
                        report.getFilePath(),
                        report.getClassName(),
                        report.getHasCodeSmell(),
                        lineNumbersString
                );
            }

            System.out.println("CSV Report generated at: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to write CSV");
        }

    }

}
