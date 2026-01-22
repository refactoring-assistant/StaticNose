package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class LongMethodDetector implements IDetector{

    List<String> javaFilePaths;

    public LongMethodDetector(List<String> javaFilePaths) {
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
            if(method.getBody() == null) continue;

            int lloc = calculateLLOC(method);

            int complexity = calculateCyclomaticComplexity(method);

            if(lloc > 30 && complexity > 5) {
                detectedLines.add(method.getPosition().getLine());
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

    // logical lines of code
    private int calculateLLOC(CtMethod<?> method) {
        List<CtStatement> statements = method.getBody().getStatements();

        int lloc = 0;
        for(CtStatement stmt : statements) {
            if(!(stmt instanceof CtBlock)) {
                lloc++;
            }
        }

        return lloc;
    }

    private int calculateCyclomaticComplexity(CtMethod<?> method) {
        int complexity = 1;

        List<Class<?>> decisionNodes = List.of(
                CtIf.class,
                CtFor.class,
                CtForEach.class,
                CtWhile.class,
                CtDo.class,
                CtCase.class,
                CtConditional.class
        );

        for(Class<?> node : decisionNodes) {
            complexity += method.getElements(new TypeFilter<>(node)).size();
        }

        for(CtBinaryOperator<?> op : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            if(op.getKind() == BinaryOperatorKind.AND || op.getKind() == BinaryOperatorKind.OR) {
                complexity++;
            }
        }

        return complexity;
    }

}
