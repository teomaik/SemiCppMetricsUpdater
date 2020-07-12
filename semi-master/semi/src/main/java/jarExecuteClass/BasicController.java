package jarExecuteClass;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import AST.ClassParser;
import clustering.cluster.Opportunity;
import db.DbController;
import gui.Analyser;
import gui.MethodOppExtractor;
import gui.MethodOppExtractorSettings;
import parsers.CodeFile;
import parsers.cFile;
import parsers.fortranFile;
import splitlongmethod.JavaClass;
import splitlongmethod.Method;
import splitlongmethod.OppMethodList;

public class BasicController {

	DbController dbCon=null;

	private String projectName=null;
	private String projectProgramingLanguage=null;
	private String projectDirectoryPath=null;
	private String credPath=null;

	private ArrayList<JavaClass> classResults = new ArrayList<>();

	// Fortran, Fortran90, C, Cpp projects
	private ArrayList<CodeFile> projectFiles = new ArrayList<>();
	private ArrayList<CodeFile> cHeaderFiles = new ArrayList<>();

	// Java projects
	private Analyser analyser=null;
	JavaClass javaClass=null;
	public static boolean DebugMode = false;
	public MethodOppExtractorSettings extractor_settings=null;
	public static String cohesion_metric = "LCOM2";
	public static int cohesion_metric_index = 2;

	private int C_ProjectVersion;

	public BasicController(String type, String projectName, String C_ProjectVersion, String directoryPath, String dbCredPath) {
		if(type==null || type.isEmpty() || 
				projectName==null || projectName.isEmpty() ||
						C_ProjectVersion==null || C_ProjectVersion.isEmpty() || 
								directoryPath==null || directoryPath.isEmpty() ||
										dbCredPath==null || dbCredPath.isEmpty()) {
			return;
		}
		if (!type.equals("c") && !type.equals("cpp")) {
			return;
		}

		credPath = dbCredPath;
		this.C_ProjectVersion = Integer.valueOf(C_ProjectVersion);
		dbCon = new DbController(dbCredPath);
		if (!dbCon.isReady()) {
			return;
		}
		this.projectProgramingLanguage = type;
		this.projectName = projectName;
		this.projectDirectoryPath = directoryPath;
		this.extractor_settings = new MethodOppExtractorSettings();

		analyser = new Analyser();

	}

	private void promptEnterKey() {
		System.out.println("Press \"ENTER\" to continue...");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
	}

	public boolean runExperiment() {

		if(this.dbCon==null) {
			return false;
		}
		
		if (!this.dbCon.isReady()) {
			System.out.println("Problem with databaseConnection");
			return false;
		}

		dbCon.closeConn();

		boolean commit = true;

		switch (projectProgramingLanguage) {
		case "c":
			commit = doAnalysis_C_Cpp(projectDirectoryPath);
			System.out.println("doAnalysis_C_Cpp: "+commit);
			commit = commit && updatePrepareValues();
			System.out.println("updatePrepareValues: "+commit);
			break;
		case "cpp":
			commit = doAnalysis_C_Cpp(projectDirectoryPath);
			System.out.println("doAnalysis_C_Cpp: "+commit);
			commit = commit && updatePrepareValues();
			System.out.println("updatePrepareValues: "+commit);
			break;

		default:
			System.out.println("wrong argument for Programing Language (java, c, f, f90)");
		}

		if (!commit) {
			System.out.println("Something went wrong with the file analysis");
			return false;
		}
		//Toolkit.getDefaultToolkit().beep();
		//promptEnterKey(); //***DEBUG
		// System.out.println("moving on..."); //***DEBUG

		dbCon.getNewConnection(credPath);

		if (!this.dbCon.isReady()) {
			System.out.println("Problem with databaseConnection");

			deleteUsedFiles();
			return false;
		}
		
		if (projectProgramingLanguage.equals("c") || projectProgramingLanguage.equals("cpp")) {

			commit = commit && dbCon.dbActions(C_ProjectVersion); // TODO
		}

		if (commit) {

			dbCon.connCommitAndClose();
			System.out.println("FIN!");

			deleteUsedFiles();
			return true;
		} else {
			dbCon.connRollBackAndClose();

			deleteUsedFiles();
			return false;
		}
	}

	private String getMeCorrectNameFormat(String oldName) {
		String retName = oldName;
		String[] splited = projectDirectoryPath.split(File.separator);
		String baseDirectory = splited[splited.length - 1];
		retName = retName.replaceFirst(projectDirectoryPath, baseDirectory);

		return retName;
	}

