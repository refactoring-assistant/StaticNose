package edu.northeastern.smells;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class DivergentChangeDetector extends AbstractDetector {

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

        List<CtMethod<?>> methodsToAnalyze = new ArrayList<>();
        for (CtMethod<?> m : methods) {
            if (m.getBody() != null
                    && !isGetterOrSetter(m)
                    && !isOverride(m)) {
                methodsToAnalyze.add(m);
            }
        }

        if (methodsToAnalyze.size() < 2) return detectedLines;

        Map<CtMethod<?>, Set<String>> methodFieldUsage = new HashMap<>();
        Map<String, Integer> fieldUsageCounts = new HashMap<>();

        for (CtMethod<?> m : methodsToAnalyze) {
            Set<String> fields = new HashSet<>();

            List<CtFieldAccess<?>> accesses = m.getElements(new TypeFilter<>(CtFieldAccess.class));
            for (CtFieldAccess<?> acc : accesses) {
                if (acc.getVariable() != null) {
                    String fieldName = acc.getVariable().getSimpleName();
                    fields.add(fieldName);
                }
            }
            methodFieldUsage.put(m, fields);

            for (String f : fields) {
                fieldUsageCounts.put(f, fieldUsageCounts.getOrDefault(f, 0) + 1);
            }
        }

        Set<String> glueFields = new HashSet<>();
        int totalMethods = methodsToAnalyze.size();
        int totalFields = fieldUsageCounts.size();

        for (Map.Entry<String, Integer> entry : fieldUsageCounts.entrySet()) {
            double ratio = (double) entry.getValue() / totalMethods;

            if (ratio >= UBIQUITY_THRESHOLD && totalFields >= 1) {
                glueFields.add(entry.getKey());
            }
        }

        Map<CtMethod<?>, Set<CtMethod<?>>> adjacencyList = new HashMap<>();
        for (CtMethod<?> m : methodsToAnalyze) {
            adjacencyList.put(m, new HashSet<>());
        }

        for (int i = 0; i < methodsToAnalyze.size(); i++) {
            CtMethod<?> m1 = methodsToAnalyze.get(i);

            for (int j = i + 1; j < methodsToAnalyze.size(); j++) {
                CtMethod<?> m2 = methodsToAnalyze.get(j);

                if (areConnected(m1, m2, methodFieldUsage, glueFields)) {
                    adjacencyList.get(m1).add(m2);
                    adjacencyList.get(m2).add(m1);
                }
            }
        }

        int components = countComponents(adjacencyList);

        if (components > 1) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private boolean areConnected(CtMethod<?> m1, CtMethod<?> m2,
                                 Map<CtMethod<?>, Set<String>> fieldUsage,
                                 Set<String> glueFields) {

        Set<String> f1 = fieldUsage.get(m1);
        Set<String> f2 = fieldUsage.get(m2);

        for (String field : f1) {
            if (f2.contains(field) && !glueFields.contains(field)) {
                return true;
            }
        }

        if (callsMethod(m1, m2)) return true;
        if (callsMethod(m2, m1)) return true;

        return false;
    }

    private boolean callsMethod(CtMethod<?> caller, CtMethod<?> target) {
        List<CtInvocation<?>> invocations = caller.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> inv : invocations) {
            if (inv.getExecutable() != null &&
                    inv.getExecutable().getSimpleName().equals(target.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

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

    private boolean isGetterOrSetter(CtMethod<?> m) {
        String name = m.getSimpleName();
        return (name.startsWith("get") && m.getParameters().isEmpty()) ||
                (name.startsWith("set") && m.getParameters().size() == 1);
    }

    private boolean isOverride(CtMethod<?> m) {
        return m.getAnnotations().stream().anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }
}