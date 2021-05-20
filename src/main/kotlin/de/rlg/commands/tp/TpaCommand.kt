package de.rlg.commands.tp

import de.rlg.INSTANCE
import de.rlg.asPlayer
import de.rlg.targetMap
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

class TpaCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size == 1) {
            if (!Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[0]))) {
                sender.sendMessage(ChatColor.RED.toString() + "Player is not online!")
                return true
            }
            val target: Player = Bukkit.getPlayer(args[0])!!
            val senderP = sender.asPlayer()
            if (target.uniqueId == senderP.uniqueId) {
                sender.sendMessage(ChatColor.RED.toString() + "You may not teleport to yourself!")
                return true
            }
            if (targetMap.containsKey(senderP.uniqueId)) {
                sender.sendMessage(ChatColor.GOLD.toString() + "You already have a pending request!")
                return false
            }
            target.sendMessage(
                """${ChatColor.RED}${senderP.name}${ChatColor.GOLD} wants to teleport to you. 
Type ${ChatColor.RED}/tpaccept${ChatColor.GOLD} to accept this request.
Type ${ChatColor.RED}/tpdeny${ChatColor.GOLD} to deny this request.
You have 5 minutes to respond."""
            )
            targetMap[senderP.uniqueId] = target.uniqueId
            sender.sendMessage(ChatColor.GOLD.toString() + "Send TPA request to " + ChatColor.RED + target.name)
            object : BukkitRunnable() {
                override fun run() {
                    targetMap.remove(senderP.uniqueId)
                }
            }.runTaskLaterAsynchronously(INSTANCE, 6000L)
        } else {
            sender.sendMessage(ChatColor.RED.toString() + "Invalid synax!")
        }
        return true
    }
}