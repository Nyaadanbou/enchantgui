package cc.mewcraft.enchantment.gui.api

import cc.mewcraft.spatula.message.Translations
import com.google.inject.Injector
import me.lucko.helper.plugin.ExtendedJavaPlugin

abstract class UiEnchantPlugin : ExtendedJavaPlugin() {
    lateinit var languages: Translations
    lateinit var injector: Injector
}
