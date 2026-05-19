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

    // few fields threshold
    private final int ACCESSOR_OR_FIELD_FEW_LEVEL;
    // many fields threshold
    private final int ACCESSOR_OR_FIELD_MANY_LEVEL;
    // percentage of weight of class (functional/total methods)
    // should be less than 33%
    private final double WOC_LEVEL;
    // weight of methods threshold.
    private final int WMC_HIGH_LEVEL;
    // if many fields then use this threshold
    private final int WMC_VERY_HIGH_LEVEL;

    // --- STRICT MODE FLAG ---
    private boolean strictMode = false;

    public DataClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        ACCESSOR_OR_FIELD_FEW_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "ACCESSOR_OR_FIELD_FEW_LEVEL", 3);
        ACCESSOR_OR_FIELD_MANY_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "ACCESSOR_OR_FIELD_MANY_LEVEL", 5);
        WOC_LEVEL = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "WOC_LEVEL", 1.0 / 3.0);
        WMC_HIGH_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "WMC_HIGH_LEVEL", 31);
        WMC_VERY_HIGH_LEVEL = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "WMC_VERY_HIGH_LEVEL", 47);
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
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

        // --- ENHANCED STRICT MODE LOGIC ---
        if (strictMode) {
            boolean allStrictAccessors = true;
            List<CtMethod<?>> methods = new ArrayList<>(type.getMethods());

            // If a class has no methods but has fields, it is essentially a pure C-style struct (Data Class)
            if (methods.isEmpty() && !type.getFields().isEmpty()) {
                if (type.getPosition().isValidPosition()) detectedLines.add(type.getPosition().getLine());
                return detectedLines;
            }

            // Check every single method to ensure it's a pure getter/setter
            for (CtMethod<?> m : methods) {
                if (isObjectBoilerplate(m)) {
                    continue;
                }

                if (!isStrictAccessor(m, type)) {
                    allStrictAccessors = false;
                    break;
                }
            }

            // If ALL methods are strict accessors, it is a data class.
            if (allStrictAccessors && !methods.isEmpty()) {
                if (type.getPosition().isValidPosition()) {
                    detectedLines.add(type.getPosition().getLine());
                }
            }

            // In strict mode, we skip the heuristic evaluation completely
            return detectedLines;
        }

        // --- ORIGINAL HEURISTIC LOGIC ---
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
        // A pure accessor MUST have exactly one statement.
        if (statements.size() != 1) return false;

        CtStatement stmt = statements.get(0);

        // 1. STRICT GETTER CHECK
        if (method.getParameters().isEmpty() && !method.getType().getSimpleName().equals("void")) {
            if (stmt instanceof CtReturn<?> retStmt) {
                CtExpression<?> retExp = retStmt.getReturnedExpression();
                // It must return a field access (e.g. `return this.name;` or `return name;`)
                if (retExp instanceof CtFieldAccess<?> fieldAccess) {
                    return isFieldBelongingToClass(fieldAccess, type);
                }
            }
            return false;
        }

        // 2. STRICT SETTER CHECK
        if (method.getParameters().size() == 1 && method.getType().getSimpleName().equals("void")) {
            if (stmt instanceof CtAssignment<?, ?> assignStmt) {
                CtExpression<?> lhs = assignStmt.getAssigned();
                CtExpression<?> rhs = assignStmt.getAssignment();

                // Left side must be a field, Right side must be the raw parameter variable
                if (lhs instanceof CtFieldAccess<?> fieldAccess && rhs instanceof CtVariableRead<?> varRead) {
                    boolean correctField = isFieldBelongingToClass(fieldAccess, type);
                    boolean correctParam = varRead.getVariable().getSimpleName()
                            .equals(method.getParameters().get(0).getSimpleName());

                    return correctField && correctParam;
                }
            }
            return false;
        }

        return false; // Not a getter or setter
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
                .filter(m -> !isObjectBoilerplate(m))
                .toList();

        long totalPublicMethods = publicMethods.size();
        if (totalPublicMethods == 0) return 0.0;

        long functionalMethods = publicMethods.stream()
                .filter(m -> !isAccessor(m, true))
                .count();

        return (double) functionalMethods / totalPublicMethods;
    }

    private boolean isObjectBoilerplate(CtMethod<?> method) {
        String name = method.getSimpleName();
        int paramCount = method.getParameters().size();

        if (name.equals("toString") && paramCount == 0) return true;
        if (name.equals("hashCode") && paramCount == 0) return true;
        if (name.equals("equals") && paramCount == 1) return true;

        // Add clone to the boilerplate filter
        if (name.equals("clone") && paramCount == 0) return true;

        // You can add finalize() too just to be perfectly safe,
        // though it shouldn't be in any modern codebase.
        if (name.equals("finalize") && paramCount == 0) return true;

        return false;
    }
}