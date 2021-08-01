package dev.crash.player

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crash.*
import dev.crash.permission.givePerms
import dev.crash.permission.ranks
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit

private val PlayerData: HashMap<Player, RLGPlayer> = HashMap()

fun Player.rlgPlayer() : RLGPlayer {
    if(!PlayerData.containsKey(this)) {
        println("loading player ${this.name}")
        this.load()
    }
    return PlayerData[this]!!
}

data class PlayerSaveData(val uuid: String, var rank: Int, var remainingClaims: Int, var sharedClaims: List<String>, var remainingHomes: Int,
                          var addedClaims: Int, var addedHomes: Int, var balance: Long, var quests: List<QuestSaveObj>, var hasDaily: Boolean,
                          var hasWeekly: Boolean, var lastDailyQuest: Long, var lastWeeklyQuest: Long, var xpLevel: Int, var xp: Long, var vxpLevel: Int,
                          var guildId: Int, var lastKeys: Long, var leftKeys: HashMap<Int, Int>, var homepoints: HashMap<String, String>,
                          var warns: List<Warn>)
data class QuestSaveObj(var qid: Int, var status: Int, var progress: Int, var isDaily: Boolean)
data class Warn(val reason: String, val modName: String, val time: Long)
// "${INSTANCE.dataFolder.path}/player/"
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
    }else {
        saveFile.createNewFile()
        val playerSaveData = PlayerSaveData(player.uniqueId.toString(), 0, ranks[0]!!.claims, listOf(), ranks[0]!!.homes, 0, 0, 0,
            listOf(), false, false, System.currentTimeMillis(), System.currentTimeMillis(), 0, 0, 0, 0, 0,
            ranks[0]!!.weeklyKeys, hashMapOf(), listOf())
        jacksonObjectMapper().writeValue(saveFile, playerSaveData)
        val rlgPlayer = RLGPlayer(player)
        PlayerData[player] = rlgPlayer
        weeklyQuestCreation(player)
        player.teleport(warps["spawn"]!!)
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