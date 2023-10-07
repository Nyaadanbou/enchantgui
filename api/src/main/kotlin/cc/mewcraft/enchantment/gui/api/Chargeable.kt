package cc.mewcraft.enchantment.gui.api

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack

/**
 * A UiEnchant that is chargeable.
 */

private val MINI_MESSAGE_STRICT = MiniMessage.builder().strict(true).build() // make it strict to generate closed tags

class Chargeable(
    base: UiEnchant,
    fuelItem: ItemStack, // it's the name of fuel item in MiniMessage string representation
    fuelConsumeMapping: (Int) -> Int,
    fuelRechargeMapping: (Int) -> Int,
    maximumFuelMapping: (Int) -> Int,
) : UiEnchant by base {
    val fuel: String = fuelItem.let {
        val itemMeta = it.itemMeta
        val component = if (itemMeta?.hasDisplayName() == true) itemMeta.displayName() else Component.translatable(fuelItem)
        component?.let(MINI_MESSAGE_STRICT::serialize) ?: ""
    }
    val fuelConsume: Map<Int, Int> = levelScale(fuelConsumeMapping)
    val fuelRecharge: Map<Int, Int> = levelScale(fuelRechargeMapping)
    val maximumFuel: Map<Int, Int> = levelScale(maximumFuelMapping)
}
