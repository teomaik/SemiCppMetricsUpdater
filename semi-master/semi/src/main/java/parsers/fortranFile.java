
package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nikos
 */
public class fortranFile extends CodeFile{

    ArrayList<String> methodsName =new ArrayList<>();
    ArrayList<Integer> methodsLocStart =new ArrayList<>();
    ArrayList<Integer> methodsLocStop =new ArrayList<>();
    ArrayList<Integer> methodsCCArray =new ArrayList<>();
    private boolean f90;
            
    public fortranFile(File file, boolean f90) {
        super(file);
        this.f90=f90;
    }

    @Override
    public void parse() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            fanOut=0;
            int countLOC=0;
            boolean checkForPreviousEnd= false;
            int checkForPreviousEndLine= 0;
            ArrayList<String> useFiles=new ArrayList<>();
            
            //System.out.println(file.getAbsolutePath()+"  "+file.getName());
            while ((line = br.readLine()) != null) {
                if (!line.trim().equals("")){
                    String[] lineTable= line.trim().split(" ");
                    if( !isCommentLine(lineTable[0]) ){
                        countLOC ++;
                    
                        // For fan-out
                        String[] use= lineTable[0].split(",");
                        if( use[0].equalsIgnoreCase("use") ){
                            if (line.contains("::")){       //<use,INTRINSIC :: name>
                                String[] useNameTemp= line.split("::");
                                String[] useName= useNameTemp[1].split(",");
                                if(!useFiles.contains(useName[0])){
                                    useFiles.add(useName[0]);
                                    fanOut ++;
                                }
                            }
                            else{   //<use name>
                                String[] useName= lineTable[1].split(",");
                                if(!useFiles.contains(useName[0])){
                                    useFiles.add(useName[0]);
                                    fanOut ++;
                                }
                            }
                        }
                        if(lineTable[0].equals("INCLUDE") && line.contains(".inc"))
                            fanOut ++;
                        
                        // For start count LOC in function/subroutine
                        if( lineTable[0].equalsIgnoreCase("function") || lineTable[0].equalsIgnoreCase("subroutine") ){
                            methodStartsHere(checkForPreviousEnd,checkForPreviousEndLine,lineTable,1,countLOC);
                        }
                        else if( !lineTable[0].equalsIgnoreCase("end") ){
                            int tempCom=10;
                            for(int i=0; i<lineTable.length; i++){
                                if(isCommentStarts(lineTable[i]))
                                    tempCom=i;
                            }
                            if(lineTable.length>2 && tempCom>1){
                                if(lineTable[1].equalsIgnoreCase("function") || lineTable[1].equalsIgnoreCase("subroutine") )
                                    methodStartsHere(checkForPreviousEnd,checkForPreviousEndLine,lineTable,2,countLOC);
                            }
                            if(lineTable.length>3 && tempCom>2){
                                if(lineTable[2].equalsIgnoreCase("function") || lineTable[2].equalsIgnoreCase("subroutine") )
                                    methodStartsHere(checkForPreviousEnd,checkForPreviousEndLine,lineTable,3,countLOC);
                            }
                            if(lineTable.length>4 && tempCom>3){
                                if(lineTable[3].equalsIgnoreCase("function") || lineTable[3].equalsIgnoreCase("subroutine") )
                                    methodStartsHere(checkForPreviousEnd,checkForPreviousEndLine,lineTable,4,countLOC);
                            }
                        }
                        
                        // For stop count LOC in function/subroutine
                        if( lineTable[0].equalsIgnoreCase("end") && lineTable.length>1 ){
                            if( lineTable[1].equalsIgnoreCase("function") || lineTable[1].equalsIgnoreCase("subroutine") ){
                                methodEndsHere(countLOC);
                            }
                        }
                        else if( lineTable[0].equalsIgnoreCase("endfunction") || lineTable[0].equalsIgnoreCase("endsubroutine")){
                            methodEndsHere(countLOC);
                        }
                        else if (lineTable[0].equalsIgnoreCase("end") && lineTable.length==1){
                            if(!checkForPreviousEnd){
                                checkForPreviousEnd= true;
                                checkForPreviousEndLine= countLOC;
                            }
                        }
                        if( checkForPreviousEndLine!= countLOC)
                            checkForPreviousEnd= false;
                        
                        
                        // For CC
                        if( lineTable[0].equalsIgnoreCase("if") || lineTable[0].equalsIgnoreCase("else")
                                || lineTable[0].equalsIgnoreCase("else if") || lineTable[0].equalsIgnoreCase("elseif")
                                || lineTable[0].equalsIgnoreCase("do")
                                || lineTable[0].equalsIgnoreCase("case")){
                            if( methodsLocStart.size() == methodsLocStop.size()+1){
                                int cc= methodsCCArray.get(methodsLocStart.size()-1);
                                methodsCCArray.set(methodsLocStart.size()-1, (cc+1) );
                            }
                            else if(methodsLocStart.size() == methodsLocStop.size()){
                                for(int i=methodsLocStop.size()-1; i>=0; i--){
                                    if(methodsLocStop.get(i)==0){
                                        int cc= methodsCCArray.get(i);
                                        methodsCCArray.set(i, (cc+1) );
                                        break;
                                    }
                                }
                            }
                        }
                        
                    }
                }
            }
            if(checkForPreviousEnd)
                methodEndsHere(checkForPreviousEndLine);
            
