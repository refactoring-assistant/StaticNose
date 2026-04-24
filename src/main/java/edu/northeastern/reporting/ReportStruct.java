package edu.northeastern.reporting;

import edu.northeastern.utils.PathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes a report structure for a single analyzed Java
 * file. It contains information to find where the code smell occurred in
 * the given project if it did. If no code smell occurred in the class,
 * the lineNumbers List contains a single integer -1. Else, it contains
 * the list of line numbers where it occurred.
 */
public class ReportStruct {
    private final String smellName;
    private final String filePath;
    private final String className;
    private final List<Integer> lineNumbers;
    private final String relativeFilePath;
    private final String additionalInfo;

    public ReportStruct(String smellName, String filePath, String inputDirPath, String className, String additionalInfo) {
        this.smellName = smellName;
        this.filePath = filePath;
        this.className = className;
        this.lineNumbers = new ArrayList<>();
        this.relativeFilePath = PathHelper.getPathFromInputFolder(filePath, inputDirPath);
        this.additionalInfo = additionalInfo;
    }

    /**
     * Returns the file path of the file where this code smell was detected.
     * @return filepath string
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the additional information stored about this code smell.
     * @return additional information string
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Returns the relative file path of the file where this code smell was detected.
     * @return the relative filepath string
     */
    public String getRelativeFilePath() { return relativeFilePath; }

    /**
     * Returns the class name where this code smell was detected.
     * @return the classname string
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the list of line numbers where this code smell was detected.
     * @return the list of lines where this code smell was detected
     */
    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    /**
     * Returns the name of the code smell detected.
     * @return name of the code smell string
     */
    public String getSmellName() {
        return smellName;
    }

    /**
     * Function to add a line number to this code smell occurrence.
     * @param lineNumber the line number in the file
     */
    public void addLineNumber(int lineNumber) {
        this.lineNumbers.add(lineNumber);
    }

    /**
     * Function to add multiple line numbers are once to this code smell occurrence.
     * @param lineNumbers the list of line numbers to add
     */
    public void addLineNumbers(List<Integer> lineNumbers) {
        this.lineNumbers.addAll(lineNumbers);
    }
}
