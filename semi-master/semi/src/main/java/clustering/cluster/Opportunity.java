package clustering.cluster;

import gui.LongMethodDetector;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author Antonis Gkortzis (s2583070, antonis.gkortzis@gmail.com)
 */
public class Opportunity  implements Comparable<Opportunity> {

    private static final String separator = ",";
    private int id;
    private HashSet<String> isCritical;
    private OpportunityList cluster;
    private boolean is_beaten;
    ArrayList<Double> metricsValues;
    ArrayList<Double> metricsDeltaValues;
    ArrayList<Double> metricsBenefits;
    private int position;
    private int lines_max_difference;
    private HashSet<String> original_critical;

    //cluster metrics
    private String lines_cluster;
    private int start_line_cluster;
    private int end_line_cluster;
    private int size_cluster;
    private double cob_cluster;
    private double lcom1_cluster;
    private double lcom2_cluster;
    private double lcom3_cluster;
    private double lcom4_cluster;
    private double lcom5_cluster;
    private double tcc_cluster;
    private double lcc_cluster;
    private double dcd_cluster;
    private double dci_cluster;
    private double cc_cluster;
    private double coh_cluster;
    private double scom_cluster;
    private double lscc_cluster;
    private double comments;
    private double lines_cluster_alwaysZero;

    //original metrics
    private String lines_original;
    private int start_line_original;
    private int end_line_original;
    private int size_original;
    private double cob_original;
    private double lcom1_original;
    private double lcom2_original;
    private double lcom3_original;
    private double lcom4_original;
    private double lcom5_original;
    private double tcc_original;
    private double lcc_original;
    private double dcd_original;
    private double dci_original;
    private double cc_original;
    private double coh_original;
    private double scom_original;
    private double lscc_original;

    //new metrics
    private boolean hasNewMetrics;
    private String lines_new;
    private int start_line_new;
    private int end_line_new;
    private int size_new;
    private double cob_new;
    private double lcom1_new;
    private double lcom2_new;
    private double lcom3_new;
    private double lcom4_new;
    private double lcom5_new;
    private double tcc_new;
    private double lcc_new;
    private double dcd_new;
    private double dci_new;
    private double cc_new;
    private double coh_new;
    private double scom_new;
    private double lscc_new;

    //benefit metrics
    private double cob_benefit;
    private double lcom1_benefit;
    private double lcom2_benefit;
    private double lcom3_benefit;
    private double lcom4_benefit;
    private double lcom5_benefit;
    private double tcc_benefit;
    private double lcc_benefit;
    private double dcd_benefit;
    private double dci_benefit;
    private double cc_benefit;
    private double coh_benefit;
    private double scom_benefit;
    private double lscc_benefit;

    public Opportunity(int id, int start_line, int end_line, int size,
            double cob, double lcom1, double lcom2, double lcom3, double lcom4, double lcom5,
            double tcc, double lcc, double dcd, double dci, double cc, double coh,
            double scom, double lscc, double comments, double lines_cluster,
            int lines_max_difference) {
        super();
        this.id = id;
        this.start_line_cluster = start_line;
        this.end_line_cluster = end_line;
        this.size_cluster = size;
        this.cob_cluster = cob;
        this.lcom1_cluster = lcom1;
        this.lcom2_cluster = lcom2;
        this.lcom3_cluster = lcom3;
        this.lcom4_cluster = lcom4;
        this.lcom5_cluster = lcom5;
        this.tcc_cluster = tcc;
        this.lcc_cluster = lcc;
        this.dcd_cluster = dcd;
        this.dci_cluster = dci;
        this.cc_cluster = cc;
        this.coh_cluster = coh;
        this.scom_cluster = scom;
        this.lscc_cluster = lscc;
        this.comments = comments;
        this.lines_cluster_alwaysZero = lines_cluster;

        this.cluster = new OpportunityList();
        this.is_beaten = false;
        this.isCritical = new HashSet<>();
        this.original_critical = new HashSet<>();

        this.position = 0;
        this.setLinesCluster(String.format("%03d", start_line) + " to " + String.format("%03d", end_line));
        addMetricsToList();
    }

    public Opportunity() {
        this.cluster = new OpportunityList();
        this.is_beaten = false;
        this.isCritical = new HashSet<>();
        this.original_critical = new HashSet<>();

        this.position = 0;
    }

