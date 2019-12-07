package edu.neu.coe.info6205.life.base;

import edu.neu.coe.info6205.life.library.Library;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FirstPatternTest {
    @Test
    public void testGetFirstPattern() {
        String patternName = "Blinker";
        final String pattern = Library.get(patternName);
        GetFirstPattern gfp = GetFirstPattern.of(pattern);
        assertFalse(gfp.toString().isEmpty());
    }

    @Test
    public void testGetFitness() {
        String patternName = "Blinker";
        final String pattern = Library.get(patternName);
        GetFirstPattern gfp = GetFirstPattern.of(pattern);
        assertTrue(gfp.fitness().apply(gfp.get_points()) >= 0L);
    }
}
