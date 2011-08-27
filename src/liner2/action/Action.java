package liner2.action;

public abstract class Action {

	abstract public void run() throws Exception;
	
//	/**
//	 * Prints evaluation details in a form of LaTeX table.
//	 * @param chunkingEvaluation
//	 */
//	public void printEvaluationDetails(ChunkingEvaluation chunkingEvaluation){
//		HashMap<String, PrecisionRecallEvaluation> evaluationDetails = chunkingEvaluation.precisionRecallEvaluationDetails();
//		
//		Main.log(String.format("+---------------------------------------+"));
//        Main.log(String.format("| Evaluation details (LaTeX formatting) |"));
//        Main.log(String.format("+---------------------------------------+"));
//        if (evaluationDetails == null){
//        	Main.log("No details!");
//        }else{
//        	
//        	String sHeader       = "                 & ";
//        	String sTruePositive = "   TruePositive  & ";
//        	String sFalsePositive= "   FalsePositive & ";
//        	String sFalseNegative= "   FalseNegative & ";
//        	String sPrecision    = "   Precision     & ";
//        	String sRecall       = "   Recall        & ";
//        	String sFmeasure     = "   F$_1$         & ";
//
//        	// Set header order
//        	ArrayList<String> typesOrder = new ArrayList<String>();
//        	for (String type : LinerOptions.get().summaryTypesOrder)
//        		if (evaluationDetails.containsKey(type))
//        			typesOrder.add(type);
//        	for (String type : evaluationDetails.keySet())
//        		if (!typesOrder.contains(type))
//        			typesOrder.add(type);        		
//        	
//        	// Go through all types in set order
//        	for (String type : typesOrder){
//            	String cellFormat = " %" + type.length() + "s &";
//        		PrecisionRecallEvaluation evaluation = evaluationDetails.get(type);        		
//        		sHeader += String.format(cellFormat, type);
//        		sTruePositive += String.format(cellFormat, evaluation.truePositive());
//        		sFalsePositive += String.format(cellFormat, evaluation.falsePositive());
//        		sFalseNegative += String.format(cellFormat, evaluation.falseNegative());
//        		sPrecision += String.format(cellFormat, String.format("%6.2f", evaluation.precision()*100).replace(",", ".") + "\\%");
//        		sRecall += String.format(cellFormat, String.format("%6.2f", evaluation.recall()*100).replace(",", ".") + "\\%");
//        		sFmeasure += String.format(cellFormat, String.format("%6.2f", evaluation.fMeasure()*100).replace(",", ".") + "\\%");
//        	}
//        	
//        	PrecisionRecallEvaluation evaluation = chunkingEvaluation.precisionRecallEvaluation();
//        	String cellFormat = " %10s \\\\";
//    		sHeader += String.format(cellFormat, "Total");
//    		sTruePositive += String.format(cellFormat, String.format("%6d", evaluation.truePositive()));
//    		sFalsePositive += String.format(cellFormat, String.format("%6d", evaluation.falsePositive()));
//    		sFalseNegative += String.format(cellFormat, String.format("%6d", evaluation.falseNegative()));
//    		sPrecision += String.format(cellFormat, String.format("%6.2f", evaluation.precision()*100).replace(",", ".") + "\\%");
//    		sRecall += String.format(cellFormat, String.format("%6.2f", evaluation.recall()*100).replace(",", ".") + "\\%");
//    		sFmeasure += String.format(cellFormat, String.format("%6.2f", evaluation.fMeasure()*100).replace(",", ".") + "\\%");
//        	
//        	Main.log(sHeader);
//        	Main.log(sTruePositive);
//        	Main.log(sFalsePositive);
//        	Main.log(sFalseNegative);
//        	Main.log(sPrecision);
//        	Main.log(sRecall);
//        	Main.log(sFmeasure);
//        }
//	}

}
