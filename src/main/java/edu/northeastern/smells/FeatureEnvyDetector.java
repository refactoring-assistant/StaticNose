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

            Set<String> internalFeatures = new HashSet<>();

            Map<String, Set<String>> externalInstanceAccessMap = new HashMap<>();

            List<CtFieldAccess<?>> fieldAccesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));
            for(CtFieldAccess<?> access : fieldAccesses) {
                if(access.getVariable().isStatic()) continue;

                CtTypeReference<?> declaringType = access.getVariable().getDeclaringType();
                if(declaringType == null) continue;

                String fieldName = access.getVariable().getSimpleName();

                if(currentTypeRef.isSubtypeOf(declaringType)) {
                    CtTypeReference<?> fieldType = access.getVariable().getType();
                    if (fieldType != null && !currentTypeRef.isSubtypeOf(fieldType) && isProjectClass(fieldType.getQualifiedName())) {
                        String instanceKey = fieldType.getQualifiedName() + "::" + fieldName;
                        externalInstanceAccessMap.computeIfAbsent(instanceKey, k -> new HashSet<>()).add("ref");
                    } else {
                        internalFeatures.add(fieldName);
                    }
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(isProjectClass(targetName)) {
                        String instanceId = "unknown";
                        if (access.getTarget() != null) {
                            instanceId = access.getTarget().toString();
                            if (instanceId.startsWith("this.")) {
                                instanceId = instanceId.substring(5);
                            }
                        }
                        String instanceKey = targetName + "::" + instanceId;
                        externalInstanceAccessMap.computeIfAbsent(instanceKey, k -> new HashSet<>()).add(fieldName);
                    }
                }
            }

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
                        String instanceId = "unknown";
                        if (invocation.getTarget() != null) {
                            instanceId = invocation.getTarget().toString();
                            if (instanceId.startsWith("this.")) {
                                instanceId = instanceId.substring(5);
                            }
                        }
                        String instanceKey = targetName + "::" + instanceId;
                        externalInstanceAccessMap.computeIfAbsent(instanceKey, k -> new HashSet<>()).add(methodSig);
                    }
                }
            }

            int internalAccessCount = internalFeatures.size();
            int maxExternalAccessesToSingleInstance = 0;

            for (Set<String> uniqueFeatures : externalInstanceAccessMap.values()) {
                maxExternalAccessesToSingleInstance = Math.max(maxExternalAccessesToSingleInstance, uniqueFeatures.size());
            }

            if(maxExternalAccessesToSingleInstance > internalAccessCount &&
                    maxExternalAccessesToSingleInstance >= FOREIGN_DATA_THRESHOLD) {
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
}