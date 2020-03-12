package g419.liner2.core.tools;

import g419.corpus.structure.Document;
import g419.corpus.structure.Paragraph;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


public class ProcessingTimer {

  @Data
  @AllArgsConstructor
  class Task {
    private String label = null;
    private long time = 0;
    private boolean countInTotal = false;

    public void addTime(final long time) {
      this.time += time;
    }
  }

  private String label = null;
  private long startTime = 0;
  private int tokensNumber = 0;
  private int textSize = 0;
  private boolean countInTotal = false;

  ArrayList<Task> tasks = new ArrayList<>();
  HashMap<String, Task> tasksMap = new HashMap<>();

  public void addTokens(final Document ps) {
    int tokens = 0;
    int chars = 0;
    for (final Paragraph p : ps.getParagraphs()) {
      for (final Sentence s : p.getSentences()) {
        for (final Token t : s.getTokens()) {
          chars += t.getOrth().getBytes().length + (t.getNoSpaceAfter() ? 0 : 1);
          tokens++;
        }
      }
    }
    textSize += chars;
    tokensNumber += tokens;
  }

  public void countTokens(final Document ps) {
    textSize = 0;
    tokensNumber = 0;
    addTokens(ps);
  }

  public void startTimer(final String label) {
    startTimer(label, true);
  }

  public void startTimer(final String label, final boolean countInTotal) {
    this.label = label;
    startTime = System.nanoTime();
    this.countInTotal = countInTotal;
  }

  public void stopTimer() {
    final long duration = System.nanoTime() - startTime;

    if (tasksMap.containsKey(label)) {
      tasksMap.get(label).addTime(duration);
    } else {
      final Task task = new Task(label, duration, countInTotal);
      tasks.add(task);
      tasksMap.put(label, task);
    }

    label = null;
  }

  public void printStats() {
    final float nanosec = 1000000000f;
    System.out.println();
    System.out.println(StringUtils.repeat("=", 80));
    System.out.println("Processing time");
    System.out.println(StringUtils.repeat("=", 80));
    int i = 1;
    long totalTime = 0;
    for (final Task task : tasks) {
      String suffix = "";
      if (task.isCountInTotal()) {
        totalTime += task.getTime();
      } else {
        suffix = "(not in total time)";
      }

      System.out.println(String.format("%d) %-45s : %10s (%dns) %s", i++,
          task.getLabel(), timetoString(task.getTime()), task.getTime(), suffix));
    }

    System.out.println(StringUtils.repeat("-", 80));
    System.out.println(String.format("## %-45s   %10s (%dns)", "Total time", timetoString(totalTime), totalTime));
    System.out.println(StringUtils.repeat("-", 80));
    System.out.println(String.format("Tokens           : %8d", tokensNumber));
    System.out.println(String.format("Text kB          : %11.2f", (float) textSize / 1024f));
    System.out.println(String.format("Tokens  / second : %11.2f", (float) tokensNumber / (totalTime / nanosec)));
    System.out.println(String.format("Text kB / second : %11.2f", (float) textSize / 1024f / (totalTime / nanosec)));
    System.out.println(StringUtils.repeat("-", 80));
  }

  private String timetoString(long time) {
    final long secondsInNano = 1000000000;
    final long minutesInMilli = secondsInNano * 60;
    final long hoursInMilli = minutesInMilli * 60;

    final long elapsedHours = time / hoursInMilli;
    time = time % hoursInMilli;

    final long elapsedMinutes = time / minutesInMilli;
    time = time % minutesInMilli;

    final long elapsedSeconds = time / secondsInNano;

    final String hours = elapsedHours > 0 ? String.format("%02d", elapsedHours) : "--";
    final String minutes = elapsedHours > 0 || elapsedMinutes > 0 ? String.format("%02d", elapsedMinutes) : "--";

    return String.format(
        "%sh %sm %02ds",
        hours, minutes, elapsedSeconds);
  }

}