    public void addMetricsToList() {
        this.metricsValues = new ArrayList<>(Arrays.asList(this.cob_cluster, this.lcom1_cluster, this.lcom2_cluster, this.lcom3_cluster, this.lcom4_cluster, this.lcom5_cluster,
                this.tcc_cluster, this.lcc_cluster, this.dcd_cluster, this.dci_cluster, this.cc_cluster, this.coh_cluster, this.scom_cluster, this.lscc_cluster));
    }

    public void addBenefitMetricsToList() {
        this.metricsBenefits = new ArrayList<>(Arrays.asList(this.cob_benefit, this.lcom1_benefit, this.lcom2_benefit, this.lcom3_benefit, this.lcom4_benefit, this.lcom5_benefit,
                this.tcc_benefit, this.lcc_benefit, this.dcd_benefit, this.dci_benefit, this.cc_benefit, this.coh_benefit, this.scom_benefit, this.lscc_benefit));
    }

    public void calculateDeltaMetrics() {
        //declaring delta values
        double cob_delta, lcom1_delta, lcom2_delta, lcom3_delta, lcom4_delta, lcom5_delta, tcc_delta, lcc_delta,
                dcd_delta, dci_delta, cc_delta, coh_delta, scom_delta, lscc_delta;

        //cob
        if (this.cob_original == -Double.MAX_VALUE || this.cob_new == -Double.MAX_VALUE) {
            cob_delta = -Double.MAX_VALUE;
        } else {
            cob_delta = this.cob_original - this.cob_new;
        }
        //lcom1
        if (this.lcom1_original == -Double.MAX_VALUE || this.lcom1_new == -Double.MAX_VALUE) {
            lcom1_delta = -Double.MAX_VALUE;
        } else {
            lcom1_delta = this.lcom1_original - this.lcom1_new;
        }
        //lcom2
        if (this.lcom2_original == -Double.MAX_VALUE || this.lcom2_new == -Double.MAX_VALUE) {
            lcom2_delta = -Double.MAX_VALUE;
        } else {
            lcom2_delta = this.lcom2_original - this.lcom2_new;
        }
        //lcom3
        if (this.lcom3_original == -Double.MAX_VALUE || this.lcom3_new == -Double.MAX_VALUE) {
            lcom3_delta = -Double.MAX_VALUE;
        } else {
            lcom3_delta = this.lcom3_original - this.lcom3_new;
        }
        //lcom4
        if (this.lcom4_original == -Double.MAX_VALUE || this.lcom4_new == -Double.MAX_VALUE) {
            lcom4_delta = -Double.MAX_VALUE;
        } else {
            lcom4_delta = this.lcom4_original - this.lcom4_new;
        }
        //lcom5
        if (this.lcom5_original == -Double.MAX_VALUE || this.lcom5_new == -Double.MAX_VALUE) {
            lcom5_delta = -Double.MAX_VALUE;
        } else {
            lcom5_delta = this.lcom5_original - this.lcom5_new;
        }
        //tcc
        if (this.tcc_original == -Double.MAX_VALUE || this.tcc_new == -Double.MAX_VALUE) {
            tcc_delta = -Double.MAX_VALUE;
        } else {
            tcc_delta = this.tcc_original - this.tcc_new;
        }
        //lcc
        if (this.lcc_original == -Double.MAX_VALUE || this.lcc_new == -Double.MAX_VALUE) {
            lcc_delta = -Double.MAX_VALUE;
        } else {
            lcc_delta = this.lcc_original - this.lcc_new;
        }
        //dcd
        if (this.dcd_original == -Double.MAX_VALUE || this.dcd_new == -Double.MAX_VALUE) {
            dcd_delta = -Double.MAX_VALUE;
        } else {
            dcd_delta = this.dcd_original - this.dcd_new;
        }
        //dci
        if (this.dci_original == -Double.MAX_VALUE || this.dci_new == -Double.MAX_VALUE) {
            dci_delta = -Double.MAX_VALUE;
        } else {
            dci_delta = this.dci_original - this.dci_new;
        }
        //cc
        if (this.cc_original == -Double.MAX_VALUE || this.cc_new == -Double.MAX_VALUE) {
            cc_delta = -Double.MAX_VALUE;
        } else {
            cc_delta = this.cc_original - this.cc_new;
        }
        //coh
        if (this.coh_original == -Double.MAX_VALUE || this.coh_new == -Double.MAX_VALUE) {
            coh_delta = -Double.MAX_VALUE;
        } else {
            coh_delta = this.coh_original - this.coh_new;
        }
        //scom
        if (this.scom_original == -Double.MAX_VALUE || this.scom_new == -Double.MAX_VALUE) {
            scom_delta = -Double.MAX_VALUE;
        } else {
            scom_delta = this.scom_original - this.scom_new;
        }
        //lscc
        if (this.lscc_original == -Double.MAX_VALUE || this.lscc_new == -Double.MAX_VALUE) {
            lscc_delta = -Double.MAX_VALUE;
        } else {
            lscc_delta = this.lscc_original - this.lscc_new;
        }

        this.metricsDeltaValues = new ArrayList<>(Arrays.asList(cob_delta, lcom1_delta, lcom2_delta,
                lcom3_delta, lcom4_delta, lcom5_delta, tcc_delta, lcc_delta, dcd_delta, dci_delta,
                cc_delta, coh_delta, scom_delta, lscc_delta));
    }

