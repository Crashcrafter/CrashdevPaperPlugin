package de.rlg.commands.mod

import de.rlg.asPlayer
import de.rlg.mute
import de.rlg.timeMultiplierFromString
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class MuteCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(player.hasPermission("rlg.mute")){
            if(args.size < 3){
                player.sendMessage("§4Unvollständiger Command!")
                return true
            }
            val target = Bukkit.getPlayer(args[0])!!
            if(target.isOp) return true
            val time = args[1].toLong()
            val timeMultiplier:Long = timeMultiplierFromString(args[2])
            target.mute(time*timeMultiplier)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        return when(args.size) {
            2 -> mutableListOf("1", "3", "7", "14", "30")
            3 -> mutableListOf("Minuten", "Stunden", "Tage", "Wochen", "Monate")
            else -> null
        }
    }
}