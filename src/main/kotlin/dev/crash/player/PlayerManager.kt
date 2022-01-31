package dev.crash.player

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crash.*
import dev.crash.permission.givePerms
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

private val PlayerData: HashMap<Player, CrashPlayer> = HashMap()

fun Player.crashPlayer() : CrashPlayer {
    if(!PlayerData.containsKey(this)) {
        println("loading player ${this.name}")
        this.load()
    }
    return PlayerData[this]!!
}

data class PlayerSaveData(val uuid: String, var quests: MutableList<QuestSaveObj>, var hasDaily: Boolean,
                          var hasWeekly: Boolean, var lastDailyQuest: Long, var lastWeeklyQuest: Long, var lastKeys: Long, var leftKeys: HashMap<Int, Int>,
                          var homepoints: HashMap<String, String>, var warns: MutableList<Warn>)
data class QuestSaveObj(var qid: Int, var status: Int, var progress: Int, var isDaily: Boolean)
data class Warn(val reason: String, val modName: String, val time: Long)

internal fun Player.load(){
    val player = this
    if(PlayerData.containsKey(player)) return
    val saveFile = File("${INSTANCE.dataFolder.path}/player/${player.uniqueId}.json")
    if(!saveFile.exists()){
        saveFile.createNewFile()
        val playerSaveData = CrashPlayer.getDefaultPlayerSaveData(player)
        if(warps.containsKey(CONFIG.defaultWarpName)) player.teleport(warps[CONFIG.defaultWarpName]!!)
        transaction {
            PlayerTable.insert {
                it[uuid] = player.uniqueId.toString()
            }
        }
        jacksonObjectMapper().writeValue(saveFile, playerSaveData)
    }
    val crashPlayer = CrashPlayer(player)
    PlayerData[player] = crashPlayer
    if(Instant.ofEpochMilli(crashPlayer.lastWeeklyQuest).isBefore(Instant.now().minus(7, ChronoUnit.DAYS))){
        crashPlayer.weeklyQuestCreation()
    }else if(Instant.ofEpochMilli(crashPlayer.lastDailyQuest).isBefore(Instant.now().minus(1, ChronoUnit.DAYS))){
        if(crashPlayer.quests.size < 6){
            crashPlayer.weeklyQuestCreation()
        }else {
            crashPlayer.dailyQuestCreation()
        }
    }else if(crashPlayer.quests.size < 6){
        crashPlayer.weeklyQuestCreation()
    }
    player.givePerms()
}

internal fun Player.unload(){
    if(!PlayerData.containsKey(this)) return
    val crashPlayer = crashPlayer()
    crashPlayer.manaGen?.cancel()
    crashPlayer.save()
    PlayerData.remove(this)
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