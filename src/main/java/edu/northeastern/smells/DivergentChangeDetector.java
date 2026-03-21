package edu.northeastern.smells;

import spoon.reflect.code.CtInvocation;
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
    private static final double UBIQUITY_THRESHOLD = 0.70;

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
        List<CtMethod<?>> methods = new ArrayList<>(type.getMethods());

        // Get a list of methods ignoring getters and setters
        List<CtMethod<?>> methodsToAnalyze = new ArrayList<>();
        for (CtMethod<?> m : methods) {
            if (m.getBody() != null
                    && !isAccessor(m, false)
                    && !isOverride(m)) {
                methodsToAnalyze.add(m);
            }
        }

        // There should be more than 2 methods to analyze in a class
        if (methodsToAnalyze.size() < 2) return detectedLines;

        // --- THE SHARED UTILITY SHORTCUT ---
        // Instantly get the mapped fields with glue fields (like Loggers) already filtered out
        Map<CtMethod<?>, Set<String>> methodFieldUsage = getMethodFieldUsageMap(type, methodsToAnalyze, UBIQUITY_THRESHOLD);

        // checks which methods are connected to other methods through a graph
        Map<CtMethod<?>, Set<CtMethod<?>>> adjacencyList = new HashMap<>();
        for (CtMethod<?> m : methodsToAnalyze) {
            adjacencyList.put(m, new HashSet<>());
        }

        for (int i = 0; i < methodsToAnalyze.size(); i++) {
            CtMethod<?> m1 = methodsToAnalyze.get(i);

            for (int j = i + 1; j < methodsToAnalyze.size(); j++) {
                CtMethod<?> m2 = methodsToAnalyze.get(j);

                if (areConnected(m1, m2, methodFieldUsage)) {
                    adjacencyList.get(m1).add(m2);
                    adjacencyList.get(m2).add(m1);
                }
            }
        }

        // count the number of disjointed method groups (islands)
        int components = countComponents(adjacencyList);

        if (components > 1) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    /**
     * Check if two methods are connected and a graph edge should
     * be drawn between them. They are connected if they share the same field usage
     * or if they call each other.
     * (Note: Glue fields have already been filtered out by the Metrics utility).
     * @param m1 Method 1
     * @param m2 Method 2
     * @param fieldUsage The fields used by methods
     * @return boolean
     */
    private boolean areConnected(CtMethod<?> m1, CtMethod<?> m2,
                                 Map<CtMethod<?>, Set<String>> fieldUsage) {

        Set<String> f1 = fieldUsage.get(m1);
        Set<String> f2 = fieldUsage.get(m2);

        for (String field : f1) {
            if (f2.contains(field)) {
                return true;
            }
        }

        if (callsMethod(m1, m2)) return true;
        return callsMethod(m2, m1);
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
     * Find disjointed methods groups in the graph
     * @param graph The method graph to find islands from
     * @return the amount of islands in a graph
     */
    private int countComponents(Map<CtMethod<?>, Set<CtMethod<?>>> graph) {
        int count = 0;
        Set<CtMethod<?>> visited = new HashSet<>();

        for (CtMethod<?> node : graph.keySet()) {
            if (!visited.contains(node)) {
                count++;
                bfs(node, graph, visited);
            }
        }
        return count;
    }

    /**
     * Run a breadth first search on the method graph
     * @param start Starting node
     * @param graph The method graph
     * @param visited A visited method in the graph
     */
    private void bfs(CtMethod<?> start, Map<CtMethod<?>, Set<CtMethod<?>>> graph, Set<CtMethod<?>> visited) {
        Queue<CtMethod<?>> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            CtMethod<?> current = queue.poll();
            for (CtMethod<?> neighbor : graph.get(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
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