package g419.liner2.api.converter;

import g419.corpus.Logger;
import g419.corpus.structure.*;
import g419.liner2.api.features.tokens.ClassFeature;
import g419.liner2.api.tools.MaltSentence;

import java.util.*;

/**
 * Created by michal on 2/20/15.
 */
public class AnnotationWrapConverter extends Converter {

    private ClassFeature classFeature = new ClassFeature("class");
    @Override
    public void finish(Document doc) {

    }

    @Override
    public void apply(Sentence sentence) {
//        MaltSentence maltSent = new MaltSentence(sentence, sentence.getChunks());
        HashMap<Token, String> textFormsMapping = new HashMap<>();
        HashSet<Token> annotationHeads = new HashSet<>();
        HashSet<Token> wrappedTokens = new HashSet<>();
        HashSet<Token> annotatedTokens = new HashSet<>();
        HashMap<Token, String> newAnns = new HashMap<>();
        ArrayList<Token> sentenceTokens = sentence.getTokens();
        for(Annotation ann: sentence.getChunks()){
            if(ann.getBegin() != ann.getEnd()){
                int substIdx = -1;
                int ignIdx = -1;
                for(int i=ann.getBegin(); i<= ann.getEnd();  i++ ){
                    String tokClass = classFeature.generate(sentenceTokens.get(i), sentence.getAttributeIndex());
                    if(tokClass != null){
                        if(tokClass.equals("subst")){
                            substIdx = i;
                            break;
                        }
                        else if(tokClass.equals("ign") && ignIdx == -1){
                            ignIdx = i;
                        }
                    }
                }

                int headIdx = substIdx != -1 ? substIdx : ignIdx;
                if(headIdx == -1){
                    headIdx = ann.getBegin();
                }
                Token head = sentenceTokens.get(headIdx);
                String oldText = ann.getText();
                setText(head, ann, sentenceTokens, headIdx);
                head.setNoSpaceAfter(sentenceTokens.get(ann.getEnd()).getNoSpaceAfter());
                textFormsMapping.put(head, oldText);
                annotationHeads.add(head);
                newAnns.put(head, ann.getType());
                wrappedTokens.add(head);
            }
            else{
                annotationHeads.add(sentenceTokens.get(ann.getBegin()));
                newAnns.put(sentenceTokens.get(ann.getBegin()), ann.getType());
            }
            ann.getTokens().forEach((token) -> annotatedTokens.add(sentenceTokens.get(token)));

        }
//        if(!newAnns.isEmpty()){
//            System.out.println("--------------");
//            System.out.println(sentence.toString());
//        }
        annotatedTokens.removeAll(annotationHeads); //tokens to remove
        sentenceTokens.removeAll(annotatedTokens);
//        System.out.println(sentence.toString());
        sentence.getChunks().clear();
        for(int i=0; i<sentenceTokens.size(); i++){
            if(newAnns.containsKey(sentenceTokens.get(i))){
                Annotation wrapped = new Annotation(i, newAnns.get(sentenceTokens.get(i)), sentence);
                sentence.addChunk(wrapped);
                if(textFormsMapping.containsKey(sentenceTokens.get(i))){
                    System.out.println(sentence.getId() + "\t" + i + "\t" + textFormsMapping.get(sentenceTokens.get(i)));
                }
            }
        }
    }

    private void setText(Token tok, Annotation ann, List<Token> sentenceTokens, int headIdx){
//        System.out.println(tok.getOrth() + " | " + ann.getText());
//        sentenceTokens.subList(ann.getBegin(), ann.getEnd() + 1).forEach((token) -> System.out.println(classFeature.generate(token, token.attrIdx)));
        String orth = tok.getOrth();
        String base = tok.getAttributeValue("base");
        List<Token> tokensAfter = sentenceTokens.subList(headIdx, Math.min(ann.getEnd() + 1, sentenceTokens.size()));
        List<Token> tokensBefore = sentenceTokens.subList(ann.getBegin(), headIdx);

        if(tok.getNoSpaceAfter()){
            for(int i=1; i<tokensAfter.size(); i++){
                Token t = tokensAfter.get(i);
                if(t.getNoSpaceAfter()){
                    orth += t.getOrth();
                    base += t.getAttributeValue("base");
                }
                else{
                    break;
                }
            }
        }

        for(int i=tokensBefore.size() - 1; i>=0; i--){
            Token t = tokensBefore.get(i);
            if(t.getNoSpaceAfter()){
                orth = t.getOrth() + orth;
                base = t.getAttributeValue("base") + base;
            }
            else{
                break;
            }
        }

        tok.setAttributeValue("orth", orth);
        tok.setAttributeValue("base", base);
//        System.out.println(tok.getOrth());
    }
}
