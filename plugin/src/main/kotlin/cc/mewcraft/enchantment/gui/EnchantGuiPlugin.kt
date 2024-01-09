package cc.mewcraft.enchantment.gui

import cc.mewcraft.enchantment.gui.api.UiEnchantPlugin
import cc.mewcraft.enchantment.gui.api.UiEnchantProvider
import cc.mewcraft.enchantment.gui.command.EnchantGuiCommands
import cc.mewcraft.spatula.message.Translations
import com.google.inject.AbstractModule
import com.google.inject.Guice
import net.kyori.adventure.text.minimessage.MiniMessage
import xyz.xenondevs.inventoryaccess.component.i18n.AdventureComponentLocalizer
import xyz.xenondevs.inventoryaccess.component.i18n.Languages
import java.io.IOException
import javax.inject.Singleton

class EnchantGuiPlugin : UiEnchantPlugin() {
    inner class PluginModule : AbstractModule() {
        override fun configure() {
            bind(UiEnchantPlugin::class.java).toInstance(this@EnchantGuiPlugin)
            bind(EnchantGuiPlugin::class.java).toInstance(this@EnchantGuiPlugin)
            bind(Translations::class.java).toProvider {
                Translations(this@EnchantGuiPlugin, "lang/message")
            }.`in`(Singleton::class.java)
        }
    }

    override fun enable() {
        saveResourceRecursively("lang")
        saveDefaultConfig()
        reloadConfig()

        injector = Guice.createInjector(PluginModule())

        // Load message languages
        languages = injector.getInstance(Translations::class.java)

        // Load modding languages
        try {
            Languages.getInstance().loadLanguage(
                "zh_cn",
                getBundledFile("lang/modding/zh_cn.json"),
                Charsets.UTF_8
            )
        } catch (e: IOException) {
            slF4JLogger.error("Failed to load language files", e)
        }

        // Set MiniMessage parser
        AdventureComponentLocalizer.getInstance().setComponentCreator { MiniMessage.miniMessage().deserialize(it) }

        // Initialize commands
        try {
            injector.getInstance(EnchantGuiCommands::class.java).registerCommands()
        } catch (e: Exception) {
            slF4JLogger.error("Failed to initialize commands", e)
        }

        // Initialize UiEnchant providers
        try {
            UiEnchantProvider.initialize(this)
        } catch (e: Exception) { // catch all exceptions to avoid this plugin failing to be enabled
            slF4JLogger.error("Failed to initialize UiEnchantProvider", e)
        }
    }
}