            //System.out.println("N= " +fanOut);
            for(int i=0; i<methodsName.size(); i++){
                //System.out.println("Method: "+methodsName.get(i)+" lines of code: "
                //        + (methodsLocStop.get(i)-methodsLocStart.get(i)-1) +" CC:"+ methodsCCArray.get(i));
                methodsLOC.put(methodsName.get(i), (methodsLocStop.get(i)-methodsLocStart.get(i)-1));
                methodsCC.put(methodsName.get(i), methodsCCArray.get(i));
            }
            calculateCohesion();
        } catch (IOException ex) {
            Logger.getLogger(fortranFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     /**
     * Its called to Start the method 
     * @param checkForPreviousEnd to check for previous methods ends
     * @param checkForPreviousEndLine the line of the previous method end
     * @param lineTable to take the name of the method
     * @param countLOC where method starts
     */
    private void methodStartsHere(boolean checkForPreviousEnd,int checkForPreviousEndLine,
                    String[] lineTable,int nameInt, int countLOC){
        if(checkForPreviousEnd){
            methodEndsHere(checkForPreviousEndLine);
        }
        if(methodsLocStop.size() != methodsLocStart.size()){
            methodsLocStop.add(0);
        }
        String[] methodName= lineTable[nameInt].split("\\(");
        methodsName.add(methodName[0]);
        methodsLocStart.add(countLOC);
        methodsCCArray.add(0);
    }
    
    /**
     * Its called to Stop the method
     * @param countLOC the line of code
     */
    private void methodEndsHere( int countLOC ){
        if( methodsLocStart.size() == methodsLocStop.size()+1){
            methodsLocStop.add(countLOC);
        }
        else if(methodsLocStart.size() == methodsLocStop.size()){
            for(int i=methodsLocStop.size()-1; i>=0; i--){
                if(methodsLocStop.get(i)==0){
                    methodsLocStop.set(i, countLOC);
                    break;
                }
            }
        }
    }
    
    /**
     * It returns if there is a comment
     * @param word word to analyze
     */
    private boolean isCommentLine(String word){
        if (word.charAt(0)== '#')
            return true;
        if(f90)
            return word.charAt(0) == '!';
        else
            return (word.charAt(0) == 'C' || word.charAt(0) == 'c' || word.charAt(0) == '!' || word.charAt(0) == '*');
    }
    private boolean isCommentStarts(String word){
        if(word.length()>0){
        if(f90)
            return word.contains("!");
        else
            return ( word.toLowerCase().charAt(0)=='c' || word.toLowerCase().charAt(word.length()-1)=='c'
                    || word.contains("!") || word.contains("*") );
        }
        else
            return false;
    }

    
    /**
     * Calculates cohesion by creating file for semi
     */
    @Override
    public void calculateCohesion() {
        fortranParserSemi fortranSemi= new fortranParserSemi(file, f90,methodsLocStart, methodsName);
        fortranSemi.parse();
    }
}
