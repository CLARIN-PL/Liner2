package g419.corpus.structure

import spock.lang.Shared
import spock.lang.Specification

import java.util.regex.Pattern

class DocumentTest extends Specification {
    @Shared
            sampleSentence1,
            sampleSentence2,
            attrIndex,
            paragraph1,
            paragraph21

    def setup() {
        attrIndex = new TokenAttributeIndex()
        attrIndex.addAttribute("orth")
        sampleSentence1 = new Sentence(attrIndex)
        sampleSentence1.addToken(new Token("Ala", new Tag("Ala", "subst:sg:nom:f", true), attrIndex))
        sampleSentence1.addToken(new Token("ma", new Tag("mieć", "fin:sg:ter:imperf", true), attrIndex))
        Token tok = new Token("kota", new Tag("kot", "subst:sg:gen:m2", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence1.addToken(tok)
        sampleSentence1.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))

        sampleSentence2 = new Sentence(attrIndex)
        sampleSentence2.addToken(new Token("Kot", new Tag("kot", "subst:sg:nom:m2", true), attrIndex))
        sampleSentence2.addToken(new Token("jest", new Tag("być", "fin:sg:ter:imperf", true), attrIndex))
        Token tok2 = new Token("Ali", new Tag("Ala", "subst:sg:gen:f", true), attrIndex)
        tok.setNoSpaceAfter(true)
        sampleSentence2.addToken(tok2)
        sampleSentence2.addToken(new Token(".", new Tag(".", "interp", true), attrIndex))

        paragraph1 = new Paragraph("p1", attrIndex)
        paragraph1.addSentence(sampleSentence1)

        paragraph21 = new Paragraph("p2", attrIndex)
        paragraph21.addSentence(sampleSentence2)
        paragraph21.addSentence(sampleSentence1)
    }

