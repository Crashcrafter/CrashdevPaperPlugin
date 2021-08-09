package dev.crash

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vexsoftware.votifier.model.Vote
import kotlinx.coroutines.Job
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//CONST DATA
val warps = hashMapOf<String, Location>()
var dropRange = 10000
val customItemsMap = HashMap<String, ItemStack>()
var dropWardenName = "§4§lDrop Warden"

//VARIABLE DATA
val amount_Sleeping: ArrayList<Player> = ArrayList()
val targetMap = HashMap<UUID, UUID>()
val portals = HashMap<Block, String>()
var keyChests = HashMap<Block, Int>()
val tradingInventoryCopies: ArrayList<Inventory> = ArrayList()
var questinventories: MutableList<Inventory> = java.util.ArrayList()
val moderator: ArrayList<Player> = ArrayList()
val cachedVoteRewards: ArrayList<Vote> = ArrayList()
val allJobs: ArrayList<Job> = ArrayList()
var lastUpdate: Date? = null

//region NatureBlocks
var natureBlocks: List<Material> = ArrayList(
    listOf(
        Material.GRASS,
        Material.TALL_GRASS,
        Material.LILY_PAD,
        Material.CORNFLOWER,
        Material.SUNFLOWER,
        Material.POPPY,
        Material.ORANGE_TULIP,
        Material.PINK_TULIP,
        Material.RED_TULIP,
        Material.WHITE_TULIP,
        Material.OXEYE_DAISY,
        Material.LILY_OF_THE_VALLEY,
        Material.AZURE_BLUET,
        Material.ALLIUM,
        Material.DANDELION,
        Material.BLUE_ORCHID,
        Material.FERN,
        Material.LARGE_FERN,
        Material.ACACIA_LEAVES,
        Material.BIRCH_LEAVES,
        Material.OAK_LEAVES,
        Material.DARK_OAK_LEAVES,
        Material.JUNGLE_LEAVES,
        Material.SPRUCE_LEAVES
    )
)
//endregion

data class PluginConfig(val dbUser: String, val dbPw: String, val dbIp: String, val dbName: String, val dcLink: String,
                        val texturePackURL: String, val texturePackHash: String, val defaultWarpName: String, val scoreBoardTitle: String, val scoreBoardNews: String,
                        val playerListFooter: String)
private val DEFAULT_CONFIG = PluginConfig("NOTSET", "NOTSET", "localhost", "mcplugin", "https://discord.gg/NbW6JVvxY7",
    "", "", "spawn", Bukkit.getIp(), "", "")
lateinit var CONFIG: PluginConfig

internal fun loadPluginConfig(){
    val configFile = File(INSTANCE.dataFolder.path + "/config.json")
    if(configFile.exists()){
        CONFIG = jacksonObjectMapper().readValue(configFile)
    }else {
        configFile.createNewFile()
        CONFIG = DEFAULT_CONFIG
        jacksonObjectMapper().writeValue(configFile, DEFAULT_CONFIG)
    }
}