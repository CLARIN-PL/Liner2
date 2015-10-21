package g419;

import g419.corpus.TerminateException;
import g419.tools.*;
import g419.tools.actions.CategorizeTool;
import g419.tools.actions.ConvertGeneticRulesTool;
import g419.tools.actions.ConvertJRipRulesTool;
import g419.tools.actions.CreateDictTool;
import g419.tools.actions.FeatureMatrix;
import g419.tools.actions.Tool;
import g419.tools.actions.WordnetTool;

import java.text.ParseException;
import java.util.HashMap;

/**
 * Created by michal on 11/5/14.
 */

public class Main {

    /** List of known actions. */
    private HashMap<String, Tool> actions = new HashMap<String, Tool>();

    /**
     * Here the story begins.
     */
    public static void main(String[] args) throws Exception {

        Main main = new Main();
        main.registerTool(new ConvertJRipRulesTool());
        main.registerTool(new CreateDictTool());
        main.registerTool(new CategorizeTool());
        main.registerTool(new ConvertGeneticRulesTool());
        main.registerTool(new WordnetTool());
        main.registerTool(new FeatureMatrix());

        if ( args.length == 0 ){
            main.printCredits();
            System.out.println("[Error] Tool not given. \n\nUse one of the following tools:");
            main.printTools();
            System.out.println();
            System.out.println("usage: ./tools <tool> [options]");
            System.out.println();
        }
        else{
            String name = args[0];
            Tool tool = main.getTool(name);
            if ( tool == null ){
                main.printCredits();
                System.out.println(String.format("[Error] Tool '%s' does not exist. \n\nUse one of the following tools:", name));
                main.printTools();
                System.out.println();
                System.err.println("usage: ./tools <tool> [options]");
                System.out.println();
            }
            else{
                try{
                    tool.parseOptions(args);
                    tool.run();
                }
                catch (TerminateException e){
                    System.out.println(e.getMessage());
                }
                catch (ParseException e) {
                    main.printCredits();
                    System.out.println(String.format("[Options parse error] %s\n", e.getMessage()));
                    tool.printOptions();
                    System.out.println();
                }
                catch (Exception e) {
                    System.out.println(e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void printCredits(){
        System.out.println("*-----------------------------------------------------------------------------------------------*");
        System.out.println("* A framework for multitask sequence labeling, including: named entities, temporal expressions. *");
        System.out.println("*                                                                                               *");
        System.out.println("* Authors: Michał Marcińczuk (2010–2014), Michał Krautforst (2013-2014), Jan Kocoń (2014)       *");
        System.out.println("*          Dominik Piasecki (2013), Maciej Janicki (2011)                                       *");
        System.out.println("* Contact: michal.marcinczuk@pwr.wroc.pl                                                        *");
        System.out.println("*                                                                                               *");
        System.out.println("*          G4.19 Research Group, Wrocław University of Technology                               *");
        System.out.println("*-----------------------------------------------------------------------------------------------*");
        System.out.println();
    }

    /**
     * Register a new action. The action must have unique name.
     * @param tool -- object used to run the tool.
     */
    public void registerTool(Tool tool){
        this.actions.put(tool.getName(), tool);
    }

    /**
     * Prints a list of available actions.
     */
    public void printTools(){
        int maxLength = 0;
        for ( String name : this.actions.keySet())
            maxLength = Math.max(maxLength, name.length());

        String lineFormat = " - %-" + maxLength + "s -- %s";

        String newLine = String.format("   %"+maxLength+"s    ", " ");

        for (Tool tool : this.actions.values()){
            System.out.println(String.format(lineFormat,
                    tool.getName(),
                    tool.getDescription()).replaceAll("#", "\n" + newLine));
        }
    }

    public Tool getTool(String name){
        if ( this.actions.containsKey(name))
            return this.actions.get(name);
        else
            return null;
    }

}

