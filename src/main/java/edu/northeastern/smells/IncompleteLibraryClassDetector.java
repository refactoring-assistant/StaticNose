package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.*;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

@Deprecated
public class IncompleteLibraryClassDetector extends AbstractDetector {

    private final Map<String, List<Location>> expressionMap = new HashMap<>();
    private final Set<String> internalTypes = new HashSet<>();

    public IncompleteLibraryClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "IncompleteLibraryClass";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> reports = new ArrayList<>();
        Map<String, ReportStruct> fileReportMap = new HashMap<>();

        List<String> skeletons = new ArrayList<>(expressionMap.keySet());
        skeletons.sort((a, b) -> Integer.compare(b.length(), a.length()));

        Set<CtElement> coveredElements = new HashSet<>();
        int groupCounter = 1;

        for (String skeleton : skeletons) {
            List<Location> locations = expressionMap.get(skeleton);

            if (locations.size() > 1) {
                List<Location> validLocations = new ArrayList<>();
                for (Location loc : locations) {
                    if (!coveredElements.contains(loc.element)) {
                        validLocations.add(loc);
                    }
                }

                if (validLocations.size() > 1) {
                    String groupId = String.format("Group %d (%d instances)", groupCounter++, validLocations.size());
                    
                    for (Location loc : validLocations) {
                        String reportKey = loc.filePath + "::" + groupId;
                        ReportStruct report = fileReportMap.computeIfAbsent(reportKey, k -> new ReportStruct(
                                getSmellName(),
                                loc.filePath,
                                this.inputDirPath,
                                loc.className,
                                groupId
                        ));
                        report.addLineNumber(loc.lineNumber);

                        markCovered(loc.element, coveredElements);
                    }
                }
            }
        }

        return new ArrayList<>(fileReportMap.values());
    }

    private void markCovered(CtElement element, Set<CtElement> covered) {
        CtScanner scanner = new CtScanner() {
            @Override
            public void scan(CtElement element) {
                if (element != null) {
                    covered.add(element);
                    super.scan(element);
                }
            }
        };
        scanner.scan(element);
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        if (!type.getPosition().isValidPosition()) return new ArrayList<>();

        if (internalTypes.isEmpty()) {
            for (CtType<?> t : type.getFactory().getModel().getAllTypes()) {
                internalTypes.add(t.getQualifiedName());
            }
        }

        String filePath = type.getPosition().getFile().getPath();
        String className = type.getSimpleName();

        List<CtExpression> expressions = type.getElements(new TypeFilter<>(CtExpression.class));

        for (CtExpression<?> expr : expressions) {
            if (expr.isImplicit() || !expr.getPosition().isValidPosition()) continue;

            if (expr instanceof CtLiteral || expr instanceof CtVariableRead || 
                expr instanceof CtVariableWrite || expr instanceof CtTypeAccess || expr instanceof CtThisAccess ||
                expr instanceof CtAssignment || expr instanceof CtOperatorAssignment || expr instanceof CtInvocation) {
                continue;
            }

            if (containsExternalReference(expr)) {
                String skeleton = expr.toString();
                expressionMap.computeIfAbsent(skeleton, k -> new ArrayList<>())
                        .add(new Location(filePath, className, expr.getPosition().getLine(), expr));
            }
        }

        return new ArrayList<>();
    }

    private boolean containsExternalReference(CtElement root) {
        final boolean[] found = {false};
        CtScanner scanner = new CtScanner() {
            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                if (invocation.getExecutable() != null && invocation.getExecutable().getDeclaringType() != null) {
                    String typeName = invocation.getExecutable().getDeclaringType().getQualifiedName();
                    if (!internalTypes.contains(typeName) && !isIgnoredType(typeName)) {
                        found[0] = true;
                    }
                }
                super.visitCtInvocation(invocation);
            }

            @Override
            public <T> void visitCtLambda(CtLambda<T> lambda) {
                if (lambda.getType() != null) {
                    String typeName = lambda.getType().getQualifiedName();
                    if (!internalTypes.contains(typeName) && !isIgnoredType(typeName)) {
                        found[0] = true;
                    }
                }
                super.visitCtLambda(lambda);
            }
        };
        scanner.scan(root);
        return found[0];
    }

    private boolean isIgnoredType(String typeName) {
        if (typeName == null) return true;
        if (typeName.startsWith("java.lang.String")) return true;
        if (typeName.startsWith("java.lang.System")) return true;
        if (typeName.equals("java.io.PrintStream") || typeName.equals("java.io.PrintWriter")) return true;
        if (typeName.endsWith("Logger") || typeName.endsWith("Log")) return true;
        if (typeName.startsWith("java.util.List") || typeName.startsWith("java.util.Map") || typeName.startsWith("java.util.Set") || typeName.startsWith("java.util.Collection")) return true;
        if (typeName.startsWith("java.util.ArrayList") || typeName.startsWith("java.util.HashMap") || typeName.startsWith("java.util.HashSet")) return true;
        if (typeName.endsWith("Exception") || typeName.endsWith("Error")) return true;
        return false;
    }

    private record Location(String filePath, String className, int lineNumber, CtElement element) {}
}