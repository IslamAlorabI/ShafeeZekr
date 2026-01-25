package islamalorabi.shafeezekr.pbuh.update

import android.util.Log
import islamalorabi.shafeezekr.pbuh.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UpdateManager {

    private const val BASE_URL = "https://api.github.com/"
    private const val TAG = "UpdateManager"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(GithubApi::class.java)

    suspend fun checkForUpdates(): GithubRelease? {
        return try {
            val release = service.getLatestRelease()
            val currentVersion = BuildConfig.VERSION_NAME
            
            // Basic semantic version comparison logic or string comparison
            // Assuming tag_name is like "v1.0.1" or "1.0.1"
            val remoteVersion = release.tagName.removePrefix("v")
            
            if (isUpdateAvailable(currentVersion, remoteVersion)) {
                release
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            null
        }
    }

    private fun isUpdateAvailable(current: String, remote: String): Boolean {
        // Simple version comparison logic
        // This can be improved to handle semantic versioning properly if needed
        return remote != current
    }
}
