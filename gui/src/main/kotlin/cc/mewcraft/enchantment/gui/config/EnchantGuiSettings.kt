@file:Suppress("PropertyName")

package cc.mewcraft.enchantment.gui.config

import cc.mewcraft.enchantment.gui.api.UiEnchantPlugin
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnchantGuiSettings
@Inject
constructor(
    plugin: UiEnchantPlugin,
) {
    val GUI_LAYOUT: Array<String> = plugin.config.getStringList("gui.layout").toTypedArray()
    val ITEM_MATERIAL: Material = Material.matchMaterial(plugin.config.getString("gui.icon.material")!!)!!
    val DISPLAY_NAME_FORMAT: String = plugin.config.getString("gui.icon.name")!!
    val LORE_FORMAT: List<String> = plugin.config.getStringList("gui.icon.lore")

    val SOUND_OPEN: Sound = createSound(plugin.config.getString("sound.open"))
    val SOUND_SWITCH: Sound = createSound(plugin.config.getString("sound.switch"))
    val SOUND_PAGE_TURN: Sound = createSound(plugin.config.getString("sound.page_turn"))
    val SOUND_TEST: Sound = createSound(plugin.config.getString("sound.test"))
}

private fun createSound(key: String?): Sound = Sound.sound(Key.key(key!!), Sound.Source.MASTER, 1f, 1f)
