package dev.crash.commands.tp

import dev.crash.targetMap
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TpDenyCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val senderP = sender as Player
        if (targetMap.containsValue(senderP.uniqueId)) {
            targetMap.forEach {
                if (it.value == senderP.uniqueId) {
                    targetMap.remove(it.key)
                    val originalSender = Bukkit.getPlayer(it.key)
                    originalSender!!.sendMessage("§4Deine TP Request wurde abgelehnt!")
                    sender.sendMessage("§2TP Request wurde abgelehnt.")
                    return@forEach
                }
            }
        } else {
            sender.sendMessage("§4Du hast keine offenen TP Requests!")
        }
        return true
    }
}