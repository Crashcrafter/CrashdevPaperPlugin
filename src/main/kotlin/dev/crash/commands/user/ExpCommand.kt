package dev.crash.commands.user

import dev.crash.asPlayer
import dev.crash.getEXPForLevel
import dev.crash.player.crashPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class ExpCommand : CommandExecutor, TabCompleter{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.isOp) {
            if (args.size >= 2) {
                val arg: String = args[0]
                val target: Player = Bukkit.getPlayer(args[1])!!
                if (args.size <= 3) {
                    player.sendMessage("§4Invalid arguments!")
                    return true
                }
                val amount: Long = args[2].toLong()
                val factor = if(arg == "remove") -1 else 1
                if (args[3].contentEquals("XP")) {
                    target.crashPlayer().changeXP(amount * factor)
                } else if (args[3].contentEquals("Level")) {
                    var finalExp: Long = 0
                    val crashPlayer = target.crashPlayer()
                    for (i in 0 until amount) {
                        finalExp += getEXPForLevel(crashPlayer.xpLevel + (i.toInt() * factor) - if(factor == -1) 1 else 0)
                    }
                    crashPlayer.changeXP(finalExp * factor)
                }
                player.sendMessage("§2${(amount*factor)} ${args[3]} were given to ${args[1]}!")
            } else {
                player.sendMessage("§4Invalid arguments!")
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
        val player = sender.asPlayer()
        if (player.isOp) {
            val list: MutableList<String> = ArrayList()
            if (args.size == 1) {
                list.add("add")
                list.add("remove")
            } else if (args[0].contentEquals("add") || args[0].contentEquals("remove")) {
                when (args.size) {
                    2 -> {
                        val currentString: String = args[1]
                        for (player1 in Bukkit.getOnlinePlayers()) {
                            if (player1.name.startsWith(currentString)) {
                                list.add(player1.name)
                            }
                        }
                    }
                    3 -> {
                        list.add("1")
                        list.add("2")
                        list.add("3")
                        list.add("5")
                        list.add("10")
                    }
                    4 -> {
                        list.add("Level")
                        list.add("XP")
                    }
                }
            }
            return list
        }
        return null
    }
}