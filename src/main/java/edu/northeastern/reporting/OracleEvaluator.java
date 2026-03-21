package edu.northeastern.reporting;

import edu.northeastern.cli.CodeSmell;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class OracleEvaluator {

    private final File oracleFile;
    private final List<ReportStruct> detectedReports;
    private final List<CodeSmell> targetSmells;
    private final String outputDirPath;

    public OracleEvaluator(File oracleFile, List<ReportStruct> detectedReports, List<CodeSmell> targetSmells, String outputDirPath) {
        this.oracleFile = oracleFile;
        this.detectedReports = detectedReports;
        this.targetSmells = targetSmells;
        this.outputDirPath = outputDirPath;
    }

    public void evaluateAndReport() {
        Map<String, Set<String>> expectedMap = parseOracleFile();
        Map<String, Set<String>> detectedMap = parseDetectedReports();

        // 1. Generate the side-by-side CSV
        generateComparisonCSV(expectedMap, detectedMap);

        // 2. Setup global aggregators
        Set<String> allFiles = new HashSet<>(expectedMap.keySet());
        allFiles.addAll(detectedMap.keySet());

        Set<String> allSmellNames = new HashSet<>();
        for (Set<String> smells : expectedMap.values()) allSmellNames.addAll(smells);
        for (Set<String> smells : detectedMap.values()) allSmellNames.addAll(smells);

        int totalTp = 0, totalFp = 0, totalFn = 0, totalTn = 0;

        // 3. Calculate Global TP, FP, FN, TN
        for (String fileName : allFiles) {
            Set<String> expected = expectedMap.getOrDefault(fileName, Collections.emptySet());
            Set<String> detected = detectedMap.getOrDefault(fileName, Collections.emptySet());

            for (String smellName : allSmellNames) {
                boolean isExpected = expected.contains(smellName);
                boolean isDetected = detected.contains(smellName);

                if (isExpected && isDetected) totalTp++;
                else if (!isExpected && isDetected) totalFp++;
                else if (isExpected && !isDetected) totalFn++;
                else totalTn++;
            }
        }

        // 4. Calculate Master Metrics
        double precision = (totalTp + totalFp == 0) ? 0.0 : (double) totalTp / (totalTp + totalFp);
        double recall = (totalTp + totalFn == 0) ? 0.0 : (double) totalTp / (totalTp + totalFn);
        double f1 = (precision + recall == 0) ? 0.0 : 2 * ((precision * recall) / (precision + recall));

        // 5. Print the Global Report
        System.out.println("\n========================================");
        System.out.println("   STATICNOSE GLOBAL METRICS REPORT     ");
        System.out.println("========================================");
        System.out.printf("  Total TP: %-4d Total FP: %-4d\n", totalTp, totalFp);
        System.out.printf("  Total FN: %-4d Total TN: %-4d\n", totalFn, totalTn);
        System.out.println("----------------------------------------");
        System.out.printf("  Overall Precision : %.2f%%\n", precision * 100);
        System.out.printf("  Overall Recall    : %.2f%%\n", recall * 100);
        System.out.printf("  Overall F1-Score  : %.2f\n", f1);
        System.out.println("========================================\n");
    }

    private Map<String, Set<String>> parseOracleFile() {
        Map<String, Set<String>> map = new HashMap<>();

        // 1. Add .setAllowMissingColumnNames(true) to stop the trailing comma crash
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setAllowMissingColumnNames(true)
                .build();

        try (FileReader reader = new FileReader(oracleFile);
             CSVParser parser = format.parse(reader)) {

            for (CSVRecord record : parser) {
                // 2. Use indices (0 and 1) to bypass the invisible BOM character issue
                // Column 0 = File Name, Column 1 = Expected Code Smells
                String fileName = record.get(0).trim();
                String rawSmells = record.get(1);

                map.put(fileName, cleanAndSplitSmells(rawSmells));
            }
        } catch (Exception e) {
            System.err.println("Failed to read Oracle file: " + e.getMessage());
        }
        return map;
    }

    private Map<String, Set<String>> parseDetectedReports() {
        Map<String, Set<String>> map = new HashMap<>();

        for (ReportStruct report : detectedReports) {
            // Extract just the file name (e.g., "Pawn.java")
            String fileName = Paths.get(report.getFilePath()).getFileName().toString();

            // FIX: Strip the ".java" extension so it matches the Oracle keys
            if (fileName.endsWith(".java")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }

            map.putIfAbsent(fileName, new HashSet<>());
            map.get(fileName).add(report.getSmellName());
        }
        return map;
    }

    private void generateComparisonCSV(Map<String, Set<String>> expectedMap, Map<String, Set<String>> detectedMap) {
        String outputPath = outputDirPath + "/staticnose-report/oracle_comparison.csv";
        File outFile = new File(outputPath);

        if (outFile.getParentFile() != null) {
            FileUtils.safeCreateDir(outFile.getParentFile().getAbsolutePath());
        }

        String[] HEADERS = {"File Name", "Expected Code Smells", "Detected Code Smells"};
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader(HEADERS).build();

        Set<String> allFiles = new HashSet<>(expectedMap.keySet());
        allFiles.addAll(detectedMap.keySet());

        try (FileWriter out = new FileWriter(outFile);
             CSVPrinter printer = new CSVPrinter(out, format)) {

            for (String fileName : allFiles) {
                Set<String> expected = expectedMap.getOrDefault(fileName, Collections.emptySet());
                Set<String> detected = detectedMap.getOrDefault(fileName, Collections.emptySet());

                printer.printRecord(
                        fileName,
                        formatSmellSet(expected),
                        formatSmellSet(detected)
                );
            }
            System.out.println("\nComparison CSV generated at: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to write Comparison CSV: " + e.getMessage());
        }
    }

    // Helper to turn "['Data Clumps', 'Feature Envy']" into a clean Set of strings
    private Set<String> cleanAndSplitSmells(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new HashSet<>();

        return Arrays.stream(raw.split(","))
                .map(s -> s.replaceAll("[\\[\\]'\"]", "").trim()) // Remove brackets and quotes
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    // Helper to turn a Set of strings back into "['Smell1', 'Smell2']"
    private String formatSmellSet(Set<String> smells) {
        if (smells.isEmpty()) return "[]";
        return smells.stream()
                .map(smell -> "'" + smell + "'")
                .collect(Collectors.joining(", ", "[", "]"));
    }
}