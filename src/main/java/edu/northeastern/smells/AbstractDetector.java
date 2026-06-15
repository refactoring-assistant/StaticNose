package edu.northeastern.smells;

import edu.northeastern.reporting.ReportStruct;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import spoon.reflect.visitor.filter.TypeFilter;

public abstract class AbstractDetector implements IDetector{

    protected final List<String> javaFilePaths;
    protected final String inputDirPath;
    protected Set<String> projectClassNames = null;

    public AbstractDetector(List<String> javaFilePaths, String inputDirPath) {
        this.javaFilePaths = javaFilePaths;
        this.inputDirPath = inputDirPath;
    }

    protected abstract String getSmellName();

    @Override
    public List<ReportStruct> run() {

        Launcher launcher = new Launcher();

        for(String javaFilePath : javaFilePaths) {
            launcher.addInputResource(javaFilePath);
        }

        launcher.getEnvironment().setComplianceLevel(17);
        launcher.getEnvironment().setNoClasspath(true);
        configureLauncher(launcher);

        try {
            launcher.buildModel();
        } catch(Exception e) {
            System.err.println("Error building Spoon model: " + e.getMessage());
            return new ArrayList<>();
        }

        CtModel model = launcher.getModel();
        
        projectClassNames = new HashSet<>();
        for (CtType<?> t : model.getElements(new TypeFilter<>(CtType.class))) {
            projectClassNames.add(t.getQualifiedName());
        }

        List<ReportStruct> reportStructList = new ArrayList<>();

        for(CtType<?> type : model.getAllTypes()) {
            if(!type.getPosition().isValidPosition()) {
                continue;
            }

            List<Integer> detectedLines = analyzeType(type);

            if(!detectedLines.isEmpty()) {
                File originFile = type.getPosition().getFile();
                String originPath = (originFile != null) ? originFile.getPath()  : "Unknown File";

                ReportStruct report = new ReportStruct(
                        getSmellName(),
                        originPath,
                        this.inputDirPath,
                        type.getSimpleName(),
                        ""
                );

                report.addLineNumbers(detectedLines);
                reportStructList.add(report);
            }
        }

        return reportStructList;
    }

    protected void configureLauncher(Launcher launcher)
    {

    }

    protected abstract List<Integer> analyzeType(CtType<?> type);

    protected boolean isProjectClass(String qualifiedName) {
        if (qualifiedName == null || projectClassNames == null) return false;
        return projectClassNames.contains(qualifiedName);
    }
}
