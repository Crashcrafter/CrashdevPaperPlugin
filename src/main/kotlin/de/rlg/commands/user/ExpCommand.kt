package de.rlg.commands.user

import de.rlg.asPlayer
import de.rlg.getEXPForLevel
import de.rlg.player.rlgPlayer
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
                if (arg.contentEquals("add")) {
                    if (args.size <= 3) {
                        player.sendMessage("§4Bitte gib eine Anzahl und einen Typ an")
                        return true
                    }
                    val amount: Long = args[2].toLong()
                    if (args[3].contentEquals("XP")) {
                        target.rlgPlayer().changeXP(amount)
                    } else if (args[3].contentEquals("Level")) {
                        var finalexp: Long = 0
                        val rlgPlayer = target.rlgPlayer()
                        for (i in 0 until amount) {
                            finalexp += getEXPForLevel(rlgPlayer.xpLevel + i.toInt())
                        }
                        rlgPlayer.changeXP(finalexp)
                    }
                    player.sendMessage("§2Dem Spieler wurden " + amount + " " + args[3] + " gegeben!")
                } else if (arg.contentEquals("remove")) {
                    if (args.size <= 3) {
                        player.sendMessage("§4Bitte gib eine Anzahl und einen Typ an")
                        return true
                    }
                    val amount: Long = args[2].toLong()
                    if (args[3].contentEquals("XP")) {
                        target.rlgPlayer().changeXP(-amount)
                    } else if (args[3].contentEquals("Level")) {
                        var finalexp: Long = 0
                        val rlgPlayer = target.rlgPlayer()
                        for (i in 1 until amount+1) {
                            finalexp += getEXPForLevel(rlgPlayer.xpLevel - i.toInt())
                        }
                        rlgPlayer.changeXP(-finalexp)
                    }
                    player.sendMessage("§2Dem Spieler wurden " + amount + " " + args[3] + " entfernt!")
                }
            } else {
                player.sendMessage("§4Unvollständiger Command")
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