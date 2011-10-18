package liner2.action;

// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.InputStream;
// import java.io.InputStreamReader;
// import java.io.IOException;
// import java.io.OutputStream;
// import java.io.OutputStreamWriter;

//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.Socket;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;

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

import liner2.tools.LinerServer;
import liner2.LinerOptions;
import liner2.Main;

/**
 * Daemon mode for liner2 WebService.
 * @author Maciej Janicki
 * TODO wydzielić obsługę serwera jako osobną klasę?
 */
public class ActionDaemon extends Action{
	public void run() {
		new LinerServer().run();
	}
}
