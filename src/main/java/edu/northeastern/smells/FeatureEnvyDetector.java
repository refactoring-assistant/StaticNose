package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class FeatureEnvyDetector implements IDetector{

    List<String> javaFilePaths;

    public FeatureEnvyDetector(List<String> javaFilePaths) {
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

            int internalAccessCount = 0;
            int externalAccessCount = 0;
            // Map<String, Integer> externalAccessMap = new HashMap<>();

            // scan for field access

            List<CtFieldAccess<?>> fieldAccesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));
            for(CtFieldAccess<?> access : fieldAccesses) {
                if(access.getVariable().isStatic()) continue;

                // if field accessed is its own class'

                CtTypeReference<?> declaringType = access.getVariable().getDeclaringType();
                if(declaringType == null) continue;

                if(declaringType.getQualifiedName().equals(type.getQualifiedName())) {
                    internalAccessCount++;
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(!isStandardLibrary(targetName)) {
                        externalAccessCount++;
                    }
                }
            }

            // check for method invocations (only getters)
            List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));

            for(CtInvocation<?> invocation: invocations) {
                // ignores utility calls
                if(invocation.getExecutable().isStatic()) continue;

                CtTypeReference<?> declaringType = invocation.getExecutable().getDeclaringType();
                if(declaringType == null) continue;
                String targetName = declaringType.getQualifiedName();

                if(targetName.equals(type.getQualifiedName())) {
                    internalAccessCount++;
                } else if(!isStandardLibrary(targetName)) {
                    if(isGetter(invocation)) {
                        externalAccessCount++;
                    }
                }
            }

            // calculate scores

            if(externalAccessCount > internalAccessCount) {
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

    private boolean isGetter(CtInvocation<?> invocation) {
        String name = invocation.getExecutable().getSimpleName();
        int paramCount = invocation.getArguments().size();
        CtTypeReference<?> returnType = invocation.getExecutable().getType();

        if(paramCount > 0) return false;

        if(returnType != null && returnType.getSimpleName().equals("void")) return false;

        return name.startsWith("get") || name.startsWith("is") || name.startsWith("has");
    }

    private boolean isStandardLibrary(String qualifiedName) {
        return qualifiedName.startsWith("java.") ||
                qualifiedName.startsWith("javax.") ||
                qualifiedName.startsWith("sun.") ||
                qualifiedName.startsWith("jdk.");
    }
}
