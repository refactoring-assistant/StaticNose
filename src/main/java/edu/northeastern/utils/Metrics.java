package edu.northeastern.utils;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * This class contains methods that calculate commonly required Metrics
 * for different detectors.
 */
public class Metrics {

    /**
     * Calculate the Weighted Methods per Class metric
     * WMC is the total complexity of all the methods in a class
     * calculated by summing up the cyclomatic complexities of every
     * method in the class.
     * Data Classes have low WMC since they do not do much more than
     * setting and getting fields.
     *
     * @param type The class whose WMC needs to be calculated
     * @return the WMC metric value
     */
    public static int calculateWMC(CtType<?> type) {
        int totalComplexity = 0;
        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() != null) {
                totalComplexity += calculateCyclomaticComplexity(method);
            }
        }
        return totalComplexity;
    }

    /**
     * Calculate the cyclomatic complexity of a method.
     * Cyclomatic complexity is the number of independent paths present
     * in a piece of code
     * @param method The method whose cyclomatic complexity needs to be calculated
     * @return the cyclomatic complexity
     */
    public static int calculateCyclomaticComplexity(CtMethod<?> method) {
        if (method.getBody() == null) return 1;

        int complexity = 1;

        List<Class<? extends CtElement>> decisionNodes = List.of(
                CtIf.class, CtFor.class, CtForEach.class, CtWhile.class,
                CtDo.class, CtConditional.class, CtCatch.class, CtThrow.class
        );

        for (Class<? extends CtElement> node : decisionNodes) {
            complexity += method.getElements(new TypeFilter<>(node)).size();
        }

        for (CtCase<?> switchCase : method.getElements(new TypeFilter<>(CtCase.class))) {
            if (switchCase.getCaseExpression() != null) {
                complexity++;
            }
        }

        for (CtBinaryOperator<?> op : method.getElements(new TypeFilter<>(CtBinaryOperator.class))) {
            if (op.getKind() == BinaryOperatorKind.AND || op.getKind() == BinaryOperatorKind.OR) {

                CtElement parent = op.getParent();
                while (parent != null && !(parent instanceof CtMethod)) {
                    if (parent instanceof CtIf || parent instanceof CtLoop || parent instanceof CtConditional) {
                        complexity++;
                        break;
                    }
                    parent = parent.getParent();
                }
            }
        }

        return complexity;
    }

    /**
     * Checks if a method is a true accessor (getter/setter) based on both its
     * structural signature and its internal behavior size.
     * @param method The method to check.
     * @param requirePublic If true, strictly limits to public accessors.
     * @return boolean True if it is a simple getter or setter.
     */
    public static boolean isAccessor(CtMethod<?> method, boolean requirePublic) {
        if (method.isStatic()) return false;
        if (requirePublic && !method.isPublic()) return false;

        String name = method.getSimpleName();
        int paramCount = method.getParameters().size();

        if (method.getType() == null) return false;
        String returnType = method.getType().getSimpleName();

        boolean isGetter = (name.startsWith("get") || name.startsWith("is"))
                && paramCount == 0
                && !returnType.equals("void");

        boolean isSetter = name.startsWith("set")
                && paramCount == 1
                && returnType.equals("void");

        if (!isGetter && !isSetter) {
            return false;
        }

        if (method.getBody() == null) {
            return true;
        }

        return method.getBody().getStatements().size() <= 2;
    }

    /**
     * Calculate the number of Logical Lines of Code
     * A Logical Line of Code is code that is a statement in Java
     * @param method The method to check
     * @return the number of LLOC
     */
    public static int calculateLLOC(CtMethod<?> method) {
        // 1. Use getElements() to recursively grab ALL statements, no matter how deeply nested
        List<CtStatement> statements = method.getElements(new TypeFilter<>(CtStatement.class));

        int lloc = 0;
        for(CtStatement stmt : statements) {
            // 2. Ignore structural blocks (like {} itself) and compiler-generated implicit code
            if(!(stmt instanceof CtBlock) && !stmt.isImplicit()) {
                lloc++;
            }
        }

        return lloc;
    }

    /**
     * Creates a mapped registry of which methods access which internal class fields,
     * while safely ignoring ubiquitous "glue" fields (like loggers) that artificially
     * inflate cohesion metrics.
     * @param type The class being analyzed.
     * @param methodsToAnalyze The filtered list of methods to map.
     * @param ubiquityThreshold The percentage (0.0 to 1.0) at which a field is considered "glue".
     * @return A map of methods to their accessed internal field names.
     */
    public static Map<CtMethod<?>, Set<String>> getMethodFieldUsageMap(
            CtType<?> type,
            List<CtMethod<?>> methodsToAnalyze,
            double ubiquityThreshold) {

        Map<CtMethod<?>, Set<String>> methodFieldUsage = new HashMap<>();
        Map<String, Integer> fieldUsageCounts = new HashMap<>();

        // 1. Map all internal field accesses (Large Class Logic)
        for (CtMethod<?> m : methodsToAnalyze) {
            Set<String> accessedFields = new HashSet<>();
            List<CtFieldAccess<?>> accesses = m.getElements(new TypeFilter<>(CtFieldAccess.class));

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
            methodFieldUsage.put(m, accessedFields);

            for (String f : accessedFields) {
                fieldUsageCounts.put(f, fieldUsageCounts.getOrDefault(f, 0) + 1);
            }
        }

        // 2. Identify and remove Glue Fields (Divergent Change Logic)
        int totalMethods = methodsToAnalyze.size();
        Set<String> glueFields = new HashSet<>();
        for (Map.Entry<String, Integer> entry : fieldUsageCounts.entrySet()) {
            if (((double) entry.getValue() / totalMethods) >= ubiquityThreshold) {
                glueFields.add(entry.getKey());
            }
        }

        for (Set<String> fields : methodFieldUsage.values()) {
            fields.removeAll(glueFields);
        }

        return methodFieldUsage;
    }

}
