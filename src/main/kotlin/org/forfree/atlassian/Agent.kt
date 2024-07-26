package org.forfree.atlassian

import java.lang.instrument.Instrumentation

object Agent {
    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        try {
            inst.addTransformer(KeyTransformer(args!!))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}