package dev.crash.commands.home

import dev.crash.asPlayer
import dev.crash.player.crashPlayer
import dev.crash.player.CrashPlayer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class HomesCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val crashPlayer = player.crashPlayer()
        if (crashPlayer.isMod && args.isNotEmpty()) {
            val target: Player = Bukkit.getPlayer(args[0])!!
            val targetcrashPlayer = target.crashPlayer()
            val msgBuilder = StringBuilder()
            msgBuilder.append("§6These are ${target.name}'s homepoints:§r")
            msgBuilder.addHomes(targetcrashPlayer)
            player.sendMessage(msgBuilder.toString())
        } else {
            val msgBuilder = StringBuilder()
            msgBuilder.append("§6Here are your homepoints:§r")
            msgBuilder.addHomes(crashPlayer)
            player.sendMessage(msgBuilder.toString())
        }
        return true
    }
}

fun StringBuilder.addHomes(crashPlayer: CrashPlayer){
    for (name in crashPlayer.homes.keys) {
        val location: Location = crashPlayer.homes[name]!!.location
        this.append("\n§a" + name + "§r: " + location.world.name + ": " + location.x + "|" + location.y + "|" + location.z)
    }
}