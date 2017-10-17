package g419.liner2.core.tools.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import g419.corpus.structure.Annotation;
import g419.corpus.structure.Sentence;
import g419.corpus.structure.Token;
import g419.corpus.structure.TokenAttributeIndex;
import g419.corpus.structure.WrappedToken;
import g419.liner2.core.features.tokens.ClassFeature;

/**
 * Created by michal on 1/9/15.
 */
public class TokenWrapper {

    private static ClassFeature classFeature = new ClassFeature("class");

    public static Sentence wrapAnnotations(Sentence sentence, List<Pattern> annotationTypes){
        Sentence wrappedSent = new Sentence();
        TokenAttributeIndex attrIdx = sentence.getAttributeIndex().clone();
        wrappedSent.setAttributeIndex(attrIdx);
        HashMap<Integer, Integer> newTokenIndexes = new HashMap<Integer, Integer>();
        HashSet<Integer> notWrappedTokens = new HashSet<Integer>();
        List<Token> oldTokens = sentence.getTokens();
        int sentenceIndex=0;
        while ( sentenceIndex<sentence.getTokenNumber() ){
            List<Annotation> tokenAnnsToWrap = getLongestAnns(sentence.getChunksAt(sentenceIndex, annotationTypes));
            if(tokenAnnsToWrap.isEmpty()){
                wrappedSent.addToken(oldTokens.get(sentenceIndex));
                newTokenIndexes.put(sentenceIndex, sentenceIndex);
                notWrappedTokens.add(sentenceIndex);
                sentenceIndex++;
            }
            else{
                Annotation ann = tokenAnnsToWrap.get(0);
                for(int tokIdx: ann.getTokens()){
                    newTokenIndexes.put(sentenceIndex, tokIdx);
                }
                List<Token> tokensToWrap = new ArrayList<Token>();
                for (int j=ann.getBegin(); j<=ann.getEnd(); j++){
                    tokensToWrap.add(sentence.getTokens().get(j));
                }
                if ( tokensToWrap.size() == 0 ){
                   System.out.println("no tokens");
                }
                else{
	                Token head = getAnnotationHead(tokensToWrap, sentence);
	                WrappedToken newToken = new WrappedToken(head.getOrth(), head.getTags().get(0), attrIdx, tokensToWrap, sentence);
	                wrappedSent.addToken(newToken);
	                //Logger.getLogger(TokenWrapper.class).info("Wrapped: " + newToken.getFullOrth());
                }             
                // ToDo: dodać pozostałe anotacje
                for(Annotation an2: tokenAnnsToWrap){
                    wrappedSent.addChunk(new Annotation(wrappedSent.getTokenNumber()-1, an2.getType(), wrappedSent));
                }
                sentenceIndex+= ann.getTokens().size();
            }
        }

        for(Annotation ann: sentence.getChunks()){
            if(notWrappedTokens.containsAll(ann.getTokens())){
                Annotation newAnn = new Annotation(newTokenIndexes.get(ann.getBegin()), ann.getType(), wrappedSent);
                for(int tokIdx: ann.getTokens()){
                    int newIdx = newTokenIndexes.get(tokIdx);
                    if(!newAnn.getTokens().contains(newIdx)){
                        newAnn.addToken(newIdx);
                    }
                }
                //System.out.println(newAnn);
                if ( newAnn.getEnd() >= wrappedSent.getTokenNumber() ){
                	Logger.getLogger(TokenWrapper.class).error("Annotation boundary exceeds sentence boundary");
                } else {
                	wrappedSent.addChunk(newAnn);
                }
            }
        }
        
        return wrappedSent;
    }

    private static Token getAnnotationHead(List<Token> tokens, Sentence sentence){
        Token ign = null;
        for(Token tok: tokens){
            String tokClass = classFeature.generate(tok, sentence.getAttributeIndex());
            if(tokClass != null){
                if(tokClass.equals("subst")){
                    return tok;
                }
                else if(tokClass.equals("ign") && ign != null){
                    ign = tok;
                }
            }
        }
        return ign != null ? ign : tokens.get(0);
    }

    private static List<Annotation> getLongestAnns(List<Annotation> annotations){
        HashMap<Annotation, Integer> annsLen = new HashMap<Annotation, Integer>();
        int maxLen = 0;
        for(Annotation ann: annotations){
            int annLen = ann.getTokens().size();
            annsLen.put(ann, annLen);
            if(annLen > maxLen){
                maxLen = annLen;
            }
        }
        List<Annotation> longestAnns = new ArrayList<Annotation>();
        for(Annotation ann: annsLen.keySet()){
            if(annsLen.get(ann) == maxLen){
                longestAnns.add(ann);
            }
        }
        return longestAnns;
    }
}
