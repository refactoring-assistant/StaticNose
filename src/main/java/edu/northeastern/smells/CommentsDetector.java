package edu.northeastern.smells;

import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class CommentsDetector extends AbstractDetector{

    public CommentsDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected void configureLauncher(Launcher launcher) {
        launcher.getEnvironment().setCommentEnabled(true);
    }

    @Override
    protected String getSmellName() {
        return "Comments";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
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

        return detectedLines;
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
