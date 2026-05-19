package edu.northeastern.smells;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * This class is a Feature Envy detector.
 * Feature envy is when a method accesses more fields of a specific foreign class
 * compared to its own class hierarchy's fields.
 */
public class FeatureEnvyDetector extends AbstractDetector{

    // A method must access at least this many foreign fields/getters from a SINGLE
    // external class to even be considered for Feature Envy. Filters out basic comparators.
    private final int FOREIGN_DATA_THRESHOLD;

    public FeatureEnvyDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        FOREIGN_DATA_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "FOREIGN_DATA_THRESHOLD", 2);
    }

    @Override
    protected String getSmellName() {
        return "Feature Envy";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();
        CtTypeReference<?> currentTypeRef = type.getReference();

        for(CtMethod<?> method : type.getMethods()) {
            if(method.getBody() == null) continue;

            // TRACK UNIQUE FEATURES
            // Internal: Unique field names/getter signatures from this class hierarchy
            Set<String> internalFeatures = new HashSet<>();

            // External: Map of ClassName -> Set of unique features accessed in that class
            Map<String, Set<String>> externalAccessMap = new HashMap<>();

            // 1. Scan for direct field accesses
            List<CtFieldAccess<?>> fieldAccesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));
            for(CtFieldAccess<?> access : fieldAccesses) {
                if(access.getVariable().isStatic()) continue;

                CtTypeReference<?> declaringType = access.getVariable().getDeclaringType();
                if(declaringType == null) continue;

                String fieldName = access.getVariable().getSimpleName();

                if(currentTypeRef.isSubtypeOf(declaringType)) {
                    internalFeatures.add(fieldName);
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(isProjectClass(targetName)) {
                        externalAccessMap.computeIfAbsent(targetName, k -> new HashSet<>()).add(fieldName);
                    }
                }
            }

            // 2. Scan for getter invocations
            List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
            for(CtInvocation<?> invocation: invocations) {
                if(invocation.getExecutable().isStatic()) continue;

                CtTypeReference<?> declaringType = invocation.getExecutable().getDeclaringType();
                if(declaringType == null) continue;

                String methodSig = invocation.getExecutable().getSignature();

                if(currentTypeRef.isSubtypeOf(declaringType)) {
                    internalFeatures.add(methodSig);
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(isProjectClass(targetName) && isGetter(invocation)) {
                        externalAccessMap.computeIfAbsent(targetName, k -> new HashSet<>()).add(methodSig);
                    }
                }
            }

            // 3. Calculate scores based on UNIQUE feature counts
            int internalAccessCount = internalFeatures.size();
            int maxExternalAccessesToSingleClass = 0;

            for (Set<String> uniqueFeatures : externalAccessMap.values()) {
                maxExternalAccessesToSingleClass = Math.max(maxExternalAccessesToSingleClass, uniqueFeatures.size());
            }

            // Threshold Check
            if(maxExternalAccessesToSingleClass > internalAccessCount &&
                    maxExternalAccessesToSingleClass >= FOREIGN_DATA_THRESHOLD) {
                detectedLines.add(method.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private boolean isGetter(CtInvocation<?> invocation) {
        String name = invocation.getExecutable().getSimpleName();
        int paramCount = invocation.getArguments().size();
        CtTypeReference<?> returnType = invocation.getExecutable().getType();

        if(paramCount > 0) return false;
        if(returnType != null && returnType.getSimpleName().equals("void")) return false;

        return name.startsWith("get") || name.startsWith("is") || name.startsWith("has");
    }

    private boolean isProjectClass(String qualifiedName) {
        return !qualifiedName.startsWith("java.") &&
                !qualifiedName.startsWith("javax.") &&
                !qualifiedName.startsWith("sun.") &&
                !qualifiedName.startsWith("jdk.");
    }
}