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

    public DivergentChangeDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Divergent Change";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        if (type.getFields().isEmpty()) {
            return detectedLines;
        }

        double tcc = calculateTCC(type);

        if (tcc <= 0.2) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private double calculateTCC(CtType<?> type) {
        List<CtMethod<?>> methods = new ArrayList<>();

        for (CtMethod<?> m : type.getMethods()) {
            if (m.getBody() != null && !isAccessor(m, false)) {
                methods.add(m);
            }
        }

        int n = methods.size();
        if (n < 2) return 1.0;

        long maxPairs = (long) n * (n - 1) / 2;
        long connectedPairs = 0;

        Map<CtMethod<?>, Set<String>> fieldUsage = getMethodFieldUsageMap(type, methods, 0.7);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<String> fields1 = fieldUsage.get(methods.get(i));
                Set<String> fields2 = fieldUsage.get(methods.get(j));

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

}

