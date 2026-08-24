package com.nightcallaudio

import android.app.Application
import com.nightcallaudio.di.AppContainer

class NightcallAudioApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onTerminate() {
        container.playbackRepository.close()
        container.database.close()
        super.onTerminate()
    }
}
