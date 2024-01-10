package cc.mewcraft.enchantment.gui.gui

import cc.mewcraft.enchantment.gui.api.Chargeable
import cc.mewcraft.enchantment.gui.api.UiEnchant
import cc.mewcraft.enchantment.gui.config.EnchantGuiSettings
import cc.mewcraft.enchantment.gui.util.wrapper
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import me.lucko.helper.text3.I18nLore
import me.lucko.helper.text3.Text
import me.lucko.helper.text3.arguments
import me.lucko.helper.text3.translatable
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
                ////// build displayName //////

                // val displayName = settings.DISPLAY_NAME_FORMAT.translatable.arguments(enchant.displayName()[level]!!)
                val displayName = settings.DISPLAY_NAME_FORMAT.translatable.arguments(enchant.displayName()[level]!!.let { Text.decolorize(it) }) // FIXME 更新适用于MC1.20.4版本的ExcellentEnchants

                ////// build lore //////

                val lore = I18nLore.create()
                lore.append(settings.LORE_FORMAT)
                lore.arguments("menu.enchantment.icon.description", enchant.description()[level]!!)
                // lore.arguments("menu.enchantment.icon.rarity", enchant.rarity().name)
                lore.arguments("menu.enchantment.icon.rarity", enchant.rarity().name.let { Text.decolorize(it) }) // FIXME 更新适用于MC1.20.4版本的ExcellentEnchants
                lore.arguments("menu.enchantment.icon.target", enchant.enchantmentTargets().map { targetTranslator.translate(it) }.reduceOrNull { t1, t2 -> "$t1, $t2" } ?: "")
                lore.arguments("menu.enchantment.icon.level", TranslationArgument.numeric(enchant.minimumLevel()), TranslationArgument.numeric(enchant.maximumLevel()))
                lore.argumentsMany("menu.enchantment.icon.conflict.item") {
                    enchant.conflict()
                        .chunked(3)
                        .map { chunk ->
                            chunk.map {
                                it.name()
                            }.reduceOrNull { e1, e2 ->
                                "$e1, $e2"
                            } ?: ""
                        }
                }
                if (enchant is Chargeable) {
                    lore.arguments("menu.enchantment.icon.charging.fuel", enchant.fuel)
                    lore.arguments("menu.enchantment.icon.conflict.consume_amount", TranslationArgument.numeric(enchant.fuelConsume[level]!!))
                    lore.arguments("menu.enchantment.icon.conflict.recharge_amount", TranslationArgument.numeric(enchant.fuelRecharge[level]!!))
                    lore.arguments("menu.enchantment.icon.conflict.max_amount", TranslationArgument.numeric(enchant.maximumFuel[level]!!))
                }
                lore.arguments("menu.enchantment.icon.obtaining.enchanting", TranslationArgument.numeric(enchant.enchantingChance()))
                lore.arguments("menu.enchantment.icon.obtaining.villager", TranslationArgument.numeric(enchant.villagerTradeChance()))
                lore.arguments("menu.enchantment.icon.obtaining.loot_generation", TranslationArgument.numeric(enchant.lootGenerationChance()))
                lore.arguments("menu.enchantment.icon.obtaining.fishing", TranslationArgument.numeric(enchant.fishingChance()))
                lore.arguments("menu.enchantment.icon.obtaining.mob_spawning", TranslationArgument.numeric(enchant.mobSpawningChance()))

                lore.sanitize("conflict") { enchant.conflict().isEmpty() }
                lore.sanitize("charging") { enchant !is Chargeable }
                lore.sanitize("obtaining.enchanting") { enchant.enchantingChance() <= 0 }
                lore.sanitize("obtaining.villager") { enchant.villagerTradeChance() <= 0 }
                lore.sanitize("obtaining.loot_generation") { enchant.lootGenerationChance() <= 0 }
                lore.sanitize("obtaining.fishing") { enchant.fishingChance() <= 0 }
                lore.sanitize("obtaining.mob_spawning") { enchant.mobSpawningChance() <= 0 }

                ////// build item //////

                val builder = ItemBuilder(settings.ITEM_MATERIAL).apply {
                    this.setDisplayName(displayName.wrapper)
                    this.setLore(lore.build().wrapper)
                }

                states += ItemWrapper(builder.get("zh_cn")) // FIXME pass the player's locale to here
            }

            return states.toTypedArray()
        }
    }
}
