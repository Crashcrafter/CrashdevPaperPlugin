package dev.crash.commands.user

import dev.crash.asPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class EcCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if ((!player.world.name.contentEquals("event") || player.isOp) && player.hasPermission("rlg.ec")) {
            player.openInventory(player.enderChest)
        } else {
            player.sendMessage("§4Du kannst nicht /ec nutzen!")
        }
        return true
    }
}