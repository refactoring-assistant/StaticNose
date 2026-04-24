package edu.northeastern.reporting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Abstract Class for report generation. To implement a new report format,
 * it must extend this abstract class and implement the required methods.
 * A reporter must be able to provide the file extension it uses to store its report.
 * It must also contain the actual function to generate the report given
 * a list of ReportStruct objects.
 */
public abstract class AbstractReportGenerator implements IReportGenerator{

    protected final String inputDirPath;
    protected final String outputPath;

    /**
     * Constructor for the abstract report generator.
     * @param inputDirPath the input project path
     */
    public AbstractReportGenerator(String inputDirPath) {
        this.inputDirPath = inputDirPath;
        this.outputPath = prepareOutputPath();
    }

    /**
     * Provides the string of the file extension of the specific
     * report type.
     * @return
     */
    protected abstract String getFileExtension();

    /**
     * This function takes the creates the final filename
     * where the report is saved.
     * @return the full filepath where report is stored.
     */
    public String prepareOutputPath() {
        String reportDirPath = inputDirPath + "/staticnose-report/";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = now.format(dtf);

        return reportDirPath + "report-"+timestamp+getFileExtension();
    }

    @Override
    public abstract void generate(List<ReportStruct> reportStructList);

}
