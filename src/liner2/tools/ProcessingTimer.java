package liner2.tools;

import java.util.ArrayList;

import liner2.structure.Paragraph;
import liner2.structure.ParagraphSet;
import liner2.structure.Sentence;
import liner2.structure.Token;

public class ProcessingTimer {

	class Task{
		private String label = null;
		private long time = 0;
		
		public Task(String label, long time){
			this.label = label;
			this.time = time;
		}
		
		public String getLabel(){
			return this.label;
		}
		
		public long getTime(){
			return this.time;
		}
	}
	
	private String label = null;
	private long startTime = 0;	
	private int tokensNumber = 0;
	private int textSize = 0;

	ArrayList<Task> tasks = new ArrayList<Task>();

	public void countTokens(ParagraphSet ps){
		int tokens = 0;
		int chars = 0;
		for (Paragraph p : ps.getParagraphs() )
			for (Sentence s : p.getSentences() )
				for (Token t : s.getTokens()){
					chars += t.getFirstValue().length() + 1;
					tokens ++;
				}
		this.textSize = chars;
		this.tokensNumber = tokens;
	}

	public void startTimer(String label){
		this.label = label;
		this.startTime = System.nanoTime();
	}
	
	public void stopTimer(){
		this.tasks.add(new Task(this.label, System.nanoTime() - this.startTime ));
		this.label = null;
	}
	
	public void printStats(){
		float milisec = 1000000000f; 
		System.out.println("====================================================");
		System.out.println("Processing time");
		System.out.println("====================================================");
		int i=1;
		long totalTime = 0;
		for ( Task task : this.tasks ){
			System.out.println(String.format("%d) %-20s %5.2f s", i++, task.getLabel(), (float)task.getTime()/milisec));
			totalTime += task.getTime();
		}
		System.out.println("----------------------------------------------------");
		System.out.println(String.format("## %-20s %5.2f s", "Total time", (float)totalTime/milisec));
		System.out.println("----------------------------------------------------");
		System.out.println(String.format("Tokens  / second: %5.2f", (float)this.tokensNumber / (totalTime/milisec) ));
		System.out.println(String.format("Text kB / second: %5.2f", (float)this.textSize / 1024f / (totalTime/milisec) ));
		System.out.println("----------------------------------------------------");
	}
}
