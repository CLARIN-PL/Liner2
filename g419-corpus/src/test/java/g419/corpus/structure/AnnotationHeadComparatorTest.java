package g419.corpus.structure;

import org.junit.Assert;
import org.junit.Test;

public class AnnotationHeadComparatorTest {

	@Test
	public void testAnnotationHeadComparator() {
		AnnotationHeadComparator annotationHeadComparator = new AnnotationHeadComparator();

		Assert.assertNotNull("Constructor failed to create an object", annotationHeadComparator);
	}

	@Test
	public void testAnnotationHeadComparatorBoolean() {
		AnnotationHeadComparator annotationHeadComparator1 = new AnnotationHeadComparator(true);
		AnnotationHeadComparator annotationHeadComparator2 = new AnnotationHeadComparator(false);

		Assert.assertNotNull("Constructor failed to create an object", annotationHeadComparator1);
		Assert.assertNotNull("Constructor failed to create an object", annotationHeadComparator2);
	}

	@Test
	public void testCompare() {
		// ToDo: Fix the test
//		Sentence sentence = new Sentence();
//		sentence.id = "someId";
//		int begin = 0;
//		String type = "";
//		Annotation annotation1 = new Annotation(begin, type, sentence);
//		Annotation annotation2 = new Annotation(begin, type, sentence);
//
//		AnnotationHeadComparator annotationHeadComparator = new AnnotationHeadComparator(true);
//		Assert.assertEquals(0,annotationHeadComparator.compare(annotation1, annotation2));
//
		//TODO Add other scenarios for -1 and +1 (also edge scenarios)
	}

}
