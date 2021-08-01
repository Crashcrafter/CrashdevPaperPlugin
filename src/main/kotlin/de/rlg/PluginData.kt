package de.rlg

import com.vexsoftware.votifier.model.Vote
import kotlinx.coroutines.Job
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//CONST DATA
val warps = hashMapOf<String, Location>()
const val texturePackUrl = "https://drive.google.com/uc?export=download&id=1PE5Lynv4otTJwzzI_cuj3bYTYFnuJdJh"
const val texturePackHash = "7C1C1A920C874DCE2A64911772F9F696053CF2EC"
var dropRange = 10000
val customItemsMap = HashMap<String, ItemStack>()
const val dcLink = "https://discord.gg/qQtaYsDN6w"
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

//region Beginner Book
var beginnerbook = arrayOf(
    ("\n~~~~~~~~~~~~~~~~\n" +
            "All unsere Regeln findest du auf unserem Discord (/discord). Falls du Fragen hast, frag einfach im Chat. " +
            "Das Buch aktualisiert sich von selbst, du musst es nicht neu hohlen.\n\n" +
            "~~~~~~~~~~~~~~~~"),
    ("\n~~~~~~~~~~~~~~~~\n" +
            "MCGermany ist ein Survivalserver mit einem Claim-System. Lauf einfach aus dem Spawn und such die ein schönes Plätzchen. " +
            "Sobald du einen Platz zum Bauen gefunden hast, drücke F3+G, um die Chunkgrenzen zu sehen.\n" +
            "~~~~~~~~~~~~~~~~"),
    ("\n~~~~~~~~~~~~~~~~\n" +
            "Du kannst als normales Mitglied 4 Chunks claimen. Nutze einfach den Befehl /claim, um einen Chunk zu claimen. " +
            "Um zu überprüfen, ob ein Chunk einem anderen Spieler bereits gehört, nutze /claim info.\n" +
            "~~~~~~~~~~~~~~~~")
)
//endregion