package edu.northeastern.smells;

import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.northeastern.utils.Metrics.calculateLLOC;

/**
 * This class detects the presence of the Comments code smell.
 * Comments that are smelly are those which simply restate the existing
 * readable line of code, commented out code, or which simply do not add
 * sufficient information to the already existing code and bloat the code.
 */
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
        Set<Integer> detectedLines = new HashSet<>();

        for(CtMethod<?> method : type.getMethods()) {

            if(method.getBody() == null) continue;

            List<CtComment> comments = method.getBody().getElements(new TypeFilter<>(CtComment.class));

            List<CtComment> contributingComments = new ArrayList<>();
            int commentLineCount = 0;

            for(CtComment comment : comments) {
                if(comment.getCommentType() == CtComment.CommentType.JAVADOC) {
                    addLineIfValid(comment, detectedLines);
                }

                String content = comment.getContent().trim();
                String upperContent = content.toUpperCase();

                if (upperContent.startsWith("TODO") || upperContent.startsWith("FIXME")) {
                    continue;
                }

                if (isCommentedOutCode(content)) {
                    addLineIfValid(comment, detectedLines);
                    continue;
                }

                contributingComments.add(comment);

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

            int LLOC = calculateLLOC(method);

            // TODO: explain this in comments
            if(LLOC > 0) {
                double ratio = (double) commentLineCount / LLOC;

                if(ratio > .3) {
                    for(CtComment c : contributingComments) {
                        addLineIfValid(c, detectedLines);
                    }
                }
            }
        }

        return new ArrayList<>(detectedLines);
    }

    /**
     * Best estimate of whether a comment is a piece of code.
     * Comments can contain broken/unfinished code which cannot be compiled
     * and Spoon will throw an error. Instead, get the best estimate of if
     * the comment is a piece of code.
     * @param commentContent the piece of comment to check for code
     * @return boolean
     */
    private boolean isCommentedOutCode(String commentContent) {

        String cleanContent = commentContent.trim();
        if (cleanContent.isEmpty()) return false;

        // statement ending
        if (cleanContent.endsWith(";")) {
            return true;
        }

        // structural java chars.
        if (cleanContent.contains(";") && (cleanContent.contains("=") || cleanContent.contains("."))) {
            return true;
        }

        // method signature or control flow
        if (cleanContent.matches(".*\\b(if|for|while|switch|catch)\\s*\\(.*")) {
            return true;
        }

        // braces in code context
        if (cleanContent.contains("{") && cleanContent.contains("}")) {
            return true;
        }

        return false;
    }

    /**
     * Check if line is valid and only then add it.
     *
     * @param element The line to check
     * @param lines Set of lines
     */
    private void addLineIfValid(spoon.reflect.declaration.CtElement element, Set<Integer> lines) {
        if (element.getPosition() != null && element.getPosition().isValidPosition()) {
            lines.add(element.getPosition().getLine());
        }
    }
}
