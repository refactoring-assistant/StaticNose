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

    public LongMethodDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
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

            if (lloc > 30 && complexity > 5) {
                // Always check if the position is valid first
                if (method.getPosition().isValidPosition()) {

                    // THE FIX: Use the position of the method's body (the '{')
                    // or the return type. This completely bypasses all Javadoc
                    // and annotations that sit above the method signature.
                    if (method.getBody() != null && method.getBody().getPosition().isValidPosition()) {

                        // Points to the exact line where the actual code block starts
                        detectedLines.add(method.getBody().getPosition().getLine());

                    } else if (method.getType() != null && method.getType().getPosition().isValidPosition()) {

                        // Fallback: Points to the return type (e.g., the 'void' in 'public void main')
                        detectedLines.add(method.getType().getPosition().getLine());

                    } else {

                        // Absolute fallback
                        detectedLines.add(method.getPosition().getLine());
                    }
                }
            }
        }

        return detectedLines;
    }

}
