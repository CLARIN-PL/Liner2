package liner2.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

// import liner2.reader.FeatureGenerator;
import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

// import liner2.structure.AttributeIndex;
// import liner2.structure.Chunk;
// import liner2.structure.Chunking;
import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
// import liner2.structure.Token;

// import liner2.tools.ParameterException;
import liner2.LinerOptions;
import liner2.Main;

//import org.apache.commons.lang.StringEscapeUtils;

/**
 * Server class for liner2 WebService.
 * @author Maciej Janicki
 *
 */
public class LinerServer extends Thread {
	ShutdownThread shutdownThread = new ShutdownThread(this);
	Connection db_connection;
	Chunker chunker;
	String myId;
	ServerSocket serverSocket;
	int port = 0;
	
	@Override
    public void run() {
    	try {
    		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
    		this.chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    
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
				Main.log("Sleeping...", true);
				accepted = this.serverSocket.accept();
				accepted.close();
				Main.log("Woken up!", true);
				int reqid = searchForRequests();
				while (reqid > -1) {
					Main.log("Processing request with id: " + reqid, false);
					processRequest(reqid);
					Main.log("Request processing completed: " + reqid, false);
					reqid = searchForRequests();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
    }
    
    public void shutdown() {
    	try {
    		this.interrupt();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	Main.log("Shutting down...", false);
		
		// close server socket
		if (this.serverSocket != null) {
			Main.log("Closing socket...", false);
			try {
				this.serverSocket.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (this.db_connection != null) {
			try {
				// remove my ID from the database
				Main.log("Unregistering daemon...", false);
				Statement statement = this.db_connection.createStatement();
				statement.executeQuery(String.format("CALL unregister_daemon(\"%s\")",
					this.myId));
				// close database connection
				Main.log("Closing database connection...", false);
				this.db_connection.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		Main.log("Done.", true);
    }

    public void connect(String host, String port, String user, String pass, String name)
    	throws SQLException, ClassNotFoundException {
    	Class.forName("com.mysql.jdbc.Driver");
		String addr = "jdbc:mysql://" + host + "/" + name + "?user=" + user + "&password=" + pass +
			"&useUnicode=true&characterEncoding=UTF-8";
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
    
    private void processRequest(int requestId) throws SQLException, Exception {
    	Statement statement = this.db_connection.createStatement();
		statement.executeQuery(String.format("CALL start_processing(%d);", requestId));
		
		ResultSet resultSet = statement.executeQuery(String.format(
			"SELECT input_format, output_format FROM liner2_requests WHERE request_id = %d",
			requestId));
		String input_format = "iob", output_format = "iob";
		if (resultSet.next()) {
			input_format = resultSet.getString("input_format").toLowerCase();
			output_format = resultSet.getString("output_format").toLowerCase();
		}
		
		resultSet = statement.executeQuery(String.format(
			"SELECT text FROM liner2_requests_contents WHERE request_id = %d",
			requestId));
		String rawText;
		if (resultSet.next())
			rawText = resultSet.getString("text");
		else
			return;
				
		ByteArrayInputStream ins = new ByteArrayInputStream(rawText.getBytes());
		StreamReader reader = ReaderFactory.get().getStreamReader(ins, input_format);
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		StreamWriter writer = WriterFactory.get().getStreamWriter(ous, output_format);
		
		// process text
		ParagraphSet ps = reader.readParagraphSet();
		for (Paragraph p : ps.getParagraphs())
			for (Sentence s : p.getSentences())
				this.chunker.chunkSentenceInPlace(s);
		writer.writeParagraphSet(ps);
		
		// write results to the database
		rawText = ous.toString();
		PreparedStatement preparedStatement = this.db_connection.prepareStatement("CALL submit_result(?, ?);");
		preparedStatement.setInt(1, requestId);
		preparedStatement.setString(2, rawText);
		preparedStatement.executeUpdate();
    }
    
    private int searchForRequests() throws SQLException {
    	Statement statement = this.db_connection.createStatement();
    	ResultSet resultSet = statement.executeQuery(
    		"SELECT request_id FROM liner2_requests WHERE state =\'QUEUED\' LIMIT 1");
    	if (resultSet.next())
    		return resultSet.getInt("request_id");
    	else
    		return -1;
    }
	
	class ShutdownThread extends Thread {
		LinerServer linerServer;
	
		public ShutdownThread(LinerServer linerServer) {
			this.linerServer = linerServer;
		}
	
		public void run() {
			this.linerServer.shutdown();
		}
	}
}	
