
package parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Nikos
 */
public class fortranParserSemi {
    File file;
    String line;
    boolean f90;
    int countLOC=0;
    ArrayList<Integer> methodsLocStart;
    ArrayList<String> methodsName;
    private BufferedWriter writer;

    public fortranParserSemi(File file, boolean f90, ArrayList<Integer> methodsLocStart, ArrayList<String> methodsName) {
        this.file=file;
        this.f90=f90;
        this.methodsLocStart= methodsLocStart;
        this.methodsName= methodsName;
    }
    
    public void parse(){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            boolean methodDel= false;
            boolean first=false;
            ArrayList<String> methodParam=new ArrayList<>();
            String methodToPrint="";
            
            writer = new BufferedWriter(new FileWriter(file.getName()+"_parsed.txt", true));
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()){
                    if( !isCommentLine(line.trim()) ){
                        if(line.contains("!")){
                            String[] newLine= line.split("!");
                            this.line=newLine[0];
                        }
                        countLOC ++;
                        
                        // Check if is line of Method start
                        for(int i=0;i<methodsLocStart.size(); i++){
                            if(methodsLocStart.get(i)==(countLOC)){
                                if(f90){
                                    methodToPrint="Method:"+methodsName.get(i)+"(";
                                    String[] newLine= line.split(methodsName.get(i),2);
                                    line= newLine[1].trim().substring(1).trim();
                                }
                                else{
                                    methodToPrint="Method:"+methodsName.get(i)+"(";
                                    String[] newLine= line.split(methodsName.get(i),2);
                                    line= newLine[1].trim();
                                }
                                first=true;
                                methodDel=true;
                                break;
                            } 
                        }
                        
                        // Method Decleration
                        if(methodDel){
                            if(f90){
                                 if(line.trim().charAt(line.trim().length()-1)=='&') {
                                    addMethodParam(methodParam);
                                }
                                else{
                                    addMethodParam(methodParam);
                                    //System.out.println(methodToPrint.substring(0, methodToPrint.length()-1)+"();");
                                    writer.append(methodToPrint.substring(0, methodToPrint.length()-1)+"();"
                                            +System.lineSeparator());
                                    //ToDo
                                    // find type first
                                    //
                                    //for(String str: methodParam){
                                        //System.out.println("parameter#"+str+";");
                                    //}
                                    methodToPrint="";
                                    methodParam.clear();
                                    methodDel=false;
                                }
                            }
                            else{
                                if(line.contains("&") || first){
                                    addMethodParam(methodParam);
                                    first=false;
                                }
                                else {
                                    //System.out.println(methodToPrint.substring(0, methodToPrint.length()-1)+"();");
                                    writer.append(methodToPrint.substring(0, methodToPrint.length()-1)+"();"
                                            +System.lineSeparator());
                                    for(String str: methodParam){
                                        //ToDo
                                        // find type first
                                        //
                                        //System.out.println("parameter#"+str+";");
                                    }
                                    methodToPrint="";
                                    methodParam.clear();
                                    methodDel=false;
                                    usageForLine();
                                }
                            }
                        }
                        
                        //For everything use "usage:"
                        else {
                            usageForLine();
                        }
                        
                    }
                }
            }
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(cParserSemi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

     /**
     * It adds the parameters of the method
     * @param methodParam the array of parameters
     */
    private void addMethodParam(ArrayList<String> methodParam) {
        line=line.replace("&", "").trim();
        line=line.replace(")", "").trim();
        line=line.replace("(", "").trim();
        String[] var= line.split(",");
        for(String str: var){
            if(!str.trim().equals(""))
                methodParam.add(str.trim());
        }
    }
    
    /**
     * It writes the usages of parameters
     */
    private void usageForLine() throws IOException{
        replaceWithSpaces();
        String[] lineTable= line.trim().split(" ");
        for(String str: lineTable){
            if(!str.equals("")){
                if(!isNumeric(str)){
                    //System.out.println("Usage#"+str+"#"+countLOC+";");
                    writer.append("Usage#"+str+"#"+countLOC+";"
                                            +System.lineSeparator());
                }
            }
        }
    }

    /**
     * It deletes all non important
     */
    private void replaceWithSpaces(){
        line= line.replaceAll("\\+", " ");
        line= line.replaceAll("-", " ");
        line= line.replaceAll("\\(", " ");
        line= line.replaceAll("\\)", " ");
        line= line.replaceAll("\\*", " ");
        line= line.replaceAll("/", " ");
        line= line.replaceAll("%", " ");
        line= line.replaceAll("=", " ");
        line= line.replaceAll("&", " ");
        line= line.replaceAll("#", " ");
        line= line.replaceAll(":", " ");
        line= line.replaceAll(";", " ");
        line= line.replaceAll("\\[", " ");
        line= line.replaceAll("\\]", " ");
        line= line.replaceAll("\\{", " ");
        line= line.replaceAll("\\}", " ");
        line= line.replaceAll(",", " ");
        line= line.replaceAll(">", " ");
        line= line.replaceAll("<", " ");
        line= line.replaceAll("\"", " ");
        line= line.replaceAll("'", " ");
        line= line.replaceAll("\\|\\|", " ");
        line= line.replaceAll("\\?", " ");
    }
    
    /**
     * It returns if there is a comment
     * @param word word to analyze
     */
    private boolean isCommentLine(String word){
        if(word.charAt(0)=='#')
            return true;
        if(f90)
            return word.charAt(0) == '!';
        else
            return (word.charAt(0) == 'C' || word.charAt(0) == 'c' || word.charAt(0) == '!' || word.charAt(0) == '*');
    }
    
    /**
     * It returns if a string is a number
     * @param str string to check
     */
    public boolean isNumeric(final String str) {    //TODO 1.0D0 
        return (str.chars().allMatch(Character::isDigit) ||
                Pattern.matches("([0-9]*)\\.([0-9]*)", str) );
    }
}
