package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

public class ShotgunSurgeryDetector extends AbstractDetector {

    private static final int THRESHOLD_CC = 5;
    private static final int THRESHOLD_FAN_OUT = 3;
    private static final int THRESHOLD_FAN_IN = 5; // Use 2 for your small example

    private final Map<String, MethodMetrics> methodRegistry = new HashMap<>();

    private final Map<String, Set<String>> incomingCouplingMap = new HashMap<>();

    public ShotgunSurgeryDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Shotgun Surgery";
    }

    @Override
    public List<ReportStruct> run() {
        super.run();

        List<ReportStruct> reports = new ArrayList<>();

        for (String signature : methodRegistry.keySet()) {
            MethodMetrics metrics = methodRegistry.get(signature);

            Set<String> clients = incomingCouplingMap.getOrDefault(signature, Collections.emptySet());
            int fanIn = clients.size();

            boolean isRippleRisk = fanIn >= THRESHOLD_FAN_IN;
            boolean isCoordinatorRisk = (metrics.cc >= THRESHOLD_CC) && (metrics.fanOut >= THRESHOLD_FAN_OUT);

            if (isRippleRisk || isCoordinatorRisk) {

                String riskType;
                if (isRippleRisk && metrics.cc >= THRESHOLD_CC) {
                    riskType = "CRITICAL (God Method)";
                } else if (isRippleRisk) {
                    riskType = "High Ripple Risk (Many Dependents)";
                } else {
                    riskType = "Complex Coordinator (Logic + Dependencies)";
                }

                String info = String.format("%s [CC=%d, Fan-Out=%d, Fan-In=%d]",
                        riskType, metrics.cc, metrics.fanOut, fanIn);

                ReportStruct report = new ReportStruct(
                        getSmellName(),
                        metrics.filePath,
                        this.inputDirPath,
                        metrics.className,
                        info
                );
                report.addLineNumber(metrics.lineNum);
                reports.add(report);
            }
        }

        return reports;
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        if (!type.getPosition().isValidPosition()) return new ArrayList<>();

        String currentClassName = type.getQualifiedName();

        for (CtMethod<?> method : type.getMethods()) {
            if (method.getBody() == null) continue;

            int cc = calculateCC(method);
            int fanOut = calculateFanOut(method);

            String signature = method.getSignature();

            methodRegistry.put(signature, new MethodMetrics(
                    type.getPosition().getFile().getPath(),
                    type.getSimpleName(),
                    method.getPosition().getLine(),
                    cc,
                    fanOut
            ));

            List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));

            for (CtInvocation<?> inv : invocations) {
                CtExecutableReference<?> exec = inv.getExecutable();
                if (exec != null && exec.getDeclaringType() != null) {

                    String targetClass = exec.getDeclaringType().getQualifiedName();

                    if (!targetClass.startsWith("java.") && !targetClass.equals(currentClassName)) {
                        String targetSig = exec.getSignature();
                        incomingCouplingMap.putIfAbsent(targetSig, new HashSet<>());
                        incomingCouplingMap.get(targetSig).add(currentClassName);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private int calculateFanOut(CtMethod<?> method) {
        Set<String> uniqueCalled = new HashSet<>();
        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        for (CtInvocation<?> inv : invocations) {
            if (inv.getExecutable() != null) {
                uniqueCalled.add(inv.getExecutable().getSignature());
            }
        }
        return uniqueCalled.size();
    }

    private int calculateCC(CtMethod<?> method) {
        ComplexityVisitor v = new ComplexityVisitor();
        v.scan(method);
        return v.complexity;
    }

    private static class MethodMetrics {
        String filePath, className;
        int lineNum, cc, fanOut;

        public MethodMetrics(String f, String c, int l, int cc, int fo) {
            this.filePath = f; this.className = c; this.lineNum = l;
            this.cc = cc; this.fanOut = fo;
        }
    }

    private static class ComplexityVisitor extends CtScanner {
        int complexity = 1;

        @Override
        public void visitCtIf(CtIf ifElement) {
            complexity++;
            super.visitCtIf(ifElement);
        }

        @Override
        public void visitCtFor(CtFor forLoop) {
            complexity++;
            super.visitCtFor(forLoop);
        }

        @Override
        public void visitCtForEach(CtForEach forEach) {
            complexity++;
            super.visitCtForEach(forEach);
        }

        @Override
        public void visitCtWhile(CtWhile whileLoop) {
            complexity++;
            super.visitCtWhile(whileLoop);
        }

        @Override
        public void visitCtDo(CtDo doLoop) {
            complexity++;
            super.visitCtDo(doLoop);
        }

        @Override
        public <E> void visitCtCase(CtCase<E> caseElement) {
            complexity++;
            super.visitCtCase(caseElement);
        }

        @Override
        public void visitCtCatch(CtCatch catchBlock) {
            complexity++;
            super.visitCtCatch(catchBlock);
        }

        @Override
        public <T> void visitCtConditional(CtConditional<T> conditional) {
            complexity++;
            super.visitCtConditional(conditional);
        }

        @Override
        public <T> void visitCtBinaryOperator(CtBinaryOperator<T> operator) {
            BinaryOperatorKind kind = operator.getKind();
            if (kind == BinaryOperatorKind.AND || kind == BinaryOperatorKind.OR) {
                complexity++;
            }
            super.visitCtBinaryOperator(operator);
        }
    }
}