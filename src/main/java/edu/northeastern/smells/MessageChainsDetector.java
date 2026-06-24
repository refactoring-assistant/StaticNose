package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class MessageChainsDetector extends AbstractDetector {

    private final int CHAIN_THRESHOLD;

    public MessageChainsDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        CHAIN_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "CHAIN_THRESHOLD", 3);
    }

    @Override
    protected String getSmellName() {
        return "Message Chains";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        List<CtInvocation<?>> invocations = type.getElements(new TypeFilter<>(CtInvocation.class));

        for (CtInvocation<?> inv : invocations) {

            if (isInnerInvocation(inv)) {
                continue;
            }

            int chainDepth = getChainDepth(inv);

            if (chainDepth >= CHAIN_THRESHOLD) {
                if (!isFluentApi(inv)) {
                    if (inv.getPosition().isValidPosition()) {
                        detectedLines.add(inv.getPosition().getLine());
                    }
                }
            }
        }

        return detectedLines;
    }

    /**
     * Determines if this invocation is the target of another invocation.
     * If true, it means we are in the middle of a chain.
     */
    private boolean isInnerInvocation(CtInvocation<?> inv) {
        CtElement parent = inv.getParent();
        if (parent instanceof CtInvocation<?> parentInv) {
            return parentInv.getTarget() == inv;
        }
        return false;
    }

    /**
     * Recursively traces the targets backward to count the number of chained calls.
     */
    private int getChainDepth(CtInvocation<?> outermostInv) {
        int depth = 1;
        CtExpression<?> currentTarget = outermostInv.getTarget();

        while (currentTarget instanceof CtInvocation) {
            depth++;
            currentTarget = ((CtInvocation<?>) currentTarget).getTarget();
        }

        return depth;
    }

    /**
     * Heuristics to identify Builder Patterns, Java Streams, and StringBuilders.
     */
    private boolean isFluentApi(CtInvocation<?> outermostInv) {
        CtExpression<?> currentTarget = outermostInv.getTarget();
        CtInvocation<?> currentInv = outermostInv;

        while (currentTarget instanceof CtInvocation<?> targetInv) {

            CtTypeReference<?> currentType = currentInv.getType();
            CtTypeReference<?> targetType = targetInv.getType();

            if (currentType != null && currentType.equals(targetType)) {
                return true;
            }

            if (targetType != null) {
                String typeName = targetType.getQualifiedName();
                if (typeName.startsWith("java.util.stream") ||
                        typeName.equals("java.lang.StringBuilder") ||
                        typeName.equals("java.lang.StringBuffer") ||
                        typeName.startsWith("java.util.Optional")) {
                    return true;
                }
            }

            currentInv = targetInv;
            currentTarget = targetInv.getTarget();
        }

        return false;
    }
}