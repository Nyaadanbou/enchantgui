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
import java.util.*
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
            val locale = Locale.SIMPLIFIED_CHINESE // FIXME pass the player's locale to here
            val min = enchant.minLevel()
            val max = enchant.maxLevel()
            val states = ArrayList<ItemProvider>(max)

            for (level in (min..max)) {
                ////// build displayName //////

                val displayName = pluginSettings.DISPLAY_NAME_FORMAT.translatable.arguments(enchant.displayName(level))

                ////// build lore //////

                val loreBuilder = I18nLore.create()
                loreBuilder.append(pluginSettings.LORE_FORMAT)
                loreBuilder.argumentsMany("menu.enchant.icon.description", enchant.description(level))
                loreBuilder.arguments("menu.enchant.icon.rarity", enchant.rarity().name)
                loreBuilder.arguments("menu.enchant.icon.target") {
                    enchant.targets().map {
                        val key = "menu.enchant.icon.target." + it.name.lowercase()
                        Component.translatable(key)
                    }.let { Component.join(JoinConfiguration.commas(true), it) }
                }
                loreBuilder.arguments("menu.enchant.icon.level", TranslationArgument.numeric(enchant.minLevel()), TranslationArgument.numeric(enchant.maxLevel()))
                loreBuilder.argumentsMany("menu.enchant.icon.conflict.item") {
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
                    loreBuilder.arguments("menu.enchant.icon.charging.fuel", enchant.fuelName)
                    loreBuilder.arguments("menu.enchant.icon.conflict.consume_amount", TranslationArgument.numeric(enchant.fuelConsume(level)))
                    loreBuilder.arguments("menu.enchant.icon.conflict.recharge_amount", TranslationArgument.numeric(enchant.fuelRecharge(level)))
                    loreBuilder.arguments("menu.enchant.icon.conflict.max_amount", TranslationArgument.numeric(enchant.maximumFuel(level)))
                }
                loreBuilder.arguments("menu.enchant.icon.obtaining.enchanting", TranslationArgument.numeric(enchant.enchantingChance()))
                loreBuilder.arguments("menu.enchant.icon.obtaining.villager", TranslationArgument.numeric(enchant.villagerTradeChance()))
                loreBuilder.arguments("menu.enchant.icon.obtaining.loot_generation", TranslationArgument.numeric(enchant.lootGenerationChance()))
                loreBuilder.arguments("menu.enchant.icon.obtaining.fishing", TranslationArgument.numeric(enchant.fishingChance()))
                loreBuilder.arguments("menu.enchant.icon.obtaining.mob_spawning", TranslationArgument.numeric(enchant.mobSpawningChance()))

                loreBuilder.sanitize("conflict") { enchant.conflicts().isEmpty() }
                loreBuilder.sanitize("charging") { enchant !is Chargeable }
                loreBuilder.sanitize("obtaining.enchanting") { enchant.enchantingChance() <= 0 }
                loreBuilder.sanitize("obtaining.villager") { enchant.villagerTradeChance() <= 0 }
                loreBuilder.sanitize("obtaining.loot_generation") { enchant.lootGenerationChance() <= 0 }
                loreBuilder.sanitize("obtaining.fishing") { enchant.fishingChance() <= 0 }
                loreBuilder.sanitize("obtaining.mob_spawning") { enchant.mobSpawningChance() <= 0 }

                val lore = loreBuilder.build()

                ////// build item //////

                val builder = ItemBuilder(pluginSettings.ITEM_MATERIAL).apply {
                    setDisplayName(displayName.wrapper)
                    setLore(lore.wrapper)
                }

                // the letter case of the `locale.toString()`
                // must be exactly the same as file name!
                states += ItemWrapper(builder.get(locale.toString().lowercase()))
            }

            return states.toTypedArray()
        }
    }
}
