package dev.crash.commands.home

import dev.crash.asPlayer
import dev.crash.permission.chunkData
import dev.crash.permission.isClaimed
import dev.crash.player.crashPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetHomeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val chunk = player.location.chunk
        if (args.isNotEmpty()) {
            val keyword: String = args[0]
            val crashPlayer = player.crashPlayer()
            if(crashPlayer.remainingHomes > 0){
                if (!crashPlayer.homes.containsKey(keyword)) {
                    if(chunk.isClaimed()){
                        if(chunk.chunkData()!!.owner_uuid == player.uniqueId.toString())crashPlayer.setHome(keyword) else player.sendMessage("§4Du kannst nicht in fremden Chunks Homepoints setzen!")
                    }else crashPlayer.setHome(keyword)
                } else {
                    player.sendMessage("§4You already have a homepoint with that name!")
                }
            }else {
                player.sendMessage("§4You cant set more homepoints!")
            }
        } else {
            player.sendMessage("§4Please enter a name for the homepoint!")
        }
        return true
    }
}