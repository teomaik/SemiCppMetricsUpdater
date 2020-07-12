package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public class Utilities {

    static StringBuilder builder = new StringBuilder();

    public static String getSystemSettings() {
        String s = "operating_system: " + System.getProperty("os.name") + ", "
                + "architecture: " + System.getProperty("os.arch") + ", "
                + "version: " + System.getProperty("os.version") + "\n"
                + "java_version: " + System.getProperty("java.version") + ", "
                + "java_classpath: " + System.getProperty("java.class.path") + "\n"
                + "home_dir: " + System.getProperty("user.home") + "\n"
                + "user_dir (working directory): " + System.getProperty("user.dir");
        return s;
    }

    public static void writeCSV(String pathname, String content, boolean append) {
        File file = new File(pathname);
        try {
            FileWriter writer = null;
            if (append) {
                writer = new FileWriter(file, true);
            } else {
                writer = new FileWriter(file);
            }
            BufferedWriter bf = new BufferedWriter(writer);
//            System.out.print("Writing csv file.... ");
            bf.write(content);
            bf.close();
            writer.close();
//            System.out.print("done!\n");
        } catch (IOException e) {
//            System.out.print("FAILED\n");
            e.printStackTrace();
        }
    }

    public static void writeCSV(String pathname, ArrayList<String> content, boolean append) {
        File file = new File(pathname);
        try {
            FileWriter writer = null;
            if (append) {
                writer = new FileWriter(file, true);
            } else {
                writer = new FileWriter(file);
            }
            BufferedWriter bf = new BufferedWriter(writer);
//            System.out.print("Writing csv file.... ");
            for (String line : content) {
                bf.write(line + "\n");
            }
            bf.close();
            writer.close();
//            System.out.print("done!\n");
        } catch (IOException e) {
//            System.out.print("FAILED\n");
            e.printStackTrace();
        }
    }

    public static void writeCSV(String pathname) {
        File file = new File(pathname);
        try {
            FileWriter writer = null;

            writer = new FileWriter(file);

            BufferedWriter bf = new BufferedWriter(writer);
//            System.out.print("Writing csv file.... ");
            bf.write(builder.toString());

            bf.close();
//            System.out.print("done!\n");
        } catch (IOException e) {
//            System.out.print("FAILED\n");
            e.printStackTrace();
        }
    }

    public static String getDuration(long startTime, long stopTime) {
        long duration = stopTime - startTime;
        long millis = duration % 1000;
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        long hours = (duration / (1000 * 60 * 60)) % 60;
        return String.format("%02dh:%02dm:%02ds:%dms", hours, minutes, seconds, millis);
    }

    public static void appendNewLine(String line) {
        builder.append(line).append("\n");
    }

    public static void append(String line) {
        builder.append(line);
    }

    @SuppressWarnings("resource")
    public static ArrayList<String> getFileContent(String pathname) {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(pathname));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return lines;
    }
}