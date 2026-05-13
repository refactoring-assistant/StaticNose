package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemporaryFieldDetector extends AbstractDetector{

    public TemporaryFieldDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Temporary Field";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {

        List<Integer> detectedLines = new ArrayList<>();

        for(CtField<?> field : type.getFields()) {

            if(field.isStatic() || field.isFinal()) continue;

            if(field.getDefaultExpression() != null) {
                continue;
            }

            Set<String> writeExecutables = new HashSet<>();
            Set<String> readExecutables = new HashSet<>();

            List<CtFieldAccess<?>> globalAccesses = field.getFactory().getModel().getElements(new TypeFilter<>(CtFieldAccess.class) {
                @Override
                public boolean matches(CtFieldAccess<?> element) {
                    return super.matches(element) &&
                            element.getVariable().getDeclaration() != null &&
                            element.getVariable().getDeclaration().equals(field);
                }
            });

            for(CtFieldAccess<?> access : globalAccesses) {
                CtExecutable<?> executable = access.getParent(CtExecutable.class);

                if(executable != null) {
                    String execName = executable.getSimpleName();

                    if (access instanceof spoon.reflect.code.CtFieldWrite) {
                        boolean isAssigningNull = false;

                        if (access.getParent() instanceof CtAssignment<?,?> assign) {
                            if (assign.getAssignment() instanceof CtLiteral<?> literal && literal.getValue() == null) {
                                isAssigningNull = true;
                            }
                        }

                        if (!isAssigningNull) {
                            writeExecutables.add(execName);
                        }
                    } else if (access instanceof spoon.reflect.code.CtFieldRead) {
                        readExecutables.add(execName);
                    }
                }
            }

            boolean hasCodeSmell = false;

            if (writeExecutables.size() == 1 && !readExecutables.isEmpty()) {

                String theOnlyWriter = writeExecutables.iterator().next();

                if (!theOnlyWriter.equals("<init>") && !theOnlyWriter.startsWith("set")) {
                    hasCodeSmell = true;
                }
            }

            if(hasCodeSmell) {
                if(field.getPosition().isValidPosition()) {
                    detectedLines.add(field.getPosition().getLine());
                }
            }
        }

        return detectedLines;
    }
}