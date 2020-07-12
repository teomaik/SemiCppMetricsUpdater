package AST;

import gui.LongMethodDetector;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 * @author Antonis Gkortzis (s2583070, antonis.gkortzis@gmail.com)
 */
public class ClassParser {

    private String filepath;
    private ClassVisitor visitor;

    public ClassParser(String filepath) {
        this.filepath = filepath;
    }

    public ArrayList<String> getOutput() {
        return this.visitor.getOutput();
    }

    public void parse() {
        if (LongMethodDetector.DebugMode) {
            System.out.print("Parsing file " + filepath + "..");
        }

        long start = System.currentTimeMillis();
        String sourcecode = "";
        final ArrayList<ArrayList<Integer>> invalid_lines = new ArrayList<ArrayList<Integer>>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int line_num = 1;

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());

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
                line = br.readLine();
                line_num++;
            }

            // adding first valid line in the invalid lines clusters
            for (int i = 0; i < invalid_lines.size(); i++) {
                ArrayList<Integer> invalid_lines_cluster = invalid_lines.get(i);
                int first_valid_line = invalid_lines_cluster.get(invalid_lines_cluster.size() - 1) + 1;
                invalid_lines_cluster.add(first_valid_line);
            }

            sourcecode = sb.toString();
            br.close();
        } catch (Exception e) {
            System.err.println(e);
        }

        visitor = new ClassVisitor(sourcecode);
        visitor.visit(invalid_lines);

        if (LongMethodDetector.DebugMode) {
            System.out.println("done in " + utils.Utilities.getDuration(start, System.currentTimeMillis()));
        }
    }
}
