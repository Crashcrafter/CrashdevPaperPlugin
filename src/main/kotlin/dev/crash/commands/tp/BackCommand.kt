package dev.crash.commands.tp

import dev.crash.asPlayer
import dev.crash.player.rlgPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class BackCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val rlgPlayer = player.rlgPlayer()
        if (rlgPlayer.deathPos != null) {
            delayedTeleport(player, rlgPlayer.deathPos!!){
                rlgPlayer.deathPos = null
            }
        } else {
            player.sendMessage("§4Du bist nicht gestorben oder hast /back schon benutzt!")
        }
        return true
    }
}