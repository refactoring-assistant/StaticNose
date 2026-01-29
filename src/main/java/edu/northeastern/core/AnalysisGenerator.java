package edu.northeastern.core;

import edu.northeastern.cli.CodeSmell;
import edu.northeastern.reporting.IReportGenerator;
import edu.northeastern.reporting.ReportStruct;
import edu.northeastern.smells.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class AnalysisGenerator {
    private final File sourceFolder;
    private final CodeSmell codeSmell;
    private final IReportGenerator reportGenerator;

    private static final Map<String, BiFunction<List<String>, String, IDetector>> DETECTORS = new HashMap<>();

    static {
        DETECTORS.put("middle-man", MiddleManDetector::new);
        DETECTORS.put("feature-envy", FeatureEnvyDetector::new);
        DETECTORS.put("long-method", LongMethodDetector::new);
        DETECTORS.put("temp-field", TemporaryFieldDetector::new);
        DETECTORS.put("long-params", LongMethodDetector::new);
        DETECTORS.put("refused-bequest", RefusedBequestDetector::new);
        DETECTORS.put("comments", CommentsDetector::new);
    }

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

        BiFunction<List<String>, String, IDetector> detectorConstructor = DETECTORS.get(codeSmell.toString());

        if(detectorConstructor == null) {
            throw new IllegalArgumentException("No detector found for smell: " + codeSmell.toString());
        }

        IDetector detector = detectorConstructor.apply(javaFilePaths, sourceFolder.toString());
        List<ReportStruct> reportStructList = detector.run();

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
