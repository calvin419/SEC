import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.opencsv.exceptions.CsvValidationException;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.io.FileUtils;


import com.opencsv.CSVReader;

public class SEC {

    public static final String csvUrl = "https://www.sec.gov/dera/data/Public-EDGAR-log-file-data/2017/Qtr2/log20170630.zip";
    public static final String zipName = "secCsv.zip";
    public static List<String> csvFileNames = new ArrayList<>();
    public static final int IP = 0;
    public static final int CODE = 1;
    public static final int SIZE = 2;

    public static void downloadFile(String destPath) throws IOException {
        System.out.println("Downloading File...\n");
        FileUtils.copyURLToFile(new URL(csvUrl), new File(destPath));
    }
    public static void unzipFile() throws IOException {
        System.out.println("Unzipping File...\n");
        byte[] buffer = new byte[1024];
        ZipInputStream is = new ZipInputStream(new FileInputStream(zipName));
        ZipEntry entry = is.getNextEntry();
        while(entry != null) {
            csvFileNames.add(entry.getName());
            File file = new File(".", entry.getName());
            FileOutputStream os = new FileOutputStream(file);
            int bytes;
            while ((bytes = is.read(buffer)) >= 0) {
                os.write(buffer, 0, bytes);
            }
            os.close();
            entry = is.getNextEntry();
        }
    }

    private static void printInfo(String csvName) throws IOException {
        System.out.println("Calculating Statistics for " + csvName + "...\n");
        Set<String> seenIPs = new HashSet<>();
        HashMap<String, Integer> codes = new HashMap<>();
        double bytesTransferred = 0;
        String[] csvEntry;
        CsvParserSettings settings = new CsvParserSettings();
        settings.selectFields("ip", "code", "size");
        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(new FileReader(csvName));
        // Remove first line that is just header
        csvEntry = parser.parseNext();
        while((csvEntry = parser.parseNext()) != null) {
                seenIPs.add(csvEntry[IP]);
                Integer occurrences = codes.get(csvEntry[CODE]);
                codes.put(csvEntry[CODE], (occurrences == null ? 1 : occurrences + 1));
                bytesTransferred += Double.parseDouble(csvEntry[SIZE]);
            }
        System.out.println(csvName + ": \n");
        System.out.println("Number of unique IPs: " + seenIPs.size());
        System.out.println("\nOccurrences of each code: \n");
        for (String code : codes.keySet()) {
            System.out.println((code == null ? "No Code" : code) + ": " + codes.get(code));
        }
        System.out.println("\nTotal Bytes Transferred: " + bytesTransferred + " bytes");
    }

    public static void main(String[] args) throws IOException {
        downloadFile(zipName);
        unzipFile();
        for(String csv : csvFileNames) {
            if(csv.endsWith(".csv")) printInfo(csv);
        }
    }
}
