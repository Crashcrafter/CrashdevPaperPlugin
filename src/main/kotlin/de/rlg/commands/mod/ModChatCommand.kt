package de.rlg.commands.mod

import de.rlg.asPlayer
import de.rlg.player.rlgPlayer
import de.rlg.sendModchatMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ModChatCommand : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.hasPermission("rlg.modchat")) {
            val builder = StringBuilder()
            for (msgPart in args) {
                builder.append(msgPart).append(" ")
            }
            sendModchatMessage(builder.toString(), player)
        }
        return true
    }
}