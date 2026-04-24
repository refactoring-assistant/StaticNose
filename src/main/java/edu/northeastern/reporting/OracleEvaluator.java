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

//    public void evaluateAndReport() {
//        Map<String, Set<String>> expectedMap = parseOracleFile();
//        Map<String, Set<String>> detectedMap = parseDetectedReports();
//
//        // 1. Generate the side-by-side CSV
//        generateComparisonCSV(expectedMap, detectedMap);
//
//        // 2. Setup global aggregators
//        // Use TreeSet so the printed list of files is alphabetically sorted
//        Set<String> allFiles = new TreeSet<>(expectedMap.keySet());
//        allFiles.addAll(detectedMap.keySet());
//
//        // REMOVE EMPTY KEYS: Prevent overcounting if a blank line was parsed
//        allFiles.remove("");
//
//        int totalTp = 0, totalFp = 0, totalFn = 0, totalTn = 0;
//
//        // 3. Calculate Global TP, FP, FN, TN
//        for (String fileName : allFiles) {
//            Set<String> expected = expectedMap.getOrDefault(fileName, Collections.emptySet());
//            Set<String> detected = detectedMap.getOrDefault(fileName, Collections.emptySet());
//
//            Set<String> truePositives = new HashSet<>(expected);
//            truePositives.retainAll(detected);
//            totalTp += truePositives.size();
//
//            Set<String> falsePositives = new HashSet<>(detected);
//            falsePositives.removeAll(expected);
//            totalFp += falsePositives.size();
//
//            Set<String> falseNegatives = new HashSet<>(expected);
//            falseNegatives.removeAll(expected); // Note: Fix logic here to match your specific set math
//            totalFn += (expected.size() - truePositives.size());
//
//            if (expected.isEmpty() && detected.isEmpty()) {
//                totalTn++;
//            }
//        }
//
//        // 4. Calculate Master Metrics
//        double precision = (totalTp + totalFp == 0) ? 0.0 : (double) totalTp / (totalTp + totalFp);
//        double recall = (totalTp + totalFn == 0) ? 0.0 : (double) totalTp / (totalTp + totalFn);
//        double f1 = (precision + recall == 0) ? 0.0 : 2 * ((precision * recall) / (precision + recall));
//
//        // 5. Print the Global Report
//        System.out.println("\n========================================");
//        System.out.println("   STATICNOSE GLOBAL METRICS REPORT     ");
//        System.out.println("========================================");
//        System.out.printf("  Total Files Analyzed : %d\n", allFiles.size());
//        System.out.println("----------------------------------------");
//        System.out.printf("  Total TP: %-4d Total FP: %-4d\n", totalTp, totalFp);
//        System.out.printf("  Total FN: %-4d Total TN: %-4d\n", totalFn, totalTn);
//        System.out.println("----------------------------------------");
//        System.out.printf("  Overall Precision : %.2f%%\n", precision * 100);
//        System.out.printf("  Overall Recall    : %.2f%%\n", recall * 100);
//        System.out.printf("  Overall F1-Score  : %.2f\n", f1);
//        System.out.println("----------------------------------------");
////
////        // 6. Print the list of all files
////        System.out.println("  FILES INCLUDED IN ANALYSIS:");
////        int count = 1;
////        for (String file : allFiles) {
////            System.out.printf("  %3d. %s\n", count++, file);
////        }
////        System.out.println("========================================\n");
//    }


