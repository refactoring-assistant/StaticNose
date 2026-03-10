package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LargeClassDetector extends AbstractDetector{

    private static final int WMC_THRESHOLD = 47;
    private static final double TCC_THRESHOLD = 0.33;

    public LargeClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Large Class";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        int wmc = calculateWMC(type);

        double tcc = calculateTCC(type);

        boolean isGodClass = (wmc >= WMC_THRESHOLD && tcc < TCC_THRESHOLD);

        if (isGodClass) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private int calculateWMC(CtType<?> type) {
        int totalComplexity = 0;
        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() != null) {
                totalComplexity += calculateCyclomaticComplexity(method);
            }
        }
        return totalComplexity;
    }

    private int calculateCyclomaticComplexity(CtMethod<?> method) {
        int complexity = 1;

        List<Class<? extends CtElement>> decisionPoints = List.of(
                CtIf.class,
                CtFor.class,
                CtForEach.class,
                CtWhile.class,
                CtDo.class,
                CtCase.class,
                CtConditional.class,
                CtCatch.class
        );

        for (Class<? extends CtElement> elementClass : decisionPoints) {
            complexity += method.getElements(new TypeFilter<>(elementClass)).size();
        }

        for (CtBinaryOperator<?> op : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            if (op.getKind() == BinaryOperatorKind.AND || op.getKind() == BinaryOperatorKind.OR) {
                complexity++;
            }
        }

        return complexity;
    }

    private double calculateTCC(CtType<?> type) {
        List<CtMethod<?>> methods = new ArrayList<>();

        for (CtMethod<?> m : type.getMethods()) {
            if (m.getBody() != null && !isGetterOrSetter(m)) {
                methods.add(m);
            }
        }

        int n = methods.size();
        if (n < 2) return 1.0;

        long maxPairs = (long) n * (n - 1) / 2;
        long connectedPairs = 0;

        List<Set<String>> methodFieldAccesses = new ArrayList<>();
        for (CtMethod<?> method : methods) {
            methodFieldAccesses.add(getAccessedFieldNames(method, type));
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<String> fields1 = methodFieldAccesses.get(i);
                Set<String> fields2 = methodFieldAccesses.get(j);

                if (isConnected(fields1, fields2)) {
                    connectedPairs++;
                }
            }
        }

        return (double) connectedPairs / maxPairs;
    }

    private boolean isConnected(Set<String> fields1, Set<String> fields2) {
        for (String field : fields1) {
            if (fields2.contains(field)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getAccessedFieldNames(CtMethod<?> method, CtType<?> type) {
        Set<String> accessedFields = new HashSet<>();

        List<CtFieldAccess<?>> accesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));
        for (CtFieldAccess<?> access : accesses) {
            if (access.getTarget() == null || access.getTarget().toString().equals("this") || access.getTarget().getType() == null) {
                accessedFields.add(access.getVariable().getSimpleName());
            } else {
                String targetType = access.getTarget().getType().getSimpleName();
                if (targetType.equals(type.getSimpleName())) {
                    accessedFields.add(access.getVariable().getSimpleName());
                }
            }
        }
        return accessedFields;
    }

    private boolean isGetterOrSetter(CtMethod<?> method) {
        String name = method.getSimpleName();
        boolean looksLikeAccessor = name.startsWith("get") || name.startsWith("set") || name.startsWith("is");
        if (!looksLikeAccessor) return false;

        int statements = method.getBody().getStatements().size();
        return statements <= 2;
    }
}
