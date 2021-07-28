package de.rlg.commands

import de.rlg.asPlayer
import de.rlg.permission.*
import de.rlg.player.rlgPlayer
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
            if (player.rlgPlayer().remainingClaims > 0 || player.isOp) {
                if(player.rlgPlayer().isMod && player.gameMode == GameMode.CREATIVE){
                    chunk.claim("0", "Server-Team", player)
                }else {
                    chunk.claim(player)
                }
            } else {
                player.sendMessage("§4Du kannst erst 1 Minute nach dem Joinen auf dem Server claimen!")
            }
        } else if (args[0].equals("info", ignoreCase = true)) {
            if(!chunk.isClaimed()){
                player.sendMessage(
                    "§6Dieser Chunk gehört niemanden!\nDu kannst noch ${getRemainingClaims(player.uniqueId.toString())} Chunks claimen"
                )
                return true
            }
            val chunkClass = chunks[chunk]!!
            player.sendMessage(
                "§6Dieser Chunk gehört ${chunkClass.name}\nDu kannst noch ${getRemainingClaims(player.uniqueId.toString())} Chunks claimen"
            )
        } else if (args[0].equals("remove", ignoreCase = true)) {
            if(chunk.isClaimed()) {
                if (player.isOp && (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR)) {
                    player.sendMessage("§aClaim wurde entfernt!")
                    chunk.unClaim()
                } else if (!player.world.name.contentEquals("shops")) {
                    if (chunks[chunk]!!.owner_uuid == player.uniqueId.toString()) {
                        player.sendMessage("§aClaim wurde entfernt!")
                        chunk.unClaim()
                    }
                } else {
                    player.sendMessage("§4Du kannst nicht in der Shop-Welt Claims entfernen!")
                }
            }else player.sendMessage("§4Chunk ist nicht geclaimt!")
        } else if (args[0].contentEquals("access")) {
            var s2 = ""
            try {
                s2 = args[2]
            } catch (e: ArrayIndexOutOfBoundsException) {
                player.sendMessage("§4Bitte benutze die Richtige Syntax: /claim access [add/remove] [Player]")
            }
            if (!player.name.contentEquals(s2)) {
                var give = false
                try {
                    if (args[1].contentEquals("add")) {
                        give = true
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                    player.sendMessage("§4Bitte gib einen Spieler an!")
                }
                val target = Bukkit.getPlayer(s2)
                if(target != null) {
                    if(give)chunk.grantChunkAccess(player, target) else chunk.revokeChunkAccess(player, target)
                    return true
                }
                val targetUuid = MojangAPI.getUUID(s2).toString()
                if(!give)chunk.revokeChunkAccess(targetUuid, player)
            } else {
                player.sendMessage("§aDu bist der Besitzer des Chunks!")
            }
        } else if (args[0].contentEquals("accessall")) {
            if (!player.name.contentEquals(args[2])) {
                var give = false
                try {
                    if (args[1].contentEquals("add")) {
                        give = true
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                    player.sendMessage("§4Bitte gib einen Aktion an!")
                }
                try {
                    val target = Bukkit.getPlayer(args[2])!!.uniqueId.toString()
                    player.changeAccessAllChunks(target, give)
                    return true
                }catch (ex: Exception) {
                    if(give) {
                        player.sendMessage("§4Bitte gib einen Spieler an!")
                        return true
                    }
                    val targetUuid = MojangAPI.getUUID(args[2])!!.toString()
                    player.changeAccessAllChunks(targetUuid, false)
                    player.sendMessage("§2Dem Spieler ${args[2]} wurde der Zugriff auf diesen Chunk entfernt entfernt!")
                }
            } else {
                player.sendMessage("§4Du bist der Besitzer der Chunks!\nDu kannst dich nicht selbst entfernen!")
            }
        } else if (args[0].contentEquals("removeall")) {
            removeAllClaims(player)
            player.sendMessage("§aAlle deine Claims wurden entfernt!")
        } else if (args[0].contentEquals("list")) {
            player.sendMessage("${getRemainingClaims(player.uniqueId.toString())}")
        } else if (args[0].contentEquals("add")) {
            if (player.isOp) {
                try {
                    val player1: Player = Bukkit.getPlayer(args[1])!!
                    val amount: Int = Integer.valueOf(args[2])
                    changeAddedClaims(player1, amount)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler und Anzahl an")
                }
            }
        } else if (args[0].contentEquals("removeadded")) {
            if (player.isOp) {
                try {
                    val player1: Player = Bukkit.getPlayer(args[1])!!
                    val amount: Int = Integer.valueOf(args[2])
                    changeAddedClaims(player1, -amount)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    player.sendMessage("§4Bitte gib einen gültigen Spieler und Anzahl an")
                }
            }
        } else if (args[0].contentEquals("addedinfo")) {
            try {
                val player1: Player = Bukkit.getPlayer(args[1])!!
                player.sendMessage(player1.name + " hat " + getAddedClaims(player1.uniqueId.toString()))
            } catch (e: ArrayIndexOutOfBoundsException) {
                player.sendMessage("§4Bitte gib einen gültigen Spieler an")
            }
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
            if (player.isOp) list.addAll(listOf("add", "removeadded", "addedinfo"))
            return list
        } else if (args[0].contentEquals("access") || args[0].contentEquals("accessall")) {
            if (args.size == 2) {
                return mutableListOf("add", "remove")
            } else if (args.size == 3) {
                if (args[1].contentEquals("add")) {
                    val list: MutableList<String> = ArrayList()
                    for (player1 in Bukkit.getOnlinePlayers()) {
                        list.add(player1.name)
                    }
                    return list
                } else if (args[1].contentEquals("remove")) {
                    val list: MutableList<String> = ArrayList()
                    val chunk = player.location.chunk
                    for (chunkClass in chunkList) {
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
                    return list
                }
            }
        }
        return null
    }
}