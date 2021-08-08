package dev.crash.commands.tp

import dev.crash.targetMap
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TpDenyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as Player
        if(!player.hasPermission("crash.tpa")) return true
        if (targetMap.containsValue(player.uniqueId)) {
            targetMap.forEach {
                if (it.value == player.uniqueId) {
                    targetMap.remove(it.key)
                    val originalSender = Bukkit.getPlayer(it.key)
                    originalSender!!.sendMessage("§4Your TPA was denied!")
                    sender.sendMessage("§2TPA was denied.")
                    return@forEach
                }
            }
        } else {
            sender.sendMessage("§4You have no pending requests!")
        }
        return true
    }
}