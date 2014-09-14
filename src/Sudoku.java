import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Sudoku {

    public Sudoku() {
    }

    public Integer[][][] solve(int[][] sudoku) {
        Integer[][][] unsolved = to3DArray.apply(sudoku);
        ImmutableList<Function<List, Integer[][]>> allRegions = ImmutableList.of(rowPossibilities, columnPossibilities, boxPossibilities);
        int k = 0;
        while (!isSolved.apply(unsolved) && k < 100) {
            k++;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    Integer[] possibilities = unsolved[i][j];
                    if (possibilities.length == 1) {
                        continue;
                    }

                    Integer[] filteredPossibilities = removePossibilities(i, j, unsolved, allRegions, mergeDistinctPossibilities, possibilities);

                    if (filteredPossibilities.length > 1) {
                        Optional<Integer[]> first = removeUnsolvedPossibilities(i, j, unsolved, allRegions, filteredPossibilities);
                        if (first.isPresent()) {
                            filteredPossibilities = first.get();
                        }
                    }
                    unsolved[i][j] = filteredPossibilities;
                    if (filteredPossibilities.length == 1) {
                        i = 0;
                        j = 0;
                    }
                }
            }
        }
        System.out.println("Final Result:");
        print.accept(unsolved);
        return unsolved;
    }

    /**
     * searches the first region, where a possibility of the current cell doesn't occur
     * @param x - x Coordinate of the Cell
     * @param y - y Coordinate of the Cell
     * @param matrix - whole Sudoku
     * @param regionFunctions - list of Region functions to apply
     * @param filteredPossibilities - possibilities to remove from
     * @return
     */
    protected Optional<Integer[]> removeUnsolvedPossibilities(final int x, final int y, Integer[][][] matrix, List<Function<List, Integer[][]>> regionFunctions, final Integer[] filteredPossibilities) {
        return regionFunctions
                .stream()
                .map(fun -> {
                    List<Function<List, Integer[][]>> funcs = new ArrayList<>();
                    funcs.add(fun);
                    return removePossibilities(x, y, matrix, funcs, mergePossibilities, filteredPossibilities);
                })
                .filter(val -> val.length == 1)
                .findFirst();
    }

    /**
     * removes Possibilities from the given Cell (x,y) (non-destructive)
     * @param x - x Coordinate of the Cell
     * @param y - y Coordinate of the Cell
     * @param matrix - whole Sudoku
     * @param regionFunctions - list of Region functions to apply
     * @param removalMode - function for the removal algorithm (solved/unsolved etc.)
     * @param possibilities - possibilities to remove from
     * @return
     */
    protected final Integer[] removePossibilities(int x, int y, Integer[][][] matrix, List<Function<List, Integer[][]>> regionFunctions, Function<Integer[][], Integer[]> removalMode, final Integer[] possibilities) {
        ImmutableList<Object> arguments = ImmutableList.of(x, y, matrix);
        Optional<Integer[]> regionPossibilities = regionFunctions
                .stream()
                .map(fun -> removalMode.apply(fun.apply(arguments)))
                .reduce((init, curr) -> Stream.concat(Arrays.stream(init), Arrays.stream(curr)).toArray(Integer[]::new));

        if (regionPossibilities.isPresent()) {
            List<Integer> resultList = Arrays.asList(regionPossibilities.get());
            return Arrays.stream((possibilities))
                    .filter(item -> !(resultList.contains(item)))
                    .toArray(Integer[]::new);
        }

        return new Integer[0];
    }

    /**
     * merges Possibility Arrays
     */
    protected final Function<Integer[][], Integer[]> mergePossibilities = (arr) -> {
        Optional<Integer[]> possibilities = Arrays.stream(arr)
                .reduce((init, curr) -> Stream.concat(Arrays.stream(init), Arrays.stream(curr))
                        .toArray(Integer[]::new));
        if (possibilities.isPresent()) {
            return Arrays.stream(possibilities.get()).distinct().toArray(Integer[]::new);
        }
        return new Integer[0];
    };

    /**
     * merge Possibility Arrays of solved Cells only
     */
    protected final Function<Integer[][], Integer[]> mergeDistinctPossibilities = (arr) -> mergePossibilities.apply((Arrays.stream(arr)
            .filter((value) -> value.length == 1)
            .toArray(Integer[][]::new)));

    /**
     * Functions for calculating the possibilities for different Regions (box, column, row)
     */
    protected final Function<List, Integer[][]> rowPossibilities = (list) -> {
        Integer[][][] matrix = (Integer[][][]) list.get(2);
        return IntStream.range(0, matrix.length)
                .filter(i -> i != (int) list.get(1))
                .mapToObj(i -> matrix[((int) list.get(0))][i])
                .toArray(Integer[][]::new);
    };

    protected final Function<List, Integer[][]> columnPossibilities = (list) -> {
        Integer[][][] matrix = (Integer[][][]) list.get(2);
        return IntStream.range(0, matrix.length)
                .filter(i -> i != (int) list.get(0))
                .mapToObj(i -> matrix[i][((int) list.get(1))])
                .toArray(Integer[][]::new);
    };

    protected final Function<List, Integer[][]> boxPossibilities = (list) -> {
        int x = (int) list.get(0);
        int y = (int) list.get(1);
        Integer[][][] matrix = (Integer[][][]) list.get(2);

        int boxy = y / 3;
        int boxx = x / 3;

        return IntStream.range(boxx * 3, boxx * 3 + 3)
                .mapToObj(i -> IntStream.range(boxy * 3, boxy * 3 + 3)
                        .filter(j -> j != y || i != x)
                        .mapToObj(j -> matrix[i][j]).toArray(Integer[][]::new)).flatMap(Arrays::stream).toArray(Integer[][]::new);
    };

    /**
     * Check if the given Sudoku is solved
     */
    protected final Function<Integer[][][], Boolean> isSolved = (array) -> Arrays.stream(array)
            .filter((row) -> Arrays.stream(row)
                    .filter((column) -> column.length > 1)
                    .count() == 0)
            .count() == 9;

    /**
     * Print the Sudoku field
     */
    protected final Consumer<Integer[][][]> print = (arr) -> Arrays.stream(arr).forEach(
            (row) -> {
                Arrays.stream(row).forEach(
                        (col) -> {
                            if (col.length == 1) {
                                System.out.print(col[0]);
                            } else {
                                Arrays.stream(col).forEach(System.out::print);
                            }
                            System.out.print("|");
                        }
                );
                System.out.print("\n");
            }
    );

    /**
     * Convert the sudoku from CSV to an Array of the Cell's possibilities
     */
    protected final Function<int[][], Integer[][][]> to3DArray = (arr) -> Arrays.stream(arr).map((row) -> Arrays.stream(row)
            .mapToObj(col -> {
                if (0 == col) {
                    return IntStream.range(1, 10).boxed().toArray(Integer[]::new);
                } else {
                    return IntStream.range(col, col + 1).boxed().toArray(Integer[]::new);
                }
            })
            .toArray(Integer[][]::new))
            .toArray(Integer[][][]::new);
}
