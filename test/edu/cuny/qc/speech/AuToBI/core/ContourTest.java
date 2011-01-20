package edu.cuny.qc.speech.AuToBI.core;

import edu.cuny.qc.speech.AuToBI.featureextractor.NormalizedContourFeatureExtractor;
import edu.cuny.qc.speech.AuToBI.util.ContourUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for Contour.
 *
 * @see Contour
 */
public class ContourTest {

  @Test
  public void testConstructor() {
    Contour c = new Contour(0.0, 0.001, 6);
    assertEquals(6, c.size());
  }

  @Test
  public void testConstructorWithValues() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(0.0, 0.001, values);
    assertEquals(6, c.size());
  }

  @Test
  public void testTimeFromIndex() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    assertEquals(2.002, c.timeFromIndex(2), 0.00001);
  }

  @Test
  public void testIndexFromTime() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    assertEquals(4, c.indexFromTime(2.004));
  }

  @Test
  public void testSetEmpty() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    c.setEmpty(2);
    assertTrue(c.isEmpty(2));
  }

  @Test
  public void testContentSize() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};

    Contour c = new Contour(2.0, 0.001, values);
    c.setEmpty(2);
    assertEquals(5, c.contentSize());
  }

  @Test
  public void testContourForEach() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);


    int i = 0;
    for (Pair<Double, Double> tvp : c) {
      assertEquals(2.0 + 0.001 * i, tvp.first, 0.0001);
      assertEquals(0.1 * i, tvp.second, 0.0001);
      ++i;
    }
    assertEquals(6, i);
  }

  @Test
  public void testContourForEachSkipsEmptyEntries() {
    double[] values = new double[]{0.0, 0.1, 0.2, 0.3, 0.4, 0.5};
    Contour c = new Contour(2.0, 0.001, values);

    c.setEmpty(2);
    c.setEmpty(3);

    int i = 0;
    for (Pair<Double, Double> tvp : c) {
      if (i < 2) {
        assertEquals(2.0 + 0.001 * i, tvp.first, 0.0001);
        assertEquals(0.1 * i, tvp.second, 0.0001);
      } else {
        assertEquals(2.0 + 0.001 * (i + 2), tvp.first, 0.0001);
        assertEquals(0.1 * (i + 2), tvp.second, 0.0001);
      }
      ++i;
    }
    assertEquals(4, i);
  }
}