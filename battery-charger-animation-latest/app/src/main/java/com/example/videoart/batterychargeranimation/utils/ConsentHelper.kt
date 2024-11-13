package com.example.videoart.batterychargeranimation.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.videoart.batterychargeranimation.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentInformation.ConsentStatus
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

fun isGDPR(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val gdpr = prefs.getInt("IABTCF_gdprApplies", 0)
    return gdpr == 1
}

fun canShowAds(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
    //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

    val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
    val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
    val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
    val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

    val googleId = 755
    val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
    val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

    // Minimum required for at least non-personalized ads
    return hasConsentFor(listOf(1), purposeConsent, hasGoogleVendorConsent)
            && hasConsentOrLegitimateInterestFor(
        listOf(2, 7, 9, 10),
        purposeConsent,
        purposeLI,
        hasGoogleVendorConsent,
        hasGoogleVendorLI
    )

}

fun canShowPersonalizedAds(context: Context): Boolean {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
    //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

    val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
    val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
    val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
    val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

    val googleId = 755
    val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
    val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

    return hasConsentFor(listOf(1, 3, 4), purposeConsent, hasGoogleVendorConsent)
            && hasConsentOrLegitimateInterestFor(
        listOf(2, 7, 9, 10),
        purposeConsent,
        purposeLI,
        hasGoogleVendorConsent,
        hasGoogleVendorLI
    )
}

// Check if a binary string has a "1" at position "index" (1-based)
private fun hasAttribute(input: String, index: Int): Boolean {
    return input.length >= index && input[index - 1] == '1'
}

// Check if consent is given for a list of purposes
private fun hasConsentFor(
    purposes: List<Int>,
    purposeConsent: String,
    hasVendorConsent: Boolean
): Boolean {
    return purposes.all { p -> hasAttribute(purposeConsent, p) } && hasVendorConsent
}

// Check if a vendor either has consent or legitimate interest for a list of purposes
private fun hasConsentOrLegitimateInterestFor(
    purposes: List<Int>,
    purposeConsent: String,
    purposeLI: String,
    hasVendorConsent: Boolean,
    hasVendorLI: Boolean
): Boolean {
    return purposes.all { p ->
        (hasAttribute(purposeLI, p) && hasVendorLI) ||
                (hasAttribute(purposeConsent, p) && hasVendorConsent)
    }
}

object ConsentHelper {
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private var showingForm = false
    private var showingWarning = false
    private var hasShowedForm = false

    private fun initializeMobileAdsSdk(context: Context) {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(context)
    }

    // Called from app settings to determine whether to
    // show a button so the user can launch the dialog
    fun isUpdateConsentButtonRequired(context: Context): Boolean {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        return consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    // Called when the user clicks the button to launch
    // the CMP dialog and change their selections
    fun updateConsent(
        context: Activity,
        loadAds: () -> Unit,
        onDone: () -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(context) { error ->
            hasShowedForm = true
            val ci = UserMessagingPlatform.getConsentInformation(context)
            handleConsentResult(context, ci, loadAds = loadAds, onDone = onDone)
        }
    }

    fun obtain(context: AppCompatActivity, loadAds: () -> Unit, onDone: () -> Unit) {
        val ci = UserMessagingPlatform.getConsentInformation(context)
        if (ci.consentStatus == ConsentStatus.OBTAINED) {
            Log.d("${this::class.simpleName}", "call to 1")
            updateConsent(context, loadAds, onDone)
        } else {
            Log.d("${this::class.simpleName}", "call to 2")
            obtainConsentAndShow(context, loadAds, onDone)
        }
    }

    // Called from onCreate or on app load somewhere
    private fun obtainConsentAndShow(context: AppCompatActivity, loadAds: () -> Unit, onDone: () -> Unit) {
        val params = if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("282086AFAF8C1E500705AB94C8D1F71E") // Get ID from Logcat
                .addTestDeviceHashedId("DC7F90218FB89875BC491D375ADABED9") // Get ID from Logcat
                .addTestDeviceHashedId("BF2EDA2B0FEA80BA70876B306954F3AB")
                .addTestDeviceHashedId("8C2A2EF6FC4B4D84745C69452C09560D")
                .build()
            ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .setConsentDebugSettings(debugSettings)
                .build()
        } else {
            ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()
        }

        val ci = UserMessagingPlatform.getConsentInformation(context)
        Log.d(
            "${this::class.simpleName}",
            "obtainConsentAndShow: ${ci.privacyOptionsRequirementStatus} ${ci.consentStatus} ${ci.isConsentFormAvailable}"
        )


        ci.requestConsentInfoUpdate(
            context,
            params,
            {   // Load and show the consent form. Add guard to prevent showing form more than once at a time.
                if (showingForm) return@requestConsentInfoUpdate

                showingForm = true
//                hasShowedForm = !ci.isConsentFormAvailable && isUpdateConsentButtonRequired(context)
                Log.d(
                    "${this::class.simpleName}",
                    "obtainConsentAndShow1: ${ci.privacyOptionsRequirementStatus} ${ci.consentStatus} ${ci.isConsentFormAvailable}"
                )

                UserMessagingPlatform.loadAndShowConsentFormIfRequired(context) { error: FormError? ->
                    showingForm = false
                    Log.d("${this::class.simpleName}", "error: ${error?.message}")
                    handleConsentResult(context, ci, loadAds, onDone)
                }
            },
            { error ->
                // Consent gathering failed.
                Log.w("AD_HANDLER", "${error.errorCode}: ${error.message}")
            })

        // Consent has been gathered already, load ads
        if (ci.canRequestAds()) {
            initializeMobileAdsSdk(context.applicationContext)
            loadAds()
        }
    }

    private fun handleConsentResult(
        context: Activity,
        ci: ConsentInformation,
        loadAds: () -> Unit,
        onDone: () -> Unit
    ) {

        Log.d(
            "${this::class.simpleName}",
            "handleConsentResult: ${ci.privacyOptionsRequirementStatus} ${ci.consentStatus} ${ci.isConsentFormAvailable}"
        )
        if (ci.canRequestAds()) {
            logConsentChoices(context, loadAds, onDone)
        } else {
            // This is an error state - should never get here
            logConsentChoices(context, loadAds, onDone)
        }

        // Consent has been gathered.

    }

    private fun logConsentChoices(context: Activity, loadAds: () -> Unit, onDone: () -> Unit) {
        // After completing the consent workflow, check the
        // strings in SharedPreferences to see what they
        // consented to and act accordingly
        val canShow = canShowAds(context)
        val isEEA = isGDPR(context)

        // Check what level of consent the user actually provided
        Log.d("${this::class.simpleName}", "TEST:    user consent choices")
        Log.d("${this::class.simpleName}", "TEST:      is EEA = $isEEA")
        Log.d("${this::class.simpleName}", "TEST:      can show ads = $canShow")
        Log.d(
            "${this::class.simpleName}",
            "TEST:      can show personalized ads = ${canShowPersonalizedAds(context)}"
        )

        if (!isEEA) {
            initializeMobileAdsSdk(context.applicationContext)
            loadAds.invoke()
            return
        }

        if (!canShow) {
            onDone.invoke()
        } else {
            initializeMobileAdsSdk(context.applicationContext)
            loadAds.invoke()
        }

        // handle user choice, activate trial mode, etc

    }
}