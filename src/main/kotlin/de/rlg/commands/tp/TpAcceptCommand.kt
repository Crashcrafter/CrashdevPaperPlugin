package de.rlg.commands.tp

import de.rlg.asPlayer
import de.rlg.targetMap
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.jetbrains.annotations.Nullable
import java.util.*

class TpAcceptCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (targetMap.containsValue(player.uniqueId)) {
            sender.sendMessage("§6TPA request accepted!")
            var key: UUID? = null
            targetMap.forEach {
                if(it.value == player.uniqueId) {
                    key = it.key
                    return@forEach
                }
            }
            val tpRequester: @Nullable Player = Bukkit.getPlayer(key!!) ?: return true
            delayedTeleport(tpRequester, player.location)
        } else {
            player.sendMessage("§4Du hast keine ausstehenden Anfragen!")
        }
        return true
    }
}