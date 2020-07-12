package splitlongmethod;

import clustering.cluster.Opportunity;
import java.util.ArrayList;
import java.util.Locale;

public class Cluster {
	int start;
	int end;
        int cluster_size;
	double cob;
	double lcom1;
	double lcom2;
	double lcom3;
	double lcom4;
	double lcom5;
	double tcc;
	double lcc;
	double dcd;
	double dci;
	double cc;
	double scom;
	double coh;
	double lscc;
	double in_params;
	double out_params;
        
        Opportunity opp;

	public Cluster(int s, int e) {
		start = s;
		end = e;
                
                opp = new Opportunity();
	}	
	
	public boolean endBefore(Cluster x) {
		if (start<x.start) return true;
		 else return false;
	}
	
	public boolean equalsTo(Cluster x) {
		boolean temp = false;
		if (start == x.start && end == x.end) temp = true;
		return temp;
	}

	
	public boolean linesAccessTheSameElement(int line1, int line2, ArrayList<Variables> vList) {
		boolean connected =false;
		for (int j=0; j<vList.size(); j++) {
			if (vList.get(j).getName().equals("{") || vList.get(j).getName().equals("}") || vList.get(j).getName().equals("SWITCH") || vList.get(j).getName().equals("IF") || vList.get(j).getName().equals("ELSE") || vList.get(j).getName().equals("BREAK") || vList.get(j).getName().equals("BREAK_FINAL") || vList.get(j).getName().equals("null")) {
				// do nothing
			} else {
				if (vList.get(j).accesses(line1) && vList.get(j).accesses(line2)) {
					connected = true;
					break;
				}
			}
		}
		return connected;
	}

	public boolean linesAccessTheSameVariable(int line1, int line2, ArrayList<Variables> vList) {
		boolean connected =false;
		for (int j=0; j<vList.size(); j++) {
			if (vList.get(j).getName().equals("{") || vList.get(j).getName().equals("}") || vList.get(j).getName().equals("SWITCH") || vList.get(j).getName().equals("IF") || vList.get(j).getName().equals("ELSE") || vList.get(j).getName().equals("BREAK") || vList.get(j).getName().equals("BREAK_FINAL") || vList.get(j).getName().equals("null")) {
				// do nothing
			} else {
				if (vList.get(j).getType().equals("attribute") || vList.get(j).getType().equals("local_variable") || vList.get(j).getType().equals("parameter")) {
					if (vList.get(j).accesses(line1) && vList.get(j).accesses(line2)) {
						connected = true;
						break;
					}
				}
			}
		}
		return connected;
	}
		
	public boolean checkForConnectionWithAGraphLCOM3(int line, ArrayList<Integer> currentGraph, ArrayList<Variables> vList) {
		boolean connected = false;	
		for (int i=0; i<currentGraph.size(); i++) {
			int currentLine = currentGraph.get(i);
			if (linesAccessTheSameVariable(line, currentLine, vList)) {
				connected =true;
				break;
			}
		}		
		return connected;
	}

	public boolean checkForConnectionWithAGraphLCOM4(int line, ArrayList<Integer> currentGraph, ArrayList<Variables> vList) {
		boolean connected = false;	
		for (int i=0; i<currentGraph.size(); i++) {
			int currentLine = currentGraph.get(i);
			if (linesAccessTheSameElement(line, currentLine, vList)) {
				connected = true;
				break;
			}
		}		
		return connected;
	}
	
	public boolean checkIfGraphsAreConnectedLCOM3(ArrayList<Integer> graph1, ArrayList<Integer> graph2, ArrayList<Variables> vList) {
		boolean connected = false;

		for (int i=0; i<graph1.size(); i++) {
			for (int j=0; j<graph2.size(); j++) {
				connected = linesAccessTheSameVariable(graph1.get(i), graph2.get(j), vList);
				if (connected) break;
			}
			if (connected) break;
		}
		
		return connected;
	}

	public boolean checkIfGraphsAreConnectedLCOM4(ArrayList<Integer> graph1, ArrayList<Integer> graph2, ArrayList<Variables> vList) {
		boolean connected = false;

		for (int i=0; i<graph1.size(); i++) {
			for (int j=0; j<graph2.size(); j++) {
				connected = linesAccessTheSameElement(graph1.get(i), graph2.get(j), vList);
				if (connected) break;
			}
			if (connected) break;
		}
		
		return connected;
	}	
	
	public ArrayList<ArrayList<Integer>> checkIfGraphsAreConnectedLCOM3(ArrayList<ArrayList<Integer>> graph, ArrayList<Variables> vList) {
		boolean connected = false;
		
		for (int i=0; i<(graph.size()-1); i++) {
			for (int j=i+1; j<graph.size(); j++) {
				ArrayList<Integer> graph1 = graph.get(i);
				ArrayList<Integer> graph2 = graph.get(j);
				connected = checkIfGraphsAreConnectedLCOM3(graph1, graph2, vList);
				if (connected) {
					ArrayList<Integer> temp = new ArrayList<Integer> ();
					temp.addAll(graph1);
					temp.addAll(graph2);
					graph.set(i, temp);
					graph.remove(j);
					j--;
				}				
			}
		}
		
		return graph;
	}

	public ArrayList<ArrayList<Integer>> checkIfGraphsAreConnectedLCOM4(ArrayList<ArrayList<Integer>> graph, ArrayList<Variables> vList) {
		boolean connected = false;
		
		for (int i=0; i<(graph.size()-1); i++) {
			for (int j=i+1; j<graph.size(); j++) {
				ArrayList<Integer> graph1 = graph.get(i);
				ArrayList<Integer> graph2 = graph.get(j);
				connected = checkIfGraphsAreConnectedLCOM4(graph1, graph2, vList);
				if (connected) {
					ArrayList<Integer> temp = new ArrayList<Integer> ();
					temp.addAll(graph1);
					temp.addAll(graph2);
					graph.set(i, temp);
					graph.remove(j);
					j--;
				}				
			}
		}
		
		return graph;
	}
		
