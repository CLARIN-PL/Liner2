package liner2.daemon;

import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import java.sql.SQLException;

import liner2.chunker.Chunker;
import liner2.chunker.factory.ChunkerFactory;

import liner2.LinerOptions;
import liner2.Main;

/**
 * Daemon class for NER WebService.
 * @author Maciej Janicki
 */
public class LinerDaemon extends Thread {
	String db_addr, myId;
	int port;
	Database db;
	Chunker chunker;
	ServerSocket serverSocket;
	boolean working;
	WorkingThread workingThread;
	
	public LinerDaemon() {
		String db_host = LinerOptions.getOption(LinerOptions.OPTION_DB_HOST);
		String db_port = LinerOptions.getOption(LinerOptions.OPTION_DB_PORT);
		String db_user = LinerOptions.getOption(LinerOptions.OPTION_DB_USER);
		String db_pass = LinerOptions.getOption(LinerOptions.OPTION_DB_PASSWORD);
		String db_name = LinerOptions.getOption(LinerOptions.OPTION_DB_NAME);

		this.db_addr = "jdbc:mysql://" + db_host + "/" + db_name +
			"?user=" + db_user + "&password=" + db_pass +
			"&useUnicode=true&characterEncoding=UTF-8";
		this.db = new Database(this.db_addr);
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
    	this.port = Integer.parseInt(LinerOptions.getOption(LinerOptions.OPTION_PORT));
    
    	// register daemon
		try {
			this.myId = InetAddress.getLocalHost().getHostName() + ":" + this.port;
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		Main.log("My ID: " + this.myId, true);
		Main.log("Registering daemon...", false);
		try {
			this.db.connect();
			this.db.registerDaemon(this.myId);
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
    	
		// start listening for notifications
		Main.log("Listening on port: " + port, false);
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		work();
				
		while (!serverSocket.isClosed()) {
			try {
				Socket accepted = this.serverSocket.accept();
				accepted.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			Main.log("Received PING!", true);

			// if not working -- start work in a new thread
			if (!this.working)
				work();
		}
	}

	public void sleep() {
		Main.log("Sleeping...", false);
		this.working = false;
		this.workingThread = null;
	}

	public void work() {
		Main.log("Woke up and working.", false);
		this.working = true;
		this.workingThread = new WorkingThread(this, this.chunker, this.db_addr);
		this.workingThread.run();
	}
	
	public void shutdown() {
    	try {
			if (this.working)
				this.workingThread.interrupt();
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
		try {
			this.db.connect();
			this.db.unregisterDaemon(this.myId);
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		Main.log("Done.", false);
	}
}
