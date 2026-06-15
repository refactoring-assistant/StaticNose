package edu.northeastern.utils;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

/**
 * A shared utility that normalizes an AST statement by stripping away
 * unique identifiers (like variable names and literals) and replacing
 * them with generic tokens. This allows detectors to compare the
 * purely structural logic of two code snippets.
 */
public class AstNormalizer {

    /**
     * Returns the string that is normalized AST code
     * @param stmt code that needs to be normalized
     * @return the normalized String
     */
    public static String generateSkeleton(CtStatement stmt) {
        return generateSkeleton(stmt, false);
    }

    /**
     * Returns the string that is normalized AST code for any element, optionally including arguments
     * @param element code that needs to be normalized
     * @param includeArguments whether to include method arguments
     * @return the normalized String
     */
    public static String generateSkeleton(CtElement element, boolean includeArguments) {
        String prefix = element.getClass().getSimpleName().replace("Impl", "") + ":";

        SkeletonVisitor visitor = new SkeletonVisitor(includeArguments);
        visitor.scan(element);
        return prefix + visitor.getSkeleton();
    }

    /**
     * Class that normalizes the given AST by replacing unique aspects of the code
     * to generics so that it is easier to compare two ASTs based on their structure
     * and function over unique attributes like variable names.
     */
    private static class SkeletonVisitor extends CtScanner {
        private final StringBuilder sb = new StringBuilder();
        private final boolean includeArguments;

        public SkeletonVisitor() {
            this.includeArguments = false;
        }

        public SkeletonVisitor(boolean includeArguments) {
            this.includeArguments = includeArguments;
        }

        public String getSkeleton() { return sb.toString(); }

        @Override public <T> void visitCtVariableRead(CtVariableRead<T> v) { sb.append("$VAR"); }
        @Override public <T> void visitCtVariableWrite(CtVariableWrite<T> v) { sb.append("$VAR"); }
        @Override public <T> void visitCtLiteral(CtLiteral<T> l) { sb.append("$LIT"); }
        @Override public <T> void visitCtLocalVariable(CtLocalVariable<T> v) { scan(v.getDefaultExpression()); }

        @Override public <T> void visitCtBinaryOperator(CtBinaryOperator<T> op) {
            sb.append("(");
            scan(op.getLeftHandOperand());
            sb.append(op.getKind());
            scan(op.getRightHandOperand());
            sb.append(")");
        }

        @Override public <T> void visitCtInvocation(CtInvocation<T> inv) {
            if (inv.getExecutable() != null) {
                sb.append("CALL(").append(inv.getExecutable().getSimpleName()).append(")");
            }
            if (includeArguments) {
                sb.append("[");
                for (CtExpression<?> arg : inv.getArguments()) {
                    scan(arg);
                }
                sb.append("]");
            }
        }
    }
}