package de.rlg.commands.home

import de.rlg.asPlayer
import de.rlg.permission.rankData
import de.rlg.player.RLGPlayer
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HomesCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val rlgPlayer = player.rlgPlayer()
        if (rankData[rlgPlayer.rank]!!.isMod && args.isNotEmpty()) {
            val target: Player = Bukkit.getPlayer(args[0])!!
            val targetRlgPlayer = target.rlgPlayer()
            val msgBuilder = StringBuilder()
            msgBuilder.append("§6Das sind " + target.name + "'s Homepoints:§r")
            msgBuilder.addHomes(targetRlgPlayer)
            player.sendMessage(msgBuilder.toString())
        } else {
            val msgBuilder = StringBuilder()
            msgBuilder.append("§6Das sind deine Homepoints:§r")
            msgBuilder.addHomes(rlgPlayer)
            player.sendMessage(msgBuilder.toString())
        }
        return true
    }
}

fun StringBuilder.addHomes(rlgPlayer: RLGPlayer){
    for (name in rlgPlayer.homes.keys) {
        val location: Location = rlgPlayer.homes[name]!!.location
        this.append("\n§a" + name + "§r: " + location.world.name + ": " + location.x + "|" + location.y + "|" + location.z)
    }
}