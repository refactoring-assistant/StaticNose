package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import org.jspecify.annotations.NonNull;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * This class detects the presence of Data Clumps.
 * A Data Clump code smell is a repeated set of variables that occur
 * and which should be extracted into a parameter object
 */
public class DataClumpsDetector extends AbstractDetector {

    // How many times the set of variables occur for it to be counted
    // as a data clump
    private final int CLUMP_SIZE_THRESHOLD;

    private final Map<String, Map<String, List<Integer>>> fileClumpUsageMap = new HashMap<>();

    public DataClumpsDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        CLUMP_SIZE_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "CLUMP_SIZE_THRESHOLD", 2);
    }

    @Override
    protected String getSmellName() {
        return "Data Clumps";
    }

    @Override
    public List<ReportStruct> run() {
        List<ReportStruct> reports = new ArrayList<>(super.run());

        for (String filePath : fileClumpUsageMap.keySet()) {
            Map<String, List<Integer>> clumpsInFile = fileClumpUsageMap.get(filePath);

            for (Map.Entry<String, List<Integer>> entry : clumpsInFile.entrySet()) {
                String clumpSig = entry.getKey();
                List<Integer> lines = entry.getValue();

                if (lines.size() >= 2) {
                    ReportStruct report = getReportStruct(filePath, clumpSig, lines);
                    reports.add(report);
                }
            }
        }

        return reports;
    }

    private @NonNull ReportStruct getReportStruct(String filePath, String clumpSig, List<Integer> lines) {
        String className = new java.io.File(filePath).getName().replace(".java", "");

        ReportStruct report = new ReportStruct(
                "Data Clumps",
                filePath,
                this.inputDirPath,
                className,
                "Variables: " + clumpSig
        );

        for (Integer line : lines) {
            report.addLineNumber(line);
        }
        return report;
    }

