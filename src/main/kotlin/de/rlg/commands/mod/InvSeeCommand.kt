package de.rlg.commands.mod

import de.rlg.asPlayer
import de.rlg.permission.invSeeInventories
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class InvSeeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("rlg.invsee")) return true
        try {
            val target: Player = Bukkit.getPlayer(args[0])!!
            if (!target.isOp) {
                val targetInventory: Inventory = target.inventory
                player.openInventory(targetInventory)
                invSeeInventories[targetInventory] = target
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            player.sendMessage("Der Spieler wurde nicht gefunden")
        } catch (e: NullPointerException) {
            player.sendMessage("Der Spieler wurde nicht gefunden")
        }
        return true
    }
}