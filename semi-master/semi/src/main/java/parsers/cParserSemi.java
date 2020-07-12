package parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Nikos
 */
public class cParserSemi {

    File file;
    boolean commentBlock = false;
    String line;
    int countLOC = 0;
    HashMap<String, Integer> methodsLocDecl;
    private BufferedWriter writer;

    public cParserSemi(File file, HashMap<String, Integer> methodsLocDecl) {
        this.file = file;
        this.methodsLocDecl = methodsLocDecl;
    }

    public void parse() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            boolean methodDel = false;
            ArrayList<String> methodParam = new ArrayList<>();
            String methodToPrint = "";

            writer = new BufferedWriter(new FileWriter(file.getName()+"_parsed.txt", true));
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("")) {
                    if (!isCommentLine(line.trim()) && !commentBlock) {
                        countLOC++;
                        line = line.trim().replace(" +", " ");

                        // Check if is line of Method start
                        for (String str : methodsLocDecl.keySet()) {
                            if (methodsLocDecl.get(str) == countLOC) {
                                if (str.trim().contains(" ")) {
                                    str = str.trim().replace(" +", " ");
                                    methodToPrint = "Method:" + str.split(" ")[(str.split(" ").length - 1)].trim() + "(";
                                } else {
                                    methodToPrint = "Method:" + str + "(";
                                }
                                line = line.replace(str, "").trim().substring(1).trim();
                                methodDel = true;
                                break;
                            }
                        }

                        //For everything else Use "usage:"
                        if (!methodDel) {
                            int possitionOfend = line.trim().indexOf(";");
                            if (possitionOfend < line.trim().indexOf("//")) {
                                if (possitionOfend == -1) {
                                    possitionOfend = 0;
                                }
                                line = line.substring(0, possitionOfend);
                            }
                            replaceWithSpaces();
                            String[] lineTable = line.trim().split(" ");
                            for (String str : lineTable) {
                                str= str.replaceAll("#", "");
                                if (!str.trim().equals("")) {
                                    if (!isNumeric(str)) {
                                        //System.out.println("Usage#" + str + "#" + countLOC + ";");
                                        writer.append("Usage#"+str+"#"+countLOC+";"+System.lineSeparator());
                                    }
                                }
                            }
                        }
                        //For Method declaration
                        else {
                            //Method declaration get parameters and types
                            if (line.length() > 1) {
                                line = line.trim().replaceAll(" +", " ");
                                if (line.charAt(0) != ')') {
                                    String[] var = line.split(",");
                                    for (String str : var) {
                                        String[] var1 = str.trim().split(" ");
                                        if (var1[0].equals("const")) {
                                            methodToPrint = methodToPrint + "" + var1[1].replace("*", "").replace("&", "") + ",";
                                        } else {
                                            methodToPrint = methodToPrint + "" + var1[0].replace("*", "").replace("&", "") + ",";
                                        }
                                        int position = 1;
                                        if (var1[var1.length - 1].replace("*", "").replace("&", "").equals("{")) {
                                            position++;
                                        }
                                        if (var1[var1.length - position].replace("*", "").replace("&", "").equals(")")) {
                                            position++;
                                        }
                                        methodParam.add(var1[var1.length - position].replace("*", "").replace("&", ""));
                                    }
                                }
                            }
                            //Method declaration finish
                            if (line.contains("{")) {
                                methodDel = false;
                                if (methodToPrint.contains(",")) {
                                    //System.out.println(methodToPrint.substring(0, methodToPrint.length() - 1) + ");");
                                    writer.append(methodToPrint.substring(0, methodToPrint.length() - 1)+");"
                                            +System.lineSeparator());
                                } else {
                                    //System.out.println(methodToPrint + ");");
                                    writer.append(methodToPrint+");"
                                            +System.lineSeparator());
                                }
                                for (String str : methodParam) {
                                    if (str.contains(")")) {
                                        str = str.replace(")", "");
                                    }
                                    if(!str.equals("")){
                                        if (str.charAt(str.length()-1)==';')
                                            str= str.substring(0, str.length() - 1);
                                        //System.out.println("parameter#" + str + ";");
                                        writer.append("parameter#"+str+";"
                                                +System.lineSeparator());
                                    }
                                }
                                methodToPrint = "";
                                methodParam.clear();
                            }
                        }
                    } else if (commentBlock) {          //TODO test if ->  */ <code>
                        stopCommentBlock(line);
                    }

                }
            }
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(cParserSemi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    /**
     * It deletes all non important
     */
    private void replaceWithSpaces() {
        line = line.replaceAll("\\+", " ");
        line = line.replaceAll("-", " ");
        line = line.replaceAll("\\(", " ");
        line = line.replaceAll("\\)", " ");
        line = line.replaceAll("\\*", " ");
        line = line.replaceAll("/", " ");
        line = line.replaceAll("%", " ");
        line = line.replaceAll("=", " ");
        line = line.replaceAll("&", " ");
        line = line.replaceAll(":", " ");
        line = line.replaceAll(";", " ");
        line = line.replaceAll("\\[", " ");
        line = line.replaceAll("\\]", " ");
        line = line.replaceAll("\\{", " ");
        line = line.replaceAll("\\}", " ");
        line = line.replaceAll(",", " ");
        line = line.replaceAll(">", " ");
        line = line.replaceAll("<", " ");
        line = line.replaceAll("\"", " ");
        line = line.replaceAll("'", " ");
        line = line.replaceAll("^", " ");
        line = line.replaceAll("!", " ");
        line = line.replaceAll("\\?", " ");
        line = line.replaceAll("\\|\\|", " ");
        line = line.replaceAll("\\|", " ");
    }

    /**
     * It returns if there is a comment
     *
     * @param word string to analyze
     */
    private boolean isCommentLine(String word) {
        if (word.length() > 1) {
            if (word.charAt(0) == '#' || word.charAt(1) == '#') {
                if (word.contains("include")) {
                    countLOC++;
                }
                return true;
            }
            if (word.contains("printf(")) {
                countLOC++;
                return true;
            }
            if (word.contains("/*")) {//.substring(0, 2).equals("/*") ){
                String lastTwo = word.substring(word.length() - 2);
                if (lastTwo.equals("*/")) {
                    return true;
                } else if (word.contains("*//*")) {
                    commentBlock = true;
                    return true;
                } else if (word.contains("*/")) {
                    this.line= word.replaceAll("\\/\\*.*?\\*\\/", "");
                    line = line.trim().replace(" +", " ");
                    return false;
                }
                commentBlock = true;
                return true;
            }
            return (word.substring(0, 2).equals("//"));
        }
        return false;
    }

    /**
     * It returns if comment block stops
     *
     * @param line string to analyze
     */
    private void stopCommentBlock(String line) {
        if (line.contains("*/")) {
            commentBlock = false;
            if (line.contains("*//*")) {
                commentBlock = true;
            }
        }
    }

    /**
     * It returns if a string is a number
     *
     * @param str string to check
     */
    public boolean isNumeric(String str) {
        return (//str.chars().allMatch(Character::isDigit) ||
                //Pattern.matches("([0-9]*)\\.([0-9]*)", str) ||
                Pattern.matches("([0-9]*)", str.charAt(0) + ""));
    }
}
