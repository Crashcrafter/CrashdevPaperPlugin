package dev.crash.commands.admin

import dev.crash.asPlayer
import dev.crash.loadFromDb
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("rlg.reloaddb")) return true
        loadFromDb()
        sender.sendMessage("§2DB Reloaded!")
        return true
    }
}