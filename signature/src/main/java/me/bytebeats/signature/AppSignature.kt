package me.bytebeats.signature

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import java.security.MessageDigest

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

    fun getSignatures(context: Context, packageName: String): Array<Signature> {
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

    fun getSimpleSignature(
        context: Context,
        packageName: String,
        cryptType: CryptType = CryptType.MD5
    ): String {
        val signatures = getSignatures(context, packageName)
        if (!signatures.isNullOrEmpty()) {
            return getDigestString(signatures[0].toByteArray(), cryptType)
        }
        return ""
    }

    fun getDetailedSignature(
        context: Context,
        packageName: String,
        cryptType: CryptType
    ): String {
        val digest = StringBuilder()
        val signatures = getSignatures(context, packageName)
        signatures.forEachIndexed { index, signature ->
            digest.append(getDigestString(signature.toByteArray(), cryptType))
            if (index != signatures.lastIndex) {
                digest.append("\n")
            }
        }
        return digest.toString()
    }

    fun getAppSimpleSignature(context: Context, cryptType: CryptType = CryptType.MD5): String {
        return getSimpleSignature(context, context.packageName, cryptType)
    }

    fun getAppDetailedSignature(
        context: Context,
        cryptType: CryptType
    ): String {
        return getDetailedSignature(context, context.packageName, cryptType)
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

    fun getInstalledAppSimpleSignatures(
        context: Context,
        cryptType: CryptType = CryptType.MD5
    ): List<String> {
        return getInstalledAppSignatures(context).map { it[0] }
            .map { getDigestString(it.toByteArray(), cryptType) }
    }

    fun getInstalledAppDetailedSignatures(
        context: Context,
        cryptType: CryptType = CryptType.MD5
    ): List<String> {
        return getInstalledAppPackageInfos(context).map { packageInfo ->
            getDetailedSignature(
                context,
                packageInfo.packageName,
                cryptType
            )
        }
    }

    fun getDigestString(signature: ByteArray, cryptType: CryptType = CryptType.MD5): String {
        val digest = MessageDigest.getInstance(cryptType.name)
        digest.update(signature)
        val hashText = digest.digest()
        return toHex(hashText)
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