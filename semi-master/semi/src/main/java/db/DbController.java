package db;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import com.mysql.cj.jdbc.MysqlDataSource;

import clustering.cluster.Opportunity;
import splitlongmethod.JavaClass;

public class DbController {

	String path = "";
	Connection conn = null;
	MysqlDataSource dataSource = null;
	String username = null;
	String password = null;
	String serverName = null;
	String databaseName = null;

	public int debCounter = 0;
	public int insertCounter = 0;
	public int updateCounter = 0;

	ArrayList<PreparedStatement> stmts = new ArrayList<PreparedStatement>();
	ArrayList<String> projectName = new ArrayList<String>();
	ArrayList<String> className = new ArrayList<String>();
	ArrayList<String> methodName = new ArrayList<String>();
	ArrayList<String> classPath = new ArrayList<String>();

	ArrayList<Integer> line_start = new ArrayList<Integer>();
	ArrayList<Integer> line_end = new ArrayList<Integer>();

	ArrayList<Double> cohesion_benefit = new ArrayList<Double>();
	ArrayList<Double> methodOriginalCohesion = new ArrayList<Double>();
	ArrayList<Double> LoC = new ArrayList<Double>();

	public DbController(String path) {
		this.path = path;
		conn = getConnection(path);
		if (conn == null) {
			System.out.println("Null Connection");
		} else {
			beginRUTransaction();
		}
	}

	public boolean isReady() {
		if (conn != null) {
			return true;
		}
		return false;
	}

	public void closeConn() {
		try {
			this.conn.close();
			this.conn=null;
		} catch (Exception e) {
			this.connRollBackAndClose();
		}

	}

	private String[] classNames;
	private int version;
	private double[][] metrics;
	String projName;

	public boolean updatePrepareValuesForDBCon(String[] classNames, double[][] metrics, int version, String projName) {
		
		if(version<0 || classNames==null || classNames.length==0 || metrics==null || metrics.length==0 || projName==null || projName.isEmpty()) {
			return false;
		}
		
		this.classNames = classNames;
		this.metrics = metrics;
		this.version = version;
		this.projName = projName;
		return true; // TODO more in the future, than just saving the values
	}

	int failedUpdates = 0;
	
	public boolean dbActions(int C_ProjectVersion) {
		if(!this.isReady() || C_ProjectVersion<0) {
			return false;
		}

		setVersion(C_ProjectVersion);
		return doUpdates();
	}
	
	private boolean doUpdates() {
		if(!this.isReady()) {
			return false;
		}

		try {
			for (int i = 0; i < classNames.length; i++) {	
				
				String query = "UPDATE cMetrics SET cohesion=?, coupling=? WHERE ? LIKE CONCAT(\"%\",cMetrics.class_name) AND version=? AND project_name=?;";

				// create the mysql insert preparedstatement
				PreparedStatement preparedStmt = conn.prepareStatement(query);
				preparedStmt.setDouble(1, metrics[i][0]);
				preparedStmt.setDouble(2, metrics[i][1]);
				preparedStmt.setString(3, classNames[i]);
				preparedStmt.setInt(4, version);
				preparedStmt.setString(5, projName);

				int res = preparedStmt.executeUpdate();
				updateCounter++;
				if (res <= 0) {
					System.out.println("***Thema, den egine to update gia to arxeio: "+classNames[i]);
					failedUpdates++;
				}
			}
			System.out.println("***DEBUG initialClasses:" + classNames.length + ", UpdatesExecuted:" + (updateCounter- this.failedUpdates));
			return true;
		} catch (Exception e) {
			System.out.println("Exception while updating table, with metrics");
			System.out.println("***DEBUG initialClasses:" + classNames.length + ", UpdatesExecuted:" + (updateCounter-this.failedUpdates));
			//this.connRollBackAndClose();
			return false;
		}

	}

	private boolean beginRUTransaction() { // READ_UNCOMMITTED_SQL_TRANSACTION
		if(!this.isReady()) {
			return false;
		}
		try {
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(conn.TRANSACTION_READ_UNCOMMITTED);
			return true;
		} catch (Exception e) {
			System.out.println("Could not start a Read_Unncommitted transaction");
			return false;
		}
	}

	public boolean connRollBackAndClose() {
		if(!this.isReady()) {
			return false;
		}
		try {
			conn.rollback();
			conn.close();

			System.out.println("Rolling back transaction");
			return true;
		} catch (SQLException exc) {
			System.out.println("Could not roll back transaction");
			exc.printStackTrace();
			return false;
		}
	}

	public boolean connCommitAndClose() {
		if(!this.isReady()) {
			return false;
		}
		try {
			conn.commit();
			conn.close();

			System.out.println("Commiting transaction");
			return true;
		} catch (SQLException exc) {
			System.out.println("Could not commit transaction");
			exc.printStackTrace();
			connRollBackAndClose();
			return false;
		}
	}

	public void getNewConnection(String path) {
		this.conn = getConnection(path);
		beginRUTransaction();
	}

	private Connection getConnection(String path) {
		
		if(path==null || path.isEmpty()) {
			return null;
		}
		
		boolean ok = false;

		try {
			ok = true;
			// BufferedReader reader = new BufferedReader(new FileReader(filename));
			// String line;

			File file = new File(path);
			
			if(!file.exists() || !file.isFile()) {
				return null;
			}
			
			Scanner input = new Scanner(new FileInputStream(file));

			boolean flag = input.hasNextLine();
			if (!input.hasNextLine()) {
				return null;
			}

			while (flag) {
				String line = input.nextLine();
				if (line.startsWith("username=")) {
					username = line.replaceFirst("username=", "");
					// username = line;
				} else if (line.startsWith("password=")) {
					password = line.replaceFirst("password=", "");
					// password = line;
				} else if (line.startsWith("serverName=")) {
					serverName = line.replaceFirst("serverName=", "");
					// serverName = line;
				} else if (line.startsWith("databaseName=")) {
					databaseName = line.replaceFirst("databaseName=", "");
					// databaseName = line;
					// "jdbc:mysql://"+serverName+"/"+line+ "?user=" +username + "&password=" +
					// password + "&useUnicode=true&characterEncoding=UTF-8";
				}
				flag = input.hasNextLine();
			}

			// if (username == null || password == null || serverName == null ||
			// databaseName == null) {
			if (serverName == null || databaseName == null) {

				ok = false;
			}
			if (!ok) {
				System.out.println("One or more of the Credentials given is null");
				return null;
			}

		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", path);
			e.printStackTrace();
			return null;
		}

		// <

		String url = "jdbc:mysql://" + serverName + "/" + databaseName + "";

		System.out.println("Connecting database...");

		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connected!");
			return connection;
		} catch (SQLException e) {
			System.out.println("Cannot connect the database!\n" + e.getMessage());
			return null;
		}
		// >

	}

	public int getVersion() {
		return version;
	}

	private void setVersion(int version) {
		this.version = version;
	}

}