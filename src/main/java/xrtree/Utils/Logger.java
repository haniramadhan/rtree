package xrtree.Utils;

import java.io.*;

public class Logger {
    private static String header;
    private static String logFileName;
    private static String currentLine;
    private static File logFile;
    private static FileOutputStream fos;
    private static BufferedWriter bw;

    public static void createHeaderFile(String headerFile, String headers){
        File header = new File(headerFile);
        try {
            fos = new FileOutputStream(header);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(headers);
            bw.newLine();
            bw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startLog(String logFileNameInput){
        logFileName = logFileNameInput;
        currentLine = "";

        logFile = new File(logFileNameInput);
        try {
            fos = new FileOutputStream(logFile);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void concatLine(String concatString){
        currentLine = currentLine+concatString;
    }

    public static void writeLine(){
        try {
            bw.write(currentLine);
            bw.newLine();
            bw.flush();
            currentLine = "";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void endLog(){
        try {
            bw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
