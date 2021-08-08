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
        if(!player.hasPermission("crash.invec"))return true
        if(args.isEmpty()){
            player.sendMessage("§4Please specify a player!")
            return true
        }
        val target: Player = Bukkit.getPlayer(args[0])!!
        if (!target.isOp) {
            val targetInventory = target.enderChest
            player.openInventory(targetInventory)
            invSeeECs[targetInventory] = target
        }
        return true
    }
}