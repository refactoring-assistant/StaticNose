package edu.northeastern.smells;

import spoon.reflect.declaration.*;

import java.util.ArrayList;
import java.util.List;

public class LongParameterListDetector extends AbstractDetector{

    public LongParameterListDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Long Parameter List";
    }


    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        for(CtMethod<?> method : type.getMethods()) {
            int paramCount = method.getParameters().size();

            if(paramCount > 3) {
                if(isOverridden(method)) {
                    continue;
                }
                detectedLines.add(method.getPosition().getLine());
            }
        }

        if (type instanceof CtClass<?> clazz) {

            for (CtConstructor<?> constructor : clazz.getConstructors()) {
                if (constructor.getParameters().size() > 5) {
                    if (constructor.getPosition().isValidPosition()) {
                        detectedLines.add(constructor.getPosition().getLine());
                    }
                }
            }
        }

        return detectedLines;
    }

    private boolean isOverridden(CtMethod<?> method) {
        for(CtAnnotation<?> annotation : method.getAnnotations()) {
            if(annotation.getAnnotationType().getSimpleName().equals("Override")) {
                return true;
            }
        }
        return false;
    }
}
