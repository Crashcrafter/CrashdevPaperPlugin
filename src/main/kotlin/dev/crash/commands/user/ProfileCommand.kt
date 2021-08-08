package dev.crash.commands.user

import dev.crash.asPlayer
import dev.crash.getEXPForLevel
import dev.crash.getExpDisplay
import dev.crash.player.crashPlayer
import dev.crash.withPoints
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ProfileCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        var target: Player?
        if (args.isNotEmpty()) {
            target = Bukkit.getPlayer(args[0])
            if (target == null) {
                target = player
            }
        }else target = player
        val targetcrashPlayer = target.crashPlayer()
        val builder = StringBuilder()
        builder.append("§6Level ").append(targetcrashPlayer.xpLevel).append(": ")
        val percent: Double = (targetcrashPlayer.xp.toDouble().div(getEXPForLevel(targetcrashPlayer.xpLevel)))
        builder.getExpDisplay(percent)
        player.sendMessage(
            """
                §7----------------Profil----------------
                §6Name: §a${target.name}
                §6Credits: §a${targetcrashPlayer.balance.withPoints()}
                $builder
                """.trimIndent()
        )
        return true
    }
}