	public int countFrequency(int line, ArrayList<Variables> vList, ArrayList<Integer> lines_for_else) {
		
		int count = 0;
		for (int j=0; j<vList.size(); j++) {
			if (vList.get(j).getName().equals("{") || vList.get(j).getName().equals("}") || vList.get(j).getName().equals("SWITCH") || vList.get(j).getName().equals("IF") || vList.get(j).getName().equals("ELSE") || vList.get(j).getName().equals("BREAK") || vList.get(j).getName().equals("BREAK_FINAL") || vList.get(j).getName().equals("null")) {
				// do nothing
			} else {
				if (vList.get(j).checkIfUsedInAll(line, line)) { 
					count++;
				}
			}				
		}
		
		for (int j=0; j<lines_for_else.size(); j++) {
			if (lines_for_else.get(j).equals(line))
				count--;
		}
		
		return count;
	}
	
	public double calculatesimilarityCC(int line1, int line2, ArrayList<Variables> vList) {
		double similarity ;
		double common = 0, unique = 0;
		
		//if (line1 == 143 && line2 == 144) {
		//System.out.println("\n\nCalculating Similarity between lines: " + line1 + " and " + line2);
		//}
		for (int i=0; i<vList.size(); i++) {
			Variables current = vList.get(i);
			if (current.getType().equals("attribute") || current.getType().equals("parameter") || current.getType().equals("local_variable")) {
				if (current.accesses(line1) && current.accesses(line2)) {
					common++;
					//System.out.println("Variable: " + current.getName() + " is common, Common: " + common);
				} else if (current.accesses(line1)) {
					unique++;
					//System.out.println("Variable: " + current.getName() + " is unique, Unique: " + unique);
				} else if (current.accesses(line2)) {
					unique++;
					//System.out.println("Variable: " + current.getName() + " is unique, Unique: " + unique);
				}
			}
		}
		
		if ((unique + common) == 0) {
			similarity = -1;
		} else {
			similarity = common / (unique + common);
		}
		//System.out.println("\t\t\tSimilarity = " + similarity);
		return similarity;
	}
	
	public double calculatesimilaritySCOM(int line1, int line2, ArrayList<Variables> vList) {
		double similarity ;
		double not_accessed = 0, common = 0, unique_line1 = 0, unique_line2 = 0, l = 0, min = 0;
		
		//if (line1 == 143 && line2 == 144) {
		//System.out.println("Calculating Similarity between lines: " + line1 + " and " + line2);
		//}
		for (int i=0; i<vList.size(); i++) {
			Variables current = vList.get(i);
			if (current.getType().equals("attribute") || current.getType().equals("parameter") || current.getType().equals("local_variable")) {
				if (current.accesses(line1) && current.accesses(line2)) {
					common++;
					//System.out.println("Variable: " + current.getName() + " is common, Common: " + common);
				} else if (current.accesses(line1)) {
					unique_line1++;
					//System.out.println("Variable: " + current.getName() + " is unique, Unique_line1: " + unique_line1);
				} else if (current.accesses(line2)) {
					unique_line2++;
					//System.out.println("Variable: " + current.getName() + " is unique, Unique_line2: " + unique_line2);
				} else {
					not_accessed++;
					//System.out.println("Variable: " + current.getName() + " is not accessed, Not accessed: " + not_accessed);
				}
			}
		}
		
		l = common + unique_line1 + unique_line2 + not_accessed;
		if (unique_line1 < unique_line2) {
			min = unique_line1 + common;
		} else {
			min = unique_line2 + common;
		}
		
		if (l == 0 || min == 0) {
			similarity = -1;
		} else {
			similarity = (common/min)*( (common + unique_line1 + unique_line2) / l);
		}
		//System.out.println("\t\t\tSimilarity = " + similarity);
		return similarity;
	}	
	
	public void calculateCC_SCOM(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		double sumCC = 0;
		double sumSCOM = 0;
		int pairs_CC = 0, pairs_SCOM = 0;
		
		for (int i=start; i<end; i++) {
			for (int j=i+1; j<=end; j++) {
				if (lines_not_calculated_in_metrics.contains(i) == false && lines_not_calculated_in_metrics.contains(j) == false) {
					pairs_CC++;
					pairs_SCOM++;
					double similarityCC = calculatesimilarityCC(i, j, vList); 
					double similaritySCOM = calculatesimilaritySCOM(i, j, vList); 
					
					if (similarityCC == -1)  {
						pairs_CC--;
					} else {
						sumCC = sumCC + similarityCC;
					}
					if (similaritySCOM == -1)  {
						pairs_SCOM--;
					} else {
						sumSCOM = sumSCOM + similaritySCOM;
					}
				}
			}
		}
		
		//System.out.println("pairs_CC = " + pairs_CC);
		//System.out.println("pairs_SCOM = " + pairs_SCOM);
		
		cc = sumCC / pairs_CC;
		scom = sumSCOM / pairs_SCOM;
	}

	public void calculateLSCC(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		// equals number of attributes
		int l = 0;
		// equals number of lines
		int k = 0;
		double sumLSCC = 0;
		
		for (int i=start; i<=end; i++) {
			if (lines_not_calculated_in_metrics.contains(i) == false) {			
				k++;
			}
		}
		
		for (int j=0; j<vList.size();j++) {
			Variables current = vList.get(j);
			if (current.getType().equals("attribute") || current.getType().equals("parameter") || current.getType().equals("local_variable")) {
				double x = vList.get(j).useFrequencyInVariable(start, end, lines_not_calculated_in_metrics); 
				//System.out.println("\t\t\t For variable: " + vList.get(j).getName() + ", x = " + x);
				if (x > 0) l++;
				sumLSCC = sumLSCC + (x * (x - 1));
			}
		}

		
		//System.out.println("Lines: " + k);
		//System.out.println("Variables Used: " + l);
		//System.out.println("sumLSCC: " + sumLSCC);
		if (k == 1 || k == 0|| l == 0) {
			lscc = -1;
		} else {
			lscc = sumLSCC / (l*k*(k-1));
		}
	}
		
