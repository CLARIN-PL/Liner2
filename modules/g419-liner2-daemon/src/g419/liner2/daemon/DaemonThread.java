package g419.liner2.daemon;


import g419.liner2.api.LinerOptions;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.chunker.factory.ChunkerFactory;
import g419.liner2.api.chunker.factory.ChunkerManager;
import g419.liner2.api.features.TokenFeatureGenerator;
import g419.liner2.api.tools.Logger;
import g419.liner2.api.tools.ParameterException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




/**
 * Daemon class for NER WebService.
 * @author Maciej Janicki
 */
public class DaemonThread extends Thread {
	private static final int DEFAULT_MAX_THREADS = 5;

	String db_addr, myAddr, ip;
	int port, myId = -1;
	Database db;
	ServerSocket serverSocket;
	int numWorkingThreads, maxThreads;
	Vector<WorkingThread> workingThreads;
    private HashMap<String, TokenFeatureGenerator> featureGenerators;
    private HashMap<String, Chunker> chunkers;

	public DaemonThread() throws ParameterException {
		// setup database address
		String db_host = null, db_port = "3306", db_user = null,
			db_pass = "", db_name = null;

		// getGlobal access data from db_uri parameter
		String db_uri = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_URI);
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
		if (DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_HOST) != null)
			db_host = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_HOST);
		if (DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_PORT) != null)
			db_port = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_PORT);
		if (DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_USER) != null)
			db_user = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_USER);
		if (DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_PASSWORD) != null)
			db_pass = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_PASSWORD);
		if (DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_NAME) != null)
			db_name = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_DB_NAME);

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
		this.ip = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_IP);
		if (this.ip == null)
			throw new ParameterException("Daemon mode: -ip (IP address) option is obligatory!");
		String optPort = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_PORT);
		if (optPort == null)
			throw new ParameterException("Daemon mode: -p (port) option is obligatory!");
    	try {
			this.port = Integer.parseInt(optPort);
		} catch (NumberFormatException ex) {
			throw new ParameterException("Incorrect port number: " + optPort);
		}

		// setup maximum threads number
		this.maxThreads = DEFAULT_MAX_THREADS;
		String optMaxThreads = DaemonOptions.getGlobal().getOption(DaemonOptions.OPTION_MAX_THREADS);
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

        chunkers = new HashMap<String, Chunker>();
        featureGenerators = new HashMap<String, TokenFeatureGenerator>();
        try {
            for (String modelNam: DaemonOptions.getGlobal().models.keySet()){
                LinerOptions modelConfig = DaemonOptions.getGlobal().models.get(modelNam);
                ChunkerManager cm = ChunkerFactory.loadChunkers(modelConfig);
                this.chunkers.put(modelNam, cm.getChunkerByName(modelConfig.getOptionUse()));
                TokenFeatureGenerator gen = null;
                if (!modelConfig.features.isEmpty()) {
                     gen = new TokenFeatureGenerator(modelConfig.features);
                }
                this.featureGenerators.put(modelNam, gen);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

	}

	@Override
	public void run() {
    
    	Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { shutdown(); }});
    
    	// register daemon
		this.myAddr = this.ip + ":" + this.port;
		Logger.log("My address: " + this.myAddr, true);
		Logger.log("Registering daemon...", false);
		try {
			this.db.connect();
			this.myId = this.db.registerDaemon(this.myAddr);
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		Logger.log("Registered with id: " + this.myId, false);
    	
		// start listening for notifications
		Logger.log("Listening on port: " + port, false);
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
						Logger.log("Received PING!", true);
					}
					else if (line.equals("NOTIFY")) {
						Logger.log("Received NOTIFY!", true);
						
						// start work in a new thread
						if (this.numWorkingThreads < this.maxThreads)
							startWorkingThread();
					}
					else {
						Logger.log("Received something weird: " + line, true);
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
			Logger.log("Sleeping...", false);
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

        if (this.numWorkingThreads == 0) {
            Logger.log("Woke up!", false);
        }
        this.numWorkingThreads++;
        WorkingThread newThread = new WorkingThread(this, this.db_addr, this.chunkers, this.featureGenerators);
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
    	Logger.log("Shutting down...", false);
		
		// close server socket
		if (this.serverSocket != null) {
			Logger.log("Closing socket...", false);
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

		Logger.log("Done.", false);
	}
}
