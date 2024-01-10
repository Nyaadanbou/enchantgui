package cc.mewcraft.enchantment.gui.api

import com.google.inject.Injector
import me.lucko.helper.plugin.ExtendedJavaPlugin

abstract class UiEnchantPlugin : ExtendedJavaPlugin() {
    lateinit var injector: Injector // TODO migrate DI framework to Koin
}
