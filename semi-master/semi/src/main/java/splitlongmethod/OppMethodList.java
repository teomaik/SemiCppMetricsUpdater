/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitlongmethod;

import clustering.cluster.Opportunity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import splitlongmethod.Method;

/**
 *
 * @author angor
 */
public class OppMethodList {
    private ArrayList<Method> methods;
    
    public OppMethodList() {
         this.methods = new ArrayList<>();
    }
    
    public void add(Method method) {
        methods.add(method);
    }
    
    public Method getMethodByName(String name) {
        for(Method method : methods){
            if(method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public int size() {
        return this.methods.size();
    }
    
    public Method get(int index){
        return this.methods.get(index);
    }

    public void clusterOpportunities(int size_max_dif, int lines_max_difference, String comparison_metric_name, int comparison_metric_index, boolean deltaRun, double min_overlap, double sig_dif) {
        for (Method method : methods) {
            method.clusterOpportunities(size_max_dif, lines_max_difference, comparison_metric_name, comparison_metric_index, deltaRun, min_overlap, sig_dif);
        }
    }

//    void updateList(String metric, int metric_index) {
//        for (Method method : methods) {
//            method.updateList(metric, metric_index);
//        }
//    }

    public int getMethodIndex(String methodName) {
        String current_method_name = "";
        for(int index = 0; index<methods.size(); index++) {
            current_method_name = methods.get(index).getName().replace("(", " (").replace(",", ", ");
            if(current_method_name.equals(methodName))
                return index;
        }
        return -1;
    }
    
    public void sortMethodsOnMetric(final String metric) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(this.methods, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                double metric1 = o1.getMetricIndexFromName(metric);
                double metric2 = o2.getMetricIndexFromName(metric);
                
                if(metric.equals("size") || metric.equals("lcom1") || metric.equals("lcom2") || metric.equals("lcom4")){
                    metric1 = String.valueOf(metric1).equals("NaN") ? 0 : metric1;
                    metric2 = String.valueOf(metric2).equals("NaN") ? 0 : metric2;
                    return metric1 < metric2 ? 1 : -1;
                } else {
                    metric1 = String.valueOf(metric1).equals("NaN") ? 1.000001 : metric1;
                    metric2 = String.valueOf(metric2).equals("NaN") ? 1.000001 : metric2;
                    return metric1 > metric2 ? 1 : -1;
                }
            }
                
        });
    }
    
    public double getMinValueOfMetric(String metric) {
        double min = Double.MAX_VALUE;
        for(Method method : methods) {
            if(method.getMetricIndexFromName(metric) <= min) {
                min = method.getMetricIndexFromName(metric);
            }
        }
        return min;
    }
    
    public double getMaxValueOfMetric(String metric) {
        double max = -Double.MIN_VALUE;
        for(Method method : methods){
            if(method.getMetricIndexFromName(metric) >= max) {
                max = method.getMetricIndexFromName(metric);
            }
        }
        return max;
    }
}
