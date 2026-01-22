package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.*;

import java.util.ArrayList;
import java.util.List;

public class LongParameterListDetector implements IDetector{

    List<String> javaFilePaths;

    public LongParameterListDetector(List<String> javaFilePaths) {
        this.javaFilePaths = javaFilePaths;
    }

    @Override
    public List<ReportStruct> run() {
        List<ReportStruct> reportStructList = new ArrayList<>();

        for(String javaFilePath: javaFilePaths) {
            List<ReportStruct> fileReportStructList = analyzeJavaFile(javaFilePath);
            reportStructList.addAll(fileReportStructList);
        }

        return reportStructList;
    }

    private List<ReportStruct> analyzeJavaFile(String javaFilePath) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(javaFilePath);
        launcher.getEnvironment().setComplianceLevel(17);
        launcher.buildModel();

        CtModel model = launcher.getModel();
        List<ReportStruct> fileReportStructList = new ArrayList<>();

        for (CtType<?> type : model.getAllTypes()) {

            ReportStruct classReportStruct = analyzeClass(type, javaFilePath);

            if(classReportStruct != null) {
                fileReportStructList.add(classReportStruct);
            }
        }

        return fileReportStructList;
    }

    private ReportStruct analyzeClass(CtType<?> type, String javaFilePath) {
        List<Integer> detectedLines = new ArrayList<>();

        for(CtMethod<?> method : type.getMethods()) {
            int paramCount = method.getParameters().size();

            if(paramCount > 3) {
                if(isOverridden(method)) {
                    continue;
                }
                detectedLines.add(method.getPosition().getLine());
            }
        }

        if (type instanceof CtClass) {
            CtClass<?> clazz = (CtClass<?>) type;

            for (CtConstructor<?> constructor : clazz.getConstructors()) {
                if (constructor.getParameters().size() > 5) {
                    if (constructor.getPosition().isValidPosition()) {
                        detectedLines.add(constructor.getPosition().getLine());
                    }
                }
            }
        }
        boolean hasCodeSmell = !detectedLines.isEmpty();

        ReportStruct report = new ReportStruct(javaFilePath, type.getSimpleName(), hasCodeSmell);

        if(hasCodeSmell) {
            report.addLineNumbers(detectedLines);
        } else {
            report.addLineNumber(-1);
        }

        return report;
    }

    private boolean isOverridden(CtMethod<?> method) {
        for(CtAnnotation<?> annotation : method.getAnnotations()) {
            if(annotation.getAnnotationType().getSimpleName().equals("Override")) {
                return true;
            }
        }
        return false;
    }
}
