package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class SwitchStatementDetector extends AbstractDetector {

    private final int MAX_SWITCH_CASES;
    private final int MAX_IF_CHAIN_LENGTH;

    public SwitchStatementDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        MAX_SWITCH_CASES = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_SWITCH_CASES", 2);
        MAX_IF_CHAIN_LENGTH = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_IF_CHAIN_LENGTH", 2);
    }

    @Override
    protected String getSmellName() {
        return "Switch Statements";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        SwitchScanner scanner = new SwitchScanner(detectedLines);
        scanner.scan(type);

        return detectedLines;
    }

    private class SwitchScanner extends CtScanner {
        private final List<Integer> detectedLines;
        private final Set<CtIf> visitedIfs = new HashSet<>();

        public SwitchScanner(List<Integer> detectedLines) {
            this.detectedLines = detectedLines;
        }

        @Override
        public <S> void visitCtSwitch(CtSwitch<S> switchStatement) {
            List<CtCase<? super S>> cases = switchStatement.getCases();

            if (cases.size() > MAX_SWITCH_CASES) {
                if (switchStatement.getPosition().isValidPosition()) {
                    detectedLines.add(switchStatement.getPosition().getLine());
                }
            }
            super.visitCtSwitch(switchStatement);
        }

        @Override
        public void visitCtIf(CtIf ifElement) {
            if (visitedIfs.contains(ifElement)) {
                super.visitCtIf(ifElement);
                return;
            }

            List<CtIf> chain = collectIfChain(ifElement);

            if (chain.size() > MAX_IF_CHAIN_LENGTH) {
                String typeCodeVar = findCommonVariable(chain);

                if (typeCodeVar != null) {
                    if (ifElement.getPosition().isValidPosition()) {
                        detectedLines.add(ifElement.getPosition().getLine());
                    }
                    visitedIfs.addAll(chain);
                }
            }

            super.visitCtIf(ifElement);
        }
    }

    private List<CtIf> collectIfChain(CtIf firstIf) {
        List<CtIf> chain = new ArrayList<>();
        chain.add(firstIf);

        CtStatement current = firstIf.getElseStatement();
        while (current != null) {
            if (current instanceof CtIf nextIf) {
                chain.add(nextIf);
                current = nextIf.getElseStatement();
            } else if (current instanceof CtBlock<?> block) {
                if (block.getStatements().size() == 1 && block.getStatement(0) instanceof CtIf) {
                    CtIf nextIf = block.getStatement(0);
                    chain.add(nextIf);
                    current = nextIf.getElseStatement();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return chain;
    }

    private String findCommonVariable(List<CtIf> chain) {
        Map<String, Integer> variableCounts = new HashMap<>();

        for (CtIf ifStmt : chain) {
            CtExpression<?> condition = ifStmt.getCondition();

            String variable = extractVariableFromCondition(condition);

            if (variable != null) {
                variableCounts.put(variable, variableCounts.getOrDefault(variable, 0) + 1);
            }
        }

        int threshold = chain.size() / 2;

        for (Map.Entry<String, Integer> entry : variableCounts.entrySet()) {
            if (entry.getValue() > threshold) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String extractVariableFromCondition(CtExpression<?> condition) {
        // Find ANY variable read happening inside this condition
        List<CtVariableRead<?>> variableReads = condition.getElements(new TypeFilter<>(CtVariableRead.class));

        if (!variableReads.isEmpty()) {
            // Usually the first variable read in the condition is the one being tested.
            // E.g., in `empType.equals("Professor")`, empType is the first read.
            // E.g., in `"Professor".equals(empType)`, empType is the first read.
            return variableReads.get(0).getVariable().getSimpleName();
        }

        return null;
    }
}