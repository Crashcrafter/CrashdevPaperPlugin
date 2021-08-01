package dev.crash.commands.home

import dev.crash.asPlayer
import dev.crash.player.rlgPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class DelhomeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (args.isNotEmpty()) {
            val keyword: String = args[0]
            val rlgPlayer = player.rlgPlayer()
            if (rlgPlayer.homes.containsKey(keyword)) {
                rlgPlayer.homes.remove(keyword)
                rlgPlayer.delHome(keyword)
            } else {
                player.sendMessage("Bitte gib einen gültigen Namen des Homepoints ein")
            }
        } else {
            player.sendMessage("Bitte gib den Namen des Homes ein")
        }
        return true
    }
}
