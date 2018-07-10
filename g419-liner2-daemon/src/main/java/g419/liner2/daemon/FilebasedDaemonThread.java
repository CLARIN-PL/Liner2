package g419.liner2.daemon;

import g419.lib.cli.ParameterException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by michal on 11/20/14.
 */
public class FilebasedDaemonThread extends DaemonThread {

    protected File db_path;
    URL url;

    public FilebasedDaemonThread(File db_path, String url, int max_threads) throws ParameterException, MalformedURLException {
        super(max_threads);
        this.db_path = db_path;
        this.url = new URL(url);
    }

    @Override
    public void run() {
        super.run();
        daemon_loop:
        while (true) {
            try{
                JSONObject response = checkForJob();
                String task = response != null ? response.getString("task") : "";
                if(!task.isEmpty()){
                    File request = new File((String.format("%s/progress/%s", db_path.getAbsolutePath(), task)));
                    JSONObject options = new JSONObject(response.getString("options"));
                    boolean job_assigned = false;
                    while(!job_assigned){
                        if (this.workingThreads.size() < this.maxThreads) {
                            startWorkingThread(request, options);
                            job_assigned = true;
                        }
                        else {
                            for (WorkingThread thread : workingThreads) {
                                if (!((FileBasedWorkingThread) thread).isBusy()) {
                                    ((FileBasedWorkingThread) thread).assignJob(request, options);
                                    job_assigned =true;
                                    break;
                                }
                            }
                        }
		     	try{
                            Thread.sleep(1000);
                        } catch (InterruptedException e){ e.printStackTrace();}
                    }
                }
                else{
                    try{
                        Thread.sleep(1000);
                    } catch (InterruptedException e){ e.printStackTrace();}
                    for (WorkingThread thread : workingThreads) {
                        if (!((FileBasedWorkingThread) thread).isBusy()) {
                            finishWorkingThread(thread);
                            continue daemon_loop;
                        }
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
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
            if(responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return new JSONObject(response.toString());
            }
            else{
                return null;
            }
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

    public void startWorkingThread(File request, JSONObject options) {
        super.startWorkingThread();
        FileBasedWorkingThread newThread = new FileBasedWorkingThread(this);
        newThread.assignJob(request, options);
        newThread.start();
        this.workingThreads.add(newThread);
    }

    @Override
    public void shutdown() {

    }


}
