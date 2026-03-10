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

    public String getFilePath() {
        return filePath;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getRelativeFilePath() { return relativeFilePath; }

    public String getClassName() {
        return className;
    }

    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public String getSmellName() {
        return smellName;
    }

    public void addLineNumber(int lineNumber) {
        this.lineNumbers.add(lineNumber);
    }

    public void addLineNumbers(List<Integer> lineNumbers) {
        this.lineNumbers.addAll(lineNumbers);
    }
}
