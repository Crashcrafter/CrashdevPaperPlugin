package de.rlg.commands.user

import de.rlg.asPlayer
import de.rlg.checkMessage
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class MessageCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val target = Bukkit.getPlayer(args[0])
        if(target == null) {
            player.sendMessage("§4Der Spieler ${args[0]} wurde nicht gefunden!")
            return true
        }
        val msgBuilder = StringBuilder()
        for (i in 1 until args.size) {
            msgBuilder.append(args[i]).append(" ")
        }
        val msg = msgBuilder.toString()
        if (!checkMessage(msg, sender.asPlayer()) && player.rlgPlayer().mutedUntil > System.currentTimeMillis()) {
            target.sendMessage("§7" + player.name + " sagt: " + msg)
            player.sendMessage("§7" + target.name + " wurde gesagt: " + msg)
        }
        return true
    }
}