package edu.northeastern.reporting;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class AbstractReportGenerator implements IReportGenerator{

    protected final String inputDirPath;
    protected String outputPath;

    public AbstractReportGenerator(String inputDirPath) {
        this.inputDirPath = inputDirPath;
        this.outputPath = prepareOutputPath();
    }

    public String prepareOutputPath() {
        String reportDirPath = inputDirPath + "/staticnose-report/";
        File reportDir = FileUtils.safeCreateDir(reportDirPath);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = now.format(dtf);
        String outputPath = reportDirPath + "report-"+timestamp+".json";

        return outputPath;
    }

    @Override
    public abstract void generate(List<ReportStruct> reportStructList);

}
