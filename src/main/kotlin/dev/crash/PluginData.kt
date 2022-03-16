package dev.crash

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vexsoftware.votifier.model.Vote
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*
import kotlin.collections.HashMap

//CONST DATA
val warps = hashMapOf<String, Location>()
var dropRange = 10000
val customItemsMap = HashMap<String, ItemStack>()
var dropWardenName = "§4§lDrop Warden"

//VARIABLE DATA
val targetMap = HashMap<UUID, UUID>()
val portals = HashMap<Block, String>()
var keyChests = HashMap<Block, Int>()
val tradingInventoryCopies: MutableList<Inventory> = mutableListOf()
var questInvs: MutableList<Inventory> = mutableListOf()
val moderator: MutableList<Player> = mutableListOf()
val cachedVoteRewards: MutableList<Vote> = mutableListOf()
val allJobs: MutableList<Job> = mutableListOf()

data class PluginConfig(val dbUser: String, val dbPw: String, val dbIp: String, val dbName: String, val dcLink: String,
                        val texturePackURL: String, val texturePackHash: String, val defaultWarpName: String, val scoreBoardTitle: String, val scoreBoardNews: String,
                        val playerListFooter: String, val votifierEnabled: Boolean, val voteLinks: MutableList<String>)
private val DEFAULT_CONFIG = PluginConfig("NOT SET", "NOT SET", "localhost", "crashdev_mc", "https://discord.gg/NbW6JVvxY7",
    "", "", "spawn", Bukkit.getIp(), "", "", false, mutableListOf())
lateinit var CONFIG: PluginConfig

internal fun loadPluginConfig(){
    if(!File(INSTANCE.dataFolder.path).exists()) File(INSTANCE.dataFolder.path).mkdirs()
    val configFile = File(INSTANCE.dataFolder.path + "/config.json")
    CONFIG = if(configFile.exists()){
        try {
            jacksonObjectMapper().readValue(configFile)
        }catch (ex: MismatchedInputException){
            jacksonObjectMapper().readTree(configFile).run {
                val votes = this["votes"].elements()
                val voteList = mutableListOf<String>()
                votes.forEach {
                    voteList.add(it.asText())
                }
                PluginConfig(getStringOrDefault("dbUser", DEFAULT_CONFIG.dbUser), getStringOrDefault("dbPw", DEFAULT_CONFIG.dbPw),
                    getStringOrDefault("dbIp", DEFAULT_CONFIG.dbIp), getStringOrDefault("dbName", DEFAULT_CONFIG.dbName),
                    getStringOrDefault("dcLink", DEFAULT_CONFIG.dcLink), getStringOrDefault("texturePackURL", DEFAULT_CONFIG.texturePackURL),
                    getStringOrDefault("texturePackHash", DEFAULT_CONFIG.texturePackHash), getStringOrDefault("defaultWarpName", DEFAULT_CONFIG.defaultWarpName),
                    getStringOrDefault("scoreBoardTitle", DEFAULT_CONFIG.scoreBoardTitle), getStringOrDefault("scoreBoardNews", DEFAULT_CONFIG.scoreBoardNews),
                    getStringOrDefault("playerListFooter", DEFAULT_CONFIG.playerListFooter), getBooleanOrDefault("votifierEnabled", DEFAULT_CONFIG.votifierEnabled),
                    voteList
                )
            }
        }
    }else {
        configFile.createNewFile()
        DEFAULT_CONFIG
    }
    jacksonObjectMapper().writeValue(configFile, CONFIG)
}