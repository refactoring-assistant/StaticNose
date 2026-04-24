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
        return "Lazy Class";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();
        List<ReportStruct> reports = new ArrayList<>();

        for (CtClass<?> ctClass : allConcreteClasses) {
            int weight = calculateLogicWeight(ctClass);

            if (weight > LOW_WEIGHT_THRESHOLD) continue;

            // THE CONTRACT SHIELD: If it implements an interface or abstract method,
            // it is structurally necessary. It is NOT a Lazy Class.
            if (isImplementingContract(ctClass)) continue;

            String className = ctClass.getQualifiedName();
            Set<String> callers = dependencyGraph.getOrDefault(className, new HashSet<>());
            int callerCount = callers.size();

            // RESTORED: Catch 0-caller (RGBBad) and 1-caller classes
            if (callerCount <= 1) {
                String callerInfo = (callerCount == 0)
                        ? "is unused (Dead Code)"
                        : "is only used by '" + callers.iterator().next() + "'";

                String info = String.format("Lazy Class: Logic weight is %d and %s. Consider 'Inline Class' or deletion.", weight, callerInfo);
                reports.add(createReport(ctClass, info));
            }
            // Collapse Hierarchy fallback
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
        // FIX 2: Ask Spoon to find ALL CtClasses associated with this type's context
        // This ensures peer classes like PrintHelloUserBad are actually found.
        List<CtClass<?>> classesInFile = type.getElements(new TypeFilter<>(CtClass.class));

        for (CtClass<?> ctClass : classesInFile) {
            if (!ctClass.getPosition().isValidPosition() || ctClass.isAbstract()) continue;

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

        // True boilerplate getters usually only have 1 statement (return)
        if (name.startsWith("get") && e.getParameters().isEmpty()) {
            return e.getBody().getStatements().size() <= 1;
        }

        // True boilerplate setters usually only have 1 statement (assignment)
        if (name.startsWith("set") && e.getParameters().size() == 1) {
            return e.getBody().getStatements().size() == 1; // Allows setNewProductionFacility to survive!
        }

        return name.equals("toString") || name.equals("hashCode") || name.equals("equals");
    }

    /**
     * Protects classes ONLY if they are structurally required to exist
     * (e.g., they implement an interface or an abstract class).
     * Normal concrete subclasses are NOT protected and will be flagged if lazy.
     */
    private boolean isImplementingContract(CtClass<?> ctClass) {
        // 1. Directly implements an interface
        if (!ctClass.getSuperInterfaces().isEmpty()) {
            return true;
        }

        // 2. Extends an abstract class (meaning this class MUST exist to be instantiated)
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