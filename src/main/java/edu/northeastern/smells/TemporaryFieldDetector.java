package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TemporaryFieldDetector implements IDetector{

    List<String> javaFilePaths;

    public TemporaryFieldDetector(List<String> javaFilePaths) {
        this.javaFilePaths = javaFilePaths;
    }

    @Override
    public List<ReportStruct> run() {
        List<ReportStruct> reportStructList = new ArrayList<>();

        for(String javaFilePath: javaFilePaths) {
            List<ReportStruct> fileReportStructList = analyzeJavaFile(javaFilePath);
            reportStructList.addAll(fileReportStructList);
        }

        return reportStructList;
    }

    private List<ReportStruct> analyzeJavaFile(String javaFilePath) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(javaFilePath);
        launcher.getEnvironment().setComplianceLevel(17);
        launcher.buildModel();

        CtModel model = launcher.getModel();
        List<ReportStruct> fileReportStructList = new ArrayList<>();

        for (CtType<?> type : model.getAllTypes()) {

            ReportStruct classReportStruct = analyzeClass(type, javaFilePath);

            if(classReportStruct != null) {
                fileReportStructList.add(classReportStruct);
            }
        }

        return fileReportStructList;
    }

    private ReportStruct analyzeClass(CtType<?> type, String javaFilePath) {

        List<Integer> detectedLines = new ArrayList<>();

        for(CtField<?> field : type.getFields()) {

            if(field.isStatic() || field.isFinal()) continue;

            //assigned at declaration
            if(field.getDefaultExpression() != null) {
                continue;
            }

            boolean inConstructor = false;

            if(type instanceof spoon.reflect.declaration.CtClass) {
                spoon.reflect.declaration.CtClass<?> clazz = (spoon.reflect.declaration.CtClass<?>) type;
                @SuppressWarnings("unchecked")
                Set<CtConstructor<?>> constructors = (Set<CtConstructor<?>>) (Set) clazz.getConstructors();

                if(constructors.isEmpty()) {
                    inConstructor = false;
                } else {
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

            List<CtFieldAccess<?>> accesses = type.getElements(new TypeFilter<CtFieldAccess<?>>(CtFieldAccess.class) {
                @Override
                public boolean matches(CtFieldAccess<?> element) {
                        return super.matches(element) &&
                                element.getVariable().getSimpleName().equals(field.getSimpleName());
                    }
            });

            for(CtFieldAccess<?> access : accesses) {
                CtMethod<?> method = access.getParent(CtMethod.class);

                if(method != null) {
                    usageMethods.add(method.getSignature());
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

        boolean hasCodeSmell = !detectedLines.isEmpty();
        ReportStruct report = new ReportStruct(javaFilePath, type.getSimpleName(), hasCodeSmell);

        if(hasCodeSmell) {
            report.addLineNumbers(detectedLines);
        } else {
            report.addLineNumber(-1);
        }

        return report;
    }

    private boolean isGuaranteedAssignment(CtElement element, CtField<?> targetField, Set<String> visitedMethods) {
        if(element == null) return false;

        // check statement in block
        if(element instanceof CtBlock) {
            CtBlock<?> block = (CtBlock<?>) element;
            for(CtStatement stmt : block.getStatements()) {
                if(isGuaranteedAssignment(stmt, targetField, visitedMethods)) {
                    return true;
                }
            }
            return false;
        }

        // check if the actual assignment
        if(element instanceof CtAssignment) {
            CtAssignment<?, ?> assign = (CtAssignment<?, ?>) element;
            CtExpression<?> assigned = assign.getAssigned();
            if(assigned instanceof CtFieldAccess) {
                CtFieldAccess<?> access = (CtFieldAccess<?>) assigned;
                return access.getVariable().getSimpleName().equals(targetField.getSimpleName());
            }
            return false;
        }

        // check method invocations
        if(element instanceof CtInvocation) {
            CtInvocation<?> invocation = (CtInvocation<?>) element;
            CtExecutable<?> executable = invocation.getExecutable().getDeclaration();

            if(executable == null || executable.getBody() == null) return false;

            String sig = executable.getSignature();

            if(visitedMethods.contains(sig)) return false;

            Set<String> newVisited = new HashSet<>(visitedMethods);
            newVisited.add(sig);
            return isGuaranteedAssignment(executable.getBody(), targetField, newVisited);
        }

        // check inside if
        if(element instanceof CtIf) {
            CtIf ifStmt = (CtIf) element;
            CtStatement thenStmt = ifStmt.getThenStatement();
            CtStatement elseStmt = ifStmt.getElseStatement();

            if(elseStmt == null) return false;

            return isGuaranteedAssignment(thenStmt, targetField, visitedMethods) &&
                    isGuaranteedAssignment(elseStmt, targetField, visitedMethods);
        }

        // check inside switch
        if(element instanceof CtSwitch) {
            CtSwitch<?> switchStmt = (CtSwitch<?>) element;
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
        if(element instanceof CtCase) {
            CtCase<?> c = (CtCase<?>) element;
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
        if(element instanceof CtTry) {
            CtTry tryStmt = (CtTry) element;
            return isGuaranteedAssignment(tryStmt.getBody(), targetField, visitedMethods);
        }

        return false;
    }
}
