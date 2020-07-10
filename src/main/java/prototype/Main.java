package prototype;

import java.io.IOException;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

	public static void main(String[] args) throws IOException, AclFormatException {

		// Start the HSQL database
		// -----------------------------------------------------------------------------------------
		// This has to be done before any other action to ensure that the DB server is
		// available. We are putting that here to avoid starting the database separately
		// outside of the Java process. However it would be easily possible to move this
		// out to the command line to run the database completely independent from that
		// Java application itself.
		// This will prevent the Java application from termination, i.e. the process
		// will run.

		HsqlProperties props = new HsqlProperties();
		// props.setProperty("server.database.0", "file:" + System.getenv("EXE_PATH") +
		// "/database/swlbuilder");
		props.setProperty("server.database.0", "file:" + "/database/prototype");
		props.setProperty("server.dbname.0", "prototype");
		org.hsqldb.Server dbServer = new org.hsqldb.Server();
		dbServer.setProperties(props);
		dbServer.start();

		// Initialize the Spring IoC container
		// -----------------------------------------------------------------------------------------

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/appctx.xml");

	}

}
