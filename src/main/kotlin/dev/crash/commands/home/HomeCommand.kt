package dev.crash.commands.home

import dev.crash.asPlayer
import dev.crash.commands.tp.delayedTeleport
import dev.crash.player.crashPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class HomeCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val crashPlayer = player.crashPlayer()
        if (args.isNotEmpty()) {
            val keyword: String = args[0]
            if (crashPlayer.homes.containsKey(keyword)) {
                delayedTeleport(player, crashPlayer.homes[keyword]!!.location.add(0.5, 0.0, 0.5))
            } else {
                player.sendMessage("§4You dont have a homepoint called $keyword")
            }
        } else {
            player.sendMessage("§4Please enter the name of the homepoint")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        return if(args.size == 1) sender.asPlayer().crashPlayer().homes.keys.toMutableList() else null
    }
}