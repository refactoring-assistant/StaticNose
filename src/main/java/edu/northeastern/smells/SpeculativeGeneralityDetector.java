package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

@Deprecated
public class SpeculativeGeneralityDetector extends AbstractDetector {

    private final List<ReportStruct> customReports = new ArrayList<>();
    private final List<CtType<?>> allTypes = new ArrayList<>();
    private final Map<String, Integer> typeUsageCount = new HashMap<>();
    private final Map<String, List<CtType<?>>> childrenMap = new HashMap<>();

    public SpeculativeGeneralityDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Speculative Generality";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        for (CtType<?> type : allTypes) {
            if (type.isAbstract() || type.isInterface()) {
                String name = type.getQualifiedName();
                int usage = typeUsageCount.getOrDefault(name, 0);
                int meaningfulSubs = getMeaningfulSubclassCount(name);

                if (usage == 0 && meaningfulSubs <= 1) {
                    ReportStruct report = new ReportStruct(
                            getSmellName(),
                            type.getPosition().isValidPosition() ? type.getPosition().getFile().getPath() : "Unknown File",
                            this.inputDirPath,
                            type.getSimpleName(),
                            "Unnecessary abstract class/interface (Collapse Hierarchy)"
                    );
                    if (type.getPosition().isValidPosition()) {
                        report.addLineNumber(type.getPosition().getLine());
                    }
                    customReports.add(report);
                }
            }
        }
        
        return customReports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        if (!type.getPosition().isValidPosition()) return new ArrayList<>();
        allTypes.add(type);
        
        String filePath = type.getPosition().getFile().getPath();
        String className = type.getSimpleName();

        if (type.getSuperclass() != null && !type.getSuperclass().getQualifiedName().equals("java.lang.Object")) {
            String superName = type.getSuperclass().getQualifiedName();
            childrenMap.computeIfAbsent(superName, k -> new ArrayList<>()).add(type);
        }

        for (CtTypeReference<?> iface : type.getSuperInterfaces()) {
            String ifaceName = iface.getQualifiedName();
            childrenMap.computeIfAbsent(ifaceName, k -> new ArrayList<>()).add(type);
        }

        CtScanner scanner = new CtScanner() {
            @Override
            public <T> void visitCtTypeReference(CtTypeReference<T> reference) {
                if (reference != null && !reference.isImplicit()) {
                    CtElement parent = reference.getParent();
                    boolean isExtendsOrImplements = false;
                    if (parent instanceof CtType<?> typeParent) {
                        if (reference.equals(typeParent.getSuperclass()) || typeParent.getSuperInterfaces().contains(reference)) {
                            isExtendsOrImplements = true;
                        }
                    }
                    if (!isExtendsOrImplements) {
                        String name = reference.getQualifiedName();
                        typeUsageCount.put(name, typeUsageCount.getOrDefault(name, 0) + 1);
                    }
                }
                super.visitCtTypeReference(reference);
            }
        };
        scanner.scan(type);

        if (isEmptySubclass(type)) {
            ReportStruct report = new ReportStruct(getSmellName(), filePath, inputDirPath, className, "Empty subclass adds no behavior (Inline Class)");
            report.addLineNumber(type.getPosition().getLine());
            customReports.add(report);
        }

        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() == null) continue;
            boolean isUnimplemented = false;
            String unimplementedReason = "";

            List<CtStatement> statements = method.getBody().getStatements();
            List<CtStatement> explicitStatements = new ArrayList<>();
            for (CtStatement stmt : statements) {
                if (!stmt.isImplicit()) explicitStatements.add(stmt);
            }

            if (explicitStatements.size() == 1 && explicitStatements.get(0) instanceof CtThrow) {
                CtThrow throwStmt = (CtThrow) explicitStatements.get(0);
                if (throwStmt.getThrownExpression() != null && throwStmt.getThrownExpression().getType() != null) {
                    String exceptionName = throwStmt.getThrownExpression().getType().getSimpleName();
                    if (exceptionName.equals("UnsupportedOperationException") || exceptionName.equals("NotImplementedException")) {
                        if (!isOverride(method)) {
                            isUnimplemented = true;
                            unimplementedReason = "Method explicitly throws " + exceptionName;
                        }
                    }
                }
            }

            if (isUnimplemented) {
                ReportStruct report = new ReportStruct(getSmellName(), filePath, inputDirPath, className, unimplementedReason);
                if (method.getPosition().isValidPosition()) {
                    report.addLineNumber(method.getPosition().getLine());
                }
                customReports.add(report);
            }

            if (!isOverride(method) && !isMainMethod(method) && !isFrameworkMethod(method)) {
                for (CtParameter<?> parameter : method.getParameters()) {
                    boolean[] referenced = {false};
                    CtScanner paramScanner = new CtScanner() {
                        @Override
                        public <T> void visitCtVariableRead(CtVariableRead<T> variableRead) {
                            if (variableRead.getVariable() != null && variableRead.getVariable().getSimpleName().equals(parameter.getSimpleName())) {
                                referenced[0] = true;
                            }
                            super.visitCtVariableRead(variableRead);
                        }

                        @Override
                        public <T> void visitCtVariableWrite(CtVariableWrite<T> variableWrite) {
                            if (variableWrite.getVariable() != null && variableWrite.getVariable().getSimpleName().equals(parameter.getSimpleName())) {
                                referenced[0] = true;
                            }
                            super.visitCtVariableWrite(variableWrite);
                        }
                    };
                    paramScanner.scan(method.getBody());

                    if (!referenced[0]) {
                        ReportStruct report = new ReportStruct(getSmellName(), filePath, inputDirPath, className, "Unused parameter: " + parameter.getSimpleName());
                        if (parameter.getPosition().isValidPosition()) {
                            report.addLineNumber(parameter.getPosition().getLine());
                        } else if (method.getPosition().isValidPosition()) {
                            report.addLineNumber(method.getPosition().getLine());
                        }
                        customReports.add(report);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private boolean isOverride(CtMethod<?> method) {
        if (method.getAnnotations().stream().anyMatch(a -> a.getAnnotationType().getSimpleName().equals("Override"))) {
            return true;
        }
        try {
            return !method.getTopDefinitions().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isMainMethod(CtMethod<?> method) {
        return method.getSimpleName().equals("main") && method.isStatic() && method.getParameters().size() == 1;
    }

    private boolean isFrameworkMethod(CtMethod<?> method) {
        for (CtAnnotation<?> annotation : method.getAnnotations()) {
            if (annotation.getAnnotationType() != null) {
                String name = annotation.getAnnotationType().getSimpleName();
                if (name.equals("Test") || name.equals("Before") || name.equals("After") ||
                    name.equals("BeforeEach") || name.equals("AfterEach") ||
                    name.equals("Bean") || name.equals("PostConstruct") ||
                    name.equals("EventListener") || name.equals("Subscribe") ||
                    name.endsWith("Mapping")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmptySubclass(CtType<?> type) {
        if (!(type instanceof CtClass<?>) || type.isAbstract()) return false;
        if (type.getSuperclass() == null || type.getSuperclass().getQualifiedName().equals("java.lang.Object")) return false;
        if (!type.getFields().isEmpty() || !type.getMethods().isEmpty()) return false;
        for (CtConstructor<?> constructor : ((CtClass<?>) type).getConstructors()) {
            if (constructor.getBody() != null) {
                for (CtStatement stmt : constructor.getBody().getStatements()) {
                    if (stmt.isImplicit()) continue;
                    if (stmt instanceof CtInvocation<?> inv && inv.getExecutable().getSimpleName().equals("<init>")) {
                        continue;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private int getMeaningfulSubclassCount(String typeName) {
        int count = 0;
        List<CtType<?>> children = childrenMap.getOrDefault(typeName, Collections.emptyList());
        for (CtType<?> child : children) {
            if (!child.isAbstract() && !child.isInterface() && !isEmptySubclass(child)) {
                count++;
            }
            count += getMeaningfulSubclassCount(child.getQualifiedName());
        }
        return count;
    }
}
