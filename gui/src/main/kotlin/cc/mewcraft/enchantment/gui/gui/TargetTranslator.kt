package cc.mewcraft.enchantment.gui.gui

import cc.mewcraft.enchantment.gui.api.UiEnchantPlugin
import cc.mewcraft.enchantment.gui.api.UiEnchantTarget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TargetTranslator
@Inject constructor(
    private val plugin: UiEnchantPlugin,
) {
    fun translate(target: UiEnchantTarget): String =
        plugin.languages.of("item_target_${target.name.lowercase()}").plain()
}
