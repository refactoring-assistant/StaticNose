package edu.northeastern.smells;

import spoon.Launcher;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.declaration.CtElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataClassDetector extends AbstractDetector {

    private static final int ACCESSOR_OR_FIELD_FEW_LEVEL = 3;
    private static final int ACCESSOR_OR_FIELD_MANY_LEVEL = 5;
    private static final double WOC_LEVEL = 1.0 / 3.0; // 33.3%
    private static final int WMC_HIGH_LEVEL = 31;
    private static final int WMC_VERY_HIGH_LEVEL = 47;

    public DataClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Data Class";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        if (type.isInterface() || type.isEnum()) {
            return detectedLines;
        }

        int wmc = calculateWMC(type);
        int nopa = calculateNOPA(type);
        int noam = calculateNOAM(type);
        double woc = calculateWOC(type);

        boolean interfaceRevealsData = woc < WOC_LEVEL;

        boolean revealsDataAndLacksComplexity =
                (nopa + noam > ACCESSOR_OR_FIELD_FEW_LEVEL && wmc < WMC_HIGH_LEVEL) ||
                        (nopa + noam > ACCESSOR_OR_FIELD_MANY_LEVEL && wmc < WMC_VERY_HIGH_LEVEL);

        if (interfaceRevealsData && revealsDataAndLacksComplexity) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private int calculateNOPA(CtType<?> type) {
        int count = 0;
        for (CtField<?> field : type.getFields()) {
            if (field.isPublic() && !field.isStatic() && !field.isFinal()) {
                count++;
            }
        }
        return count;
    }

    private int calculateNOAM(CtType<?> type) {
        int count = 0;
        for (CtMethod<?> method : type.getMethods()) {
            if (isAccessor(method)) {
                count++;
            }
        }
        return count;
    }

    private int calculateWMC(CtType<?> type) {
        int totalComplexity = 0;
        for (CtMethod<?> method : type.getMethods()) {
            totalComplexity += calculateCyclomaticComplexity(method);
        }
        return totalComplexity;
    }

    private double calculateWOC(CtType<?> type) {
        List<CtMethod<?>> publicMethods = type.getMethods().stream()
                .filter(CtMethod::isPublic)
                .filter(m -> !m.isAbstract()) // interfaces/abstract classes check
                .collect(Collectors.toList());

        long totalPublicMethods = publicMethods.size();
        if (totalPublicMethods == 0) return 0.0;

        long functionalMethods = publicMethods.stream()
                .filter(m -> !isAccessor(m))
                .count();

        return (double) functionalMethods / totalPublicMethods;
    }

    private boolean isAccessor(CtMethod<?> method) {
        if (!method.isPublic() || method.isStatic()) return false;

        String name = method.getSimpleName();
        int paramCount = method.getParameters().size();
        String returnType = method.getType().getSimpleName();

        if ((name.startsWith("get") || name.startsWith("is")) && paramCount == 0 && !returnType.equals("void")) {
            return true;
        }

        if (name.startsWith("set") && paramCount == 1 && returnType.equals("void")) {
            return true;
        }

        return false;
    }

    private int calculateCyclomaticComplexity(CtMethod<?> method) {
        if (method.getBody() == null) return 1;

        int complexity = 1;

        List<Class<? extends CtElement>> decisionNodes = List.of(
                CtIf.class, CtFor.class, CtForEach.class, CtWhile.class,
                CtDo.class, CtCase.class, CtConditional.class
        );

        for (Class<?> node : decisionNodes) {
            complexity += method.getElements(new TypeFilter(node)).size();
        }

        for (CtBinaryOperator<?> op : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            if (op.getKind() == BinaryOperatorKind.AND || op.getKind() == BinaryOperatorKind.OR) {
                complexity++;
            }
        }

        return complexity;
    }
}