package edu.northeastern.reporting;

import java.util.List;

public interface IReportGenerator {

    /**
     * This function takes in a list of ReportStruct objects and then
     * compiles them together in a specific file format. This function
     * is responsible for writing to the actual file.
     * @param reportStructList The list of ReportStruct objects to generate a report from
     */
    void generate(List<ReportStruct> reportStructList);

}
