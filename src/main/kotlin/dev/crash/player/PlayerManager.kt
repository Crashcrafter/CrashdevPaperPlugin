package dev.crash.player

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crash.*
import dev.crash.permission.givePerms
import dev.crash.permission.ranks
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private val PlayerData: HashMap<Player, RLGPlayer> = HashMap()

fun Player.rlgPlayer() : RLGPlayer {
    if(!PlayerData.containsKey(this)) {
        println("loading player ${this.name}")
        this.load()
    }
    return PlayerData[this]!!
}

data class PlayerSaveData(val uuid: String, var rank: Int, var remainingClaims: Int, var sharedClaims: MutableList<String>, var remainingHomes: Int,
                          var addedClaims: Int, var addedHomes: Int, var balance: Long, var quests: MutableList<QuestSaveObj>, var hasDaily: Boolean,
                          var hasWeekly: Boolean, var lastDailyQuest: Long, var lastWeeklyQuest: Long, var xpLevel: Int, var xp: Long, var vxpLevel: Int,
                          var guildId: Int, var lastKeys: Long, var leftKeys: HashMap<Int, Int>, var homepoints: HashMap<String, String>,
                          var warns: MutableList<Warn>, var chunks: MutableList<String>)
data class QuestSaveObj(var qid: Int, var status: Int, var progress: Int, var isDaily: Boolean)
data class Warn(val reason: String, val modName: String, val time: Long)

