package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrimitiveObsessionDetector extends AbstractDetector{

    private static final int VALIDATION_DISTRIBUTION_THRESHOLD = 2;
    private static final int MAX_PRIMITIVE_FIELDS = 4;

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

        if (candidateFields.size() > MAX_PRIMITIVE_FIELDS) {
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

        return new ArrayList<>(detectedLines);
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

        if (parent instanceof CtBinaryOperator) {
            CtBinaryOperator<?> op = (CtBinaryOperator<?>) parent;
            BinaryOperatorKind kind = op.getKind();
            return kind == BinaryOperatorKind.EQ || kind == BinaryOperatorKind.NE ||
                    kind == BinaryOperatorKind.GT || kind == BinaryOperatorKind.LT ||
                    kind == BinaryOperatorKind.GE || kind == BinaryOperatorKind.LE;
        }

        if (parent instanceof CtInvocation) {
            CtInvocation<?> inv = (CtInvocation<?>) parent;
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

        if (parent instanceof CtIf) {
            CtIf ifStmt = (CtIf) parent;
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