//    @Override
//    protected List<Integer> analyzeType(CtType<?> type) {
//        Set<Integer> detectedLines = new HashSet<>();
//        String filePath = type.getPosition().isValidPosition() ? type.getPosition().getFile().getPath() : "";
//
//        List<CtMethod<?>> methods = new ArrayList<>(type.getAllMethods());
//
//        for (int i = 0; i < methods.size(); i++) {
//            CtMethod<?> m1 = methods.get(i);
//
//            if (m1.getParameters().size() < CLUMP_SIZE_THRESHOLD || !m1.getPosition().isValidPosition()) continue;
//
//            for (int j = i + 1; j < methods.size(); j++) {
//                CtMethod<?> m2 = methods.get(j);
//
//                if (m2.getParameters().size() < CLUMP_SIZE_THRESHOLD || !m2.getPosition().isValidPosition()) continue;
//
//                if (m1.getSimpleName().equals(m2.getSimpleName())) continue;
//
//                if (hasMatchingParams(m1, m2)) {
//                    detectedLines.add(m1.getPosition().getLine());
//                    detectedLines.add(m2.getPosition().getLine());
//                }
//            }
//        }
//
//        List<CtInvocation<?>> invocations = type.getElements(new TypeFilter<>(CtInvocation.class));
//
//        /*
//        TODO: Add logic that checks if method invocations are for the same
//        TODO: method then ignore because multiple invocations for the same
//        TODO: method are not data clumps
//         */
//        for (CtInvocation<?> inv : invocations) {
//            if (inv.getArguments().size() >= CLUMP_SIZE_THRESHOLD) {
//
//                List<String> argNames = extractVariableArguments(inv);
//                if (argNames.size() < CLUMP_SIZE_THRESHOLD) continue;
//
//                Collections.sort(argNames);
//                String clumpSig = String.join(", ", argNames);
//
//                if (!filePath.isEmpty()) {
//                    fileClumpUsageMap.putIfAbsent(filePath, new HashMap<>());
//                    fileClumpUsageMap.get(filePath).putIfAbsent(clumpSig, new ArrayList<>());
//
//                    if (inv.getPosition().isValidPosition()) {
//                        fileClumpUsageMap.get(filePath).get(clumpSig).add(inv.getPosition().getLine());
//                    }
//                }
//            }
//        }
//
//        return new ArrayList<>(detectedLines);
//    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        Set<Integer> detectedLines = new HashSet<>();
        String filePath = type.getPosition().isValidPosition() ? type.getPosition().getFile().getPath() : "";

        // 1. Existing Method Parameter Analysis
        List<CtMethod<?>> methods = new ArrayList<>(type.getAllMethods());
        for (int i = 0; i < methods.size(); i++) {
            CtMethod<?> m1 = methods.get(i);
            if (m1.getParameters().size() < CLUMP_SIZE_THRESHOLD || !m1.getPosition().isValidPosition()) continue;

            for (int j = i + 1; j < methods.size(); j++) {
                CtMethod<?> m2 = methods.get(j);
                if (m2.getParameters().size() < CLUMP_SIZE_THRESHOLD || !m2.getPosition().isValidPosition()) continue;
                if (m1.getSimpleName().equals(m2.getSimpleName())) continue;

                if (hasMatchingParams(m1, m2)) {
                    detectedLines.add(m1.getPosition().getLine());
                    detectedLines.add(m2.getPosition().getLine());
                }
            }
        }

        // 2. Refined Invocation Analysis
        List<CtInvocation<?>> invocations = type.getElements(new TypeFilter<>(CtInvocation.class));

        // Map: ClumpSignature -> Set of Unique Target Method Signatures
        Map<String, Set<String>> clumpToMethodsMap = new HashMap<>();
        // Map: ClumpSignature -> List of Line Numbers where it occurred
        Map<String, List<Integer>> clumpToLinesMap = new HashMap<>();

        for (CtInvocation<?> inv : invocations) {
            if (inv.getArguments().size() >= CLUMP_SIZE_THRESHOLD) {

                // IGNORE: Non-project library calls (String.format, etc)
                if (inv.getExecutable().getDeclaringType() != null) {
                    String declaringType = inv.getExecutable().getDeclaringType().getQualifiedName();
                    if (!isProjectClass(declaringType)) {
                        continue;
                    }
                }

                List<String> argNames = extractVariableArguments(inv);
                if (argNames.size() < CLUMP_SIZE_THRESHOLD) continue;

                Collections.sort(argNames);
                String clumpSig = String.join(", ", argNames);
                String targetMethodSig = inv.getExecutable().getSignature();

                clumpToMethodsMap.putIfAbsent(clumpSig, new HashSet<>());
                clumpToMethodsMap.get(clumpSig).add(targetMethodSig);

                clumpToLinesMap.putIfAbsent(clumpSig, new ArrayList<>());
                if (inv.getPosition().isValidPosition()) {
                    clumpToLinesMap.get(clumpSig).add(inv.getPosition().getLine());
                }
            }
        }

        // Only commit to fileClumpUsageMap if the clump appears across DIFFERENT methods
        if (!filePath.isEmpty()) {
            for (String clumpSig : clumpToMethodsMap.keySet()) {
                // THE FIX: Only flag if the clump is used in 2 or more DIFFERENT method signatures
                if (clumpToMethodsMap.get(clumpSig).size() >= 2) {
                    fileClumpUsageMap.putIfAbsent(filePath, new HashMap<>());
                    fileClumpUsageMap.get(filePath).put(clumpSig, clumpToLinesMap.get(clumpSig));
                }
            }
        }

        return new ArrayList<>(detectedLines);
    }

    /**
     * Check if two methods have matching params
     * @param m1 Method 1
     * @param m2 Method 2
     * @return boolean
     */
    private boolean hasMatchingParams(CtMethod<?> m1, CtMethod<?> m2) {
        int matchCount = 0;
        for (CtParameter<?> p1 : m1.getParameters()) {
            for (CtParameter<?> p2 : m2.getParameters()) {
                // Exact Match (Name + Type)
                if (p1.getSimpleName().equals(p2.getSimpleName()) &&
                        p1.getType().getSimpleName().equals(p2.getType().getSimpleName())) {
                    matchCount++;
                    break;
                }
            }
        }
        return matchCount >= CLUMP_SIZE_THRESHOLD;
    }

    /**
     * Return a list of variable names from a method invocation
     * @param inv The method invocation
     * @return List of variable names in String
     */
    private List<String> extractVariableArguments(CtInvocation<?> inv) {
        List<String> names = new ArrayList<>();
        for (CtExpression<?> arg : inv.getArguments()) {
            if (arg instanceof CtVariableRead) {
                names.add(((CtVariableRead<?>) arg).getVariable().getSimpleName());
            }
        }
        return names;
    }
}