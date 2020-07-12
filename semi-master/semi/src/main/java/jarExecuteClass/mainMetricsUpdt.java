package jarExecuteClass;

public class mainMetricsUpdt {

	public static void main(String[] args) {
		//System.setProperty("java.awt.headless", "false");
		//System.out.println(java.awt.GraphicsEnvironment.isHeadless());

		if (args.length != 5) {
			System.out.println("Wrong number of arguments");
			System.out.println("You need to provide 5 arguments: " + "\n1: programming language (c, cpp)"
					+ "\n2: project name" + "\n3: project version" + "\n4: path to project directory"
					+ "\n5: path to database credential file");
		}
		
		if(!args[0].equals("c") && !args[0].equals("cpp")) {
			System.out.println("Wrong Programming language. \nYou gave: "+args[0]+", but this only accepts c / cpp");
			return;
		}
		
		BasicController controller = new BasicController(args[0], args[1], args[2], args[3], args[4]);
		boolean result = controller.runExperiment();

		if (result) {
			System.out.println("Executed correctly");
			System.exit(0);
		} else {
			System.out.println("There was an error");
			System.exit(1);
		}

	}

}
