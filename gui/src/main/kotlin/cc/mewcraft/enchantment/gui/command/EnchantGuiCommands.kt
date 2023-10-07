package cc.mewcraft.enchantment.gui.command

import cc.mewcraft.enchantment.gui.api.UiEnchantPlugin
import cc.mewcraft.enchantment.gui.gui.EnchantMenu
import cc.mewcraft.spatula.command.SimpleCommands
import cloud.commandframework.bukkit.parsers.PlayerArgument
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnchantGuiCommands
@Inject constructor(
    plugin: UiEnchantPlugin,
) : SimpleCommands<UiEnchantPlugin>(plugin) {
    override fun prepareAndRegister() {
        // Prepare commands
        registry.prepareCommand(
            registry
                .commandBuilder("enchantgui")
                .literal("open")
                .permission("enchantgui.command.open")
                .argument(PlayerArgument.optional("target"))
                .handler {

                    val viewer: Player = if (it.contains("target")) {
                        it.get("target")
                    } else if (it.sender is Player) {
                        it.sender as Player
                    } else return@handler

                    plugin.injector.getInstance(EnchantMenu::class.java).showMenu(viewer)

                }.build()
        )
        registry.prepareCommand(
            registry
                .commandBuilder("enchantgui")
                .literal("reload")
                .permission("enchantgui.command.reload")
                .handler {

                    plugin.onDisable()
                    plugin.onEnable()

                }.build()
        )

        // Register commands
        registry.registerCommands()
    }
}
