package edu.northeastern.reporting;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CSVReportGenerator extends AbstractReportGenerator{

    public CSVReportGenerator(String outputPath) {
        super(outputPath);
    }

    @Override
    public void generate(List<ReportStruct> reportStructList) {
        String[] HEADERS = { "File Path", "Class Name", "Has Code Smell", "Line Numbers" };

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