internal fun Player.load(){
    val player = this
    if(PlayerData.containsKey(player)) return
    val saveFile = File("${INSTANCE.dataFolder.path}/player/${player.uniqueId}.json")
    if(saveFile.exists()){
        val saveObj = jacksonObjectMapper().readValue<PlayerSaveData>(saveFile)
        val homes = hashMapOf<String, Block>()
        saveObj.homepoints.forEach {
            homes[it.key] = getBlockByPositionString(it.value)
        }
        val rlgPlayer = RLGPlayer(player)
        if (rlgPlayer.isMod) moderator.add(player)
        PlayerData[player] = rlgPlayer
        player.givePerms()
        if(Instant.ofEpochMilli(saveObj.lastWeeklyQuest).isBefore(Instant.now().minus(7, ChronoUnit.DAYS))){
            weeklyQuestCreation(player)
        }else if(Instant.ofEpochMilli(saveObj.lastDailyQuest).isBefore(Instant.now().minus(1, ChronoUnit.DAYS))){
            dailyQuestCreation(player)
        }
        if(Instant.ofEpochMilli(saveObj.lastKeys).isBefore(Instant.now().minus(7, ChronoUnit.DAYS))){
            player.sendMessage("§2Du kannst mit /weekly deine wöchentlichen Keys abholen!")
        }
        if(rlgPlayer.quests.size < 6){
            weeklyQuestCreation(player)
        }
    }else {
        saveFile.createNewFile()
        val playerSaveData = PlayerSaveData(player.uniqueId.toString(), 0, ranks[0]!!.claims, mutableListOf(), ranks[0]!!.homes, 0, 0, 0,
            mutableListOf(), false, false, System.currentTimeMillis(), System.currentTimeMillis(), 0, 0, 0, 0, 0,
            ranks[0]!!.weeklyKeys, hashMapOf(), mutableListOf(), mutableListOf())
        transaction {
            val query = PlayersTable.select(where = {PlayersTable.uuid eq player.uniqueId.toString()})
            if(!query.empty()){
                val it = query.first()
                playerSaveData.rank = it[PlayersTable.rank]
                playerSaveData.remainingClaims = it[PlayersTable.remainingClaims]
                playerSaveData.addedClaims = it[PlayersTable.addedClaims]
                playerSaveData.remainingHomes = it[PlayersTable.remainingHomes]
                playerSaveData.balance = it[PlayersTable.balance]
                val questString = it[PlayersTable.quests].split(" ")
                val questStatus = it[PlayersTable.questStatus].split(" ")
                val questProgress = it[PlayersTable.questProgress].split(" ")
                for(i in questString.indices){
                    if(i == 3) {
                        playerSaveData.hasDaily = questStatus[i].toInt() == 2
                        continue
                    }else if(i == 7) {
                        playerSaveData.hasDaily = questStatus[i].toInt() == 2
                        continue
                    }
                    val j = if(i > 3) i+1 else i
                    playerSaveData.quests.add(QuestSaveObj(questString[i].toInt(), questStatus[j].toInt(), questProgress[i].toInt(), i < 3))
                }
                playerSaveData.lastDailyQuest = it[PlayersTable.lastDailyQuest].atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                playerSaveData.lastWeeklyQuest = it[PlayersTable.lastWeeklyQuest].atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                playerSaveData.xp = it[PlayersTable.xp]
                playerSaveData.xpLevel = it[PlayersTable.xpLevel]
                playerSaveData.vxpLevel = it[PlayersTable.vxpLevel]
                playerSaveData.guildId = it[PlayersTable.guildId]
                val homes = hashMapOf<String, String>()
                HomepointTable.select(where = {HomepointTable.uuid eq player.uniqueId.toString()}).forEach {
                    homes[it[HomepointTable.keyword]] = it[HomepointTable.homePos]
                }
                playerSaveData.homepoints = homes
                val processedResult = ProcessedTable.select(where = {ProcessedTable.uuid eq player.uniqueId.toString()})
                if(!processedResult.empty()){
                    val processed = processedResult.first()
                    playerSaveData.lastKeys = processed[ProcessedTable.lastTime].atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                    val keys = processed[ProcessedTable.leftKeys].split(" ")
                    keysData.forEach {
                        val amount = if(keys.size <= it.key) 0 else keys[it.key].toInt()
                        playerSaveData.leftKeys[it.key] = amount
                    }
                }
                ChunkTable.select(where = {ChunkTable.uuid eq player.uniqueId.toString()}).forEach {
                    val worldName = it[ChunkTable.world]
                    val chunkKey = Chunk.getChunkKey(it[ChunkTable.x], it[ChunkTable.z])
                    playerSaveData.chunks.add("$worldName:$chunkKey")
                }
            }else {
                player.teleport(warps["spawn"]!!)
            }
        }
        jacksonObjectMapper().writeValue(saveFile, playerSaveData)
        val rlgPlayer = RLGPlayer(player)
        PlayerData[player] = rlgPlayer
        if(Instant.ofEpochMilli(rlgPlayer.lastWeeklyQuest).isBefore(Instant.now().minus(7, ChronoUnit.DAYS))){
            weeklyQuestCreation(player)
        }else if(Instant.ofEpochMilli(rlgPlayer.lastDailyQuest).isBefore(Instant.now().minus(1, ChronoUnit.DAYS))){
            dailyQuestCreation(player)
        }
        if(rlgPlayer.quests.size < 6){
            weeklyQuestCreation(player)
        }
        player.givePerms()
    }
}

internal fun Player.unload(){
    val player = this
    if(!PlayerData.containsKey(player)) return
    val rlgPlayer = player.rlgPlayer()
    rlgPlayer.managen?.cancel()
    rlgPlayer.save()
    PlayerData.remove(player)
}

internal fun clearPlayerData() = PlayerData.clear()

fun modifyPlayerData(uuid: String, f: (PlayerSaveData) -> PlayerSaveData){
    f.invoke(getPlayerData(uuid)).save()
}

fun getPlayerData(uuid: String): PlayerSaveData {
    val saveFile = File("${INSTANCE.dataFolder.path}/player/${uuid}.json")
    return jacksonObjectMapper().readValue(saveFile)
}

private fun PlayerSaveData.save(){
    val saveFile = File("${INSTANCE.dataFolder.path}/player/${uuid}.json")
    jacksonObjectMapper().writeValue(saveFile, this)
}