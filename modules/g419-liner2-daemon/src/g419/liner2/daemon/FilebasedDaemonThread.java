package g419.liner2.daemon;

import g419.corpus.Logger;
import g419.corpus.io.reader.AbstractDocumentReader;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.lib.cli.ParameterException;
import g419.liner2.api.chunker.Chunker;
import g419.liner2.api.features.TokenFeatureGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.TreeMap;

/**
 * Created by michal on 11/20/14.
 */
public class FilebasedDaemonThread extends DaemonThread {

    protected File db_path;
    URL url;
    private TreeMap<File, JSONObject> awaiting_requests;

    public FilebasedDaemonThread(File db_path, int max_threads) throws ParameterException, MalformedURLException {
        super(max_threads);
        this.db_path = db_path;
        url = new URL("http://10.108.19.111:8099/reservation");
    }

    @Override
    public void run() {
        super.run();
        while (this.workingThreads.size() < this.maxThreads) {
            startWorkingThread();
        }

        while (true) {
            for (WorkingThread thread : workingThreads) {
                if (!((FileBasedWorkingThread) thread).isBusy()) {
                    try {
                        JSONObject response = checkForJob();
                        String task = response.getString("task");
                        if (!task.isEmpty()) {
                            File request = new File((String.format("%s/progress/%s", db_path.getAbsolutePath(), task)));
                            JSONObject options = new JSONObject(response.getString("options"));
                            ((FileBasedWorkingThread) thread).assignJob(request, options);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private JSONObject checkForJob(){
        try{
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write("{\"tool\":\"liner2\"}");
            osw.flush();
            osw.close();
            int responseCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new JSONObject(response.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void startWorkingThread() {
        super.startWorkingThread();
        FileBasedWorkingThread newThread = new FileBasedWorkingThread(this);
        newThread.start();
        this.workingThreads.add(newThread);
    }

    @Override
    public void shutdown() {

    }


}
