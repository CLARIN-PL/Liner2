package g419.crete.cli.action;

import g419.lib.cli.action.Action;

/**
 * Akcja klasyfikacji koreferencji w dokumentach.
 * Dla zadanego dokumentu tworzy relacje pomiędzy wzmiankami - frazami oznaczonymi jako odnoszące się do nazwy własnej 
 * oraz nazwami własnymi. Dodatkowo klasyfikuje także powiązania koreferencyjne pomiędzy samymi nazwami własnymi.
 * 
 * @author Adam Kaczmarek
 *
 */
public class ActionClassify extends Action {

	public ActionClassify(String name) {
		super("classify");
	}

	@Override
	public void parseOptions(String[] args) throws Exception {
		// TODO Auto-generated method stub

	}

//	public void initializeResolvers(){
//		CreteResolverFactory.getFactory().register("svm_cluster_ranking", new SvmClusterRankingResolver());
//	}
	
	
	/**
	 * Przebieg klasyfikacji wszystkich relacji koreferencyjnych dla wszystkich dokumentów
	 * @pattern TemplateMethod
	 * 
	 */
	
	
	
	@Override
	public void run() throws Exception {
	
		// load documents - reader is a document iterator
//		AbstractDocumentReader reader = getReader();
//		
//		// load classifier model
//		String modelPath;
//		Model<?> model = new Model<?>)();
//		model.load(modelPath);
//		
		// Instantiate resolver
//		AbstractCreteResolver<?, ?, ?, ?> resolver = CreteResolverFactory.getFactory().getInstance("svm_cluster_ranking", "svm_ranking", "mention_cluster_generator", "mention_cluster_to_sparse_vector"); 
		
		
		// instantiate classifier...
//		List<AbstractAnnotationClusterer> clusterers = initializeClusterers();
		// .. with selectors
//		List<AbstractAnnotationSelector> selectors = initializeSelectors();
//		
//		
//		// For each document
//		while(true){
//			Document currentDocument = reader.nextDocument();
//			if(currentDocument == null) break;
//			
//			// Classify and relations
//			for(int i = 0; i < resolvers.size(); i++){
//				AbstractAnnotationSelector currentSelector = selectors.get(i);
//				AbstractResolver<?,?,?,?> currentResolver = resolvers.get(i);
//				
//				List<Annotation> mentions = currentSelector.selectAnnotations(currentDocument)
//				currentResolver.resolveDocument(currentDocument, mentions);
//				
//				
//			}
//			
//		}
		
		
	}

}