    public double getMetric(int metric_index) {
        return this.metricsValues.get(metric_index);
    }

    public double getDeltaMetric(int metric_index) {
        return this.metricsDeltaValues.get(metric_index);
    }

    public boolean CompareStrings(String a, String b) {
        boolean temp = false;
        String temp1[] = a.split(" to ");
        String temp2[] = b.split(" to ");
        int a1, a2, b1, b2, diff;
        a1 = Integer.parseInt(temp1[0]);
        a2 = Integer.parseInt(temp1[1]);
        b1 = Integer.parseInt(temp2[0]);
        b2 = Integer.parseInt(temp2[1]);

        diff = Math.abs(a1 - b1) + Math.abs(a2 - b2);

        if (diff <= this.lines_max_difference) {
            temp = true;
        }

        return temp;
    }

    public int getNumberofCriticalOpportunities() {
        return cluster.getNumberofCriticalOpportunities(this.lines_cluster);
    }

    public int lengthDifference(Opportunity comparable) {
        return Math.abs(comparable.getSizeCluster() - this.getSizeCluster());
    }

    public double overlap(Opportunity comparable) {
        System.out.println("");
        double overlap = 0;
        if ((this.start_line_cluster <= comparable.start_line_cluster
                && this.end_line_cluster >= comparable.end_line_cluster)
                || (this.start_line_cluster >= comparable.start_line_cluster
                && this.end_line_cluster <= comparable.end_line_cluster)) {
            overlap = Math.min(this.size_cluster, comparable.size_cluster);
            if (LongMethodDetector.DebugMode) {
                System.out.println("@overlaping: A contains B or B contains A | overlapping lines=" + overlap);
            }
        } else if (this.start_line_cluster <= comparable.start_line_cluster
                && this.start_line_cluster <= comparable.end_line_cluster
                && this.end_line_cluster >= comparable.start_line_cluster) {
            overlap = this.end_line_cluster - comparable.start_line_cluster;
            if (LongMethodDetector.DebugMode) {
                System.out.println("@overlaping: A is on the left | overlapping lines=" + overlap);
            }
        } else if (comparable.start_line_cluster <= this.start_line_cluster
                && comparable.start_line_cluster <= this.end_line_cluster
                && comparable.end_line_cluster >= this.start_line_cluster) {
            overlap = comparable.end_line_cluster - this.start_line_cluster;
            if (LongMethodDetector.DebugMode) {
                System.out.println("@overlaping: B is on the left | overlapping lines=" + overlap);
            }
        } else {
            return 0;
        }

        return (double) overlap / (double) Math.max(this.size_cluster, comparable.size_cluster);
    }

