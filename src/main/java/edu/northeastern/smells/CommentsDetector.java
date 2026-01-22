package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class CommentsDetector implements IDetector{

    List<String> javaFilePaths;

    public CommentsDetector(List<String> javaFilePaths) {
        this.javaFilePaths = javaFilePaths;
    }

    @Override
    public List<ReportStruct> run() {
        List<ReportStruct> reportStructList = new ArrayList<>();

        for(String javaFilePath: javaFilePaths) {
            List<ReportStruct> fileReportStructList = analyzeJavaFile(javaFilePath);
            reportStructList.addAll(fileReportStructList);
        }

        return reportStructList;
    }

    private List<ReportStruct> analyzeJavaFile(String javaFilePath) {
        Launcher launcher = new Launcher();
        launcher.addInputResource(javaFilePath);
        launcher.getEnvironment().setComplianceLevel(17);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.buildModel();

        CtModel model = launcher.getModel();
        List<ReportStruct> fileReportStructList = new ArrayList<>();

        for (CtType<?> type : model.getAllTypes()) {

            ReportStruct classReportStruct = analyzeClass(type, javaFilePath);

            if(classReportStruct != null) {
                fileReportStructList.add(classReportStruct);
            }
        }

        return fileReportStructList;
    }

    private ReportStruct analyzeClass(CtType<?> type, String javaFilePath) {
        List<Integer> detectedLines = new ArrayList<>();

        for(CtMethod<?> method : type.getMethods()) {

            if(method.getBody() == null) continue;

            List<CtComment> comments = method.getBody().getElements(new TypeFilter<>(CtComment.class));

            int commentLineCount = 0;

            for(CtComment comment : comments) {
                if(comment.getCommentType() == CtComment.CommentType.JAVADOC) {
                    addLineIfValid(comment, detectedLines);
                }

                String content = comment.getContent().trim().toUpperCase();
                if (content.startsWith("TODO") || content.startsWith("FIXME")) {
                    continue;
                }

                int lines = comment.getContent().split("\r\n|\r|\n").length;
                commentLineCount += lines;

                int wordCount = content.split("\\s+").length;

                if(wordCount < 3) {
                    addLineIfValid(comment, detectedLines);
                }

                if(wordCount > 20) {
                    addLineIfValid(comment, detectedLines);
                }
            }

            int lloc = calculateLLOC(method);

            if(lloc > 0) {
                double ratio = (double) commentLineCount / lloc;

                if(ratio > .3) {
                    addLineIfValid(method, detectedLines);
                }
            }
        }

        boolean hasCodeSmell = !detectedLines.isEmpty();

        ReportStruct report = new ReportStruct(javaFilePath, type.getSimpleName(), hasCodeSmell);

        if(hasCodeSmell) {
            report.addLineNumbers(detectedLines);
        } else {
            report.addLineNumber(-1);
        }

        return report;
    }

    private int calculateLLOC(CtMethod<?> method) {
        List<CtStatement> statements = method.getBody().getStatements();

        int lloc = 0;
        for(CtStatement stmt : statements) {
            if(!(stmt instanceof CtBlock)) {
                lloc++;
            }
        }

        return lloc;
    }

    private void addLineIfValid(spoon.reflect.declaration.CtElement element, List<Integer> lines) {
        if (element.getPosition() != null && element.getPosition().isValidPosition()) {
            lines.add(element.getPosition().getLine());
        }
    }
}
