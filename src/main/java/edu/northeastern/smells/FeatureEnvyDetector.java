package edu.northeastern.smells;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class FeatureEnvyDetector extends AbstractDetector{

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

        for(CtMethod<?> method : type.getMethods()) {

            if(method.getBody() == null) continue;

            int internalAccessCount = 0;
            int externalAccessCount = 0;
            // Map<String, Integer> externalAccessMap = new HashMap<>();

            // scan for field access

            List<CtFieldAccess<?>> fieldAccesses = method.getElements(new TypeFilter<>(CtFieldAccess.class));
            for(CtFieldAccess<?> access : fieldAccesses) {
                if(access.getVariable().isStatic()) continue;

                // if field accessed is its own class'

                CtTypeReference<?> declaringType = access.getVariable().getDeclaringType();
                if(declaringType == null) continue;

                if(declaringType.getQualifiedName().equals(type.getQualifiedName())) {
                    internalAccessCount++;
                } else {
                    String targetName = declaringType.getQualifiedName();
                    if(!isStandardLibrary(targetName)) {
                        externalAccessCount++;
                    }
                }
            }

            // check for method invocations (only getters)
            List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));

            for(CtInvocation<?> invocation: invocations) {
                // ignores utility calls
                if(invocation.getExecutable().isStatic()) continue;

                CtTypeReference<?> declaringType = invocation.getExecutable().getDeclaringType();
                if(declaringType == null) continue;
                String targetName = declaringType.getQualifiedName();

                if(targetName.equals(type.getQualifiedName())) {
                    internalAccessCount++;
                } else if(!isStandardLibrary(targetName)) {
                    if(isGetter(invocation)) {
                        externalAccessCount++;
                    }
                }
            }

            // calculate scores

            if(externalAccessCount > internalAccessCount) {
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

    private boolean isStandardLibrary(String qualifiedName) {
        return qualifiedName.startsWith("java.") ||
                qualifiedName.startsWith("javax.") ||
                qualifiedName.startsWith("sun.") ||
                qualifiedName.startsWith("jdk.");
    }
}