    public boolean beats(Opportunity comparable, int comparison_metric_index, double sig_dif, boolean deltaRun) {
        ArrayList<Double> this_metrics = this.metricsBenefits;
        ArrayList<Double> comparable_metrics = comparable.metricsBenefits;
        double dif = (double) (Math.abs(this_metrics.get(comparison_metric_index) - comparable_metrics.get(comparison_metric_index))) / (double) Math.max(this_metrics.get(comparison_metric_index),comparable_metrics.get(comparison_metric_index));
        if (LongMethodDetector.DebugMode) {
            System.out.println("\t@BEATS: Comparing [metric index:" + comparison_metric_index + "] "
                    + this.lines_cluster + "[" + this_metrics.get(comparison_metric_index) + "] with "
                    + comparable.lines_cluster + "[" + comparable_metrics.get(comparison_metric_index) + "]"
                    + " dif:sig_dif = " + dif + ":" + sig_dif);
        }

        if (comparison_metric_index >= 1 && comparison_metric_index <= 5) { //when param is of an LCOM type
            if (dif > sig_dif
                    && this_metrics.get(comparison_metric_index) != -Double.MAX_VALUE //if one of the 2 metrics is -Double.Max then compare the size
                    && comparable_metrics.get(comparison_metric_index) != -Double.MAX_VALUE) {
                return this_metrics.get(comparison_metric_index) > comparable_metrics.get(comparison_metric_index);
            } else {
                if (LongMethodDetector.DebugMode) {
                    System.out.println("\t checking the sizes " + this.getSizeCluster() + ":" + comparable.getSizeCluster());
                }
                return this.getSizeCluster() > comparable.getSizeCluster();
            }
        } else //when param is NOT of an LCOM type
            if (dif > sig_dif
                    && this_metrics.get(comparison_metric_index) != -Double.MAX_VALUE //if one of the 2 metrics is -Double.Max then compare the size
                    && comparable_metrics.get(comparison_metric_index) != -Double.MAX_VALUE) {
                return this_metrics.get(comparison_metric_index) < comparable_metrics.get(comparison_metric_index);
            } else {
                if (LongMethodDetector.DebugMode) {
                    System.out.println("\t checking the sizes " + this.getSizeCluster() + ":" + comparable.getSizeCluster());
                }
            return this.getSizeCluster() > comparable.getSizeCluster();
        }
    }

    public void stealOpportunities(Opportunity comparable) {
        if (comparable.getCluster().size() == 0) {
            return;
        } else {
            for (int i = 0; i < comparable.getCluster().size(); i++) {
                this.cluster.add(comparable.getCluster().get(i));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[" + id + "] : " + start_line_cluster + " to " + end_line_cluster + " : " + size_cluster + " : " + coh_cluster);
        if (this.isBeaten()) {
            sb.append(" : Beaten!");
        }
        return sb.toString();
    }

    public void printCluster() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.getCluster().size(); i++) {
            System.out.println("\t\t" + this.getCluster().get(i).getLinesCluster());
        }
    }

    public String getCsvOutput() {
        DecimalFormat format = new DecimalFormat("#.####");
        format.setDecimalSeparatorAlwaysShown(false);

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%03d", start_line_cluster) + " to " + String.format("%03d", end_line_cluster) + separator);
        sb.append(size_cluster + separator);
        sb.append(format.format(cob_cluster) + separator);
        sb.append(format.format(lcom1_cluster) + separator);
        sb.append(format.format(lcom2_cluster) + separator);
        sb.append(format.format(lcom3_cluster) + separator);
        sb.append(format.format(lcom4_cluster) + separator);
        sb.append(format.format(lcom5_cluster) + separator);
        sb.append(format.format(tcc_cluster) + separator);
        sb.append(format.format(lcc_cluster) + separator);
        sb.append(format.format(dcd_cluster) + separator);
        sb.append(format.format(dci_cluster) + separator);
        sb.append(format.format(cc_cluster) + separator);
        sb.append(format.format(coh_cluster) + separator);
        sb.append(format.format(scom_cluster) + separator);
        sb.append(format.format(lscc_cluster) + separator);
        sb.append(format.format(comments) + separator);
        sb.append(format.format(lines_cluster_alwaysZero) + separator);
        sb.append(this.cluster.size() + separator);
        //delta metrics
        for (double metric : this.metricsDeltaValues) {
            sb.append(format.format(metric) + separator);
        }

        sb.append("\n");
        return sb.toString();
    }

