package edu.northeastern.smells;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataClumpsDetector extends AbstractDetector {

    private static final int CLUMP_SIZE_THRESHOLD = 3;

    public DataClumpsDetector(List<String> javaFilePaths, String inputCLIArg) {
        super(javaFilePaths, inputCLIArg);
    }

    @Override
    protected String getSmellName() {
        return "Data Clumps";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        Set<Integer> detectedLines = new HashSet<>();
        List<CtMethod<?>> methods = new ArrayList<>(type.getMethods());

        if (methods.size() < 2) {
            return new ArrayList<>();
        }

        for (int i = 0; i < methods.size(); i++) {
            CtMethod<?> m1 = methods.get(i);

            if (m1.getParameters().size() < CLUMP_SIZE_THRESHOLD) continue;

            for (int j = i + 1; j < methods.size(); j++) {
                CtMethod<?> m2 = methods.get(j);

                if (m2.getParameters().size() < CLUMP_SIZE_THRESHOLD) continue;

                if (hasDataClump(m1, m2)) {
                    if (m1.getPosition().isValidPosition()) {
                        detectedLines.add(m1.getPosition().getLine());
                    }
                    if (m2.getPosition().isValidPosition()) {
                        detectedLines.add(m2.getPosition().getLine());
                    }
                }
            }
        }

        return new ArrayList<>(detectedLines);
    }

    private boolean hasDataClump(CtMethod<?> m1, CtMethod<?> m2) {
        int matchCount = 0;
        List<CtParameter<?>> params1 = m1.getParameters();
        List<CtParameter<?>> params2 = m2.getParameters();

        for (CtParameter<?> p1 : params1) {
            for (CtParameter<?> p2 : params2) {
                if (isSameParameter(p1, p2)) {
                    matchCount++;
                    if (matchCount >= CLUMP_SIZE_THRESHOLD) {
                        return true;
                    }
                    break;
                }
            }
        }

        return matchCount >= CLUMP_SIZE_THRESHOLD;
    }

    private boolean isSameParameter(CtParameter<?> p1, CtParameter<?> p2) {
        String name1 = p1.getSimpleName();
        String name2 = p2.getSimpleName();

        String type1 = p1.getType().getQualifiedName();
        String type2 = p2.getType().getQualifiedName();

        return name1.equals(name2) && type1.equals(type2);
    }
}