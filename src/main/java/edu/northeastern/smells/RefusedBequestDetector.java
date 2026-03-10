package edu.northeastern.smells;

import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class RefusedBequestDetector extends AbstractDetector{

    public RefusedBequestDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Refused Bequest";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        CtTypeReference<?> superRef = type.getSuperclass();
        if(superRef == null || "java.lang.Object".equals(superRef.getQualifiedName())) {
            return detectedLines;
        }

        CtType<?> parentClass = superRef.getTypeDeclaration();
        if(parentClass == null) {
            return detectedLines;
        }

        // check if inherits at all
        for(CtMethod<?> parentMethod : parentClass.getMethods()) {
            if(!parentMethod.isAbstract() && isEmptyBody(parentMethod)) {
                CtMethod<?> childOverride = type.getMethod(parentMethod.getSignature());

                if(childOverride == null) {
                    if(type.getPosition().isValidPosition()) {
                        detectedLines.add(type.getPosition().getLine());
                    }
                }
            }
        }

        for(CtMethod<?> method : type.getMethods()) {
            if(!hasOverrideAnnotation(method)) {
                continue;
            }

            if(method.getBody() == null) continue; // should flag as refused bequest

            // check if throws specific non-implementation exceptions and check if empty body with void return
            if(throwsRefusalException(method) || isEmptyBody(method)) {
                detectedLines.add(method.getPosition().getLine());
            }

            // check for single line throws as well for robustness
            if(method.getBody().getStatements().size() == 1) {
                if(method.getBody().getStatement(0) instanceof CtThrow) {
                    detectedLines.add(method.getPosition().getLine());
                }
            }
        }

        return detectedLines;
    }

    private boolean isEmptyBody(CtMethod<?> method) {
        return method.getType().getSimpleName().equals("void") && method.getBody() != null && method.getBody().getStatements().isEmpty();
    }

    private boolean hasOverrideAnnotation(CtMethod<?> method) {
        return method.getAnnotations().stream()
                .anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }

    private boolean throwsRefusalException(CtMethod<?> method) {
        List<CtThrow> throwStmts = method.getElements(new TypeFilter<>(CtThrow.class));

        for(CtThrow t : throwStmts) {
            CtTypeReference<?> exceptionType = t.getThrownExpression().getType();
            if(exceptionType == null) continue;

            String name = exceptionType.getSimpleName();

            if(name.contains("UnsupportedOperation") ||
            name.contains("NotImplemented") ||
            name.contains("IllegalState")) {
                return true;
            }
        }
        return false;
    }
}
