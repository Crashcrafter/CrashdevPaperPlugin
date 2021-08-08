package dev.crash.commands.mod

import dev.crash.asPlayer
import dev.crash.sendModchatMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ModChatCommand : CommandExecutor{
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.hasPermission("crash.modchat")) {
            val builder = StringBuilder()
            for (msgPart in args) {
                builder.append(msgPart).append(" ")
            }
            sendModchatMessage(builder.toString(), player)
        }
        return true
    }
}