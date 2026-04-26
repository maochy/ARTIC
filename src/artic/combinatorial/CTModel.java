package artic.combinatorial;

import artic.handler.*;
import artic.common.*;
import java.util.*;

public class CTModel
{
    public String name;
    public int parameter;
    public List<String> parameterName;
    /**
     * Number of values for each parameter.
     */
    public int[] value;
    public int t_way;
    public List<int[]> constraint;
    /**
     * Parameter-value relation table.
     */
    public int[][] relation;
    /**
     * All t-way parameter combinations.
     */
    public List<int[]> allPc;
    private ValidityChecker checker;
    public Set<Integer> constrainedParameters;
    public BArray combination;
    private long combRaw;
    private long combAll;
    private long combUncovered;
    /**
     * Number of t-way parameter combinations.
     */
    private int uniformRow;
    private int testCaseCoverMax;
    private long[][] levelCover;
    
    public CTModel(final int p, final int[] v, final int t) {
        this.parameter = p;
        System.arraycopy(v, 0, this.value = new int[p], 0, p);
        this.t_way = t;
        this.combination = null;
        this.uniformRow = ALG.combine(this.parameter, this.t_way);
        this.testCaseCoverMax = this.uniformRow;
        this.checker = new CH_Solver();
        this.constrainedParameters = new HashSet<Integer>();
        this.constraint = new ArrayList<int[]>();
    }
    
    public void setParameterName(final List<String> name) {
        (this.parameterName = new ArrayList<String>()).addAll(name);
    }
    
    public long getCombAll() {
        return this.combAll;
    }
    
    public long getCombUncovered() {
        return this.combUncovered;
    }
    
    public int getTestCaseCoverMax() {
        return this.testCaseCoverMax;
    }
    
    public void setChecker(final ValidityChecker checker) {
        this.checker = checker;
    }
    
    public void setConstraint(final List<int[]> constraint) {
        this.constraint = constraint;
        final HashSet<Integer> temp = new HashSet<Integer>();
        for (final int[] array : constraint) {
            for (final int v : array) {
                temp.add(Math.abs(v));
            }
        }
        this.relation = new int[this.parameter][];
        int start = 1;
        for (int i = 0; i < this.parameter; ++i) {
            this.relation[i] = new int[this.value[i]];
            for (int j = 0; j < this.value[i]; ++j, ++start) {
                this.relation[i][j] = start;
                if (temp.contains(start)) {
                    this.constrainedParameters.add(i);
                }
            }
        }
        this.checker.init(this);
    }
    
    public boolean isValid(final int[] test) {
        return this.checker.isValid(test);
    }
    
    public boolean isValid(final int[] position, final int[] schema) {
        return this.checker.isValid(position, schema);
    }
    
    public void removeInvalidCombinations() {
        for (final int[] pos : ALG.allCombination(this.parameter, this.t_way)) {
            for (final int[] sch : ALG.allV(pos, this.t_way, this.value)) {
                if (!this.isValid(pos, sch) && !this.covered(pos, sch, 1)) {
                    --this.combAll;
                }
            }
        }
    }
    
    public void initialization() {
        this.combination = null;
        final long combRaw = 0L;
        this.combUncovered = combRaw;
        this.combAll = combRaw;
        this.combRaw = combRaw;
        this.allPc = ALG.allCombination(this.parameter, this.t_way);
        this.combination = new BArray(this.uniformRow);
        int i = 0;
        for (final int[] pos : this.allPc) {
            final int cc = ALG.combineValue(pos, this.value);
            this.combination.initializeRow(i++, cc);
            this.combRaw += cc;
            this.combAll += cc;
            this.combUncovered += cc;
        }
        this.combination.initializeZeros();
    }
    
    public Tuple getAnUncoveredTuple() {
        if (this.combUncovered == 0L) {
            return null;
        }
        Position e = this.combination.getRandomZeroPosition();
        if (e == null) {
            return null;
        }
        int[] pos;
        int[] sch;
        for (pos = this.allPc.get(e.row), sch = ALG.num2val(e.column, pos, this.t_way, this.value); !this.isValid(pos, sch);
        		pos = this.allPc.get(e.row), sch = ALG.num2val(e.column, pos, this.t_way, this.value)) {
            this.covered(pos, sch, 1);
            if ((e = this.combination.getRandomZeroPosition()) == null) {
                return null;
            }
        }
        return new Tuple(pos, sch, this.parameter);
    }
    
    /**
     * 
     * @param test test case
     * @return number of newly covered combinations
     */
    public long fitnessValue(int[] test) {
        return (test == null) ? -1L : this.fitness(test, 0);
    }
    
