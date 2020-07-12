package splitlongmethod;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
//import opportunities.Clazz;

public class SplitLongMethod {

    private String javaclass;
    private String javafile;
    private String fileLanguage;

    public SplitLongMethod(String javaclass, String javafile) {
        this.javaclass = javaclass;
        this.javafile = javafile;
        //Find Language of File

        String[] str = javafile.split("\\.");
        if(str[str.length - 1].equalsIgnoreCase("java")){ 
        	fileLanguage = "java";
        }else if (str[str.length - 1].equalsIgnoreCase("f") || str[str.length - 1].equalsIgnoreCase("f77")
                || str[str.length - 1].equalsIgnoreCase("for") || str[str.length - 1].equalsIgnoreCase("fpp")
                || str[str.length - 1].equalsIgnoreCase("ftn")){
            fileLanguage = "f77";
        }else if( str[str.length - 1].equalsIgnoreCase("F90") ) {
            fileLanguage = "f90";
        } else if (str[str.length - 1].equalsIgnoreCase("c") || str[str.length - 1].equalsIgnoreCase("cpp") 
                || str[str.length - 1].equalsIgnoreCase("h") || str[str.length - 1].equalsIgnoreCase("hpp")) {
            fileLanguage = "c";
        }
    }
    
    //TODO return a Class to the Analyser
    public JavaClass parse() {
        final ArrayList<ArrayList<Integer>> invalid_lines = new ArrayList<ArrayList<Integer>>();
        final ArrayList<Integer> possible_invalid_bracket_close = new ArrayList<Integer>();
        BufferedReader br = null;

        JavaClass myclass = new JavaClass(this.javaclass);

        try {
            br = new BufferedReader(new FileReader(javafile));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int line_num = 1;

            boolean commentBlock=false;
            int countStart=0;   //count of comment block starting
            int countStop=0;    //count of comment block finishing
            
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());

                if (line.contains("}")) {
                    String parts[] = line.split("}");
                    if (parts.length > 1) {
                        if (!parts[1].trim().equals("")) {
                            //System.out.println("line: " + line_num + " is of size: " + line.length() + " line: " + line + " parts: " + parts.length);
                            possible_invalid_bracket_close.add(line_num);
                        }
                    }

                }
                
                //find Invalid lines of emply or comment lines
                // For C and C++
                if (fileLanguage.equals("c")) {
                    if(line.trim().startsWith("//") || line.trim().startsWith("#") 
                            || line.trim().startsWith("/*") || line.trim().equals("") || commentBlock){
                        //count block of comments starting and finishing in this line
                        countStart += line.length() - line.replace("/*", "").length();
                        countStop += line.length() - line.replace("*/", "").length();
                        if(countStart > countStop){
                            commentBlock=true;
                        }
                        if(countStart == countStop){
                            commentBlock=false;
                        }
                        
                        //invalid Line
                        invalidLinesAdd(invalid_lines, line_num);
                    }
                }
                // For Fortran
                else if(fileLanguage.equals("f77") || fileLanguage.equals("f90")){
                    if( line.trim().startsWith("#") || line.trim().equals("")
                            || (fileLanguage.equals("f77") && line.trim().startsWith("C") )
                            || (fileLanguage.equals("f90") && line.trim().startsWith("!") ) ){
                        //invalid Line
                        invalidLinesAdd(invalid_lines, line_num);
                    }
                }else if(fileLanguage.equals("java")) {
                	if (line.trim().endsWith(";") || line.trim().endsWith("}") || line.trim().endsWith("{") || line.trim().contains("{") && (line.trim().contains("//") && (line.trim().indexOf(";") < (line.trim().indexOf("//")))) || (line.trim().contains(";") && (line.trim().contains("//") && (line.trim().indexOf(";") < (line.trim().indexOf("//"))))) || (line.trim().endsWith(":") && ((line.trim().toLowerCase().contains("case")) || (line.trim().toLowerCase().contains("default"))))) {
                		//System.out.println(line_num + " is a valid line");
                	} else {
                		boolean found = false;
                		for (int i = 0; i < invalid_lines.size(); i++) {
                			ArrayList<Integer> invalid_lines_cluster = invalid_lines.get(i);
                			for (int j = 0; j < invalid_lines_cluster.size(); j++) {
                				if (invalid_lines_cluster.get(j) == (line_num - 1)) {
                					invalid_lines_cluster.add(line_num);
                					found = true;
                					break;
                				}
                			}
                			if (found == true) {
                				break;
                			}
                		}
                		if (found == false) {
                			ArrayList<Integer> new_cluster = new ArrayList<Integer>();
                			new_cluster.add(line_num);
                			invalid_lines.add(new_cluster);
                		}
                	}                	
                }
                
                line = br.readLine();
                line_num++;
            }

            // adding first valid line in the invalid lines clusters
            for (int i = 0; i < invalid_lines.size(); i++) {
                ArrayList<Integer> invalid_lines_cluster = invalid_lines.get(i);
                int first_valid_line = invalid_lines_cluster.get(invalid_lines_cluster.size() - 1) + 1;
                invalid_lines_cluster.add(first_valid_line);
            }
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        } 

        //TODO check the following methods
        myclass.cleanUpMethods();
        myclass.printMethods();
        //TODO return a Class to the JavaClass
        myclass.filepath = javafile;
        myclass.CalculateMethodsMetrics(invalid_lines);
        //test metrics
        for(int i=0; i< myclass.getMethods().size(); i++){
            System.out.println("NEW MEthod in SplitLong Method: " + myclass.getMethods().get(i).getName());
            myclass.getMethods().get(i).printMetrics();
        }
        myclass.setInvalid_lines(invalid_lines);
        myclass.setPossible_invalid_bracket_close(possible_invalid_bracket_close);
        return myclass;
    }

	private void invalidLinesAdd(final ArrayList<ArrayList<Integer>> invalid_lines, int line_num) {
	    boolean found = false;
	    for (int i = 0; i < invalid_lines.size(); i++) {
	        ArrayList<Integer> invalid_lines_cluster = invalid_lines.get(i);
	        for (int j = 0; j < invalid_lines_cluster.size(); j++) {
	            if (invalid_lines_cluster.get(j) == (line_num - 1)) {
	                invalid_lines_cluster.add(line_num);
	                found = true;
	                break;
	            }
	        }
	        if (found == true) {
	            break;
	        }
	    }
	    if (found == false) {
	        ArrayList<Integer> new_cluster = new ArrayList<Integer>();
	        new_cluster.add(line_num);
	        invalid_lines.add(new_cluster);
	    }
	}
}
