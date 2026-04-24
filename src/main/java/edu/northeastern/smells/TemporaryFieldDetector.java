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

            // Static or Final fields represent constants/shared state, not temporary algorithmic state.
            if(field.isStatic() || field.isFinal()) continue;

            // If assigned at declaration (e.g., private int count = 0;), it's persistent baseline state.
            if(field.getDefaultExpression() != null) {
                continue;
            }

            Set<String> writeExecutables = new HashSet<>();
            Set<String> readExecutables = new HashSet<>();

            // Find ALL usages of this specific field across the entire class
            List<CtFieldAccess<?>> globalAccesses = field.getFactory().getModel().getElements(new TypeFilter<>(CtFieldAccess.class) {
                @Override
                public boolean matches(CtFieldAccess<?> element) {
                    return super.matches(element) &&
                            element.getVariable().getDeclaration() != null &&
                            element.getVariable().getDeclaration().equals(field);
                }
            });

            for(CtFieldAccess<?> access : globalAccesses) {
                // Use CtExecutable to capture BOTH methods and constructors
                CtExecutable<?> executable = access.getParent(CtExecutable.class);

                if(executable != null) {
                    String execName = executable.getSimpleName();

                    if (access instanceof spoon.reflect.code.CtFieldWrite) {
                        boolean isAssigningNull = false;

                        // Verify they aren't just clearing the field (assigning 'null')
                        if (access.getParent() instanceof CtAssignment<?,?> assign) {
                            if (assign.getAssignment() instanceof CtLiteral<?> literal && literal.getValue() == null) {
                                isAssigningNull = true;
                            }
                        }

                        // If it's a real value assignment, log where it happened
                        if (!isAssigningNull) {
                            writeExecutables.add(execName);
                        }
                    } else if (access instanceof spoon.reflect.code.CtFieldRead) {
                        // Log where the field was read from
                        readExecutables.add(execName);
                    }
                }
            }

            // --- SMELL EVALUATION ---
            boolean hasCodeSmell = false;

            // THE RULE FOR TEMPORARY FIELDS:
            // It is written to in exactly ONE location (which is an algorithm, not a constructor or setter),
            // and it is actually read from (meaning it's not just dead code).
            if (writeExecutables.size() == 1 && !readExecutables.isEmpty()) {

                String theOnlyWriter = writeExecutables.iterator().next();

                // 1. If the only writer is a constructor ("<init>"), it's standard persistent state.
                // 2. If the only writer is a setter ("setX"), it's a standard POJO/DTO field.
                // 3. If the writer is anything else (e.g., "calculateTaxes"), it is acting as an
                //    elevated local variable for that specific algorithm!
                if (!theOnlyWriter.equals("<init>") && !theOnlyWriter.startsWith("set")) {
                    hasCodeSmell = true;
                }
            }

            // Flag the line number of the field declaration if it violates the rule
            if(hasCodeSmell) {
                if(field.getPosition().isValidPosition()) {
                    detectedLines.add(field.getPosition().getLine());
                }
            }
        }

        return detectedLines;
    }
}