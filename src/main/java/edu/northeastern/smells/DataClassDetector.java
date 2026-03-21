package edu.northeastern.smells;

import spoon.reflect.code.*;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.List;

import static edu.northeastern.utils.Metrics.calculateWMC;
import static edu.northeastern.utils.Metrics.isAccessor;

/**
 * Data Class code smell detector.
 * A Data Class is a class that only acts as a store for fields.
 * It primarily sets field values and then makes them accessible through
 * getters to other classes and has no other real value besides that.
 * The metrics used to detect a Data Class are
 * 1. WOC: Weight of Class (number of functional public methods/total number of public methods)
 * 2. WMC: Weighted Methods per Class (sum of CC of all methods in class)
 * 3. NOPA: Number Of Public Attributes (non-constant public fields)
 * 4. NOAM: Number of Accessor Methods (total number of getters and setters)
 */
public class DataClassDetector extends AbstractDetector {

    // few fields threshold
    private static final int ACCESSOR_OR_FIELD_FEW_LEVEL = 3;
    // many fields threshold
    private static final int ACCESSOR_OR_FIELD_MANY_LEVEL = 5;
    // percentage of weight of class (functional/total methods)
    // should be less than 33%
    private static final double WOC_LEVEL = 1.0 / 3.0;
    // weight of methods threshold.
    // if few fields then use this threshold
    private static final int WMC_HIGH_LEVEL = 31;
    // if many fields then use this threshold
    private static final int WMC_VERY_HIGH_LEVEL = 47;

    public DataClassDetector(List<String> javaFilePaths, String inputDirPath) {
        super(javaFilePaths, inputDirPath);
    }

    @Override
    protected String getSmellName() {
        return "Data Class";
    }

    @Override
    protected List<Integer> analyzeType(CtType<?> type) {
        List<Integer> detectedLines = new ArrayList<>();

        if (type.isInterface() || type.isEnum()) {
            return detectedLines;
        }

        int wmc = calculateWMC(type);
        int nopa = calculateNOPA(type);
        int noam = calculateNOAM(type);
        double woc = calculateWOC(type);

        // sum of CC of all methods should meet WOC_LEVEL
        boolean interfaceRevealsData = woc < WOC_LEVEL;

        // nopa + noam tells us how much data the class reveals about itself
        // if a class reveals too much data about itself and lacks complexity then
        // we can conclude that it is a data class
        boolean revealsDataAndLacksComplexity =
                (nopa + noam > ACCESSOR_OR_FIELD_FEW_LEVEL && wmc < WMC_HIGH_LEVEL) ||
                        (nopa + noam > ACCESSOR_OR_FIELD_MANY_LEVEL && wmc < WMC_VERY_HIGH_LEVEL);

        // Only if both combined metrics pass (IRD and RDLC)
        if (interfaceRevealsData && revealsDataAndLacksComplexity) {
            if (type.getPosition().isValidPosition()) {
                detectedLines.add(type.getPosition().getLine());
            }
        }

        return detectedLines;
    }

    /**
     * Number of Public Attributes (Fields)
     * This calculated the true number of public fields a class
     * makes available to other classes. It ignores constants
     * which are static and final.
     * @param type The class whose NOPA needs to be calculated
     * @return The NOPA metric value
     */
    private int calculateNOPA(CtType<?> type) {
        int count = 0;
        for (CtField<?> field : type.getFields()) {
            if (field.isPublic() && !field.isStatic() && !field.isFinal()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Number Of Accessor Methods
     * The total number of getter and setter methods in a class
     * @param type the class whose NOAM needs to be calculated
     * @return the NOAM metric value
     */
    private int calculateNOAM(CtType<?> type) {
        int count = 0;
        for (CtMethod<?> method : type.getMethods()) {
            if (isAccessor(method, true)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Calculate the Weight of Class metric.
     * WOC is the ratio of functional public methods to total number of public methods
     * in a class.
     * Data Classes have a low WOC score since they do not contain
     * many functional methods
     * @param type The class whose WOC metric needs to be calculated
     * @return the WOC metric
     */
    private double calculateWOC(CtType<?> type) {
        List<CtMethod<?>> publicMethods = type.getMethods().stream()
                .filter(CtMethod::isPublic)
                .filter(m -> !m.isAbstract()) // interfaces/abstract classes check
                .toList();

        long totalPublicMethods = publicMethods.size();
        if (totalPublicMethods == 0) return 0.0;

        long functionalMethods = publicMethods.stream()
                .filter(m -> !isAccessor(m, true))
                .count();

        return (double) functionalMethods / totalPublicMethods;
    }

}