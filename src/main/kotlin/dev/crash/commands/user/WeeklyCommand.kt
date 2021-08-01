package dev.crash.commands.user

import dev.crash.asPlayer
import dev.crash.player.rlgPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class WeeklyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer().rlgPlayer()
        player.weeklyKeys()
        return true
    }
}