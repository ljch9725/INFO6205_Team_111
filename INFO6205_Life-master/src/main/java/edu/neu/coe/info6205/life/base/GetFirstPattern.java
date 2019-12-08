package edu.neu.coe.info6205.life.base;

import io.jenetics.EnumGene;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static edu.neu.coe.info6205.life.base.Game.run;

public class GetFirstPattern implements Problem<ISeq<Integer>, EnumGene<Integer>, Long> {

    private final ISeq<Integer> _points;

    public ISeq<Integer> get_points() {
        return _points;
    }

    public GetFirstPattern(ISeq<Integer> points) {
        _points = points;
    }

    @Override
    public Function<ISeq<Integer>, Long> fitness() {
        return p -> {
            String s = "";
            for (int i = 1; i < p.length(); i += 2) {
                int x = p.get(i - 1);
                s += x + " ";
                int y = p.get(i);
                s += y + ",";
            }
            final Game.Behavior generations = run(0L, s);
            return generations.generation;
        };
    }

    @Override
    public Codec<ISeq<Integer>, EnumGene<Integer>> codec() {
        return Codecs.ofPermutation(_points);
    }

    public static GetFirstPattern of(String firstPattern) {
        List<String> result = new ArrayList<>();
        for (String w : firstPattern.split(", *")) {
            result.add(w);
        }
        List<Integer> pointlist = new ArrayList<>();
        Random random = new Random();
        int length = result.size() / 2;
        int randomLength = (random.nextInt(length == 0 ? 1 : length) - 1) * 2;

        for (String s : result) {
            String[] ans = s.split(" ");
            Integer point1 = Integer.parseInt(ans[0]);
            Integer point2 = Integer.parseInt(ans[1]);

            int randomNumber = random.nextInt(100);
            if (randomNumber > 5) {
                pointlist.add(point1);
                pointlist.add(point2);
            } else if (randomNumber >= 1) {
                point1 += random.nextInt(Math.abs(point1) / 10 <= 0 ? 1 : Math.abs(point1) / 10);
                point2 += random.nextInt(Math.abs(point1) / 10 <= 0 ? 1 : Math.abs(point1) / 10);
                pointlist.add(point1);
                pointlist.add(point2);
            }
            if (random.nextInt(100) < 50 && randomLength > 0) {
                int absX = Math.abs(point1);
                int absY = Math.abs(point2);
                int maxRange = Math.max(absX, absY);
                for (int i = 0; i < 2; i++) {
                    Integer point = random.nextInt(maxRange == 0 ? 1 : maxRange * 4) - (maxRange * 2);
                    pointlist.add(point);
                    randomLength--;
                }
            }
        }

        Integer[] integers = pointlist.toArray(new Integer[pointlist.size()]);

        final MSeq<Integer> points = MSeq.ofLength(integers.length);
        for (int i = 0; i < integers.length; i++) {
            final int point = integers[i];
            points.set(i, point);
        }
        return new GetFirstPattern(points.toISeq());
    }
}
