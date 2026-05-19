package edu.northeastern.smells;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

// Assuming your Metrics utility class contains the shared methods
import static edu.northeastern.utils.Metrics.*;

/**
 * This class detects the Divergent Change code smell.
 * Divergent Change code smell is when making a change in a field or
 * class as a whole, we need to make changes to unrelated methods.
 * When a class has more than one business function, it ends up being needed to be changed
 * when these business functions change, which are unrelated to each other.
 * So we check for single responsibility of a class to determine whether
 * it does more than a single thing and then flag it.
 * The detector creates a graph of methods connected because they share the same field usage
 * and then finds islands in that graph.
 */
public class DivergentChangeDetector extends AbstractDetector {

    // this is for glue fields like Loggers. if a field touches
    // more than 70% of methods, it is likely required in those methods
    // and is a utility field.
    private final double UBIQUITY_THRESHOLD;

    public DivergentChangeDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        UBIQUITY_THRESHOLD = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "UBIQUITY_THRESHOLD", 0.70);
    }

    @Override
    protected String getSmellName() {
        return "Divergent Change";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();
        List<CtMethod<?>> methodsToAnalyze = new ArrayList<>();

        for (CtMethod<?> m : type.getMethods()) {
            if (m.getBody() != null && !isAccessor(m, false) && !isOverride(m)) {
                methodsToAnalyze.add(m);
            }
        }

        if (methodsToAnalyze.size() < 2) return detectedLines;

        if (type.getFields().isEmpty()) {
            return detectedLines;
        }

        double dynamicThreshold = methodsToAnalyze.size() <= 3 ? 1.01 : UBIQUITY_THRESHOLD;
        Map<CtMethod<?>, Set<String>> methodFieldUsage = getMethodFieldUsageMap(type, methodsToAnalyze, dynamicThreshold);

        boolean hasSiloedLogic = checkSiloedLogic(methodsToAnalyze, methodFieldUsage);

        if (hasSiloedLogic) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    /**
     * Checks if a class contains multiple complex methods that share fields
     * but NEVER interact with each other (Siloed Logic).
     * This is a classic indicator of Divergent Change, where different business rules
     * are crammed together just because they act on the same primitive data.
     */
    private boolean checkSiloedLogic(List<CtMethod<?>> methods, Map<CtMethod<?>, Set<String>> fieldUsage) {
        int siloCount = 0;

        for (CtMethod<?> m : methods) {
            // Only care about methods that actually do complex work (e.g., loops/math)
            // A complexity of 2 or higher usually filters out simple wrappers.
            if (calculateCyclomaticComplexity(m) >= 2) {

                boolean callsAnotherComplexMethod = false;

                // Check if this complex method calls any OTHER method in the class
                for (CtMethod<?> target : methods) {
                    if (m != target && callsMethod(m, target)) {
                        callsAnotherComplexMethod = true;
                        break;
                    }
                }

                // If it's complex but entirely isolated (doesn't delegate or share execution flow),
                // it's a standalone business responsibility!
                if (!callsAnotherComplexMethod) {
                    siloCount++;
                }
            }
        }

        // If we found 2 or more isolated complex domains (e.g., Multiplication and perform_add),
        // the class has Divergent Change!
        return siloCount >= 2;
    }

    /**
     * Check to see if a method has the given invocation
     * @param caller The method to check inside
     * @param target The method invocation to check for
     * @return boolean
     */
    private boolean callsMethod(CtMethod<?> caller, CtMethod<?> target) {
        List<CtInvocation<?>> invocations = caller.getElements(new TypeFilter<>(CtInvocation.class));

        String targetSignature = target.getSignature();

        for (CtInvocation<?> inv : invocations) {
            if (inv.getExecutable() != null) {
                if (inv.getExecutable().getSignature().equals(targetSignature)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if method is overidding from super class
     * @param m The method to check
     * @return boolean
     */
    private boolean isOverride(CtMethod<?> m) {
        return m.getAnnotations().stream().anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }
}

