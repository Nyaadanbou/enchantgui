package cc.mewcraft.enchantment.gui.gui

import cc.mewcraft.enchantment.gui.api.Chargeable
import cc.mewcraft.enchantment.gui.api.UiEnchant
import cc.mewcraft.enchantment.gui.config.EnchantGuiSettings
import cc.mewcraft.enchantment.gui.util.Lores
import cc.mewcraft.enchantment.gui.util.Numbers
import cc.mewcraft.enchantment.gui.util.miniMessage
import cc.mewcraft.enchantment.gui.util.wrapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.kyori.adventure.text.Component
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.builder.ItemBuilder
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides a function [get] which takes [UiEnchant] as key and
 * returns an array of [ItemProvider]. The returned [ItemProvider] then can
 * be used to construct the content of [EnchantMenu] for display purposes.
 */
@Singleton
class EnchantIcons
@Inject constructor(
    private val settings: EnchantGuiSettings,
    private val targetTranslator: TargetTranslator,
) {
    private val itemProviderCache: LoadingCache<UiEnchant, Array<ItemProvider>> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofHours(2))
        .build(ItemProviderCacheLoader())

    operator fun get(key: UiEnchant): Array<ItemProvider> =
        itemProviderCache.getUnchecked(key)

    private inner class ItemProviderCacheLoader : CacheLoader<UiEnchant, Array<ItemProvider>>() {
        override fun load(enchant: UiEnchant): Array<ItemProvider> {
            val min = enchant.minimumLevel()
            val max = enchant.maximumLevel()
            val states = ArrayList<ItemProvider>(max)

            for (level in (min..max)) {
                // Prepare all the values needed to create the enchantment icon
                val displayName = enchant.displayName()[level]?.let { settings.DISPLAY_NAME_FORMAT.replace("<enchantment_display_name>", it) } ?: NULL
                val description = enchant.description()[level]?.toMutableList() ?: emptyList()
                val rarity = enchant.rarity().name
                val targets = enchant.enchantmentTargets()
                    .map { targetTranslator.translate(it) }
                    .reduce { t1, t2 -> "$t1, $t2" }
                val minLevel = enchant.minimumLevel().toString()
                val maxLevel = enchant.maximumLevel().toString()

                val loreFormat = settings.LORE_FORMAT.toMutableList() // This list will be modified multiple times below

                // Make the lore that is common to all enchantments
                Lores.replacePlaceholder("<enchantment_description>", loreFormat, description)
                Lores.replacePlaceholder("<enchantment_rarity>", loreFormat, rarity)
                Lores.replacePlaceholder("<enchantment_target_list>", loreFormat, targets)
                Lores.replacePlaceholder("<enchantment_level_min>", loreFormat, minLevel)
                Lores.replacePlaceholder("<enchantment_level_max>", loreFormat, maxLevel)

                // Make the lore that describes conflict
                if (enchant.conflict().isNotEmpty()) {
                    val conflict = enchant.conflict()
                        .chunked(3)
                        .map { chunk -> chunk.map { it.name() }.reduce { e1, e2 -> "$e1, $e2" } }
                    Lores.replacePlaceholder("<conflict>", loreFormat, settings.LORE_FORMAT_CONFLICT)
                    Lores.replacePlaceholder("<enchantment_conflict_list>", loreFormat, conflict)
                } else {
                    Lores.removePlaceholder("<conflict>", loreFormat, keep = false)
                }

                // Make the lore that describes charging
                if (enchant is Chargeable) {
                    Lores.replacePlaceholder("<charging>", loreFormat, settings.LORE_FORMAT_CHARGING)
                    Lores.replacePlaceholder("<enchantment_charges_fuel_item>", loreFormat, enchant.fuel)
                    Lores.replacePlaceholder("<enchantment_charges_consume_amount>", loreFormat, enchant.fuelConsume[level]?.let { Numbers.format(it.toDouble()) } ?: NULL)
                    Lores.replacePlaceholder("<enchantment_charges_recharge_amount>", loreFormat, enchant.fuelRecharge[level]?.let { Numbers.format(it.toDouble()) } ?: NULL)
                    Lores.replacePlaceholder("<enchantment_charges_max_amount>", loreFormat, enchant.maximumFuel[level]?.let { Numbers.format(it.toDouble()) } ?: NULL)
                } else {
                    Lores.removePlaceholder("<charging>", loreFormat, keep = false)
                }

                // Make the lore that describes how to obtain it
                if (loreFormat.contains("<obtaining>")) {
                    Lores.replacePlaceholder("<obtaining>", loreFormat, settings.LORE_FORMAT_OBTAINING)
                    replaceOrRemove("<enchantment_obtain_chance_enchanting>", loreFormat, enchant.enchantingChance())
                    replaceOrRemove("<enchantment_obtain_chance_villager>", loreFormat, enchant.villagerTradeChance())
                    replaceOrRemove("<enchantment_obtain_chance_loot_generation>", loreFormat, enchant.lootGenerationChance())
                    replaceOrRemove("<enchantment_obtain_chance_fishing>", loreFormat, enchant.fishingChance())
                    replaceOrRemove("<enchantment_obtain_chance_mob_spawning>", loreFormat, enchant.mobSpawningChance())
                }

                val builder = ItemBuilder(settings.ITEM_MATERIAL).apply {
                    this.setDisplayName(displayName.miniMessage().wrapper())
                    this.setLore(loreFormat.miniMessage().wrapper())
                }

                states += ItemWrapper(builder.get())
            }

            return states.toTypedArray()
        }
    }

    fun poc() {
        // 先读取模板
        // 把所有的 string 作为 translation key 转换成 Translatable（注意这里假设每一行就是一个 key）
        // 其中，string 作为 map key，构建的 Translatable 作为 map value
        // 然后将所有的数据放入一个 map

        // 遍历 map，根据可用的数据，为每个 Translatable 添加 args
        // 注意这里需要替换原本的 map value，因为所有 Component 都是 immutable

        // 构建物品的时候，根据物品的模板，将模板
    }
}

private const val NULL: String = "NULL"

private fun replaceOrRemove(placeholder: String, dst: MutableList<String>, chance: Double) =
    if (chance > 0) {
        Lores.replacePlaceholder(placeholder, dst, Numbers.format(chance))
    } else {
        Lores.removePlaceholder(placeholder, dst, keep = false)
    }
