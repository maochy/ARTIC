package artic.common;

import artic.combinatorial.CTModel;
import artic.combinatorial.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GeneralCase extends CaseSIR {
    public GeneralCase(String name) {
        super(name);
    }

    public GeneralCase(int parameter, int[] value) {
        this.parameter = parameter;
        this.value = value;
        this.relation = new int[this.parameter][];
        int start = 1;
        for (int j = 0; j < this.parameter; ++j) {
            this.relation[j] = new int[this.value[j]];
            for (int k = 0; k < this.value[j]; ++k) {
                this.relation[j][k] = start++;
            }
        }
        this.defaultTestCase = new int[this.parameter];
        this.defaultPV = new int[this.parameter];
        for (int l = 0; l < this.parameter; ++l) {
            this.defaultTestCase[l] = 0;
            this.defaultPV[l] = this.relation[l][this.defaultTestCase[l]];
        }
    }

    private boolean hitting(final int[] tc, final Fault fault) {
        for (final int[] fs : fault.set) {
            if (Arrays.equals(fs, tc)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CTModel getSubModel(int para, double cons, int tway) {
        final int[] val = new int[para];
        System.arraycopy(this.value, 0, val, 0, val.length);
        return new CTModel(para, val, tway);
    }

    public Set<String> getFaultDetected(TestCase tc) {
        final Set<String> detected = new HashSet<String>();
        int[] at = tc.test;
        for (final Fault fault : this.allFault) {
            if (this.hitting(at, fault)) {
                detected.add(fault.id);
            }
        }
        return detected;
    }
}
