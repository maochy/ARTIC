package artic.common;

import java.io.*;
import java.util.*;
import java.util.function.*;
import artic.combinatorial.*;
import artic.common.CaseSIR.Fault;

public class CaseLARGE implements Case
{
	public List<String> faults;
	public double rate;
    public int parameter;
    public ArrayList<String> parameterName;
    public int[] value;
    public ArrayList<int[]> constraint;
    public int[] defaultTestCase;
    public int[] defaultPV;
    public int[][] relation;
    public Map<Integer, String> relationName;
    public ArrayList<Fault> allFault;
    public Map<String, Fault> mapFault;
    private boolean INFO;
    
    public void setINFO(final boolean info) {
        this.INFO = info;
    }
    
    public CaseLARGE(final String name) {
        this.INFO = false;
        if (!name.equals("linux") && !name.equals("busybox") && !name.equals("drupal")) {
            return;
        }
        try {
            this.readModel("subject/" + name + ".model");
            this.readBugReport("subject-bugs/" + name + ".bug");
            if (this.INFO) {
                this.showCompleteModel();
                this.checkSatisfied();
            }
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void showCompleteModel() {
        System.out.println("parameter = " + this.parameter + " constraint = " + this.constraint.size());
        for (int i = 0; i < this.parameterName.size(); ++i) {
            System.out.print(i + " " + this.parameterName.get(i) + " [");
            System.out.print(this.relation[i][0] + " " + this.relation[i][1] + "]\n");
        }
        final Set<String> unique = new HashSet<String>();
        for (final int[] cons : this.constraint) {
            System.out.println(Arrays.toString(cons));
            for (final int e : cons) {
                unique.add(this.parameterName.get(index(e)));
            }
        }
        System.out.println("# parameters involved in constraints = " + unique.size());
        unique.clear();
        System.out.println("\nbugs: ");
        for (final Fault f : this.allFault) {
            System.out.println(f.id + ": " + f.path + " <- " + f.combs);
            unique.addAll(f.involved);
        }
        System.out.println("# parameters involved in bugs = " + unique.size());
    }
    
    public static int index(final int pv) {
        return (int)Math.ceil(Math.abs(pv) / 2.0) - 1;
    }
    
    private void readModel(final String filename) throws IOException {
        final BufferedReader br = ProjectResource.openReader(filename);
        String[] parts = br.readLine().trim().split(" ");
        this.parameter = Integer.valueOf(parts[1]);
        this.value = new int[this.parameter];
        parts = br.readLine().trim().split(" ");
        for (int i = 0; i < this.parameter; ++i) {
            if (!parts[i + 1].equals("2")) {
                System.err.println("incorrect value " + parts[i + 1]);
                return;
            }
            this.value[i] = 2;
        }
        this.parameterName = new ArrayList<String>();
        this.relation = new int[this.parameter][2];
        this.relationName = new HashMap<Integer, String>();
        int start = 1;
        for (int j = 0; j < this.parameter; ++j) {
            parts = br.readLine().trim().split(" ");
            this.parameterName.add(parts[1]);
            this.value[j] = 2;
            this.relation[j][0] = start++;
            this.relation[j][1] = start++;
            this.relationName.put(this.relation[j][0], "!" + parts[1]);
            this.relationName.put(this.relation[j][1], parts[1]);
        }
        br.readLine();
        br.readLine();
        parts = br.readLine().trim().split(" ");
        this.defaultTestCase = new int[this.parameter];
        this.defaultPV = new int[this.parameter];
        for (int k = 0; k < this.parameter; ++k) {
            this.defaultTestCase[k] = Integer.valueOf(parts[k]);
            this.defaultPV[k] = this.relation[k][this.defaultTestCase[k]];
        }
        this.constraint = new ArrayList<int[]>();
        br.readLine();
        final int consNum = Integer.valueOf(br.readLine().trim().split(" ")[1]);
        String line;
        while ((line = br.readLine()) != null) {
            parts = line.trim().split(" ");
            final int[] cons = new int[parts.length];
            for (int l = 0; l < parts.length; ++l) {
                cons[l] = Integer.valueOf(parts[l]);
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
        final BufferedReader reader = ProjectResource.openReader(filename);
        int bug_index = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            final String[] parts = line.split(" : ");
            final String bug_path = parts[0];
            final List<Set<String>> bug_combs = new ArrayList<Set<String>>();
            final String presenceCondition = parts[1].replaceAll("\\s", "");
            final String[] split;
            final String[] options = split = presenceCondition.split("\\)\\|\\|\\(");
            for (final String option : split) {
                final String[] macros = option.split("&&");
                final Set<String> each = new HashSet<String>();
                for (String e : macros) {
                    e = e.replaceAll("\\(", "");
                    e = e.replaceAll("\\)", "");
                    e = e.replaceAll("CONFIG_", "");
                    each.add(e);
                }
                bug_combs.add(each);
            }
            final Fault fault = new Fault(bug_index, bug_path, bug_combs);
            this.allFault.add(fault);
            this.mapFault.put(fault.getIdentifier(), fault);
            ++bug_index;
        }
    }
    
    public boolean hitting(final int[] test, final Fault fault) {
        final ArrayList<String> tc = this.getStringTest(this.amend(test));
        for (final Set<String> each : fault.combs) {
            final int cover = (int)tc.stream().filter(each::contains).count();
            if (cover == each.size()) {
                return true;
            }
        }
        return false;
    }
    
    private ArrayList<String> getStringTest(final int[] test) {
        assert test.length == this.parameter;
        final ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < this.parameter; ++i) {
//            out.add((test[i] == 1) ? this.parameterName.get(i) : ("!" +( this.parameterName.get(i))));
            out.add((test[i] == 1) ? ("!" +this.parameterName.get(i)) : ( this.parameterName.get(i)));
        }
        return out;
    }
    
    public int[] amend(final int[] t) {
        if (t.length == this.parameter) {
            return t.clone();
        }
        final int[] tc = new int[this.parameter];
        final int len = t.length;
        System.arraycopy(t, 0, tc, 0, len);
        System.arraycopy(this.defaultTestCase, len, tc, len, this.parameter - len);
        return tc;
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
        sut.setParameterName(this.parameterName);
        sut.setConstraint(this.constraint);
        return sut;
    }
    
    @Override
    public CTModel getSubModel(final int para, final double cons, final int tway) {
        final int[] val = new int[para];
        System.arraycopy(this.value, 0, val, 0, val.length);
        final List<String> subName = new ArrayList<String>();
        for (int k = 0; k < para; ++k) {
            subName.add(this.parameterName.get(k));
        }
        final CTModel sut = new CTModel(para, val, tway);
        sut.setParameterName(subName);
        final List<int[]> cs = new ArrayList<int[]>();
        final int lastPV = this.relation[para - 1][this.value[para - 1] - 1];
        final Set<Integer> fixed = new HashSet<Integer>();
        for (int i = para; i < this.parameter; ++i) {
            fixed.add(this.defaultPV[i]);
        }
        for (final int[] cp : this.constraint) {
            final List<Integer> in = new ArrayList<Integer>();
            final List<Integer> out = new ArrayList<Integer>();
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
                    if (fixed.contains(Math.abs(e))) {
                        if (e <= 0) {
                            continue;
                        }
                        outV = true;
                    }
                    else {
                        if (e >= 0) {
                            continue;
                        }
                        outV = true;
                    }
                }
                if (outV) {
                    continue;
                }
                final int[] tcp = new int[in.size()];
                for (int j = 0; j < in.size(); ++j) {
                    tcp[j] = in.get(j);
                }
                if (ALG.inList(cs, tcp)) {
                    continue;
                }
                cs.add(tcp);
            }
        }
        if (cons != -1.0) {
            for (int prop = (int)Math.round(cs.size() * cons), l = cs.size(); l > prop; --l) {
                cs.remove(l - 1);
            }
        }
        sut.setConstraint(cs);
        return sut;
    }
    
