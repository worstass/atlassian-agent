package org.forfree.atlassian;

import java.lang.instrument.Instrumentation;

public class Agent {
    public static void premain(String args, Instrumentation inst) {
        try {
            if (args == null) return;
            inst.addTransformer(new KeyTransformer(args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}