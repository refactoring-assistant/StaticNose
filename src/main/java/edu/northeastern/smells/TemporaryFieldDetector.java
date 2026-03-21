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

            //assigned at declaration
            if(field.getDefaultExpression() != null) {
                continue;
            }

            boolean inConstructor = false;

            if(type instanceof CtClass<?> clazz) {
                @SuppressWarnings("unchecked")
                Set<CtConstructor<?>> constructors = (Set<CtConstructor<?>>) (Set) clazz.getConstructors();

                if(!constructors.isEmpty()) {
                    boolean allConstructorsInit = true;
                    for(CtConstructor<?> constructor : constructors) {
                        // start of recursion to check all branches and method invocations in constructor
                        if(!isGuaranteedAssignment(constructor.getBody(), field, new HashSet<>())) {
                            allConstructorsInit = false;
                            break;
                        }
                    }

                    inConstructor = allConstructorsInit;
                }
            } else {
                inConstructor = true;
            }

            Set<String> usageMethods = new HashSet<>();

            List<CtFieldAccess<?>> globalAccesses = field.getFactory().getModel().getElements(new TypeFilter<>(CtFieldAccess.class) {
                @Override
                public boolean matches(CtFieldAccess<?> element) {
                    // Make sure we are talking about THIS exact field declaration
                    return super.matches(element) &&
                            element.getVariable().getDeclaration() != null &&
                            element.getVariable().getDeclaration().equals(field);
                }
            });

            for(CtFieldAccess<?> access : globalAccesses) {
                CtMethod<?> method = access.getParent(CtMethod.class);
                if(method != null) {
                    // Use getSignature() to uniquely identify methods across different classes
                    usageMethods.add(method.getParent(CtType.class).getQualifiedName() + "#" + method.getSignature());
                }
            }

            boolean hasCodeSmell = false;

            if (usageMethods.size() == 1) {
                if (!inConstructor) {
                    hasCodeSmell = true;
                }
            } else if (usageMethods.isEmpty() && !inConstructor) {
                hasCodeSmell = true;
            }

            if(hasCodeSmell) {
                if(field.getPosition().isValidPosition()) {
                    detectedLines.add(field.getPosition().getLine());
                }
            }
        }

        return detectedLines;
    }

    private boolean isGuaranteedAssignment(CtElement element, CtField<?> targetField, Set<String> visitedMethods) {
        if(element == null) return false;

        // check statement in block
        if(element instanceof CtBlock<?> block) {
            for(CtStatement stmt : block.getStatements()) {
                if(isGuaranteedAssignment(stmt, targetField, visitedMethods)) {
                    return true;
                }
            }
            return false;
        }

        // check if the actual assignment
        if(element instanceof CtAssignment<?, ?> assign) {
            CtExpression<?> assigned = assign.getAssigned();
            if(assigned instanceof CtFieldAccess<?> access) {
                return access.getVariable().getSimpleName().equals(targetField.getSimpleName());
            }
            return false;
        }

        // check method invocations
        if(element instanceof CtInvocation<?> invocation) {
            CtExecutable<?> executable = invocation.getExecutable().getDeclaration();

            if(executable == null || executable.getBody() == null) return false;

            String sig = executable.getSignature();

            if(visitedMethods.contains(sig)) return false;

            Set<String> newVisited = new HashSet<>(visitedMethods);
            newVisited.add(sig);
            return isGuaranteedAssignment(executable.getBody(), targetField, newVisited);
        }

        // check inside if
        if(element instanceof CtIf ifStmt) {
            CtStatement thenStmt = ifStmt.getThenStatement();
            CtStatement elseStmt = ifStmt.getElseStatement();

            if(elseStmt == null) return false;

            return isGuaranteedAssignment(thenStmt, targetField, visitedMethods) &&
                    isGuaranteedAssignment(elseStmt, targetField, visitedMethods);
        }

        // check inside switch
        if(element instanceof CtSwitch<?> switchStmt) {
            boolean hasDefault = false;

            for(CtCase<?> c : switchStmt.getCases()) {
                if(c.getCaseExpression() == null) hasDefault = true;

                if(!isGuaranteedAssignment(c, targetField, visitedMethods)) {
                    return false;
                }
            }

            return hasDefault;
        }

        // check inside case of switch
        if(element instanceof CtCase<?> c) {
            for (CtStatement stmt : c.getStatements()) {
                if(isGuaranteedAssignment(stmt, targetField, visitedMethods)) return true;
            }
            return false;
        }

        // if inside loop, execution is not guaranteed, so it might be a temporary field
        if(element instanceof CtLoop) {
            return false;
        }

        // check inside try statements (less strict check)
        if(element instanceof CtTry tryStmt) {
            return isGuaranteedAssignment(tryStmt.getBody(), targetField, visitedMethods);
        }

        return false;
    }
}
