package g419.liner2.daemon.utils;

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

    protected File workingDir;
    URL url;

    public FilebasedDaemonThread(final File workingDir, final String url, final int max_threads) throws ParameterException, MalformedURLException {
        super(max_threads);
        this.workingDir = workingDir;
        this.url = new URL(url);
    }

    @Override
    public void run() {
        super.run();
        //daemon_loop:
        while (true) {
            try {
                final JSONObject response = checkForJob();
                final String task = response != null ? response.getString("task") : "";
                if (!task.isEmpty()) {
                    final File request = new File((String.format("%s/progress/%s", workingDir.getAbsolutePath(), task)));
                    final JSONObject options = new JSONObject(response.getString("options"));
                    boolean job_assigned = false;
                    while (!job_assigned) {
                        if (workingThreads.size() < maxThreads) {
                            startWorkingThread(request, options);
                            job_assigned = true;
                        } else {
                            for (final WorkingThread thread : workingThreads) {
                                if (!((FileBasedWorkingThread) thread).isBusy()) {
                                    ((FileBasedWorkingThread) thread).assignJob(request, options);
                                    job_assigned = true;
                                    break;
                                }
                            }
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (final WorkingThread thread : workingThreads) {
                        if (!((FileBasedWorkingThread) thread).isBusy()) {
                            finishWorkingThread(thread);
                            continue;
                        }
                    }

                }
            } catch (final JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject checkForJob() {
        try {
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            final OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write("{\"tool\":\"liner2\"}");
            osw.flush();
            osw.close();
            final int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                final BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                final StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return new JSONObject(response.toString());
            } else {
                return null;
            }
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final ProtocolException e) {
            e.printStackTrace();
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void startWorkingThread(final File request, final JSONObject options) {
        super.startWorkingThread();
        final FileBasedWorkingThread newThread = new FileBasedWorkingThread(this);
        newThread.assignJob(request, options);
        newThread.start();
        workingThreads.add(newThread);
    }

    @Override
    public void shutdown() {

    }


}