	private boolean updatePrepareValues() {
		System.out.println("***RESULTS!!!");
		int fileNumber = classResults.size() + cHeaderFiles.size();

		boolean ret = false;
		double[][] metrics = new double[fileNumber][2]; // c0:Cohesion, c1:Coupling
		String[] classNames = new String[fileNumber];

		int fileIdx = 0;
		for (JavaClass clazz : this.classResults) {
			classNames[fileIdx] = getMeCorrectNameFormat(projectFiles.get(fileIdx).file.getAbsolutePath());
			OppMethodList methodList = clazz.getMethods();

			double coh = 0;
			double coup = 0;
			for (int i = 0; i < methodList.size(); i++) {
				String methodName = clazz.getMethods().get(i).getName();
				Method method = clazz.getMethods().getMethodByName(methodName);
				coh += method.getMetricIndexFromName("lcom2");
			}
			coup = projectFiles.get(fileIdx).fanOut;
			metrics[fileIdx][0] = coh;
			metrics[fileIdx][1] = coup;
			fileIdx++;
		}
		for (int i = 0; i < cHeaderFiles.size(); i++) {
			classNames[fileIdx] = getMeCorrectNameFormat(cHeaderFiles.get(i).file.getAbsolutePath());
			double coup = cHeaderFiles.get(i).fanOut;
			metrics[fileIdx][0] = 0;
			metrics[fileIdx][1] = coup;

			File fileDel = new File("./" + cHeaderFiles.get(i).file.getName() + "_parsed.txt");
			// ystem.out.println("./" + file.getName() + "_parsed.txt");
			fileDel.delete();

			fileIdx++;
		}

		ret = this.dbCon.updatePrepareValuesForDBCon(classNames, metrics, C_ProjectVersion, projectName);

		for (int i = 0; i < classNames.length; i++) {
			System.out.println(
					"File: " + classNames[i] + ", cohesion: " + metrics[i][0] + ", coupling: " + metrics[i][1]);
		}

//Toolkit.getDefaultToolkit().beep();		//***DEBUG
//promptEnterKey();						//***DEBUG
//System.out.println("moving on...");		//***DEBUG
		return ret;
	}

	ArrayList<String> fileNames = new ArrayList<String>();

	private void deleteUsedFiles() {
		try {
			for (String fName : this.fileNames) {

				File fileDel = new File("./" + fName);
				fileDel.delete();
			}
		} catch (Exception e) {
			System.out.println("Exception at deleteUsedFiles()");
		}
	}

	private boolean doAnalysis(File file) {
		// ***************************************************************************************************
		// <
		boolean ret = true;

		analyser.setFile(file);
		classResults.add(analyser.performAnalysis());

		System.out.println("***Ready to get parsed file: " + file.getName());

		fileNames.add(file.getName() + "_parsed.txt");
		// File fileDel = new File("./" + file.getName() + "_parsed.txt");
		// fileDel.delete();

		// >
		// ***************************************************************************************************
		return ret;
	}

	private boolean doAnalysis_C_Cpp(String directoryName) {
		File directory = new File(directoryName);
		if(!directory.exists() || !directory.isDirectory()) {
			return false;
		}
		return getFilesForAnalysis_C(projectDirectoryPath);
	}
	
	private boolean getFilesForAnalysis_C(String directoryName) {
		System.out.println("directory name = " + directoryName);
		boolean ret = true;
		File directory = new File(directoryName);
		// Get all files from a directory.
		File[] fList = directory.listFiles();
		if (fList != null) {
			for (File file : fList) {
				if (file.isFile() && file.getName().contains(".") && file.getName().charAt(0) != '.') {
					String[] str = file.getName().split("\\.");
					// For all the filles of this dirrecory get the extension
					if ((str[str.length - 1].equalsIgnoreCase("c")) || (str[str.length - 1].equalsIgnoreCase("cpp"))
							|| (str[str.length - 1].equalsIgnoreCase("cc"))
							|| (str[str.length - 1].equalsIgnoreCase("cp"))
							|| (str[str.length - 1].equalsIgnoreCase("cxx"))
							|| (str[str.length - 1].equalsIgnoreCase("c++"))
							|| (str[str.length - 1].equalsIgnoreCase("cu"))) {

						projectFiles.add(new cFile(file));

						// System.out.println("***DEBUG 'c' Parsing: "+file.getName());

						projectFiles.get(projectFiles.size() - 1).parse();
						// ***************************************************************************************************
						// <

						ret = ret && doAnalysis(file);

						// >
						// ***************************************************************************************************
					} else if ((str[str.length - 1].equalsIgnoreCase("h"))
							|| (str[str.length - 1].equalsIgnoreCase("hpp"))
							|| (str[str.length - 1].equalsIgnoreCase("hh"))
							|| (str[str.length - 1].equalsIgnoreCase("hp"))
							|| (str[str.length - 1].equalsIgnoreCase("hxx"))
							|| (str[str.length - 1].equalsIgnoreCase("h++"))
							|| (str[str.length - 1].equalsIgnoreCase("hcu"))) {
						cHeaderFiles.add(new cFile(file));
						// System.out.println("***DEBUG 'hpp' Parsing: "+file.getName());
						cHeaderFiles.get(cHeaderFiles.size() - 1).parse();
					}
				} else if (file.isDirectory()) {
					ret = ret && getFilesForAnalysis_C(file.getAbsolutePath());
				}
			}
		}

		return ret;
	}
}
