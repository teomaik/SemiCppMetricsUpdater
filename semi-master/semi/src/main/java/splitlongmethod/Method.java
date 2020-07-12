package splitlongmethod;

import clustering.cluster.OpportunityList;
import java.util.ArrayList;

public class Method {

    String name;
    ArrayList<Variables> vList = new ArrayList<>();
    ClusterList allClusters = new ClusterList();
    
    public boolean been_analysed; 

    public Method(String n) {
        name = n;
        this.opportunities = new OpportunityList();
        this.original_metrics = new double[15];
        been_analysed = false;
    }

    public ArrayList<Variables> getVariableList() {
        return vList;
    }

    public void sortVariables() {
        for (int i = 0; i < vList.size(); i++) {
            vList.get(i).sortLines();
        }
    }

    public void printVariables() {
        for (int i = 0; i < vList.size(); i++) {
            vList.get(i).print();
        }
    }

    public boolean hasSwitchInLine(int line) {
        boolean temp = false;
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getType().contains("Inv") == false) {
                if (vList.get(i).getName().equals("SWITCH")) {
                    if (vList.get(i).accesses(line)) {
                        temp = true;
                    }
                }
            }
        }
        return temp;
    }

    public boolean usesVariableInLine(int line_num) {
        boolean found = false;
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).accesses(line_num)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public ClusterList calculateClusters(int dist, ArrayList<ArrayList<Integer>> invalid_lines) {
        ClusterList clusters = new ClusterList();
        clusters.setMethodSize(getMethodSize(invalid_lines));
        clusters.setMethodEnd(getMethodEnd());
        clusters.setVariablesList(vList);
        for (int i = 0; i < vList.size(); i++) {
            ArrayList<Cluster> temp = vList.get(i).calculateClusters(dist);
            clusters.addAll(temp);
        }
        return clusters;
    }

    public int getMethodStart(ArrayList<ArrayList<Integer>> invalid_lines) {
        int min = 9999999;
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getMinLine(invalid_lines) < min) {
                min = vList.get(i).getMinLine(invalid_lines);
            }
        }
        //System.out.println("Method: " + name + " finishes at: " + (max+1));

        // Check if min-1 is the last line of an invalid cluster
        int alt_min = min - 1;
        boolean alt_min_last_in_invalid = false;

        for (int i = 0; i < invalid_lines.size(); i++) {
            ArrayList<Integer> cur_invalid = invalid_lines.get(i);
            int pos = cur_invalid.size() - 1;
            if (cur_invalid.get(pos) == alt_min) {
                alt_min_last_in_invalid = true;
                break;
            }
        }

        if (alt_min_last_in_invalid) {
            return alt_min;
        } else {
            return (min);
        }
    }

    public int getMethodEnd() {
        int max = -1;
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getMaxLine() > max) {
                max = vList.get(i).getMaxLine();
            }
        }
        //System.out.println("Method: " + name + " finishes at: " + (max+1));
        return (max + 1);
    }

    public int getMethodSize(ArrayList<ArrayList<Integer>> invalid_lines) {
        int max = -1;
        int min = 999999999;
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getMaxLine() > max) {
                max = vList.get(i).getMaxLine();
            }
            if (vList.get(i).getMinLine(invalid_lines) < min) {
                min = vList.get(i).getMinLine(invalid_lines);
            }
        }
        return max - min + 1;
    }

    public String getName() {
        return name;
    }

    public void addVariables(Variables v) {
        vList.add(v);
    }

    public void addToVariable(String vname, String line, String type, String assign) {
        boolean found = false;
        int pos = -1;

        //if (line.equals("889")) {
        //if (vname.equals("MAX_SEGMENTS")) {
        //	System.out.println("aaaaaaaaaaaa");
        //}
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getName().equals(vname)) {
                found = true;
                pos = i;
                //break;
            }
        }
        if (found) {

            //if (line == 253 && vname.equals("turns"))
            if (assign.equals("assign") && (vList.get(pos).getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                vList.get(pos).addAccessedLines(line);
            }
            vList.get(pos).addLines(Integer.parseInt(line));
            if (type.equals("Invocation")) {
                vList.get(pos).setType("method_call");
            }
        }

        Variables v = new Variables("", "");
        if (!found) {
            if (type.equals("Invocation")) {
                v = new Variables(vname, "method_call");
                if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                    v.addAccessedLines(line);
                }
                v.addLines(Integer.parseInt(line));
                vList.add(v);
            } else if (type.equals("Usage")) {
                if (vname.contains(".")) {
                    if (vname.startsWith("this.")) {
                        // get name without this.  (vaName)
                        String[] temp = vname.split("\\.");
                        String vaName = temp[1];
                        for (int i = 0; i < vList.size(); i++) {
                            if (vList.get(i).getName().equals(vaName)) {
                                found = true;
                                pos = i;
                                //break;
                            }
                        }
                        if (found == true) {
                            if (vList.get(pos).getType().equals("parameter")) {
                                v = new Variables(vname, "attribute");
                                if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                                    v.addAccessedLines(line);
                                }
                                v.addLines(Integer.parseInt(line));
                                vList.add(v);
                            } else {
                                vList.get(pos).addLines(Integer.parseInt(line));
                                if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                                    v.addAccessedLines(line);
                                }
                            }
                        } else {
                            v = new Variables(vname, "attribute");
                            if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                                v.addAccessedLines(line);
                            }
                            v.addLines(Integer.parseInt(line));
                            vList.add(v);
                        }
                    } else {
                        v = new Variables(vname, "enums");
                        if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                            v.addAccessedLines(line);
                        }
                        v.addLines(Integer.parseInt(line));
                        vList.add(v);
                    }
                } else {
                    // get name with this.  (vaName)
                    String vaName = "this." + vname;
                    for (int i = 0; i < vList.size(); i++) {
                        if (vList.get(i).getName().equals(vaName)) {
                            found = true;
                            pos = i;
                            //break;
                        }
                    }
                    if (found == false) {
                        v = new Variables(vname, "attribute");
                        if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                            v.addAccessedLines(line);
                        }
                        v.addLines(Integer.parseInt(line));
                        vList.add(v);
                    } else {
                        if (assign.equals("assign") && (v.getAccessedLines().contains(Integer.parseInt(line)) == false)) {
                            v.addAccessedLines(line);
                        }
                        vList.get(pos).addLines(Integer.parseInt(line));
                    }
                }
            }
        }
    }

    public void cleanUp() {
        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getNumberOfAccesses() == 0) {
                vList.remove(i);
                i--;
            }
        }
    }

    public String[] calculateMethodMetrics(ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> lines_for_else) {
        Cluster c1 = new Cluster(getMethodStart(invalid_lines), getMethodEnd());
        String original_cluster = c1.printToString(getVariableList(), invalid_lines, lines_for_else, "noparams", "original");

        String[] original_metrics_unparsed = c1.getMetrics();
//                for(String s : original_metrics){
//                    System.out.println("s : " + s);
//                }
        return original_metrics_unparsed;
    }

    public OpportunityList calculateAllClusters(int start, ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> possible_invalid_bracket_close, ArrayList<Integer> lines_for_else) {
        for (int i = 1; i <= getMethodSize(invalid_lines); i++) {
            //System.out.println("starting in line: " + i);
            ClusterList cList = calculateClusters(i, invalid_lines);
            cList.sort();
            cList.merge();
            allClusters.addAll(cList);
            allClusters.setMethodEnd(cList.getMethodEnd());
        }
        //System.out.println("Candidates Created");
        allClusters.removeDuplicates();
//		allClusters.sort();
//		allClusters.sortByEnding();
        //System.out.println("Duplicates Removed");
        allClusters.filter(start, possible_invalid_bracket_close, invalid_lines);
        //System.out.println("Filtered");
        allClusters.sort();
        allClusters.sortByEnding();
        //System.out.println("Sorted");
        allClusters.removeDuplicates();
        //System.out.println("Duplicates Removed Again");
        //TODO how to keep only the methods metrics calculation. Only the fopllwoign method is necessary. Not those above
        return allClusters.print(name, invalid_lines, possible_invalid_bracket_close, this);
    }

