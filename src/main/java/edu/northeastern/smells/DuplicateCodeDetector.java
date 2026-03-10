package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class DuplicateCodeDetector extends AbstractDetector {

    private static final int WINDOW_SIZE = 5;

    private final Map<String, List<Location>> globalSequenceMap = new HashMap<>();

    public DuplicateCodeDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Duplicate Code";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> finalReports = new ArrayList<>();

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

        finalReports.addAll(fileReportMap.values());
        return finalReports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {

        if (!type.getPosition().isValidPosition()) return new ArrayList<>();

        String filePath = type.getPosition().getFile().getPath();
        String className = type.getSimpleName();

        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() == null) continue;

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

    private static class Location {
        String filePath;
        String className;
        int lineNumber;

        public Location(String filePath, String className, int lineNumber) {
            this.filePath = filePath;
            this.className = className;
            this.lineNumber = lineNumber;
        }
    }

    private String generateSkeleton(CtStatement stmt) {
        SkeletonVisitor visitor = new SkeletonVisitor();
        visitor.scan(stmt);
        return visitor.getSkeleton();
    }

    private static class SkeletonVisitor extends CtScanner {
        private final StringBuilder sb = new StringBuilder();
        public String getSkeleton() { return sb.toString(); }

        @Override public <T> void visitCtVariableRead(spoon.reflect.code.CtVariableRead<T> v) { sb.append("$VAR"); }
        @Override public <T> void visitCtVariableWrite(spoon.reflect.code.CtVariableWrite<T> v) { sb.append("$VAR"); }
        @Override public <T> void visitCtLiteral(spoon.reflect.code.CtLiteral<T> l) { sb.append("$LIT"); }
        @Override public <T> void visitCtLocalVariable(spoon.reflect.code.CtLocalVariable<T> v) { scan(v.getDefaultExpression()); }
        @Override public <T> void visitCtBinaryOperator(spoon.reflect.code.CtBinaryOperator<T> op) {
            scan(op.getLeftHandOperand()); sb.append(op.getKind()); scan(op.getRightHandOperand());
        }
        @Override public <T> void visitCtInvocation(spoon.reflect.code.CtInvocation<T> inv) {
            sb.append("CALL(").append(inv.getExecutable().getSimpleName()).append(")");
        }
    }
}