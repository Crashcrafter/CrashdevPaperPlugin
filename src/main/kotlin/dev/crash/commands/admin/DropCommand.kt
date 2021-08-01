package dev.crash.commands.admin

import dev.crash.*
import dev.crash.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class DropCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (args.isNotEmpty() && player.hasPermission("rlg.drop")) {
            when(args[0]){
                "set" -> {
                    if (args.size == 1) {
                        val type: Int = getDropType(player.world.name)
                        setDrop(player.location.chunk, type)
                    } else {
                        var type = -1
                        when (args[1]) {
                            "common" -> type = 0
                            "uncommon" -> type = 1
                            "rare" -> type = 2
                            "epic" -> type = 3
                            "supreme" -> type = 4
                        }
                        setDrop(player.location.chunk, type)
                    }
                }
                "unset" -> unsetDrop(player.location.chunk, true)
                "enable" -> {
                    canDropStart = true
                    Bukkit.getServer().sendMessage(Component.text("§2Die Drops sind wieder aktiviert!"))
                }
                "disable" -> {
                    canDropStart = false
                    Bukkit.getServer().sendMessage(Component.text("§5Die Drops können gerade nicht gestartet werden, es kommt ein Reload!"))
                }
                "resetdropcooldown" -> {
                    player.rlgPlayer().dropCoolDown = System.currentTimeMillis()
                    player.sendMessage("§2Drop Cooldown resetted!")
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (sender.isOp) {
            val list: MutableList<String> = ArrayList()
            if (args.size == 1) {
                return mutableListOf("set", "unset", "enable", "disable", "resetdropcooldown")
            } else if (args.size == 2) {
                if (args[0].contentEquals("set")) {
                    return mutableListOf("common", "uncommon", "rare", "epic", "supreme")
                }
            }
            return list
        }
        return null
    }
}