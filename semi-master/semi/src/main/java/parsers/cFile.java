
package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nikos
 */
public class cFile extends CodeFile{

    boolean commentBlock=false;
    String line;
    HashMap<String, Integer> methodsLocDecl=new HashMap<>();
    
    public cFile(File file) {
        super(file);
    }

    @Override
    public void parse() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            fanOut=0;
            int countLOC=0;
            boolean lineContinuous=false;
            int lineContinuousParOpen=0;
            int lineContinuousParClose=0;
            ArrayList<String> includeFiles=new ArrayList<>();
            
            boolean methodStarted=false;
            int methodCC=0;
            int lineMethodStarts=-1;
            int lineMethodDecl=-1;
            int methodBrackOpen=0;
            int methodBrackClose=0;
            String methodName="";
            
            System.out.println(file.getAbsolutePath()+"  "+file.getName());
            String linePre="asd";
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("")){
                    line= line.trim().replace(" +", " ");
                    
                    String[] lineTable= line.trim().split(" ");
                    if( !isCommentLine(line.trim()) && !commentBlock ){
                        countLOC ++;
                    
                        // For fan-out
                        if( lineTable[0].equals("#include") ){
                            String temp=line.replace("#include", "");
                            if(temp.trim().charAt(0)=='"'){
                                includeFiles.add(temp.trim());
                                fanOut ++;
                            }
                        }
                        else if( lineTable[0].equals("#") && lineTable[1].equals("include") ){
                            String temp=line.replace("# include", "");
                            if(temp.trim().charAt(0)=='"'){
                                includeFiles.add(temp.trim());
                                fanOut ++;
                            }
                        }
                        
                        // For start count LOC in function/subroutine
                        if(lineContinuous && (lineContinuousParOpen == lineContinuousParClose) && !line.contains("{")){
                            lineContinuous=false;
                        }
                        
                        if(!line.contains(";") && line.contains("(") && !lineContinuous && !methodStarted 
                                && !lineTable[0].contains("#") && linePre.charAt(linePre.length()-1)!='\\' 
                                && lineTable[0].charAt(0)!='{'){
                            //System.out.println(line);
                            String[] methodDecl=line.split("\\(");
                            methodName= methodDecl[0];
                            lineMethodDecl=countLOC;
                            for (int i = 0; i < line.length(); i++) {
                                if (line.charAt(i) == '(')
                                    lineContinuousParOpen++;
                                if (line.charAt(i) == ')')
                                    lineContinuousParClose++;
                            }
                            if(lineContinuousParOpen == lineContinuousParClose){
                                if(line.contains("{")){
                                    lineMethodStarts=countLOC;
                                    methodsLocDecl.put(methodName,lineMethodDecl);
                                    methodStarted=true;
                                    lineContinuous=false;
                                }
                                else{
                                    lineContinuous=true;
                                }
                            }
                            else{
                                lineContinuous=true;
                            }
                        }
                        else if(lineContinuous){
                            for (int i = 0; i < line.length(); i++) {
                                if (line.charAt(i) == '(')
                                    lineContinuousParOpen++;
                                if (line.charAt(i) == ')')
                                    lineContinuousParClose++;
                            }
                            if(lineContinuousParOpen == lineContinuousParClose){
                                if(line.contains("{")){
                                    lineMethodStarts=countLOC;
                                    methodsLocDecl.put(methodName,lineMethodDecl);
                                    methodStarted=true;
                                    lineContinuous=false;
                                }
                                else{
                                    lineContinuous=true;
                                }
                            }
                            else{
                                lineContinuous=true;
                            }
                        }
                        
                        // For stop count LOC in function/subroutine
                        if(methodStarted){
                            lineContinuousParOpen=0;
                            lineContinuousParClose=0;
                            for (int i = 0; i < line.length(); i++) {
                                if(line.charAt(i) == '{')
                                    methodBrackOpen++;
                                if(line.charAt(i) == '}')
                                    methodBrackClose++;
                            }
                            if(methodBrackOpen == methodBrackClose){
                                methodsLOC.put(methodName,(countLOC-lineMethodStarts -1));
                                methodsCC.put(methodName, methodCC);
                                methodStarted=false;
                                methodBrackOpen=0;
                                methodBrackClose=0;
                                methodCC=0;
                            }
                            else{
                                //calculate cc
                                if(line.toLowerCase().contains("if") || line.toLowerCase().contains("else")
                                        || line.toLowerCase().contains("else if") || line.toLowerCase().contains("for")
                                        || line.toLowerCase().contains("while") 
                                        || (line.toLowerCase().contains("case") && line.contains(":"))){
                                    methodCC++;
                                }
                            }
                        }
                        if(line.contains("/*") && !line.contains("*/")){
                            commentBlock=true;
                        }
                    }
                    else if(commentBlock){          //TODO test if ->  */ <code>
                        stopCommentBlock(line);
                    }
                    
                linePre=line;
                }
            }
            
            //clear wrong methods
            HashMap<String,Integer> temp= new HashMap<>();
            for(String str : methodsLOC.keySet()) {
                if(methodsLOC.get(str) < 0){
                    methodsLocDecl.remove(str);
                    temp.put(str, methodsLOC.get(str));
                }
            }
            temp.keySet().forEach((str) -> {
                methodsLOC.remove(str);
            });
            
            /*Print methods
            System.out.println("N= " +fanOut);
            for(String str: methodsLOC.keySet()){
                System.out.println("Method: "+str+"  LOC: "+  methodsLOC.get(str)+" CC:"+ methodsCC.get(str));
            }*/
            calculateCohesion();
        } catch (IOException ex) {
            Logger.getLogger(fortranFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * It returns if there is a comment
     * @param word string to analyze
     */
    private boolean isCommentLine(String word){
        if(word.length()>1){
            if(word.charAt(0)=='#'){
                if(!word.contains("include"))
                    return true;
            }
            if (word.substring(0, 2).equals("/*") ){
                String lastTwo = word.substring(word.length() - 2);
                if(lastTwo.equals("*/"))
                    return true;
                else if (word.contains("*//*")){
                    commentBlock=true;
                    return true;
                }
                else if (word.contains("*/")){
                    String[] newLine= word.split("\\*/");
                    this.line=newLine[1];
                    return false;
                }
                commentBlock=true;
                return true;
            }
            return (word.substring(0, 2).equals("//"));
        }
        return false;
    }
    
    /**
     * It returns if comment block stops
     * @param line string to analyze
     */
    private void stopCommentBlock(String line){
        if(line.contains("*/")){
            commentBlock=false;
            if (line.contains("*//*"))
                commentBlock=true;
        }
    }
    
    /**
     * Calculates cohesion by creating file for semi
     */
    @Override
    public void calculateCohesion() {
        cParserSemi cSemi= new cParserSemi(file,methodsLocDecl);
        cSemi.parse();
    }
}