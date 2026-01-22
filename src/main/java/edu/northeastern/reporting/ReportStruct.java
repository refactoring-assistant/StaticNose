package edu.northeastern.reporting;

import java.io.File;
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
    private final String filePath;
    private final String className;
    private List<Integer> lineNumbers;
    private final boolean hasCodeSmell;

    public ReportStruct(String filePath, String className, boolean hasCodeSmell) {
        this.filePath = filePath;
        this.className = className;
        this.hasCodeSmell = hasCodeSmell;
        this.lineNumbers = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getClassName() {
        return className;
    }

    public boolean getHasCodeSmell() {
        return hasCodeSmell;
    }

    public List<Integer> getLineNumbers() {
        return lineNumbers;
    }

    public void addLineNumber(int lineNumber) {
        this.lineNumbers.add(lineNumber);
    }

    public void addLineNumbers(List<Integer> lineNumbers) {
        this.lineNumbers.addAll(lineNumbers);
    }
}
