package dev.crash.commands.admin

import dev.crash.asPlayer
import dev.crash.player.load
import dev.crash.player.unload
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadUserCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if(player.hasPermission("crash.reloaddb")){
            if(args.isEmpty()){
                player.sendMessage("§4You must send a player as first argument!")
                return true
            }
            val target = Bukkit.getPlayer(args[0])!!
            target.unload()
            target.load()
            player.sendMessage("§6Player is reloaded!")
        }
        return true
    }
}