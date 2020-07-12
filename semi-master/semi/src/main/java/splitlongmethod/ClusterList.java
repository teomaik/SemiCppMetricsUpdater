package splitlongmethod;

import clustering.cluster.Opportunity;
import clustering.cluster.OpportunityList;
import java.util.ArrayList;

public class ClusterList {
	ArrayList<Cluster> cList = new ArrayList<Cluster>();
	ArrayList<Cluster> filteredOut = new ArrayList<Cluster>();
	ArrayList<Variables> vList = new ArrayList<Variables>();
	int methodSize;
	int methodEnd;
	
	public void addAll(ArrayList<Cluster> cl) {
		cList.addAll(cl);
	}
	
	/*
	public double avgCOB() {
		double temp = 0;
		int count = 0;
		for (int i=0; i<cList.size(); i++) {
			if (cList.get(i).calculateCOB(vList) >= 0) {
				temp = temp + cList.get(i).calculateCOB(vList);
				count++;
			}
		}
		return (temp / count);
	}

	public double minCOB() {
		double temp = 1.0;
		for (int i=0; i<cList.size(); i++) {
			if (cList.get(i).calculateCOB(vList) >= 0) {
				if (cList.get(i).calculateCOB(vList) < temp)
					temp = cList.get(i).calculateCOB(vList);
			}
		}
		return temp;
	} */
	
	
	public OpportunityList print(String method_name, ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> lines_for_else, Method m) {		
		//System.out.println("\n\t\tExtract Method Opportunities:\n");		
                OpportunityList opportunities = new OpportunityList();
		Cluster c1 = new Cluster(m.getMethodStart(invalid_lines), m.getMethodEnd());			
		String original_cluster = c1.printToString(m.getVariableList(), invalid_lines, lines_for_else, "noparams", "original");
                String[] original_metrics = c1.getMetrics();
                
		for (int i=0; i<cList.size(); i++) {
			//System.out.println("Candidate Methods Number:" + cList.size());
						
			System.out.print(original_cluster);
			
			cList.get(i).print(vList, invalid_lines, lines_for_else, "withparams", "cluster");
			String[] cluster_metrics = cList.get(i).getMetrics();
                        
			ArrayList<Integer> lines_of_cluster = new ArrayList<Integer>();			
			for (int j=cList.get(i).getStart(); j<=cList.get(i).getEnd(); j++) {
				lines_of_cluster.add(j);
			}
			ArrayList<ArrayList<Integer>> invalid_lines_temp =  new ArrayList<ArrayList<Integer>>();
			invalid_lines_temp.addAll(invalid_lines);
			invalid_lines_temp.add(lines_of_cluster);
			Cluster c = new Cluster(m.getMethodStart(invalid_lines_temp), m.getMethodEnd());				
			c.print(m.getVariableList(), invalid_lines_temp, lines_for_else, "noparams", "new");
                        String[] new_metrics = c.getMetrics();
                        //TODO make print return an Opportunity
                        Opportunity opp = parseMetrics(original_metrics, cluster_metrics, new_metrics);
                        System.out.println("## Opportunity : " + opp.getCsvOutput());
                        opportunities.add(opp);
                        
                }
		
		//System.out.println("\n\n\t\tExcluded Based on Filtering:\n");
		//for (int i=0; i<filteredOut.size(); i++) {
		//	filteredOut.get(i).print(vList, invalid_lines, lines_for_else, "withparams", "cluster");
		//}
               
                return opportunities;
	}
        
