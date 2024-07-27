package org.forfree.atlassian

import javassist.CannotCompileException
import javassist.ClassPool
import javassist.NotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

object Agent {
    val LOG: Logger = LoggerFactory.getLogger(Agent::class.java)
    const val TARGET_CLASS = "com.atlassian.extras.keymanager.KeyManager"

    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        LOG.info("Agent Loaded with Args: $args")
        try {
            inst.addTransformer(object : ClassFileTransformer {
                @Throws(IllegalClassFormatException::class)
                override fun transform(
                    loader: ClassLoader,
                    className: String,
                    classBeingRedefined: Class<*>,
                    protectionDomain: ProtectionDomain,
                    classfileBuffer: ByteArray
                ): ByteArray {
                    var byteCode = classfileBuffer
                    if (className != TARGET_CLASS.replace(".", "/")) {
                        return byteCode;
                    }
                    LOG.info("Transforming class $TARGET_CLASS")
                    try {
                        if (args.isNullOrBlank()) {
                            LOG.warn("Argument pubKey is empty")
                            return byteCode
                        }
                        val pubKey = File(args).readText()
                            .replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace(System.lineSeparator().toRegex(), "")
                            .replace("-----END PUBLIC KEY-----", "")
                        val cp = ClassPool.getDefault()
                        val cc = cp.get(TARGET_CLASS)
                        val m = cc.getDeclaredMethod("reset")
                        val statement = "this.loadKey(new com.atlassian.extras.keymanager.Key(\"${pubKey}\", \"${Constants.LICENSE_STRING_KEY_V2}\", com.atlassian.extras.keymanager.Key.Type.PUBLIC));\n"
                        m.insertAfter(statement)
                        byteCode = cc.toBytecode();
                        cc.detach()
                    } catch (ex: Exception) {
                        when (ex) {
                            is NotFoundException,
                            is CannotCompileException,
                            is IOException -> LOG.error(ex.message)
                            else -> throw ex
                        }
                    }
                    return byteCode
                }
            }, true)
            val target = Class.forName(TARGET_CLASS)
            inst.retransformClasses(target)
        } catch (e: Exception) {
            LOG.error("Agent run with error: ${e.message}")
        }
    }
}