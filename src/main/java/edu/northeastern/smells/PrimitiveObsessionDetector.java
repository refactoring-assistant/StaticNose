package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class PrimitiveObsessionDetector extends AbstractDetector{

    private final int VALIDATION_DISTRIBUTION_THRESHOLD;
    private final int MAX_PRIMITIVE_FIELDS;
    private final int COHESION_CANDIDATE_FIELDS;
    private final double MAX_DISJOINT_RATIO;

    public PrimitiveObsessionDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        VALIDATION_DISTRIBUTION_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "VALIDATION_DISTRIBUTION_THRESHOLD", 1);
        MAX_PRIMITIVE_FIELDS = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_PRIMITIVE_FIELDS", 3);
        COHESION_CANDIDATE_FIELDS = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "COHESION_CANDIDATE_FIELDS", 3);
        MAX_DISJOINT_RATIO = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "MAX_DISJOINT_RATIO", 0.5);
    }

    @Override
    protected String getSmellName() {
        return "Primitive Obsession";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> topLevelType) {
        Set<Integer> detectedLines = new HashSet<>();

        List<CtType<?>> allTypesToAnalyze = topLevelType.getElements(new TypeFilter<>(CtType.class));

        for (CtType<?> type : allTypesToAnalyze) {

            List<CtField<?>> candidateFields = new ArrayList<>();
            for (CtField<?> field : type.getFields()) {
                if (field.isStatic()) {
                    continue;
                }

                if (isPrimitiveOrString(field.getType())) {
                    candidateFields.add(field);
                }
            }

            if (candidateFields.isEmpty()) continue;

            if (candidateFields.size() >= MAX_PRIMITIVE_FIELDS) {
                if (type.getPosition().isValidPosition()) {
                    detectedLines.add(type.getPosition().getLine());
                }
            }

            for (CtField<?> field : candidateFields) {
                if (isFieldValidationLeaking(field, type)) {
                    if (field.getPosition().isValidPosition()) {
                        detectedLines.add(field.getPosition().getLine());
                    }
                }
            }

            checkPrimitiveCohesion(type, candidateFields, detectedLines);

            if (isArrayUsedAsStruct(type)) {
                if (type.getPosition().isValidPosition()) {
                    detectedLines.add(type.getPosition().getLine());
                }
            }
        }

        return new ArrayList<>(detectedLines);
    }

    /**
     * Calculates the Jaccard Similarity (Intersection / Union) of primitive fields
     * accessed across all methods to determine if the class contains separable concepts.
     */
    private void checkPrimitiveCohesion(CtType<?> type, List<CtField<?>> candidateFields, Set<Integer> detectedLines) {
        if (candidateFields.size() < COHESION_CANDIDATE_FIELDS) return;

        Map<CtMethod<?>, Set<CtField<?>>> methodFieldUsage = new HashMap<>();

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

            if (!usedPrimitives.isEmpty()) {
                methodFieldUsage.put(method, usedPrimitives);
            }
        }

        List<CtMethod<?>> methods = new ArrayList<>(methodFieldUsage.keySet());
        if (methods.size() < 2) return;

        int disjointPairs = 0;
        int totalPairs = 0;
        double totalJaccardScore = 0.0;

        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                Set<CtField<?>> setA = methodFieldUsage.get(methods.get(i));
                Set<CtField<?>> setB = methodFieldUsage.get(methods.get(j));

                Set<CtField<?>> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);

                Set<CtField<?>> union = new HashSet<>(setA);
                union.addAll(setB);

                if (intersection.isEmpty()) {
                    disjointPairs++;
                }

                totalJaccardScore += (double) intersection.size() / union.size();
                totalPairs++;
            }
        }

        if (totalPairs == 0) return;

        double avgJaccardScore = totalJaccardScore / totalPairs;
        double disjointRatio = (double) disjointPairs / totalPairs;

        double dynamicJaccardThreshold = 1.0 / candidateFields.size();

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
                        methodName.equals("startsWith") ||
                        methodName.equals("endsWith") ||
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

        if (typeRef.isPrimitive() || "String".equals(typeRef.getSimpleName())) {
            return true;
        }

        if (typeRef.isArray()) {
            spoon.reflect.reference.CtArrayTypeReference<?> arrayRef =
                    (spoon.reflect.reference.CtArrayTypeReference<?>) typeRef;
            return isPrimitiveOrString(arrayRef.getComponentType());
        }

        if (typeRef.getQualifiedName().startsWith("java.util.")) {
            List<CtTypeReference<?>> generics = typeRef.getActualTypeArguments();
            if (!generics.isEmpty()) {
                CtTypeReference<?> innerType = generics.get(0);
                String innerName = innerType.getSimpleName();

                return "String".equals(innerName) || innerType.unbox().isPrimitive();
            }
        }

        return false;
    }

    /**
     * Detects if an array is being used as a fake Object/Struct.
     * It does this by checking if the array is accessed using multiple
     * different hardcoded integer literals (e.g., arr[0], arr[1], arr[3])
     * rather than through a dynamic loop variable.
     */
    private boolean isArrayUsedAsStruct(CtType<?> type) {
        List<CtArrayAccess<?, ?>> arrayAccesses = type.getElements(new TypeFilter<>(CtArrayAccess.class));

        Map<String, Set<Integer>> fieldToIndices = new HashMap<>();

        for (CtArrayAccess<?, ?> access : arrayAccesses) {

            if (access.getTarget() instanceof CtFieldAccess<?> fieldAccess) {
                String fieldName = fieldAccess.getVariable().getSimpleName();

                if (access.getIndexExpression() instanceof CtLiteral<?> literal) {
                    Object value = literal.getValue();

                    if (value instanceof Integer) {
                        fieldToIndices.putIfAbsent(fieldName, new HashSet<>());
                        fieldToIndices.get(fieldName).add((Integer) value);
                    }
                }
            }
        }

        for (Map.Entry<String, Set<Integer>> entry : fieldToIndices.entrySet()) {
            Set<Integer> distinctHardcodedIndices = entry.getValue();

            if (distinctHardcodedIndices.size() >= 2) {
                return true;
            }
        }

        return false;
    }
}