	public void calculateTCC_LCC_DCD_DCI(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		double q_tight = 0, q_loose = 0, qm_tight = 0, qm_loose = 0,count= 0;		
		boolean common_tight = false, common_loose = false, common_tight_method = false, common_loose_method = false;
		
		for (int i=start; i<end; i++) {
			for (int j=i+1; j<=end; j++) {
				if (lines_not_calculated_in_metrics.contains(i) == false && lines_not_calculated_in_metrics.contains(j) == false) {
					count++;
					common_tight = false;
					common_loose = false;
					common_tight_method = false;
					common_loose_method = false;
					for (int k=0; k<vList.size();k++) {
						Variables current = vList.get(k);
						if (current.getType().equals("attribute") || current.getType().equals("parameter") || current.getType().equals("local_variable")) {
							if (current.accesses(i) && current.accesses(j)) {
								common_tight = true;
								//System.out.println("tight coupling between lines: " + i + " and " + j + " due to " + current.getName());
								//break;
							} else if (current.accesses(i)) {								
								// find all lines that access current + some additional variables
								ArrayList<Variables> additional_variables = new ArrayList<Variables>();
								ArrayList<Integer> lines_accessed_by_current = new ArrayList<Integer>();
								lines_accessed_by_current = current.getLinesInInterval(start, end, i, lines_not_calculated_in_metrics);
								for (int z=0; z<lines_accessed_by_current.size(); z++) {
									for (int q=0; q<vList.size();q++) {
										if (vList.get(q).getType().equals("attribute") || vList.get(q).getType().equals("parameter") || vList.get(q).getType().equals("local_variable")) {
											if (vList.get(q).accesses(lines_accessed_by_current.get(z))) {
												additional_variables.add(vList.get(q));
											}
										}
									}
									
								}
								// for all the additional variables, check if at least one is accessed in j
								for (int z=0; z<additional_variables.size(); z++) {
									if (additional_variables.get(z).accesses(j)) {
										common_loose = true;
										//System.out.println("loose coupling between lines: " + i + " and " + j + " due to " + current.getName() + " and " + additional_variables.get(z).getName());
										for (int m=0; m<lines_accessed_by_current.size(); m++) {
											if (additional_variables.get(z).accesses(lines_accessed_by_current.get(m))) {
												//System.out.println("\t\t\t Joint line: " + lines_accessed_by_current.get(m));
											}
										}
										break;										
									}
								}
							} else if (current.accesses(j)) {
								// find all lines that access current + some additional variables
								ArrayList<Variables> additional_variables = new ArrayList<Variables>();
								ArrayList<Integer> lines_accessed_by_current = new ArrayList<Integer>();
								lines_accessed_by_current = current.getLinesInInterval(start, end, j, lines_not_calculated_in_metrics);
								for (int z=0; z<lines_accessed_by_current.size(); z++) {
									for (int q=0; q<vList.size();q++) {
										if (vList.get(q).getType().equals("attribute") || vList.get(q).getType().equals("parameter") || vList.get(q).getType().equals("local_variable")) {
											if (vList.get(q).accesses(lines_accessed_by_current.get(z))) {
												additional_variables.add(vList.get(q));
											}
										}
									}
									
								}
								// for all the additional variables, check if at least one is accessed in j
								for (int z=0; z<additional_variables.size(); z++) {
									if (additional_variables.get(z).accesses(i)) {
										common_loose = true;
										//System.out.println("loose coupling between lines: " + i + " and " + j + " due to " + current.getName() + " and " + additional_variables.get(z).getName());
										for (int m=0; m<lines_accessed_by_current.size(); m++) {
											if (additional_variables.get(z).accesses(lines_accessed_by_current.get(m))) {
												//System.out.println("\t\t\t Joint line: " + lines_accessed_by_current.get(m));
											}
										}
										break;										
									}
								}
							}
						} else if (current.getType().equals("method_call")) {
							if (current.accesses(i) && current.accesses(j)) {
								common_tight_method = true;
								//System.out.println("tight coupling between lines by method: " + i + " and " + j + " due to " + current.getName());
								//break;
							} else if (current.accesses(i)) {								
								// find all lines that access current + some additional variables
								ArrayList<Variables> additional_variables = new ArrayList<Variables>();
								ArrayList<Integer> lines_accessed_by_current = new ArrayList<Integer>();
								lines_accessed_by_current = current.getLinesInInterval(start, end, i, lines_not_calculated_in_metrics);
								for (int z=0; z<lines_accessed_by_current.size(); z++) {
									for (int q=0; q<vList.size();q++) {
										if (vList.get(q).accesses(lines_accessed_by_current.get(z))) {
											if (vList.get(q).getType().equals("method_call")) additional_variables.add(vList.get(q));
										}
									}
									
								}
								// for all the additional variables, check if at least one is accessed in j
								for (int z=0; z<additional_variables.size(); z++) {
									if (additional_variables.get(z).accesses(j)) {
										common_loose_method = true;
										//System.out.println("loose coupling between lines by method: " + i + " and " + j + " due to " + current.getName() + " and " + additional_variables.get(z).getName());
										for (int m=0; m<lines_accessed_by_current.size(); m++) {
											if (additional_variables.get(z).accesses(lines_accessed_by_current.get(m))) {
												//System.out.println("\t\t\t Joint line: " + lines_accessed_by_current.get(m));
											}
										}
										break;										
									}
								}
							} else if (current.accesses(j)) {
								// find all lines that access current + some additional variables
								ArrayList<Variables> additional_variables = new ArrayList<Variables>();
								ArrayList<Integer> lines_accessed_by_current = new ArrayList<Integer>();
								lines_accessed_by_current = current.getLinesInInterval(start, end, j, lines_not_calculated_in_metrics);
								for (int z=0; z<lines_accessed_by_current.size(); z++) {
									for (int q=0; q<vList.size();q++) {
										if (vList.get(q).accesses(lines_accessed_by_current.get(z))) {
											if (vList.get(q).getType().equals("method_call")) additional_variables.add(vList.get(q));
										}
									}
									
								}
								// for all the additional variables, check if at least one is accessed in j
								for (int z=0; z<additional_variables.size(); z++) {
									if (additional_variables.get(z).accesses(i)) {
										common_loose_method = true;
										//System.out.println("loose coupling between lines by method: " + i + " and " + j + " due to " + current.getName() + " and " + additional_variables.get(z).getName());
										for (int m=0; m<lines_accessed_by_current.size(); m++) {
											if (additional_variables.get(z).accesses(lines_accessed_by_current.get(m))) {
												//System.out.println("\t\t\t Joint line: " + lines_accessed_by_current.get(m));
											}
										}
										break;										
									}
								}
							}
						}
					}
					if (common_tight == true) {
						q_tight++;
						q_loose++;
						qm_tight++;
						qm_loose++;
					} else if (common_loose == true) {
						q_loose++;
						qm_loose++;						
					} else if (common_tight_method == true) {
						qm_tight++;
						qm_loose++;
					} else if (common_loose_method == true) {
						qm_loose++;						
					}
				}
			}
		}		
		//System.out.println("count: " + count + " q_tight: " + q_tight);
		//System.out.println("count: " + count + " q_loose: " + q_loose);
		//System.out.println("count: " + count + " qm_tight: " + qm_tight);
		//System.out.println("count: " + count + " qm_loose: " + qm_loose);
		
		tcc = q_tight / count;
		lcc = q_loose / count;
		dcd = qm_tight / count;
		dci = qm_loose / count;
	}	
	
