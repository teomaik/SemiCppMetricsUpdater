package splitlongmethod;

import clustering.cluster.OpportunityList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
//import opportunities.Clazz;

public class JavaClass {

//    ArrayList<Method> methods = new ArrayList<Method>();
    OppMethodList methods = new OppMethodList();
    
    String name;
    ArrayList<Integer> lines_for_else = new ArrayList<Integer>();
    public String filepath;

    public JavaClass(String fname) {
        System.out.println("Parsing fname: " + fname);
        String[] temp = fname.split("\\\\");
        name = temp[temp.length - 1];
        
        System.out.println("***JavaClass() FileName: "+name);
        if(name.contains(".java_parsed.txt")) {
        	name = name.replace(".java_parsed.txt", "");
        }else if(name.contains(".cpp_parsed.txt")) {
        	System.out.println("***Found cpp");
        	name = name.replace(".cpp_parsed.txt", "");
        }else if(name.contains(".c_parsed.txt")) {
        	System.out.println("***Found c");
        	name = name.replace(".c_parsed.txt", "");
        }else {
        	System.out.println("***Found nothing");
        }

        BufferedReader br = null;
        String line = "";
        String new_line = "";

        try {
            br = new BufferedReader(new FileReader(fname));
        }catch(Exception e) {
        	System.out.println("***File name not found: "+fname);
        	int f=0;
        	f = 3/f;
        }
        try {
            //br = new BufferedReader(new FileReader(fname));
            line = br.readLine();
            while ((new_line = br.readLine()) != null) {
                line = line + new_line;
            }
            String[] tokens = line.split("Method:");
            String[] attributes = tokens[0].split(";");

            for (int i = 1; i < tokens.length; i++) {

                String[] method_data = tokens[i].split(";");
                //TODO add the attributes in the methods name
                Method m = new Method(method_data[0]);
                m.addVariables(new Variables("{", "syntax"));
                m.addVariables(new Variables("}", "syntax"));
                m.addVariables(new Variables("IF", "syntax"));
                m.addVariables(new Variables("SWITCH", "syntax"));
                m.addVariables(new Variables("ELSE", "syntax"));
                m.addVariables(new Variables("BREAK", "syntax"));
                m.addVariables(new Variables("BREAK_FINAL", "syntax"));
                m.addVariables(new Variables("null", "syntax"));
                m.addVariables(new Variables("this", "attribute"));

                for (int j = 0; j < attributes.length; j++) {
                    String[] attributes_data = attributes[j].split("#");
                    if (attributes_data[0].equals("Declaration")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        Variables v = new Variables(attributes_data[1].trim(), "attribute");
                        m.addVariables(v);
                    }
                }

                for (int j = 1; j < method_data.length; j++) {

                    String[] attributes_data = method_data[j].split("#");

					//if (attributes_data[1].equals("76")) {
                    //	System.out.println("aaaaaaaaaaaaaaa");
                    //}
                    if (attributes_data[0].equals("parameter")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        Variables v = new Variables(attributes_data[1].trim(), "parameter");
                        m.addVariables(v);
                    }
                    if (attributes_data[0].equals("Usage")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        m.addToVariable(attributes_data[1].trim(), attributes_data[2], "Usage", "not_assign");
                    }
                    if (attributes_data[0].equals("Invocation")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        m.addToVariable(attributes_data[1].trim(), attributes_data[2], "Invocation", "not_assign");
                    }
                    if (attributes_data[0].equals("Usage-IF")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        m.addToVariable(attributes_data[1].trim(), attributes_data[2], "Usage", "not_assign");
                        lines_for_else.add(Integer.parseInt(attributes_data[2]));
                    }
                    if (attributes_data[0].equals("Usage-ASSIGN")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        m.addToVariable(attributes_data[1].trim(), attributes_data[2], "Usage", "assign");
                        lines_for_else.add(Integer.parseInt(attributes_data[2]));
                    }
                    if (attributes_data[0].equals("Invocation-IF")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        m.addToVariable(attributes_data[1].trim(), attributes_data[2], "Invocation", "not_assign");
                        lines_for_else.add(Integer.parseInt(attributes_data[2]));
                    }
                    if (attributes_data[0].equals("Declaration")) {
                        attributes_data[1] = attributes_data[1].replace("'", "");
                        Variables v = new Variables(attributes_data[1].trim(), "local_variable");
                        v.addLines(Integer.parseInt(attributes_data[2]));
                        m.addVariables(v);
                    }
                    if (attributes_data[0].equals("BEGIN_IF") || attributes_data[0].equals("BEGIN_ELSE") || attributes_data[0].equals("BEGIN_CONDITIONAL") || attributes_data[0].equals("BEGIN_CASE") || attributes_data[0].equals("BEGIN_DO") || attributes_data[0].equals("BEGIN_FOR") || attributes_data[0].equals("BEGIN_WHILE") || attributes_data[0].equals("BEGIN_TRY") || attributes_data[0].equals("BEGIN_CATCH")) {
                        m.addToVariable("{", attributes_data[1], "Syntax", "not_assign");
                    }
                    if (attributes_data[0].equals("END_IF") || attributes_data[0].equals("BEGIN_ELSE") || attributes_data[0].equals("END_CONDITIONAL") || attributes_data[0].equals("END_SWITCH") || attributes_data[0].equals("END_CASE") || attributes_data[0].equals("END_DO") || attributes_data[0].equals("END_FOR") || attributes_data[0].equals("END_WHILE") || attributes_data[0].equals("END_TRY") || attributes_data[0].equals("END_CATCH")) {
                        m.addToVariable("}", attributes_data[1], "Syntax", "not_assign");
                    }

                    if (attributes_data[0].equals("BEGIN_SWITCH")) {
                        m.addToVariable("SWITCH", attributes_data[1], "Syntax", "not_assign");
                    }

                    if (attributes_data[0].equals("BEGIN_CASE")) {
                        if (m.hasSwitchInLine((Integer.parseInt(attributes_data[1]) - 1))) {
                            m.addToVariable("IF", attributes_data[1], "Syntax", "not_assign");
                        } else {
                            m.addToVariable("}", attributes_data[1], "Syntax", "not_assign");
                            m.addToVariable("ELSE", attributes_data[1], "Syntax", "not_assign");
                            m.addToVariable("BREAK", attributes_data[1], "Syntax", "not_assign");
                        }
                    }

                    if (attributes_data[0].equals("BEGIN_IF") || attributes_data[0].equals("BEGIN_TRY") || attributes_data[0].equals("BEGIN_CONDITIONAL")) {
                        m.addToVariable("IF", attributes_data[1], "Syntax", "not_assign");
                    }
                    if (attributes_data[0].equals("BEGIN_ELSE") || attributes_data[0].equals("BEGIN_CATCH")) {
                        m.addToVariable("ELSE", attributes_data[1], "Syntax", "not_assign");
                        m.addToVariable("BREAK", attributes_data[1], "Syntax", "not_assign");
                    }
                    if (attributes_data[0].equals("END_IF") || attributes_data[0].equals("END_TRY") || attributes_data[0].equals("END_CONDITIONAL") || attributes_data[0].equals("END_SWITCH")) {
                        m.addToVariable("BREAK", attributes_data[1], "Syntax", "not_assign");
                        m.addToVariable("BREAK_FINAL", attributes_data[1], "Syntax", "not_assign");
                    }
                }

				// this for statement is used for parsing again the list of method data for actions that
                // need to check the use of a variable in the specific line before printing (e.g. return x).  
                for (int j = 1; j < method_data.length; j++) {

					//System.out.println("line: " + method_data);
                    String[] attributes_data = method_data[j].split("#");
                    if (attributes_data[1].equals("null")) {
                        if (m.usesVariableInLine(Integer.parseInt(attributes_data[2])) == false) {
                            m.addToVariable("null", attributes_data[2], "Syntax", "not_assign");
                        }
                    }
                }
                System.out.println("NEW adding method : " + m.name);
                methods.add(m);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initializeGuiComponents();//new
    }

    public void printMethods() {
        for (int i = 0; i < methods.size(); i++) {
            System.out.println("\n\tHandling Method: " + methods.get(i).getName());
            methods.get(i).printVariables();
            System.out.println("-----------------------------------------------");
        }
    }

    public String getName() {
        return name;
    }

    public void CalculateMethodsMetrics(ArrayList<ArrayList<Integer>> invalid_lines) {
       
            System.out.println("##NEW CalculateMethodsMetrics Methods size : " + methods.size() );
        for (int j = 0; j < methods.size(); j++) {
            //Method method = new Method(methods.get(j).getName());
            System.out.println("##NEW Calculating metrics for : " + methods.get(j).getName());
//            method.setOriginalMetrics(methods.get(j).calculateMethodMetrics(invalid_lines, lines_for_else));
            methods.get(j).setOriginalMetrics(methods.get(j).calculateMethodMetrics(invalid_lines, lines_for_else));

            //this.addMethod(method); //new
        }
    }
 
    public void identifyExtractMethodOpportunities(ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> possible_invalid_bracket_close) {
        System.out.println("NEW JavaClass : identifyExtractMethodOpportunities [methods size : " + methods.size() + "]");
        //TODO pass the method name as a parameter
        //TODO create clusters only for this method
        for (int j = 0; j < methods.size(); j++) {
            System.out.println("\n\tNEW Handling Method: " + methods.get(j).getName());
//            utils.Utilities.appendNewLine("\n\tHandling Method: " + methods.get(j).getName());
            //Cluster c = new Cluster(methods.get(j).getMethodStart(invalid_lines), methods.get(j).getMethodEnd());
            //System.out.println("\t\tMetrics:");
            //c.print(methods.get(j).getVariableList(), invalid_lines, possible_invalid_bracket_close, "noparams", "original");
            //TODO SUPER apply algortihm from here
            OpportunityList opportunities = methods.get(j).calculateAllClusters(methods.get(j).getMethodStart(invalid_lines), invalid_lines, possible_invalid_bracket_close, lines_for_else);
//			System.out.println("-----------------------------------------------");
            utils.Utilities.appendNewLine("-----------------------------------------------");
        }
    }

    public void identifyExtractMethodOpportunitiesForOneMethod(ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> possible_invalid_bracket_close, String method_name) {
        System.out.println("NEW JavaClass : identifyExtractMethodOpportunities [methods size : " + methods.size() + "] selected method = " + method_name);
        Method selected_method = this.methods.getMethodByName(method_name);
        if (selected_method != null) {
            System.out.println("Method " + method_name + " succesfully retrieved!");
        } else {
            System.out.println("Error on retrieving " + method_name + " method.");
        }

        OpportunityList opportunities = selected_method.calculateAllClusters(selected_method.getMethodStart(invalid_lines), invalid_lines, possible_invalid_bracket_close, lines_for_else);
//			System.out.println("-----------------------------------------------");
        
        opportunities.calculateBenefits();
        opportunities.addBenefitsToList();
        selected_method.setOppList(opportunities);
        selected_method.been_analysed = true; //set been_analysed to TRUE in order to avoid re run in the future.
        
        System.out.println("Opportunities:\n" + selected_method.getOpportunityList().getCsvOutput());
        utils.Utilities.appendNewLine("-----------------------------------------------");
//        }
    }
    
    public void cleanUpMethods() {
        for (int i = 0; i < methods.size(); i++) {
            methods.get(i).cleanUp();
            methods.get(i).sortVariables();
        }
    }
    
//    private final String name;
//    private OppMethodList methods;
    private JavaClass javaClass;
    
    //gui components
    private javax.swing.JPanel jPanelMethods;
    private javax.swing.JTabbedPane jTabbedPaneMethods;
    private ArrayList<ArrayList<Integer>> invalid_lines;
    private ArrayList<Integer> possible_invalid_bracket_close;
    
    public String getFilePath () {
        return this.filepath;
    }
    
//    public Clazz (String name){
//        this.name = name;
//        this.methods = new OppMethodList();
//
//        initializeGuiComponents();
//    }
    
    public void addMethod(Method method) {
        this.methods.add(method);
    }
    
//    public String getName() {
//        return this.name;
//    }
    
    public javax.swing.JPanel getJPanel() {
        return this.jPanelMethods;
    }
    
    public javax.swing.JTabbedPane getJTabbedPane(){
        return this.jTabbedPaneMethods;
    }
    
    public void initializeGuiComponents() {
        //gui components' initialization
        jPanelMethods = new javax.swing.JPanel();
        jPanelMethods.setName(this.name);
        
        jTabbedPaneMethods = new javax.swing.JTabbedPane();
        jPanelMethods.add(jTabbedPaneMethods);
        
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanelMethods);
        jPanelMethods.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneMethods)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPaneMethods)
        );
    }
    
