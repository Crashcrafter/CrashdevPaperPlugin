package dev.crash.commands.mod

import dev.crash.asPlayer
import dev.crash.mute
import dev.crash.player.crashPlayer
import dev.crash.timeMultiplierFromString
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class MuteCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("crash.mute")) return true
        if(args.size < 3){
            player.sendMessage("§4Incomplete Command!")
            return true
        }
        val target = Bukkit.getPlayer(args[0])!!
        if(target.isOp) return true
        val time = args[1].toLong()
        val timeMultiplier:Long = timeMultiplierFromString(args[2])
        target.crashPlayer().mute(time*timeMultiplier)
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
            3 -> mutableListOf("minutes", "hours", "days", "weeks", "months")
            else -> null
        }
    }
}