	public void calculateLCOM3_LCOM4(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		
		ArrayList<ArrayList<Integer>> connectedGraphsLCOM3 = new ArrayList<ArrayList<Integer>> ();  
		ArrayList<ArrayList<Integer>> connectedGraphsLCOM4 = new ArrayList<ArrayList<Integer>> ();
		int j = 0;
		
		//if (start == 245 && end == 256) {
		//	System.out.println("aaaaaaaaaaaaaaa");
		//}
		
		for (int i=start; i<=end; i++) {
			if (lines_not_calculated_in_metrics.contains(i) == false) {
				boolean connectedToAGraph = false;
				for (j=0; j<connectedGraphsLCOM3.size();j++) {
					ArrayList<Integer> currentGraph = connectedGraphsLCOM3.get(j);
					connectedToAGraph = checkForConnectionWithAGraphLCOM3(i, currentGraph, vList);
					if (connectedToAGraph) break;
				}
				if (connectedToAGraph) {
					connectedGraphsLCOM3.get(j).add(i);
					connectedGraphsLCOM3 = checkIfGraphsAreConnectedLCOM3(connectedGraphsLCOM3, vList);
				} else {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(i);
					connectedGraphsLCOM3.add(temp);
				}
			}
		}

		for (int i=start; i<=end; i++) {
			if (lines_not_calculated_in_metrics.contains(i) == false) {
				boolean connectedToAGraph = false;
				for (j=0; j<connectedGraphsLCOM4.size();j++) {
					ArrayList<Integer> currentGraph = connectedGraphsLCOM4.get(j);
					connectedToAGraph = checkForConnectionWithAGraphLCOM4(i, currentGraph, vList);
					if (connectedToAGraph) break;
				}
				if (connectedToAGraph) {
					connectedGraphsLCOM4.get(j).add(i);
					connectedGraphsLCOM4 = checkIfGraphsAreConnectedLCOM4(connectedGraphsLCOM4, vList);
				} else {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(i);
					connectedGraphsLCOM4.add(temp);
				}
			}
		}
		
		//for (int i=0; i<connectedGraphsLCOM3.size();i++) {
		//	System.out.println("Connected Graph: " + (i+1));
		//	for (int k=0; k<connectedGraphsLCOM3.get(i).size(); k++) {
		//		System.out.println("\t\tConnected Graph: " + connectedGraphsLCOM3.get(i).get(k));
		//	}
		//}

		//System.out.println("\n\n\n\n");
		
		//for (int i=0; i<connectedGraphsLCOM4.size();i++) {
		//	System.out.println("Connected Graph: " + (i+1));
		//	for (int k=0; k<connectedGraphsLCOM4.get(i).size(); k++) {
		//		System.out.println("\t\tConnected Graph: " + connectedGraphsLCOM4.get(i).get(k));
		//	}
		//}		
		
		lcom3 = connectedGraphsLCOM3.size();
		lcom4 = connectedGraphsLCOM4.size();
	}

	public void calculateLCOM1_LCOM2(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		int p = 0;
		int q = 0;		
		boolean common = false;
		
		for (int i=start; i<end; i++) {
			for (int j=i+1; j<=end; j++) {
				if (lines_not_calculated_in_metrics.contains(i) == false && lines_not_calculated_in_metrics.contains(j) == false) {
					common = false;
					for (int k=0; k<vList.size();k++) {
						Variables current = vList.get(k);
						if (current.getType().equals("attribute") || current.getType().equals("parameter") || current.getType().equals("local_variable")) {
							if (current.accesses(i) && current.accesses(j)) {
								//System.out.println("Variable " + current.getName() + " is common in lines: " + i + " and " + j);
								common = true;
								break;
							}
						}
					}
					if (common == false) {
						p++;					
					} else {
						q++;
					}
				}
			}
		}		
				
		//System.out.println("Disconnected Pairs: " + p);
		//System.out.println("Connected Pairs: " + q);
		lcom1 = p;
		
		if (p > q) { 
			p = p - q;
		} else {
			p = 0;
		}
		
		lcom2 = p; 
	}

