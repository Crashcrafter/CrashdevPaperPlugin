package dev.crash.commands.admin

import dev.crash.*
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class DropCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (!player.hasPermission("crash.drop")) return true
        when(args[0]){
            "set" -> {
                if (args.size == 1) {
                    val type: Int = getDropType(player.world.name)
                    setDrop(player.location.chunk, type)
                } else {
                    var type = -1
                    dropTypeMap.values.forEach {
                        if(it.name == args[1]){
                            type = it.type
                        }
                    }
                    setDrop(player.location.chunk, type)
                }
            }
            "unset" -> unsetDrop(player.location.chunk, true)
            "enable" -> {
                canDropStart = true
                Bukkit.getServer().sendMessage(Component.text("§2The drops are enabled again!"))
            }
            "disable" -> {
                canDropStart = false
                Bukkit.getServer().sendMessage(Component.text("§5The drops are disabled!"))
            }
            "resetdropcooldown" -> {
                player.crashPlayer().dropCoolDown = System.currentTimeMillis()
                player.sendMessage("§2Drop-cooldown resetted!")
        }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        if (args.size == 1) {
            return mutableListOf("set", "unset", "enable", "disable", "resetdropcooldown")
        } else if (args.size == 2) {
            if (args[0].contentEquals("set")) {
                return dropTypeMap.values.map { it.name }.toMutableList()
            }
        }
        return list
    }
}