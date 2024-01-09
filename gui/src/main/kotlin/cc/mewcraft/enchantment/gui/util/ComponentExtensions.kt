package cc.mewcraft.enchantment.gui.util

import net.kyori.adventure.text.Component
import xyz.xenondevs.inventoryaccess.component.ComponentWrapper

internal val Component?.wrapper: ComponentWrapper
    get() = AdventureComponentWrapper(this ?: Component.empty())

internal val List<Component>?.wrapper: List<ComponentWrapper>
    get() = this?.map(Component::wrapper) ?: emptyList()
