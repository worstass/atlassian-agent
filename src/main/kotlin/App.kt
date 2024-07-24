package org.forfree.atlassian

import com.atlassian.extras.common.org.springframework.util.DefaultPropertiesPersister
import com.atlassian.extras.decoder.v2.Version2LicenseDecoder
import com.atlassian.extras.keymanager.Key
import com.atlassian.extras.keymanager.KeyManager
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.versionOption
import java.io.*
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

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

class Keypair: CliktCommand() {
    override fun run() {
        try {
            val keygen = KeyPairGenerator.getInstance("DSA")
            keygen.initialize(1024)
            val pair = keygen.genKeyPair()
            val privateKey = pair.private
            val publicKey = pair.public

            var ksp = PKCS8EncodedKeySpec(publicKey.encoded)
            var fos = FileOutputStream("public.key")
            fos.write(ksp.encoded)
            fos.close()

            ksp = PKCS8EncodedKeySpec(privateKey.encoded)
            fos = FileOutputStream("private.key")
            fos.write(ksp.encoded)
            fos.close()
            echo("done")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}

class License: CliktCommand() {
    override fun run() {
    }
}

class Verify:  CliktCommand() {
    private val pubKey: String by option("--pubkey", "-k").required()
    private val inPath: String by option("--in", "-i").required()

    override fun run() {

    }
}

class Generate: CliktCommand() {
    private val keyFile: String by option("--key", "-k").required()
    private val inPath: String by option("--in", "-i").required()

    override fun run() {
        val manager = KeyManager.getInstance()
        val LICENSE_STRING_KEY_V2 = "LICENSE_STRING_KEY_V2"
        val LICENSE_STRING_KEY = "1600708331"
        manager.loadKey(Key("", "LICENSE_STRING_KEY_V2", Key.Type.PUBLIC))
        manager.loadKey(Key("", "1600708331", Key.Type.PRIVATE))

        val props = Properties()
        props.load(StringReader(File(inPath).readText()))
//        return props

//
//                val df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        String today = df.format(LocalDate.now());
//        val props = Properties()
//        props.setProperty(LicensePropertiesConstants.LICENSE_ID, "LIDSEN-L10468221");
//        props.setProperty(LicensePropertiesConstants.LICENSE_VERSION, "2");
//        props.setProperty(LicensePropertiesConstants.LICENSE_TYPE_NAME, LicenseType.COMMERCIAL.toString());
//        props.setProperty(LicensePropertiesConstants.LICENSE_EXPIRY_DATE, df.format(LocalDate.now().plusDays(3650)));
//        props.setProperty(LicensePropertiesConstants.MAX_NUMBER_OF_USERS, LicensePropertiesConstants.UNLIMITED_USERS.toString());
//        props.setProperty(LicensePropertiesConstants.PURCHASE_DATE, df.format(LocalDate.now()));
//        props.setProperty(LicensePropertiesConstants.CREATION_DATE, df.format(LocalDate.now()));
//        props.setProperty(LicensePropertiesConstants.MAINTENANCE_EXPIRY_DATE, df.format(LocalDate.now().plusDays(3650)));
//        props.setProperty(LicensePropertiesConstants.CONTACT_EMAIL, "hole@badass.org");
//        props.setProperty(LicensePropertiesConstants.SERVER_ID, sId);
//        props.setProperty(LicensePropertiesConstants.SUPPORT_ENTITLEMENT_NUMBER, "SEN-L10468221");
//        props.setProperty(LicensePropertiesConstants.ORGANISATION, "Badass")
//        props.setProperty(LicensePropertiesConstants.EVALUATION_LICENSE, "false")
//        props.setProperty(LicensePropertiesConstants.getKey(Product.JIRA, LicensePropertiesConstants.LICENSE_TYPE_NAME), lic.getNewLicenseTypeName());
//
//        props.setProperty("jira.LicenseEdition",  LicenseEdition.UNLIMITED.toString());
//        props.setProperty("jira.active", LicensePropertiesConstants.ACTIVE_VALUE);
//        props.setProperty("jira.NumberOfUsers", LicensePropertiesConstants.UNLIMITED_USERS.toString())
//        props.setProperty("jira.product.jira-software.Starter", "false");
//        props.setProperty("jira.product.jira-software.active", LicensePropertiesConstants.ACTIVE_VALUE)
//        props.setProperty("greenhopper.LicenseTypeName", LicenseType.COMMERCIAL.toString());
//        props.setProperty("greenhopper.LicenseEdition",  LicenseEdition.UNLIMITED.toString());
//        props.setProperty("greenhopper.active", LicensePropertiesConstants.ACTIVE_VALUE);
//        props.setProperty("greenhopper.enterprise", LicensePropertiesConstants.ACTIVE_VALUE);
////        props.setProperty(LicensePropertiesConstants.DESCRIPTION, lic.getDescription());
//
//        val products = Product.getAtalssianProductsAsArray()
//        for (p in products) {


//            props.setProperty(p.getNamespace()+"."+LicensePropertiesConstants.LICENSE_EDITION,LicenseEdition.ENTERPRISE.name());
//            props.setProperty(p.getNamespace()+"."+LicensePropertiesConstants.LICENSE_TYPE_NAME,LicenseType.COMMERCIAL.name());
//            props.setProperty(p.getNamespace()+"."+LicensePropertiesConstants.ACTIVE_FLAG,LicensePropertiesConstants.ACTIVE_VALUE);
//            props.setProperty(p.getNamespace()+"."+LicensePropertiesConstants.MAX_NUMBER_OF_USERS, String.valueOf(LicensePropertiesConstants.UNLIMITED_USERS));
//            System.out.println(p.getNamespace());
//            System.out.println(p.getName());
//        }

        val os = ByteArrayOutputStream();
            os.write(Version2LicenseDecoder.LICENSE_PREFIX);
            val dos = DeflaterOutputStream(os, Deflater());
            val writer = OutputStreamWriter(dos, "UTF-8");
            DefaultPropertiesPersister().store(props, writer, null);
            writer.close();
            val licenseText = os.toByteArray();
        val lic =  java.util.Base64.getEncoder().encodeToString(licenseText)
//            val lic =  Base64.Default.encode(licenseText)
//            val licenseText = writer.toString().byteInputStream().readAllBytes()
//            val dsa = Signature.getInstance("SHA1withDSA");
//            val privateKey = manager.getPrivateKey(LICENSE_STRING_KEY_V2)
//            dsa.initSign(privateKey);
//            dsa.update(licenseText);
//            val hash = dsa.sign();
//            if (KeyVerifier.verify(licenseContent)) {
//                System.out.println("License: " + licenseContent);
//            } else {
//                System.out.println("Can't generate a license! Something went wrong");
//            }

           val hash =  manager.sign(lic, LICENSE_STRING_KEY_V2)
        val licenseContent = Version2LicenseDecoder.packLicense(licenseText, hash.toByteArray());
        echo("License: $licenseContent")
    }
}

object App {
    @JvmStatic
    fun main(args: Array<String>) = Atlassian().subcommands(License().subcommands(Generate(), Verify()), Keypair()).main(args)
}
