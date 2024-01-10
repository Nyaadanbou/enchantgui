package cc.mewcraft.enchantment.gui.gui

import cc.mewcraft.enchantment.gui.api.Chargeable
import cc.mewcraft.enchantment.gui.api.UiEnchant
import cc.mewcraft.enchantment.gui.config.EnchantGuiSettings
import cc.mewcraft.enchantment.gui.util.wrapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import me.lucko.helper.text3.I18nLore
import me.lucko.helper.text3.translatable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.TranslationArgument
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
@Inject
constructor(
    private val pluginSettings: EnchantGuiSettings,
    private val targetTranslator: TargetTranslator,
) {
    private val itemProviderCache: LoadingCache<UiEnchant, Array<ItemProvider>> = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofHours(2))
        .build(ItemProviderCacheLoader())

    operator fun get(key: UiEnchant): Array<ItemProvider> =
        itemProviderCache.getUnchecked(key)

    private inner class ItemProviderCacheLoader : CacheLoader<UiEnchant, Array<ItemProvider>>() {

        // TODO create a new data class for key:
        //  data class(val UiEnchant, val Locale)

        override fun load(enchant: UiEnchant): Array<ItemProvider> {
            val min = enchant.minLevel()
            val max = enchant.maxLevel()
            val states = ArrayList<ItemProvider>(max)

            for (level in (min..max)) {
                ////// build displayName //////

                val displayName = pluginSettings.DISPLAY_NAME_FORMAT.translatable.arguments(enchant.displayName(level))

                ////// build lore //////

                val lore = I18nLore.create()
                lore.append(pluginSettings.LORE_FORMAT)
                lore.argumentsMany("menu.enchantment.icon.description", enchant.description(level))
                lore.arguments("menu.enchantment.icon.rarity", enchant.rarity().name)
                lore.arguments("menu.enchantment.icon.target", enchant.targets().map { targetTranslator.translate(it) }.reduceOrNull { t1, t2 -> "$t1, $t2" } ?: "")
                lore.arguments("menu.enchantment.icon.level", TranslationArgument.numeric(enchant.minLevel()), TranslationArgument.numeric(enchant.maxLevel()))
                lore.argumentsMany("menu.enchantment.icon.conflict.item") {
                    enchant.conflicts()
                        .chunked(3)
                        .map { chunk ->
                            Component.join(
                                JoinConfiguration.commas(true),
                                chunk.map { it.displayName() }
                            )
                        }
                }
                if (enchant is Chargeable) {
                    lore.arguments("menu.enchantment.icon.charging.fuel", enchant.fuelName)
                    lore.arguments("menu.enchantment.icon.conflict.consume_amount", TranslationArgument.numeric(enchant.fuelConsume(level)))
                    lore.arguments("menu.enchantment.icon.conflict.recharge_amount", TranslationArgument.numeric(enchant.fuelRecharge(level)))
                    lore.arguments("menu.enchantment.icon.conflict.max_amount", TranslationArgument.numeric(enchant.maximumFuel(level)))
                }
                lore.arguments("menu.enchantment.icon.obtaining.enchanting", TranslationArgument.numeric(enchant.enchantingChance()))
                lore.arguments("menu.enchantment.icon.obtaining.villager", TranslationArgument.numeric(enchant.villagerTradeChance()))
                lore.arguments("menu.enchantment.icon.obtaining.loot_generation", TranslationArgument.numeric(enchant.lootGenerationChance()))
                lore.arguments("menu.enchantment.icon.obtaining.fishing", TranslationArgument.numeric(enchant.fishingChance()))
                lore.arguments("menu.enchantment.icon.obtaining.mob_spawning", TranslationArgument.numeric(enchant.mobSpawningChance()))

                lore.sanitize("conflict") { enchant.conflicts().isEmpty() }
                lore.sanitize("charging") { enchant !is Chargeable }
                lore.sanitize("obtaining.enchanting") { enchant.enchantingChance() <= 0 }
                lore.sanitize("obtaining.villager") { enchant.villagerTradeChance() <= 0 }
                lore.sanitize("obtaining.loot_generation") { enchant.lootGenerationChance() <= 0 }
                lore.sanitize("obtaining.fishing") { enchant.fishingChance() <= 0 }
                lore.sanitize("obtaining.mob_spawning") { enchant.mobSpawningChance() <= 0 }

                ////// build item //////

                val builder = ItemBuilder(pluginSettings.ITEM_MATERIAL).apply {
                    this.setDisplayName(displayName.wrapper)
                    this.setLore(lore.build().wrapper)
                }

                states += ItemWrapper(builder.get("zh_cn")) // FIXME pass the player's locale to here
            }

            return states.toTypedArray()
        }
    }
}
