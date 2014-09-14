import com.google.common.collect.Lists;
import org.junit.Test;
import java.io.*;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SudokuTest extends Sudoku {

    Sudoku sudoku = new Sudoku();

    @Test
    public void testisNotSolved() {
        assertEquals(false, sudoku.isSolved.apply(sudoku.to3DArray.apply(readCSV("final_unsolved.csv"))));
    }

    @Test
    public void testisSolved() {
        assertEquals(true, sudoku.isSolved.apply(sudoku.to3DArray.apply(readCSV("final_solved.csv"))));
    }

    @Test
    public void testRowPossibilities() {
        Integer[][][] unsolved = sudoku.to3DArray.apply(readCSV("final_unsolved.csv"));
        Integer[] result = sudoku.mergeDistinctPossibilities.apply(sudoku.rowPossibilities.apply(Lists.newArrayList(0, 0, unsolved)));

        assertEquals(3, Arrays.stream(result).count());
        assertEquals(new Integer(4), result[0]);
        assertEquals(new Integer(3), result[1]);
        assertEquals(new Integer(2), result[2]);
    }

    @Test
    public void testColumnPossibilities() {
        Integer[][][] unsolved = sudoku.to3DArray.apply(readCSV("final_unsolved.csv"));
        Integer[] result = sudoku.mergeDistinctPossibilities.apply(sudoku.columnPossibilities.apply(Lists.newArrayList(0, 0, unsolved)));

        assertEquals(3, Arrays.stream(result).count());
        assertEquals(new Integer(2), result[0]);
        assertEquals(new Integer(3), result[1]);
        assertEquals(new Integer(1), result[2]);
    }

    @Test
    public void testBoxPossibilities() {
        Integer[][][] unsolved = sudoku.to3DArray.apply(readCSV("final_unsolved.csv"));
        Integer[] result = sudoku.mergeDistinctPossibilities.apply(sudoku.boxPossibilities.apply(Lists.newArrayList(0, 0, unsolved)));

        assertEquals(3, Arrays.stream(result).count());
        assertEquals(new Integer(4), result[0]);
        assertEquals(new Integer(3), result[1]);
        assertEquals(new Integer(7), result[2]);
    }

    @Test
    public void testSolve() {
        assertTrue(sudoku.isSolved.apply(sudoku.solve(readCSV("final_unsolved.csv"))));
    }

    public int[][] readCSV(String filename) {
        try {
            int[][] sudoku = new int[9][9];
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
            int[] idx = {0};
            reader.lines()
                    .forEach(line -> {
                        int[] current = new int[9];
                        int[] innerIdx = {0};
                        Arrays.stream(line.split(","))
                                .forEach(num -> {
                                    String item = num.replaceAll("\"", "");
                                    if (item != null && !"".equals(item)) {
                                        current[innerIdx[0]] = Integer.parseInt(item);
                                    }
                                    innerIdx[0]++;
                                });
                        sudoku[idx[0]] = current;
                        idx[0]++;
                    });
            return sudoku;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
