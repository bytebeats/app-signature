package me.bytebeats.signature

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * Created by bytebeats on 2021/7/15 : 16:50
 * E-mail: happychinapc@gmail.com
 * Quote: Peasant. Educated. Worker
 */
object AppSignature {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    private fun requireSdk28(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    private fun getSignatures(context: Context, packageName: String): Array<Signature> {
        return if (requireSdk28())
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo.signingCertificateHistory
        else {
            context.packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            ).signatures
        }
    }

    fun getFirstCryptedSignature(
        context: Context,
        packageName: String,
        cryptType: CryptType = CryptType.MD5
    ): String {
        val signatures = getSignatures(context, packageName)
        if (!signatures.isNullOrEmpty()) {
            return getCryptedString(signatures[0].toByteArray(), cryptType)
        }
        return ""
    }

    fun getTotalCryptedSignature(
        context: Context,
        packageName: String,
        cryptType: CryptType = CryptType.MD5
    ): String {
        val details = StringBuilder()
        val signatures = getSignatures(context, packageName)
        signatures.forEachIndexed { index, signature ->
            if (index != 0) {
                details.append("\n")
            }
            details.append(getCryptedString(signature.toByteArray(), cryptType))
        }
        return details.toString()
    }

    fun getAppFirstCryptedSignature(
        context: Context,
        cryptType: CryptType = CryptType.MD5
    ): String {
        return getFirstCryptedSignature(context, context.packageName, cryptType)
    }

    fun getAppTotalCryptedSignature(
        context: Context,
        cryptType: CryptType = CryptType.MD5
    ): String {
        return getTotalCryptedSignature(context, context.packageName, cryptType)
    }

    fun getAppSignatures(context: Context): Array<Signature> {
        return getSignatures(context, context.packageName)
    }

    fun getInstalledAppPackageInfos(context: Context): List<PackageInfo> {
        return context.packageManager.getInstalledPackages(
            if (requireSdk28())
                PackageManager.GET_SIGNING_CERTIFICATES
            else
                PackageManager.GET_SIGNATURES
        )
    }

    fun getInstalledAppSignatures(context: Context): List<Array<Signature>> {
        return getInstalledAppPackageInfos(context).map { packageInfo ->
            getSignatures(
                context,
                packageInfo.packageName
            )
        }
    }

    fun getInstalledAppFirstCryptedSignatures(
        context: Context,
        cryptType: CryptType = CryptType.MD5
    ): List<String> {
        return getInstalledAppSignatures(context).map { it[0] }
            .map { getCryptedString(it.toByteArray(), cryptType) }
    }

    fun getInstalledAppTotalCryptedSignatures(
        context: Context,
        cryptType: CryptType = CryptType.MD5
    ): List<String> {
        return getInstalledAppPackageInfos(context).map { packageInfo ->
            getTotalCryptedSignature(
                context,
                packageInfo.packageName,
                cryptType
            )
        }
    }

    fun getDecryptedSignature(signature: Signature): StringBuilder {
        val details = StringBuilder()
        try {
            val rawCertificate = signature.toByteArray()
            val certStream = ByteArrayInputStream(rawCertificate)
            val certificateFactory =
                CertificateFactory.getInstance("X509") as CertificateFactory
            val x509Certificate =
                certificateFactory.generateCertificate(certStream) as X509Certificate
            details.append("Certificate subject: ${x509Certificate.subjectDN}\n")
            details.append("Certificate issuer: ${x509Certificate.issuerDN}\n")
            details.append("Certificate serialNumber: ${x509Certificate.serialNumber}\n")
        } catch (e: CertificateException) {
            e.printStackTrace()
        }
        return details
    }

    fun getFirstDecryptedSignature(context: Context, packageName: String): String {
        val signatures = getSignatures(context, packageName)
        if (!signatures.isNullOrEmpty()) {
            val details = StringBuilder("<br>$packageName<br/>\n")
            details.append(getDecryptedSignature(signatures.first()))
            return details.toString()
        }
        return ""
    }

