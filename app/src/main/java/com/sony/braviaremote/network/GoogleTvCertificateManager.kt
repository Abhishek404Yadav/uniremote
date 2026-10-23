package com.sony.braviaremote.network

import android.content.Context
import android.util.Base64
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.net.Socket
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import javax.net.ssl.*

class GoogleTvCertificateManager(context: Context) {

    private val prefs = context.getSharedPreferences("google_tv_crypto_prefs", Context.MODE_PRIVATE)

    companion object {
        init {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(BouncyCastleProvider())
            }
        }

        private const val KEY_PRIVATE_KEY = "gtv_private_key"
        private const val KEY_CERTIFICATE = "gtv_certificate"
    }

    private var cachedKeyPair: KeyPair? = null
    private var cachedCertificate: X509Certificate? = null

    init {
        loadOrGenerateCredentials()
    }

    val certificate: X509Certificate
        get() = cachedCertificate ?: loadOrGenerateCredentials().second

    val keyPair: KeyPair
        get() = cachedKeyPair ?: loadOrGenerateCredentials().first

    @Synchronized
    private fun loadOrGenerateCredentials(): Pair<KeyPair, X509Certificate> {
        val savedKeyB64 = prefs.getString(KEY_PRIVATE_KEY, null)
        val savedCertB64 = prefs.getString(KEY_CERTIFICATE, null)

        if (!savedKeyB64.isNullOrBlank() && !savedCertB64.isNullOrBlank()) {
            try {
                val keyBytes = Base64.decode(savedKeyB64, Base64.DEFAULT)
                val certBytes = Base64.decode(savedCertB64, Base64.DEFAULT)

                val keyFactory = KeyFactory.getInstance("RSA")
                val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(keyBytes)) as RSAPrivateKey

                val certFactory = CertificateFactory.getInstance("X.509")
                val cert = certFactory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate

                val pair = KeyPair(cert.publicKey, privateKey)
                cachedKeyPair = pair
                cachedCertificate = cert
                return Pair(pair, cert)
            } catch (e: Exception) {
                // If loading fails, generate fresh credentials
            }
        }

        val generated = generateNewCredentials()
        cachedKeyPair = generated.first
        cachedCertificate = generated.second

        prefs.edit()
            .putString(KEY_PRIVATE_KEY, Base64.encodeToString(generated.first.private.encoded, Base64.NO_WRAP))
            .putString(KEY_CERTIFICATE, Base64.encodeToString(generated.second.encoded, Base64.NO_WRAP))
            .apply()

        return generated
    }

    private fun generateNewCredentials(): Pair<KeyPair, X509Certificate> {
        val kpg = KeyPairGenerator.getInstance("RSA", "BC")
        kpg.initialize(2048, SecureRandom())
        val keyPair = kpg.generateKeyPair()

        val commonName = "CN=atvremote, O=Google, C=US"
        val subject = X500Name(commonName)
        val serial = BigInteger(64, SecureRandom())
        val notBefore = Date(System.currentTimeMillis() - 86400000L) // Yesterday
        val calendar = Calendar.getInstance().apply {
            time = notBefore
            add(Calendar.YEAR, 20)
        }
        val notAfter = calendar.time

        val certBuilder = JcaX509v3CertificateBuilder(
            subject,
            serial,
            notBefore,
            notAfter,
            subject,
            keyPair.public
        )

        val signer = JcaContentSignerBuilder("SHA256WithRSAEncryption")
            .setProvider("BC")
            .build(keyPair.private)

        val certHolder = certBuilder.build(signer)
        val cert = JcaX509CertificateConverter()
            .setProvider("BC")
            .getCertificate(certHolder)

        return Pair(keyPair, cert)
    }

    /**
     * Builds an SSLContext configured with our client certificate and trust-all manager.
     */
    fun createSslContext(): SSLContext {
        val cert = certificate
        val key = keyPair.private

        val keyManager = object : X509KeyManager {
            override fun getClientAliases(keyType: String?, issuers: Array<out Principal>?): Array<String> = arrayOf("client")
            override fun chooseClientAlias(keyType: Array<out String>?, issuers: Array<out Principal>?, socket: Socket?): String = "client"
            override fun getServerAliases(keyType: String?, issuers: Array<out Principal>?): Array<String>? = null
            override fun chooseServerAlias(keyType: String?, issuers: Array<out Principal>?, socket: Socket?): String? = null
            override fun getCertificateChain(alias: String?): Array<X509Certificate> = arrayOf(cert)
            override fun getPrivateKey(alias: String?): PrivateKey = key
        }

        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(arrayOf(keyManager), arrayOf(trustManager), SecureRandom())
        return sslContext
    }

    /**
     * Computes the pairing secret hash from the client and server certificates and the on-screen PIN.
     * Code format: 6-character hexadecimal PIN, e.g. "A1B2C3".
     */
    fun computePairingSecret(serverCertificate: X509Certificate, pinInput: String): ByteArray {
        val cleanPin = pinInput.trim().uppercase()
        if (cleanPin.length < 4) {
            throw IllegalArgumentException("PIN must be at least 4 characters")
        }

        val clientRsa = certificate.publicKey as RSAPublicKey
        val serverRsa = serverCertificate.publicKey as RSAPublicKey

        val clientModulus = toFixedByteArray(clientRsa.modulus, 256)
        val clientExponent = toFixedByteArray(clientRsa.publicExponent, 3)
        val serverModulus = toFixedByteArray(serverRsa.modulus, 256)
        val serverExponent = toFixedByteArray(serverRsa.publicExponent, 3)

        // The PIN is hashed without its first 2 characters (first byte)
        val pinBytes = hexStringToBytes(cleanPin)
        val pinRestBytes = hexStringToBytes(cleanPin.substring(2))

        val md = MessageDigest.getInstance("SHA-256")
        md.update(clientModulus)
        md.update(clientExponent)
        md.update(serverModulus)
        md.update(serverExponent)
        md.update(pinRestBytes)
        val hash = md.digest()

        // Verify that the first byte of the digest matches the first byte of the PIN
        if (hash[0] != pinBytes[0]) {
            throw IllegalArgumentException("Pairing PIN mismatch. Please check the code on your TV screen.")
        }

        return hash
    }

    private fun toFixedByteArray(value: BigInteger, size: Int): ByteArray {
        val bytes = value.toByteArray()
        if (bytes.size == size) return bytes
        if (bytes.size == size + 1 && bytes[0] == 0.toByte()) {
            return bytes.copyOfRange(1, bytes.size)
        }
        val result = ByteArray(size)
        val srcPos = if (bytes.size > size) bytes.size - size else 0
        val dstPos = if (size > bytes.size) size - bytes.size else 0
        val len = minOf(bytes.size, size)
        System.arraycopy(bytes, srcPos, result, dstPos, len)
        return result
    }

    private fun hexStringToBytes(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
