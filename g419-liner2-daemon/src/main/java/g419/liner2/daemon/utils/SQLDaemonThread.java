package g419.liner2.daemon.utils;


import g419.corpus.ConsolePrinter;
import g419.lib.cli.ParameterException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;


/**
 * Daemon class for NER WebService.
 *
 * @author Maciej Janicki
 */
public class SQLDaemonThread extends DaemonThread {
    String db_addr, myAddr, ip;
    int port, myId = -1;
    Database db;
    ServerSocket serverSocket;

    public SQLDaemonThread(final String db_host, final String db_port, final String db_user, final String db_pass, final String db_name, final String ip, final int port, final int max_threads) throws ParameterException {
        super(max_threads);

        db_addr = "jdbc:mysql://" + db_host;
        if (db_port != null) {
            db_addr += ":" + db_port;
        }
        db_addr += "/" + db_name;
        db_addr += "?user=" + db_user;
        if ((db_pass != null) && (!db_pass.isEmpty())) {
            db_addr += "&password=" + db_pass;
        }
        db_addr += "&useUnicode=true&characterEncoding=UTF-8";

        //this.db_addr = "jdbc:mysql://" + db_host + "/" + db_name +
        //	"?user=" + db_user + "&password=" + db_pass +
        //	"&useUnicode=true&characterEncoding=UTF-8";
        db = new Database(db_addr);

        // setup ip address and port number
        this.ip = ip;
        this.port = port;

    }

    @Override
    public void run() {
        super.run();
        // register daemon
        myAddr = ip + ":" + port;
        ConsolePrinter.log("My address: " + myAddr, true);
        ConsolePrinter.log("Registering daemon...", false);
        try {
            db.connect();
            myId = db.registerDaemon(myAddr);
            db.disconnect();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        } catch (final ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        ConsolePrinter.log("Registered with id: " + myId, false);

        // start listening for notifications
        ConsolePrinter.log("Listening on port: " + port, false);
        try {
            serverSocket = new ServerSocket(port);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        for (int i = 0; i < maxThreads; i++) {
            startWorkingThread();
        }

        while (!serverSocket.isClosed()) {
            try {
                final Socket accepted = serverSocket.accept();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(
                        accepted.getInputStream()));
                final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        accepted.getOutputStream()));

                final String line = reader.readLine();
                if (line != null) {
                    if (line.equals("PING")) {
                        ConsolePrinter.log("Received PING!", true);
                    } else if (line.equals("NOTIFY")) {
                        ConsolePrinter.log("Received NOTIFY!", true);

                        // start work in a new thread
                        if (workingThreads.size() < maxThreads) {
                            startWorkingThread();
                        }
                    } else {
                        ConsolePrinter.log("Received something weird: " + line, true);
                    }
                    // respond to connection
                    writer.write("OK");
                    writer.newLine();
                    writer.flush();
                }
                accepted.close();
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public int getDaemonId() {
        return myId;
    }

    @Override
    public synchronized void finishWorkingThread(final WorkingThread callingThread) {
        synchronized (db) {
            try {
                db.connect();
                db.daemonReady(myId);
                db.disconnect();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }

        super.finishWorkingThread(callingThread);
    }

    @Override
    public void startWorkingThread() {
        super.startWorkingThread();

        synchronized (db) {
            try {
                db.connect();
                db.daemonNotReady(myId);
                db.disconnect();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }

        final SQLWorkingThread newThread = new SQLWorkingThread(this, db_addr, chunkers, featureGenerators);
        workingThreads.add(newThread);
        newThread.start();
    }

    @Override
    public void shutdown() {
        super.shutdown();

        // close server socket
        if (serverSocket != null) {
            ConsolePrinter.log("Closing socket...", false);
            try {
                serverSocket.close();
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }

        // unregister daemon
        if (myId > -1) {
            try {
                db.connect();
                db.unregisterDaemon(myId);
                db.disconnect();
            } catch (final SQLException ex) {
                ex.printStackTrace();
            } catch (final ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        ConsolePrinter.log("Done.", false);
    }
}
