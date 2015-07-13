package g419.liner2.api.tools;

import g419.corpus.structure.*;
import g419.liner2.api.features.tokens.ClassFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

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
        ArrayList<Token> oldTokens = sentence.getTokens();
        for(int i=0; i<sentence.getTokenNumber(); i++){
            ArrayList<Annotation> tokenAnnsToWrap = getLongestAnns(sentence.getChunksAt(i, annotationTypes));
            if(tokenAnnsToWrap.isEmpty()){
                wrappedSent.addToken(oldTokens.get(i));
                newTokenIndexes.put(i, i);
                notWrappedTokens.add(i);
            }
            else{
                Annotation ann = tokenAnnsToWrap.get(0);
                for(int tokIdx: ann.getTokens()){
                    newTokenIndexes.put(i, tokIdx);
                }
                List<Token> tokensToWrap = oldTokens.subList(ann.getBegin(), ann.getEnd());
                Token head = getAnnotationHead(tokensToWrap, sentence);
                Token newToken = new WrappedToken(head.getOrth(), head.getTags().get(0), attrIdx, tokensToWrap, sentence);
                wrappedSent.addToken(newToken);
            }

            for(Annotation ann: tokenAnnsToWrap){
                wrappedSent.addChunk(new Annotation(i, ann.getType(), wrappedSent));
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
                wrappedSent.addChunk(newAnn);
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

    private static ArrayList<Annotation> getLongestAnns(ArrayList<Annotation> annotations){
        HashMap<Annotation, Integer> annsLen = new HashMap<Annotation, Integer>();
        int maxLen = 0;
        for(Annotation ann: annotations){
            int annLen = ann.getTokens().size();
            annsLen.put(ann, annLen);
            if(annLen > maxLen){
                maxLen = annLen;
            }
        }
        ArrayList<Annotation> longestAnns = new ArrayList<Annotation>();
        for(Annotation ann: annsLen.keySet()){
            if(annsLen.get(ann) == maxLen){
                longestAnns.add(ann);
            }
        }
        return longestAnns;
    }
}
