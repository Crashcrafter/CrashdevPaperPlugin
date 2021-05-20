package de.rlg.commands.home

import de.rlg.asPlayer
import de.rlg.commands.tp.delayedTeleport
import de.rlg.player.rlgPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class HomeCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val rlgPlayer = player.rlgPlayer()
        try {
            if (args.isNotEmpty()) {
                val keyword: String = args[0]
                if (rlgPlayer.homes.containsKey(keyword)) {
                    if(player.isOp) player.teleport(rlgPlayer.homes[keyword]!!.location.add(0.5, 0.0, 0.5))
                    else delayedTeleport(player, rlgPlayer.homes[keyword]!!.location.add(0.5, 0.0, 0.5))
                } else {
                    player.sendMessage("§4Du hast keinen Homepoint mit diesem Namen!")
                }
            } else {
                player.sendMessage("§4Bitte gib den Namen des Homes ein!")
            }
        } catch (e: NullPointerException) {
            player.sendMessage("§4Du hast keinen Homepoint mit diesem Namen!")
        } catch (e: IllegalArgumentException) {
            player.sendMessage("§4Du hast keinen Homepoint mit diesem Namen!")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        return if(args.size == 1) sender.asPlayer().rlgPlayer().homes.keys.toMutableList() else null
    }
}