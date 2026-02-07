package islamalorabi.shafeezekr.pbuh

import android.app.Application
import android.content.Context
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils

class ShafeeZekrApplication : Application() {
    
    override fun attachBaseContext(base: Context) {
        val languageCode = base.getSharedPreferences("settings_sync", Context.MODE_PRIVATE)
            .getString("language_code", "") ?: ""
        super.attachBaseContext(LocaleUtils.updateResources(base, languageCode))
    }
}
