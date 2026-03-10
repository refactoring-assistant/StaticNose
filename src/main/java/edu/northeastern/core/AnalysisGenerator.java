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
    private final List<CodeSmell> codeSmells;
    private final IReportGenerator reportGenerator;

    private static final Map<String, BiFunction<List<String>, String, IDetector>> DETECTORS = new HashMap<>();

    static {
        DETECTORS.put("alt-classes", AlternativeClassesDetector::new);
        DETECTORS.put("comments", CommentsDetector::new);
        DETECTORS.put("data-class", DataClassDetector::new);
        DETECTORS.put("data-clumps", DataClumpsDetector::new);
        DETECTORS.put("divergent-change", DivergentChangeDetector::new);
        DETECTORS.put("dup-code", DuplicateCodeDetector::new);
        DETECTORS.put("feature-envy", FeatureEnvyDetector::new);
        DETECTORS.put("intimacy", InappropriateIntimacyDetector::new);
        DETECTORS.put("large-class", LargeClassDetector::new);
        DETECTORS.put("lazy-class", LazyClassDetector::new);
        DETECTORS.put("long-method", LongMethodDetector::new);
        DETECTORS.put("long-params", LongParameterListDetector::new);
        DETECTORS.put("message-chains", MessageChainsDetector::new);
        DETECTORS.put("middle-man", MiddleManDetector::new);
        DETECTORS.put("parallel-hierarchy", ParallelInheritanceHierarchyDetector::new);
        DETECTORS.put("prim-obsession", PrimitiveObsessionDetector::new);
        DETECTORS.put("refused-bequest", RefusedBequestDetector::new);
        DETECTORS.put("shotgun", ShotgunSurgeryDetector::new);
        DETECTORS.put("switch-stmts", SwitchStatementDetector::new);
        DETECTORS.put("temp-field", TemporaryFieldDetector::new);
    }

    public AnalysisGenerator(File sourceFolder, List<CodeSmell> codeSmells, IReportGenerator reportGenerator) {
        this.sourceFolder = sourceFolder;
        this.codeSmells = codeSmells;
        this.reportGenerator = reportGenerator;
    }

    public void start() {
        List<String> javaFilePaths = new ArrayList<>();
        collectJavaFilePaths(sourceFolder, javaFilePaths);

        List<ReportStruct> masterReportList = new ArrayList<>();

        for (CodeSmell smell : codeSmells) {
            String smellKey = smell.toString();

            BiFunction<List<String>, String, IDetector> detectorConstructor = DETECTORS.get(smellKey);

            if(detectorConstructor == null) {
                throw new IllegalArgumentException("No detector found for smell: " + smellKey);
            }

            System.out.println("Running " + smellKey + " detector...");

            IDetector detector = detectorConstructor.apply(javaFilePaths, sourceFolder.toString());
            masterReportList.addAll(detector.run());
        }

        reportGenerator.generate(masterReportList);

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
