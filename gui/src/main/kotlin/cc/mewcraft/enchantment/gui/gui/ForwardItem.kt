package cc.mewcraft.enchantment.gui.gui

import cc.mewcraft.enchantment.gui.config.EnchantGuiSettings
import cc.mewcraft.enchantment.gui.util.translatable
import cc.mewcraft.enchantment.gui.util.wrapper
import org.bukkit.Material
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.builder.ItemBuilder
import javax.inject.Inject

class ForwardItem
@Inject constructor(
    settings: EnchantGuiSettings,
) : AnimatedPageItem(settings, true) {
    override fun getItemProvider(gui: PagedGui<*>): ItemProvider =
        ItemBuilder(Material.SPECTRAL_ARROW).apply {
            displayName = "menu.enchantment.next_page".translatable().wrapper()
        }
}
