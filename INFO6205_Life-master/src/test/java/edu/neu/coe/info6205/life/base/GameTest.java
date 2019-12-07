package edu.neu.coe.info6205.life.base;

import edu.neu.coe.info6205.life.library.Library;
import io.jenetics.AnyGene;
import io.jenetics.Phenotype;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void testRunBlip() {
        String patternName = "Blip";
        System.out.println("Game of Life with starting pattern: " + patternName);
        final String pattern = Library.get(patternName);
        final Game.Behavior generations = Game.run(0L, pattern);
        assertEquals(0, generations.generation);
    }

    @Test
    public void testRunBlinker() {
        String patternName = "Blinker";
        System.out.println("Game of Life with starting pattern: " + patternName);
        final String pattern = Library.get(patternName);
        final Game.Behavior generations = Game.run(0L, pattern);
        assertEquals(new Game.Behavior(2, 0, 1), generations);
    }

    @Test
    public void generation() {
        // TODO implement test
    }

    @Test
    public void testFitness() {
        String patternName = "Blinker";
        System.out.println("Game of Life with starting pattern: " + patternName);
        final String pattern = Library.get(patternName);
        final long fitness = Game.fitness(pattern);
        assertEquals(2L, fitness);
    }

    @Test
    public void testRandomString() {
        String testString = Game.RandomString();
        assertFalse(testString.isEmpty());
    }

    @Test
    public void testFirstPatternWithoutMutation() {
        assertTrue(Game.findbestFirstPatternWithoutMutation().getFitness() >= 0);
    }

    @Test
    public void testGenoAndPheno() {
        Phenotype<AnyGene<String>, Long> pheno = Game.findbestFirstPatternWithoutMutation();

        assertTrue(!pheno.toString().equals(pheno.getGenotype().toString()));
    }
}
