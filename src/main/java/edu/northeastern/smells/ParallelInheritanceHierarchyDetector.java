package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class ParallelInheritanceHierarchyDetector extends AbstractDetector {

    private final int MIN_SUBCLASSES;
    private final int PARALLEL_LINK_THRESHOLD;

    private final Map<String, List<CtType<?>>> hierarchyMap = new HashMap<>();

    public ParallelInheritanceHierarchyDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        MIN_SUBCLASSES = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MIN_SUBCLASSES", 2);
        PARALLEL_LINK_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "PARALLEL_LINK_THRESHOLD", 2);
    }

    @Override
    protected String getSmellName() {
        return "Parallel Inheritance Hierarchies";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> reports = new ArrayList<>();

        List<String> validSuperTypes = new ArrayList<>();
        for (Map.Entry<String, List<CtType<?>>> entry : hierarchyMap.entrySet()) {
            if (entry.getValue().size() >= MIN_SUBCLASSES) {
                validSuperTypes.add(entry.getKey());
            }
        }

        for (int i = 0; i < validSuperTypes.size(); i++) {
            for (int j = i + 1; j < validSuperTypes.size(); j++) {
                String superA = validSuperTypes.get(i);
                String superB = validSuperTypes.get(j);

                List<CtType<?>> subTypesA = hierarchyMap.get(superA);
                List<CtType<?>> subTypesB = hierarchyMap.get(superB);

                CtType<?> subTypeAFirst = hierarchyMap.get(superA).getFirst();


                int nocA = subTypesA.size();
                int nocB = subTypesB.size();
                int ditA = getHierarchyMaxDIT(subTypesA);
                int ditB = getHierarchyMaxDIT(subTypesB);

                // If the hierarchies are fundamentally different shapes, reject them immediately.
                if (Math.abs(nocA - nocB) > 1 || Math.abs(ditA - ditB) > 1) {
                    continue;
                }

                boolean isLexicalMatch = shareCoreConcept(
                        superA.substring(superA.lastIndexOf('.') + 1),
                        superB.substring(superB.lastIndexOf('.') + 1)
                );

                int parallelLinks = countParallelLinks(subTypesA, subTypesB);
                int maxExpectedLinks = Math.max(nocA, nocB);

                double structuralCoupling = (double) parallelLinks / maxExpectedLinks;

                // Rule A: Overwhelming Structural Evidence (e.g., > 50% coupled).
                // Names don't matter, the code proves they are parallel.
                boolean isStronglyCoupled = structuralCoupling >= 0.5;

                // Rule B: High Suspicion + Minimum Evidence.
                // They look identical (Shape + Name), so even a single link proves the smell.
                boolean isSuspiciousAndLinked = isLexicalMatch && (parallelLinks > 0);

                if (isStronglyCoupled || isSuspiciousAndLinked) {
                    String info = String.format(
                            "Parallel Hierarchy detected with '%s'. Shape Match (NOC: %d vs %d, DIT: %d vs %d). Structural Coupling: %.0f%%.",
                            superB, nocA, nocB, ditA, ditB, structuralCoupling * 100
                    );
                    reports.add(createReport(superA, info, subTypeAFirst));
                }
            }
        }

        return reports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        if (!type.getPosition().isValidPosition()) return new ArrayList<>();

        // 1. Check Superclass
        CtTypeReference<?> superClass = type.getSuperclass();
        if (superClass != null && isProjectClass(superClass)) {
            addToHierarchy(superClass.getQualifiedName(), type);
        }

        // 2. Check Super Interfaces
        for (CtTypeReference<?> superInterface : type.getSuperInterfaces()) {
            if (isProjectClass(superInterface)) {
                addToHierarchy(superInterface.getQualifiedName(), type);
            }
        }

        return new ArrayList<>();
    }

    /**
     * Checks if the subtypes of Hierarchy A systematically depend on the subtypes of Hierarchy B.
     */
    private int countParallelLinks(List<CtType<?>> subTypesA, List<CtType<?>> subTypesB) {
        int links = 0;

        // Keep track of matched B-types so we ensure a 1-to-1 mapping
        Set<CtType<?>> matchedB = new HashSet<>();

        for (CtType<?> typeA : subTypesA) {

            // Extract all the dependencies that TypeA has
            Set<String> dependenciesOfA = extractDependencies(typeA);

            for (CtType<?> typeB : subTypesB) {
                if (matchedB.contains(typeB)) continue;

                // Extract all the dependencies that TypeB has
                Set<String> dependenciesOfB = extractDependencies(typeB);

                // Is there a structural bridge between these two specific subclasses?
                boolean aDependsOnB = dependenciesOfA.contains(typeB.getQualifiedName());
                boolean bDependsOnA = dependenciesOfB.contains(typeA.getQualifiedName());

                // Optional Fallback: Check if their names mirror each other (e.g. Car -> CarBuilder)
                // This catches parallel hierarchies that might not directly reference each other yet.
                boolean nameMirror = shareCoreConcept(typeA.getSimpleName(), typeB.getSimpleName());

                if (aDependsOnB || bDependsOnA || nameMirror) {
                    links++;
                    matchedB.add(typeB);
                    break; // Move on to the next TypeA to enforce 1-to-1 mapping
                }
            }
        }
        return links;
    }

    /**
     * Extracts all classes that this type relies on (Fields, Parameters, Return Types).
     */
    private Set<String> extractDependencies(CtType<?> type) {
        Set<String> deps = new HashSet<>();
        List<CtTypeReference<?>> references = type.getElements(new TypeFilter<>(CtTypeReference.class));

        for (CtTypeReference<?> ref : references) {
            if (!ref.isPrimitive()) {
                deps.add(ref.getQualifiedName());
            }
        }
        return deps;
    }

    /**
     * A lightweight heuristic to catch name mirroring.
     * e.g., If we strip "Impl", "Builder", "Factory", do they share a core name?
     */
    private boolean shareCoreConcept(String nameA, String nameB) {
        // Find the longest common substring starting at the beginning
        // (Assuming standard prefix naming like 'Car' and 'CarBuilder')
        int minLength = Math.min(nameA.length(), nameB.length());
        int matchLength = 0;

        for (int i = 0; i < minLength; i++) {
            if (nameA.charAt(i) == nameB.charAt(i)) {
                matchLength++;
            } else {
                break;
            }
        }

        // If they share at least 4 starting characters (e.g. 'User'), we count it as a name mirror.
        return matchLength >= 4;
    }

    private void addToHierarchy(String superTypeName, CtType<?> subType) {
        hierarchyMap.putIfAbsent(superTypeName, new ArrayList<>());
        hierarchyMap.get(superTypeName).add(subType);
    }

    private boolean isProjectClass(CtTypeReference<?> ref) {
        // Ignore standard Java libraries to focus only on internal architecture
        return !ref.getQualifiedName().startsWith("java.");
    }

    /**
     * Calculates the Depth of Inheritance Tree (DIT) for a given class.
     * Object is considered depth 0.
     */
    private int calculateDIT(CtType<?> type) {
        int depth = 1; // Start at 1 for the class itself
        CtTypeReference<?> superClass = type.getSuperclass();

        while (superClass != null && !superClass.getSimpleName().equals("Object")) {
            depth++;

            // Move up the tree. Note: In Spoon, getting the declaration of a reference
            // can sometimes be null if the source code isn't available, so we must null-check.
            CtType<?> superDeclaration = superClass.getTypeDeclaration();
            if (superDeclaration == null) {
                break;
            }
            superClass = superDeclaration.getSuperclass();
        }
        return depth;
    }

    /**
     * Finds the maximum depth within a specific hierarchy to determine its overall shape.
     */
    private int getHierarchyMaxDIT(List<CtType<?>> subTypes) {
        int maxDepth = 0;
        for (CtType<?> type : subTypes) {
            maxDepth = Math.max(maxDepth, calculateDIT(type));
        }
        return maxDepth;
    }

    private ReportStruct createReport(String className, String info, CtType<?> type) {
        // Because this smell applies to the *relationship* between hierarchies,
        // we flag the root interface/superclass itself.
        String filePath = type.getPosition().getFile().getPath();

        ReportStruct report = new ReportStruct(
                getSmellName(),
                filePath, // Path might not be singular
                this.inputDirPath,
                className,
                info
        );

        report.addLineNumber(1);

        return report;
    }
}