    def "Should create Document(String, TokenAttributeIndex)"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.name == "doc_1"
        document.attributeIndex == attrIndex
        document.paragraphs == []
        document.uri == null
    }

    def "Should create Document(String, List<Paragraph>, TokenAttributeIndex)"() {
        given:
        Document document = new Document("doc_1", [paragraph1, paragraph21], attrIndex)

        expect:
        document.name == "doc_1"
        document.attributeIndex == attrIndex
        document.paragraphs == [paragraph1, paragraph21]
        document.uri == null
    }

    def "Should create Document(String, List<Paragraph>, TokenAttributeIndex, RelationSet"() {
        given:
        RelationSet relationsSet = new RelationSet()
        Document document = new Document("doc_1", [paragraph1, paragraph21], attrIndex, relationsSet)

        expect:
        document.name == "doc_1"
        document.attributeIndex == attrIndex
        document.paragraphs == [paragraph1, paragraph21]
        document.uri == null
        document.relations.relations == relationsSet.relations
    }

    def "Should set name"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.name == "doc_1"

        when:
        document.setName("doc_2")

        then:
        document.getName() == "doc_2"
    }

    def "Should get name"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getName() == "doc_1"
    }

    def "Should get attribute index"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getAttributeIndex() == attrIndex

        when:
        TokenAttributeIndex newAttr = new TokenAttributeIndex()
        document.attributeIndex = newAttr

        then:
        document.getAttributeIndex() == newAttr

    }

    def "Should set attribute idnex"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getAttributeIndex() == attrIndex

        when:
        TokenAttributeIndex newAttr = new TokenAttributeIndex()
        document.setAttributeIndex(newAttr)

        then:
        document.attributeIndex == newAttr
    }

    def "Should set uri"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        document.uri = "test_uri"

        when:
        document.setUri("test_set_uri")

        then:
        document.uri == "test_set_uri"
    }

    def "Should get uri"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getUri() == null

        when:
        document.uri = "test_uri"

        then:
        document.getUri() == "test_uri"
    }

    def "Should set relations"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.relations.relations == (HashSet) []

        when:
        final int begin1 = 0
        final int begin2 = 2
        final String type1 = "type1"
        final String type2 = "type2"

        final Annotation annotation1 = new Annotation(begin1, type1, sampleSentence1)
        final Annotation annotation2 = new Annotation(begin1, type1, sampleSentence1)
        final Annotation annotation3 = new Annotation(begin2, type2, sampleSentence1)
        final Annotation annotation4 = new Annotation(begin2, type2, sampleSentence2)

        final Relation relation1 = new Relation(annotation1, annotation2, type1)
        final Relation relation2 = new Relation(annotation2, annotation3, type1)
        final Relation relation3 = new Relation(annotation3, annotation4, type2)
        final Relation relation4 = new Relation(annotation4, annotation1, type2)

        final RelationSet relations = new RelationSet()

        relations.addRelation(relation1)
        relations.addRelation(relation2)
        relations.addRelation(relation3)
        relations.addRelation(relation4)

        document.setRelations(relations)

        then:
        document.relations == relations
    }

    def "Should get relations"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getRelations().relations == (HashSet) []

        when:
        final int begin1 = 0
        final int begin2 = 2
        final String type1 = "type1"
        final String type2 = "type2"

        final Annotation annotation1 = new Annotation(begin1, type1, sampleSentence1)
        final Annotation annotation2 = new Annotation(begin1, type1, sampleSentence1)
        final Annotation annotation3 = new Annotation(begin2, type2, sampleSentence1)
        final Annotation annotation4 = new Annotation(begin2, type2, sampleSentence2)

        final Relation relation1 = new Relation(annotation1, annotation2, type1)
        final Relation relation2 = new Relation(annotation2, annotation3, type1)
        final Relation relation3 = new Relation(annotation3, annotation4, type2)
        final Relation relation4 = new Relation(annotation4, annotation1, type2)

        final RelationSet relations = new RelationSet()

        relations.addRelation(relation1)
        relations.addRelation(relation2)
        relations.addRelation(relation3)
        relations.addRelation(relation4)

        document.relations = relations

        then:
        document.getRelations().relations == relations.relations
    }

    def "Should add paragraph"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.paragraphs == []

        when:
        document.addParagraph(paragraph1)

        then:
        document.paragraphs == [paragraph1]

        when:
        document.addParagraph(paragraph21)

        then:
        document.paragraphs == [paragraph1, paragraph21]
    }

    def "Should get paragraphs"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getParagraphs() == []

        when:
        document.paragraphs = [paragraph1, paragraph21]

        then:
        document.getParagraphs() == [paragraph1, paragraph21]
    }

    def "Should get chunkings"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getChunkings() == (HashMap) []

        when:
        // ann_sentNbAnnNb
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_21 = new Annotation(2, 2, "imie", sampleSentence2)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_22 = new Annotation(0, 0, "zwierze", sampleSentence2)
        final Annotation annotation_13 = new Annotation(0, 3, "zdanie", sampleSentence1)
        final Annotation annotation_23 = new Annotation(0, 3, "zdanie", sampleSentence2)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence1)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence2)

        sampleSentence1.setAnnotations(annotationSet1)
        sampleSentence2.setAnnotations(annotationSet2)

        annotationSet1.addChunk(annotation_11)
        annotationSet1.addChunk(annotation_12)
        annotationSet1.addChunk(annotation_13)
        annotationSet2.addChunk(annotation_21)
        annotationSet2.addChunk(annotation_22)
        annotationSet2.addChunk(annotation_23)

        Paragraph _paragraph1 = new Paragraph("test_1")
        Paragraph _paragraph2 = new Paragraph("test_2")

        _paragraph1.addSentence(sampleSentence1)
        _paragraph2.addSentence(sampleSentence2)

        document.addParagraph(_paragraph1)

        HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>()
        chunkings.put(sampleSentence1, annotationSet1)

        then:
        document.getChunkings() == chunkings

        when:
        document.addParagraph(_paragraph2)
        chunkings.put(sampleSentence2, annotationSet2)

        then:
        document.getChunkings() == chunkings
    }

    def "Should replace chunkings"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getChunkings() == (HashMap) []

        when:
        // ann_sentNbAnnNb
        final Annotation annotation_1 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_2 = new Annotation(2, 2, "imie", sampleSentence1)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence1)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence1)

        sampleSentence1.setAnnotations(annotationSet1)

        annotationSet1.addChunk(annotation_1)
        annotationSet1.addChunk(annotation_2)
        annotationSet2.addChunk(annotation_1)
        annotationSet2.addChunk(annotation_2)

        Paragraph _paragraph1 = new Paragraph("test_1")

        _paragraph1.addSentence(sampleSentence1)

        document.addParagraph(_paragraph1)

        HashMap<Sentence, AnnotationSet> chunkings = new HashMap<>()
        chunkings.put(sampleSentence1, annotationSet1)

        then:
        document.getChunkings() == chunkings

        when:
        HashMap<Sentence, AnnotationSet> annSetToSet = new HashMap<>()
        annSetToSet.put(sampleSentence1, annotationSet2)
        document.setAnnotations(annSetToSet)

        then:
        document.getChunkings() == annSetToSet
    }

    def "Should get sentences"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getSentences() == []

        when:
        // ann_sentNbAnnNb
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_21 = new Annotation(2, 2, "imie", sampleSentence2)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_22 = new Annotation(0, 0, "zwierze", sampleSentence2)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence1)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence2)

        sampleSentence1.setAnnotations(annotationSet1)
        sampleSentence2.setAnnotations(annotationSet2)

        annotationSet1.addChunk(annotation_11)
        annotationSet1.addChunk(annotation_12)
        annotationSet2.addChunk(annotation_21)
        annotationSet2.addChunk(annotation_22)

        Paragraph _paragraph1 = new Paragraph("test_1")
        Paragraph _paragraph2 = new Paragraph("test_2")

        _paragraph1.addSentence(sampleSentence1)
        _paragraph2.addSentence(sampleSentence2)

        document.addParagraph(_paragraph1)

        then:
        document.getSentences() == [sampleSentence1]

        when:
        document.addParagraph(_paragraph2)

        then:
        document.getSentences() == [sampleSentence1, sampleSentence2]

    }

    def "Should get annotations"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_21 = new Annotation(2, 2, "imie", sampleSentence2)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_22 = new Annotation(0, 0, "zwierze", sampleSentence2)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence1)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence2)

        sampleSentence1.setId("id_1")
        sampleSentence2.setId("id_2")

        sampleSentence1.setAnnotations(annotationSet1)
        sampleSentence2.setAnnotations(annotationSet2)

        annotationSet1.addChunk(annotation_11)
        annotationSet1.addChunk(annotation_12)
        annotationSet2.addChunk(annotation_21)
        annotationSet2.addChunk(annotation_22)

        Paragraph _paragraph1 = new Paragraph("test_1")
        Paragraph _paragraph2 = new Paragraph("test_2")

        _paragraph1.addSentence(sampleSentence1)
        _paragraph2.addSentence(sampleSentence2)

        document.addParagraph(_paragraph1)
        document.addParagraph(_paragraph2)

        expect:
        document.getAnnotations() == [annotation_11, annotation_12, annotation_21, annotation_22]
        document.getAnnotations([Pattern.compile("imie")]) == [annotation_11, annotation_21]
        document.getAnnotation(sampleSentence1.getId(), "zwierze", 0) == annotation_12
    }

    def "Should remove annotations"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_21 = new Annotation(2, 2, "imie", sampleSentence2)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_22 = new Annotation(0, 0, "zwierze", sampleSentence2)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence1)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence2)

        sampleSentence1.setAnnotations(annotationSet1)
        sampleSentence2.setAnnotations(annotationSet2)

        annotationSet1.addChunk(annotation_11)
        annotationSet1.addChunk(annotation_12)
        annotationSet2.addChunk(annotation_21)
        annotationSet2.addChunk(annotation_22)

        Paragraph _paragraph1 = new Paragraph("test_1")
        Paragraph _paragraph2 = new Paragraph("test_2")

        _paragraph1.addSentence(sampleSentence1)
        _paragraph2.addSentence(sampleSentence2)

        document.addParagraph(_paragraph1)
        document.addParagraph(_paragraph2)

        expect:
        document.getAnnotations() == [annotation_11, annotation_12, annotation_21, annotation_22]

        when:
        document.removeAnnotations([annotation_21, annotation_22])

        then:
        document.getAnnotations() == [annotation_11, annotation_12]

        when:
        document.removeAnnotations('zwierze')

        then:
        document.getAnnotations() == [annotation_11]

        when:
        document.removeAnnotations()

        then:
        document.getAnnotations() == []
    }

    def "Should remove annotations by type pattern"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        Pattern pattern = Pattern.compile("imie")
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_21 = new Annotation(2, 2, "imie", sampleSentence2)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_22 = new Annotation(0, 0, "zwierze", sampleSentence2)

        AnnotationSet annotationSet1 = new AnnotationSet(sampleSentence1)
        AnnotationSet annotationSet2 = new AnnotationSet(sampleSentence2)

        sampleSentence1.setAnnotations(annotationSet1)
        sampleSentence2.setAnnotations(annotationSet2)

        annotationSet1.addChunk(annotation_11)
        annotationSet1.addChunk(annotation_12)
        annotationSet2.addChunk(annotation_21)
        annotationSet2.addChunk(annotation_22)

        Paragraph _paragraph1 = new Paragraph("test_1")
        Paragraph _paragraph2 = new Paragraph("test_2")

        _paragraph1.addSentence(sampleSentence1)
        _paragraph2.addSentence(sampleSentence2)

        document.addParagraph(_paragraph1)
        document.addParagraph(_paragraph2)

        expect:
        document.getAnnotations() == [annotation_11, annotation_12, annotation_21, annotation_22]

        when:
        document.removeAnnotationsByTypePatterns([pattern])

        then:
        document.getAnnotations() == [annotation_12, annotation_22]
    }

    def "Should get document descriptor"() {
        given:
        Document document = new Document("doc_1", attrIndex)

        expect:
        document.getDocumentDescriptor() == new DocumentDescriptor()
    }

    def "Should add relation"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_21 = new Annotation(2, 2, "imie", sampleSentence2)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_22 = new Annotation(0, 0, "zwierze", sampleSentence2)

        final Relation relation1 = new Relation(annotation_11, annotation_12, "test_1")
        final Relation relation2 = new Relation(annotation_21, annotation_22, "test_2")

        expect:
        document.relations.relations == (Set) []

        when:
        document.addRelation(relation1)

        then:
        document.relations.relations == (Set) [relation1]

        when:
        document.addRelation(relation2)

        then:
        document.relations.relations == (Set) [relation1, relation2]
    }

    def "Should add annotations"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        final Annotation annotation_11 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_12 = new Annotation(2, 2, "zwierze", sampleSentence1)
        final Annotation annotation_21 = new Annotation(0, 0, "zwierze", sampleSentence2)
        final Annotation annotation_22 = new Annotation(2, 2, "imie", sampleSentence2)

        AnnotationSet annSet1 = new AnnotationSet(sampleSentence1)
        annSet1.addChunk(annotation_11)
        annSet1.addChunk(annotation_12)
        AnnotationSet annSet2 = new AnnotationSet(sampleSentence1)
        annSet2.addChunk(annotation_21)
        annSet2.addChunk(annotation_22)

        Paragraph paragraph1 = new Paragraph("test_1")
        paragraph1.addSentence(sampleSentence1)
        Paragraph paragraph2 = new Paragraph("test_2")
        paragraph2.addSentence(sampleSentence2)

        HashMap<Sentence, AnnotationSet> chunkings1 = new HashMap()
        chunkings1.put(sampleSentence1, annSet1)
        HashMap<Sentence, AnnotationSet> chunkings2 = new HashMap()
        chunkings2.put(sampleSentence2, annSet2)

        expect:
        document.getAnnotations() == []

        when:
        document.addParagraph(paragraph1)

        then:
        document.getAnnotations() == []

        when:
        document.addAnnotations(chunkings1)

        then:
        document.getAnnotations() == [annotation_11, annotation_12]

        when:
        document.addAnnotations(chunkings2)

        then:
        document.getAnnotations() == [annotation_11, annotation_12]

        when:
        document.addParagraph(paragraph2)

        then:
        document.getAnnotations() == [annotation_11, annotation_12]

        when:
        document.addAnnotations(chunkings2)

        then:
        document.getAnnotations() == [annotation_11, annotation_12, annotation_21, annotation_22]
    }

    def "Should clone document"() {
        given:
        Document document = new Document("doc_1", attrIndex)
        final Annotation annotation_1 = new Annotation(0, 0, "imie", sampleSentence1)
        final Annotation annotation_2 = new Annotation(2, 2, "zwierze", sampleSentence1)

        AnnotationSet annSet = new AnnotationSet(sampleSentence1)
        annSet.addChunk(annotation_1)
        annSet.addChunk(annotation_2)
        sampleSentence1.addAnnotations(annSet)

        Document clonedDocument = document.clone()

        expect:
        clonedDocument.getSentences().equals(document.getSentences())
        clonedDocument.getAnnotations() == document.getAnnotations()

        when:
        document.addParagraph(paragraph1)

        then:
        !clonedDocument.getSentences().equals(document.getSentences())
        clonedDocument.getAnnotations() != document.getAnnotations()

        when:
        clonedDocument = document.clone()

        then:
        clonedDocument.getSentences().toString() == document.getSentences().toString()
        clonedDocument.getAnnotations() == document.getAnnotations()
    }
}
