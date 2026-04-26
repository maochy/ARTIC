package artic.handler;

import artic.combinatorial.*;
import artic.common.*;
import java.util.*;
import org.sat4j.specs.*;

public class CH_Solver implements ValidityChecker
{
    private int[][] relation;
    /**
     * Basic physical constraints: one parameter cannot take multiple values.
     */
    private Vector<Constraint> basicConstraint;
    /**
     * Hard logical constraints.
     */
    private Vector<Constraint> hardConstraint;
    private SAT4J solver;
    
    public CH_Solver() {
        this.basicConstraint = new Vector<Constraint>();
        this.hardConstraint = new Vector<Constraint>();
    }
    
    @Override
    public void init(final CTModel model) {
        this.relation = model.relation;
        for (int i = 0; i < model.parameter; ++i) {
            this.basicConstraint.add(new Constraint(this.relation[i]));
        }
        for (int i = 0; i < model.parameter; ++i) {
            for (final int[] row : ALG.allCombination(model.value[i], 2)) {
                final int[] tp = { 0 - this.relation[i][row[0]], 0 - this.relation[i][row[1]] };
                this.basicConstraint.add(new Constraint(tp));
            }
        }
        
        for (final int[] x : model.constraint) {
            this.hardConstraint.add(new Constraint(x));
        }
        final int SS = this.basicConstraint.size() + this.hardConstraint.size();
        
        final int MM = this.relation[model.parameter - 1][model.value[model.parameter - 1] - 1];
        
        this.solver = new SAT4J(MM, SS);
        try {
            this.solver.addClauses(this.basicConstraint);
            this.solver.addClauses(this.hardConstraint);
        }
        catch (ContradictionException e) {
            System.err.println("CH_Solver Contradiction Error: " + e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see artic.handler.ValidityChecker#isValid(int[])
     */
    @Override
    public boolean isValid(final int[] test) {
        if (this.hardConstraint.size() == 0) {
            return true;
        }
        final ArrayList<Integer> list = new ArrayList<Integer>();
        for (int j = 0; j < test.length; ++j) {
            if (test[j] != -1) {
                list.add(this.relation[j][test[j]]);
            }
        }
        final int[] clause = list.stream().mapToInt(i -> i).toArray();
        boolean satisfiable = false;
        try {
            satisfiable = this.solver.isSatisfiable(clause);
        }
        catch (TimeoutException e) {
            System.err.println("CH_Solver Timeout Error: " + e.getMessage());
        }
        return satisfiable;
    }
    
    /* (non-Javadoc)
     * @see artic.handler.ValidityChecker#isValid(int[], int[])
     */
    @Override
    public boolean isValid(final int[] position, final int[] schema) {
        if (this.hardConstraint.size() == 0) {
            return true;
        }
        final ArrayList<Integer> list = new ArrayList<Integer>();
        for (int j = 0; j < position.length; ++j) {
            list.add(this.relation[position[j]][schema[j]]);
        }
        final int[] clause = list.stream().mapToInt(i -> i).toArray();
        boolean satisfiable = false;
        try {
            satisfiable = this.solver.isSatisfiable(clause);
        }
        catch (TimeoutException e) {
            System.err.println("CH_Solver Timeout Error: " + e.getMessage());
        }
        return satisfiable;
    }
}
