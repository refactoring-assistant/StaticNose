package edu.northeastern.core;

import edu.northeastern.cli.CodeSmell;
import edu.northeastern.reporting.IReportGenerator;
import edu.northeastern.reporting.ReportStruct;
import edu.northeastern.smells.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalysisGenerator {
    private final File sourceFolder;
    private final CodeSmell codeSmell;
    private final IReportGenerator reportGenerator;

    public AnalysisGenerator(File sourceFolder, CodeSmell codeSmell, IReportGenerator reportGenerator) {
        this.sourceFolder = sourceFolder;
        this.codeSmell = codeSmell;
        this.reportGenerator = reportGenerator;
    }

    public void start() {
        // 1. Get all java files

        List<String> javaFilePaths = new ArrayList<>();
        collectJavaFilePaths(sourceFolder, javaFilePaths);


        // 3. after you get a file, based on the code smell you recieved from the command
        //    line interface, make the appropriate detector and send the file.

        List<ReportStruct> reportStructList = new ArrayList<>();

        if(codeSmell.toString().equals("middle-man")) {
            IDetector middleManDetector = new MiddleManDetector(javaFilePaths);
            reportStructList = middleManDetector.run();
        } else if(codeSmell.toString().equals("feature-envy")) {
            IDetector featureEnvyDetector = new FeatureEnvyDetector(javaFilePaths);
            reportStructList = featureEnvyDetector.run();
        } else if(codeSmell.toString().equals("long-method")) {
            IDetector longMethodDetector = new LongMethodDetector(javaFilePaths);
            reportStructList = longMethodDetector.run();
        } else if(codeSmell.toString().equals("temp-field")) {
            IDetector tempFieldDetector = new TemporaryFieldDetector(javaFilePaths);
            reportStructList = tempFieldDetector.run();
        } else if(codeSmell.toString().equals("long-params")) {
            IDetector longParamsDetector = new LongParameterListDetector(javaFilePaths);
            reportStructList = longParamsDetector.run();
        } else if(codeSmell.toString().equals("refused-bequest")) {
            IDetector refusedBequestDetector = new RefusedBequestDetector(javaFilePaths);
            reportStructList = refusedBequestDetector.run();
        } else if(codeSmell.toString().equals("comments")) {
            IDetector commentsDetector = new CommentsDetector(javaFilePaths);
            reportStructList = commentsDetector.run();
        }

        // 4. the detector should run analyzeFile and return a ReportStruct which contains
        //    the report of the file
        // 5. Once you get a list of ReportStructs send it to the ReportGenerator

        reportGenerator.generate(reportStructList);

    }

    protected void collectJavaFilePaths(File file, List<String> result) {

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                collectJavaFilePaths(f, result);
            }
        } else if (file.isFile() && file.getName().endsWith(".java")) {
            result.add(file.getAbsolutePath());
        }
    }


}
