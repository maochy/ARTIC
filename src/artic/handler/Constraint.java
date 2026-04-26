package artic.handler;

import java.util.*;

public class Constraint implements Cloneable {
	public int[] disjunction;

	public Constraint(final int[] clause) {
		System.arraycopy(clause, 0, this.disjunction = new int[clause.length], 0, clause.length);
	}

	public Constraint clone() {
		Constraint cs;
		try {
			cs = (Constraint) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
		cs.disjunction = this.disjunction.clone();
		return cs;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.disjunction);
	}

	public boolean isSuperior(final Constraint con) {
		if (con.disjunction.length < this.disjunction.length) {
			return false;
		}
		for (int i = 0; i < this.disjunction.length; ++i) {
			int j;
			for (j = 0; j < con.disjunction.length && con.disjunction[j] != this.disjunction[i]; ++j) {
			}
			
			if (j == con.disjunction.length && this.disjunction[i] != con.disjunction[j - 1]) {
				return false;
			}
		}
		return true;
	}
}
