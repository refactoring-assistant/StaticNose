package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;

import static edu.northeastern.utils.Metrics.*;

/**
 * This class detects a Large Class code smell.
 * A Large class is one that is doing too many things or simply
 * breaking single responsibility.
 * The detector works by calculating the WMC and TCC metrics
 */
public class LargeClassDetector extends AbstractDetector{

    private final int WMC_THRESHOLD;
    private final double TCC_THRESHOLD;

    public LargeClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        WMC_THRESHOLD = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "WMC_THRESHOLD", 47);
        TCC_THRESHOLD = edu.northeastern.core.ConfigurationManager.getDouble(getSmellName(), "TCC_THRESHOLD", 0.33);
    }

    @Override
    protected String getSmellName() {
        return "Large Class";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        if (type.isEnum() || type.isInterface() || type.isAnnotationType() || type.getMethods().isEmpty()) {
            return detectedLines;
        }

        int wmc = calculateWMC(type);

        double tcc = calculateTCC(type);

        int fieldCount = type.getFields().size();

        // 2. Count Logical Lines of Code (if you have calculateLLOC in Metrics)
         int lloc = calculateLLOC(type);

        boolean isGodClass = (wmc >= WMC_THRESHOLD && tcc < TCC_THRESHOLD);

        boolean isDataHeavyClass = (fieldCount >= 8);

        if (isGodClass || isDataHeavyClass) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    private double calculateTCC(CtType<?> type) {
        List<CtMethod<?>> methods = new ArrayList<>();

        for (CtMethod<?> m : type.getMethods()) {
            if (m.getBody() != null && !isAccessor(m, false)) {
                methods.add(m);
            }
        }

        int n = methods.size();
        if (n < 2) return 1.0;

        long maxPairs = (long) n * (n - 1) / 2;
        long connectedPairs = 0;

        Map<CtMethod<?>, Set<String>> fieldUsage = getMethodFieldUsageMap(type, methods, 0.70);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<String> fields1 = fieldUsage.get(methods.get(i));
                Set<String> fields2 = fieldUsage.get(methods.get(j));

                if (isConnected(fields1, fields2)) {
                    connectedPairs++;
                }
            }
        }

        return (double) connectedPairs / maxPairs;
    }

    private boolean isConnected(Set<String> fields1, Set<String> fields2) {
        for (String field : fields1) {
            if (fields2.contains(field)) {
                return true;
            }
        }
        return false;
    }


}
