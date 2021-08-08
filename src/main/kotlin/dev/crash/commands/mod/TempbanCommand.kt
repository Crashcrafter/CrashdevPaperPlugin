package dev.crash.commands.mod

import dev.crash.asPlayer
import dev.crash.player.crashPlayer
import dev.crash.tempbanOnlineUser
import dev.crash.tempbanUser
import dev.crash.timeMultiplierFromString
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class TempbanCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.hasPermission("crash.tempban")) {
            val time: Int = args[1].toInt()
            val timeMultiplier = timeMultiplierFromString(args[2])
            val date = Date(System.currentTimeMillis() + time.toLong() * timeMultiplier * 1000)
            val sb = StringBuilder()
            for (i in 3 until args.size) {
                sb.append(args[i]).append(" ")
            }
            val reason = sb.toString()
            val target: Player? = Bukkit.getPlayer(args[0])
            if (target == null) {
                player.sendMessage("The player ${args[0]} was banned for $time ${args[2]}")
                tempbanUser("You are banned for $time ${args[2]}\nReason: $reason", date, player, args[0])
                return true
            }
            if(target.crashPlayer().isMod) {
                player.sendMessage("§4You cant ban moderators!")
                return true
            }
            tempbanOnlineUser(target, "You are banned for $time ${args[2]}\nReason: $reason", date)
            player.sendMessage("The player ${args[0]} was banned for $time ${args[2]}")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when(args.size) {
            1 -> {
                val list: MutableList<String> = ArrayList()
                val currentString: String = args[0]
                for (player in Bukkit.getOnlinePlayers()) {
                    if (player.name.startsWith(currentString)) {
                        list.add(player.name)
                    }
                }
                list
            }
            2 -> mutableListOf("1", "3", "7", "14", "30")
            3 -> mutableListOf("minutes", "hours", "days", "weeks", "months")
            else -> mutableListOf("<reason>")
        }
    }
}