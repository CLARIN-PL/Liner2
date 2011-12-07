package liner2.daemon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.sql.SQLException;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.tools.ParameterException;

import liner2.LinerOptions;
import liner2.Main;

/**
 * Daemon class for NER WebService.
 * @author Maciej Janicki
 */
public class LinerDaemon extends Thread {
	private static final int DEFAULT_MAX_THREADS = 1;

	String db_addr, myAddr, ip;
	int port, myId = -1;
	Database db;
	Chunker chunker;
	ServerSocket serverSocket;
	int numWorkingThreads, maxThreads;
	Vector<WorkingThread> workingThreads;
	
	public LinerDaemon() throws ParameterException {
		// setup database address
		String db_host = null, db_port = "3306", db_user = null,
			db_pass = "", db_name = null;

		// get access data from db_uri parameter
		String db_uri = LinerOptions.getOption(LinerOptions.OPTION_DB_URI);
		if (db_uri != null) {
			Pattern dbUriPattern = Pattern.compile("([^:@]*)(:([^:@]*))?@([^:@]*)(:([^:@]*))?/(.*)");
			Matcher dbUriMatcher = dbUriPattern.matcher(db_uri);
			if (dbUriMatcher.find()) {
				db_user = dbUriMatcher.group(1);
				if (dbUriMatcher.group(3) != null)
					db_pass = dbUriMatcher.group(3);
				db_host = dbUriMatcher.group(4);
				if (dbUriMatcher.group(6) != null)
					db_port = dbUriMatcher.group(6);
				db_name = dbUriMatcher.group(7);
			}
		}

		// overwrite with access data from db_* parameters
		if (LinerOptions.getOption(LinerOptions.OPTION_DB_HOST) != null)
			db_host = LinerOptions.getOption(LinerOptions.OPTION_DB_HOST);
		if (LinerOptions.getOption(LinerOptions.OPTION_DB_PORT) != null)
			db_port = LinerOptions.getOption(LinerOptions.OPTION_DB_PORT);
		if (LinerOptions.getOption(LinerOptions.OPTION_DB_USER) != null)
			db_user = LinerOptions.getOption(LinerOptions.OPTION_DB_USER);
		if (LinerOptions.getOption(LinerOptions.OPTION_DB_PASSWORD) != null)
			db_pass = LinerOptions.getOption(LinerOptions.OPTION_DB_PASSWORD);
		if (LinerOptions.getOption(LinerOptions.OPTION_DB_NAME) != null)
			db_name = LinerOptions.getOption(LinerOptions.OPTION_DB_NAME);

		if ((db_host == null) || (db_user == null) || (db_name == null))
			throw new ParameterException("Daemon mode: database access data required!");

		this.db_addr = "jdbc:mysql://" + db_host; 
		if (db_port != null)
			this.db_addr += ":" + db_port;
		this.db_addr += "/" + db_name;
		this.db_addr += "?user=" + db_user;
		if ((db_pass != null) && (!db_pass.isEmpty()))
			this.db_addr += "&password=" + db_pass;
		this.db_addr += "&useUnicode=true&characterEncoding=UTF-8";

		//this.db_addr = "jdbc:mysql://" + db_host + "/" + db_name +
		//	"?user=" + db_user + "&password=" + db_pass +
		//	"&useUnicode=true&characterEncoding=UTF-8";
		this.db = new Database(this.db_addr);

		// setup ip address and port number
		this.ip = LinerOptions.getOption(LinerOptions.OPTION_IP);
		if (this.ip == null)
			throw new ParameterException("Daemon mode: -ip (IP address) option is obligatory!");
		String optPort = LinerOptions.getOption(LinerOptions.OPTION_PORT);
		if (optPort == null)
			throw new ParameterException("Daemon mode: -p (port) option is obligatory!");
    	try {
			this.port = Integer.parseInt(optPort);
		} catch (NumberFormatException ex) {
			throw new ParameterException("Incorrect port number: " + optPort);
		}

		// setup maximum threads number
		this.maxThreads = this.DEFAULT_MAX_THREADS;
		String optMaxThreads = LinerOptions.getOption(LinerOptions.OPTION_MAX_THREADS);
		if (optMaxThreads != null) {
			try {
				this.maxThreads = Integer.parseInt(optMaxThreads);
			} catch (NumberFormatException ex) {
				throw new ParameterException("Incorrect maximum threads number: " + optMaxThreads);
			}
		}

		// setup working threads
		this.numWorkingThreads = 0;
		this.workingThreads = new Vector<WorkingThread>();
	}

	@Override
	public void run() {
		// load chunker
		try {
			ChunkerFactory.loadChunkers(LinerOptions.get().chunkersDescription);
			this.chunker = ChunkerFactory.getChunkerPipe(LinerOptions.getOption(LinerOptions.OPTION_USE));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    
    	Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { shutdown(); }});
    
    	// register daemon
		this.myAddr = this.ip + ":" + this.port;
//		try {
//			this.myAddr = InetAddress.getLocalHost().getHostName() + ":" + this.port;
//		} catch (UnknownHostException ex) {
//			ex.printStackTrace();
//		}
		Main.log("My address: " + this.myAddr, true);
		Main.log("Registering daemon...", false);
		try {
			this.db.connect();
			this.myId = this.db.registerDaemon(this.myAddr);
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		Main.log("Registered with id: " + this.myId, false);
    	
		// start listening for notifications
		Main.log("Listening on port: " + port, false);
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		for (int i = 0; i < this.maxThreads; i++) 
			startWorkingThread();
				
		while (!serverSocket.isClosed()) {
			try {
				Socket accepted = this.serverSocket.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(
					accepted.getInputStream()));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					accepted.getOutputStream()));

				String line = reader.readLine();
				if (line != null) {
					if (line.equals("PING")) {
						Main.log("Received PING!", true);
					}
					else if (line.equals("NOTIFY")) {
						Main.log("Received NOTIFY!", true);
						
						// start work in a new thread
						if (this.numWorkingThreads < this.maxThreads)
							startWorkingThread();
					}
					else {
						Main.log("Received something weird: " + line, true);
					}
					// respond to connection
					writer.write("OK");
					writer.newLine();
					writer.flush();
				}
				accepted.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public int getDaemonId() {
		return this.myId;
	}

	public synchronized void finishWorkingThread(WorkingThread callingThread) {
		synchronized (this.db) {
			try {
				this.db.connect();
				this.db.daemonReady(this.myId);
				this.db.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		this.numWorkingThreads--;
		this.workingThreads.remove(callingThread);
		if (this.numWorkingThreads == 0)
			Main.log("Sleeping...", false);
	}

	public void startWorkingThread() {
		synchronized (this.db) {
			try {
				this.db.connect();
				this.db.daemonNotReady(this.myId);
				this.db.disconnect();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (this.numWorkingThreads == 0)
			Main.log("Woke up!", false);
		this.numWorkingThreads++;
		WorkingThread newThread = new WorkingThread(this, this.chunker, this.db_addr);
		this.workingThreads.add(newThread);
		newThread.start();
	}
	
	public void shutdown() {
    	try {
			for (WorkingThread wt : this.workingThreads)
				wt.interrupt();
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
		
		// unregister daemon
		if (this.myId > -1) {
			try {
				this.db.connect();
				this.db.unregisterDaemon(this.myId);
				this.db.disconnect();
			} catch (SQLException ex) {
				ex.printStackTrace();
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			}
		}

		Main.log("Done.", false);
	}
}
