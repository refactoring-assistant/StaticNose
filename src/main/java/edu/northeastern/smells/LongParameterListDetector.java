package edu.northeastern.smells;

import spoon.reflect.declaration.*;

import java.util.ArrayList;
import java.util.List;

public class LongParameterListDetector extends AbstractDetector{

    private final int MAX_METHOD_PARAMS;
    private final int MAX_CONSTRUCTOR_PARAMS;

    public LongParameterListDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
        this.MAX_METHOD_PARAMS = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_METHOD_PARAMS", 3);
        this.MAX_CONSTRUCTOR_PARAMS = edu.northeastern.core.ConfigurationManager.getInt(getSmellName(), "MAX_CONSTRUCTOR_PARAMS", 5);
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

            if(paramCount > MAX_METHOD_PARAMS) {
                if(isOverridden(method)) {
                    continue;
                }
                detectedLines.add(method.getPosition().getLine());
            }
        }

        if (type instanceof CtClass<?> clazz) {

            for (CtConstructor<?> constructor : clazz.getConstructors()) {
                if (constructor.getParameters().size() > MAX_CONSTRUCTOR_PARAMS) {
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
