package de.rlg.commands.admin

import de.rlg.PlayersTable
import de.rlg.asPlayer
import de.rlg.permission.getRankByString
import de.rlg.permission.givePerms
import de.rlg.permission.rankData
import de.rlg.player.rlgPlayer
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
        val rlgPlayer = player.rlgPlayer()
        if (rlgPlayer.isMod) {
            if (args[0].contentEquals("set")) {
                val rank: Int = getRankByString(args[2])
                if (rank <= rlgPlayer.rank) {
                    val target: Player? = Bukkit.getPlayer(args[1])
                    if (target == null) {
                        val offlineTarget: OfflinePlayer = Bukkit.getOfflinePlayer(MojangAPI.getUUID(args[1]))
                        if (player.isOp) {
                            setRank(offlineTarget.uniqueId.toString(), rank)
                            player.sendMessage("Der " + args[2] + "-Rank wurde an " + offlineTarget.name + " vergeben")
                        }
                    } else {
                        if (target.rlgPlayer().rank <= rlgPlayer.rank) {
                            target.sendMessage("Du hast den " + args[2] + "-Rank von " + player.name + " erhalten")
                            player.sendMessage("Der " + args[2] + "-Rank wurde an " + target.name + " vergeben")
                            setRank(target, rank)
                        }
                    }
                }
            } else if (args[0].contentEquals("info")) {
                try {
                    val target: Player = Bukkit.getPlayer(args[1])!!
                    player.sendMessage("Der Spieler " + target.name + " hat den " + rankData[target.rlgPlayer().rank]!!.name + "-Rank")
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
        if (player1.rlgPlayer().isMod) {
            if (args.size == 1) {
                val list: MutableList<String> = ArrayList()
                list.add("set")
                list.add("info")
                return list
            } else if (args[0].equals("set", ignoreCase = true)) {
                if (args.size == 3) {
                    val list: MutableList<String> = ArrayList()
                    rankData.keys.forEach {
                        list.add(rankData[it]!!.name)
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
    val newRemainingClaims = currentClaims + (rankData[rank]!!.claims - rankData[currentRank]!!.claims)
    val newRemainingHomes = currentHomes + (rankData[rank]!!.homes - rankData[currentRank]!!.homes)
    transaction {
        PlayersTable.update(where = {PlayersTable.uuid eq player.uniqueId.toString()}){
            it[remainingClaims] = newRemainingClaims
            it[remainingHomes] = newRemainingHomes
            it[PlayersTable.rank] = rank
        }
    }
    rlgPlayer.rank = rank
    rlgPlayer.remainingClaims = newRemainingClaims
    rlgPlayer.remainingHomes = newRemainingHomes
    rlgPlayer.isMod = rankData[rank]!!.isMod
    rlgPlayer.setName()
    player.givePerms()
}

fun setRank(uuid: String, rank: Int) {
    transaction {
        val data = PlayersTable.select(where = {PlayersTable.uuid eq uuid}).first()
        val currentRank = data[PlayersTable.rank]
        val currentClaims = data[PlayersTable.remainingClaims]
        val currentHomes = data[PlayersTable.remainingHomes]
        val newRemainingClaims = currentClaims + (rankData[rank]!!.claims - rankData[currentRank]!!.claims)
        val newRemainingHomes = currentHomes + (rankData[rank]!!.homes - rankData[currentRank]!!.homes)
        PlayersTable.update(where = {PlayersTable.uuid eq uuid}){
            it[remainingClaims] = newRemainingClaims
            it[remainingHomes] = newRemainingHomes
            it[PlayersTable.rank] = rank
        }
    }
}