package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MiddleManDetector implements IDetector {
    List<String> javaFilePaths;

    public MiddleManDetector(List<String> javaFilePaths) {
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

        System.out.println(fileReportStructList.get(0).getFilePath());

        return fileReportStructList;
    }

    private ReportStruct analyzeClass(CtType<?> type, String javaFilePath) {
        // fan out

        Set<CtTypeReference<?>> referencedTypes = type.getReferencedTypes();
        long fanOut = referencedTypes.stream()
                .filter(ref -> !ref.isPrimitive())
                .filter(ref -> !ref.getQualifiedName().equals(type.getQualifiedName()))
                .count();

        // single line ratio

        Set<CtMethod<?>> methods = type.getMethods();
        if(methods.isEmpty()) {
            ReportStruct report = new ReportStruct(javaFilePath, type.getSimpleName(), false);
            report.addLineNumber(-1);
            return report;
        }

        int singleLineDelegates = 0;

        for(CtMethod<?> method : methods) {
            if(method.getBody() == null) continue;

            List<CtStatement> statements = method.getBody().getStatements();
            int statementCount = statements.size();

            if(statementCount == 1) {
                CtStatement stmt = statements.get(0);
                if(isInvocataion(stmt) || isReturnInvocation(stmt)) {
                    singleLineDelegates++;
                }
            }

            // two line statements (assign and return)
            else if (statementCount == 2) {
                CtStatement first = statements.get(0);
                CtStatement second = statements.get(1);

                if(isAssignInvocationAndReturn(first, second)) {
                    singleLineDelegates++;
                }
            }
        }

        double ratio = (double) singleLineDelegates / methods.size();

        // code smell check
        boolean hasCodeSmell = false;
        if(ratio > 0.5 && fanOut > 0) {
            hasCodeSmell = true;
        }

        ReportStruct report = new ReportStruct(javaFilePath, type.getSimpleName(), hasCodeSmell);


        if(hasCodeSmell) {
            report.addLineNumber(1);
        } else {
            report.addLineNumber(-1);
        }

        return report;
    }

    private boolean isInvocataion(CtStatement stmt) {
        return stmt instanceof CtInvocation;
    }

    private boolean isReturnInvocation(CtStatement stmt) {
        if(stmt instanceof CtReturn) {
            CtReturn<?> ret = (CtReturn<?>) stmt;
            return ret.getReturnedExpression() instanceof CtInvocation;
        }
        return false;
    }

    private boolean isAssignInvocationAndReturn(CtStatement first, CtStatement second) {
        if(!(first instanceof  CtLocalVariable)) return false;

        if(!(second instanceof CtReturn)) return false;

        CtLocalVariable<?> local = (CtLocalVariable<?>) first;
        CtReturn<?> ret = (CtReturn<?>) second;

        if(!(local.getDefaultExpression() instanceof CtInvocation)) return false;

        if(!(ret.getReturnedExpression() instanceof CtVariableRead)) return false;

        CtVariableRead<?> read = (CtVariableRead<?>) ret.getReturnedExpression();
        return read.getVariable().getDeclaration() == local;
    }

}
