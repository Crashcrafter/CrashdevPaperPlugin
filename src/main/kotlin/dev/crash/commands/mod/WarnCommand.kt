package dev.crash.commands.mod

import dev.crash.*
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WarnCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val mod = sender.asPlayer()
        val target: Player? = Bukkit.getPlayer(args[1])
        if (mod.hasPermission("rlg.warn")) {
            if (args[0].contentEquals("add")) {
                val sb = StringBuilder()
                for (i in 2 until args.size) {
                    sb.append(args[i]).append(" ")
                }
                val reason = sb.toString()
                mod.sendMessage("Der Spieler " + args[1] + " wurde wegen " + reason + " gewarnt!")
                if (target != null) {
                    target.sendMessage("Du wurdest wegen " + reason + "gewarnt!")
                    warnPlayer(target, sb.toString(), mod.name)
                }
            } else if (args[0].contentEquals("list")) {
                val list: String = getWarns(target!!)
                mod.sendMessage(
                    """
                ${target.name}'s Warnungen:
                $list
                """.trimIndent()
                )
            } else if (args[0].contentEquals("remove")) {
                if (args[2].contentEquals("all")) {
                    if(target == null) {
                        mod.sendMessage("§4Der Spieler wurde nicht gefunden!")
                        return true
                    }
                    removeAllWarns(target)
                    mod.sendMessage("§2Alle Warnungen wurden entfernt")
                } else {
                    val number: Int = args[2].toInt()
                    removeWarn(target!!, number)
                    mod.sendMessage("Warnung Nummer $number wurde entfernt")
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