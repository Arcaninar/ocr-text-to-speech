package com.ocrtts.utils
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import com.google.common.io.BaseEncoding
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object ShaUtility {
    fun getSignature(pm: PackageManager, packageName: String): String? {
        return try {
            val packageInfo: PackageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            packageInfo.signatures?.firstOrNull()?.let { signature ->
                signatureDigest(signature)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun signatureDigest(sig: Signature): String? {
        val signature: ByteArray = sig.toByteArray()
        return try {
            val md = MessageDigest.getInstance("SHA1")
            val digest: ByteArray = md.digest(signature)
            BaseEncoding.base16().lowerCase().encode(digest)
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }
}