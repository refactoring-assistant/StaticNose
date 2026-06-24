package edu.northeastern.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class generates a report of the file type HTML.
 * It builds a self-contained HTML file which includes the file contents
 * and highlighting for the code smells.
 */
public class HTMLReportGenerator extends AbstractReportGenerator {

    public HTMLReportGenerator(String inputDirPath) {
        super(inputDirPath);
    }

    @Override
    protected String getFileExtension() {
        return ".html";
    }

    @Override
    public void generate(List<ReportStruct> reportStructList) {
        Map<String, String> filesContentMap = new HashMap<>();
        for (ReportStruct rs : reportStructList) {
            String path = rs.getFilePath();
            String relativePath = rs.getRelativeFilePath();
            if (!filesContentMap.containsKey(relativePath)) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(path)));
                    filesContentMap.put(relativePath, content);
                } catch (IOException e) {
                    System.err.println("Failed to read file: " + path);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String reportDataJson = "[]";
        String filesContentMapJson = "{}";
        try {
            reportDataJson = mapper.writeValueAsString(reportStructList);
            filesContentMapJson = mapper.writeValueAsString(filesContentMap);
        } catch (Exception e) {
            System.err.println("Failed to serialize data: " + e.getMessage());
        }

        String htmlTemplate = getHtmlTemplate(reportDataJson, filesContentMapJson);

        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();

        if (parentDir != null) {
            try {
                FileUtils.safeCreateDir(parentDir.getAbsolutePath());
            } catch (RuntimeException e) {
                System.err.println("Aborting report generation: " + e.getMessage());
                return;
            }
        }

        try {
            Files.write(Paths.get(outputPath), htmlTemplate.getBytes());
            System.out.println("HTML Report generated at: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to write HTML: " + e.getMessage());
        }
    }

    private String getHtmlTemplate(String reportDataJson, String filesContentMapJson) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>StaticNose Report Viewer</title>\n" +
                "    <link href=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-vsc-dark-plus.min.css\" rel=\"stylesheet\" />\n" +
                "    <style>\n" +
                "        /* --- Layout & Reset --- */\n" +
                "        body { margin: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #1e1e1e; color: #d4d4d4; display: flex; height: 100vh; overflow: hidden; }\n" +
                "        * { box-sizing: border-box; }\n" +
                "        /* --- Sidebar --- */\n" +
                "        #sidebar { width: 320px; background: #252526; border-right: 1px solid #333; display: flex; flex-direction: column; flex-shrink: 0; }\n" +
                "        .sidebar-header { padding: 15px; background: #333; border-bottom: 1px solid #3e3e42; }\n" +
                "        .header-group { margin-bottom: 12px; }\n" +
                "        .header-group:last-child { margin-bottom: 0; }\n" +
                "        .header-title { font-weight: 600; color: #fff; font-size: 0.75rem; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; opacity: 0.7; }\n" +
                "        select { width: 100%; padding: 6px; background: #1e1e1e; color: #d4d4d4; border: 1px solid #3e3e42; border-radius: 4px; font-size: 0.85rem; outline: none; }\n" +
                "        select:focus { border-color: #007acc; }\n" +
                "        #file-list { flex: 1; overflow-y: auto; list-style: none; padding: 0; margin: 0; }\n" +
                "        .file-item { padding: 8px 15px; cursor: pointer; border-bottom: 1px solid #2d2d30; display: flex; justify-content: space-between; align-items: center; }\n" +
                "        .file-item:hover { background: #2a2d2e; }\n" +
                "        .file-item.active { background: #37373d; border-left: 3px solid #4ec9b0; }\n" +
                "        .file-name { font-size: 0.85rem; color: #cccccc; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 220px; }\n" +
                "        .badge { background: #f44336; color: white; font-size: 0.7rem; font-weight: bold; padding: 2px 6px; border-radius: 4px; min-width: 20px; text-align: center; }\n" +
                "        /* --- Main Code Viewer --- */\n" +
                "        #main-view { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #1e1e1e; }\n" +
                "        #code-header { padding: 10px 20px; background: #2d2d30; border-bottom: 1px solid #3e3e42; font-family: 'Consolas', monospace; color: #9cdcfe; font-size: 0.9rem; }\n" +
                "        #code-container { flex: 1; overflow: auto; position: relative; padding-bottom: 50px; }\n" +
                "        /* --- TABLE LAYOUT --- */\n" +
                "        .code-table { width: 100%; border-collapse: collapse; font-family: 'Consolas', 'Monaco', monospace; font-size: 14px; line-height: 1.5; table-layout: auto; }\n" +
                "        .line-num-cell { width: 50px; text-align: right; padding-right: 15px; color: #6e7681; user-select: none; border-right: 1px solid #3e3e42; vertical-align: top; background: #1e1e1e; }\n" +
                "        .code-cell { padding-left: 15px; white-space: pre; color: #d4d4d4; vertical-align: top; width: 100%; }\n" +
                "        .info-cell { width: 1px; white-space: nowrap; vertical-align: top; padding-right: 10px; }\n" +
                "        .has-smell { background-color: rgba(244, 67, 54, 0.15); }\n" +
                "        .has-smell .line-num-cell { color: #f44336; font-weight: bold; }\n" +
                "        .smell-tag { display: inline-block; font-size: 11px; font-family: 'Segoe UI', sans-serif; background: #d32f2f; color: white; padding: 2px 8px; border-radius: 10px; margin-left: 20px; opacity: 0.9; box-shadow: 0 1px 3px rgba(0,0,0,0.3); }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"sidebar\">\n" +
                "    <div class=\"sidebar-header\">\n" +
                "        <div class=\"header-group\">\n" +
                "            <div class=\"header-title\">Filter by Type</div>\n" +
                "            <select id=\"smell-filter\">\n" +
                "                <option value=\"ALL\">All Smells</option>\n" +
                "            </select>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <div id=\"file-list\"></div>\n" +
                "</div>\n" +
                "<div id=\"main-view\">\n" +
                "    <div id=\"code-header\">No file selected</div>\n" +
                "    <div id=\"code-container\"></div>\n" +
                "</div>\n" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js\"></script>\n" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-java.min.js\"></script>\n" +
                "<script>\n" +
                "    const filesContentMap = " + filesContentMapJson + ";\n" +
                "    const reportData = " + reportDataJson + ";\n" +
                "    let issuesMap = {};\n" +
                "    const smellFilter = document.getElementById('smell-filter');\n" +
                "    smellFilter.addEventListener('change', () => renderSidebar());\n" +
                "    function loadReport() {\n" +
                "        issuesMap = {};\n" +
                "        const uniqueSmells = new Set();\n" +
                "        reportData.forEach(entry => {\n" +
                "            if (!entry.lineNumbers || entry.lineNumbers.length === 0) return;\n" +
                "            if (entry.lineNumbers.length === 1 && entry.lineNumbers[0] === -1) return;\n" +
                "            const path = entry.relativeFilePath;\n" +
                "            if (!issuesMap[path]) issuesMap[path] = {};\n" +
                "            const smellName = entry.smellName || 'Code Smell';\n" +
                "            const additionalInfo = entry.additionalInfo || '';\n" +
                "            uniqueSmells.add(smellName);\n" +
                "            entry.lineNumbers.forEach(line => {\n" +
                "                if(!issuesMap[path][line]) issuesMap[path][line] = [];\n" +
                "                const issueObj = { name: smellName, info: additionalInfo };\n" +
                "                const exists = issuesMap[path][line].some(i => i.name === smellName && i.info === additionalInfo);\n" +
                "                if (!exists) issuesMap[path][line].push(issueObj);\n" +
                "            });\n" +
                "        });\n" +
                "        populateFilter(uniqueSmells);\n" +
                "        document.getElementById('code-container').innerHTML = '';\n" +
                "        document.getElementById('code-header').textContent = 'Select a file to view';\n" +
                "        renderSidebar();\n" +
                "    }\n" +
                "    function populateFilter(smellSet) {\n" +
                "        smellFilter.innerHTML = '<option value=\"ALL\">All Smells</option>';\n" +
                "        Array.from(smellSet).sort().forEach(smell => {\n" +
                "            const opt = document.createElement('option');\n" +
                "            opt.value = smell;\n" +
                "            opt.textContent = smell;\n" +
                "            smellFilter.appendChild(opt);\n" +
                "        });\n" +
                "        smellFilter.value = 'ALL';\n" +
                "    }\n" +
                "    function renderSidebar() {\n" +
                "        const list = document.getElementById('file-list');\n" +
                "        list.innerHTML = '';\n" +
                "        const filter = smellFilter.value;\n" +
                "        let visibleFiles = 0;\n" +
                "        Object.keys(issuesMap).forEach(path => {\n" +
                "            const fileIssues = issuesMap[path];\n" +
                "            let count = 0;\n" +
                "            Object.values(fileIssues).forEach(issueList => {\n" +
                "                if (filter === 'ALL' || issueList.some(issue => issue.name === filter)) count++;\n" +
                "            });\n" +
                "            if (count === 0) return;\n" +
                "            visibleFiles++;\n" +
                "            const li = document.createElement('li');\n" +
                "            li.className = 'file-item';\n" +
                "            li.innerHTML = `<span class=\"file-name\" title=\"${path}\">${path.split('/').pop()}</span><span class=\"badge\">${count}</span>`;\n" +
                "            li.onclick = () => loadFile(path, li);\n" +
                "            list.appendChild(li);\n" +
                "        });\n" +
                "        if (visibleFiles === 0) list.innerHTML = '<div style=\"padding:15px; color:#888; font-size:0.9rem\">No files match filter.</div>';\n" +
                "    }\n" +
                "    function loadFile(path, listItem) {\n" +
                "        document.querySelectorAll('.file-item').forEach(i => i.classList.remove('active'));\n" +
                "        listItem.classList.add('active');\n" +
                "        document.getElementById('code-header').textContent = path;\n" +
                "        const content = filesContentMap[path];\n" +
                "        if (!content) return;\n" +
                "        const container = document.getElementById('code-container');\n" +
                "        const lines = content.split(/\\r?\\n/);\n" +
                "        const allIssues = issuesMap[path] || {};\n" +
                "        const filter = smellFilter.value;\n" +
                "        let html = `<table class=\"code-table\">`;\n" +
                "        lines.forEach((lineText, index) => {\n" +
                "            const lineNum = index + 1;\n" +
                "            const lineIssues = allIssues[lineNum];\n" +
                "            let visibleIssues = [];\n" +
                "            if (lineIssues) {\n" +
                "                if (filter === 'ALL') visibleIssues = lineIssues;\n" +
                "                else visibleIssues = lineIssues.filter(i => i.name === filter);\n" +
                "            }\n" +
                "            let rowClass = visibleIssues.length > 0 ? 'has-smell' : '';\n" +
                "            let tagHtml = visibleIssues.map(i => {\n" +
                "                const displayText = i.info ? `${i.name}: ${i.info}` : i.name;\n" +
                "                return `<span class=\"smell-tag\">${displayText}</span>`;\n" +
                "            }).join(' ');\n" +
                "            const highlightedCode = Prism.highlight(lineText, Prism.languages.java, 'java');\n" +
                "            html += `\n" +
                "                    <tr class=\"${rowClass}\">\n" +
                "                        <td class=\"line-num-cell\">${lineNum}</td>\n" +
                "                        <td class=\"code-cell\">${highlightedCode}</td>\n" +
                "                        <td class=\"info-cell\">${tagHtml}</td>\n" +
                "                    </tr>`;\n" +
                "        });\n" +
                "        html += `</table>`;\n" +
                "        container.innerHTML = html;\n" +
                "    }\n" +
                "    loadReport();\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }
}
