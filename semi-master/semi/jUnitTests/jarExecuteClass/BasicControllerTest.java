package jarExecuteClass;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BasicControllerTest {

	@Test
	void testBasicController() {
//BasicController(String type, String projectName, String C_ProjectVersion, String directoryPath, String dbCredPath)

		BasicController test = new BasicController("c", "testProject", "0", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("c", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("random_text", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("  ", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController(null, "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "   ", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", null, "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "   ", "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", null, "path/to/project/directory", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "3", "", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "3", "   ", "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "3", null, "path/to/db/credentials/file.txt");
		test = new BasicController("cpp", "testProject", "3", "path/to/project/directory", "");
		test = new BasicController("cpp", "testProject", "3", "path/to/project/directory", "   ");
		test = new BasicController("cpp", "testProject", "3", "path/to/project/directory", null);
	}

	@Test
	void testRunExperiment() {
		BasicController test = new BasicController("cpp", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		assertFalse(test.runExperiment());
		
		test = new BasicController("random_text", "testProject", "3", "path/to/project/directory", "path/to/db/credentials/file.txt");
		assertFalse(test.runExperiment());
	}

}