    fun getTotalDecryptedSignature(context: Context, packageName: String): String {
        val signatures = getSignatures(context, packageName)
        if (!signatures.isNullOrEmpty()) {
            val details = StringBuilder("<br>$packageName<br/>\n")
            signatures.forEachIndexed { index, signature ->
                if (index != 0) {
                    details.append("\n")
                }
                details.append(getDecryptedSignature(signature))
            }
            return details.toString()
        }
        return ""
    }

    fun getAppFirstDecryptedSignature(context: Context): String {
        return getFirstDecryptedSignature(context, context.packageName)
    }

    fun getAppTotalDecryptedSignature(context: Context): List<String> {
        return getAppSignatures(context).map {
            getDecryptedSignature(it).insert(0, "\n").toString()
        }
    }

    fun getInstalledAppFirstDecryptedSignature(context: Context): List<String> {
        return getInstalledAppPackageInfos(context).map {
            val signatures = getSignatures(context, it.packageName)
            val details = StringBuilder("<br>${it.packageName}<br/>\n")
            details.append(getDecryptedSignature(signatures.first()))
            details.toString()
        }
    }

    private fun getCryptedString(
        signature: ByteArray,
        cryptType: CryptType = CryptType.MD5
    ): String {
        val digest = MessageDigest.getInstance(cryptType.name)
        digest.update(signature)
        val hashText = digest.digest()
        return toHex(hashText)
    }

    private fun getLocalApkPackageInfo(context: Context, apkPath: String): PackageInfo? {
        return context.packageManager.getPackageArchiveInfo(
            apkPath,
            if (requireSdk28()) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        )
    }

    fun getLocalApkSignatures(context: Context, apkPath: String): Array<Signature>? {
        val packageInfo = getLocalApkPackageInfo(context, apkPath)
        return if (requireSdk28()) packageInfo?.signingInfo?.signingCertificateHistory else packageInfo?.signatures
    }

    fun getLocalApkFirstCryptedSignature(
        context: Context,
        apkPath: String,
        cryptType: CryptType = CryptType.MD5
    ): String {
        return getLocalApkSignatures(context, apkPath)?.first()?.let {
            getCryptedString(it.toByteArray(), cryptType)
        } ?: ""
    }

    fun getLocalApkTotalCryptedSignature(
        context: Context,
        apkPath: String,
        cryptType: CryptType = CryptType.MD5
    ): String {
        return getLocalApkSignatures(context, apkPath)?.let { signatures ->
            val details = StringBuilder()
            signatures.forEach { signature ->
                details.append("\n")
                details.append(getCryptedString(signature.toByteArray(), cryptType))
            }
            details.toString()
        } ?: ""
    }

    fun getLocalApkFirstDecryptedSignature(
        context: Context,
        apkPath: String
    ): String {
        return getLocalApkSignatures(context, apkPath)?.first()?.let {
            getDecryptedSignature(it).toString()
        } ?: ""
    }

    fun getLocalApkTotalDecryptedSignature(
        context: Context,
        apkPath: String
    ): String {
        return getLocalApkSignatures(context, apkPath)?.let { signatures ->
            val details = StringBuilder()
            signatures.forEach { signature ->
                details.append("\n")
                details.append(getDecryptedSignature(signature))
            }
            details.toString()
        } ?: ""
    }

    private fun toHex(bytes: ByteArray): String {
        val hexArray =
            arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val hexChars = CharArray(bytes.size * 2)
        var v = 0
        bytes.forEachIndexed { index, byte ->
            v = byte.toInt() and 0xFF
            hexChars[index * 2] = hexArray[v ushr 4]
            hexChars[index * 2 + 1] = hexArray[v and 0x0f]
        }
        return String(hexChars)
    }

    enum class CryptType {
        MD5, SHA1, SHA256
    }

}