package clustering.cluster;

import gui.LongMethodDetector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

/**
 *
 * @author Antonis Gkortzis (s2583070, antonis.gkortzis@gmail.com)
 */
public class OpportunityList {

    private ArrayList<Opportunity> opportunities;
    private String separator = ",";

    public OpportunityList() {
        this.opportunities = new ArrayList<>();
    }

    public OpportunityList(OpportunityList opportunities_original) {
        this.opportunities = new ArrayList<>();
        this.opportunities.addAll(opportunities_original.getOpportunities());
    }

    public Opportunity findOpportunity(String key) {
        Opportunity current = null;
        for (int i = 0; i < this.opportunities.size() - 1; i++) {
            current = this.opportunities.get(i); //As A
            if (current.getLinesCluster().equals(key)) {
                break;
            }
        }
        return current;
    }

    public void start_clustering(double max_size_difference_percentage, double min_overlap, int lines_max_difference, double signifficant_difference, String cohesion_metric_name, int cohesion_metric_index, boolean deltaRun) {
        resetOpportunities();
        if (LongMethodDetector.DebugMode) {
            System.out.print("Clustering with max_dif_percentage: " + max_size_difference_percentage
                    + " and min_overlap: " + min_overlap
                    //                + " and max_lines_dif: " + lines_max_difference
                    + " and sig_diff: " + signifficant_difference
                    + " with metrics: " + cohesion_metric_name
                    + "\n");
        }

        Opportunity current_opp = null;
        Opportunity comparable_opp = null;

        for (int i = 0; i < this.opportunities.size() - 1; i++) {
            current_opp = this.opportunities.get(i); //As A

            for (int j = i + 1; j < this.opportunities.size(); j++) {

                //Skip to the next if A is already beaten.
                if (current_opp.isBeaten()) {
                    continue;
                }
                comparable_opp = this.opportunities.get(j); //as B
                //The difference in size (absolute value) between A & B
                int current_size_difference = Math.abs(comparable_opp.getSizeCluster() - current_opp.getSizeCluster());
                double current_max_size_difference = (int) Math.ceil(Math.min(comparable_opp.getSizeCluster(), current_opp.getSizeCluster()) * max_size_difference_percentage);
                double current_overlap = current_opp.overlap(comparable_opp); //FIXME
                //Checking the max_dif && the min_overlap between A & B && the "beaten" flag of B
                if (LongMethodDetector.DebugMode) {
                    System.out.println("Current overlap percentage: " + current_overlap + ", current_size_difference: " + current_size_difference + " [max_size_dif_percentage: " + current_max_size_difference + "]");
                }
                if (current_size_difference <= current_max_size_difference && current_overlap >= min_overlap && !comparable_opp.isBeaten()) {
                    if (LongMethodDetector.DebugMode) {
                        System.out.println("@CLUSTERING: current [" + (i + 1) + "]" + current_opp.getLinesCluster() + " and [" + (j + 1) + "]" + comparable_opp.getLinesCluster() + " can make a cluster.");//DEBUG
                    }
                    //checking if A beats B (check method beats() for comments)
                    if (current_opp.beats(comparable_opp, cohesion_metric_index, signifficant_difference, deltaRun)) {
                        if (LongMethodDetector.DebugMode) {
                            System.out.println("\t\tCurrent " + current_opp.getLinesCluster() + " wins!");
                        }
                        //add B to As cluster
                        current_opp.getCluster().add(comparable_opp);
                        //and A steals Bs' cluster!
                        current_opp.stealOpportunities(comparable_opp);
                        comparable_opp.setCluster(new OpportunityList());
                        //flag B as beaten!
                        comparable_opp.setBeaten(true);
                        if (LongMethodDetector.DebugMode) {
                            current_opp.printCluster();
                        }
                    } else {
                        if (LongMethodDetector.DebugMode) {
                            System.out.println("\t\tComparable " + comparable_opp.getLinesCluster() + " wins!");
                        }
                        //add A to Bs cluster
                        comparable_opp.getCluster().add(current_opp);
                        //and B steals As' cluster!
                        comparable_opp.stealOpportunities(current_opp);
                        current_opp.setCluster(new OpportunityList());
                        //flag A as beaten!
                        current_opp.setBeaten(true);

                        if (LongMethodDetector.DebugMode) {
                            comparable_opp.printCluster(); //DEBUG
                        }
                    }
                } else if (LongMethodDetector.DebugMode) {
                    System.out.println("@CLUSTERING: [" + (i + 1) + "]" + current_opp.getLinesCluster() + " and [" + (j + 1) + "]" + comparable_opp.getLinesCluster() + " cannot make a cluster.");//DEBUG
                }
            }
        }
        if (LongMethodDetector.DebugMode) {
            System.out.print("\t..finished!\n");
        }
    }

