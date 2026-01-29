package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDetector implements IDetector{

    protected final List<String> javaFilePaths;
    protected String inputDirPath;

    public AbstractDetector(List<String> javaFilePaths, String inputDirPath) {
        this.javaFilePaths = javaFilePaths;
        this.inputDirPath = inputDirPath;
    }

    @Override
    public List<ReportStruct> run() {
        List<ReportStruct> reportStructList = new ArrayList<>();
        for(String javaFilePath : javaFilePaths) {
            reportStructList.addAll(analyzeJavaFile(javaFilePath));
        }
        return reportStructList;
    }

    private List<ReportStruct> analyzeJavaFile(String javaFilePath) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(javaFilePath);
        launcher.getEnvironment().setComplianceLevel(17);

        configureLauncher(launcher);

        launcher.buildModel();
        CtModel model = launcher.getModel();

        List<ReportStruct> fileReportStructList = new ArrayList<>();

        for(CtType<?> type : model.getAllTypes()) {
            List<Integer> detectedLines = analyzeType(type);

            boolean hasCodeSmell = !detectedLines.isEmpty();

            ReportStruct report = new ReportStruct(
                    javaFilePath,
                    this.inputDirPath,
                    type.getSimpleName(),
                    hasCodeSmell
            );

            if(hasCodeSmell) {
                report.addLineNumbers(detectedLines);
            } else {
                report.addLineNumber(-1);
            }

            fileReportStructList.add(report);
        }

        return fileReportStructList;
    }

    protected void configureLauncher(Launcher launcher) {

    }

    protected abstract List<Integer> analyzeType(CtType<?> type);

}
