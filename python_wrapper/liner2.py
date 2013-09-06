#! /usr/bin/python
# -*- coding: utf-8 -*-
from jpype import *
import tempfile
import corpus2

startJVM(getDefaultJVMPath(), "-Djava.library.path=../lib", "-Djava.class.path=../liner2.jar")

class Liner2(object):
    def __init__(self, liner_ini, tagset='nkjp'):
        self.tagset = corpus2.get_named_tagset(tagset)
        ChunkerFactory = JClass("liner2.chunker.factory.ChunkerFactory")
        self.options = JClass("liner2.LinerOptions")()
        self.options.loadIni(liner_ini)
        self.chunkerManager = ChunkerFactory.loadChunkers(self.options.chunkersDescriptions)
        if not self.options.features.isEmpty():
            self.featureGen = JClass("liner2.features.TokenFeatureGenerator")(self.options.features)
        else:
            self.featureGen = None

    def add_chunker(self, name, description):
        self.chunkerManager.addChunker(name, description)

    def get_chunker(self, name=''):
        if not name:
            name = self.options.getOptionUse()
        return self.chunkerManager.getChunkerByName(name)
 

    def process_sentence(self, sentence, chunker_name='', only_new=False):
        chunker = self.get_chunker(chunker_name)
        sentence = sentence.clone_shared()
        if only_new:
            annotated_sentence = corpus2.AnnotatedSentence.wrap_sentence(sentence)
            for chan in annotated_sentence.all_channels():
                annotated_sentence.remove_channel(chan)
        ps = self.prepare_paragraph_set()
        p = JClass("liner2.structure.Paragraph")("ch1")
        liner_sentence = self.corpus_sent_to_liner(sentence)
        p.addSentence(liner_sentence)
        ps.addParagraph(p)
        self.generate_features(ps)
        chunker.chunkInPlace(ps)
        self.liner_annotations_to_corpus_sentence(liner_sentence, sentence)
        return sentence

    def process_document(self, document, chunker_name='', only_new=False):
        chunker = self.get_chunker(chunker_name)
        ps = self.prepare_paragraph_set()
        for paragraph in document.paragraphs():
            p = JClass("liner2.structure.Paragraph")(paragraph.get_attribute('id'))
            for sentence in paragraph.sentences():
                sentence = sentence.clone_shared()
                if only_new:
                    annotated_sentence = corpus2.AnnotatedSentence.wrap_sentence(sentence)
                    for chan in annotated_sentence.all_channels():
                        annotated_sentence.remove_channel(chan)
                p.addSentence(self.corpus_sent_to_liner(sentence))
            ps.addParagraph(p)
        self.generate_features(ps)
        chunker.chunkInPlace(ps)
        result_document = corpus2.Document()
        for liner_paragraph, corpus_paragraph in zip(ps.getParagraphs(), document.paragraphs()):
            p = corpus_paragraph.clone_shared()
            result_document.add_paragraph(p)
            for liner_sentence, corpus_sentence in zip(liner_paragraph.getSentences(), p.sentences()):
                self.liner_annotations_to_corpus_sentence(liner_sentence, corpus_sentence)
        return result_document

    def generate_features(self, ps):
        if self.featureGen is not None:
            self.featureGen.generateFeatures(ps)

    def prepare_paragraph_set(self):
        attribute_index = JClass("liner2.structure.TokenAttributeIndex")()
        attribute_index.addAttribute("orth");
        attribute_index.addAttribute("base");
        attribute_index.addAttribute("ctag"); 
        ps = JClass("liner2.structure.ParagraphSet")()
        ps.setAttributeIndex(attribute_index)
        return ps

    def corpus_sent_to_liner(self, corpus2_sentence):
        sentence = JClass("liner2.structure.Sentence")()
        sentence.setId(corpus2_sentence.id())
        for token in corpus2_sentence.tokens():
            sentence.addToken(self.corpus_token_to_liner(token))
        asent = corpus2.AnnotatedSentence.wrap_sentence(corpus2_sentence)
        for chan_name in asent.all_channels():
            for ann in asent.get_channel(chan_name).make_annotation_vector():
                indices = [i for i in ann.indices]
                annotation = JClass("liner2.structure.Annotation")(indices[0], chan_name.decode('utf-8'), ann.seg_number, sentence)
                for i in indices[1:]:
                    annotation.addToken(i)
                annotation.setHead(ann.head_index)
                sentence.addChunk(annotation)
        return sentence

    def corpus_token_to_liner(self, corpus2_token):
        token = JClass("liner2.structure.Token")()
        token.setAttributeValue(0, corpus2_token.orth_utf8().decode('utf-8'))
        has_preffered_lexeme = False
        for lex in corpus2_token.lexemes():
            ctag = self.tagset.tag_to_string(lex.tag())
            if not has_preffered_lexeme and lex.is_disamb():
                token.setAttributeValue(1, lex.lemma_utf8().decode('utf-8'))
                token.setAttributeValue(2, ctag)
                has_preffered_lexeme = True
            new_tag = JClass("liner2.structure.Tag")(lex.lemma_utf8().decode('utf-8'), ctag, lex.is_disamb())
            token.addTag(new_tag)
        return token

    def liner_annotations_to_corpus_sentence(self, liner_sentence, corpus_sentence):
        annotated_sentence = corpus2.AnnotatedSentence.wrap_sentence(corpus_sentence)
        for chan in annotated_sentence.all_channels():
                annotated_sentence.remove_channel(chan)
        for ann in liner_sentence.getChunks():
            chan_name = str(ann.getType().encode('utf-8'))
            if not annotated_sentence.has_channel(chan_name):
                annotated_sentence.create_channel(chan_name)    
            chan = annotated_sentence.get_channel(chan_name)
            new_ann_idx = chan.get_new_segment_index()
            for tok_idx in ann.getTokens():
                tok_idx = int(tok_idx.toString())
                chan.set_segment_at(tok_idx, new_ann_idx)
        for chan_name in annotated_sentence.all_channels():
            chan = annotated_sentence.get_channel(chan_name)
            chan.renumber_segments()