        public Opportunity parseMetrics(String[] original_metrics, String[] cluster_metrics, String[] new_metrics){
            Opportunity opp = new Opportunity();
            
//            System.out.println("\t#### Original Metrics");
//            for(String s : original_metrics)
//                System.out.println("\t\t " + s);
                        
//            System.out.println("\t#### Cluster Metrics");
//            for(String s : cluster_metrics)
//                System.out.println("\t\t " + s);
            
//            System.out.println("\t#### New Metrics");
//            for(String s : new_metrics)
//                System.out.println("\t\t " + s);
            
            //Cluster metrics
            int start_cluster = 	Integer.parseInt(cluster_metrics[0]);
            int end_cluster = 		Integer.parseInt(cluster_metrics[1]);
            int size_cluster = 		Integer.parseInt(cluster_metrics[2]);
            double cob_cluster = 	!cluster_metrics[3].equals("NaN") ? Double.parseDouble(cluster_metrics[3]) : -Double.MAX_VALUE;
            double lcom1_cluster = 	!cluster_metrics[4].equals("NaN") ? Double.parseDouble(cluster_metrics[4]) : -Double.MAX_VALUE;
            double lcom2_cluster = 	!cluster_metrics[5].equals("NaN") ? Double.parseDouble(cluster_metrics[5]) : -Double.MAX_VALUE;
            double lcom3_cluster = 	!cluster_metrics[6].equals("NaN") ? Double.parseDouble(cluster_metrics[6]) : -Double.MAX_VALUE;
            double lcom4_cluster = 	!cluster_metrics[7].equals("NaN") ? Double.parseDouble(cluster_metrics[7]) : -Double.MAX_VALUE;
            double lcom5_cluster = 	!cluster_metrics[8].equals("NaN") ? Double.parseDouble(cluster_metrics[8]) : -Double.MAX_VALUE;
            double tcc_cluster = 	!cluster_metrics[9].equals("NaN") ? Double.parseDouble(cluster_metrics[9]) : -Double.MAX_VALUE;
            double lcc_cluster = 	!cluster_metrics[10].equals("NaN") ? Double.parseDouble(cluster_metrics[10]) : -Double.MAX_VALUE;
            double dcd_cluster = 	!cluster_metrics[11].equals("NaN") ? Double.parseDouble(cluster_metrics[11]) : -Double.MAX_VALUE;
            double dci_cluster = 	!cluster_metrics[12].equals("NaN") ? Double.parseDouble(cluster_metrics[12]) : -Double.MAX_VALUE;
            double cc_cluster = 	!cluster_metrics[13].equals("NaN") ? Double.parseDouble(cluster_metrics[13]) : -Double.MAX_VALUE;
            double coh_cluster = 	!cluster_metrics[14].equals("NaN") ? Double.parseDouble(cluster_metrics[14]) : -Double.MAX_VALUE;
            double scom_cluster = 	!cluster_metrics[15].equals("NaN") ? Double.parseDouble(cluster_metrics[15]) : -Double.MAX_VALUE;
            double lscc_cluster = 	!cluster_metrics[16].equals("NaN") ? Double.parseDouble(cluster_metrics[16]) : -Double.MAX_VALUE;
            double comments_cluster =   !cluster_metrics[17].equals("NaN") ? Double.parseDouble(cluster_metrics[17]) : -Double.MAX_VALUE;
            double lines_alwaysZero_cluster = !cluster_metrics[18].equals("NaN") ? Double.parseDouble(cluster_metrics[18]) : -Double.MAX_VALUE; //to be renamed
            opp.addClusterMetrics(start_cluster, end_cluster, size_cluster, cob_cluster, lcom1_cluster, lcom2_cluster, lcom3_cluster, lcom4_cluster, lcom5_cluster, tcc_cluster, lcc_cluster, dcd_cluster, dci_cluster, cc_cluster, coh_cluster, scom_cluster, lscc_cluster, comments_cluster, lines_alwaysZero_cluster);
            
            //Original Metrics
            int start_original = 	Integer.parseInt(original_metrics[0]);
            int end_original = 		Integer.parseInt(original_metrics[1]);
            int size_original= 		Integer.parseInt(original_metrics[2]);
            double cob_original = 	!original_metrics[3].equals("NaN") ? Double.parseDouble(original_metrics[3]) : -Double.MAX_VALUE;
            double lcom1_original = 	!original_metrics[4].equals("NaN") ? Double.parseDouble(original_metrics[4]) : -Double.MAX_VALUE;
            double lcom2_original = 	!original_metrics[5].equals("NaN") ? Double.parseDouble(original_metrics[5]) : -Double.MAX_VALUE;
            double lcom3_original = 	!original_metrics[6].equals("NaN") ? Double.parseDouble(original_metrics[6]) : -Double.MAX_VALUE;
            double lcom4_original = 	!original_metrics[7].equals("NaN") ? Double.parseDouble(original_metrics[7]) : -Double.MAX_VALUE;
            double lcom5_original = 	!original_metrics[8].equals("NaN") ? Double.parseDouble(original_metrics[8]) : -Double.MAX_VALUE;
            double tcc_original = 	!original_metrics[9].equals("NaN") ? Double.parseDouble(original_metrics[9]) : -Double.MAX_VALUE;
            double lcc_original = 	!original_metrics[10].equals("NaN") ? Double.parseDouble(original_metrics[10]) : -Double.MAX_VALUE;
            double dcd_original = 	!original_metrics[11].equals("NaN") ? Double.parseDouble(original_metrics[11]) : -Double.MAX_VALUE;
            double dci_original = 	!original_metrics[12].equals("NaN") ? Double.parseDouble(original_metrics[12]) : -Double.MAX_VALUE;
            double cc_original = 	!original_metrics[13].equals("NaN") ? Double.parseDouble(original_metrics[13]) : -Double.MAX_VALUE;
            double coh_original = 	!original_metrics[14].equals("NaN") ? Double.parseDouble(original_metrics[14]) : -Double.MAX_VALUE;
            double scom_original = 	!original_metrics[15].equals("NaN") ? Double.parseDouble(original_metrics[15]) : -Double.MAX_VALUE;
            double lscc_original = 	!original_metrics[16].equals("NaN") ? Double.parseDouble(original_metrics[16]) : -Double.MAX_VALUE;
            opp.addOriginalMetrics(start_original, end_original, size_original, cob_original, lcom1_original, lcom2_original, lcom3_original, lcom4_original, lcom5_original, tcc_original, lcc_original, dcd_original, dci_original, cc_original, coh_original, scom_original, lscc_original);
                       
            //new metrics
            int start_new =     Integer.parseInt(new_metrics[0]);
            int end_new = 	Integer.parseInt(new_metrics[1]);
            int size_new = 	Integer.parseInt(new_metrics[2]);
            double cob_new = 	!new_metrics[3].equals("NaN") ? Double.parseDouble(new_metrics[3]) : -Double.MAX_VALUE;
            double lcom1_new = 	!new_metrics[4].equals("NaN") ? Double.parseDouble(new_metrics[4]) : -Double.MAX_VALUE;
            double lcom2_new = 	!new_metrics[5].equals("NaN") ? Double.parseDouble(new_metrics[5]) : -Double.MAX_VALUE;
            double lcom3_new = 	!new_metrics[6].equals("NaN") ? Double.parseDouble(new_metrics[6]) : -Double.MAX_VALUE;
            double lcom4_new = 	!new_metrics[7].equals("NaN") ? Double.parseDouble(new_metrics[7]) : -Double.MAX_VALUE;
            double lcom5_new = 	!new_metrics[8].equals("NaN") ? Double.parseDouble(new_metrics[8]) : -Double.MAX_VALUE;
            double tcc_new = 	!new_metrics[9].equals("NaN") ? Double.parseDouble(new_metrics[9]) : -Double.MAX_VALUE;
            double lcc_new = 	!new_metrics[10].equals("NaN") ? Double.parseDouble(new_metrics[10]) : -Double.MAX_VALUE;
            double dcd_new = 	!new_metrics[11].equals("NaN") ? Double.parseDouble(new_metrics[11]) : -Double.MAX_VALUE;
            double dci_new = 	!new_metrics[12].equals("NaN") ? Double.parseDouble(new_metrics[12]) : -Double.MAX_VALUE;
            double cc_new = 	!new_metrics[13].equals("NaN") ? Double.parseDouble(new_metrics[13]) : -Double.MAX_VALUE;
            double coh_new = 	!new_metrics[14].equals("NaN") ? Double.parseDouble(new_metrics[14]) : -Double.MAX_VALUE;
            double scom_new = 	!new_metrics[15].equals("NaN") ? Double.parseDouble(new_metrics[15]) : -Double.MAX_VALUE;
            double lscc_new = 	!new_metrics[16].equals("NaN") ? Double.parseDouble(new_metrics[16]) : -Double.MAX_VALUE;
            opp.addNewMetrics(start_new, end_new, size_new, cob_new, lcom1_new, lcom2_new, lcom3_new, lcom4_new, lcom5_new, tcc_new, lcc_new, dcd_new, dci_new, cc_new, coh_new, scom_new, lscc_new);
 
            opp.calculateDeltaMetrics();
            
            return opp;
        }
	
