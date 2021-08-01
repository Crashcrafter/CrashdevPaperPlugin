package dev.crash.commands.home

import dev.crash.asPlayer
import dev.crash.permission.chunks
import dev.crash.permission.isClaimed
import dev.crash.player.rlgPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SetHomeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val chunk = player.location.chunk
        if (args.isNotEmpty()) {
            val keyword: String = args[0]
            val rlgPlayer = player.rlgPlayer()
            if (!rlgPlayer.homes.containsKey(keyword)) {
                if(chunk.isClaimed()){
                    if(chunks[chunk.chunkKey]!![chunk.world.name]!!.owner_uuid == player.uniqueId.toString())rlgPlayer.setHome(keyword) else player.sendMessage("§4Du kannst nicht in fremden Chunks Homepoints setzen!")
                }else rlgPlayer.setHome(keyword)
            } else {
                player.sendMessage("§4Ein Home mit diesem Namen existiert schon")
            }
        } else {
            player.sendMessage("§4Bitte gib einen Namen ein")
        }
        return true
    }
}