	public void calculateParams(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		in_params = 0;		
		out_params = 0;
		
		//System.out.println("\n\n");
		for (int i=0; i<vList.size(); i++) {
			Variables current = vList.get(i);
			if (current.getType().equals("local_variable")) {
				if (current.checkIfUsedIn(start, end)) {
					//System.out.println("\t\t\tLocal variable: " + current.getName() + " is used (POTENTIAL IN_PARAM)");
					if (current.isUsedBefore(start,end) == false) {
						//System.out.println("\t\t\t\tLocal Variable: " + current.getName() + " can be defined locally");												
					} else {
						in_params++;
						//System.out.println("\t\t\t\tNeeds in-parameter: " + current.getName() + " to compile");						
					}
					if (current.isUsedAfter(start, end) && current.changedIn(start, end)) {
						out_params++;
						//System.out.println("\t\t\t\tNeeds out-parameter: " + current.getName() + " to compile");					
					}					
				}
			} else if (current.getType().equals("parameter")) {
				if (current.checkIfUsedIn(start, end)) {
					//System.out.println("\t\t\tparameter: " + current.getName() + " is used (POTENTIAL IN_PARAM)");
					in_params++;
					//System.out.println("\t\t\t\tNeeds in-parameter: " + current.getName() + " to compile");
					if (current.isUsedAfter(start, end) && current.changedIn(start, end)) {
						out_params++;
						//System.out.println("\t\t\t\tNeeds out-parameter: " + current.getName() + " to compile");					
					}
				}				
			}
		}
	}
	
	public void calculateCOB_LCOM5_COH(ArrayList<Variables> vList, ArrayList<Integer> lines_not_calculated_in_metrics) {
		double v = 0; // number of variables that are used in the cluster
		double b = end-start+1 - find_invalid_lines_for_cluster(end, start, lines_not_calculated_in_metrics); // LOC of cluster
		double sum = 0; // counts how many times each variables is used in the cluster
		
		//if (start == 562 && end == 565) {
		//	System.out.println("aaaaaaaaaaaa");
		//}
		
		for (int i=0; i<vList.size(); i++) {
			if (vList.get(i).checkIfUsedInVariable(start, end, lines_not_calculated_in_metrics)) {
				v++;
				//System.out.println("variable:" + vList.get(i).getName() + " used " + vList.get(i).useFrequencyInVariable(start, end, lines_not_calculated_in_metrics) + " times");
				sum = sum + vList.get(i).useFrequencyInVariable(start, end, lines_not_calculated_in_metrics);
			}
		}
		cob = sum/(v*b);
		//System.out.println("sum: " + sum + " number of variables: " + v + " number of lines: " + b);
		lcom5 = (sum- (b*v)) / (v - (b*v));
		coh = 1 - (1 - (1/b)) * lcom5;
	}
	
	public int find_invalid_lines_for_cluster(int end, int start, ArrayList<Integer> lines_not_calculated_in_metrics) {
		int temp =0;
		for (int i=start; i<=end; i++) {
			if (lines_not_calculated_in_metrics.contains(i)) {
				temp++;
				//System.out.println(i);
			}
		}
		
		//System.out.println("invalid lines in " + start + "," + end + ": " + temp);		
		return temp;
	}

    public void print(ArrayList<Variables> vList, ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> lines_for_else, String opt, String type) {
        System.out.println("## HELLO !!!! Print ##");//FIXME DEBUG		
        ArrayList<Integer> lines_not_calculated_in_metrics = new ArrayList<Integer>();
        
        // In the "lines_not_calculated_in_metrics" arrayList we add all lines that are invalid (empty, comments,lines split in multiple ones)
        for (int i=0; i<invalid_lines.size();i++) {
                ArrayList<Integer> invalid_cluster = invalid_lines.get(i);
                for (int j=0; j<(invalid_cluster.size()-1); j++) {
                        lines_not_calculated_in_metrics.add(invalid_cluster.get(j));
                }
        }

        // In the "lines_not_calculated_in_metrics" arrayList we add lines that include only "{", "}" and "null"
        //if (start == 174 && end == 282) {
        //	System.out.println("aaaaaaaaaaaaa");
        //}
        for (int i=start; i<=end; i++) {
                //if (i == 235) {
                //	System.out.println("aaaaaaaaaaaaa");
                //}
                for (int j=0; j<vList.size(); j++) {
                        if (vList.get(j).getName().equals("{")) {
                                if (vList.get(j).checkIfUsedInAll(i, i)) {
                                        if (countFrequency(i, vList, lines_for_else) < 1) {
                                                lines_not_calculated_in_metrics.add(i);
                                        }
                                }
                        }
                        if (vList.get(j).getName().equals("}")) {
                                if (vList.get(j).checkIfUsedInAll(i, i)) {
                                        if (countFrequency(i, vList, lines_for_else) < 1) {
                                                lines_not_calculated_in_metrics.add(i);
                                        }
                                }					
                        }
                        if (vList.get(j).getName().equals("null")) {
                                if (vList.get(j).checkIfUsedInAll(i, i)) {
                                        if (countFrequency(i, vList, lines_for_else) < 1) {
                                                lines_not_calculated_in_metrics.add(i);
                                        }
                                }					
                                break;
                        }
                }
        }		

        if (opt.equals("withparams")) calculateParams(vList, lines_not_calculated_in_metrics);
        calculateCOB_LCOM5_COH(vList, lines_not_calculated_in_metrics);
        calculateLCOM1_LCOM2(vList, lines_not_calculated_in_metrics);
        calculateLCOM3_LCOM4(vList, lines_not_calculated_in_metrics);
        calculateTCC_LCC_DCD_DCI(vList, lines_not_calculated_in_metrics);
        calculateCC_SCOM(vList, lines_not_calculated_in_metrics);
        calculateLSCC(vList, lines_not_calculated_in_metrics);

        int size = end-start+1-find_invalid_lines_for_cluster(end, start, lines_not_calculated_in_metrics);

        if (type.equals("cluster")) {
                size++;
        }

        if (size > 1) {

                if (type.equals("original")) {
//				System.out.print("\t\t\t");
                }

                if (opt.equals("withparams"))  {
                        System.out.print("[lines_" + type+ ": " + String.format("%03d", start) + ", " + String.format("%03d", end) + "] [size_" + type+ ": " + String.format("%02d", size) + "] [cob_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cob ) 
                        + "] [lcom1_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom1 ) + "] [lcom2_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom2 ) + "] [lcom3_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom3 )
                        + "] [lcom4_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom4 ) + "] [lcom5_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom5 ) + "] [tcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", tcc ) 
                        + "] [lcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcc) + "] [dcd_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dcd ) + "] [dci_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dci )
                        + "] [cc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cc) + "] [scom_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", scom ) + "] [coh_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", coh )
                        + "] [lscc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lscc) + "] [in_params_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", in_params) + "] [out_params_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", out_params) + "]");
                } else {
                        System.out.print("[lines_" + type+ ": " + String.format("%03d", start) + ", " + String.format("%03d", end) + "] [size_" + type+ ": " + String.format("%02d", size) + "] [cob_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cob ) 
                                + "] [lcom1_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom1 ) + "] [lcom2_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom2 ) + "] [lcom3_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom3 )
                                + "] [lcom4_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom4 ) + "] [lcom5_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom5 ) + "] [tcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", tcc ) 
                                + "] [lcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcc) + "] [dcd_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dcd ) + "] [dci_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dci )
                                + "] [cc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cc) + "] [scom_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", scom ) + "] [coh_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", coh )
                                + "] [lscc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lscc) + "]");
                }

        } 
        if (type.equals("new")) {
                System.out.println();
        }
        
        this.cluster_size = size;
} 
    
    public String[] getMetrics() {
        String[] metrics = new String[19];
        
        metrics[0] = String.format("%03d", start);
        metrics[1] = String.format("%03d", end);
        metrics[2] = String.format("%02d", cluster_size);
        metrics[3] = String.format(Locale.ENGLISH, "%.4f", cob);
        metrics[4] = String.format(Locale.ENGLISH, "%.4f", lcom1);
        metrics[5] = String.format(Locale.ENGLISH, "%.4f", lcom2);
        metrics[6] = String.format(Locale.ENGLISH, "%.4f", lcom3);
        metrics[7] = String.format(Locale.ENGLISH, "%.4f", lcom4);
        metrics[8] = String.format(Locale.ENGLISH, "%.4f", lcom5);
        metrics[9] = String.format(Locale.ENGLISH, "%.4f", tcc);
        metrics[10] = String.format(Locale.ENGLISH, "%.4f", lcc);
        metrics[11] = String.format(Locale.ENGLISH, "%.4f", dcd);
        metrics[12] = String.format(Locale.ENGLISH, "%.4f", dci);
        metrics[13] = String.format(Locale.ENGLISH, "%.4f", cc);
        metrics[14] = String.format(Locale.ENGLISH, "%.4f", coh);
        metrics[15] = String.format(Locale.ENGLISH, "%.4f", scom);
        metrics[16] = String.format(Locale.ENGLISH, "%.4f", lscc);
        metrics[17] = String.format(Locale.ENGLISH, "%.4f", in_params);
        metrics[18] = String.format(Locale.ENGLISH, "%.4f", out_params);
        
        return metrics;
    }
        
