package g419.liner2.core.tools;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;

import java.util.ArrayList;
import java.util.HashMap;


public class ProcessingTimer {

  class Task {
    private String label = null;
    private long time = 0;
    private boolean countInTotal = false;

    public Task(String label, long time, boolean countInTotal) {
      this.label = label;
      this.time = time;
      this.countInTotal = countInTotal;
    }

    public void addTime(long time) {
      this.time += time;
    }

    public String getLabel() {
      return this.label;
    }

    public long getTime() {
      return this.time;
    }

    public boolean getCountInTotal() {
      return this.countInTotal;
    }
  }

  private String label = null;
  private long startTime = 0;
  private int tokensNumber = 0;
  private int textSize = 0;
  private boolean countInTotal = false;

  ArrayList<Task> tasks = new ArrayList<Task>();
  HashMap<String, Task> tasksMap = new HashMap<String, ProcessingTimer.Task>();

  public void addTokens(Document ps) {
    int tokens = 0;
    int chars = 0;
    for (Paragraph p : ps.getParagraphs()) {
      for (Sentence s : p.getSentences()) {
        for (Token t : s.getTokens()) {
          chars += t.getOrth().getBytes().length + (t.getNoSpaceAfter() ? 0 : 1);
          tokens++;
        }
      }
    }
    this.textSize += chars;
    this.tokensNumber += tokens;
  }

  public void countTokens(Document ps) {
    this.textSize = 0;
    this.tokensNumber = 0;
    this.addTokens(ps);
  }

  public void startTimer(String label) {
    this.startTimer(label, true);
  }

  public void startTimer(String label, boolean countInTotal) {
    this.label = label;
    this.startTime = System.nanoTime();
    this.countInTotal = countInTotal;
  }

  public void stopTimer() {
    long duration = System.nanoTime() - this.startTime;

    if (this.tasksMap.containsKey(this.label)) {
      this.tasksMap.get(this.label).addTime(duration);
    } else {
      Task task = new Task(this.label, duration, this.countInTotal);
      this.tasks.add(task);
      this.tasksMap.put(this.label, task);
    }

    this.label = null;
  }

  public void printStats() {
    float nanosec = 1000000000f;
    System.out.println("====================================================");
    System.out.println("Processing time");
    System.out.println("====================================================");
    int i = 1;
    long totalTime = 0;
    for (Task task : this.tasks) {
      String suffix = "";
      if (task.getCountInTotal()) {
        totalTime += task.getTime();
      } else {
        suffix = "(not in total time)";
      }

      System.out.println(String.format("%d) %-20s : %10s (%dns) %s", i++, task.getLabel(), timetoString(task.getTime()), task.getTime(), suffix));
    }

    System.out.println("----------------------------------------------------");
    System.out.println(String.format("## %-20s   %10s (%dns)", "Total time", timetoString(totalTime), totalTime));
    System.out.println("----------------------------------------------------");
    System.out.println(String.format("Tokens           : %8d", this.tokensNumber));
    System.out.println(String.format("Text kB          : %11.2f", (float) this.textSize / 1024f));
    System.out.println(String.format("Tokens  / second : %11.2f", (float) this.tokensNumber / (totalTime / nanosec)));
    System.out.println(String.format("Text kB / second : %11.2f", (float) this.textSize / 1024f / (totalTime / nanosec)));
    System.out.println("----------------------------------------------------");
  }

  private String timetoString(long time) {
    long secondsInNano = 1000000000;
    long minutesInMilli = secondsInNano * 60;
    long hoursInMilli = minutesInMilli * 60;

    long elapsedHours = time / hoursInMilli;
    time = time % hoursInMilli;

    long elapsedMinutes = time / minutesInMilli;
    time = time % minutesInMilli;

    long elapsedSeconds = time / secondsInNano;

    String hours = elapsedHours > 0 ? String.format("%02d", elapsedHours) : "--";
    String minutes = elapsedHours > 0 || elapsedMinutes > 0 ? String.format("%02d", elapsedMinutes) : "--";

    return String.format(
        "%sh %sm %02ds",
        hours, minutes, elapsedSeconds);
  }


}
