package g419.liner2.daemon;


import g419.corpus.ConsolePrinter;
import g419.lib.cli.ParameterException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;




/**
 * Daemon class for NER WebService.
 * @author Maciej Janicki
 */
public class SQLDaemonThread extends DaemonThread {
	String db_addr, myAddr, ip;
	int port, myId = -1;
	Database db;
	ServerSocket serverSocket;

	public SQLDaemonThread(String db_host, String db_port, String db_user, String db_pass, String db_name, String ip, int port, int max_threads) throws ParameterException {
        super(max_threads);

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
		this.ip = ip;
		this.port = port;

	}

	@Override
	public void run() {
        super.run();
    	// register daemon
		this.myAddr = this.ip + ":" + this.port;
		ConsolePrinter.log("My address: " + this.myAddr, true);
		ConsolePrinter.log("Registering daemon...", false);
		try {
			this.db.connect();
			this.myId = this.db.registerDaemon(this.myAddr);
			this.db.disconnect();
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		ConsolePrinter.log("Registered with id: " + this.myId, false);
    	
		// start listening for notifications
		ConsolePrinter.log("Listening on port: " + port, false);
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
						ConsolePrinter.log("Received PING!", true);
					}
					else if (line.equals("NOTIFY")) {
						ConsolePrinter.log("Received NOTIFY!", true);
						
						// start work in a new thread
						if (this.workingThreads.size() < this.maxThreads)
							startWorkingThread();
					}
					else {
						ConsolePrinter.log("Received something weird: " + line, true);
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
		
        super.finishWorkingThread(callingThread);
	}

	public void startWorkingThread() {
        super.startWorkingThread();

		synchronized (this.db) {
            try {
                this.db.connect();
                this.db.daemonNotReady(this.myId);
                this.db.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        SQLWorkingThread newThread = new SQLWorkingThread(this, this.db_addr, this.chunkers, this.featureGenerators);
        this.workingThreads.add(newThread);
		newThread.start();
	}
	
	public void shutdown() {
        super.shutdown();

		// close server socket
		if (this.serverSocket != null) {
			ConsolePrinter.log("Closing socket...", false);
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

		ConsolePrinter.log("Done.", false);
	}
}
