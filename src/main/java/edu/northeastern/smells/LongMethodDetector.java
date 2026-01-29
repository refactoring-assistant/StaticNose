package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class LongMethodDetector extends AbstractDetector{

    public LongMethodDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        for(CtMethod<?> method : type.getMethods()) {
            if(method.getBody() == null) continue;

            int lloc = calculateLLOC(method);

            int complexity = calculateCyclomaticComplexity(method);

            if(lloc > 30 && complexity > 5) {
                detectedLines.add(method.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    // logical lines of code
    private int calculateLLOC(CtMethod<?> method) {
        List<CtStatement> statements = method.getBody().getStatements();

        int lloc = 0;
        for(CtStatement stmt : statements) {
            if(!(stmt instanceof CtBlock)) {
                lloc++;
            }
        }

        return lloc;
    }

    private int calculateCyclomaticComplexity(CtMethod<?> method) {
        int complexity = 1;

        List<Class<?>> decisionNodes = List.of(
                CtIf.class,
                CtFor.class,
                CtForEach.class,
                CtWhile.class,
                CtDo.class,
                CtCase.class,
                CtConditional.class
        );

        for(Class<?> node : decisionNodes) {
            complexity += method.getElements(new TypeFilter<>(node)).size();
        }

        for(CtBinaryOperator<?> op : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            if(op.getKind() == BinaryOperatorKind.AND || op.getKind() == BinaryOperatorKind.OR) {
                complexity++;
            }
        }

        return complexity;
    }

}
