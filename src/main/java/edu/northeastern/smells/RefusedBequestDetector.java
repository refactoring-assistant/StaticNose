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

        // --- THE FIX: Gather BOTH Superclasses and Interfaces ---
        List<CtType<?>> parentTypes = new ArrayList<>();

        // 1. Get the extended Superclass
        CtTypeReference<?> superRef = type.getSuperclass();
        if (superRef != null && !"java.lang.Object".equals(superRef.getQualifiedName())) {
            CtType<?> parentClass = superRef.getTypeDeclaration();
            if (parentClass != null) parentTypes.add(parentClass);
        }

        // 2. Get all implemented Interfaces
        for (CtTypeReference<?> interfaceRef : type.getSuperInterfaces()) {
            CtType<?> parentInterface = interfaceRef.getTypeDeclaration();
            if (parentInterface != null) parentTypes.add(parentInterface);
        }

        // If it doesn't inherit or implement anything, there is no bequest to refuse!
        if (parentTypes.isEmpty()) {
            return detectedLines;
        }

        // 1. Check if the child completely ignores an empty method from ANY parent
        for (CtType<?> parent : parentTypes) {
            for(CtMethod<?> parentMethod : parent.getMethods()) {

                // We only care about concrete methods that are empty (interfaces can have default methods!)
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

        // 2. Check the child's overridden methods (Your existing logic)
        for(CtMethod<?> method : type.getMethods()) {
            if(!hasOverrideAnnotation(method)) continue;
            if(method.getBody() == null) continue;

            // Flag if the method is intentionally left completely empty
            if(isEmptyBody(method)) {
                detectedLines.add(method.getPosition().getLine());
                continue;
            }

            // REFINED LOGIC: Flag ONLY if the single line is a refusal exception
            if(isSingleLineRefusalThrow(method)) {
                detectedLines.add(method.getPosition().getLine());
            }
        }

        // 3. check for implicit refusal
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

        // 1. Truly empty (no statements at all)
        if (statements.isEmpty()) {
            return true;
        }

        // 2. The "Fake Empty" (Only contains a 'return;' statement)
        if (statements.size() == 1) {
            CtStatement onlyStatement = statements.get(0);

            // If the only statement is a return statement...
            if (onlyStatement instanceof spoon.reflect.code.CtReturn<?> returnStmt) {
                // ...and it doesn't actually return a value (e.g., 'return;')
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

    // The new, hyper-specific heuristic
    private boolean isSingleLineRefusalThrow(CtMethod<?> method) {
        // 1. Must be exactly one statement
        if (method.getBody().getStatements().size() != 1) return false;

        CtStatement onlyStatement = method.getBody().getStatement(0);

        // 2. That statement must be a throw
        if (!(onlyStatement instanceof CtThrow throwStmt)) return false;

        CtTypeReference<?> exceptionType = throwStmt.getThrownExpression().getType();
        if (exceptionType == null) return false;

        String name = exceptionType.getSimpleName();

        // 3. That throw must be a refusal exception
        return name.contains("UnsupportedOperation") || name.contains("NotImplemented");
    }

    /**
     * Calculates the Base Class Usage Ratio (BUR).
     * If a subclass adds a lot of its own methods but uses very few of the
     * methods it inherited, it is implicitly refusing the bequest.
     */
    private boolean isImplicitRefusal(CtType<?> child, CtType<?> parent) {

        // 1. Gather all inheritable methods from the parent
        List<CtMethod<?>> inheritableMethods = new ArrayList<>();
        for (CtMethod<?> m : parent.getMethods()) {
            // Ignore private methods (child can't use them anyway) and standard Object methods
            if ((m.isPublic() || m.isProtected()) && !isObjectMethod(m)) {
                inheritableMethods.add(m);
            }
        }

        // If the parent is too small, we can't mathematically prove a refusal
        if (inheritableMethods.size() < 3) return false;

        int usedMethodsCount = 0;
        List<CtInvocation<?>> childInvocations = child.getElements(new TypeFilter<>(CtInvocation.class));

        // 2. Check how many parent methods the child actually used
        for (CtMethod<?> parentMethod : inheritableMethods) {
            String signature = parentMethod.getSignature();

            // A. Did the child override it? (Checking explicitly declared methods)
            boolean isOverridden = child.getMethods().stream()
                    .anyMatch(m -> m.getSignature().equals(signature));

            if (isOverridden) {
                usedMethodsCount++;
                continue;
            }

            // B. Did the child invoke it? (e.g., calling this.getLength())
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

        // 3. Check how many BRAND NEW methods the child added
        long newMethodsAdded = child.getMethods().stream()
                .filter(m -> !hasOverrideAnnotation(m)) // Exclude overrides
                .count();

        // THE HEURISTIC:
        // If it uses less than a third of the parent's API (< 0.34)
        // AND it adds a significant amount of its own independent behavior (>= 3)
        // Then it is a Refused Bequest!
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