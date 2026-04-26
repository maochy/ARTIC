package artic.common;

import java.util.*;

public class BArray
{
    /**
     * Coverage matrix for t-way parameter combinations.
     */
    private boolean[][] matrix;
    private Random random;
    private int[] zero_row;
    private int[] zero_column;
    private int zero_total;
    
    public BArray(final int row) {
        this.random = new Random();
        this.matrix = new boolean[row][];
        this.zero_total = 0;
        for (int i = 0; i < row; ++i) {
            this.matrix[i] = null;
        }
    }
    
    public BArray(final int row, final int column) {
        this.random = new Random();
        this.matrix = new boolean[row][column];
        this.zero_total = row * column;
        this.zero_row = new int[this.zero_total];
        this.zero_column = new int[this.zero_total];
        int index = 0;
        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < column; ++j) {
                this.matrix[i][j] = false;
                this.zero_row[index] = i;
                this.zero_column[index] = j;
                ++index;
            }
        }
    }
    
    /**
     * @param index row index for a t-way parameter combination
     * @param column number of value combinations in the row
     */
    public void initializeRow(final int index, final int column) {
        if (this.matrix[index] == null) {
            this.matrix[index] = new boolean[column];
            for (int j = 0; j < column; ++j) {
                this.matrix[index][j] = false;
            }
            this.zero_total += column;
        }
    }
    
    public void initializeZeros() {
        this.zero_row = new int[this.zero_total];
        this.zero_column = new int[this.zero_total];
        int index = 0;
        for (int i = 0; i < this.matrix.length; ++i) {
            for (int j = 0; j < this.matrix[i].length; ++j) {
                this.zero_row[index] = i;
                this.zero_column[index] = j;
                ++index;
            }
        }
    }
    
    public boolean getElement(final int i, final int j) {
        return this.matrix[i][j];
    }
    
    public void setElement(final int i, final int j, final boolean value) {
        this.matrix[i][j] = value;
    }
    
    /**
     * @return a random uncovered t-way combination position
     */
    public Position getRandomZeroPosition() {
        while (this.zero_total > 0) {
            final int index = this.random.nextInt(this.zero_total);
            final int row = this.zero_row[index];
            final int column = this.zero_column[index];
            if (!this.matrix[row][column]) {
                return new Position(row, column);
            }
            final int right = this.zero_total - 1;
            this.zero_row[index] = this.zero_row[right];
            this.zero_column[index] = this.zero_column[right];
            --this.zero_total;
        }
        return null;
    }
}
