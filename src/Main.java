/* Created By: Aditya Sharma */
/* Last Modified By: Aditya Sharma */
/* Last Modified On: 11/08/2024 */


import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static int missingCounter = 0;
    public static void main(String[] args) {
        String[] files = {"A", "D", "H", "M", "P", "S", "W"};

        for (String file : files)
        {
            String pdfFilePath = file+".pdf";
            String csvFilePath = file+".csv";
            String textFilePath = file+".txt";

            try {
                String pdfContent = extractTextFromPDF(pdfFilePath);
                processContentAndCreateCSV(pdfContent, csvFilePath, textFilePath);
                System.out.println("CSV file created successfully!");
            } catch (IOException | TikaException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Missing: " + missingCounter);
    }

    private static String extractTextFromPDF(String pdfFilePath) throws IOException, TikaException {
        Tika tika = new Tika();
        tika.setMaxStringLength(-1);
        return tika.parseToString(new File(pdfFilePath));
    }

    private static void processContentAndCreateCSV(String content, String csvFilePath, String textFilePath) throws IOException {
        Pattern pattern = Pattern.compile("([A-Za-z\\.\\s0-9()'’,-]+):([A-Za-z0-9\\s;\\.,()'’]*)(\\[*[A-Za-z\\s\\.0-9(),\\-]*\\]*)([^A-Za-z\\[\\]]+)");
        Matcher matcher = pattern.matcher(content);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {

            String textFile = Files.readString(Paths.get(textFilePath));
            // Write CSV header
            writer.write("English Phrase,Meanings,Acts,Hindi Translation\n");

            Pattern hindiPattern = Pattern.compile("[\\u0900-\\u097F]+[\\s\\u0900-\\u097F]*");

            while (matcher.find()) {
                String englishPhrase = matcher.group(1).trim().replaceAll("\\s+", " ");
                String meanings = matcher.group(2).trim().replaceAll("\\s+", " ");
                String acts = matcher.group(3).trim().replace("[", "").replace("]", "").replaceAll("\\s+", " ");
                String hindiTranslation = matcher.group(4).trim().replaceAll("\\s+", " ");

                // Escape commas in the fields
                englishPhrase = escapeCSV(englishPhrase);
                meanings = escapeCSV(meanings);
                acts = escapeCSV(acts);
                hindiTranslation = escapeCSV(hindiTranslation);

                if(englishPhrase.isEmpty() || hindiTranslation.isEmpty())
                {
                    // Some Regex or parser problem
                    missingCounter++;
                    continue;
                }

                if(hindiTranslation.contains("�"))
                {
                    int startIndex = textFile.indexOf(englishPhrase+" : ") + englishPhrase.length()+3;
                    if(startIndex == -1)
                    {
                        System.out.println("Not Found: " + englishPhrase);
                        missingCounter++;
                    }
                    else
                    {
                        int endIndex =  textFile.indexOf(":", startIndex);
                        if(endIndex == -1) continue;
                        String replacement = textFile.substring(startIndex, endIndex);

                        Matcher hindiMatcher = hindiPattern.matcher(replacement);
                        hindiTranslation = hindiMatcher.find() ? hindiMatcher.group().trim().replaceAll("\\s+", " ") : "";
                        if (hindiTranslation.isEmpty())
                        {
                            missingCounter++;
                            continue;
                        }
                    }
                }

                // Write the CSV line
                writer.write(String.format("%s,%s,%s,%s\n", englishPhrase, meanings, acts, hindiTranslation));
            }
        }
    }

    private static String escapeCSV(String field) {
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            field = field.replace("\"", "\"\"");
            field = "\"" + field + "\"";
        }
        return field;
    }
}
