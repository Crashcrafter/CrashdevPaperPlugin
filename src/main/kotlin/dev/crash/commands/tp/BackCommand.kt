package dev.crash.commands.tp

import dev.crash.asPlayer
import dev.crash.player.crashPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class BackCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("crash.back")) return true
        val crashPlayer = player.crashPlayer()
        if (crashPlayer.deathPos != null) {
            delayedTeleport(player, crashPlayer.deathPos!!){
                crashPlayer.deathPos = null
            }
        } else {
            player.sendMessage("§4You haven't died or already used /back!")
        }
        return true
    }
}