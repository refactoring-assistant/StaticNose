package edu.northeastern.smells;

import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.code.CtInvocation;
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

        if (type.isEnum()) {
            return detectedLines;
        }

        List<CtType<?>> parentTypes = new ArrayList<>();

        CtTypeReference<?> superRef = type.getSuperclass();
        if (superRef != null && !"java.lang.Object".equals(superRef.getQualifiedName())) {
            CtType<?> parentClass = superRef.getTypeDeclaration();
            if (parentClass != null) parentTypes.add(parentClass);
        }

        for (CtTypeReference<?> interfaceRef : type.getSuperInterfaces()) {
            CtType<?> parentInterface = interfaceRef.getTypeDeclaration();
            if (parentInterface != null) parentTypes.add(parentInterface);
        }

        if (parentTypes.isEmpty()) {
            return detectedLines;
        }

        for (CtType<?> parent : parentTypes) {
            for(CtMethod<?> parentMethod : parent.getMethods()) {
                if(!parentMethod.isAbstract() && isEmptyBody(parentMethod)) {
                    CtMethod<?> childOverride = type.getMethod(parentMethod.getSignature());

                    if(childOverride == null) {
                        if(type.getPosition().isValidPosition()) {
                            detectedLines.add(type.getPosition().getLine());
                        }
                    }
                }
            }
        }

        for(CtMethod<?> method : type.getMethods()) {
            if(!hasOverrideAnnotation(method)) continue;
            if(method.getBody() == null) continue;

            if(isEmptyBody(method)) {
                detectedLines.add(method.getPosition().getLine());
                continue;
            }
            if(isSingleLineRefusalThrow(method)) {
                detectedLines.add(method.getPosition().getLine());
            }
        }

        for (CtType<?> parent : parentTypes) {
            if (isImplicitRefusal(type, parent)) {
                if (type.getPosition().isValidPosition()) {
                    detectedLines.add(type.getPosition().getLine());
                }
            }
        }

        return detectedLines;
    }

    private boolean isEmptyBody(CtMethod<?> method) {
        if (!method.getType().getSimpleName().equals("void") || method.getBody() == null) {
            return false;
        }

        List<CtStatement> statements = method.getBody().getStatements();

        if (statements.isEmpty()) {
            return true;
        }

        if (statements.size() == 1) {
            CtStatement onlyStatement = statements.get(0);

            if (onlyStatement instanceof spoon.reflect.code.CtReturn<?> returnStmt) {
                if (returnStmt.getReturnedExpression() == null) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasOverrideAnnotation(CtMethod<?> method) {
        return method.getAnnotations().stream()
                .anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"));
    }

    private boolean isSingleLineRefusalThrow(CtMethod<?> method) {
        if (method.getBody().getStatements().size() != 1) return false;

        CtStatement onlyStatement = method.getBody().getStatement(0);

        if (!(onlyStatement instanceof CtThrow throwStmt)) return false;

        CtTypeReference<?> exceptionType = throwStmt.getThrownExpression().getType();
        if (exceptionType == null) return false;

        String name = exceptionType.getSimpleName();

        return name.contains("UnsupportedOperation") || name.contains("NotImplemented");
    }

    /**
     * Calculates the Base Class Usage Ratio (BUR).
     * If a subclass adds a lot of its own methods but uses very few of the
     * methods it inherited, it is implicitly refusing the bequest.
     */
    private boolean isImplicitRefusal(CtType<?> child, CtType<?> parent) {

        List<CtMethod<?>> inheritableMethods = new ArrayList<>();
        for (CtMethod<?> m : parent.getMethods()) {
            if ((m.isPublic() || m.isProtected()) && !isObjectMethod(m)) {
                inheritableMethods.add(m);
            }
        }

        if (inheritableMethods.size() < 3) return false;

        int usedMethodsCount = 0;
        List<CtInvocation<?>> childInvocations = child.getElements(new TypeFilter<>(CtInvocation.class));

        for (CtMethod<?> parentMethod : inheritableMethods) {
            String signature = parentMethod.getSignature();

            boolean isOverridden = child.getMethods().stream()
                    .anyMatch(m -> m.getSignature().equals(signature));

            if (isOverridden) {
                usedMethodsCount++;
                continue;
            }

            boolean isInvoked = false;
            for (CtInvocation<?> inv : childInvocations) {
                if (inv.getExecutable().getSignature().equals(signature)) {
                    isInvoked = true;
                    break;
                }
            }

            if (isInvoked) {
                usedMethodsCount++;
            }
        }

        double usageRatio = (double) usedMethodsCount / inheritableMethods.size();

        long newMethodsAdded = child.getMethods().stream()
                .filter(m -> !hasOverrideAnnotation(m)) // Exclude overrides
                .count();

        return usageRatio < 0.34 && newMethodsAdded >= 3;
    }

    private boolean isObjectMethod(CtMethod<?> method) {
        String name = method.getSimpleName();
        return name.equals("equals") || name.equals("hashCode") ||
                name.equals("toString") || name.equals("clone") ||
                name.equals("getClass") || name.equals("notify") ||
                name.equals("notifyAll") || name.equals("wait");
    }
}