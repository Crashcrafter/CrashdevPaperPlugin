package dev.crash.commands

import dev.crash.asPlayer
import dev.crash.permission.*
import dev.crash.player.crashPlayer
import me.kbrewster.mojangapi.MojangAPI
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class ClaimCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        val chunk = player.world.getChunkAt(player.location)
        if (args.isEmpty()) {
            if(player.crashPlayer().isMod && player.gameMode == GameMode.CREATIVE){
                chunk.claim("0", "Server-Team", player)
            }else if(getRemainingClaims(player.uniqueId.toString()) > 0) {
                chunk.claim(player)
            }else {
                player.sendMessage("§4You can't claim more chunks!")
            }
        } else if (args[0] == "info") {
            if(!chunk.isClaimed()){
                player.sendMessage("§6This chunk belongs to no one!\nYou can claim ${player.crashPlayer().remainingClaims} more chunks")
                return true
            }
            val chunkClass = chunk.chunkData()!!
            player.sendMessage("§6This chunk belongs to ${chunkClass.name}\nYou can claim ${player.crashPlayer().remainingClaims} more chunks")
        } else if (args[0] == "remove") {
            if(chunk.isClaimed()) {
                if (player.isOp && (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR)) {
                    player.sendMessage("§aClaim was removed!")
                    chunk.unClaim()
                } else if (chunk.chunkData()!!.owner_uuid == player.uniqueId.toString()) {
                    player.sendMessage("§aClaim was removed!")
                    chunk.unClaim()
                }
            }else player.sendMessage("§4Chunk is not claimed!")
        } else if (args[0] == "access") {
            if(args.size < 3) {
                player.sendMessage("§4Incomplete command!")
                return true
            }
            val s2 = args[2]
            if (player.name != s2) {
                val give = args[1] == "add"
                val target = Bukkit.getPlayer(s2)
                if(target != null) {
                    chunk.changeChunkAccess(target, give, player)
                    return true
                }
                val targetUuid = MojangAPI.getUUID(s2).toString()
                if(!give)chunk.changeChunkAccess(targetUuid, false, player)
            } else {
                player.sendMessage("§aYou are the owner of the chunk!")
            }
        } else if (args[0] == "accessall") {
            if(args.size < 3) {
                player.sendMessage("§4Incomplete command!")
                return true
            }
            if (player.name != args[2]) {
                val give = args[1] == "add"
                try {
                    val target = Bukkit.getPlayer(args[2])!!.uniqueId.toString()
                    player.changeAccessAllChunks(target, give, player)
                    return true
                }catch (ex: Exception) {
                    if(give) {
                        player.sendMessage("§4Please enter a valid player!")
                        return true
                    }
                    val targetUuid = MojangAPI.getUUID(args[2])!!.toString()
                    player.changeAccessAllChunks(targetUuid, false, player)
                }
            } else {
                player.sendMessage("§4You are the owner of the chunk!\nYou can't remove your own access!")
            }
        } else if (args[0] == "removeall") {
            removeAllClaims(player)
            player.sendMessage("§aAll your claims have been removed!")
        } else if (args[0] == "list") {
            player.sendMessage("${getRemainingClaims(player.uniqueId.toString())}")
        } else if (args[0] =="changeadded") {
            if (!player.isOp) return true
            if(args.size < 3) {
                player.sendMessage("§4Incomplete command!")
                return true
            }
            val player1: Player = Bukkit.getPlayer(args[1])!!
            val amount: Int = args[2].toInt()
            changeAddedClaims(player1, amount)
        } else if (args[0] == "addedinfo") {
            if(args.size < 2) {
                player.sendMessage("§4Incomplete command!")
                return true
            }
            val player1: Player = Bukkit.getPlayer(args[1])!!
            player.sendMessage(player1.name + " has " + getAddedClaims(player1.uniqueId.toString()))
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        val player = sender.asPlayer()
        if (args.size == 1) {
            val list: MutableList<String> = mutableListOf("info", "remove", "access", "accessall", "removeall", "list")
            if (player.isOp) list.addAll(listOf("changeadded", "addedinfo"))
            return list
        } else if (args[0] == "access" || args[0] == "accessall") {
            if (args.size == 2) {
                return mutableListOf("add", "remove")
            } else if (args.size == 3) {
                if (args[1] == "add") {
                    val list: MutableList<String> = ArrayList()
                    for (player1 in Bukkit.getOnlinePlayers()) {
                        list.add(player1.name)
                    }
                    return list
                } else if (args[1] == "remove") {
                    val list: MutableList<String> = ArrayList()
                    val chunk = player.location.chunk
                    for (chunkClassWorlds in chunks.values) {
                        for(chunkClass in chunkClassWorlds.values){
                            if (chunkClass.x == chunk.x) {
                                if (chunkClass.z == chunk.z) {
                                    for (uuid in chunkClass.shared) {
                                        for (player1 in Bukkit.getOnlinePlayers()) {
                                            if (player1.uniqueId.toString().contentEquals(uuid)) {
                                                list.add(player1.name)
                                            }
                                        }
                                        for (player1 in Bukkit.getOfflinePlayers()) {
                                            if (player1.uniqueId.toString().contentEquals(uuid)) {
                                                list.add(player1.name!!)
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    }
                    return list
                }
            }
        }
        return null
    }
}