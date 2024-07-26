package org.forfree.atlassian

import com.atlassian.extras.common.org.springframework.util.DefaultPropertiesPersister
import com.atlassian.extras.common.LicensePropertiesConstants
import  com.atlassian.extras.api.LicenseEdition
import com.atlassian.extras.api.LicenseType

import com.atlassian.extras.decoder.v2.Version2LicenseDecoder
import com.atlassian.extras.keymanager.Key
import com.atlassian.extras.keymanager.KeyManager
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.versionOption
import org.apache.commons.codec.binary.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

object Constants {
    const val LICENSE_STRING_KEY_V2 = "LICENSE_STRING_KEY_V2"
    const val KEY_ALGORITHM = "DSA"
    const val SIGNATURE_ALGORITHM = "SHA1withDSA"
}

class Atlassian : CliktCommand() {
    fun help(context: Context): String = """
    Repo is a command line tool that showcases how to build complex
    command line interfaces with Clikt.

    This tool is supposed to look like a distributed version control
    system to show how something like this can be structured.
    """.trimIndent()

    init {
        versionOption("1.0")
    }

    override fun run() {
    }
}

class Product: CliktCommand() {
    override fun run() {
        val products = com.atlassian.extras.api.Product.getAtalssianProductsAsArray()
        val editions = LicenseEdition.entries.toTypedArray()
        val types = LicenseType.entries.toTypedArray()
        echo("products: $products")
        echo("editions: $editions")
        echo("types: $types")
    }
}

class Keypair : CliktCommand() {
    private val pubFile: String by option("--pub", "-k", help = "Public key with pkcs8 format").required()
    private val privFile: String by option("--priv", "-i").required()
    override fun run() {
        try {
            val pub = readX509PublicKey(File(pubFile), Constants. KEY_ALGORITHM)
            val priv = readPKCS8PrivateKey(File(privFile),  Constants. KEY_ALGORITHM)
            val payload = "hello"
            val hash = signWithKey(priv, payload,  Constants. SIGNATURE_ALGORITHM)
            val valid = verifyWithKey(pub, payload, hash, Constants. SIGNATURE_ALGORITHM)
            echo("keypair: $valid")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}

class License : CliktCommand() {
    override fun run() {
    }
}

class Verify : CliktCommand() {
    private val keyFile: String by option("--key", "-k", help = "Public key with pkcs8 format").required()
    private val inPath: String by option("--in", "-i").required()

    override fun run() {
        val pubKey = File(keyFile).readText()
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PUBLIC KEY-----", "")
        val manager = KeyManager.getInstance()
        manager.loadKey(Key(pubKey, Constants.LICENSE_STRING_KEY_V2, Key.Type.PUBLIC))
        val lic = File(inPath).readText()
        val decoder = Version2LicenseDecoder()
        val props = decoder.doDecode(lic)
        echo(props.toString())
    }
}

class Generate : CliktCommand() {
    private val keyFile: String by option("--key", "-k", help = "Private key with pkcs8 format").required()
    private val inPath: String by option("--in", "-i", help = "Java property file").required()

    override fun run() {
        val manager = KeyManager.getInstance()
        val privateKey = File(keyFile).readText()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace(System.lineSeparator(), "")
            .replace("-----END PRIVATE KEY-----", "")
        manager.loadKey(Key(privateKey, Constants.LICENSE_STRING_KEY_V2, Key.Type.PRIVATE))

        val props = Properties()
        props.load(StringReader(File(inPath).readText()))
        val os = ByteArrayOutputStream()
        os.write(Version2LicenseDecoder.LICENSE_PREFIX);
        val dos = DeflaterOutputStream(os, Deflater());
        val writer = OutputStreamWriter(dos, "UTF-8");
        DefaultPropertiesPersister().store(props, writer, null);
        writer.close();
        val licenseData = os.toByteArray()
        val payload = String(Base64.encodeBase64(licenseData), StandardCharsets.UTF_8)
        val hash = manager.sign(payload, Constants.LICENSE_STRING_KEY_V2)
        val licenseContent = Version2LicenseDecoder.packLicense(licenseData, Base64.decodeBase64(hash))
        echo("$licenseContent")
    }
}

fun verifyWithKey(key: PublicKey, payload: String, hash: String, algo: String): Boolean {
    try {
        val signature = Signature.getInstance(algo)
        signature.initVerify(key)
        signature.update(Base64.decodeBase64(payload))
        return signature.verify(Base64.decodeBase64(hash))
    } catch (e: Exception) {
        throw RuntimeException("Failed to sign", e)
    }
}

fun signWithKey(key: PrivateKey, payload: String, algo: String): String {
    try {
        val signature = Signature.getInstance(algo)
        signature.initSign(key)
        signature.update(Base64.decodeBase64(payload))
        return String(Base64.encodeBase64(signature.sign()), StandardCharsets.UTF_8)
    } catch (e: Exception) {
        throw RuntimeException("Failed to sign", e)
    }
}

fun readX509PublicKey(file: File, algo: String): PublicKey {
    val key = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
    val data = key
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace(System.lineSeparator().toRegex(), "")
        .replace("-----END PUBLIC KEY-----", "")
    val encoded = Base64.decodeBase64(data)
    val kf = KeyFactory.getInstance(algo)
    return kf.generatePublic(X509EncodedKeySpec(encoded))
}

fun readPKCS8PrivateKey(file: File, algo: String): PrivateKey {
    val key = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
    val data = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace(System.lineSeparator().toRegex(), "")
        .replace("-----END PRIVATE KEY-----", "")
    val encoded = Base64.decodeBase64(data)
    val kf = KeyFactory.getInstance(algo)
    return kf.generatePrivate(PKCS8EncodedKeySpec(encoded))
}

object App {
    @JvmStatic
    fun main(args: Array<String>) =
        Atlassian().subcommands(License().subcommands(Generate(), Verify()), Keypair()).main(args)
}
