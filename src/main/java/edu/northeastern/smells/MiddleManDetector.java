package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MiddleManDetector extends AbstractDetector {

    private final double DELEGATION_THRESHOLD;
    private final int FAN_OUT_THRESHOLD;

    public MiddleManDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        DELEGATION_THRESHOLD = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "DELEGATION_THRESHOLD", 0.5);
        FAN_OUT_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "FAN_OUT_THRESHOLD", 0);
    }

    @Override
    protected String getSmellName() {
        return "Middle Man";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        Set<CtTypeReference<?>> referencedTypes = type.getReferencedTypes();
        long fanOut = referencedTypes.stream()
                .filter(ref -> !ref.isPrimitive())
                .filter(ref -> !ref.getQualifiedName().equals(type.getQualifiedName()))
                .count();

        Set<CtMethod<?>> methods = type.getMethods();

        int singleLineDelegates = 0;

        for(CtMethod<?> method : methods) {
            if(method.getBody() == null) continue;

            List<CtStatement> statements = method.getBody().getStatements();
            int statementCount = statements.size();

            boolean isPureDelegate = false;

            if (statementCount == 2 && isAssignInvocationAndReturn(statements.get(0), statements.get(1), method)) {
                isPureDelegate = true;
            } else {
                boolean hasDelegation = false;
                boolean hasProcessing = false;

                for (CtStatement stmt : statements) {
                    if (isInvocation(stmt, method) || isReturnInvocation(stmt, method)) {
                        hasDelegation = true;
                    } else if (stmt instanceof CtInvocation) {
                    } else if (stmt instanceof CtReturn<?> ret) {
                        if (ret.getReturnedExpression() != null &&
                            !(ret.getReturnedExpression() instanceof spoon.reflect.code.CtLiteral) &&
                            !(ret.getReturnedExpression() instanceof spoon.reflect.code.CtVariableRead)) {
                            hasProcessing = true;
                            break;
                        }
                    } else {
                        hasProcessing = true;
                        break;
                    }
                }

                if (hasDelegation && !hasProcessing) {
                    isPureDelegate = true;
                }
            }

            if (isPureDelegate) {
                singleLineDelegates++;
            }
        }

        double ratio = (double) singleLineDelegates / methods.size();

        boolean hasCodeSmell = (ratio >= DELEGATION_THRESHOLD) && (singleLineDelegates >= 2);

        if(hasCodeSmell && fanOut > FAN_OUT_THRESHOLD) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private boolean isInvocation(CtStatement stmt, CtMethod<?> parentMethod) {
        if (stmt instanceof CtInvocation<?> inv) {
            return isDelegatingToField(inv, parentMethod);
        }
        return false;
    }

    private boolean isReturnInvocation(CtStatement stmt, CtMethod<?> parentMethod) {
        if (stmt instanceof CtReturn<?> ret) {
            if (ret.getReturnedExpression() instanceof CtInvocation<?> inv) {
                return isDelegatingToField(inv, parentMethod);
            }
        }
        return false;
    }

    private boolean isDelegatingToField(CtInvocation<?> inv, CtMethod<?> parentMethod) {
        if (inv.getTarget() == null) return false;

        if (inv.getTarget() instanceof spoon.reflect.code.CtVariableRead<?> read) {
            spoon.reflect.declaration.CtVariable<?> var = read.getVariable().getDeclaration();

            if (var == null) return false;

            if (var instanceof spoon.reflect.declaration.CtField<?> field) {
                if (field.getType() != null) {
                    return isProjectClass(field.getType().getQualifiedName());
                }
            }
        }

        return false;
    }

    private boolean isAssignInvocationAndReturn(CtStatement first, CtStatement second, CtMethod<?> parentMethod) {
        if(!(first instanceof CtLocalVariable<?> local)) return false;

        if(!(second instanceof CtReturn<?> ret)) return false;

        if(!(local.getDefaultExpression() instanceof CtInvocation<?> inv)) return false;

        if(!(ret.getReturnedExpression() instanceof CtVariableRead<?> read)) return false;

        if(read.getVariable().getDeclaration() != local) return false;

        return isDelegatingToField(inv, parentMethod);
    }

}