public void evaluateAndReport() {
    Map<String, Set<String>> expectedMap = parseOracleFile();
    Map<String, Set<String>> detectedMap = parseDetectedReports();

    generateComparisonCSV(expectedMap, detectedMap);

    Set<String> allFiles = new TreeSet<>(expectedMap.keySet());
    allFiles.addAll(detectedMap.keySet());
    allFiles.remove("");

    // Collect all unique smells dynamically from both expected and detected datasets
    Set<String> allSmells = new TreeSet<>();
    expectedMap.values().forEach(allSmells::addAll);
    detectedMap.values().forEach(allSmells::addAll);

    // Map to hold per-smell metrics: int array holds [TP, FP, FN, TN]
    Map<String, int[]> smellMetrics = new HashMap<>();
    for (String smell : allSmells) {
        smellMetrics.put(smell, new int[4]);
    }

    int totalTp = 0, totalFp = 0, totalFn = 0, totalTn = 0;

    // To track detailed misses for the final report
    List<String> missedSmellDetails = new ArrayList<>();

    for (String fileName : allFiles) {
        Set<String> expected = expectedMap.getOrDefault(fileName, Collections.emptySet());
        Set<String> detected = detectedMap.getOrDefault(fileName, Collections.emptySet());

        // 1. True Positives: Intersection
        Set<String> truePositives = new HashSet<>(expected);
        truePositives.retainAll(detected);
        totalTp += truePositives.size();

        // 2. False Positives: Detected but not expected
        Set<String> falsePositives = new HashSet<>(detected);
        falsePositives.removeAll(expected);
        totalFp += falsePositives.size();

        // 3. False Negatives: Expected but not detected
        Set<String> falseNegatives = new HashSet<>(expected);
        falseNegatives.removeAll(detected);

        if (!falseNegatives.isEmpty()) {
            for (String smell : falseNegatives) {
                missedSmellDetails.add(String.format("  - %-20s : Missing [%s]", fileName, smell));
            }
        }
        totalFn += falseNegatives.size();

        // 4. True Negatives (Global): Both sets completely empty
        if (expected.isEmpty() && detected.isEmpty()) {
            totalTn++;
        }

        // --- PER-SMELL METRICS CALCULATION ---
        for (String smell : allSmells) {
            boolean isExpected = expected.contains(smell);
            boolean isDetected = detected.contains(smell);

            int[] metrics = smellMetrics.get(smell);

            if (isExpected && isDetected) {
                metrics[0]++; // TP
            } else if (!isExpected && isDetected) {
                metrics[1]++; // FP
            } else if (isExpected && !isDetected) {
                metrics[2]++; // FN
            } else {
                metrics[3]++; // TN
            }
        }
    }

    // Master Metrics
    double precision = (totalTp + totalFp == 0) ? 0.0 : (double) totalTp / (totalTp + totalFp);
    double recall = (totalTp + totalFn == 0) ? 0.0 : (double) totalTp / (totalTp + totalFn);
    double f1 = (precision + recall == 0) ? 0.0 : 2 * ((precision * recall) / (precision + recall));

    // 1. Print Global Report
    System.out.println("\n========================================");
    System.out.println("   STATICNOSE GLOBAL METRICS REPORT     ");
    System.out.println("========================================");
    System.out.printf("  Total Files Analyzed : %d\n", allFiles.size());
    System.out.println("----------------------------------------");
    System.out.printf("  Total TP: %-4d Total FP: %-4d\n", totalTp, totalFp);
    System.out.printf("  Total FN: %-4d Total TN: %-4d\n", totalFn, totalTn);
    System.out.println("----------------------------------------");
    System.out.printf("  Overall Precision : %.2f%%\n", precision * 100);
    System.out.printf("  Overall Recall    : %.2f%%\n", recall * 100);
    System.out.printf("  Overall F1-Score  : %.2f\n", f1);
    System.out.println("----------------------------------------");

    // 2. Print Per-Smell Report
    System.out.println("\n=========================================================================================");
    System.out.println("                         STATICNOSE PER-SMELL METRICS REPORT");
    System.out.println("=========================================================================================");
    // Added 'Samples' to the header format
    System.out.printf("  %-24s | %7s | %4s | %4s | %4s | %4s | %9s | %8s | %4s\n",
            "Smell Name", "Samples", "TP", "FP", "FN", "TN", "Precision", "Recall", "F1");
    System.out.println("-----------------------------------------------------------------------------------------");

    for (String smell : allSmells) {
        int[] m = smellMetrics.get(smell);
        int tp = m[0], fp = m[1], fn = m[2], tn = m[3];

        // The number of actual occurrences in the Oracle (Support)
        int samples = tp + fn;

        double prec = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
        double rec = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
        double f1Smell = (prec + rec == 0) ? 0.0 : 2 * ((prec * rec) / (prec + rec));

        // Added 'samples' to the row format
        System.out.printf("  %-24s | %7d | %4d | %4d | %4d | %4d | %8.2f%% | %7.2f%% | %4.2f\n",
                smell, samples, tp, fp, fn, tn, prec * 100, rec * 100, f1Smell);
    }
    System.out.println("=========================================================================================\n");

    // 3. Print Detailed Misses
    if (!missedSmellDetails.isEmpty()) {
        System.out.println("  DETAILED FALSE NEGATIVES (MISSED):");
        missedSmellDetails.forEach(System.out::println);
    } else {
        System.out.println("  Perfect Recall! No smells were missed.");
    }
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