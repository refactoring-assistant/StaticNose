package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

import static edu.northeastern.utils.AstNormalizer.generateSkeleton;
import static edu.northeastern.utils.Metrics.isAccessor;

/**
 * This class detects Duplicate Code code Smell.
 * It does so by doing a Type 2 check by converting AST to generics (int x = 5 to $VAR = $LIT)
 * and then performing a sliding window of the given size and scanning through the code base.
 */
@Deprecated
public class DuplicateCodeDetector extends AbstractDetector {

    private final int WINDOW_SIZE;

    private final Map<String, List<Location>> globalSequenceMap = new HashMap<>();

    public DuplicateCodeDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        WINDOW_SIZE = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "WINDOW_SIZE", 5);
    }

    @Override
    protected String getSmellName() {
        return "Duplicate Code";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        Map<String, ReportStruct> fileReportMap = new HashMap<>();

        int groupCounter = 1;

        for (Map.Entry<String, List<Location>> entry : globalSequenceMap.entrySet()) {
            List<Location> locations = entry.getValue();

            if (locations.size() > 1) {
                String groupId = String.format("Group %d (%d instances)", groupCounter++, locations.size());
                for (Location loc : locations) {
                    String reportKey = loc.filePath+"::"+groupId;

                    ReportStruct report = fileReportMap.get(reportKey);

                    if(report == null) {
                        report = new ReportStruct(
                                getSmellName(),
                                loc.filePath,
                                this.inputDirPath,
                                loc.className,
                                groupId
                        );
                        fileReportMap.put(reportKey, report);
                    }

                    for(int k=0; k<WINDOW_SIZE; k++) {
                        report.addLineNumber(loc.lineNumber + k);
                    }
                }
            }
        }

        return new ArrayList<>(fileReportMap.values());
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {

        if (!type.getPosition().isValidPosition()) return new ArrayList<>();

        String filePath = type.getPosition().getFile().getPath();
        String className = type.getSimpleName();

        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() == null) continue;

            if (isBoilerplateOverride(method)) continue;

            List<CtStatement> statements = method.getElements(new TypeFilter<>(CtStatement.class));
            List<CtStatement> cleanStatements = new ArrayList<>();
            for (CtStatement stmt : statements) {
                if (!stmt.isImplicit()) cleanStatements.add(stmt);
            }

            if (cleanStatements.size() < WINDOW_SIZE) continue;

            for (int i = 0; i <= cleanStatements.size() - WINDOW_SIZE; i++) {
                StringBuilder windowSignature = new StringBuilder();
                int startLine = -1;

                for (int j = 0; j < WINDOW_SIZE; j++) {
                    CtStatement currentStmt = cleanStatements.get(i + j);
                    windowSignature.append(generateSkeleton(currentStmt)).append("|");

                    if (j == 0 && currentStmt.getPosition().isValidPosition()) {
                        startLine = currentStmt.getPosition().getLine();
                    }
                }

                if (startLine != -1) {
                    String signature = windowSignature.toString();

                    globalSequenceMap.putIfAbsent(signature, new ArrayList<>());
                    globalSequenceMap.get(signature).add(new Location(filePath, className, startLine));
                }
            }
        }

        return new ArrayList<>();
    }

    private record Location(String filePath, String className, int lineNumber) {
    }

    /**
     * Checks if the code is boilerplate code that is required
     * to stay even if it is duplicate
     * @param method The method to check
     * @return boolean
     */
    private boolean isBoilerplateOverride(CtMethod<?> method) {
        if (isObjectMethodOverride(method)) {
            return true;
        }

        if (isAccessor(method, false)) {
            return method.getBody() != null && method.getBody().getStatements().size() <= 2;
        }

        return false;
    }

    /**
     * Checks if the method override is from an Object method.
     * We need to ignore these in duplicate code checking
     * because they are supposed to be similar in nature
     * but are still required for every class
     * @param method The method to check
     * @return boolean
     */
    private boolean isObjectMethodOverride(CtMethod<?> method) {
        String name = method.getSimpleName();
        if (!name.equals("equals") && !name.equals("hashCode") &&
                !name.equals("toString") && !name.equals("clone")) {
            return false;
        }

        try {
            Collection<CtMethod<?>> topDefinitions = method.getTopDefinitions();

            for (CtMethod<?> topDef : topDefinitions) {
                if (topDef.getDeclaringType() != null &&
                        topDef.getDeclaringType().getQualifiedName().equals("java.lang.Object")) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return false;
    }
}