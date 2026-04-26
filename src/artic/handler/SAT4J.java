package artic.handler;

import org.sat4j.minisat.*;
import org.sat4j.core.*;
import java.util.*;
import org.sat4j.specs.*;

public class SAT4J implements Cloneable
{
    private int MAXVAR;
    private int NBCLAUSES;
    private ISolver solver;
    
    public SAT4J(final int MAXVAR, final int NBCLAUSES) {
        this.MAXVAR = MAXVAR;
        this.NBCLAUSES = NBCLAUSES;
        (this.solver = SolverFactory.newDefault()).newVar(MAXVAR);
        this.solver.setExpectedNumberOfClauses(NBCLAUSES);
    }
    
    public void addClauses(final Vector<Constraint> constraint) throws ContradictionException {
        for (final Constraint clause : constraint) {
            this.solver.addClause(new VecInt(clause.disjunction));
        }
    }
    
    public boolean isSatisfiable(final int[] clause) throws TimeoutException {
        final VecInt c = new VecInt(clause);
        final IProblem problem = this.solver;
        return problem.isSatisfiable(c);
    }
}
