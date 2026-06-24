package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

import static edu.northeastern.utils.Metrics.calculateLLOC;

public class LazyClassDetector extends AbstractDetector {

    private final int LOW_WEIGHT_THRESHOLD;

    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private final List<CtClass<?>> allConcreteClasses = new ArrayList<>();

    public LazyClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        LOW_WEIGHT_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "LOW_WEIGHT_THRESHOLD", 5);
    }

    @Override
    protected String getSmellName() {
        return "Lazy Class";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();
        List<ReportStruct> reports = new ArrayList<>();

        for (CtClass<?> ctClass : allConcreteClasses) {
            int weight = calculateLogicWeight(ctClass);

            if (weight > LOW_WEIGHT_THRESHOLD) continue;

            if (isImplementingContract(ctClass)) continue;

            String className = ctClass.getQualifiedName();
            Set<String> callers = dependencyGraph.getOrDefault(className, new HashSet<>());
            int callerCount = callers.size();

            if (callerCount <= 1) {
                String callerInfo = (callerCount == 0)
                        ? "is unused (Dead Code)"
                        : "is only used by '" + callers.iterator().next() + "'";

                String info = String.format("Lazy Class: Logic weight is %d and %s. Consider 'Inline Class' or deletion.", weight, callerInfo);
                reports.add(createReport(ctClass, info));
            }
            else if (hasMeaningfulSuperclass(ctClass)) {
                String parent = ctClass.getSuperclass().getSimpleName();
                String info = String.format("Lazy Class: Subclass adds minimal logic (Weight: %d). Consider 'Collapse Hierarchy' into '%s'.", weight, parent);
                reports.add(createReport(ctClass, info));
            }
        }

        return reports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<CtClass<?>> classesInFile = type.getElements(new TypeFilter<>(CtClass.class));

        for (CtClass<?> ctClass : classesInFile) {
            if (!ctClass.getPosition().isValidPosition() || ctClass.isAbstract() || ctClass.isAnonymous() || !ctClass.isTopLevel()) continue;

            if (isUtilityOrConstantsClass(ctClass)) continue;

            allConcreteClasses.add(ctClass);
            String currentClassName = ctClass.getQualifiedName();

            List<CtTypeReference<?>> references = ctClass.getElements(new TypeFilter<>(CtTypeReference.class));

            for (CtTypeReference<?> ref : references) {
                if (!ref.isPrimitive() && isProjectClass(ref.getQualifiedName())) {
                    String referencedClass = ref.getQualifiedName();

                    if (!referencedClass.equals(currentClassName)) {
                        dependencyGraph.putIfAbsent(referencedClass, new HashSet<>());
                        dependencyGraph.get(referencedClass).add(currentClassName);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * Calculates how many actual logical statements exist in the class.
     * Ignores empty constructors, basic getters/setters, and class signatures.
     */
    private int calculateLogicWeight(CtClass<?> ctClass) {
        int statementCount = 0;

        for (CtMethod<?> method : ctClass.getMethods()) {
            if (method.getBody() == null || isBoilerplate(method)) continue;

            statementCount += calculateLLOC(method);
        }

        for (spoon.reflect.declaration.CtConstructor<?> constructor : ctClass.getConstructors()) {
            if (constructor.getBody() == null || isBoilerplate(constructor)) continue;
            statementCount += calculateLLOC(constructor);
        }

        return statementCount;
    }

    private boolean isBoilerplate(spoon.reflect.declaration.CtExecutable<?> e) {
        String name = e.getSimpleName();

        if (e instanceof spoon.reflect.declaration.CtConstructor) {
            return e.getBody().getStatements().size() <= 1;
        }

        if (name.startsWith("get") && e.getParameters().isEmpty()) {
            return e.getBody().getStatements().size() <= 1;
        }

        if (name.startsWith("set") && e.getParameters().size() == 1) {
            return e.getBody().getStatements().size() == 1;
        }

        return name.equals("toString") || name.equals("hashCode") || name.equals("equals");
    }

    /**
     * Protects classes ONLY if they are structurally required to exist
     * (e.g., they implement an interface or an abstract class).
     * Normal concrete subclasses are NOT protected and will be flagged if lazy.
     */
    private boolean isImplementingContract(CtClass<?> ctClass) {
        if (!ctClass.getSuperInterfaces().isEmpty()) {
            return true;
        }

        CtTypeReference<?> superClass = ctClass.getSuperclass();
        if (superClass != null && superClass.getTypeDeclaration() != null) {
            return superClass.getTypeDeclaration().isAbstract();
        }

        return false;
    }

    private boolean hasMeaningfulSuperclass(CtClass<?> ctClass) {
        CtTypeReference<?> superClass = ctClass.getSuperclass();
        return superClass != null && !superClass.getSimpleName().equals("Object");
    }

    private boolean isUtilityOrConstantsClass(CtClass<?> ctClass) {
        if (ctClass.getMethods().isEmpty() && !ctClass.getFields().isEmpty()) {
            boolean onlyStaticFinal = true;
            for (spoon.reflect.declaration.CtField<?> field : ctClass.getFields()) {
                if (!field.isStatic() || !field.isFinal()) {
                    onlyStaticFinal = false;
                    break;
                }
            }
            if (onlyStaticFinal) return true;
        }

        if (!ctClass.getConstructors().isEmpty()) {
            boolean allPrivateConstructors = true;
            for (spoon.reflect.declaration.CtConstructor<?> ctor : ctClass.getConstructors()) {
                if (!ctor.isPrivate()) {
                    allPrivateConstructors = false;
                    break;
                }
            }
            if (allPrivateConstructors) return true;
        }

        return false;
    }

    private ReportStruct createReport(CtClass<?> ctClass, String info) {
        ReportStruct report = new ReportStruct(
                getSmellName(),
                ctClass.getPosition().getFile().getPath(),
                this.inputDirPath,
                ctClass.getSimpleName(),
                info
        );
        report.addLineNumber(ctClass.getPosition().getLine());
        return report;
    }
}