	public void removeDuplicates() {
		for (int i=0; i<cList.size()-1; i++) {
			//System.out.println("Checking for duplicates:" + i);
			for (int j=i+1; j<cList.size(); j++) {			
				if (cList.get(i).equalsTo(cList.get(j))) {
					cList.remove(j);
					j--;
				}
			}
		}		
	}
	
	public void sort() {
		for (int i=0; i<(cList.size()-1); i++) { 
			for (int j=i+1; j<cList.size(); j++) {
				if (cList.get(j).endBefore(cList.get(i))) {
					Cluster temp = cList.get(j);
					cList.set(j, cList.get(i));
					cList.set(i, temp);
				}
			}
		}
	}
	
	public void sortByEnding () {
		for (int i=0; i<cList.size()-1; i++) 
			for (int j=i+1; j<cList.size(); j++) {
				if ((cList.get(j).start == cList.get(i).start) && (cList.get(j).end < cList.get(i).end)) {
					Cluster temp = cList.get(j);
					cList.set(j, cList.get(i));
					cList.set(i, temp);
				}
			}
	}
	
	
	public void merge() {
		ArrayList<Cluster> cListTempMerged = new ArrayList<Cluster>();
		ArrayList<Cluster> cListTempOriginal = new ArrayList<Cluster>();

		for (int i=0; i<cList.size()-1; i++) {
			for (int j=i+1; j<cList.size(); j++) {
				if (cList.get(i).overlaps(cList.get(j))) {
					cListTempMerged.add(new Cluster(cList.get(i).getStart(), cList.get(j).getEnd()));
					cListTempOriginal.add(cList.get(i));
				}
			}
		}		
		
		cList.addAll(cListTempOriginal);
		cList.addAll(cListTempMerged);
	}
	
