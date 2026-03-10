package edu.northeastern.smells;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class InappropriateIntimacyDetector extends AbstractDetector {

    public InappropriateIntimacyDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Inappropriate Intimacy";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        CtTypeReference<?> currentClassRef = type.getReference();

        List<CtFieldAccess<?>> fieldAccesses = type.getElements(new TypeFilter<>(CtFieldAccess.class));

        for (CtFieldAccess<?> access : fieldAccesses) {
            CtFieldReference<?> fieldRef = access.getVariable();

            if (fieldRef == null || fieldRef.getDeclaringType() == null) {
                continue;
            }

            CtTypeReference<?> declaringClassRef = fieldRef.getDeclaringType();

            if (declaringClassRef == null || (access.getTarget() != null && access.getTarget().getType() instanceof spoon.reflect.reference.CtArrayTypeReference)) {
                continue;
            }

            if (fieldRef.getType() != null && fieldRef.getType().getQualifiedName().equals("java.lang.Class")) {
                continue;
            }

            if (!declaringClassRef.equals(currentClassRef)) {

                if (isSameTopLevelClass(currentClassRef, declaringClassRef)) {
                    continue;
                }

                if (fieldRef.isStatic() && fieldRef.isFinal()) {
                    continue;
                }

                if (declaringClassRef.isEnum()) {
                    continue;
                }

                if (access.getPosition().isValidPosition()) {
                    detectedLines.add(access.getPosition().getLine());
                }
            }
        }

        return detectedLines;
    }

    private boolean isSameTopLevelClass(CtTypeReference<?> type1, CtTypeReference<?> type2) {
        return type1.getTopLevelType().equals(type2.getTopLevelType());
    }
}