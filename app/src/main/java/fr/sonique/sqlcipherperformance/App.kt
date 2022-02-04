package fr.sonique.sqlcipherperformance

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.service.voice.VoiceInteractionService
import androidx.slice.SliceManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Grant the assistant permission when the application is create, it's okay to grant it each time.
        grantAssistantPermissions()
    }

    /**
     * Grant slice permissions to the assistance in order to display slices without user input.
     *
     * Note: this is needed when using AndroidX SliceProvider.
     */
    private fun grantAssistantPermissions() {
        getAssistantPackage()?.let { assistantPackage ->
            val sliceProviderUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(MySliceProvider.SLICE_AUTHORITY)
                .build()

            SliceManager.getInstance(this).grantSlicePermission(assistantPackage, sliceProviderUri)
        }
    }

    /**
     * Find the assistant package name
     */
    private fun getAssistantPackage(): String? {
        val resolveInfoList = packageManager?.queryIntentServices(
            Intent(VoiceInteractionService.SERVICE_INTERFACE), 0
        )
        return resolveInfoList?.firstOrNull()?.serviceInfo?.packageName
    }
}