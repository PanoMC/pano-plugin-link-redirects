package com.panomc.plugins.linkredirect.event

import com.panomc.platform.api.annotation.EventListener
import com.panomc.platform.api.event.SetupEventListener
import com.panomc.plugins.linkredirect.LinkRedirectPlugin

@EventListener
class SetupEventHandler(private val plugin: LinkRedirectPlugin) : SetupEventListener {
    private val logger by lazy {
        plugin.logger
    }

    override suspend fun onSetupFinished() {
        // isRunning, not plugin.pluginState: older Pano versions keep pluginState at its creation value.
        if (plugin.isRunning) {
            logger.info("Setup finished! Initializing plugin...")

            plugin.startPlugin()
        }
    }
}
