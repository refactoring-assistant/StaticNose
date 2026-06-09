package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import org.jspecify.annotations.NonNull;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class ShotgunSurgeryDetector extends AbstractDetector {

    private final int THRESHOLD_CM; // Changing Methods
    private final int THRESHOLD_CC; // Changing Classes
    private final int THRESHOLD_FAN_OUT; // Dropped for toy examples

    private final Map<String, MethodMetrics> methodRegistry = new HashMap<>();
    private final Map<String, Set<String>> incomingMethodMap = new HashMap<>();
    private final Map<String, Set<String>> incomingClassMap = new HashMap<>();

    public ShotgunSurgeryDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        THRESHOLD_CM = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "THRESHOLD_CM", 2);
        THRESHOLD_CC = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "THRESHOLD_CC", 2);
        THRESHOLD_FAN_OUT = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "THRESHOLD_FAN_OUT", 0);
    }

    @Override
    protected String getSmellName() {
        return "Shotgun Surgery";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> reports = new ArrayList<>();

        for (String signature : methodRegistry.keySet()) {
            MethodMetrics metrics = methodRegistry.get(signature);

            Set<String> callingMethods = incomingMethodMap.getOrDefault(signature, Collections.emptySet());
            int cm = callingMethods.size();

            Set<String> callingClasses = incomingClassMap.getOrDefault(signature, Collections.emptySet());
            int cc = callingClasses.size();

            boolean isShotgunSurgery = (cm >= THRESHOLD_CM) &&
                    (cc >= THRESHOLD_CC) &&
                    (metrics.fanOut >= THRESHOLD_FAN_OUT);

            if (isShotgunSurgery) {
                ReportStruct report = getReportStruct(metrics, cm, cc);
                reports.add(report);
            }
        }

        return reports;
    }

    private @NonNull ReportStruct getReportStruct(MethodMetrics metrics, int cm, int cc) {
        String info = String.format("Shotgun Surgery [CM=%d, CC=%d, Fan-Out=%d]",
                cm, cc, metrics.fanOut);

        ReportStruct report = new ReportStruct(
                getSmellName(),
                metrics.filePath,
                this.inputDirPath,
                metrics.className,
                info
        );
        report.addLineNumber(metrics.lineNum);
        return report;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<CtClass<?>> classesInFile = type.getElements(new TypeFilter<>(CtClass.class));

        for (CtClass<?> ctClass : classesInFile) {
            if (!ctClass.getPosition().isValidPosition()) continue;

            String currentClassName = ctClass.getQualifiedName();

            for (CtMethod<?> method : ctClass.getMethods()) {
                if (method.getBody() == null) continue;

                if (method.isImplicit() || !method.getPosition().isValidPosition()) {
                    continue;
                }

                // FIX 1: Make the current method signature truly unique
                String currentMethodSignature = currentClassName + "#" + method.getSignature();

                if (!isDefinedInInterfaceHierarchy(method)) {
                    int fanOut = calculateFanOut(method, currentClassName);

                    methodRegistry.put(currentMethodSignature, new MethodMetrics(
                            ctClass.getPosition().getFile().getAbsolutePath(),
                            ctClass.getSimpleName(),
                            method.getPosition().getLine(),
                            fanOut
                    ));
                }

                List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));

                for (CtInvocation<?> inv : invocations) {
                    CtExecutableReference<?> targetExec = inv.getExecutable();

                    if (targetExec != null && targetExec.getDeclaringType() != null) {
                        String targetClass = targetExec.getDeclaringType().getQualifiedName();

                        if (!targetClass.startsWith("java.") && !targetClass.equals(currentClassName)) {

                            // FIX 2: Make the target method signature truly unique
                            String targetMethodSig = targetClass + "#" + targetExec.getSignature();

                            incomingMethodMap.putIfAbsent(targetMethodSig, new HashSet<>());
                            incomingMethodMap.get(targetMethodSig).add(currentMethodSignature);

                            incomingClassMap.putIfAbsent(targetMethodSig, new HashSet<>());
                            incomingClassMap.get(targetMethodSig).add(currentClassName);
                        }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private boolean isDefinedInInterfaceHierarchy(CtMethod<?> method) {
        Collection<CtMethod<?>> topDefinitions = method.getTopDefinitions();
        for (CtMethod<?> topDef : topDefinitions) {
            CtType<?> declaringType = topDef.getDeclaringType();
            if (declaringType != null && declaringType.isInterface()) {
                return true;
            }
        }
        return false;
    }

    // Pass in the currentClassName to make FanOut tracking accurate too!
    private int calculateFanOut(CtMethod<?> method, String currentClassName) {
        Set<String> uniqueCalled = new HashSet<>();
        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> inv : invocations) {
            if (inv.getExecutable() != null && inv.getExecutable().getDeclaringType() != null) {
                String targetClass = inv.getExecutable().getDeclaringType().getQualifiedName();
                if (!targetClass.startsWith("java.") && !targetClass.equals(currentClassName)) {
                    // FIX 3: Fully qualify the fan-out set to prevent deduplication
                    uniqueCalled.add(targetClass + "#" + inv.getExecutable().getSignature());
                }
            }
        }
        return uniqueCalled.size();
    }

    private record MethodMetrics(String filePath, String className, int lineNum, int fanOut) {
    }
}