	public void transformToCodeFragements() {
		int i;
		cList.set(0, new Cluster(1, cList.get(0).getEnd()));
		for (i=1; i<cList.size();i++) {
			cList.set(i, new Cluster(cList.get(i-1).getEnd()+1, cList.get(i).getEnd()));
		}
		cList.add(new Cluster(cList.get(i-1).getEnd()+1, methodSize));		
	}

	public void setMethodSize(int m) {
		methodSize = m;		
	}
	
	
	public void setMethodEnd(int m) {
		methodEnd = m;
	}
	
	public int getMethodEnd() {
		return methodEnd;
	}
	
	public void setVariablesList(ArrayList<Variables> vl) {
		vList = vl;
	}

	public void addAll(ClusterList new_cl) {
		cList.addAll(new_cl.cList);
		vList = new_cl.vList;
		methodSize = new_cl.methodSize;
	}

	public void filter(int method_start, ArrayList<Integer> possible_invalid_bracket_close, ArrayList<ArrayList<Integer>> invalid_lines) {
		ArrayList<Cluster> temp = new ArrayList<Cluster>();
		ArrayList<Cluster> tobeadded = new ArrayList<Cluster>();
		int num_of_valid_lines = 0; 
		
		for (int i=0; i<cList.size(); i++) {
			//System.out.println("---------------------This method finishes at: " + methodEnd);
			Cluster current = new Cluster(cList.get(i).getStart(), cList.get(i).getEnd());
			
			//if (current.getStart() == 99 && current.getEnd() == 122) {
			//	System.out.println("aaaaaaaaaaaaaaaaaaaaaaa!!!!");
			//}
			
			num_of_valid_lines = current.get_num_of_valid_lines(invalid_lines);
			boolean shouldbeadded = cList.get(i).updateForEmptyLine(vList, methodEnd);
			if (cList.get(i).isValid(vList, method_start, possible_invalid_bracket_close) == true && num_of_valid_lines > 1) {
				temp.add(cList.get(i));
			} else {
				filteredOut.add(cList.get(i));
			}
			if (shouldbeadded) tobeadded.add(current);
		}
		
		cList.clear();
		cList.addAll(temp);
		
		temp = new ArrayList<Cluster>();
		for (int i=0; i<tobeadded.size(); i++) {
			if (tobeadded.get(i).isValid(vList, method_start, possible_invalid_bracket_close) == true) {
				temp.add(tobeadded.get(i));
			} else {
				filteredOut.add(tobeadded.get(i));
			}
		}
		
		cList.addAll(temp);
		
	}
}
