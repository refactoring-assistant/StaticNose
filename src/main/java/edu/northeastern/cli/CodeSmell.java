package edu.northeastern.cli;

/**
 * A list of Code Smells with their labels to help parse
 * arguments.
 */
public enum CodeSmell {
    DATA_CLUMPS("data-clumps"),
    LARGE_CLASS("large-class"),
    LONG_METHOD("long-method"),
    LONG_PARAMETER_LIST("long-params"),
    PRIMITIVE_OBSESSION("prim-obsession"),
    DIVERGENT_CHANGE("divergent-change"),
    PARALLEL_INHERITANCE("parallel-hierarchy"),
    SHOTGUN_SURGERY("shotgun"),
    FEATURE_ENVY("feature-envy"),
    INAPPROPRIATE_INTIMACY("intimacy"),
    INCOMPLETE_LIBRARY_CLASS("incomplete-lib"),
    MESSAGE_CHAINS("message-chains"),
    MIDDLE_MAN("middle-man"),
    COMMENTS("comments"),
    DATA_CLASS("data-class"),
    DEAD_CODE("dead-code"),
    DUPLICATE_CODE("dup-code"),
    LAZY_CLASS("lazy-class"),
    SPECULATIVE_GENERALITY("spec-gen"),
    ALT_CLASSES_DIFF_INT("alt-classes"),
    REFUSED_BEQUEST("refused-bequest"),
    SWITCH_STATEMENTS("switch-stmts"),
    TEMPORARY_FIELD("temp-field");

    private final String label;

    CodeSmell(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Returns the actual enum given the shortcode of the Code Smell.
     * @param text The shortcode of the Code Smell.
     * @return The value of it's corresponding enum.
     */
    public static CodeSmell fromLabel(String text) {
        for (CodeSmell smell : CodeSmell.values()) {
            if(smell.label.equalsIgnoreCase(text)) {
                return smell;
            }
        }
        return null;
    }
}
