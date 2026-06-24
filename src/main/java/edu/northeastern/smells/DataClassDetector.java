package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;

import static edu.northeastern.utils.Metrics.calculateWMC;
import static edu.northeastern.utils.Metrics.isAccessor;

/**
 * Data Class code smell detector.
 */
public class DataClassDetector extends AbstractDetector {

    private final int ACCESSOR_OR_FIELD_FEW_LEVEL;
    private final int ACCESSOR_OR_FIELD_MANY_LEVEL;
    private final double WOC_LEVEL;
    private final int WMC_HIGH_LEVEL;
    private final int WMC_VERY_HIGH_LEVEL;

    private final boolean strictMode = true;

    public DataClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        ACCESSOR_OR_FIELD_FEW_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "ACCESSOR_OR_FIELD_FEW_LEVEL", 3);
        ACCESSOR_OR_FIELD_MANY_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "ACCESSOR_OR_FIELD_MANY_LEVEL", 5);
        WOC_LEVEL = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "WOC_LEVEL", 1.0 / 3.0);
        WMC_HIGH_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "WMC_HIGH_LEVEL", 31);
        WMC_VERY_HIGH_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "WMC_VERY_HIGH_LEVEL", 47);
    }

    @Override
    protected String getSmellName() {
        return "Data Class";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        if (type.isInterface() || type.isEnum() || type.getModifiers().contains(spoon.reflect.declaration.ModifierKind.ABSTRACT)) {
            return detectedLines;
        }

        List<spoon.reflect.declaration.CtConstructor<?>> constructors = type.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(spoon.reflect.declaration.CtConstructor.class));
        if (!constructors.isEmpty()) {
            boolean allPrivateConstructors = true;
            for (spoon.reflect.declaration.CtConstructor<?> ctor : constructors) {
                if (!ctor.isPrivate()) {
                    allPrivateConstructors = false;
                    break;
                }
            }
            if (allPrivateConstructors) {
                return detectedLines;
            }
        }

        if (strictMode) {
            boolean allStrictAccessors = true;
            List<CtMethod<?>> methods = new ArrayList<>(type.getMethods());

            long nonStaticPublicFieldCount = type.getFields().stream().filter(f -> !f.isStatic() && f.isPublic()).count();
            if (methods.isEmpty() && nonStaticPublicFieldCount > 0) {
                if (type.getPosition().isValidPosition()) detectedLines.add(type.getPosition().getLine());
                return detectedLines;
            }

            for (CtMethod<?> m : methods) {
                if (!isStrictAccessor(m, type)) {
                    allStrictAccessors = false;
                    break;
                }
            }

            if (allStrictAccessors && !methods.isEmpty()) {
                if (type.getPosition().isValidPosition()) {
                    detectedLines.add(type.getPosition().getLine());
                }
            }

            return detectedLines;
        }

        int wmc = calculateWMC(type);
        int nopa = calculateNOPA(type);
        int noam = calculateNOAM(type);
        double woc = calculateWOC(type);

        boolean interfaceRevealsData = woc < WOC_LEVEL;
        boolean revealsDataAndLacksComplexity =
                (nopa + noam >= ACCESSOR_OR_FIELD_FEW_LEVEL && wmc < WMC_HIGH_LEVEL) ||
                        (nopa + noam > ACCESSOR_OR_FIELD_MANY_LEVEL && wmc < WMC_VERY_HIGH_LEVEL);

        if (interfaceRevealsData && revealsDataAndLacksComplexity) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    /**
     * Checks if a method is purely a getter or setter without ANY logic,
     * mathematical operations, or parameter mutations.
     */
    private boolean isStrictAccessor(CtMethod<?> method, CtType<?> type) {
        if (method.getBody() == null) return false;

        List<CtStatement> statements = method.getBody().getStatements();
        if (statements.size() != 1) return false;

        CtStatement stmt = statements.get(0);

        if (method.getParameters().isEmpty() && !method.getType().getSimpleName().equals("void")) {
            if (stmt instanceof CtReturn<?> retStmt) {
                CtExpression<?> retExp = retStmt.getReturnedExpression();
                if (retExp instanceof CtFieldAccess<?> fieldAccess) {
                    return isFieldBelongingToClass(fieldAccess, type);
                }
            }
            return false;
        }

        if (method.getParameters().size() == 1 && method.getType().getSimpleName().equals("void")) {
            if (stmt instanceof CtAssignment<?, ?> assignStmt) {
                CtExpression<?> lhs = assignStmt.getAssigned();
                CtExpression<?> rhs = assignStmt.getAssignment();

                if (lhs instanceof CtFieldAccess<?> fieldAccess && rhs instanceof CtVariableRead<?> varRead) {
                    boolean correctField = isFieldBelongingToClass(fieldAccess, type);
                    boolean correctParam = varRead.getVariable().getSimpleName()
                            .equals(method.getParameters().get(0).getSimpleName());

                    return correctField && correctParam;
                }
            }
            return false;
        }

        return false;
    }

    /**
     * Helper to verify that the field being accessed actually belongs to this class,
     * and isn't a public field of some other completely unrelated object.
     */
    private boolean isFieldBelongingToClass(CtFieldAccess<?> fieldAccess, CtType<?> type) {
        String fieldName = fieldAccess.getVariable().getSimpleName();
        return type.getFields().stream().anyMatch(f -> f.getSimpleName().equals(fieldName));
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
            if (isAccessor(method, true)) {
                count++;
            }
        }
        return count;
    }

    private double calculateWOC(CtType<?> type) {
        List<CtMethod<?>> publicMethods = type.getMethods().stream()
                .filter(CtMethod::isPublic)
                .filter(m -> !m.isAbstract())
                .toList();

        long totalPublicMethods = publicMethods.size();
        if (totalPublicMethods == 0) return 0.0;

        long functionalMethods = publicMethods.stream()
                .filter(m -> !isAccessor(m, true))
                .count();

        return (double) functionalMethods / totalPublicMethods;
    }
}