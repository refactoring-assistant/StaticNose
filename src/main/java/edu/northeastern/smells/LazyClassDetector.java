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

    private static final int LOW_WEIGHT_THRESHOLD = 5;

    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();
    private final List<CtClass<?>> allConcreteClasses = new ArrayList<>();

    public LazyClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Lazy Class (Low Value)";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> reports = new ArrayList<>();

        for (CtClass<?> ctClass : allConcreteClasses) {

            int weight = calculateLogicWeight(ctClass);

            if (weight > LOW_WEIGHT_THRESHOLD) continue;

            String className = ctClass.getQualifiedName();
            Set<String> callers = dependencyGraph.getOrDefault(className, new HashSet<>());

            if (callers.size() == 1) {
                String caller = callers.iterator().next();
                String info = String.format("Lazy Class: Has very little logic (Weight: %d) and is only used by '%s'. Consider 'Inline Class'.", weight, caller);
                reports.add(createReport(ctClass, info));
            }

            else if (hasMeaningfulSuperclass(ctClass)) {
                String parent = ctClass.getSuperclass().getSimpleName();
                String info = String.format("Lazy Class: Subclass adds almost no new logic (Weight: %d). Consider 'Collapse Hierarchy' into '%s'.", weight, parent);
                reports.add(createReport(ctClass, info));
            }
        }

        return reports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        if (type instanceof CtClass<?> ctClass && type.getPosition().isValidPosition()) {
            allConcreteClasses.add(ctClass);

            String currentClassName = ctClass.getQualifiedName();

            List<CtTypeReference<?>> references = ctClass.getElements(new TypeFilter<>(CtTypeReference.class));

            for (CtTypeReference<?> ref : references) {
                if (!ref.isPrimitive() && !ref.getQualifiedName().startsWith("java.")) {
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

        return statementCount;
    }

    private boolean isBoilerplate(CtMethod<?> m) {
        String name = m.getSimpleName();
        return (name.startsWith("get") && m.getParameters().isEmpty()) ||
                (name.startsWith("set") && m.getParameters().size() == 1) ||
                name.equals("toString") || name.equals("hashCode") || name.equals("equals");
    }

    private boolean hasMeaningfulSuperclass(CtClass<?> ctClass) {
        CtTypeReference<?> superClass = ctClass.getSuperclass();
        return superClass != null && !superClass.getSimpleName().equals("Object");
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