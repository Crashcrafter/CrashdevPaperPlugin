package dev.crash.commands.text

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class VoteCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        sender.sendMessage(
            "§aHere are the vote links:\n§f§7§nhttps://bit.ly/MCGVote1§r\n" +
                    "§f§7§nhttps://bit.ly/MCGVote2§r\n§f§7§nhttps://bit.ly/MCGVote3§r\n" +
                    "§f§7§nhttps://bit.ly/MCGVote4§r\n§f§7§nhttps://bit.ly/MCGVote5§r\n"
        )
        return true
    }
}