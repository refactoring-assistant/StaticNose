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
        // fan out

        Set<CtTypeReference<?>> referencedTypes = type.getReferencedTypes();
        long fanOut = referencedTypes.stream()
                .filter(ref -> !ref.isPrimitive())
                .filter(ref -> !ref.getQualifiedName().equals(type.getQualifiedName()))
                .count();

        // single line ratio

        Set<CtMethod<?>> methods = type.getMethods();

        int singleLineDelegates = 0;

        for(CtMethod<?> method : methods) {
            if(method.getBody() == null) continue;

            List<CtStatement> statements = method.getBody().getStatements();
            int statementCount = statements.size();

            if(statementCount == 1) {
                CtStatement stmt = statements.getFirst();

                // FIX: Pass the 'method' variable as the second argument to both helpers
                if(isInvocation(stmt, method) || isReturnInvocation(stmt, method)) {
                    singleLineDelegates++;
                }
            }

            // two line statements (assign and return)
            else if (statementCount == 2) {
                CtStatement first = statements.get(0);
                CtStatement second = statements.get(1);

                if(isAssignInvocationAndReturn(first, second)) {
                    singleLineDelegates++;
                }
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

    // NEW: The Surgical Delegation Target Checker
    // Renamed helper method for clarity
    private boolean isDelegatingToField(CtInvocation<?> inv, CtMethod<?> parentMethod) {
        if (inv.getTarget() == null) return false;

        // If the target is a variable
        if (inv.getTarget() instanceof spoon.reflect.code.CtVariableRead<?> read) {
            spoon.reflect.declaration.CtVariable<?> var = read.getVariable().getDeclaration();

            if (var == null) return false;

            // ONLY flag if it is delegating to an internal Field (Classic Middle Man)
            return var instanceof spoon.reflect.declaration.CtField;
        }

        return false;
    }

    private boolean isAssignInvocationAndReturn(CtStatement first, CtStatement second) {
        if(!(first instanceof CtLocalVariable<?> local)) return false;

        if(!(second instanceof CtReturn<?> ret)) return false;

        if(!(local.getDefaultExpression() instanceof CtInvocation)) return false;

        if(!(ret.getReturnedExpression() instanceof CtVariableRead<?> read)) return false;

        return read.getVariable().getDeclaration() == local;
    }

}