    public long fitnessValue(final ArrayList<int[]> suite) {
        if (suite == null || suite.size() == 0) {
            return -1L;
        }
        long total_covered = 0L;
        for (final int[] pos : this.allPc) {
            final int len = ALG.combineValue(pos, this.value);
            final int[] cover = new int[len];
            int covered = 0;
            final int[] sch = new int[this.t_way];
            for (final int[] tc : suite) {
                for (int k = 0; k < this.t_way; ++k) {
                    sch[k] = tc[pos[k]];
                }
                final int index = ALG.val2num(pos, sch, this.t_way, this.value);
                if (cover[index] == 0) {
                    cover[index] = 1;
                    ++covered;
                }
            }
            total_covered += covered;
        }
        return this.combUncovered = this.combAll - total_covered;
    }
    
    public void updateCombination(final int[] test) {
        long fit = this.fitness(test, 1);
//        System.out.println(fit);
    }
    
    private long fitness(final int[] test, final int FLAG) {
        long num = 0L;
        int row = 0;
        for (final int[] position : this.allPc) {
            final int[] schema = new int[this.t_way];
            for (int k = 0; k < this.t_way; ++k) {
                schema[k] = test[position[k]];
            }
            if (!this.covered(row++, schema, FLAG)) {
                ++num;
            }
        }
        return num;
    }
    
    /**
     * @param position parameter combination
     * @param schema value combination
     * @param FLAG coverage flag, 1 means covered and 0 means query only
     * @return
     */
    public boolean covered(final int[] position, final int[] schema, final int FLAG) {
        final int row = ALG.combine2num(position, this.parameter, this.t_way);
        final int column = ALG.val2num(position, schema, this.t_way, this.value);
        final boolean cov = this.combination.getElement(row, column);
        if (!cov & FLAG == 1) {
            this.combination.setElement(row, column, true);
            --this.combUncovered;
        }
        return cov;
    }
    
    
    private boolean covered(final int row, final int[] schema, final int FLAG) {
    	int[] pc = allPc.get(row);
        final int column = ALG.val2num(pc, schema, this.t_way, this.value);
        try {
            final boolean cov = this.combination.getElement(row, column);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        final boolean cov = this.combination.getElement(row, column);
        if (!cov & FLAG == 1) {
            this.combination.setElement(row, column, true);
            if(levelCover != null) {
            	for (int i = 0; i < pc.length; i++) {
            		levelCover[pc[i]][schema[i]] --;
            	}
            }
            --this.combUncovered;
        }
        return cov;
    }
    
    public void initLevelCover() {
		levelCover = new long[parameter][];
		long sumVal = 0;
		for (int i = 0; i < levelCover.length; i++) {
			sumVal += value[i];
		}
		for (int i = 0; i < levelCover.length; i++) {
			levelCover[i] = new long[value[i]];
			long val = sumVal - value[i];
			for(int j = 0 ; j < levelCover[i].length; j++) {
				levelCover[i][j] = val;
			}
		}
	}
    
    public int[] getMaxPar() {
    	long maxCover = Long.MIN_VALUE;
    	ArrayList<int []> list = new ArrayList<>();
    	for (int i = 0; i < levelCover.length; i++) {
    		int level = getMaxLevel(i);
			if(levelCover[i][level] > maxCover) {
				list.clear();
				maxCover = levelCover[i][level];
			}
			if(maxCover ==  levelCover[i][level]) {
				list.add(new int[]{i,level});
			}
		}
    	
    	return list.get(new Random().nextInt(list.size()));
    }
    
    private int getMaxLevel(int par) {
    	long maxCover = Long.MIN_VALUE;
    	ArrayList<Integer> list = new ArrayList<>();
    	for (int i = 0; i < levelCover[par].length; i++) {
			if(levelCover[par][i] > maxCover) {
				list.clear();
				maxCover = levelCover[par][i];
			}
			if(maxCover ==  levelCover[par][i]) {
				list.add(i);
			}
		}
    	
    	return list.get(new Random().nextInt(list.size()));
    }
    
    public void show() {
        System.out.print("[Model] parameter = " + this.parameter);
        System.out.print(", value = " + Arrays.toString(this.value));
        System.out.print(", t-way = " + this.t_way);
        System.out.print(", # constraints = " + this.constraint.size() + "\n");
    }

    public void updateCombinationAndLevel(int[] test) {
        this.fitnessInAETG(test, 1);
    }

    private long fitnessInAETG(final int[] test, final int FLAG) {
        long num = 0L;
        int row = 0;
        for (final int[] position : this.allPc) {
            final int[] schema = new int[this.t_way];
            for (int k = 0; k < this.t_way; ++k) {
                schema[k] = test[position[k]];
            }
            if (!this.covered(row++, schema, FLAG)) {
                ++num;
                if(levelCover != null) {
                    for (int i = 0; i < t_way; i++) {
                        levelCover[position[i]][schema[i]]--;
                    }
                }
            }
        }
        return num;
    }
}