//    public void attachMethodsTabs(){
//        for(int i = 0; i<methods.size(); i++)
//            jTabbedPaneMethods.addTab(methods.get(i).getName(), methods.get(i).getMethodPanel());
//    }
    
//    public void updateMethodsTabs(String metric, int metric_index){
//        this.methods.updateList(metric, metric_index);
//    }

    public void clusterOpportunities(int size_max_dif, int lines_max_difference, String comparison_metric_name,int comparison_metric_index, boolean deltaRun, double min_overlap, double sig_dif) {
        this.methods.clusterOpportunities(size_max_dif, lines_max_difference, comparison_metric_name, comparison_metric_index, deltaRun, min_overlap, sig_dif);
        
    }

    public OppMethodList getMethods() {
        return this.methods;
    }

    public int getMethodIndex(String methodName) {
        return this.methods.getMethodIndex(methodName);
    }

    public void sortMethodsOnMetric(String rankingMetric) {
        rankingMetric = rankingMetric.toLowerCase(); //FIXME
        this.methods.sortMethodsOnMetric(rankingMetric);
    }
    
    public double getMinValueOfMetric(String metric){
        return this.methods.getMinValueOfMetric(metric);
    }
    
    public double getMaxValueOfMetric(String metric){
        return this.methods.getMaxValueOfMetric(metric);
    }

    public void setFile(String absolutePath) {
        this.filepath = absolutePath;
    }

    public void setInvalid_lines(ArrayList<ArrayList<Integer>> invalid_lines) {
        this.invalid_lines = invalid_lines;
    }
    
    public ArrayList<ArrayList<Integer>> getInvalid_lines() {
        return this.invalid_lines;
    }

    public void setPossible_invalid_bracket_close(ArrayList<Integer> possible_invalid_bracket_close) {
        this.possible_invalid_bracket_close = possible_invalid_bracket_close;
    }
    
    public ArrayList<Integer> getPossible_invalid_bracket_close() {
        return this.possible_invalid_bracket_close;
    }
    

}
