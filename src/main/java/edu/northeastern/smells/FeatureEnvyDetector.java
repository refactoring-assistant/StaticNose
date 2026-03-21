package edu.northeastern.smells;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a Feature Envy detector.
 * Feature envy is when a method accesses more fields of a specific foreign class
 * compared to its own class hierarchy's fields.
 */
public class FeatureEnvyDetector extends AbstractDetector{

    // A method must access at least this many foreign fields/getters from a SINGLE
    // external class to even be considered for Feature Envy. Filters out basic comparators.
    private static final int FOREIGN_DATA_THRESHOLD = 3;

    public FeatureEnvyDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
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

            int internalAccessCount = 0;

            // Re-enabled your map! We need to track Envy towards SPECIFIC classes.
            Map<String, Integer> externalAccessMap = new HashMap<>();

            // 1. Scan for direct field accesses
            List<CtFieldAccess<?>> fieldAccesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));
            for(CtFieldAccess<?> access : fieldAccesses) {
                if(access.getVariable().isStatic()) continue;

                CtTypeReference<?> declaringType = access.getVariable().getDeclaringType();
                if(declaringType == null) continue;

                // FIX: Check if the current class IS A subtype of the declaring class.
                // This covers the class itself AND all superclasses/abstract parents!
                if(currentTypeRef.isSubtypeOf(declaringType)) {
                    internalAccessCount++;
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(isProjectClass(targetName)) {
                        externalAccessMap.put(targetName, externalAccessMap.getOrDefault(targetName, 0) + 1);
                    }
                }
            }

            // 2. Scan for getter invocations
            List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
            for(CtInvocation<?> invocation: invocations) {
                if(invocation.getExecutable().isStatic()) continue;

                CtTypeReference<?> declaringType = invocation.getExecutable().getDeclaringType();
                if(declaringType == null) continue;

                // FIX: Apply the exact same superclass check here
                if(currentTypeRef.isSubtypeOf(declaringType)) {
                    internalAccessCount++;
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(isProjectClass(targetName) && isGetter(invocation)) {
                        externalAccessMap.put(targetName, externalAccessMap.getOrDefault(targetName, 0) + 1);
                    }
                }
            }

            // 3. Calculate scores based on the most envied class
            int maxExternalAccessesToSingleClass = 0;
            for (int count : externalAccessMap.values()) {
                if (count > maxExternalAccessesToSingleClass) {
                    maxExternalAccessesToSingleClass = count;
                }
            }

            // To be Feature Envy, it must envy a specific class MORE than itself,
            // AND pass the minimum threshold (to avoid flagging 2-line parameter checks)
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