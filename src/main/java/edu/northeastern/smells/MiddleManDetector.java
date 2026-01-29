package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MiddleManDetector extends AbstractDetector {

    public MiddleManDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
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
        if(methods.isEmpty()) {
            return detectedLines;
        }

        int singleLineDelegates = 0;

        for(CtMethod<?> method : methods) {
            if(method.getBody() == null) continue;

            List<CtStatement> statements = method.getBody().getStatements();
            int statementCount = statements.size();

            if(statementCount == 1) {
                CtStatement stmt = statements.get(0);
                if(isInvocataion(stmt) || isReturnInvocation(stmt)) {
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

        // code smell check
        boolean hasCodeSmell = false;
        if(ratio > 0.5 && fanOut > 0) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private boolean isInvocataion(CtStatement stmt) {
        return stmt instanceof CtInvocation;
    }

    private boolean isReturnInvocation(CtStatement stmt) {
        if(stmt instanceof CtReturn) {
            CtReturn<?> ret = (CtReturn<?>) stmt;
            return ret.getReturnedExpression() instanceof CtInvocation;
        }
        return false;
    }

    private boolean isAssignInvocationAndReturn(CtStatement first, CtStatement second) {
        if(!(first instanceof  CtLocalVariable)) return false;

        if(!(second instanceof CtReturn)) return false;

        CtLocalVariable<?> local = (CtLocalVariable<?>) first;
        CtReturn<?> ret = (CtReturn<?>) second;

        if(!(local.getDefaultExpression() instanceof CtInvocation)) return false;

        if(!(ret.getReturnedExpression() instanceof CtVariableRead)) return false;

        CtVariableRead<?> read = (CtVariableRead<?>) ret.getReturnedExpression();
        return read.getVariable().getDeclaration() == local;
    }

}
