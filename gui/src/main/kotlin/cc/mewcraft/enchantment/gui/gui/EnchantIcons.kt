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
import net.kyori.adventure.text.TranslatableComponent
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

    ///////////////////////////////////////////////
    ////////////////// i18n PoC ///////////////////
    ///////////////////////////////////////////////

    // TODO 应该封装成一个 class 供其他项目复用
    // FIXME 下面是新的 i18n 实现，没测 runtime，仅供参考

    private fun poc(enchant: UiEnchant, level: Int) {
        // 给定如下参数:
        // - UiEnchant (创建 TranslationArgument 所需要的任意对象)
        // - LoreFormat (所有会用到的 translation key 以及 lore 的 format)
        // 我们的任务是根据 LoreFormat 和 UiEnchant
        // 生成一个 List<TranslatableComponent>
        // 用于设置物品的 lore

        /*
        第一步：读取所有的 TranslatableComponent

        先读取配置文件里的物品模板
        把所有的 string 看成是 translation key，然后将它们转换成 TranslatableComponent（注意这里假设每一行有且仅有一个 key）
        其中，string 将作为 map key；由其构建的 TranslatableComponent 将作为 map value
        然后将所有的数据放入一个 map (接下来叫这个 map 为 “registry”)
        */

        val registry = HashMap<String, TranslatableComponent>() // FIXME 如果这里用 ListMultimap，那就写个扩展函数
        registry.put(settings.DISPLAY_NAME_FORMAT)
        registry.put(settings.LORE_FORMAT)

        /*
        第二步：为部分 TranslatableComponent 添加 arguments

        到此为止，这个 registry 就有了终端用户定义的一些 TranslatableComponent
        但这些 TranslatableComponent 都还没有定义 arguments
        于是接下来，我们需要给支持 arguments 的那些 TranslatableComponent 添加 arguments
        至于那些不支持 arguments 的 TranslatableComponent - 要么是实现原本就不支持，要么是用户自己定义的新条目
        无论是哪种情况，都不需要给它们添加 arguments，也不需要移除它们，而是让 InvUI 自己读取 .json 中的条目就行了
        这样反倒可以让用户自行去自定义新的 i18n 条目
        */

        registry.computeIfPresent("menu.enchantment.icon.display_name") { _, v -> v.arguments(enchant.displayName()[level].miniMessage()) }
        // FIXME span multiple lines
        // registry.computeIfPresent("menu.enchantment.icon.description") { _, v -> v.arguments() }
        registry.computeIfPresent("menu.enchantment.icon.rarity") { _, v -> v.arguments(enchant.rarity().name.miniMessage()) }
        registry.computeIfPresent("menu.enchantment.icon.target") { _, v ->
            v.arguments(enchant.enchantmentTargets()
                .map { targetTranslator.translate(it) }
                .reduce { t1, t2 -> "$t1, $t2" }.miniMessage()
            )
        }
        registry.computeIfPresent("menu.enchantment.icon.level") { _, v ->
            v.arguments(
                TranslationArgument.numeric(enchant.minimumLevel()),
                TranslationArgument.numeric(enchant.maximumLevel())
            )
        }
        // FIXME span multiple lines
        // registry.computeIfPresent("menu.enchantment.icon.conflict.item") { _, v -> v.arguments() }
        if (enchant is Chargeable) {
            registry.computeIfPresent("menu.enchantment.icon.charging.fuel") { _, v -> v.arguments(enchant.fuel.miniMessage()) }
            registry.computeIfPresent("menu.enchantment.icon.conflict.consume_amount") { _, v -> v.arguments(TranslationArgument.numeric(enchant.fuelConsume[level]!!)) }
            registry.computeIfPresent("menu.enchantment.icon.conflict.recharge_amount") { _, v -> v.arguments(TranslationArgument.numeric(enchant.fuelRecharge[level]!!)) }
            registry.computeIfPresent("menu.enchantment.icon.conflict.max_amount") { _, v -> v.arguments(TranslationArgument.numeric(enchant.maximumFuel[level]!!)) }
        }
        registry.computeIfPresent("menu.enchantment.icon.obtaining.enchanting") { _, v -> v.arguments(TranslationArgument.numeric(enchant.enchantingChance())) }
        registry.computeIfPresent("menu.enchantment.icon.obtaining.villager") { _, v -> v.arguments(TranslationArgument.numeric(enchant.villagerTradeChance())) }
        registry.computeIfPresent("menu.enchantment.icon.obtaining.loot_generation") { _, v -> v.arguments(TranslationArgument.numeric(enchant.lootGenerationChance())) }
        registry.computeIfPresent("menu.enchantment.icon.obtaining.fishing") { _, v -> v.arguments(TranslationArgument.numeric(enchant.fishingChance())) }
        registry.computeIfPresent("menu.enchantment.icon.obtaining.mob_spawning") { _, v -> v.arguments(TranslationArgument.numeric(enchant.mobSpawningChance())) }

        /*
        第三步：清理 lore format，隐藏没必要呈现的内容
        */
        val sanitizedLoreFormat = sanitizeLoreFormat(enchant, settings.LORE_FORMAT)

        /*
        第四步：根据清理后的 lore format，生成对应的 List<TranslatableComponent>
        */
        val componentName = registry[settings.DISPLAY_NAME_FORMAT]!!
        val componentLore = sanitizedLoreFormat.map { registry[it]!! } // FIXME span multiple lines, we might need flatMap

        // 最后把 name 和 lore 应用到 ItemBuilder 上就行了
    }

    private fun HashMap<String, TranslatableComponent>.put(key: String) {
        put(key, Component.translatable(key))
    }

    private fun HashMap<String, TranslatableComponent>.put(keys: List<String>) {
        keys.forEach { key -> put(key, Component.translatable(key)) }
    }

    private fun sanitizeLoreFormat(enchant: UiEnchant, format: List<String>): MutableList<String> {
        val format1 = format.toMutableList()
        fun sanitize(pattern: String, predicate: () -> Boolean) {
            if (predicate.invoke()) {
                format1.removeIf { it.contains(pattern) }
            }
        }
        // 对于无法充能的附魔，隐藏充能相关的描述
        sanitize("charging") { enchant !is Chargeable }
        // 对于为0的出现概率，移除对应种类的描述
        sanitize("obtaining.enchanting") { enchant.enchantingChance() <= 0 }
        sanitize("obtaining.villager") { enchant.villagerTradeChance() <= 0 }
        sanitize("obtaining.loot_generation") { enchant.lootGenerationChance() <= 0 }
        sanitize("obtaining.fishing") { enchant.fishingChance() <= 0 }
        sanitize("obtaining.mob_spawning") { enchant.mobSpawningChance() <= 0 }
        return format1
    }

    ///////////////////////////////////////////////
    ////////////////// i18n PoC ///////////////////
    ///////////////////////////////////////////////
}

private const val NULL: String = "NULL"

private fun replaceOrRemove(placeholder: String, dst: MutableList<String>, chance: Double) =
    if (chance > 0) {
        Lores.replacePlaceholder(placeholder, dst, Numbers.format(chance))
    } else {
        Lores.removePlaceholder(placeholder, dst, keep = false)
    }
