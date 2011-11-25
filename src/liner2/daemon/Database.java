package liner2.daemon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database interfacing class.
 * @author Maciej Janicki
 * TODO: table names as constants
 */
public class Database {
	String addr;
	boolean connected;
	Connection connection;

	public Database(String addr) {
		this.addr = addr;
	}

	public void connect() throws SQLException, ClassNotFoundException {
		if (this.connected)
			return;
		this.connected = true;
		Class.forName("com.mysql.jdbc.Driver");
		this.connection = DriverManager.getConnection(addr);
	}
	
	public void disconnect() throws SQLException {
		if (!this.connected)
			return;
		this.connected = false;
		this.connection.close();
	}
	
	public int registerDaemon(String address) throws SQLException {
		Statement statement = this.connection.createStatement();
		ResultSet resultSet = statement.executeQuery(String.format("CALL register_daemon(\"%s\");", address));
		if (resultSet.next())
			return resultSet.getInt("LAST_INSERT_ID()");
		else
			return -1;
	}

	public void unregisterDaemon(int id) throws SQLException {
		Statement statement = this.connection.createStatement();
		statement.executeQuery(String.format("CALL unregister_daemon(%d);", id));
	}

	public void daemonNotReady(int id) throws SQLException {
		Statement statement = this.connection.createStatement();
		statement.executeQuery("LOCK TABLES liner2_daemons AS read_liner2_daemons READ, " +
			"liner2_daemons WRITE;");
		statement.executeQuery(String.format("CALL daemon_not_ready(%d);", id));
		statement.executeQuery("UNLOCK TABLES;");
	}

	public void daemonReady(int id) throws SQLException {
		Statement statement = this.connection.createStatement();
		statement.executeQuery(String.format("CALL daemon_ready(%d);", id));
	}

	public Request getNextRequest(int daemonId) throws SQLException {
		Statement statement = this.connection.createStatement();
		statement.executeQuery("LOCK TABLES liner2_requests AS write_liner2_requests WRITE, "
			+ "liner2_requests AS read_liner2_requests READ;");
    	ResultSet resultSet = statement.executeQuery(
    		"SELECT request_id FROM liner2_requests AS read_liner2_requests WHERE state =\'QUEUED\' LIMIT 1");
		int requestId = -1;
    	if (resultSet.next())
    		requestId = resultSet.getInt("request_id");
    	else
    		return null;

		statement.executeQuery(String.format("CALL start_processing(%d, %d);", requestId, daemonId));
		statement.executeQuery("UNLOCK TABLES;");
		
		// TODO niepotrzebnie kolejne zapytanie
		resultSet = statement.executeQuery(String.format(
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
			// TODO throw exception
			return null;
		
		return new Request(requestId, input_format, output_format, rawText);
	}

	public void submitResult(Request request) throws SQLException {
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(
				"CALL submit_result(?, ?, ?, ?, ?, ?);");
			preparedStatement.setInt(1, request.getId());
			preparedStatement.setString(2, request.getText());
			preparedStatement.setInt(3, request.getNumTokens());
			preparedStatement.setInt(4, request.getNumSentences());
			preparedStatement.setInt(5, request.getNumParagraphs());
			preparedStatement.setInt(6, request.getNumChunks());
			preparedStatement.executeUpdate();
		} catch (Exception ex) {
			submitError(request.getId(), ex.getMessage());
		}
	}

	public void submitError(int requestId, String msg) throws SQLException {
		PreparedStatement preparedStatement = this.connection.prepareStatement(
			"CALL submit_error(?, ?);");
		preparedStatement.setInt(1, requestId);
		preparedStatement.setString(2, msg);
		preparedStatement.executeUpdate();
	}
}
