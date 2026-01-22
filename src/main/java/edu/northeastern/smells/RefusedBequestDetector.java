package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class RefusedBequestDetector implements IDetector{


    List<String> javaFilePaths;

    public RefusedBequestDetector(List<String> javaFilePaths) {
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
        System.out.println(javaFilePath);
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

        CtTypeReference<?> superClass = type.getSuperclass();
        var superInterfaces = type.getSuperInterfaces();

        // check if inherits at all
        boolean hasSuperClass = (superClass != null && !"java.lang.Object".equals(superClass.getQualifiedName()));
        boolean hasInterfaces = !superInterfaces.isEmpty();

        if (!hasSuperClass && !hasInterfaces) {
            ReportStruct report = new ReportStruct(javaFilePath, type.getSimpleName(), false);
            report.addLineNumber(-1);
            return report;
        }

        for(CtMethod<?> method : type.getMethods()) {
            if(!hasOverrideAnnotation(method)) {
                continue;
            }

            if(method.getBody() == null) continue;

            // check if throws specific non-implementation exceptions
            if(throwsRefusalException(method)) {
                detectedLines.add(method.getPosition().getLine());
            } // check if empty body with void return
            else if (method.getType().getSimpleName().equals("void") && method.getBody().getStatements().isEmpty()) {
                detectedLines.add(method.getPosition().getLine());
            }

            // check for single line throws as well for robustness
            if(method.getBody().getStatements().size() == 1) {
                if(method.getBody().getStatement(0) instanceof CtThrow) {
                    detectedLines.add(method.getPosition().getLine());
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

    private boolean hasOverrideAnnotation(CtMethod<?> method) {
        return method.getAnnotations().stream()
                .anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }

    private boolean throwsRefusalException(CtMethod<?> method) {
        List<CtThrow> throwStmts = method.getElements(new TypeFilter<>(CtThrow.class));

        for(CtThrow t : throwStmts) {
            CtTypeReference<?> exceptionType = t.getThrownExpression().getType();
            if(exceptionType == null) continue;

            String name = exceptionType.getSimpleName();

            if(name.contains("UnsupportedOperation") ||
            name.contains("NotImplemented") ||
            name.contains("IllegalState")) {
                return true;
            }
        }
        return false;
    }
}
