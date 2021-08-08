package dev.crash.commands.home

import dev.crash.asPlayer
import dev.crash.player.crashPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class DelhomeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (args.isNotEmpty()) {
            val keyword: String = args[0]
            val crashPlayer = player.crashPlayer()
            if (crashPlayer.homes.containsKey(keyword)) {
                crashPlayer.homes.remove(keyword)
                crashPlayer.delHome(keyword)
            } else {
                player.sendMessage("§4You dont have a homepoint called $keyword")
            }
        } else {
            player.sendMessage("§4Please enter the name of the homepoint")
        }
        return true
    }
}
