package islamalorabi.shafeezekr.pbuh.update

import retrofit2.http.GET

interface GithubApi {
    @GET("repos/IslamAlorabI/ShafeeZekr/releases/latest")
    suspend fun getLatestRelease(): GithubRelease
}
