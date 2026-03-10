package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

/**
 * This class is ignored since it does not meaningfully solve the Dead code
 * problem beyond what standard industry tools already do.
 *
 * @deprecated
 */
@Deprecated
public class DeadCodeDetector extends AbstractDetector {

    public DeadCodeDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Dead Code";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        Map<CtNamedElement, Integer> usageMap = new HashMap<>();

        for (CtField<?> field : type.getFields()) {
            if (field.isPrivate() && !isSerialVersionUID(field)) {
                usageMap.put(field, 0);
            }
        }

        for (CtMethod<?> method : type.getMethods()) {
            if (method.isPrivate() && !isSpecialMethod(method)) {
                usageMap.put(method, 0);
            }
        }

        List<CtLocalVariable<?>> localVars = type.getElements(new TypeFilter<>(CtLocalVariable.class));
        for (CtLocalVariable<?> var : localVars) {
            usageMap.put(var, 0);
        }

        UsageScanner scanner = new UsageScanner(usageMap);
        scanner.scan(type);

        for (Map.Entry<CtNamedElement, Integer> entry : usageMap.entrySet()) {
            if (entry.getValue() == 0) {
                CtNamedElement element = entry.getKey();
                if (element.getPosition().isValidPosition()) {
                    detectedLines.add(element.getPosition().getLine());
                }
            }
        }

        detectedLines.addAll(findUnreachableCode(type));

        return detectedLines;
    }

    private List<Integer> findUnreachableCode(CtType<?> type) {
        List<Integer> lines = new ArrayList<>();
        List<CtBlock<?>> blocks = type.getElements(new TypeFilter<>(CtBlock.class));

        for (CtBlock<?> block : blocks) {
            boolean flowTerminated = false;

            for (CtStatement stmt : block.getStatements()) {
                if (flowTerminated) {
                    if (stmt.getPosition().isValidPosition()) {
                        lines.add(stmt.getPosition().getLine());
                    }
                }

                if (stmt instanceof CtReturn ||
                        stmt instanceof CtThrow ||
                        stmt instanceof CtBreak ||
                        stmt instanceof CtContinue) {
                    flowTerminated = true;
                }
            }
        }
        return lines;
    }

    private boolean isSerialVersionUID(CtField<?> field) {
        return field.getSimpleName().equals("serialVersionUID");
    }

    private boolean isSpecialMethod(CtMethod<?> method) {
        if (method.getSimpleName().equals("main") && method.isStatic()) return true;

        for (CtAnnotation<?> annotation : method.getAnnotations()) {
            String name = annotation.getAnnotationType().getSimpleName();
            if (name.equals("Test") ||
                    name.equals("Before") ||
                    name.equals("After") ||
                    name.equals("BeforeEach") ||
                    name.equals("AfterEach") ||
                    name.equals("Override") ||
                    name.equals("Bean") ||
                    name.equals("PostConstruct")) {
                return true;
            }
        }
        return false;
    }

    private static class UsageScanner extends CtScanner {
        private final Map<CtNamedElement, Integer> usageMap;

        public UsageScanner(Map<CtNamedElement, Integer> usageMap) {
            this.usageMap = usageMap;
        }

        @Override
        public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
            countReference(variableRead.getVariable());
            super.visitCtVariableRead(variableRead);
        }

        @Override
        public <T> void visitCtVariableWrite(CtVariableWrite<T> variableWrite) {
            countReference(variableWrite.getVariable());
            super.visitCtVariableWrite(variableWrite);
        }

        @Override
        public <T> void visitCtInvocation(CtInvocation<T> invocation) {
            countReference(invocation.getExecutable());
            super.visitCtInvocation(invocation);
        }

        private void countReference(spoon.reflect.reference.CtReference ref) {
            if (ref == null) return;

            CtElement declaration = ref.getDeclaration();

            if (declaration instanceof CtNamedElement && usageMap.containsKey(declaration)) {
                CtNamedElement target = (CtNamedElement) declaration;
                usageMap.put(target, usageMap.get(target) + 1);
            }
        }
    }
}