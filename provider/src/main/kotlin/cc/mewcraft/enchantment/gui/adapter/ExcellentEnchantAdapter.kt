package cc.mewcraft.enchantment.gui.adapter

import cc.mewcraft.enchantment.gui.api.*
import me.lucko.helper.text3.Text
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory
import su.nightexpress.excellentenchants.api.enchantment.ObtainType
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry
import javax.inject.Singleton

@Singleton
class ExcellentEnchantAdapter : UiEnchantAdapter<ExcellentEnchant, ItemCategory> {
    override fun initialize() {
        if (!canInitialize()) {
            return
        }
        for (enchant in EnchantRegistry.getRegistered()) {
            UiEnchantProvider.register(adaptEnchant(enchant))
        }
    }

    override fun canInitialize(): Boolean {
        return Bukkit.getPluginManager().getPlugin("ExcellentEnchants") != null
    }

    override fun adaptEnchant(providedEnchant: ExcellentEnchant): UiEnchant {
        val enchant = object : UiEnchant {
            override fun displayName(): Component {
                return Text.fromLegacySection(providedEnchant.displayName) // FIXME 更新适用于 MC1.20.4 版本的 ExcellentEnchants
            }

            override fun displayName(level: Int): Component {
                return Text.fromLegacySection(providedEnchant.getNameFormatted(level)) // FIXME 更新适用于 MC1.20.4 版本的 ExcellentEnchants
            }

            override fun description(level: Int): List<Component> {
                return providedEnchant.getDescription(level).map { Text.fromLegacySection(it) } // FIXME 更新适用于 MC1.20.4 版本的 ExcellentEnchants
            }

            override fun applicable(item: ItemStack): Boolean {
                return providedEnchant.checkEnchantCategory(item)
            }

            override fun targets(): List<UiEnchantTarget> {
                return providedEnchant.fitItemTypes.map { adaptTarget(it) }
            }

            override fun rarity(): UiEnchantRarity {
                return UiEnchantRarity(Text.fromLegacySection(providedEnchant.tier.name), NamedTextColor.AQUA) /*FIXME 更新适用于 MC1.20.4 版本的 ExcellentEnchants*/
            }

            override fun enchantingChance(): Double {
                return providedEnchant.getObtainChance(ObtainType.ENCHANTING)
            }

            override fun villagerTradeChance(): Double {
                return providedEnchant.getObtainChance(ObtainType.VILLAGER)
            }

            override fun lootGenerationChance(): Double {
                return providedEnchant.getObtainChance(ObtainType.LOOT_GENERATION)
            }

            override fun fishingChance(): Double {
                return providedEnchant.getObtainChance(ObtainType.FISHING)
            }

            override fun mobSpawningChance(): Double {
                return providedEnchant.getObtainChance(ObtainType.MOB_SPAWNING)
            }

            override fun conflicts(): List<UiEnchant> {
                return providedEnchant.conflicts.mapNotNull { UiEnchantProvider[it] }
            }

            override fun conflictsWith(other: Enchantment): Boolean {
                return providedEnchant.conflicts.contains(other.key().value())
            }

            override fun minLevel(): Int {
                return providedEnchant.startLevel
            }

            override fun maxLevel(): Int {
                return providedEnchant.maxLevel
            }

            override fun key(): Key {
                return providedEnchant.key()
            }
        }

        return when {
            providedEnchant.isChargesEnabled -> Chargeable(
                enchant,
                providedEnchant.chargesFuel,
                providedEnchant::getChargesConsumeAmount,
                providedEnchant::getChargesRechargeAmount,
                providedEnchant::getChargesMax,
            )

            else -> enchant
        }
    }

    override fun adaptTarget(providedTarget: ItemCategory): UiEnchantTarget {
        return when (providedTarget) {
            ItemCategory.HELMET -> UiEnchantTarget.HELMET
            ItemCategory.CHESTPLATE -> UiEnchantTarget.CHESTPLATE
            ItemCategory.LEGGINGS -> UiEnchantTarget.LEGGINGS
            ItemCategory.BOOTS -> UiEnchantTarget.BOOTS
            ItemCategory.ELYTRA -> UiEnchantTarget.ELYTRA
            ItemCategory.TOOL -> UiEnchantTarget.TOOL
            ItemCategory.SWORD -> UiEnchantTarget.SWORD
            ItemCategory.TRIDENT -> UiEnchantTarget.TRIDENT
            ItemCategory.AXE -> UiEnchantTarget.AXE
            ItemCategory.BOW -> UiEnchantTarget.BOW
            ItemCategory.CROSSBOW -> UiEnchantTarget.CROSSBOW
            ItemCategory.HOE -> UiEnchantTarget.HOE
            ItemCategory.PICKAXE -> UiEnchantTarget.PICKAXE
            ItemCategory.SHOVEL -> UiEnchantTarget.SHOVEL
            ItemCategory.FISHING_ROD -> UiEnchantTarget.FISHING_ROD
        }
    }
}
