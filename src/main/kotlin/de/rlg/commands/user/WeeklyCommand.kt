package de.rlg.commands.user

import de.rlg.asPlayer
import de.rlg.player.rlgPlayer
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