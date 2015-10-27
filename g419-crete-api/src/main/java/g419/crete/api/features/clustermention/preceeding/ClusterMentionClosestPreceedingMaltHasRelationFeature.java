package g419.crete.api.features.clustermention.preceeding;

import g419.corpus.io.writer.ConllStreamWriter;
import g419.corpus.structure.Annotation;
import g419.corpus.structure.AnnotationCluster;
import g419.crete.api.features.clustermention.AbstractClusterMentionFeature;
import g419.crete.api.structure.AnnotationUtil;
import g419.liner2.api.chunker.MinosChunker;
import g419.liner2.api.tools.parser.MaltParser;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;

public abstract class ClusterMentionClosestPreceedingMaltHasRelationFeature extends AbstractClusterMentionFeature<Boolean>{

	public abstract String getRelationName();
	
	MaltParser maltParser;
	
	public ClusterMentionClosestPreceedingMaltHasRelationFeature(String modelPath){
		maltParser = new MaltParser(modelPath);
//		if(MaltParser.isInitialized(modelPath))
//            maltService = MaltParser.getParser(modelPath);
//        else
//            maltService = MaltParser.addParser(modelPath);
	}
	
	public Boolean mentionHasRelation(Annotation mention, String relationName){
		int mentionPositionStart = mention.getTokens().first();
		int mentionPositionEnd = mention.getTokens().last();
		String[] conllSentence  = ConllStreamWriter.convertSentence(mention.getSentence());
		try {
			DependencyStructure maltGraph = maltParser.parseTokensToDependencyStructure(conllSentence);
			for(int i = mentionPositionStart; i <= mentionPositionEnd; i++)
				if(MinosChunker.MaltGraphUtil.hasRelation(MinosChunker.MaltGraphUtil.getEdgesToNode(maltGraph, i), relationName))
					return true;
			
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	@Override
	public void generateFeature(Pair<Annotation, AnnotationCluster> input) {
		this.value = false;
		
		Annotation mention = input.getLeft();
		AnnotationCluster cluster = input.getRight();
		
		Annotation closestPreceeding = AnnotationUtil.getClosestPreceeding(mention, cluster);
		if(closestPreceeding == null){
			this.value = false;
			return;
		}
		
		String relationName = this.getRelationName();
		this.value = mentionHasRelation(closestPreceeding, relationName);
	}

	@Override
	public Class<Boolean> getReturnTypeClass() {
		return Boolean.class;
	}


}
