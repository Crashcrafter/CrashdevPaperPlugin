package dev.crash.commands.text

import dev.crash.CONFIG
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class VoteCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val stringBuilder = StringBuilder()
        stringBuilder.append("§aHere are the vote links:\n")
        CONFIG.voteLinks.forEach {
            stringBuilder.append("§f§7§n$it§r\n")
        }
        sender.sendMessage(stringBuilder.toString())
        return true
    }
}