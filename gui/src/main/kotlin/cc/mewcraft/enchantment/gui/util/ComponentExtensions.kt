package cc.mewcraft.enchantment.gui.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper

internal fun String?.miniMessage(): Component =
    if (this == null) Component.empty() else MiniMessage.miniMessage().deserialize(this)

internal fun List<String>?.miniMessage(): List<Component> =
    this?.map(String::miniMessage) ?: emptyList()

internal fun String?.translatable(): Component =
    if (this == null) Component.empty() else Component.translatable(this)

internal fun List<String>?.translatable(): List<Component> =
    this?.map(String::translatable) ?: emptyList()

internal fun Component?.wrapper(): ComponentWrapper =
    AdventureComponentWrapper(this ?: Component.empty())

internal fun List<Component>?.wrapper(): List<ComponentWrapper> =
    this?.map(Component::wrapper) ?: emptyList()
