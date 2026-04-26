package artic.common;

import java.io.*;
import java.util.*;
import artic.combinatorial.*;

public class CaseSIR implements Case
{
    /**
     * Number of parameters.
     */
    public int parameter;
    /**
     * Number of values for each parameter.
     */
    public int[] value;
    public ArrayList<int[]> constraint;
    
    /**
     * Default test case.
     */
    public int[] defaultTestCase;
   
    /**
     * Relation indexes of the default parameter values.
     */
    public int[] defaultPV;
   
    /**
     * Parameter-value relation table.
     */
    public int[][] relation;
    
    public List<String> faults;
    public double rate;
    public ArrayList<Fault> allFault;
    public Map<String, Fault> mapFault;
    private boolean INFO;

    public CaseSIR() {
    }

    public void setINFO(final boolean info) {
        this.INFO = info;
    }
    
    public CaseSIR(String name) {
        this.INFO = false;
        try {
            this.readModel("subject/" + name + ".model");
            this.readBugReport("subject-bugs/" + name + ".bug");
            if (this.INFO) {
                this.showCompleteModel();
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void showCompleteModel() {
        System.out.println("parameter = " + this.parameter + " constraint = " + this.constraint.size());
        final Set<Integer> unique = new HashSet<Integer>();
        for (final int[] cons : this.constraint) {
            System.out.println(Arrays.toString(cons));
            for (final int e : cons) {
                unique.add(this.index(e));
            }
        }
        System.out.println("# parameters involved in constraints = " + unique.size());
        System.out.println("\n# faults = " + this.allFault.size());
        for (final Fault fault : this.allFault) {
            System.out.println(fault);
        }
    }
    
    private int index(final int pv) {
        for (int i = 0; i < this.parameter; ++i) {
            for (int j = 0; j < this.value[i]; ++j) {
                if (this.relation[i][j] == Math.abs(pv)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private void readModel(String filename) throws IOException {
        final BufferedReader br = ProjectResource.openReader(filename);
        String[] parts = br.readLine().trim().split(" ");
        this.parameter = Integer.valueOf(parts[1]);
        this.value = new int[this.parameter];
        parts = br.readLine().trim().split(" ");
        for (int i = 0; i < this.parameter; ++i) {
            this.value[i] = Integer.valueOf(parts[i + 1]);
        }
        this.relation = new int[this.parameter][];
        int start = 1;
        for (int j = 0; j < this.parameter; ++j) {
            this.relation[j] = new int[this.value[j]];
            for (int k = 0; k < this.value[j]; ++k) {
                this.relation[j][k] = start++;
            }
        }
        br.readLine();
        br.readLine();
        
        parts = br.readLine().trim().split(" ");
        this.defaultTestCase = new int[this.parameter];
        this.defaultPV = new int[this.parameter];
        for (int l = 0; l < this.parameter; ++l) {
            this.defaultTestCase[l] = Integer.valueOf(parts[l]);
            this.defaultPV[l] = this.relation[l][this.defaultTestCase[l]];
        }
        this.constraint = new ArrayList<int[]>();
        br.readLine();
        final int consNum = Integer.valueOf(br.readLine().trim().split(" ")[1]);
        String line;
        while ((line = br.readLine()) != null) {
            parts = line.trim().split(" ");
            final int[] cons = new int[parts.length];
            for (int m = 0; m < parts.length; ++m) {
                cons[m] = Integer.valueOf(parts[m]);
            }
            this.constraint.add(cons);
        }
        if (this.constraint.size() != consNum) {
            System.err.println("incorrect constraint size");
        }
    }
    
    private void readBugReport(final String filename) throws IOException {
        this.allFault = new ArrayList<Fault>();
        this.mapFault = new HashMap<String, Fault>();
        final BufferedReader br = ProjectResource.openReader(filename);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("f")) {
                final String id = line.trim();
                final List<int[]> set = new ArrayList<int[]>();
                while (!(line = br.readLine()).equals("")) {
                    final String[] parts = line.split(" ");
                    if (parts.length != this.parameter) {
                        System.out.println("incorrect faulty test case");
                        return;
                    }
                    final int[] tc = new int[this.parameter];
                    for (int x = 0; x < this.parameter; ++x) {
                        tc[x] = Integer.valueOf(parts[x]);
                    }
                    set.add(tc);
                }
                final Fault fault = new Fault(id, set);
                this.allFault.add(fault);
                this.mapFault.put(fault.id, fault);
            }
        }
    }
    
    @Override
    public int getCompleteParameter() {
        return this.parameter;
    }
    
    @Override
    public ArrayList<int[]> getCompleteConstraint() {
        return this.constraint;
    }
    
    @Override
    public CTModel getCompleteModel(final int tway) {
        final CTModel sut = new CTModel(this.parameter, this.value, tway);
        sut.setConstraint(this.constraint);
        return sut;
    }
    
    @Override
    public CTModel getSubModel(final int para, final double cons, final int tway) {
        final int[] val = new int[para];
        System.arraycopy(this.value, 0, val, 0, val.length);
        final CTModel sut = new CTModel(para, val, tway);
        final ArrayList<int[]> cs = new ArrayList<int[]>();
        final int lastPV = this.relation[para - 1][this.value[para - 1] - 1];
        final HashSet<Integer> fixed = new HashSet<Integer>();
        for (int i = para; i < this.parameter; ++i) {
            fixed.add(this.defaultPV[i]);
        }
        for (final int[] cp : this.constraint) {
            final ArrayList<Integer> in = new ArrayList<Integer>();
            final ArrayList<Integer> out = new ArrayList<Integer>();
            for (final int each : cp) {
                if (Math.abs(each) <= lastPV) {
                    in.add(each);
                }
                else {
                    out.add(each);
                }
            }
            
            if (out.size() == 0) {
                if (ALG.inList(cs, cp)) {
                    continue;
                }
                cs.add(cp.clone());
            }
            else {
                if (in.size() == 0) {
                    continue;
                }
                boolean outV = false;
                for (final int e : out) {
                    if (fixed.contains(Math.abs(e))) {		// The default value of the external parameter is true.
                        if (e <= 0) {		// The literal is false under the default value.
                            continue;
                        }
                        outV = true;	
                    }
                    else { // The default value is false.
                        if (e >= 0) {	// The literal is false under the default value.
                            continue;
                        }
                        outV = true; 
                    }
                }
                if (outV) { // The constraint is always true.
                    continue;
                }
                
                // Remove external parameters that do not affect the submodel.
                final int[] tcp = new int[in.size()];
                for (int k = 0; k < in.size(); ++k) {
                    tcp[k] = in.get(k);
                }
                if (ALG.inList(cs, tcp)) {
                    continue;
                }
                cs.add(tcp);
            }
        }
        
        if (cons != -1.0) {
            for (int prop = (int)Math.round(cs.size() * cons), j = cs.size(); j > prop; --j) {
                cs.remove(j - 1);
            }
        }
        sut.setConstraint(cs);
        return sut;
    }
    
    @Override
    public List<String> getFaultList() {
        final ArrayList<String> all = new ArrayList<String>();
        this.allFault.forEach(x -> all.add(x.id));
        return all;
    }
    
    private int[] amend(final int[] t) {
        if (t.length == this.parameter) {
            return t.clone();
        }
        final int[] tc = new int[this.parameter];
        final int len = t.length;
        System.arraycopy(t, 0, tc, 0, len);
        System.arraycopy(this.defaultTestCase, len, tc, len, this.parameter - len);
        return tc;
    }
    
    private boolean hitting(final int[] test, final Fault fault) {
        final int[] tc = this.amend(test);
        for (final int[] fs : fault.set) {
            if (Arrays.equals(fs, tc)) {
                return true;
            }
        }
        return false;
    }
    
    public Set<String> getFaultDetected(final ArrayList<TestCase> tests) {
        final CTModel ground = this.getCompleteModel(2);
        final Set<String> detected = new HashSet<String>();
        int row = 0;
        for (final TestCase tc : tests) {
            final int[] at = this.amend(tc.test);
            if (this.INFO) {
                System.out.print(row++ + ": " + Arrays.toString(tc.test) + " -> ");
            }
            if (!ground.isValid(at)) {
                if (!this.INFO) {
                    continue;
                }
                System.out.print("invalid\n");
            }
            else {
                for (final Fault fault : this.allFault) {
                    if (this.hitting(at, fault)) {
                        detected.add(fault.id);
                        if (!this.INFO) {
                            continue;
                        }
                        System.out.print(fault.id + " ");
                    }
                }
                if (!this.INFO) {
                    continue;
                }
                System.out.print("\n");
            }
        }
        return detected;
    }
    
    public Set<String> getScenarioFaultDetected(final ArrayList<TestCase> tests,List<String> faults) {
        final Set<String> detected = new HashSet<String>();
        for (final TestCase tc : tests) {
            final int[] at = tc.test;
            for (final String faultStr : faults) {
                Fault fault = mapFault.get(faultStr);
                if (this.hitting(at, fault)) {
                    detected.add(fault.id);
                }
            }
        }
        return detected;
    }
    
    @Override
    public boolean detected(TestCase tc,CTModel ground ) {
		final int[] at = this.amend(tc.test);
        for (final String faultStr : faults) {
        	Fault fault = mapFault.get(faultStr);
            if (this.hitting(at, fault)) {
               return true;
            }
        }
        return false;
    }
    
    @Override
    public int[] computeFaultMatrix(final ArrayList<TestCase> test) {
        final int[] fm = new int[this.allFault.size()];
        final Set<String> detected = this.getFaultDetected(test);
        for (int i = 0; i < fm.length; ++i) {
            final String f = this.allFault.get(i).id;
            if (detected.contains(f)) {
                fm[i] = 1;
            }
        }
        return fm;
    }
    
    @Override
    public int[] computeScenarioFaultMatrix(final ArrayList<TestCase> test) {
        final int[] fm = new int[faults.size()];
        final Set<String> detected = this.getScenarioFaultDetected(test,faults);
        for (int i = 0; i < fm.length; ++i) {
            final String f = this.mapFault.get(faults.get(i)).id;
            if (detected.contains(f)) {
                fm[i] = 1;
            }
        }
        return fm;
    }

    @Override
    public int computeEM(ArrayList<TestCase> test) {
        int cnt = 0;
        for (final TestCase tc : test) {
            final int[] at = this.amend(tc.test);
            boolean found = false;
            for (final String faultStr : faults) {
                Fault fault = mapFault.get(faultStr);
                if (this.hitting(at, fault)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                cnt ++;
            }
        }
        return cnt;
    }

    @Override
    public double getRate() {
    	return rate;
    }
    
	@Override
	public void setFaultsList(List<String> faults,double rate) {
		this.faults = faults;
		this.rate = rate;
	}
    
    public class Fault
    {
        public String id;
        public List<int[]> set;
        
        public Fault(final String id, final List<int[]> set) {
            this.id = id;
            this.set = set;
        }
        
        @Override
        public String toString() {
            return this.id + "\t -> # " + this.set.size();
        }
    }
}
