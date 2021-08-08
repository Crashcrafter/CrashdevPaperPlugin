package dev.crash.commands.mod

import dev.crash.*
import dev.crash.player.Warn
import dev.crash.player.modifyPlayerData
import dev.crash.player.crashPlayer
import me.kbrewster.mojangapi.MojangAPI
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WarnCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val target: Player? = Bukkit.getPlayer(args[1])
        if (!player.hasPermission("crash.warn")) return true
        when(args[0]){
            "add" -> {
                val sb = StringBuilder()
                for (i in 2 until args.size) {
                    sb.append(args[i]).append(" ")
                }
                val reason = sb.toString()
                player.sendMessage("The player ${args[1]} was warned for $reason!")
                if (target != null) {
                    target.sendMessage("You've been warned for $reason")
                    target.crashPlayer().warn(sb.toString(), player.name)
                }else {
                    modifyPlayerData(MojangAPI.getUUID(args[1]).toString()){
                        it.warns.add(Warn(reason, player.name, System.currentTimeMillis()))
                        it
                    }
                }
            }
            "list" -> {
                val list: String = target!!.crashPlayer().getWarnsString()
                player.sendMessage("\n${target.name}'s warns:\n$list\n")
            }
            "remove" -> {
                if (args[2] == "all") {
                    if(target == null) {
                        player.sendMessage("§4Player not found!")
                        return true
                    }
                    target.crashPlayer().removeAllWarns()
                    player.sendMessage("§2All warns have been removed from ${target.name}")
                } else {
                    val number: Int = args[2].toInt()
                    target!!.crashPlayer().removeWarn(number)
                    player.sendMessage("§2Warn $number was removed")
                }
            }
            else -> return true
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        if (args.size == 1) {
            val list: MutableList<String> = ArrayList()
            list.add("add")
            list.add("remove")
            list.add("list")
            return list
        } else if (args.size == 2) {
            val list: MutableList<String> = ArrayList()
            val currentString: String = args[1]
            for (player1 in Bukkit.getOnlinePlayers()) {
                if (player1.name.startsWith(currentString)) {
                    list.add(player1.name)
                }
            }
            return list
        } else if (args.size >= 3) {
            return when(args[0]){
                "add" -> mutableListOf("<Reason>")
                "remove" -> mutableListOf("all", "<amount>")
                else -> null
            }
        }
        return null
    }
}