package edu.northeastern.smells;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class detects Inappropriate Intimacy code smell.
 * This code smell is when a class directly access field of another
 * class when it should instead access fields through a getter (encapsulation).
 * It includes multiple edge cases as well.
 */
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

            boolean isExternal = false;
            spoon.reflect.code.CtExpression<?> target = access.getTarget();

            if (target != null && !target.isImplicit()) {
                if (target instanceof spoon.reflect.code.CtThisAccess ||
                        target instanceof spoon.reflect.code.CtSuperAccess ||
                        target instanceof spoon.reflect.code.CtTypeAccess) {

                    if (!currentClassRef.isSubtypeOf(declaringClassRef)) {
                        isExternal = true;
                    }
                } else {
                    CtTypeReference<?> targetType = target.getType();

                    if (targetType != null) {
                        if (!targetType.equals(currentClassRef)) {
                            isExternal = true;
                        }
                    } else {
                        if (!currentClassRef.isSubtypeOf(declaringClassRef)) {
                            isExternal = true;
                        }
                    }
                }
            } else {
                if (!currentClassRef.isSubtypeOf(declaringClassRef)) {
                    isExternal = true;
                }
            }

            if (isExternal) {

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

    /**
     * Check if two classes are part of the same top level class.
     * This is useful when detecting nested classes field accesses.
     * @param type1 Class 1
     * @param type2 Class 2
     * @return boolean
     */
    private boolean isSameTopLevelClass(CtTypeReference<?> type1, CtTypeReference<?> type2) {
        return type1.getTopLevelType().equals(type2.getTopLevelType());
    }
}