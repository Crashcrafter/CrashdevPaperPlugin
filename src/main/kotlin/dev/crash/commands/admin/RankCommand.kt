package dev.crash.commands.admin

import dev.crash.PlayerTable
import dev.crash.asPlayer
import dev.crash.permission.getRankByString
import dev.crash.permission.givePerms
import dev.crash.permission.rankData
import dev.crash.permission.ranks
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
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class RankCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender.asPlayer()
        if (player.hasPermission("rlg.rank")) {
            if (args[0] == "set") {
                val rank = getRankByString(args[2])
                if(rank == null){
                    player.sendMessage("§4Rank not found!")
                    return true
                }
                val target: Player? = Bukkit.getPlayer(args[1])
                if (target == null) {
                    val offlineTarget: OfflinePlayer = Bukkit.getOfflinePlayer(MojangAPI.getUUID(args[1]))
                    setRank(offlineTarget.uniqueId.toString(), rank.id)
                    player.sendMessage("The ${args[2]}-rank was given to ${offlineTarget.name}")
                } else {
                    target.sendMessage("You received the ${args[2]}-rank from ${player.name}")
                    player.sendMessage("The ${args[2]}-rank was given to ${target.name}")
                    setRank(target, rank.id)
                }
            } else if (args[0] == "info") {
                try {
                    val target: Player = Bukkit.getPlayer(args[1]) ?: player
                    player.sendMessage("The player ${target.name} has the rank ${target.rlgPlayer().rankData().name}")
                }catch (ex: NullPointerException) {
                    val uuid = MojangAPI.getUUID(args[1])
                    val offlineTarget: OfflinePlayer = Bukkit.getOfflinePlayer(uuid)
                    transaction {
                        val rank = PlayerTable.select(where = {PlayerTable.uuid eq uuid.toString()}).first()[PlayerTable.rank]
                        player.sendMessage("The player ${offlineTarget.name} has the rank ${ranks[rank]!!.name}")
                    }
                }
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
    transaction {
        PlayerTable.update(where = {PlayerTable.uuid eq uuid}){
            it[PlayerTable.rank] = rank
        }
    }
}