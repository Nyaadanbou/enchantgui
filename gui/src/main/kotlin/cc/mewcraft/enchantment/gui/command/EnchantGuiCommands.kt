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
@Inject
constructor(
    private val plugin: UiEnchantPlugin,
) : SimpleCommands(plugin) {
    override fun registerCommands() {
        // Prepare commands
        commandRegistry().addCommand(
            commandRegistry()
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

                    // TODO reuse menu instances for the same player
                    plugin.injector.getInstance(EnchantMenu::class.java).showMenu(viewer)

                }.build()
        )
        commandRegistry().addCommand(
            commandRegistry()
                .commandBuilder("enchantgui")
                .literal("reload")
                .permission("enchantgui.command.reload")
                .handler {

                    plugin.onDisable()
                    plugin.onEnable()

                }.build()
        )

        // Register commands
        commandRegistry().registerCommands()
    }
}