    public void sortOpportunitiesOnMetricAndSigDif(final int metric_index, final double signifficant_difference, final boolean deltaRun) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(this.opportunities, new Comparator<Opportunity>() {
            @Override
            public int compare(Opportunity a, Opportunity b) {
                return b.beats(a, metric_index, signifficant_difference, deltaRun) ? 1 : -1;
            }
        });

        //sort clusters
        for (int index = 0; index < this.opportunities.size(); index++) {
            Opportunity a = this.opportunities.get(index);
            //sort the cluster if exists
            if (a.getCluster().size() > 0) {
                a.getCluster().sortOpportunitiesOnMetricAndSigDif(metric_index, signifficant_difference, deltaRun);
            }
        }
    }

    public void updatePositions(boolean inCluster) {
        for (int index = 0; index < this.opportunities.size(); index++) {
            Opportunity opp = this.opportunities.get(index);
            // If the opp is leader set position -1
            if (!opp.isBeaten()) {
                opp.setPosition(1);
                //If the opp is a leader of a cluster then update positions in the cluster
                if (opp.getCluster().size() > 0) {
                    opp.getCluster().updatePositions(true);
                }
            } else if (opp.isBeaten() && inCluster) {
                opp.setPosition(index + 2);
            }
        }
    }

    public ArrayList<Opportunity> getOpportunities() {
        return opportunities;
    }

    public void setOpportunities(ArrayList<Opportunity> opportunities) {
        this.opportunities = opportunities;
    }

    public void add(Opportunity opportunity) {
        opportunities.add(opportunity);
    }

    public int size() {
        return this.opportunities.size();
    }

    public Opportunity get(int i) {
        return this.opportunities.get(i);
    }

    public String getCsvOutputHeader() {
        return "lines_original" + separator
                + "size_original" + separator
                + "cob_original" + separator
                + "lcom1_original" + separator
                + "lcom2_original" + separator
                + "lcom3_original" + separator
                + "lcom4_original" + separator
                + "lcom5_original" + separator
                + "tcc_original" + separator
                + "lcc_original" + separator
                + "dcd_original" + separator
                + "dci_original" + separator
                + "cc_original" + separator
                + "coh_original" + separator
                + "scom_original" + separator
                + "lscc_original" + separator
                + "comments" + separator
                + "missing" + separator
                + "lines_cluster" + separator
                + "cluster_size" + separator
                //delta metrics
                + "cob_delta" + separator
                + "lcom1_delta" + separator
                + "lcom2_delta" + separator
                + "lcom3_delta" + separator
                + "lcom4_delta" + separator
                + "lcom5_delta" + separator
                + "tcc_delta" + separator
                + "lcc_delta" + separator
                + "dcd_delta" + separator
                + "dci_delta" + separator
                + "cc_delta" + separator
                + "coh_delta" + separator
                + "scom_delta" + separator
                + "lscc_delta";
    }

    public String getCsvOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCsvOutputHeader()).append("\n");
        for (int i = 0; i < this.opportunities.size(); i++) {
            Opportunity current = this.opportunities.get(i);
            //Do not write those that have 0 cluster and are beaten
            if (current.getCluster().size() < 1 && current.isBeaten()) {
                continue;
            } else {
                sb.append(current.getCsvOutput());
            }
        }
        sb.append(AppendLines()).append("\n");
        return sb.toString();
    }

    public ArrayList<String> getCsvOutputArrayList() {
        ArrayList<String> results = new ArrayList<>();
        for (int i = 0; i < this.opportunities.size(); i++) {
            Opportunity current = this.opportunities.get(i);
            if (!current.isBeaten()) {
                results.add(current.getCsvOutput());
            } else if (!current.getCritical().isEmpty()) {
                results.add(current.getCsvOutput());
            } else {
                continue;
            }
        }
        return results;
    }
    
    public void printBenefits(){
        String header = "lines_cluster" + separator
                + "size_cluster" + separator
                + "cob_benefit" + separator
                + "lcom1_benefit" + separator
                + "lcom2_benefit" + separator
                + "lcom3_benefit" + separator
                + "lcom4_benefit" + separator
                + "lcom5_benefit" + separator
                + "tcc_benefit" + separator
                + "lcc_benefit" + separator
                + "dcd_benefit" + separator
                + "dci_benefit" + separator
                + "cc_benefit" + separator
                + "coh_benefit" + separator
                + "scom_benefit" + separator
                + "lscc_benefit";
        System.out.println(header);
        for (Opportunity opp : this.opportunities) {
            opp.printBenefits();
        }
    }

    private String AppendLines() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("=======" + separator);
        }
        sb.append("=======");
        return sb.toString();
    }

    public int getNumberofCriticalOpportunities(String name) {
        int count = 0;
        HashSet<String> temp_tentative_critical = new HashSet<>();

        for (int i = 0; i < this.opportunities.size(); i++) {
            Opportunity current = this.opportunities.get(i);
            if (!current.getCritical().isEmpty()) {
                if (temp_tentative_critical.addAll(current.getCritical())) {
                }
            }
        }
        return temp_tentative_critical.size();
    }

    public int getNumberOfOpportunitySuggestions() {
        int count = 0;
        for (Opportunity opp : this.opportunities) {
            if (!opp.isBeaten()) //if current opportunity is not beaten --> is a cluster leader
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Searches an Opportunity that it's Lines matches the @param and that
     * Opportunity is not_beaten (leader).
     *
     * @param crit a String of type "xxx to xxx"
     * @return the first Opportunity that matches the @param or Null if no match
     * is found
     */
    public Opportunity getRealOpportunityOnlyIfLeader(String crit) {
        for (int i = 0; i < this.opportunities.size(); i++) {
            Opportunity opp = this.opportunities.get(i);
            if (opp.CompareStrings(opp.getLinesCluster(), crit)) {
                if (!opp.isBeaten()) {
                    return opp;
                }
            }
        }
        return null;
    }

    public Opportunity getOpportunity(String selectedValue) {
        for (Opportunity opp : opportunities) {
            if (opp.getLinesCluster().equals(selectedValue)) {
                return opp;
            }
        }
        return null;
    }

    private void resetOpportunities() {
        for (int i = 0; i < this.opportunities.size(); i++) {
            this.opportunities.get(i).setBeaten(false); //reset beaten
            this.opportunities.get(i).resetOpportinityList(); //reset the cluster of the opp
        }
    }

    public void calculateBenefits() {
        for (int i = 0; i < this.opportunities.size(); i++) {
            this.opportunities.get(i).calculateBenefit();
        }
    }

    public void addBenefitsToList() {
        for (int i = 0; i < this.opportunities.size(); i++) {
            this.opportunities.get(i).addBenefitMetricsToList();
        }
    }

    public void printOptimals() {
        System.out.println("$$ Optimal opportunities $$");
        int cnt = 1;
        for (int i = 0; i < this.opportunities.size(); i++) {
            Opportunity opp = this.opportunities.get(i);
            if (!opp.isBeaten()) {
                System.out.println(cnt++ + " " + opp.getLinesCluster());
            }
        }
    }
    public ArrayList<Opportunity> getOptimals() {
    	ArrayList<Opportunity> ret = new ArrayList<Opportunity>();
        int cnt = 1;
        for (int i = 0; i < this.opportunities.size(); i++) {
            Opportunity opp = this.opportunities.get(i);
            if (!opp.isBeaten()) {
            	ret.add(opp);
                System.out.println(cnt++ + " " + opp.getLinesCluster());
            }
        }

        //Arrays.sort(ret);
        Collections.sort(ret);
        return ret;
    }

}
