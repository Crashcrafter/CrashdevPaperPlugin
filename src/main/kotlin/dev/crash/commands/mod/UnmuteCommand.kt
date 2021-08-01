package dev.crash.commands.mod

import dev.crash.asPlayer
import dev.crash.player.rlgPlayer
import dev.crash.unmute
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class UnmuteCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(player.hasPermission("rlg.unmute")){
            try {
                val target = Bukkit.getPlayer(args[0])!!
                player.sendMessage("§2Spieler wurde entmuted!")
                target.unmute()
            }catch (ex: ArrayIndexOutOfBoundsException){
                player.sendMessage("§4Du musst einen Spieler angeben!")
                return true
            }catch (ex: NullPointerException) {
                player.sendMessage("§4Spieler wurde nicht gefunden!")
                return true
            }
        }
        return true
    }
}