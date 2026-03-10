package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;

import java.util.*;

public class AlternativeClassesDetector extends AbstractDetector {

    private static final double SIMILARITY_THRESHOLD = 0.90; // 90% overall similarity

    private static final double WEIGHT_FIELDS = 0.20;
    private static final double WEIGHT_VOLUME = 0.10;
    private static final double WEIGHT_METHODS = 0.70;

    private final Map<String, ClassProfile> registry = new HashMap<>();

    public AlternativeClassesDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Alternative Classes with Different Interfaces";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> reports = new ArrayList<>();
        List<String> classNames = new ArrayList<>(registry.keySet());

        for (int i = 0; i < classNames.size(); i++) {
            for (int j = i + 1; j < classNames.size(); j++) {
                String nameA = classNames.get(i);
                String nameB = classNames.get(j);

                ClassProfile classA = registry.get(nameA);
                ClassProfile classB = registry.get(nameB);

                double similarityScore = calculateClassSimilarity(classA, classB);

                if (similarityScore >= SIMILARITY_THRESHOLD) {
                    String info = String.format("Matches '%s' with %.1f%% structural and logical similarity.",
                            nameB, similarityScore * 100);

                    ReportStruct report = new ReportStruct(getSmellName(), classA.filePath, this.inputDirPath, nameA, info);
                    report.addLineNumber(classA.lineNumber);
                    reports.add(report);
                }
            }
        }
        return reports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        if (type instanceof CtClass && type.getPosition().isValidPosition()) {
            ClassProfile profile = buildProfile((CtClass<?>) type);
            if (profile.methods.size() >= 2) {
                registry.put(type.getQualifiedName(), profile);
            }
        }
        return new ArrayList<>();
    }

    private double calculateClassSimilarity(ClassProfile a, ClassProfile b) {
        double volumeScore = calculateVolumeSimilarity(a, b);
        if (volumeScore < 0.5) return 0.0;

        double fieldScore = calculateJaccard(a.fieldTypes, b.fieldTypes);
        double methodScore = calculateMethodSimilarity(a.methods, b.methods);

        return (fieldScore * WEIGHT_FIELDS) +
                (volumeScore * WEIGHT_VOLUME) +
                (methodScore * WEIGHT_METHODS);
    }

    private double calculateVolumeSimilarity(ClassProfile a, ClassProfile b) {
        int maxMethods = Math.max(a.methods.size(), b.methods.size());
        if (maxMethods == 0) return 1.0;
        int minMethods = Math.min(a.methods.size(), b.methods.size());
        return (double) minMethods / maxMethods;
    }

    private double calculateMethodSimilarity(List<MethodProfile> methodsA, List<MethodProfile> methodsB) {
        if (methodsA.isEmpty() && methodsB.isEmpty()) return 1.0;
        if (methodsA.isEmpty() || methodsB.isEmpty()) return 0.0;

        List<MethodProfile> poolB = new ArrayList<>(methodsB);
        double totalScore = 0.0;

        for (MethodProfile mA : methodsA) {
            double bestMatchScore = 0.0;
            MethodProfile bestMatch = null;

            for (MethodProfile mB : poolB) {
                double sigScore = calculateJaccard(mA.signatureTokens, mB.signatureTokens);

                double astScore = calculateType3Similarity(mA.astTokens, mB.astTokens);

                double combinedScore = (sigScore * 0.5) + (astScore * 0.5);

                if (combinedScore > bestMatchScore) {
                    bestMatchScore = combinedScore;
                    bestMatch = mB;
                }
            }

            if (bestMatch != null) {
                totalScore += bestMatchScore;
                poolB.remove(bestMatch);
            }
        }

        int maxMethods = Math.max(methodsA.size(), methodsB.size());
        return totalScore / maxMethods;
    }

    private double calculateJaccard(List<String> listA, List<String> listB) {
        if (listA.isEmpty() && listB.isEmpty()) return 1.0;
        if (listA.isEmpty() || listB.isEmpty()) return 0.0;

        Map<String, Integer> freqA = new HashMap<>();
        for (String s : listA) freqA.put(s, freqA.getOrDefault(s, 0) + 1);

        Map<String, Integer> freqB = new HashMap<>();
        for (String s : listB) freqB.put(s, freqB.getOrDefault(s, 0) + 1);

        int intersection = 0, union = 0;
        Set<String> allKeys = new HashSet<>(freqA.keySet());
        allKeys.addAll(freqB.keySet());

        for (String key : allKeys) {
            int countA = freqA.getOrDefault(key, 0);
            int countB = freqB.getOrDefault(key, 0);
            intersection += Math.min(countA, countB);
            union += Math.max(countA, countB);
        }
        return (double) intersection / union;
    }

    private double calculateType3Similarity(List<String> tokensA, List<String> tokensB) {
        if (tokensA.isEmpty() && tokensB.isEmpty()) return 1.0;
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0;

        int lenA = tokensA.size();
        int lenB = tokensB.size();
        int[][] dp = new int[lenA + 1][lenB + 1];

        for (int i = 0; i <= lenA; i++) dp[i][0] = i;
        for (int j = 0; j <= lenB; j++) dp[0][j] = j;

        for (int i = 1; i <= lenA; i++) {
            for (int j = 1; j <= lenB; j++) {
                int cost = tokensA.get(i - 1).equals(tokensB.get(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        int editDistance = dp[lenA][lenB];
        int maxLength = Math.max(lenA, lenB);

        return 1.0 - ((double) editDistance / maxLength);
    }

    private ClassProfile buildProfile(CtClass<?> ctClass) {
        ClassProfile profile = new ClassProfile();
        profile.filePath = ctClass.getPosition().getFile().getPath();
        profile.lineNumber = ctClass.getPosition().getLine();

        for (CtField<?> field : ctClass.getFields()) {
            if (field.getType() != null) profile.fieldTypes.add(field.getType().getQualifiedName());
        }

        for (CtMethod<?> method : ctClass.getMethods()) {
            if (method.getBody() == null || isOverride(method)) continue;

            MethodProfile mp = new MethodProfile();
            mp.signatureTokens.add(method.getType().getQualifiedName());
            method.getParameters().forEach(p -> mp.signatureTokens.add(p.getType().getQualifiedName()));

            if (method.getBody().getStatements() != null) {
                for (CtStatement stmt : method.getBody().getStatements()) {

                    if (stmt instanceof spoon.reflect.code.CtComment) {
                        continue;
                    }

                    if (!stmt.isImplicit()) {
                        mp.astTokens.add(generateSkeleton(stmt));
                    }
                }
            }
            profile.methods.add(mp);
        }
        return profile;
    }

    private String generateSkeleton(CtStatement stmt) {
        String prefix = stmt.getClass().getSimpleName().replace("Impl", "") + ":";

        MethodSkeletonVisitor visitor = new MethodSkeletonVisitor();
        visitor.scan(stmt);
        return prefix + visitor.getSkeleton();
    }

    private boolean isOverride(CtMethod<?> m) {
        return m.getAnnotations().stream().anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }

    private static class ClassProfile {
        String filePath;
        int lineNumber;
        List<String> fieldTypes = new ArrayList<>();
        List<MethodProfile> methods = new ArrayList<>();
    }

    private static class MethodProfile {
        List<String> signatureTokens = new ArrayList<>();
        List<String> astTokens = new ArrayList<>();
    }

    private static class MethodSkeletonVisitor extends CtScanner {
        private final StringBuilder sb = new StringBuilder();

        public String getSkeleton() { return sb.toString(); }

        @Override public <T> void visitCtVariableRead(spoon.reflect.code.CtVariableRead<T> v) { sb.append("$VAR"); }
        @Override public <T> void visitCtVariableWrite(spoon.reflect.code.CtVariableWrite<T> v) { sb.append("$VAR"); }
        @Override public <T> void visitCtLiteral(spoon.reflect.code.CtLiteral<T> l) { sb.append("$LIT"); }
        @Override public <T> void visitCtLocalVariable(spoon.reflect.code.CtLocalVariable<T> v) { scan(v.getDefaultExpression()); }

        @Override public <T> void visitCtBinaryOperator(spoon.reflect.code.CtBinaryOperator<T> op) {
            sb.append("(");
            scan(op.getLeftHandOperand());
            sb.append(op.getKind());
            scan(op.getRightHandOperand());
            sb.append(")");
        }

        @Override public <T> void visitCtInvocation(spoon.reflect.code.CtInvocation<T> inv) {
            if (inv.getExecutable() != null) {
                sb.append("CALL(").append(inv.getExecutable().getSimpleName()).append(")");
            }
        }
    }
}