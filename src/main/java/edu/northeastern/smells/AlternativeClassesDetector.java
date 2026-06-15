package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

import static edu.northeastern.utils.AstNormalizer.generateSkeleton;

public class AlternativeClassesDetector extends AbstractDetector {

    // total similarity threshold for volume + field + method
    private final double SIMILARITY_THRESHOLD;

    // weight assigned to each metric calculated when calculating
    // the final class similarity score
    private final double WEIGHT_FIELDS;
    private final double WEIGHT_VOLUME;
    private final double WEIGHT_METHODS;

    // --- NEW FLAG ---
    // If true, only compares classes that live in the exact same .java file
    // If false, compares every class in the project against every other class
    private boolean singleFileMode = true;

    private final Map<String, ClassProfile> registry = new HashMap<>();

    public AlternativeClassesDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        SIMILARITY_THRESHOLD = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "SIMILARITY_THRESHOLD", 0.75);
        WEIGHT_FIELDS = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "WEIGHT_FIELDS", 0.20);
        WEIGHT_VOLUME = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "WEIGHT_VOLUME", 0.10);
        WEIGHT_METHODS = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "WEIGHT_METHODS", 0.70);
    }

    // --- NEW SETTER ---
    public void setSingleFileMode(boolean singleFileMode) {
        this.singleFileMode = singleFileMode;
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

                // --- NEW LOGIC: Enforce the boundary flag ---
                if (singleFileMode) {
                    if (!classA.filePath.equals(classB.filePath)) {
                        continue; // Skip comparing if they are from different files
                    }
                }

                // --- NEW LOGIC: Prevent punishing polymorphism ---
                // If they share a common interface or superclass, they are explicitly designed
                // to be polymorphic. Therefore, they are NOT "Alternative Classes with DIFFERENT Interfaces"
                boolean shareSuperType = false;
                for (String sType : classA.superTypes) {
                    if (classB.superTypes.contains(sType) && !sType.equals("java.lang.Object")) {
                        shareSuperType = true;
                        break;
                    }
                }

                if (shareSuperType) {
                    continue; 
                }

                double similarityScore = calculateClassSimilarity(classA, classB);

                if (similarityScore >= SIMILARITY_THRESHOLD) {
                    String info = String.format("Match: '%s', Similarity Score: %.1f%%",
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
        // --- UPDATED LOGIC: Gather all nested/peer classes in the file ---
        // This is strictly required for 'singleFileMode' to have anything to compare!
        List<CtClass<?>> classesInFile = type.getElements(new TypeFilter<>(CtClass.class));

        for (CtClass<?> ctClass : classesInFile) {
            if (ctClass.getPosition().isValidPosition()) {
                ClassProfile profile = buildProfile(ctClass);
                // Only register classes with enough methods to actually compare
                if (profile.methods.size() >= 2) {
                    registry.put(ctClass.getQualifiedName(), profile);
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Calculate the Class Similarity Score for two classes
     * The Class Similarity score is a combined metric that
     * is calculated based on:
     * 1. The Volume Similarity Score
     * 2. The Method Similarity Score
     * 3. The Field Similarity Score
     * These scores are summed together based on the assigned weights for
     * each metric.
     * @param a The class to compare against
     * @param b The other class to compare against
     * @return The CSS
     */
    private double calculateClassSimilarity(ClassProfile a, ClassProfile b) {
        double volumeScore = calculateVolumeSimilarity(a, b);
        if (volumeScore < 0.5) return 0.0;

        double fieldScore = calculateJaccard(a.fieldTypes, b.fieldTypes);
        double methodScore = calculateMethodSimilarity(a.methods, b.methods);

        return (fieldScore * WEIGHT_FIELDS) +
                (volumeScore * WEIGHT_VOLUME) +
                (methodScore * WEIGHT_METHODS);
    }

    /**
     * The Volume Similarity Score is the size ratio for two classes
     * minimum of the number of methods between A and B by the
     * maximum of the number of methods between A and B
     * @param a Class profile of A
     * @param b Class profile of B
     * @return The VSS
     */
    private double calculateVolumeSimilarity(ClassProfile a, ClassProfile b) {
        int maxMethods = Math.max(a.methods.size(), b.methods.size());
        if (maxMethods == 0) return 1.0;
        int minMethods = Math.min(a.methods.size(), b.methods.size());
        return (double) minMethods / maxMethods;
    }

    /**
     * The Method Similarity Score is a combination of the
     * Jaccard Similarity Index for the parameters of two methods
     * and the Levenshtein Distance for the AST tokens of two methods
     * at 50% weightage for each.
     * It creates 1-1 pairs of methods between two classes based
     * on the two metrics above and then returns the
     * sum of all the scores divided by the maximum number of methods
     * in the class
     * @param methodsA Method A to compare
     * @param methodsB Method B to compare
     * @return the MSS
     */
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

                double astScore = calculateLevenshteinDistance(mA.astTokens, mB.astTokens);

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

    /**
     * Calculates the Jaccard Similarity Index for two method signatures
     * JSI is given as | A ∩ B | / | A ∪ B |
     * @param listA Signature of method A
     * @param listB Signature of method B
     * @return The JSI
     */
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

    /**
     * Calculate the Levenshtein Distance or Edit Distance.
     * The fewest number of edits required to be made in the tokens of method A
     * to achieve the tokens of method B
     * @param tokensA The AST tokens of Method A
     * @param tokensB The AST tokens of Method B
     * @return the LD
     */
    private double calculateLevenshteinDistance(List<String> tokensA, List<String> tokensB) {
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

    /**
     * Builds a custom AST that is normalized based on the given Class AST.
     * It strips away all non-functional aspects of code like comments
     * and creates a class profile for a given class that includes
     * fields that are easier to compare for other metrics
     * @param ctClass The class whose custom profile needs to be created
     * @return the ClassProfile of the class
     */
    private ClassProfile buildProfile(CtClass<?> ctClass) {
        ClassProfile profile = new ClassProfile();
        profile.filePath = ctClass.getPosition().getFile().getPath();
        profile.lineNumber = ctClass.getPosition().getLine();

        if (ctClass.getSuperclass() != null) {
            profile.superTypes.add(ctClass.getSuperclass().getQualifiedName());
        }
        for (spoon.reflect.reference.CtTypeReference<?> ref : ctClass.getSuperInterfaces()) {
            profile.superTypes.add(ref.getQualifiedName());
        }

        for (CtField<?> field : ctClass.getFields()) {
            if (field.isStatic()) continue;
            if (field.getType() != null) profile.fieldTypes.add(field.getType().getQualifiedName());
        }

        for (CtMethod<?> method : ctClass.getMethods()) {
            if (method.getBody() == null) continue;

            MethodProfile mp = new MethodProfile();
            mp.signatureTokens.add(method.getType().getQualifiedName());

            method.getParameters().forEach(p -> {
                String typeName = p.getType().getQualifiedName();
                if (isProjectClass(typeName)) {
                    mp.signatureTokens.add("CustomObject"); // Mask it!
                } else {
                    mp.signatureTokens.add(typeName);
                }
            });

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

    /**
     * Check if a method is overriding an interface or abstract method
     * For ACDI, since they already override, a method, we do not need to add this
     * to our comparison
     * @param m The method to check if it is overriding
     * @return boolean
     */
    private boolean isOverride(CtMethod<?> m) {
        return m.getAnnotations().stream().anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }

    /**
     * The Class profile of a class
     * which stores important information required to calculate
     * the metrics for Alternative Classes Detector
     */
    private static class ClassProfile {
        String filePath;
        int lineNumber;
        final List<String> fieldTypes = new ArrayList<>();
        final List<MethodProfile> methods = new ArrayList<>();
        final Set<String> superTypes = new HashSet<>();
    }

    /**
     * The Method profile for a method
     * which stores important information required to calculate
     * the method similarity score
     */
    private static class MethodProfile {
        final List<String> signatureTokens = new ArrayList<>();
        final List<String> astTokens = new ArrayList<>();
    }
}