    public String getCsvOutputForArrayList() {
        DecimalFormat format = new DecimalFormat("#.####");
        format.setDecimalSeparatorAlwaysShown(false);

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%03d", start_line_cluster) + " to " + String.format("%03d", end_line_cluster) + separator);
        sb.append(size_cluster + separator);
        sb.append(format.format(cob_cluster) + separator);
        sb.append(format.format(lcom1_cluster) + separator);
        sb.append(format.format(lcom2_cluster) + separator);
        sb.append(format.format(lcom3_cluster) + separator);
        sb.append(format.format(lcom4_cluster) + separator);
        sb.append(format.format(lcom5_cluster) + separator);
        sb.append(format.format(tcc_cluster) + separator);
        sb.append(format.format(lcc_cluster) + separator);
        sb.append(format.format(dcd_cluster) + separator);
        sb.append(format.format(dci_cluster) + separator);
        sb.append(format.format(cc_cluster) + separator);
        sb.append(format.format(coh_cluster) + separator);
        sb.append(format.format(scom_cluster) + separator);
        sb.append(format.format(lscc_cluster) + separator);
        sb.append(format.format(comments) + separator);
        sb.append(format.format(this.getNumberofCriticalOpportunities()) + separator);
        sb.append(format.format(lines_cluster_alwaysZero));
        return sb.toString();
    }

    public String getLinesCluster() {
        return lines_cluster;
    }

    public void setLinesCluster(String lines) {
        this.lines_cluster = lines;
    }

    public double getCohCluster() {
        return coh_cluster;
    }

    public void setCohCluster(double coh) {
        this.coh_cluster = coh;
    }

    public int getStartLineCluster() {
        return start_line_cluster;
    }

    public void setStartLineCluster(int start_line) {
        this.start_line_cluster = start_line;
    }

    public int getEndLineCluster() {
        return end_line_cluster;
    }

    public void setEndLineCluster(int end_line) {
        this.end_line_cluster = end_line;
    }

    public int getSizeCluster() {
        return size_cluster;
    }

    public void setSizeCluster(int opp_length) {
        this.size_cluster = opp_length;
    }

    public OpportunityList getCluster() {
        return cluster;
    }

    public void setCluster(OpportunityList cluster) {
        this.cluster = cluster;
    }

    public boolean isBeaten() {
        return is_beaten;
    }

    public void setBeaten(boolean is_beaten) {
        this.is_beaten = is_beaten;
    }

    public double getLcom5Cluster() {
        return lcom5_cluster;
    }