//        public String name;
    private OpportunityList opportunities;

    private javax.swing.JTextArea sourceCodeTextArea;
    private double[] original_metrics;

    public double[] getOriginalMetrics() {
        return this.original_metrics;
    }

//    public OppMethod(String name) {
//        this.name = name;
//        this.opportunities = new OpportunityList();
//    }
//
//    public String getName() {
//        return this.name;
//    }
//    
    public OpportunityList getOpportunityList() {
        return this.opportunities;
    }

    public void setOppList(OpportunityList opportunities) {
        this.opportunities = opportunities;
    }

    public void clusterOpportunities(double size_max_dif, int lines_max_difference, String comparison_metric_name, int comparison_metric_index, boolean deltaRun, double min_overlap, double sig_dif) {
        
        System.out.println("## Initial Opportunity List with benefits! ##");
        this.opportunities.printBenefits();
        System.out.println("");
        
        System.out.println("Clustering opportunities with parameters:");//DEBUG
        System.out.println("\tsize_max_dif_percentage: " + size_max_dif);//DEBUG
        System.out.println("\tlines_max_difference: " + lines_max_difference);//DEBUG
        System.out.println("\tcomparison_metric_name: " + comparison_metric_name);//DEBUG
        System.out.println("\tcomparison_metric_index: " + comparison_metric_index);//DEBUG
        System.out.println("\tdeltaRun: " + deltaRun);//DEBUG
        System.out.println("\tmin_overlap: " + min_overlap);//DEBUG
        System.out.println("\tsig_dif: " + sig_dif);//DEBUG
        
        if(opportunities == null)
            System.out.println("### ERROR ### Opportunities are empty!");
    //Start clustering the opportunities according to the given parameters
        opportunities.start_clustering(size_max_dif, min_overlap, lines_max_difference, sig_dif, comparison_metric_name, comparison_metric_index, deltaRun);
        //sorting the Opportunity List and its clusters by the current metric
        opportunities.sortOpportunitiesOnMetricAndSigDif(comparison_metric_index, sig_dif, deltaRun);
        //update the positions of the opportunities in the clusters
        opportunities.updatePositions(false);
        System.out.println("@clusterOpportunities : clustering completed");
    }

    public boolean needsRefactoring(String rankingMetric) {
        rankingMetric = rankingMetric.toLowerCase(); 
        double b0 =0, b1 = 0;
        double metric = getMetricIndexFromName(rankingMetric);

        switch (rankingMetric) {
            case "size":
                b0 = -4.491;
                b1 = 0.103;
                break;
            case "lcom1":
                b0 = -1.281;
                b1 = 0.002;
                break;
            case "lcom2":
                b0 = -0.809;
                b1 = 0.002;
                break;
            case "lcom4":
                b0 = -0.536;
                b1 = 0.113;
                break;
            case "coh":
                b0 = 1.392;
                b1 = -10.200;
                break;
            case "cc":
                b0 = 0.996;
                b1 = -5.362;
                break;
        }
        double super_script = - (b0 + b1 * metric);

        double fx = 1 / (1 + Math.pow(Math.E, super_script));

        return fx > 0.5;
    }

    public void setOriginalMetrics(String[] metrics) {
        System.out.println("NEW : Method metrics : " + this.name);
        this.original_metrics = new double[metrics.length];
        for (int i = 0; i < metrics.length; i++) {
            System.out.print(metrics[i] + "|" + Double.parseDouble(metrics[i]) + ", ");
            this.original_metrics[i] = Double.parseDouble(metrics[i]);
        }
//        System.out.println("\n");
        //TODO is that right????
        if (this.original_metrics[2] < 0) {
            this.original_metrics[2] = 0;
        }
//        
//        System.out.println("NEW FOR GOD SAKE!");
//        printMetrics();
        //ok until here!
    }

    public double getMetricIndexFromName(String rankingMetric) {
        rankingMetric = rankingMetric.toLowerCase(); //FIXME
        switch (rankingMetric) {
            case "size":
                return this.original_metrics[2];
            case "lcom1":
                return this.original_metrics[4];
            case "lcom2":
                return this.original_metrics[5];
            case "lcom4":
                return this.original_metrics[7];
            case "coh":
                return this.original_metrics[14];
            case "cc":
                return this.original_metrics[13];
            default:
                return -1000;
        }
    }

    public void printMetrics() {
        for (int i = 0; i < this.original_metrics.length; i++) {
            System.out.print(original_metrics[i] + " | ");
        }

        System.out.println("");
    }

}
