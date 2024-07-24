package org.forfree.atlassian

import javassist.*
import java.io.File
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.IllegalClassFormatException
import java.security.ProtectionDomain
import org.slf4j.LoggerFactory;
import java.io.IOException

class KeyTransformer(val keyFile: String) : ClassFileTransformer {
    companion object {
        private var LOG = LoggerFactory.getLogger(KeyTransformer::class.java)
        private val TARGET_CLASS = "com.atlassian.extras.keymanager.KeyManager"
    }

    @Throws(IllegalClassFormatException::class)
    override fun transform(
        loader: ClassLoader,
        className: String,
        classBeingRedefined: Class<*>,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray {
        var byteCode = classfileBuffer
//        String finalTargetClassName = this.targetClassName
//            .replaceAll("\\.", "/");
        if (!className.equals(TARGET_CLASS.replace("\\.", "/"))) {
            return byteCode;
        }
        LOG.info("[Agent] Transforming class ${TARGET_CLASS}")
        try {
            val pubKey = File(keyFile).readText()
            val cp = ClassPool.getDefault()
//                cp.importPackage("java.util.Arrays")
//                cp.importPackage("javax.xml.bind.DatatypeConverter")

//            val mod = Modifier.PRIVATE or Modifier.STATIC or Modifier.FINAL
            val cc = cp.get(TARGET_CLASS)
            val m = cc.getDeclaredMethod("reset")
//                val cb: CtClass = cp.get("byte[]")

            m.insertAfter("this.loadKey(new Key(\"${pubKey}\", \"LICENSE_STRING_KEY_V2\", Key.Type.PUBLIC));")
//
//                val cfOld: CtField = CtField(cb, "__h_ok", cc)
//                val cfNew: CtField = CtField(cb, "__h_nk", cc)
//                cfOld.setModifiers(mod)
//                cfNew.setModifiers(mod)
//                cc.addField(
//                    cfOld,
//                    "DatatypeConverter.parseBase64Binary(\"MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAIvfweZvmGo5otwawI3no7Udanxal3hX2haw962KL/nHQrnC4FG2PvUFf34OecSK1KtHDPQoSQ+DHrfdf6vKUJphw0Kn3gXm4LS8VK/LrY7on/wh2iUobS2XlhuIqEc5mLAUu9Hd+1qxsQkQ50d0lzKrnDqPsM0WA9htkdJJw2nS\");"
//                )
//                cc.addField(
//                    cfNew,
//                    "DatatypeConverter.parseBase64Binary(\"MIIBuDCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYUAAoGBAO0DidNibJHhtgxAnM9NszURYU25CVLAlwFdOWhiUkjrjOY459ObRZDVd35hQmN/cCLkDox7y2InJE6PDWfbx9BsgPmPvH75yKgPs3B8pClQVkgIpJp08R59hoZabYuvm7mxCyDGTl2lbrOi0a3j4vM5OoCWKQjIEZ28OpjTyCr3\");"
//                )
//                val cm: CtConstructor = cc.getConstructor("([B)V")
//                cm.insertBeforeBody("if(Arrays.equals($1,__h_ok)){$1=__h_nk;System.out.println(\"============================== agent working ==============================\");}")

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
}