    @Override
    public List<String> getFaultList() {
        final ArrayList<String> all = new ArrayList<String>();
        this.allFault.forEach(x -> all.add(x.getIdentifier()));
        return all;
    }
    
    public Set<Integer> getScenarioFaultDetected(final ArrayList<TestCase> tests,List<String> faults) {
        final Set<Integer> detected = new HashSet<Integer>();
        for (final TestCase tc : tests) {
            final int[] at = this.amend(tc.test);
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
    public boolean detected(TestCase tc, CTModel model) {
    	final int[] at = this.amend(tc.test);
        for (final String faultStr : faults) {
        	Fault fault = mapFault.get(faultStr);
            if (this.hitting(at, fault)) {
            	return true;
            }
        }
    	return false;
    }
    
    public Set<Integer> computeFaultDetected(final List<TestCase> suite) {
        final CTModel ground = this.getCompleteModel(2);
        final Set<Integer> detected = new HashSet<Integer>();
        int row = 0;
        for (final TestCase tc : suite) {
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
                        System.out.print(fault.getIdentifier() + " ");
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
    
    @Override
    public int[] computeFaultMatrix(final ArrayList<TestCase> test) {
        final int[] fm = new int[this.allFault.size()];
        final Set<Integer> detected = this.computeFaultDetected(test);
        for (int i = 0; i < fm.length; ++i) {
            final int fid = this.allFault.get(i).id;
            if (detected.contains(fid)) {
                fm[i] = 1;
            }
        }
        return fm;
    }
    
    @Override
    public int[] computeScenarioFaultMatrix(final ArrayList<TestCase> test) {
        final int[] fm = new int[faults.size()];
        final Set<Integer> detected = this.getScenarioFaultDetected(test,faults);
        for (int i = 0; i < fm.length; ++i) {
            final int f = this.mapFault.get(faults.get(i)).id;
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

    private void checkSatisfied() {
        final CTModel sut = this.getCompleteModel(2);
        final ArrayList<Integer> invalid = new ArrayList<Integer>();
        for (final Fault f : this.allFault) {
            for (final Set<String> x : f.combs) {
                final int[] test = new int[this.parameter];
                for (int k = 0; k < this.parameter; ++k) {
                    test[k] = -1;
                }
                for (final String e : x) {
                    final int idx = e.startsWith("!") ? this.parameterName.indexOf(e.substring(1)) : this.parameterName.indexOf(e);
                    test[idx] = (e.startsWith("!") ? 0 : 1);
                }
                if (!sut.isValid(test)) {
                    invalid.add(f.id);
                    System.out.println(">>> invalid fault: " + f.id);
                    System.out.println("    combination: " + x);
                    System.out.println("    tuple: " + Arrays.toString(test));
                }
            }
        }
        System.out.println();
        if (invalid.size() == 0) {
            System.out.println("all faults are satisfied");
        }
        System.out.println("default test case validity: " + sut.isValid(this.defaultTestCase));
        final ArrayList<TestCase> temp = new ArrayList<TestCase>();
        temp.add(new TestCase(this.defaultTestCase));
        final Set<Integer> dec = this.computeFaultDetected(temp);
        System.out.println("default test case hits: " + dec);
    }
    
    public class Fault
    {
        public int id;
        public String path;
        public List<Set<String>> combs;
        public Set<String> involved;
        
        public Fault(final int id, final String path, final List<Set<String>> combs) {
            this.id = id;
            this.path = path;
            this.combs = combs;
            this.involved = new HashSet<String>();
            combs.forEach(x -> x.forEach(y -> this.involved.add(y.startsWith("!") ? y.substring(1) : y)));
        }
        
        public String getIdentifier() {
            return String.format("f.%d", this.id);
        }
        
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("f." + this.id + " ");
            for (final Set<String> x : this.combs) {
                sb.append(x).append(" ");
            }
            return sb.toString();
        }
    }

	@Override
	public void setFaultsList(List<String> faults,double rate) {
		this.faults = faults;
		this.rate = rate;
	}

	@Override
	public double getRate() {
		return rate;
	}
}
