package dev.crash.commands.admin

import dev.crash.asPlayer
import dev.crash.permission.getRankByString
import dev.crash.permission.givePerms
import dev.crash.permission.rankData
import dev.crash.permission.ranks
import dev.crash.player.modifyPlayerData
import dev.crash.player.rlgPlayer
import dev.crash.updateTabOfPlayers
import me.kbrewster.mojangapi.MojangAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class RankCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.isOp) {
            if (args[0].contentEquals("set")) {
                val rank = getRankByString(args[2])
                if(rank == null){
                    player.sendMessage("§4Rank not found!")
                    return true
                }
                val target: Player? = Bukkit.getPlayer(args[1])
                if (target == null) {
                    val offlineTarget: OfflinePlayer = Bukkit.getOfflinePlayer(MojangAPI.getUUID(args[1]))
                    setRank(offlineTarget.uniqueId.toString(), rank.id)
                    player.sendMessage("Der " + args[2] + "-Rank wurde an " + offlineTarget.name + " vergeben")
                } else {
                    target.sendMessage("Du hast den " + args[2] + "-Rank von " + player.name + " erhalten")
                    player.sendMessage("Der " + args[2] + "-Rank wurde an " + target.name + " vergeben")
                    setRank(target, rank.id)
                }
            } else if (args[0].contentEquals("info")) {
                try {
                    val target: Player = Bukkit.getPlayer(args[1]) ?: player
                    player.sendMessage("Der Spieler ${target.name} hat den ${target.rlgPlayer().rankData().name}-Rank")
                }catch (ex: NullPointerException) {
                    player.sendMessage("§4Spieler ist nicht online!")
                }
            }
        } else {
            player.sendMessage("You dont have the permissions to do that!")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String>? {
        val player1 = sender.asPlayer()
        if (player1.isOp) {
            if (args.size == 1) {
                val list: MutableList<String> = ArrayList()
                list.add("set")
                list.add("info")
                return list
            } else if (args[0].equals("set", ignoreCase = true)) {
                if (args.size == 3) {
                    val list: MutableList<String> = ArrayList()
                    ranks.values.forEach {
                        list.add(it.name)
                    }
                    return list
                } else if (args.size == 2) {
                    val list: MutableList<String> = ArrayList()
                    val currentString: String = args[1]
                    for (player in Bukkit.getOnlinePlayers()) {
                        if (player.name.startsWith(currentString)) {
                            list.add(player.name)
                        }
                    }
                    for (player in Bukkit.getOfflinePlayers()) {
                        if (player.name!!.startsWith(currentString)) {
                            list.add(player.name!!)
                        }
                    }
                    return list
                }
            } else if (args[0].equals("info", ignoreCase = true)) {
                val list: MutableList<String> = ArrayList()
                val players = Bukkit.getOnlinePlayers() as Collection<Player>
                for (player in players) {
                    list.add(player.name)
                }
                return list
            }
        }
        return null
    }
}

fun setRank(player: Player, rank: Int) {
    val rlgPlayer = player.rlgPlayer()
    val currentRank = rlgPlayer.rank
    val currentClaims = rlgPlayer.remainingClaims
    val currentHomes = rlgPlayer.remainingHomes
    val newRemainingClaims = currentClaims + (ranks[rank]!!.claims - ranks[currentRank]!!.claims)
    val newRemainingHomes = currentHomes + (ranks[rank]!!.homes - ranks[currentRank]!!.homes)
    rlgPlayer.rank = rank
    rlgPlayer.remainingClaims = newRemainingClaims
    rlgPlayer.remainingHomes = newRemainingHomes
    rlgPlayer.isMod = ranks[rank]!!.isMod
    rlgPlayer.setName()
    player.givePerms()
    updateTabOfPlayers()
}

fun setRank(uuid: String, rank: Int) {
    modifyPlayerData(uuid){

        it
    }
}