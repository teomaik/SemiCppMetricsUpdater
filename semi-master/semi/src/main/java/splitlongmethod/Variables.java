package splitlongmethod;

import java.util.ArrayList;

public class Variables {
	String name;
	ArrayList<Integer> lines = new ArrayList<Integer>();
	ArrayList<Integer> changed_values = new ArrayList<Integer>();
	String type;
	
	public Variables(String n, String t) {
		name = n;
		type = t;
		lines  = new ArrayList<Integer>();
		changed_values  = new ArrayList<Integer>();
	}

	public Variables(String n, ArrayList<Integer> l) {
		name = n;
		lines = l;
	}
	
	public ArrayList<Integer> getAccessedLines() {
		return changed_values;
	}
	
	public void setType(String s) {
		type = s;
	}
	
	public String getType() {
		return type;
	}
	
	public ArrayList<Integer> getLines() {
		return lines;
	}

	public void addAccessedLines(String line) {
		changed_values.add(Integer.parseInt(line));
	}
	
	public ArrayList<Integer> getLinesInInterval(int start, int end, int excluding, ArrayList<Integer> lines_not_calculated_in_metrics) {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i=0;i<lines.size();i++) {
			if (lines.get(i) <= end && lines.get(i) >= start && lines.get(i) != excluding && lines_not_calculated_in_metrics.contains(lines.get(i)) == false)
				temp.add(lines.get(i));
		}
		return temp;
	}
	
	public void sortLines() {
		for (int i=0; i<(lines.size()-1); i++) {
			for (int j=(i+1); j<lines.size(); j++) {
				if (lines.get(i) > lines.get(j)) {
					int temp = lines.get(i);
					lines.set(i, lines.get(j));
					lines.set(j, temp);
				}
			}
		}
	}
	
	public boolean checkIfUsedIn(int start, int end) {
		boolean temp = false;
		for (int i=0; i<lines.size(); i++) {
			if (lines.get(i)>= start && lines.get(i)<= end) {
				if (name.contains(".")==false) {
					temp = true;
					break;
				}
			}
		}
		return temp;
	}

	public boolean checkIfUsedInVariable(int start, int end, ArrayList<Integer> lines_not_calculated_in_metrics) {
		boolean temp = false;
		for (int i=0; i<lines.size(); i++) {
			if (lines.get(i)>= start && lines.get(i)<= end && !lines_not_calculated_in_metrics.contains(lines.get(i))) {
				if (type.equals("attribute")==true || type.equals("parameter")==true || type.equals("local_variable")==true ) {
					temp = true;
					break;
				}
			}
		}
		return temp;
	}

	public int useFrequencyInVariable(int start, int end, ArrayList<Integer> lines_not_calculated_in_metrics) {
		int temp = 0;
		for (int i=0; i<lines.size(); i++) {
			if (lines.get(i)>= start && lines.get(i)<= end && !lines_not_calculated_in_metrics.contains(lines.get(i))) {
				if (type.equals("attribute")==true || type.equals("parameter")==true || type.equals("local_variable")==true ) {
					temp++;
					//System.out.println("\t\t in line: " + lines.get(i));
				}
			}
		}
		return temp;
	}
		
	public boolean checkIfUsedInAll(int start, int end) {
		boolean temp = false;
		for (int i=0; i<lines.size(); i++) {
			if (lines.get(i)>= start && lines.get(i)<= end) {
				temp = true;
				break;
			}
		}
		return temp;
	}
		
	public int useFrequencyIn(int start, int end) {
		int temp = 0;
		for (int i=0; i<lines.size(); i++) {
			if (lines.get(i)>= start && lines.get(i)<= end) {
				if (name.contains(".")==false) {
					temp++;
				}
			}
		}
		return temp;
	}
	
	public void addLines(int line) {
		boolean found = false;
		for (int i=0; i<lines.size(); i++) {
			if (lines.get(i).equals(line)) {
				found = true;
				break;
			}
		}
		if (!found) lines.add(line);
	}
	
	public void print() {
		System.out.print("\t\tVariable: " + name + "(type: " + type+ "), accesses: ");
		for (int i=0;i<lines.size();i++) {
			System.out.print(lines.get(i).toString() + ";");
		}
		System.out.print(" and changed value: ");
		for (int i=0;i<changed_values.size();i++) {
			System.out.print(changed_values.get(i).toString() + ";");			
		}
		System.out.println("");
	}
	
	public boolean isUsedBefore(int start, int end) {
		boolean temp = false;
		for (int i=0; i<lines.size();i++) {
			if (lines.get(i) < start) {
				temp = true;
				break;
			}
		}
		return temp;
	}

	public boolean changedIn(int start, int end) {
		boolean temp = false;
		for (int i=start; i<=end; i++) {
			if (changed_values.contains(i)) {
				temp = true;
				//System.out.println("Variable " + name + " is assigned a value in line: " + i);
				break;
			}
		}
		return temp;
	}
	
	public boolean isUsedAfter(int start, int end) {
		boolean temp = false;
		for (int i=0; i<lines.size();i++) {
			if (lines.get(i) > end) {
				temp = true;
				break;
			}
		}
		return temp;
	}
	
	public ArrayList<Cluster> calculateClusters(int dist) {
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		if (name.equals("BREAK_FINAL") == false && name.equals("BREAK") == false && name.equals("IF") == false && name.equals("ELSE") == false && name.equals("{") == false && name.equals("}") == false && name.equals("null") == false) {
			for (int i = 0; i < lines.size() - 1; i++) {
				if (lines.get(i + 1) - lines.get(i) <= dist) {
					Cluster c = new Cluster(lines.get(i), lines.get(i + 1));
					clusters.add(c);
				}
			}
			boolean merged = true;
			while (merged) {
				merged = false;
				for (int i = 0; i < clusters.size() - 1; i++) {
					if (clusters.get(i).overlaps(clusters.get(i + 1))) {
						merged = true;
						clusters.set(i, new Cluster(clusters.get(i).getStart(),
								clusters.get(i + 1).getEnd()));
						clusters.remove(i + 1);
						continue;
					}
				}
			}
		}
		return clusters;
	}
	
	public String getName() {
		return name;
	}

	public boolean accesses(int i) {		
		return lines.contains(i);
	}

	public int firstOccurenceIn(int start, int end) {
		int first = 0;
		for (int i=0; i<lines.size();i++) {
			if (lines.get(i)>=start && lines.get(i)<=end) {
				first = lines.get(i);
				break;
			}
		}
		return first;
	}

	public int getNumberOfAccesses() {
		return lines.size();
	}

	public int getMaxLine() {
		return lines.get(lines.size()-1);
	}

	public boolean checkIfLineInInvalidFragement(int line, ArrayList<ArrayList<Integer>> invalid_lines) {
		
		boolean temp = false;
		
		for (int i=0;i<invalid_lines.size();i++) {
			ArrayList<Integer> invalid_lines_cluster = invalid_lines.get(i);
			for (int j=0;j<invalid_lines_cluster.size();j++) {
				if (line == invalid_lines_cluster.get(j)) {
					temp = true;
					break;
				}
			}
			if (temp == true) break;
		}		
		
		return temp;
	}
	
	public int getMinLine(ArrayList<ArrayList<Integer>> invalid_lines) {
		int temp = 99999999;
		
		for (int k=0; k<lines.size();k++) {
			if (checkIfLineInInvalidFragement(lines.get(k), invalid_lines)==false) {
				temp = lines.get(k);
				break;
			}
		}
				
		return temp;
	}	
	
}
