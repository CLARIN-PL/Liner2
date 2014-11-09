package g419.corpus.structure;

import static org.junit.Assert.*;

import org.junit.Test;

public class AnnotationPositionComparatorTest {

	@Test
	public void testCompare() {
		int lower = -1;
		int equal = 0;
		int greater = 1;
		
		Sentence sentence1 = new Sentence();
		sentence1.setId("sentence1");
		Sentence sentence2 = new Sentence();
		sentence2.setId("sentence2");

		int begin1 = 0;
		int end1 = 0;
		int begin2 = 5;
		int end2 = 10;
		String type = "";
		Annotation annotation1 = new Annotation(begin1, end1, type, sentence1);
		Annotation annotation2 = new Annotation(begin1, end1, type, sentence1);
		Annotation annotation3 = new Annotation(begin2, end2, type, sentence1);
		Annotation annotation4 = new Annotation(begin1, end1, type, sentence2);
		Annotation annotation5 = new Annotation(begin1, end2, type, sentence2);

		AnnotationHeadComparator annotationHeadComparator = new AnnotationHeadComparator();
		
		assertEquals("Annotation is not equal to itself", equal, annotationHeadComparator.compare(annotation1, annotation1));
		assertEquals("Annotation is not equal to same annotation", equal, annotationHeadComparator.compare(annotation1, annotation2));
		assertEquals("Annotation is supposed to be lower if is earlier in senence", lower, annotationHeadComparator.compare(annotation1, annotation3));
		assertEquals("Annotation is supposed to be greater if is later in senence", greater, annotationHeadComparator.compare(annotation3, annotation1));
		assertEquals("Annotation is supposed to be lower if is set of tokens is smaller", greater, annotationHeadComparator.compare(annotation4, annotation5));
		assertEquals("Annotation is supposed to be greater if is set of tokens is bigger", greater, annotationHeadComparator.compare(annotation5, annotation4));
		
		//TODO What happens if two sentences are different?
	}

}
