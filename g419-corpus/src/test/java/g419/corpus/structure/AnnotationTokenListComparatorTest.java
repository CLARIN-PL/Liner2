package g419.corpus.structure;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AnnotationTokenListComparatorTest {

	@Test
	public void testAnnotationTokenListComparator() {
		AnnotationTokenListComparator annotationTokenListComparator = new AnnotationTokenListComparator();
		assertNotNull(annotationTokenListComparator);
	}

	@Test
	public void testAnnotationTokenListComparatorBoolean() {
		AnnotationTokenListComparator annotationTokenListComparator;
		
		// Scenario 1 (true)
		annotationTokenListComparator = new AnnotationTokenListComparator(true);
		assertNotNull(annotationTokenListComparator);
		// Scenario 2 (false)
		annotationTokenListComparator = new AnnotationTokenListComparator(false);
		assertNotNull(annotationTokenListComparator);
	}

	@Test
	public void testCompare() {
		//fail("Not yet implemented");
	}

}
