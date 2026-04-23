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

    // TODO: Make sure field accesses from super class aren't flagged
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

            // ignore Array object field accesses
            if (declaringClassRef == null || (access.getTarget() != null && access.getTarget().getType() instanceof spoon.reflect.reference.CtArrayTypeReference)) {
                continue;
            }

            // ignore standard Java language field accesses
            if (fieldRef.getType() != null && fieldRef.getType().getQualifiedName().equals("java.lang.Class")) {
                continue;
            }

            // --- THE FIX: Evaluate the Target Expression ---
            boolean isExternal = false;
            spoon.reflect.code.CtExpression<?> target = access.getTarget();

            if (target != null && !target.isImplicit()) {
                if (target instanceof spoon.reflect.code.CtThisAccess ||
                        target instanceof spoon.reflect.code.CtSuperAccess ||
                        target instanceof spoon.reflect.code.CtTypeAccess) {

                    // It is this.field, super.field, or Class.staticField
                    if (!currentClassRef.isSubtypeOf(declaringClassRef)) {
                        isExternal = true;
                    }
                } else {
                    // It is an explicit object reference (e.g., base.ingredients)
                    CtTypeReference<?> targetType = target.getType();

                    if (targetType != null) {
                        // Accessing another instance of the exact same class is usually valid encapsulation
                        if (!targetType.equals(currentClassRef)) {
                            isExternal = true;
                        }
                    } else {
                        // Fallback if Spoon cannot resolve the target type
                        if (!currentClassRef.isSubtypeOf(declaringClassRef)) {
                            isExternal = true;
                        }
                    }
                }
            } else {
                // Implicit target (e.g., just 'ingredients')
                if (!currentClassRef.isSubtypeOf(declaringClassRef)) {
                    isExternal = true;
                }
            }

            // make sure the field access is external (outside current class)
            if (isExternal) {

                // ignore cases of nested classes
                if (isSameTopLevelClass(currentClassRef, declaringClassRef)) {
                    continue;
                }

                // ignore constants
                if (fieldRef.isStatic() && fieldRef.isFinal()) {
                    continue;
                }

                // ignore enums
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