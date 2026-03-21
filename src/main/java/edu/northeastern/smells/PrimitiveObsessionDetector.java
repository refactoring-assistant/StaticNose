package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class PrimitiveObsessionDetector extends AbstractDetector{

    private static final int VALIDATION_DISTRIBUTION_THRESHOLD = 2;
    private static final int MAX_PRIMITIVE_FIELDS = 4;

    // If more than 50% of method pairs share ZERO primitive fields, the class is highly separable.
    private static final double MAX_DISJOINT_RATIO = 0.5;

    public PrimitiveObsessionDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Primitive Obsession";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        Set<Integer> detectedLines = new HashSet<>();

        List<CtField<?>> candidateFields = new ArrayList<>();
        for (CtField<?> field : type.getFields()) {
            if (isPrimitiveOrString(field.getType())) {
                candidateFields.add(field);
            }
        }

        if (candidateFields.isEmpty()) return new ArrayList<>();

        // 1. Traditional Volume Check (Data Clumps)
        if (candidateFields.size() > MAX_PRIMITIVE_FIELDS) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        // 2. Leaking Validation Check (Behavioral)
        for (CtField<?> field : candidateFields) {
            if (isFieldValidationLeaking(field, type)) {
                if (field.getPosition().isValidPosition()) {
                    detectedLines.add(field.getPosition().getLine());
                }
            }
        }

        // 3. NEW: Semantic Cohesion Analysis (Separability Check via Set Math)
        checkPrimitiveCohesion(type, candidateFields, detectedLines);

        return new ArrayList<>(detectedLines);
    }

    /**
     * Calculates the Jaccard Similarity (Intersection / Union) of primitive fields
     * accessed across all methods to determine if the class contains separable concepts.
     */
    private void checkPrimitiveCohesion(CtType<?> type, List<CtField<?>> candidateFields, Set<Integer> detectedLines) {
        // We need at least 2 primitive fields to even consider them "separable"
        if (candidateFields.size() < 2) return;

        Map<CtMethod<?>, Set<CtField<?>>> methodFieldUsage = new HashMap<>();

        // Step A: Map which primitive fields are used by which methods
        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() == null) continue;

            Set<CtField<?>> usedPrimitives = new HashSet<>();
            List<CtFieldAccess<?>> accesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));

            for (CtFieldAccess<?> access : accesses) {
                CtField<?> matchedField = getCandidateField(access, candidateFields);
                if (matchedField != null) {
                    usedPrimitives.add(matchedField);
                }
            }

            // Only track methods that actually touch primitive fields
            if (!usedPrimitives.isEmpty()) {
                methodFieldUsage.put(method, usedPrimitives);
            }
        }

        List<CtMethod<?>> methods = new ArrayList<>(methodFieldUsage.keySet());
        if (methods.size() < 2) return; // Need at least two methods to compare

        int disjointPairs = 0;
        int totalPairs = 0;
        double totalJaccardScore = 0.0;

        // Step B: Compare every method against every other method (Bipartite Graph Analysis)
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                Set<CtField<?>> setA = methodFieldUsage.get(methods.get(i));
                Set<CtField<?>> setB = methodFieldUsage.get(methods.get(j));

                // Intersection: Fields used by BOTH methods
                Set<CtField<?>> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);

                // Union: Total unique fields used by EITHER method
                Set<CtField<?>> union = new HashSet<>(setA);
                union.addAll(setB);

                if (intersection.isEmpty()) {
                    disjointPairs++; // They touch completely different primitive data
                }

                totalJaccardScore += (double) intersection.size() / union.size();
                totalPairs++;
            }
        }

        if (totalPairs == 0) return;

        double avgJaccardScore = totalJaccardScore / totalPairs;
        double disjointRatio = (double) disjointPairs / totalPairs;

        // Step C: The Dynamic Threshold Evaluation
        // If a class has 10 primitive fields, we expect lower cohesion naturally.
        // Therefore, the threshold scales dynamically: 1.0 / (number of primitive fields).
        double dynamicJaccardThreshold = 1.0 / candidateFields.size();

        // Flag as a smell if the cohesion is mathematically terrible, OR
        // if more than half of the methods don't share ANY primitive data with each other.
        if (avgJaccardScore < dynamicJaccardThreshold || disjointRatio > MAX_DISJOINT_RATIO) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }
    }

    private CtField<?> getCandidateField(CtFieldAccess<?> access, List<CtField<?>> candidateFields) {
        String accessName = access.getVariable().getSimpleName();
        for (CtField<?> field : candidateFields) {
            if (field.getSimpleName().equals(accessName)) {
                return field;
            }
        }
        return null;
    }

    // --- Existing Helper Methods Below ---

    private boolean isFieldValidationLeaking(CtField<?> field, CtType<?> type) {
        Set<String> validatingMethods = new HashSet<>();
        String fieldName = field.getSimpleName();

        List<CtFieldAccess<?>> accesses = type.getElements(new TypeFilter<>(CtFieldAccess.class));

        for (CtFieldAccess<?> access : accesses) {
            if (!access.getVariable().getSimpleName().equals(fieldName)) continue;

            if (isUsedInValidationContext(access)) {
                String locationName = getParentMethodOrConstructorName(access);
                validatingMethods.add(locationName);
            }
        }

        return validatingMethods.size() >= VALIDATION_DISTRIBUTION_THRESHOLD;
    }

    private boolean isUsedInValidationContext(CtFieldAccess<?> access) {
        CtElement parent = access.getParent();

        if (parent instanceof CtBinaryOperator<?> op) {
            BinaryOperatorKind kind = op.getKind();
            return kind == BinaryOperatorKind.EQ || kind == BinaryOperatorKind.NE ||
                    kind == BinaryOperatorKind.GT || kind == BinaryOperatorKind.LT ||
                    kind == BinaryOperatorKind.GE || kind == BinaryOperatorKind.LE;
        }

        if (parent instanceof CtInvocation<?> inv) {
            if (inv.getTarget() == access) {
                String methodName = inv.getExecutable().getSimpleName();
                return methodName.equals("matches") ||
                        methodName.equals("contains") ||
                        methodName.equals("isEmpty") ||
                        methodName.equals("isBlank") ||
                        methodName.startsWith("check");
            }
            for (Object arg : inv.getArguments()) {
                if (arg == access) {
                    String methodName = inv.getExecutable().getSimpleName();
                    return methodName.startsWith("check") ||
                            methodName.startsWith("validate") ||
                            methodName.equals("isEmpty") ||
                            methodName.equals("isBlank");
                }
            }
        }

        if (parent instanceof CtIf ifStmt) {
            return ifStmt.getCondition() == access;
        }

        return false;
    }

    private String getParentMethodOrConstructorName(CtElement element) {
        CtMethod<?> method = element.getParent(CtMethod.class);
        if (method != null) return method.getSimpleName();

        CtConstructor<?> constructor = element.getParent(CtConstructor.class);
        if (constructor != null) return "<init>";

        return "unknown";
    }

    private boolean isPrimitiveOrString(CtTypeReference<?> typeRef) {
        if (typeRef == null) return false;
        if (typeRef.isPrimitive()) return true;
        return "String".equals(typeRef.getSimpleName());
    }
}