package edu.neu.coe.info6205.life.base;

import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class Game implements Generational<Game, Grid>, Countable, Renderable {

    /**
     * Method to get the cell count.
     *
     * @return the number of live cells.
     */
    @Override
    public int getCount() {
        return grid.getCount();
    }

    @Override
    public String toString() {
        return "Game{" +
                "grid=" + grid +
                ", generation=" + generation +
                '}';
    }

    /**
     * Method to test equality, ignoring generation.
     *
     * @param o the other Game.
     * @return true if this and o are equivalent.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Game)) return false;
        Game game = (Game) o;
        return grid.equals(game.grid);
    }

    /**
     * Method to generate a hashCode, ignoring generation.
     *
     * @return hashCode for this.
     */
    @Override
    public int hashCode() {
        return Objects.hash(grid);
    }

    @Override
    public Game generation(BiConsumer<Long, Grid> monitor) {
        monitor.accept(generation, grid);
        return new Game(generation + 1, grid.generation(this.monitor), this, this.monitor);
    }

    public Game(long generation, BiConsumer<Long, Group> monitor) {
        this(generation, new Grid(generation), null, monitor);
    }

    public Game(long generation) {
        this(generation, (l, g) -> {
        });
    }

    public Game() {
        this(0L);
    }

    @Override
    public String render() {
        return grid.render();
    }

    /**
     * Get the (unique) Group belonging to the grid.
     *
     * @return a Group.
     */
    public Group getGroup() {
        return grid.getGroup();
    }

    /**
     * Method to get a very crude measure of growth rate based on this Game and the very first Game in the series.
     *
     * @return a double which will be zero for expired Games and 1.0 for stable games.
     * Anything else suggests sustained growth.
     */
    public double growthRate() {
        Game game = this;
        while (game.previous != null) {
            game = game.previous;
        }
        long growth = (long) getCount() - game.getCount();
        long generations = generation - game.generation;
        return generations > 0 ? growth * 1.0 / generations : -0.1;
    }

    public static final int MaxGenerations = 1000;

    /**
     * Main program for Game of Life.
     *
     * @param args the name of the starting pattern (defaults to "Blip")
     */

    public static long fitness(final String pattern) {
        final Behavior generations = run(0L, pattern);
        return generations.generation;
    }

    public static String RandomString() {
        Random random = new Random();
        int length = random.nextInt(10);
        length = length == 0 ? 2 : length * 2;
        int min = -15;
        int max = 15;
        String s = "";
        for (int i = 0; i < length; i++) {
            if (i != 0 && i % 2 == 0) s += ",";
            s += random.nextInt(max - min) + min;
            s += " ";
        }
        return s;
    }

    public static Phenotype<AnyGene<String>, Long> findbestFirstPatternWithoutMutation() {
        final Engine<AnyGene<String>, Long> engine = Engine // Create a new builder with the given fitness
                .builder(
                        Game::fitness, Codecs.ofScalar(Game::RandomString))
                .optimize(Optimize.MAXIMUM)
                .build();

        return engine.stream()
                .limit(50)
                .collect(toBestPhenotype());
    }

    public static void main(String[] args) {
//        String patternName = args.length > 0 ? args[0] : "Blip";
//        System.out.println("Game of Life with starting pattern: " + patternName);
//        final String pattern = Library.get(patternName);
//        final Behavior generations = run(0L, pattern);
//        System.out.println("Ending Game of Life after " + generations + " generations");

        Phenotype<AnyGene<String>, Long> bestWithoutMutation = findbestFirstPatternWithoutMutation();
        String bestPatternWithoutMutation = bestWithoutMutation.getGenotype().toString().substring(2, bestWithoutMutation.getGenotype().toString().length() - 2);

        GetFirstPattern gfp = GetFirstPattern.of(bestPatternWithoutMutation);
        Engine<EnumGene<Integer>, Long> engine = Engine
                .builder(gfp)
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new Mutator<>(0.6),
                        new SinglePointCrossover<>(0.3)
                )
                .build();
        EvolutionStatistics<Long, ?>
                statistics = EvolutionStatistics.ofNumber();
        Phenotype<EnumGene<Integer>, Long> best =
                engine.stream()
                        .limit(bySteadyFitness(5))
                        .limit(100)
                        .peek(statistics)
                        .collect(toBestPhenotype());
//        System.out.println(statistics);

        String[] patterns = best.getGenotype().toString().substring(1, best.getGenotype().toString().length() - 1).split("\\|");
        String pattern = "";
        for (int i = 0; i < patterns.length; i += 2) {
            pattern += patterns[i] + " " + patterns[i + 1] + " ,";
        }
        pattern = pattern.substring(0, pattern.length() - 1);
        int evolution = 1;
        long bestFitness = best.getFitness();
        long bestFitnesWithoutMutation = bestWithoutMutation.getFitness();
        long currentFitness = bestFitness;

        ArrayList<Long> fitnessArray = new ArrayList<Long>();
        fitnessArray.add(bestFitness);
        ArrayList<String> patternArray = new ArrayList<String>();
        String currentPattern = pattern;
        patternArray.add(currentPattern);
        while (currentFitness > 0 && bestFitness < 1000L) {
            gfp = GetFirstPattern.of(pattern);
            engine = Engine
                    .builder(gfp)
                    .optimize(Optimize.MAXIMUM)
                    .alterers(
                            new Mutator<>(0.6),
                            new SinglePointCrossover<>(0.3)
                    )
                    .build();
            best = engine.stream()
                    .limit(bySteadyFitness(20))
                    .limit(100)
                    .peek(statistics)
                    .collect(toBestPhenotype());
            if (best.getFitness().longValue() >= bestFitness) {
                patterns = best.getGenotype().toString().substring(1, best.getGenotype().toString().length() - 1).split("\\|");
                pattern = "";
                for (int i = 0; i < patterns.length; i += 2) {
                    pattern += patterns[i] + " " + patterns[i + 1] + " ,";
                }
                pattern = pattern.substring(0, pattern.length() - 1);
                bestFitness = best.getFitness();
            }

            String[] currentPatterns = best.getGenotype().toString().substring(1, best.getGenotype().toString().length() - 1).split("\\|");
            currentPattern = "";
            for (int i = 0; i < currentPatterns.length; i += 2) {
                currentPattern += currentPatterns[i] + " " + currentPatterns[i + 1] + " ,";
            }
            currentPattern = currentPattern.substring(0, currentPattern.length() - 1);
            patternArray.add(currentPattern);
            currentFitness = best.getFitness();
            fitnessArray.add(currentFitness);
            evolution++;
        }

        System.out.println("Data:");
        for (int i = 0; i < fitnessArray.size() && i < patternArray.size(); i++) {
            System.out.println("[" + patternArray.get(i) + "] -> " + fitnessArray.get(i));
        }

        System.out.println(statistics);

        int count = 0;
        for (; count < fitnessArray.size() - 1; count++) {
            System.out.print(fitnessArray.get(count) + " -> ");
        }
        System.out.println(fitnessArray.get(count));

        System.out.println("Result: ");
        System.out.println("the bestWithoutMutation is : " + bestPatternWithoutMutation);
        System.out.println("the bestWithoutMutation fitness is : " + bestFitnesWithoutMutation);

        if (bestFitness >= bestFitnesWithoutMutation) {
            System.out.println("Evolution " + evolution + " recursion");
            System.out.println("Found Best Pattern : " + pattern);
            System.out.println("Found Best Fitness : " + bestFitness);
        } else {
            System.out.println("Didn't evaluate pattern better than first pattern");
            System.out.println("Found Best Pattern : " + bestPatternWithoutMutation);
            System.out.println("Found Best Pattern : " + bestFitnesWithoutMutation);
        }
    }

    /**
     * Run the game starting with pattern.
     *
     * @param generation the starting generation.
     * @param pattern    the pattern name.
     * @return the generation at which the game expired.
     */
    public static Behavior run(long generation, String pattern) {
        return run(generation, pattern, MaxGenerations);
    }

    /**
     * Run the game starting with pattern.
     *
     * @param generation     the starting generation.
     * @param pattern        the pattern name.
     * @param maxGenerations the maximum number of generations for this run.
     * @return the generation at which the game expired.
     */
    public static Behavior run(long generation, String pattern, int maxGenerations) {
        return run(generation, Point.points(pattern), maxGenerations);
    }

    /**
     * Run the game starting with a list of points.
     *
     * @param generation     the starting generation.
     * @param points         a list of points in Grid coordinates.
     * @param maxGenerations the maximum number of generations for this run.
     * @return the generation at which the game expired.
     */
    public static Behavior run(long generation, List<Point> points, int maxGenerations) {
        return run(create(generation, points), (l, g) -> System.out.println("generation " + l + "; grid=" + g), maxGenerations);
    }

    /**
     * Factory method to create a new Game starting at the given generation and with the given points.
     *
     * @param generation the starting generation.
     * @param points     a list of points in Grid coordinates.
     * @return a Game.
     */
    public static Game create(long generation, List<Point> points) {
        final Grid grid = new Grid(generation);
        grid.add(Group.create(generation, points));
        BiConsumer<Long, Group> groupMonitor = (l, g) -> System.out.println("generation " + l + ";\ncount=" + g.getCount());
        return new Game(generation, grid, null, groupMonitor);
    }

    /**
     * Method to run a Game given a monitor method for Grids.
     *
     * @param game           the game to run.
     * @param gridMonitor    the monitor
     * @param maxGenerations the maximum number of generations for this run.
     * @return the generation when expired.
     */
    public static Behavior run(Game game, BiConsumer<Long, Grid> gridMonitor, int maxGenerations) {
        if (game == null) throw new LifeException("run: game must not be null");
        Game g = game;
        while (!g.terminated()) g = g.generation(gridMonitor);
        int reason = g.generation >= maxGenerations ? 2 : g.getCount() <= 1 ? 0 : 1;
        return new Behavior(g.generation, g.growthRate(), reason);
    }

    /**
     * Class to model the behavior of a game of life.
     */
    public static class Behavior {
        /**
         * The generation at which the run stopped.
         */
        public final long generation;
        /**
         * The average rate of growth.
         */
        public final double growth;
        /**
         * The reason the run stopped:
         * 0: the cells went extinct
         * 1: a repeating sequence was noted;
         * 2: the maximum configured number of generations was reached.
         */
        private final int reason;

        public Behavior(long generation, double growth, int reason) {
            this.generation = generation;
            this.growth = growth;
            this.reason = reason;
        }

        public double evaluate() {
            switch (reason) {
                case 1:
                    return 0;
                case 2:
                    return growth;
                default:
                    return -1;
            }
        }

        @Override
        public String toString() {
            return "Behavior{" +
                    "generation=" + generation +
                    ", growth=" + growth +
                    ", reason=" + reason +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Behavior)) return false;
            Behavior behavior = (Behavior) o;
            return generation == behavior.generation &&
                    Double.compare(behavior.growth, growth) == 0 &&
                    reason == behavior.reason;
        }

        @Override
        public int hashCode() {
            return Objects.hash(generation, growth, reason);
        }
    }

    private Game(long generation, Grid grid, Game previous, BiConsumer<Long, Group> monitor) {
        this.grid = grid;
        this.generation = generation;
        this.previous = previous;
        this.monitor = monitor;
    }

    private boolean terminated() {
        return testTerminationPredicate(g -> g.generation >= MaxGenerations, "having exceeded " + MaxGenerations + " generations") ||
                testTerminationPredicate(g -> g.getCount() <= 1, "extinction") ||
                // TODO now we look for two consecutive equivalent games...
                testTerminationPredicate(Game::previousMatchingCycle, "having matching previous games");
    }

    /**
     * Check to see if there is a previous pair of matching games.
     * <p>
     * NOTE this method of checking for cycles is not guaranteed to work!
     * NOTE this method may be very inefficient.
     * <p>
     * TODO project teams may need to fix this method.
     *
     * @param game the game to check.
     * @return a Boolean.
     */
    private static boolean previousMatchingCycle(Game game) {
        MatchingGame matchingGame = findCycle(game);
        int cycles = matchingGame.k;
        Game history = matchingGame.match;
        if (history == null) return false;
        Game current = game;
        while (current != null && history != null && cycles > 0) {
            if (current.equals(history)) {
                current = current.previous;
                history = history.previous;
                cycles--;
            } else
                return false;
        }
        return true;
    }

    /**
     * Find a game which matches the given game.
     *
     * @param game the game to match.
     * @return a MatchingGame object: if the match field is null it means that we did not find a match;
     * the k field is the number of generations between game and the matching game.
     */
    private static MatchingGame findCycle(Game game) {
        int k = 1;
        Game candidate = game.previous;
        while (candidate != null && !game.equals(candidate)) {
            candidate = candidate.previous;
            k++;
        }
        return new MatchingGame(k, candidate);
    }

    private static class MatchingGame {
        private final int k;
        private final Game match;

        public MatchingGame(int k, Game match) {
            this.k = k;
            this.match = match;
        }
    }

    private boolean testTerminationPredicate(Predicate<Game> predicate, String message) {
        if (predicate.test(this)) {
            System.out.println("Terminating due to: " + message);
            return true;
        }
        return false;
    }

    private final Grid grid;
    private final Game previous;
    private final BiConsumer<Long, Group> monitor;
    private final long generation;
}