/*	public void print(ArrayList<Variables> vList, ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> lines_for_else, String opt, String type) {
		System.out.println("## HELLO !!!! Print ##");//FIXME DEBUG		
		ArrayList<Integer> lines_not_calculated_in_metrics = new ArrayList<Integer>();

		// In the "lines_not_calculated_in_metrics" arrayList we add all lines that are invalid (empty, comments,lines split in multiple ones)
		for (int i=0; i<invalid_lines.size();i++) {
			ArrayList<Integer> invalid_cluster = invalid_lines.get(i);
			for (int j=0; j<(invalid_cluster.size()-1); j++) {
				lines_not_calculated_in_metrics.add(invalid_cluster.get(j));
			}
		}
		
		// In the "lines_not_calculated_in_metrics" arrayList we add lines that include only "{", "}" and "null"
		//if (start == 174 && end == 282) {
		//	System.out.println("aaaaaaaaaaaaa");
		//}
		for (int i=start; i<=end; i++) {
			//if (i == 235) {
			//	System.out.println("aaaaaaaaaaaaa");
			//}
			for (int j=0; j<vList.size(); j++) {
				if (vList.get(j).getName().equals("{")) {
					if (vList.get(j).checkIfUsedInAll(i, i)) {
						if (countFrequency(i, vList, lines_for_else) < 1) {
							lines_not_calculated_in_metrics.add(i);
						}
					}
				}
				if (vList.get(j).getName().equals("}")) {
					if (vList.get(j).checkIfUsedInAll(i, i)) {
						if (countFrequency(i, vList, lines_for_else) < 1) {
							lines_not_calculated_in_metrics.add(i);
						}
					}					
				}
				if (vList.get(j).getName().equals("null")) {
					if (vList.get(j).checkIfUsedInAll(i, i)) {
						if (countFrequency(i, vList, lines_for_else) < 1) {
							lines_not_calculated_in_metrics.add(i);
						}
					}					
					break;
				}
			}
		}		
		
		if (opt.equals("withparams")) calculateParams(vList, lines_not_calculated_in_metrics);
		calculateCOB_LCOM5_COH(vList, lines_not_calculated_in_metrics);
		calculateLCOM1_LCOM2(vList, lines_not_calculated_in_metrics);
		calculateLCOM3_LCOM4(vList, lines_not_calculated_in_metrics);
		calculateTCC_LCC_DCD_DCI(vList, lines_not_calculated_in_metrics);
		calculateCC_SCOM(vList, lines_not_calculated_in_metrics);
		calculateLSCC(vList, lines_not_calculated_in_metrics);
		
		int size = end-start+1-find_invalid_lines_for_cluster(end, start, lines_not_calculated_in_metrics);
		
		if (type.equals("cluster")) {
			size++;
		}
		
		if (size > 1) {
			
			if (type.equals("original")) {
//				System.out.print("\t\t\t");
			}
			
			if (opt.equals("withparams"))  {
				System.out.print("[lines_" + type+ ": " + String.format("%03d", start) + ", " + String.format("%03d", end) + "] [size_" + type+ ": " + String.format("%02d", size) + "] [cob_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cob ) 
				+ "] [lcom1_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom1 ) + "] [lcom2_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom2 ) + "] [lcom3_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom3 )
				+ "] [lcom4_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom4 ) + "] [lcom5_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom5 ) + "] [tcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", tcc ) 
				+ "] [lcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcc) + "] [dcd_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dcd ) + "] [dci_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dci )
				+ "] [cc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cc) + "] [scom_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", scom ) + "] [coh_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", coh )
				+ "] [lscc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lscc) + "] [in_params_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", in_params) + "] [out_params_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", out_params) + "]");
			} else {
				System.out.print("[lines_" + type+ ": " + String.format("%03d", start) + ", " + String.format("%03d", end) + "] [size_" + type+ ": " + String.format("%02d", size) + "] [cob_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cob ) 
					+ "] [lcom1_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom1 ) + "] [lcom2_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom2 ) + "] [lcom3_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom3 )
					+ "] [lcom4_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom4 ) + "] [lcom5_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom5 ) + "] [tcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", tcc ) 
					+ "] [lcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcc) + "] [dcd_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dcd ) + "] [dci_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dci )
					+ "] [cc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cc) + "] [scom_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", scom ) + "] [coh_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", coh )
					+ "] [lscc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lscc) + "]");
			}
			
		} 
		if (type.equals("new")) {
			System.out.println();
		}
	}
*/
	public String printToString(ArrayList<Variables> vList, ArrayList<ArrayList<Integer>> invalid_lines, ArrayList<Integer> lines_for_else, String opt, String type) {
//		          System.out.println("## PrintToString ##");//FIXME DEBUG
		String temp ="";
		
		ArrayList<Integer> lines_not_calculated_in_metrics = new ArrayList<Integer>();

		// In the "lines_not_calculated_in_metrics" arrayList we add all lines that are invalid (empty, comments,lines split in multiple ones)
		for (int i=0; i<invalid_lines.size();i++) {
			ArrayList<Integer> invalid_cluster = invalid_lines.get(i);
			for (int j=0; j<(invalid_cluster.size()-1); j++) {
				lines_not_calculated_in_metrics.add(invalid_cluster.get(j));
			}
		}
		
		// In the "lines_not_calculated_in_metrics" arrayList we add lines that include only "{", "}" and "null"
		//if (start == 174 && end == 282) {
		//	System.out.println("aaaaaaaaaaaaa");
		//}
		for (int i = start; i <= end; i++) {
			//if (i == 235) {
                //	System.out.println("aaaaaaaaaaaaa");
                //}
                for (int j = 0; j < vList.size(); j++) {
                    if (vList.get(j).getName().equals("{")) {
                        if (vList.get(j).checkIfUsedInAll(i, i)) {
                            if (countFrequency(i, vList, lines_for_else) < 1) {
                                lines_not_calculated_in_metrics.add(i);
                            }
                        }
                    }
                    if (vList.get(j).getName().equals("}")) {
                        if (vList.get(j).checkIfUsedInAll(i, i)) {
                            if (countFrequency(i, vList, lines_for_else) < 1) {
                                lines_not_calculated_in_metrics.add(i);
                            }
                        }
                    }
                    if (vList.get(j).getName().equals("null")) {
                        if (vList.get(j).checkIfUsedInAll(i, i)) {
                            if (countFrequency(i, vList, lines_for_else) < 1) {
                                lines_not_calculated_in_metrics.add(i);
                            }
                        }
                        break;
                    }
                }
            }
		
		if (opt.equals("withparams")) calculateParams(vList, lines_not_calculated_in_metrics);
		calculateCOB_LCOM5_COH(vList, lines_not_calculated_in_metrics);
		calculateLCOM1_LCOM2(vList, lines_not_calculated_in_metrics);
		calculateLCOM3_LCOM4(vList, lines_not_calculated_in_metrics);
		calculateTCC_LCC_DCD_DCI(vList, lines_not_calculated_in_metrics);
		calculateCC_SCOM(vList, lines_not_calculated_in_metrics);
		calculateLSCC(vList, lines_not_calculated_in_metrics);
		
		int size = end-start+1-find_invalid_lines_for_cluster(end, start, lines_not_calculated_in_metrics);
		
		if (type.equals("cluster")) {
			size++;
		}
		
		if (size > 1) {
			
			if (type.equals("original")) {
//				System.out.print("\t\t\t");
			}
			
			if (opt.equals("withparams"))  {
				temp = "[lines_" + type+ ": " + String.format("%03d", start) + ", " + String.format("%03d", end) + "] [size_" + type+ ": " + String.format("%02d", size) + "] [cob_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cob ) 
				+ "] [lcom1_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom1 ) + "] [lcom2_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom2 ) + "] [lcom3_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom3 )
				+ "] [lcom4_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom4 ) + "] [lcom5_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom5 ) + "] [tcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", tcc ) 
				+ "] [lcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcc) + "] [dcd_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dcd ) + "] [dci_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dci )
				+ "] [cc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cc) + "] [scom_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", scom ) + "] [coh_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", coh )
				+ "] [lscc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lscc) + "] [in_params_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", in_params) + "] [out_params_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", out_params) + "]";
			} else {
				temp = "[lines_" + type+ ": " + String.format("%03d", start) + ", " + String.format("%03d", end) + "] [size_" + type+ ": " + String.format("%02d", size) + "] [cob_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cob ) 
					+ "] [lcom1_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom1 ) + "] [lcom2_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom2 ) + "] [lcom3_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom3 )
					+ "] [lcom4_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom4 ) + "] [lcom5_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcom5 ) + "] [tcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", tcc ) 
					+ "] [lcc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lcc) + "] [dcd_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dcd ) + "] [dci_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", dci )
					+ "] [cc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", cc) + "] [scom_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", scom ) + "] [coh_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", coh )
					+ "] [lscc_" + type+ ": " + String.format(Locale.ENGLISH, "%.4f", lscc) + "]";
			}
			
		} 
		if (type.equals("new")) {
			temp = "";
		}
                
		this.cluster_size = size;
                
		return temp;
		
	}
	
	
	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}	
	
	public boolean overlaps(Cluster x) {
		boolean temp;
		if (end >= x.start) temp = true;
			else temp =false; 
		return temp;
	}

	public boolean isValid(ArrayList<Variables> vList, int method_start, ArrayList<Integer> possible_invalid_bracket_close) {
		boolean temp = true;
		int openPending = 0, pendingIF = 0;

		//if (start == 18 && end == 85) {
		//	System.out.println("Checking for validity!!!!");
		//}		
		
		if (start < method_start) {
			temp =false;
		}
		
		
		          if (start == end) {
                temp = false;
            }
            //System.out.print("Checking: " + start + "," + end + ": ");
            for (int j = start; j <= end; j++) {
                for (int i = 0; i < vList.size(); i++) {
                    if (vList.get(i).getName().equals("{")) {
                        if (vList.get(i).checkIfUsedIn(j, j) == true) {
                            openPending = openPending + vList.get(i).useFrequencyIn(j, j);
                        }
                    } else if (vList.get(i).getName().equals("}")) {
                        if (vList.get(i).checkIfUsedIn(j, j) == true) {
                            if (j < end) {
                                openPending = openPending - vList.get(i).useFrequencyIn(j, j);
                            } else {
                                if (openPending > 0) {
                                    openPending = openPending - vList.get(i).useFrequencyIn(j, j);
                                } else {
                                    // elgxoume an einai stin lista me ta agkistra pou kleinoun k yparxei kodikas stin idia grammi: } x=3;
                                    if (possible_invalid_bracket_close.contains(j)) {
                                        temp = false;
                                        break;
                                    }
                                }
                            }
                            if (openPending < 0) {
                                temp = false;
                                break;
                            }
                        }
                        break;
                    } else {
                        // does nothing - just testing testAST
                    }
                }
            }

		          //System.out.println(openPending);
            if (openPending > 0) {
                temp = false;
                //System.out.println(" Excluded because of {}");
            }

            for (int j = start; j <= end; j++) {
                for (int i = 0; i < vList.size(); i++) {
                    if (vList.get(i).getName().equals("IF")) {
                        if (vList.get(i).checkIfUsedIn(j, j) == true) {
                            pendingIF = pendingIF + vList.get(i).useFrequencyIn(j, j);
                        }
                    } else if (vList.get(i).getName().equals("ELSE")) {
                        if (vList.get(i).checkIfUsedIn(j, j) == true) {
                            //pendingIF = pendingIF - vList.get(i).useFrequencyIn(j, j);
                            if (pendingIF <= 0) {
                                temp = false;
                            }
                        }
                    } else if (vList.get(i).getName().equals("BREAK_FINAL")) {
                        if (vList.get(i).checkIfUsedIn(j, j) == true) {
                            if (j < end) {
                                pendingIF = pendingIF - vList.get(i).useFrequencyIn(j, j);
                            } else {
                                if (pendingIF > 0) {
                                    pendingIF = pendingIF - vList.get(i).useFrequencyIn(j, j);
                                }
                            }
                            if (pendingIF < 0) {
                                temp = false;
                                break;
                            }
                        }
                        break;
                    } else {
                        // does nothing - just testing testAST
                    }
                }
            }	
		
		// check if cluster includes only an else or catch statement
		boolean startsWithElse = false;		
		for (int i = 0; i < vList.size(); i++) {
			if (vList.get(i).getName().equals("ELSE")) {
				startsWithElse = vList.get(i).checkIfUsedIn(start, start);
				//if (startsWithElse) System.out.println(" starts with else");
				break;
			}
		}
	
		if (startsWithElse) temp = false;
		
		return temp;
	}
	
    public boolean pendingClosingBrackets(ArrayList<Variables> vList) {
        int openPending = 0, closePending = 0, total;
        boolean temp;

        for (int i = 0; i < vList.size(); i++) {
            if (vList.get(i).getName().equals("{")) {
                if (vList.get(i).checkIfUsedIn(start, end) == true) {
                    openPending = openPending + vList.get(i).useFrequencyIn(start, end);
                }
            } else if (vList.get(i).getName().equals("}")) {
                if (vList.get(i).checkIfUsedIn(start, end) == true) {
                    closePending = closePending + vList.get(i).useFrequencyIn(start, end);
                    break;
                }
            }
        }

        total = openPending - closePending;
        if (total > 0) {
            temp = true;
        } else {
            temp = false;
        }

        return temp;
    }
	
	public boolean updateForEmptyLine(ArrayList<Variables> vList, int methodEnd) {
		boolean endInEmptyLine = true;
		boolean shouldKeepOld = false;
		boolean found = false;

		//if (start == 121 && end == 267) {
		//	System.out.println("Checking for update!!!!");
		//}
		
		while (endInEmptyLine == true && end < methodEnd) {
			endInEmptyLine = false;
			found = false;
			for (int i=0; i<vList.size(); i++) {
				if (vList.get(i).accesses(end+1) == true) {
					found = true;
					if (vList.get(i).getName().equals("null") || vList.get(i).getName().equals("{")) {
						endInEmptyLine = true;
						if (vList.get(i).getName().equals("null")) shouldKeepOld = true;
						break;
					} else if (vList.get(i).getName().equals("}") && pendingClosingBrackets(vList)==true) {
						endInEmptyLine = true;
						shouldKeepOld = false;
						break;
					}
				} 
			}
			if (found==false) {
				endInEmptyLine = true;				
			}
			if (endInEmptyLine == true && end < methodEnd) {
				//System.out.println("Updated line: " + end + " to line " + (end+1));
				end = end+1;
			}
		}
		
		return shouldKeepOld;
	}

	public int get_num_of_valid_lines(ArrayList<ArrayList<Integer>> invalid_lines) {		
		int temp = 0;
		boolean flag =false;
		
 		//if (start == 186 && end == 191) {
 		//	System.out.println("");
 		//}
		
		for (int i=start; i<=end; i++) {
			flag = false;
			for (int j=0; j<invalid_lines.size(); j++) {
				ArrayList<Integer> invalid_cluster = invalid_lines.get(j);
				for (int k=0; k<(invalid_cluster.size()-1); k++) {
					if (invalid_cluster.get(k) == i) {
						flag = true;
						temp++;
						break;
					}
				}
				if (flag) break;
			}
		}
		
		temp = end - start + 1 - temp;
		return temp;
	}
	
}
