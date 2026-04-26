package artic.common;

import artic.combinatorial.CTModel;

import java.util.Random;

/**
 * Random sampling utility.
 */
public class RandomSample {
    public static int[] sample0(final CTModel model, final Random random) {
        final int[] test = new int[model.parameter];
        for (int j = 0; j < model.parameter; ++j) {
            test[j] = random.nextInt(model.value[j]);
        }
        while (!model.isValid(test)) {
            for (int j = 0; j < model.parameter; ++j) {
                test[j] = random.nextInt(model.value[j]);
            }
        }
        return test;
    }

    public static int[] sample(final CTModel model, final Random random) {
        final int[] test = new int[model.parameter];
        for (int j = 0; j < model.parameter; ++j) {
            test[j] = -1;
        }
        for (int j = 0; j < model.parameter; ++j) {
            test[j] = random.nextInt(model.value[j]);
        }
        return test;
    }
}
