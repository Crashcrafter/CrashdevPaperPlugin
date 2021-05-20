package de.rlg.commands.admin

import de.rlg.*
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
            if (args[0].contentEquals("set")) {
                if (args.size == 1) {
                    val type: Int = getDropType()
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
            } else if (args[0].contentEquals("unset")) {
                unsetDrop(player.location.chunk, true)
            } else if (args[0].contentEquals("enable")) {
                canDropStart = true
                Bukkit.getServer().sendMessage(Component.text("§2Die Drops sind wieder aktiviert!"))
            } else if (args[0].contentEquals("disable")) {
                canDropStart = false
                Bukkit.getServer().sendMessage(Component.text("§5Die Drops können gerade nicht gestartet werden, es kommt ein Reload!"))
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
                return mutableListOf("set", "unset", "enable", "disable")
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