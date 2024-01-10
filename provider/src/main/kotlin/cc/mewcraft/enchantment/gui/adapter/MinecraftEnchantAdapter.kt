package cc.mewcraft.enchantment.gui.adapter

import cc.mewcraft.enchantment.gui.api.UiEnchant
import cc.mewcraft.enchantment.gui.api.UiEnchantAdapter
import cc.mewcraft.enchantment.gui.api.UiEnchantTarget
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget

import javax.inject.Singleton

// TODO implement vanilla enchantments into the menu

@Singleton
class MinecraftEnchantAdapter : UiEnchantAdapter<Enchantment, EnchantmentTarget> {
    override fun initialize() {}

    override fun canInitialize(): Boolean = true // vanilla enchantments are always available

    override fun adaptEnchant(providedEnchant: Enchantment): UiEnchant = TODO()

    override fun adaptTarget(providedTarget: EnchantmentTarget): UiEnchantTarget = TODO()
}
