package liner2.tools;

// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.IOException;
// import java.io.OutputStream;
// import java.io.OutputStreamWriter;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// import liner2.chunker.Chunker;
// import liner2.chunker.factory.ChunkerFactory;

// import liner2.reader.FeatureGenerator;
// import liner2.reader.ReaderFactory;
// import liner2.reader.StreamReader;

// import liner2.structure.AttributeIndex;
// import liner2.structure.Chunk;
// import liner2.structure.Chunking;
// import liner2.structure.Paragraph;
// import liner2.structure.Sentence;
// import liner2.structure.Token;

// import liner2.tools.ParameterException;
import liner2.LinerOptions;
import liner2.Main;

/**
 * Server class for liner2 WebService.
 * @author Maciej Janicki
 *
 */
public class LinerServer extends Thread {
	ShutdownThread shutdownThread = new ShutdownThread(this);
	Connection db_connection;
	String myId;
	ServerSocket serverSocket;
	int port = 0;
	
    public void run() {
    	Runtime.getRuntime().addShutdownHook(this.shutdownThread);
    	this.port = Integer.parseInt(LinerOptions.getOption(LinerOptions.OPTION_PORT));
    
    	// connect to the database
    	String db_host = LinerOptions.getOption(LinerOptions.OPTION_DB_HOST);
    	String db_port = LinerOptions.getOption(LinerOptions.OPTION_DB_PORT);
    	String db_user = LinerOptions.getOption(LinerOptions.OPTION_DB_USER);
    	String db_pass = LinerOptions.getOption(LinerOptions.OPTION_DB_PASSWORD);
    	String db_name = LinerOptions.getOption(LinerOptions.OPTION_DB_NAME);
    	Main.log("Connecting to database... ", false);
    	try {
    		connect(db_host, db_port, db_user, db_pass, db_name);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	Main.log("Done.", true);
    	
		Main.log("Listening on port: " + port, false);
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		while (!serverSocket.isClosed()) {
			Socket accepted;
			try {
				accepted = this.serverSocket.accept();
				Main.log("PING!", false);
				accepted.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    }

    public void connect(String host, String port, String user, String pass, String name)
    	throws SQLException, ClassNotFoundException {
    	Class.forName("com.mysql.jdbc.Driver");
		String addr = "jdbc:mysql://" + host + "/" + name + "?user=" + user + "&password=" + pass;
		this.db_connection = DriverManager.getConnection(addr);
		try {
			this.myId = InetAddress.getLocalHost().getHostName() + ":" + this.port;
			Main.log("My ID: " + this.myId, true);
			
			// write my id to the database
			Main.log("Registering daemon...", false);
			Statement statement = this.db_connection.createStatement();
			statement.executeQuery(String.format("CALL register_daemon(\"%s\")", this.myId));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
    }
    
    protected Connection getDbConnection() {
    	return db_connection;
    }
    
    protected String getMyId() {
    	return this.myId;
    }
    
    protected ServerSocket getServerSocket() {
    	return this.serverSocket;
    }
}

class ShutdownThread extends Thread {
	LinerServer linerServer;
	
	public ShutdownThread(LinerServer linerServer) {
		this.linerServer = linerServer;
	}
	
	public void run() {
		Main.log("Shutting down...", false);
		
		// close server socket
		ServerSocket socket = this.linerServer.getServerSocket();
		if (socket != null) {
			Main.log("Closing socket...", false);
			try {
				socket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		Connection connection = this.linerServer.getDbConnection();
		if (connection != null) {
			try {
				// remove my ID from the database
				Main.log("Unregistering daemon...", false);
				Statement statement = connection.createStatement();
				statement.executeQuery(String.format("CALL unregister_daemon(\"%s\")",
					this.linerServer.getMyId()));
				// close database connection
				Main.log("Closing database connection...", false);
				connection.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Main.log("Done.", true);
	}
}
