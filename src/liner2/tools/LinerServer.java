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

import liner2.reader.ReaderFactory;
import liner2.reader.StreamReader;
import liner2.writer.StreamWriter;
import liner2.writer.WriterFactory;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;

import liner2.LinerOptions;
import liner2.Main;

/**
 * Server class for liner2 WebService.
 * @author Maciej Janicki
 *
 */
public class LinerServer extends Thread {
	ShutdownThread shutdownThread = new ShutdownThread(this);
	String db_host, db_port, db_user, db_pass, db_name;
	Connection db_connection;
	Chunker chunker;
	String myId;
	ServerSocket serverSocket;
	int port = 0;

	public LinerServer() {
		this.db_host = LinerOptions.getOption(LinerOptions.OPTION_DB_HOST);
		this.db_port = LinerOptions.getOption(LinerOptions.OPTION_DB_PORT);
		this.db_user = LinerOptions.getOption(LinerOptions.OPTION_DB_USER);
		this.db_pass = LinerOptions.getOption(LinerOptions.OPTION_DB_PASSWORD);
		this.db_name = LinerOptions.getOption(LinerOptions.OPTION_DB_NAME);
	}
	
	@Override
    public void run() {
		try {
		ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
		this.chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
    
    	Runtime.getRuntime().addShutdownHook(this.shutdownThread);
    	this.port = Integer.parseInt(LinerOptions.getOption(LinerOptions.OPTION_PORT));
    
    	// register daemon
		connect();
		this.myId = InetAddress.getLocalHost().getHostName() + ":" + this.port;
		Main.log("My ID: " + this.myId, true);
		Main.log("Registering daemon...", false);
		Statement statement = this.db_connection.createStatement();
		statement.executeQuery(String.format("CALL register_daemon(\"%s\")", this.myId));
		disconnect();

    	
		Main.log("Listening on port: " + port, false);
		this.serverSocket = new ServerSocket(this.port);
				
		while (!serverSocket.isClosed()) {
			Socket accepted;
			connect();
			int reqid = searchForRequests();
			while (reqid > -1) {
				Main.log("Processing request with id: " + reqid, false);
				processRequest(reqid);
				Main.log("Request processing completed: " + reqid, false);
				reqid = searchForRequests();
			}
			Main.log("Sleeping...", true);
			disconnect();
			accepted = this.serverSocket.accept();
			accepted.close();
			Main.log("Woken up!", true);
		}
		} catch (Exception ex) { ex.printStackTrace(); }
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
		
		try {
			if (this.db_connection.isClosed())
				connect();
			// remove my ID from the database
			Main.log("Unregistering daemon...", false);
			Statement statement = this.db_connection.createStatement();
			statement.executeQuery(String.format("CALL unregister_daemon(\"%s\")",
				this.myId));
			// close database connection
			disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Main.log("Done.", true);
    }

    public void connect() throws SQLException, ClassNotFoundException {
    	Class.forName("com.mysql.jdbc.Driver");
		String addr = "jdbc:mysql://" + this.db_host + "/" + this.db_name + 
			"?user=" + this.db_user + "&password=" + this.db_pass +
			"&useUnicode=true&characterEncoding=UTF-8";
		this.db_connection = DriverManager.getConnection(addr);
    }

	public void disconnect() throws SQLException {
		this.db_connection.close();
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

		// calculate stats
		int numTokens = 0, numSentences = 0, numParagraphs = 0, numChunks = 0;
		for (Paragraph p : ps.getParagraphs()) {
			numParagraphs++;
			for (Sentence s : p.getSentences()) {
				numSentences++;
				numTokens += s.getTokenNumber();
				numChunks += s.getChunks().size();
			}
		}
		
		// write results to the database
		rawText = ous.toString();
		PreparedStatement preparedStatement = this.db_connection.prepareStatement(
			"CALL submit_result(?, ?, ?, ?, ?, ?);");
		preparedStatement.setInt(1, requestId);
		preparedStatement.setString(2, rawText);
		preparedStatement.setInt(3, numTokens);
		preparedStatement.setInt(4, numSentences);
		preparedStatement.setInt(5, numParagraphs);
		preparedStatement.setInt(6, numChunks);
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
