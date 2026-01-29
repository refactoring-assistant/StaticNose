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
    public void generate(List<ReportStruct> reportStructList) {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(new File(outputPath), reportStructList);

            System.out.println("JSON Report generated at: " + outputPath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to write JSON");
        }
    }
}
