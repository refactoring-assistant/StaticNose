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

            Set<CtExecutable<?>> writeExecutables = new HashSet<>();
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
                            writeExecutables.add(executable);
                        }
                    } else if (access instanceof spoon.reflect.code.CtFieldRead) {
                        readExecutables.add(execName);
                    }
                }
            }

            boolean hasCodeSmell = false;

            if (writeExecutables.size() == 1 && !readExecutables.isEmpty()) {

                CtExecutable<?> theOnlyWriterExec = writeExecutables.iterator().next();
                String theOnlyWriter = theOnlyWriterExec.getSimpleName();

                if (!theOnlyWriter.equals("<init>") && !theOnlyWriter.startsWith("set")) {
                    hasCodeSmell = true;

                    if (theOnlyWriterExec instanceof CtMethod<?> method && method.isPrivate()) {
                        CtType<?> parentType = method.getParent(CtType.class);
                        if (parentType != null) {
                            for (CtConstructor<?> constructor : parentType.getElements(new TypeFilter<>(CtConstructor.class))) {
                                List<CtInvocation<?>> invocations = constructor.getElements(new TypeFilter<>(CtInvocation.class) {
                                    @Override
                                    public boolean matches(CtInvocation<?> element) {
                                        return super.matches(element) && 
                                               element.getExecutable().getDeclaration() != null && 
                                               element.getExecutable().getDeclaration().equals(method);
                                    }
                                });
                                
                                for (CtInvocation<?> invocation : invocations) {
                                    if (isUnconditional(invocation, constructor)) {
                                        hasCodeSmell = false;
                                        break;
                                    }
                                }
                                if (!hasCodeSmell) break;
                            }
                        }
                    }
                }
            }

            Set<CtConstructor<?>> allConstructors = new HashSet<>(type.getElements(new TypeFilter<>(CtConstructor.class)));
            if (!hasCodeSmell && allConstructors.size() > 1 && !readExecutables.isEmpty()) {
                int totalConstructors = allConstructors.size();
                Set<CtConstructor<?>> writingConstructors = new HashSet<>();
                boolean onlyWrittenInConstructors = true;

                for (CtExecutable<?> exec : writeExecutables) {
                    if (exec instanceof CtConstructor<?>) {
                        writingConstructors.add((CtConstructor<?>) exec);
                    } else {
                        onlyWrittenInConstructors = false;
                    }
                }

                if (onlyWrittenInConstructors && !writingConstructors.isEmpty()) {
                    int coveredConstructors = writingConstructors.size();
                    for (CtConstructor<?> ctor : allConstructors) {
                        if (!writingConstructors.contains(ctor) && delegatesToThis(ctor, type)) {
                            coveredConstructors++;
                        }
                    }

                    if (coveredConstructors < totalConstructors) {
                        hasCodeSmell = true;
                    }
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

    private boolean isUnconditional(CtElement element, CtElement stopAt) {
        CtElement current = element.getParent();
        while (current != null && current != stopAt) {
            if (current instanceof CtIf ||
                current instanceof CtLoop ||
                current instanceof CtSwitch ||
                current instanceof CtConditional ||
                current instanceof CtLambda ||
                current instanceof CtType ||
                current instanceof CtCatch) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    private boolean delegatesToThis(CtConstructor<?> ctor, CtType<?> currentType) {
        if (ctor.getBody() == null || ctor.getBody().getStatements().isEmpty()) return false;
        CtStatement first = ctor.getBody().getStatements().get(0);
        if (first instanceof CtInvocation<?> inv) {
            if (inv.getExecutable() != null && inv.getExecutable().isConstructor()) {
                return currentType.getReference().equals(inv.getExecutable().getDeclaringType());
            }
        }
        return false;
    }
}