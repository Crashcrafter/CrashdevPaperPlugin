package dev.crash.commands.text

import dev.crash.CONFIG
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class DiscordCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        sender.sendMessage("Our Discord:\n\n${CONFIG.dcLink}")
        return true
    }
}