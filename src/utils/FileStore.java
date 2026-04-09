package utils;

import java.io.*;
import java.util.*;

// handles all CSV file operations
public class FileStore {

    public static final String CREDENTIALS_PATH = "data/credentials.csv";
    public static final String HISTORY_PATH = "data/history.csv";
    public static final String VOCAB_PATH = "data/learned_vocab.csv";
    public static final String FEEDBACK_LOG_PATH = "data/feedback_log.csv";

    // adds a row to CSV file
    public void appendCsv(String filePath, String[] headers, String[] values) {
        try {
            File file = new File(filePath);
            ensureParentDirs(file);
            boolean needsHeader = !file.exists() || file.length() == 0;
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
                if (needsHeader) pw.println(String.join(",", escapeRow(headers)));
                pw.println(String.join(",", escapeRow(values)));
            }
        } catch (IOException e) {
            System.err.println("appendCsv failed for " + filePath + ": " + e.getMessage());
        }
    }

    // reads all rows from CSV
    public List<String[]> readCsv(String filePath) {
        List<String[]> rows = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) rows.add(parseCsvLine(line));
            }
        } catch (IOException e) {
            System.err.println("readCsv failed for " + filePath + ": " + e.getMessage());
        }
        return rows;
    }

    // rewrites entire CSV file
    public void rewriteCsv(String filePath, String[] headers, List<String[]> rows) {
        try {
            File file = new File(filePath);
            ensureParentDirs(file);
            try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
                pw.println(String.join(",", escapeRow(headers)));
                for (String[] row : rows) pw.println(String.join(",", escapeRow(row)));
            }
        } catch (IOException e) {
            System.err.println("rewriteCsv failed for " + filePath + ": " + e.getMessage());
        }
    }

    private void ensureParentDirs(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }

    // wraps fields with commas in quotes
    private String[] escapeRow(String[] fields) {
        String[] escaped = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            String f = fields[i] == null ? "" : fields[i];
            if (f.contains(",") || f.contains("\"") || f.contains("\n")) {
                f = "\"" + f.replace("\"", "\"\"") + "\"";
            }
            escaped[i] = f;
        }
        return escaped;
    }

    // parses CSV line with quoted fields
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); 
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    sb.append(c);
                }
            } else {
                if (c == '"') { 
                    inQuotes = true; 
                } else if (c == ',') { 
                    fields.add(sb.toString()); 
                    sb.setLength(0); 
                } else { 
                    sb.append(c); 
                }
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}
