package dev.crash.commands.mod

import dev.crash.asPlayer
import dev.crash.player.crashPlayer
import dev.crash.unmute
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class UnmuteCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(player.hasPermission("crash.unmute")){
            try {
                val target = Bukkit.getPlayer(args[0])!!
                player.sendMessage("§2Player ${target.name} is unmuted!")
                target.crashPlayer().unmute()
            }catch (ex: ArrayIndexOutOfBoundsException){
                player.sendMessage("§4You must specify a player!")
                return true
            }catch (ex: NullPointerException) {
                player.sendMessage("§4Player not found!")
                return true
            }
        }
        return true
    }
}