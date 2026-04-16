package edu.northeastern.smells;

import spoon.Launcher;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.reflect.declaration.CtExecutable;

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

    private static final int WORD_COUNT_TRESHOLD_BELOW_MIN = 3;
    private static final int WORD_COUNT_ABOVE_MAX = 5;
    private static final double COMMENT_TO_LLOC_RATIO = 0.3;


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

        // ==========================================
        // 1. CLASS & FIELD LEVEL COMMENTS
        // ==========================================
        // Grab all comments directly attached to the class or its fields
        List<CtComment> classLevelComments = type.getElements(new TypeFilter<>(CtComment.class));
        for (CtComment comment : classLevelComments) {

            // IGNORE JAVADOCS: Standard documentation is not a smell
            if (comment.getCommentType() == CtComment.CommentType.JAVADOC) {
                continue;
            }

            // Make sure we only process comments that are OUTSIDE of methods/constructors
            // (We will handle internal comments in step 2)
            if (comment.getParent(CtExecutable.class) == null) {
                analyzeIndividualComment(comment, detectedLines);
            }
        }

        // ==========================================
        // 2. METHOD & CONSTRUCTOR LEVEL COMMENTS
        // ==========================================
        // CtExecutable catches BOTH CtMethod and CtConstructor
        List<CtExecutable<?>> executables = type.getElements(new TypeFilter<>(CtExecutable.class));

        for(CtExecutable<?> executable : executables) {

            if(executable.getBody() == null) continue;

            List<CtComment> comments = executable.getBody().getElements(new TypeFilter<>(CtComment.class));

            List<CtComment> contributingComments = new ArrayList<>();
            int commentLineCount = 0;

            for(CtComment comment : comments) {

                // IGNORE JAVADOCS INSIDE METHODS TOO
                if(comment.getCommentType() == CtComment.CommentType.JAVADOC) {
                    continue;
                }

                // Call our extracted analysis logic
                boolean isSmelly = analyzeIndividualComment(comment, detectedLines);

                if (!isSmelly) {
                    contributingComments.add(comment);
                    int lines = comment.getContent().split("\r\n|\r|\n").length;
                    commentLineCount += lines;
                }
            }

            int LLOC = calculateLLOC(executable); // Make sure your LLOC method accepts CtExecutable!

            // Density Check: If the ratio of comments to code is too high
            if(LLOC > 0) {
                double ratio = (double) commentLineCount / LLOC;

                if(ratio > COMMENT_TO_LLOC_RATIO) {
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
     * Analyzes a single comment for obvious smells (Commented out code, extreme lengths).
     * @return true if the comment was flagged as a smell, false if it is just a standard comment.
     */
    private boolean analyzeIndividualComment(CtComment comment, Set<Integer> detectedLines) {
        String content = comment.getContent().trim();
        String upperContent = content.toUpperCase();

        if (upperContent.startsWith("TODO") || upperContent.startsWith("FIXME")) {
            return false; // Not a smell, just technical debt marker
        }

        if (isCommentedOutCode(content)) {
            addLineIfValid(comment, detectedLines);
            return true;
        }

        int wordCount = content.split("\\s+").length;

        // "Ghost" comments that are too short (e.g. "// a")
        if(wordCount < WORD_COUNT_TRESHOLD_BELOW_MIN) {
            addLineIfValid(comment, detectedLines);
            return true;
        }

        // "Novel" comments that are too long (e.g. your RGBColor example)
        if(wordCount > WORD_COUNT_ABOVE_MAX) {
            addLineIfValid(comment, detectedLines);
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
