package dev.crash.commands.mod

import dev.crash.asPlayer
import dev.crash.permission.invSeeECs
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class InvEcCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(!player.hasPermission("rlg.invec"))return true
        try {
            val target: Player = Bukkit.getPlayer(args[0])!!
            if (!target.isOp) {
                val targetInventory = target.enderChest
                player.openInventory(targetInventory)
                invSeeECs[targetInventory] = target
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            player.sendMessage("Der Spieler wurde nicht gefunden")
        } catch (e: NullPointerException) {
            player.sendMessage("Der Spieler wurde nicht gefunden")
        }
        return true
    }
}