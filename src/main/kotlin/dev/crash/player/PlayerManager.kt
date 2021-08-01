package dev.crash.player

import dev.crash.*
import dev.crash.permission.givePerms
import dev.crash.permission.ranks
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate

private val PlayerData: HashMap<Player, RLGPlayer> = HashMap()

fun Player.rlgPlayer() : RLGPlayer {
    if(!PlayerData.containsKey(this)) {
        println("loading player ${this.name}")
        this.load()
    }
    return PlayerData[this]!!
}

fun Player.load(){
    val player = this
    if(PlayerData.containsKey(player)) return
    transaction {
        val queryResult = PlayersTable.select(where = { PlayersTable.uuid eq player.uniqueId.toString() })
        if (!queryResult.empty()) {
            val result = queryResult.first()
            val homes = HashMap<String, Block>()
            HomepointTable.select(where = { HomepointTable.uuid eq player.uniqueId.toString() }).forEach {
                homes[it[HomepointTable.keyword]] = getBlockByPositionString(it[HomepointTable.homePos])
            }
            val rlgPlayer = RLGPlayer(
                player,
                result[PlayersTable.rank],
                result[PlayersTable.remainingClaims],
                homes,
                result[PlayersTable.remainingHomes],
                result[PlayersTable.balance],
                result[PlayersTable.quests],
                result[PlayersTable.questStatus],
                result[PlayersTable.questProgress],
                result[PlayersTable.xpLevel],
                result[PlayersTable.xp],
                result[PlayersTable.vxpLevel],
                result[PlayersTable.guildId]
            )
            if (rlgPlayer.isMod) moderator.add(player)
            PlayerData[player] = rlgPlayer
            player.givePerms()
            if(result[PlayersTable.lastWeeklyQuest].isBefore(LocalDate.now().minusDays(6))){
                weeklyQuestCreation(player)
            } else if(result[PlayersTable.lastDailyQuest].isBefore(LocalDate.now())){
                dailyQuestCreation(player)
            }
        } else {
            PlayersTable.insert {
                it[uuid] = player.uniqueId.toString()
            }
            val rlgPlayer = RLGPlayer(
                player, 0, ranks[0]!!.claims, HashMap(), ranks[0]!!.homes, 0,
                "1 2 3 1 2 3", "0 0 0 0 0 0 0 0", "0 0 0 0 0 0", 0, 0, 0, 0
            )
            PlayerData[player] = rlgPlayer
            player.teleport(warps["spawn"]!!)
            player.givePerms()
        }
    }
}

fun Player.unload(){
    val player = this
    if(!PlayerData.containsKey(player)) return
    val rlgPlayer = player.rlgPlayer()
    rlgPlayer.managen?.cancel()
    val quests: List<Quest> = rlgPlayer.quests
    val questStatusBuilder = StringBuilder()
    val questProgressBuilder = StringBuilder()
    for (i in 0..5) {
        val quest = quests[i]
        questStatusBuilder.append(quest.status.toString() + " ")
        questProgressBuilder.append(quest.counter.toString() + " ")
        if (i == 2) {
            questStatusBuilder.append(if (rlgPlayer.hasDaily) 2 else 0).append(" ")
        } else if (i == 5) {
            questStatusBuilder.append(if (rlgPlayer.hasWeekly) 2 else 0).append(" ")
        }
    }
    transaction {
        PlayersTable.update(where = {PlayersTable.uuid eq player.uniqueId.toString()}){
            it[questStatus] = questStatusBuilder.toString()
            it[questProgress] = questProgressBuilder.toString()
            it[vxpLevel] = rlgPlayer.vxpLevel
            it[xpLevel] = rlgPlayer.xpLevel
            it[xp] = rlgPlayer.xp
        }
    }
    PlayerData.remove(player)
}

fun clearPlayerData() = PlayerData.clear()