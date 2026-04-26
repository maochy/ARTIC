package artic.combinatorial;

import java.util.*;


public class Tuple
{
    public int[] tuple;
    public int[] position;
    public int[] schema;
    public int length;
    public double fit;
    
    public Tuple(final int[] t) {
        this.tuple = t.clone();
        this.length = 0;
        final ArrayList<Integer> pos = new ArrayList<Integer>();
        final ArrayList<Integer> sch = new ArrayList<Integer>();
        for (int k = 0; k < t.length; ++k) {
            if (t[k] != -1) {
                pos.add(k);
                sch.add(t[k]);
                ++this.length;
            }
        }
        this.position = pos.stream().mapToInt(i -> i).toArray();
        this.schema = sch.stream().mapToInt(i -> i).toArray();
    }
    
    /**
     * Builds a tuple from a parameter combination and value combination.
     * @param pos
     * @param sch
     * @param full_length
     */
    public Tuple(final int[] pos, final int[] sch, final int full_length) {
        this.position = pos.clone();
        this.schema = sch.clone();
        this.length = pos.length;
        this.tuple = new int[full_length];
        for (int i = 0; i < full_length; ++i) {
            this.tuple[i] = -1;
        }
        for (int i = 0; i < this.length; ++i) {
            this.tuple[this.position[i]] = this.schema[i];
        }
    }

    public Tuple(final int[] pos, final int[] sch) {
        this.position = pos;
        this.schema = sch;
        this.length = pos.length;
    }
    
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        for (final int i : this.tuple) {
            str.append((i == -1) ? "- " : (String.valueOf(i) + " "));
        }
        return str.toString();
    }
    
    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof Tuple)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final Tuple otherTuple = (Tuple)other;
        return Arrays.equals(this.tuple, otherTuple.tuple);
    }
}