    public void setLcom5Cluster(double lcom5) {
        this.lcom5_cluster = lcom5;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getCobCluster() {
        return cob_cluster;
    }

    public void setCobCluster(double cob) {
        this.cob_cluster = cob;
    }

    public double getLcom1Cluster() {
        return lcom1_cluster;
    }

    public void setLcom1Cluster(double lcom1) {
        this.lcom1_cluster = lcom1;
    }

    public double getLcom2Cluster() {
        return lcom2_cluster;
    }

    public void setLcom2Cluster(double lcom2) {
        this.lcom2_cluster = lcom2;
    }

    public double getLcom3Cluster() {
        return lcom3_cluster;
    }

    public void setLcom3Cluster(double lcom3) {
        this.lcom3_cluster = lcom3;
    }

    public double getLcom4Cluster() {
        return lcom4_cluster;
    }

    public void setLcom4Cluster(double lcom4) {
        this.lcom4_cluster = lcom4;
    }

    public double getTccCluster() {
        return tcc_cluster;
    }

    public void setTccCluster(double tcc) {
        this.tcc_cluster = tcc;
    }

    public double getLccCluster() {
        return lcc_cluster;
    }

    public void setLccCluster(double lcc) {
        this.lcc_cluster = lcc;
    }

    public double getDcdCluster() {
        return dcd_cluster;
    }

    public void setDcdCluster(double dcd) {
        this.dcd_cluster = dcd;
    }

    public double getDciCluster() {
        return dci_cluster;
    }

    public void setDciCluster(double dci) {
        this.dci_cluster = dci;
    }

    public double getCcCluster() {
        return cc_cluster;
    }

    public void setCcCluster(double cc) {
        this.cc_cluster = cc;
    }

    public double getScomCluster() {
        return scom_cluster;
    }

    public void setScomCluster(double scom) {
        this.scom_cluster = scom;
    }

    public double getLsccCluster() {
        return lscc_cluster;
    }

    public void setLscc(double lscc) {
        this.lscc_cluster = lscc;
    }

    public double getCommentsCluster() {
        return comments;
    }

    public void setCommentsCluster(double comments) {
        this.comments = comments;
    }

    public double getLines_clusterCluster() {
        return lines_cluster_alwaysZero;
    }

    public void setLines_clusterCluster(double lines_cluster) {
        this.lines_cluster_alwaysZero = lines_cluster;
    }

    public HashSet<String> getCritical() {
        return isCritical;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public HashSet<String> getOriginal_critical() {
        return original_critical;
    }

    public void setOriginal_critical(HashSet<String> original_critical) {
        this.original_critical = original_critical;
    }

    public void addClusterMetrics(int start_cluster, int end_cluster, int size_cluster,
            double cob_cluster, double lcom1_cluster, double lcom2_cluster,
            double lcom3_cluster, double lcom4_cluster, double lcom5_cluster,
            double tcc_cluster, double lcc_cluster, double dcd_cluster, double dci_cluster,
            double cc_cluster, double coh_cluster, double scom_cluster,
            double lscc_cluster, double comments_cluster, double lines_cluster_cluster) {
        this.start_line_cluster = start_cluster;
        this.end_line_cluster = end_cluster;
        this.size_cluster = size_cluster;
        this.cob_cluster = cob_cluster;
        this.lcom1_cluster = lcom1_cluster;
        this.lcom2_cluster = lcom2_cluster;
        this.lcom3_cluster = lcom3_cluster;
        this.lcom4_cluster = lcom4_cluster;
        this.lcom5_cluster = lcom5_cluster;
        this.tcc_cluster = tcc_cluster;
        this.lcc_cluster = lcc_cluster;
        this.dcd_cluster = dcd_cluster;
        this.dci_cluster = dci_cluster;
        this.cc_cluster = cc_cluster;
        this.coh_cluster = coh_cluster;
        this.scom_cluster = scom_cluster;
        this.lscc_cluster = lscc_cluster;
        this.comments = comments_cluster;
        this.lines_cluster_alwaysZero = lines_cluster_cluster;

        this.setLinesCluster(String.format("%03d", start_line_cluster) + " to " + String.format("%03d", end_line_cluster));
        addMetricsToList();
    }

    public void addOriginalMetrics(int start_original, int end_original,
            int size_original, double cob_original, double lcom1_original,
            double lcom2_original, double lcom3_original,
            double lcom4_original, double lcom5_original,
            double tcc_original, double lcc_original, double dcd_original,
            double dci_original, double cc_original, double coh_original,
            double scom_original, double lscc_original) {
        this.start_line_original = start_original;
        this.end_line_original = end_original;
        this.size_original = size_original;
        this.cob_original = cob_original;
        this.lcom1_original = lcom1_original;
        this.lcom2_original = lcom2_original;
        this.lcom3_original = lcom3_original;
        this.lcom4_original = lcom4_original;
        this.lcom5_original = lcom5_original;
        this.tcc_original = tcc_original;
        this.lcc_original = lcc_original;
        this.dcd_original = dcd_original;
        this.dci_original = dci_original;
        this.cc_original = cc_original;
        this.coh_original = coh_original;
        this.scom_original = scom_original;
        this.lscc_original = lscc_original;
    }

    public double[] getOriginalMetrics() {
        return new double[]{size_original, cob_original, lcom1_original, lcom2_original, lcom3_original, lcom4_original, lcom5_original,
            tcc_original, lcc_original, dcd_original, dci_original, cc_original, coh_original, scom_original, lscc_original};
    }

    public void addNewMetrics(int start_new, int end_new, int size_new,
            double cob_new, double lcom1_new, double lcom2_new,
            double lcom3_new, double lcom4_new, double lcom5_new,
            double tcc_new, double lcc_new, double dcd_new, double dci_new,
            double cc_new, double coh_new, double scom_new, double lscc_new) {
        this.start_line_new = start_new;
        this.end_line_new = end_new;
        this.size_new = size_new;
        this.cob_new = cob_new;
        this.lcom1_new = lcom1_new;
        this.lcom2_new = lcom2_new;
        this.lcom3_new = lcom3_new;
        this.lcom4_new = lcom4_new;
        this.lcom5_new = lcom5_new;
        this.tcc_new = tcc_new;
        this.lcc_new = lcc_new;
        this.dcd_new = dcd_new;
        this.dci_new = dci_new;
        this.cc_new = cc_new;
        this.coh_new = coh_new;
        this.scom_new = scom_new;
        this.lscc_new = lscc_new;

        this.hasNewMetrics = true;
    }

    public void setHasNewMetrics(boolean flag) {
        this.hasNewMetrics = flag;
    }

    public double getOpportunityCohesionByMetric(String metric_name) {
        switch (metric_name.toLowerCase()) {
            case "cob":
                return this.cob_cluster;
            case "lcom1":
                return this.lcom1_cluster;
            case "lcom2":
                return this.lcom2_cluster;
            case "lcom3":
                return this.lcom3_cluster;
            case "lcom4":
                return this.lcom4_cluster;
            case "lcom5":
                return this.lcom5_cluster;
            case "tcc":
                return this.tcc_cluster;
            case "lcc":
                return this.lcc_cluster;
            case "dcd":
                return this.dcd_cluster;
            case "dci":
                return this.dci_cluster;
            case "cc":
                return this.cc_cluster;
            case "coh":
                return this.coh_cluster;
            case "scom":
                return this.scom_cluster;
            case "lscc":
                return this.lscc_cluster;
            default:
                return -1000;
        }
    }

    public double getOpportunityOriginalCohesionByName(String metric_name) {
        switch (metric_name.toLowerCase()) {
            case "cob":
                return this.cob_original;
            case "lcom1":
                return this.lcom1_original;
            case "lcom2":
                return this.lcom2_original;
            case "lcom3":
                return this.lcom3_original;
            case "lcom4":
                return this.lcom4_original;
            case "lcom5":
                return this.lcom5_original;
            case "tcc":
                return this.tcc_original;
            case "lcc":
                return this.lcc_original;
            case "dcd":
                return this.dcd_original;
            case "dci":
                return this.dci_original;
            case "cc":
                return this.cc_original;
            case "coh":
                return this.coh_original;
            case "scom":
                return this.scom_original;
            case "lscc":
                return this.lscc_original;
            default:
                return -1000;
        }
    }

    public double getOpportunityDeltaMetricByName(String metric_name) {
        switch (metric_name.toLowerCase()) {
            case "cob":
                return this.metricsDeltaValues.get(0);
            case "lcom1":
                return this.metricsDeltaValues.get(1);
            case "lcom2":
                return this.metricsDeltaValues.get(2);
            case "lcom3":
                return this.metricsDeltaValues.get(3);
            case "lcom4":
                return this.metricsDeltaValues.get(4);
            case "lcom5":
                return this.metricsDeltaValues.get(5);
            case "tcc":
                return this.metricsDeltaValues.get(6);
            case "lcc":
                return this.metricsDeltaValues.get(7);
            case "dcd":
                return this.metricsDeltaValues.get(8);
            case "dci":
                return this.metricsDeltaValues.get(9);
            case "cc":
                return this.metricsDeltaValues.get(10);
            case "coh":
                return this.metricsDeltaValues.get(11);
            case "scom":
                return this.metricsDeltaValues.get(12);
            case "lscc":
                return this.metricsDeltaValues.get(13);
            default:
                return -1000;
        }
    }

    public void calculateBenefit() {
//        cob_benefit = Math.max(this.cob_new, cob_cluster) - cob_original;
        cob_benefit = cob_original - Math.max(this.cob_new, cob_cluster);
        lcom1_benefit = lcom1_original - Math.max(lcom1_new, lcom1_cluster);
        lcom2_benefit = lcom2_original - Math.max(lcom2_new, lcom2_cluster);
        lcom3_benefit = lcom3_original - Math.max(lcom3_new, lcom3_cluster);
        lcom4_benefit = lcom4_original - Math.max(lcom4_new, lcom4_cluster);
        lcom5_benefit = lcom5_original - Math.max(lcom5_new, lcom5_cluster);
        tcc_benefit = tcc_original-Math.max(this.tcc_new, tcc_cluster);
        lcc_benefit = lcc_original-Math.max(this.lcc_new, lcc_cluster);
        dcd_benefit = dcd_original-Math.max(this.dcd_new, dcd_cluster);
        dci_benefit = dci_original-Math.max(this.dci_new, dci_cluster);
        cc_benefit = cc_original-Math.max(this.cc_new, cc_cluster);
        coh_benefit = coh_original-Math.max(this.coh_new, coh_cluster);
        scom_benefit = scom_original-Math.max(this.scom_new, scom_cluster);
        lscc_benefit = lscc_original-Math.max(this.lscc_new, lscc_cluster);
//        tcc_benefit = Math.max(this.tcc_new, tcc_cluster) - tcc_original;
//        lcc_benefit = Math.max(this.lcc_new, lcc_cluster) - lcc_original;
//        dcd_benefit = Math.max(this.dcd_new, dcd_cluster) - dcd_original;
//        dci_benefit = Math.max(this.dci_new, dci_cluster) - dci_original;
//        cc_benefit = Math.max(this.cc_new, cc_cluster) - cc_original;
//        coh_benefit = Math.max(this.coh_new, coh_cluster) - coh_original;
//        scom_benefit = Math.max(this.scom_new, scom_cluster) - scom_original;
//        lscc_benefit = Math.max(this.lscc_new, lscc_cluster) - lscc_original;
    }

    public double getOpportunityBenefitMetricByName(String metric_name) {
        switch (metric_name.toLowerCase()) {
            case "cob":
                return cob_benefit;
            case "lcom1":
                return lcom1_benefit;
            case "lcom2":
                return lcom2_benefit;
            case "lcom3":
                return lcom3_benefit;
            case "lcom4":
                return lcom4_benefit;
            case "lcom5":
                return lcom5_benefit;
            case "tcc":
                return tcc_benefit;
            case "lcc":
                return lcc_benefit;
            case "dcd":
                return dcd_benefit;
            case "dci":
                return dci_benefit;
            case "cc":
                return cc_benefit;
            case "coh":
                return coh_benefit;
            case "scom":
                return scom_benefit;
            case "lscc":
                return lscc_benefit;
            default:
                return -1000;
        }
    }

    void resetOpportinityList() {
        this.cluster = new OpportunityList();
    }

    double lenghtPercentageDifference(Opportunity comparable_opp, double max_size_difference_percentage) {
        return Math.min(comparable_opp.getSizeCluster(), this.getSizeCluster()) * max_size_difference_percentage;
    }
    
    public void printBenefits(){
        DecimalFormat format = new DecimalFormat("#.####");
        format.setDecimalSeparatorAlwaysShown(false);

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%03d", start_line_cluster) + " to " + String.format("%03d", end_line_cluster) + separator);
        sb.append(size_cluster + separator);
        sb.append(format.format(cob_benefit) + separator);
        sb.append(format.format(lcom1_benefit) + separator);
        sb.append(format.format(lcom2_benefit) + separator);
        sb.append(format.format(lcom3_benefit) + separator);
        sb.append(format.format(lcom4_benefit) + separator);
        sb.append(format.format(lcom5_benefit) + separator);
        sb.append(format.format(tcc_benefit) + separator);
        sb.append(format.format(lcc_benefit) + separator);
        sb.append(format.format(dcd_benefit) + separator);
        sb.append(format.format(dci_benefit) + separator);
        sb.append(format.format(cc_benefit) + separator);
        sb.append(format.format(coh_benefit) + separator);
        sb.append(format.format(scom_benefit) + separator);
        sb.append(format.format(lscc_benefit));

        System.out.println(sb.toString());
    }
    @Override
	public int compareTo(Opportunity compareOpp) {
		// this.calcFinalFitness(); //***TEMPCOMMENT
		// compareIndiv.calcFinalFitness(); //***TEMPCOMMENT

		double compareNumber = compareOpp.getOpportunityBenefitMetricByName("lcom2");
		if (this.getOpportunityBenefitMetricByName("lcom2") > compareNumber) {
			return -1;
		} else if (this.getOpportunityBenefitMetricByName("lcom2") == compareNumber) {
			return 0;
		}
		return 1;

	}
}
