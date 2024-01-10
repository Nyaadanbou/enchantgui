package cc.mewcraft.enchantment.gui.api

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

/**
 * Represents an enchantment that can be displayed in the GUI.
 *
 * The methods are necessary to render all the enchantment information
 * (such as 'displayName', 'description' and 'conflicts') for the items in
 * the GUI.
 */
interface UiEnchant : Keyed {
    ////// Display //////

    fun displayName(): Component
    fun displayName(level: Int): Component
    fun description(level: Int): List<Component>

    ////// Target //////

    fun applicable(item: ItemStack): Boolean
    fun targets(): List<UiEnchantTarget>

    ////// Rarity //////

    fun rarity(): UiEnchantRarity

    ////// Obtaining //////

    fun enchantingChance(): Double
    fun villagerTradeChance(): Double
    fun lootGenerationChance(): Double
    fun fishingChance(): Double
    fun mobSpawningChance(): Double

    ////// Conflicts //////

    /**
     * Returns a list of non-null enchants that conflict with this enchant.
     *
     * The enchants that exists in the configuration of backed enchant plugin
     * **but not** exists in [UiEnchantProvider] will be excluded from the
     * returned list.
     *
     * @return a list of non-null enchants that conflict with this enchant
     */
    fun conflicts(): List<UiEnchant>
    fun conflictsWith(other: Enchantment): Boolean

    ////// Min/Max enchantment levels //////

    fun minLevel(): Int
    fun maxLevel(): Int

    /**
     * Creates mappings to transform certain level to corresponding value.
     *
     * @param func a mapper transforming certain level to corresponding value
     * @param <T> the value type
     * @return a map containing all values of each level </T>
     */
    @Deprecated("not used anymore")
    fun <T> levelScale(func: (Int) -> T): Map<Int, T> {
        val mappings = Int2ObjectArrayMap<T>()
        for (level in minLevel()..maxLevel()) {
            mappings[level] = func.invoke(level)
        }
        return mappings
    }
}

/**
 * A "chargeable" enchantment.
 */
class Chargeable(
    enchant: UiEnchant,
    fuelItem: ItemStack,
    private val fuelConsumeMapping: (Int) -> Int,
    private val fuelRechargeMapping: (Int) -> Int,
    private val maximumFuelMapping: (Int) -> Int,
) : UiEnchant by enchant {

    val fuelName: Component = let {
        val itemMeta = fuelItem.itemMeta
        if (itemMeta?.hasDisplayName() == true) {
            itemMeta.displayName()!!
        } else {
            Component.translatable(fuelItem)
        }
    }

    fun fuelConsume(level: Int): Int = fuelConsumeMapping(level)
    fun fuelRecharge(level: Int): Int = fuelRechargeMapping(level)
    fun maximumFuel(level: Int): Int = maximumFuelMapping(level)
}
