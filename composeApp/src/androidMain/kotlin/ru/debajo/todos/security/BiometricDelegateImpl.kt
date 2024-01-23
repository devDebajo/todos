package ru.debajo.todos.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.github.aakira.napier.Napier
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.debajo.todos.app.ActivityResultLaunchers
import ru.debajo.todos.common.runCatchingAsync
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.strings.R

// https://gist.github.com/frengky/b2b96a4b1ec234080e9d8a9164240f1a
internal class BiometricDelegateImpl(
    applicationContext: Context,
    private val activityResultLaunchersProvider: () -> ActivityResultLaunchers,
    private val preferences: Preferences,
) : BiometricDelegate {

    private val biometricManager: BiometricManager by lazy { BiometricManager.from(applicationContext) }

    override val available: Boolean
        get() = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS

    override suspend fun encryptData(rawData: String): String? {
        return runCatchingAsync { encodeDataUnsafe(rawData) }
            .onFailure { Napier.e("encodeData error", it) }
            .getOrNull()
    }

    private suspend fun encodeDataUnsafe(rawData: String): String? {
        if (!available) {
            return null
        }
        val secretKey = createSecretKey(SECRET_KEY_ALIAS) ?: return null
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val biometricResult = awaitBiometricPrompt(cipher)

        if (biometricResult is BiometricResult.Succeeded) {
            val encodedData = cipher.doFinal(rawData.toByteArray(Charset.defaultCharset()))
            saveToPrefsBytes(IV_KEY, cipher.iv)
            return Base64.encodeToString(encodedData, Base64.NO_WRAP)
        }

        return null
    }

    override suspend fun decryptData(encryptedData: String): String? {
        return runCatchingAsync { decodeDataUnsafe(encryptedData) }
            .onFailure { Napier.e("decodeData error", it) }
            .getOrNull()
    }

    private suspend fun decodeDataUnsafe(encryptedData: String): String? {
        if (!available) {
            return null
        }
        val secretKey = createSecretKey(SECRET_KEY_ALIAS) ?: return null
        val cipher = getCipher()
        val iv = getFromPrefsBytes(IV_KEY) ?: return null
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        val biometricResult = awaitBiometricPrompt(cipher)

        if (biometricResult is BiometricResult.Succeeded) {
            val decodedData = cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP))
            return String(decodedData, Charset.defaultCharset())
        }

        return null
    }

    private suspend fun awaitBiometricPrompt(cipher: Cipher): BiometricResult {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setTitle(R.strings.biometricTitle)
            .setNegativeButtonText(R.strings.cancel)
            .build()

        val activity = activityResultLaunchersProvider().activity
        return showBiometricPrompt(activity, promptInfo, cipher)
    }

    private suspend fun showBiometricPrompt(activity: FragmentActivity, promptInfo: BiometricPrompt.PromptInfo, cipher: Cipher?): BiometricResult {
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resumeWith(Result.success(BiometricResult.Error(errorCode = errorCode, errString = errString)))
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resumeWith(Result.success(BiometricResult.Succeeded))
                    }
                })

            if (cipher == null) {
                prompt.authenticate(promptInfo)
            } else {
                prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        }
    }

    private fun createSecretKey(keyName: String, force: Boolean = false): SecretKey? {
        return runCatching { createSecretKeyUnsafe(keyName, force) }
            .onFailure { Napier.e("createSecretKey error", it) }
            .getOrNull()
    }

    private fun createSecretKeyUnsafe(keyName: String, force: Boolean): SecretKey? {
        val keyStore = KeyStore.getInstance(KEY_STORE)
        keyStore.load(null)

        if (force) {
            keyStore.deleteEntry(keyName)
        }

        if (!keyStore.containsAlias(keyName)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE)

            keyGenerator.init(
                KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(false) // TODO разобраться с этим
                    .build()
            )

            keyGenerator.generateKey()
        }

        return keyStore.getKey(keyName, null) as? SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
    }

    private sealed interface BiometricResult {
        data class Error(val errorCode: Int, val errString: CharSequence) : BiometricResult

        data object Succeeded : BiometricResult
    }

    private suspend fun saveToPrefsBytes(key: String, data: ByteArray) {
        val base64 = Base64.encodeToString(data, Base64.NO_WRAP)
        preferences.putString(key, base64)
    }

    private suspend fun getFromPrefsBytes(key: String): ByteArray? {
        val base64 = preferences.getString(key)
        if (base64.isNullOrEmpty()) {
            return null
        }

        return Base64.decode(base64, Base64.NO_WRAP)
    }

    private companion object {
        const val SECRET_KEY_ALIAS: String = "KeyStoreSecretAlias"
        const val KEY_STORE: String = "AndroidKeyStore"
        const val IV_KEY: String = "BiometricIv"
    }
}
