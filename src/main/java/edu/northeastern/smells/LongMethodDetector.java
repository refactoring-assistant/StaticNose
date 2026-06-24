package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.ArrayList;
import java.util.List;

import static edu.northeastern.utils.Metrics.calculateCyclomaticComplexity;
import static edu.northeastern.utils.Metrics.calculateLLOC;

public class LongMethodDetector extends AbstractDetector{

    private final int MAX_LLOC;
    private final int MAX_COMPLEXITY;

    public LongMethodDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        this.MAX_LLOC = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_LLOC", 30);
        this.MAX_COMPLEXITY = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_COMPLEXITY", 15);
    }

    @Override
    protected String getSmellName() {
        return "Long Method";
    }


    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        for(CtMethod<?> method : type.getMethods()) {
            if(method.getBody() == null) continue;

            int lloc = calculateLLOC(method);
            int complexity = calculateCyclomaticComplexity(method);

            if (lloc >= MAX_LLOC || complexity > MAX_COMPLEXITY) {
                if (method.getPosition().isValidPosition()) {
                    if (method.getBody() != null && method.getBody().getPosition().isValidPosition()) {
                        detectedLines.add(method.getBody().getPosition().getLine());
                    } else if (method.getType() != null && method.getType().getPosition().isValidPosition()) {
                        detectedLines.add(method.getType().getPosition().getLine());
                    } else {
                        detectedLines.add(method.getPosition().getLine());
                    }
                }
            }
        }

        return detectedLines;
    }

}


