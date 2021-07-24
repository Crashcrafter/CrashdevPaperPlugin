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

//CONST DATA
lateinit var spawn: Location
lateinit var event: Location
const val texturePackUrl = "https://drive.google.com/uc?export=download&id=11hodrHTq9L9E4F2pn7QF9s-Jr0-z1IBk"
const val texturePackHash = "6AA010DD8D1B373B98550F69E03503AC5BD5DEDA"
const val DropRange = 10000
val customItemsMap = HashMap<String, ItemStack>()
const val dcLink = "https://discord.gg/qQtaYsDN6w"
const val dropName = "§4§lDrop Warden"

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
var btcPrice: Int? = null
var ltcPrice: Int? = null
var ethPrice: Int? = null
var nanoPrice: Int? = null
var dogePrice: Int? = null
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

//region BasicMagicBook
var basicmagicbook = arrayOf(
    "§l§nBasiswissen Magie§r\n\n" +
            "§n1. Rezepte§r\n\n" +
            "§n2. Welt§r\n\n" +
            "§n3. Zauberstäbe§r",
    ("§l§nRezepte§r\n" +
            "\n" +
            "§nMana Dust:§r\n\n" +
            "§nA | A | A§r A: Mana Shard\n" +
            "§nA | B | A§r B: Redstone\n" +
            "A | A | A\n" +
            "\n" +
            "§nMana Crystal:§r\n\n" +
            "§nA | B | A§r A: Mana Dust\n" +
            "§nB | C | B§r B: Diamond\n" +
            "A | B | A C: End Crystal"),
    ("§l§nWelt§r\n\n" +
            "Mana Shards und Elemente kannst du in der Welt in Drops finden. Momentan können Drops nur von X/Z -" + DropRange + " bis " + DropRange + " gefunden werden."),
    ("§l§nZauberstäbe§r\n\n" +
            "§nElemente finden\n" +
            "§nFeuer§r\n" +
            "§nWasser§r\n" +
            "§nChaos§r\n" +
            "§nNatur§r\n" +
            "§nWetter§r\n"),
    ("§l§nWie man Elemente findet§r\n\n" +
            "Es gibt für jeden Zauberstab ein entsprechendes Element. Diese Elemente sind sehr selten und können nur in Drops gefunden werden.\n" +
            "Die Elemente sind ausschließlich in Drops zu finden."),
    ("§l§nFeuer§r\n\n" +
            "§nCrafting:§r\n\n" +
            "§n   | C | A§r\n" +
            "§nE | B | C§r\n" +
            "D | E |   \n\n" +
            "A: Feuer-Element\n" +
            "B: Mana-Crystal\n" +
            "C: Mana-Dust\n" +
            "D: Blaze Rod\n" +
            "E: Nether Bricks"),
    ("§l§nWasser§r\n\n" +
            "§nCrafting:§r\n\n" +
            "§n   | C | A§r\n" +
            "§nE | B | C§r\n" +
            "D | E |   \n\n" +
            "A: Wasser-Element\n" +
            "B: Mana-Crystal\n" +
            "C: Mana-Dust\n" +
            "D: Stick\n" +
            "E: Nautilusschalen"),
    ("§l§nChaos§r\n\n" +
            "§nCrafting:§r\n\n" +
            "§n   | C | A§r\n" +
            "§nE | B | C§r\n" +
            "D | E |   \n\n" +
            "A: Chaos-Element\n" +
            "B: Mana-Crystal\n" +
            "C: Mana-Dust\n" +
            "D: Stick\n" +
            "E: Obsidian"),
    ("§l§nNatur§r\n\n" +
            "§nCrafting:§r\n\n" +
            "§n   | C | A§r\n" +
            "§n   | B | C§r\n" +
            "D |    |   \n\n" +
            "A: Natur-Element\n" +
            "B: Mana-Crystal\n" +
            "C: Mana-Dust\n" +
            "D: Stick"),
    ("§l§nWetter§r\n\n" +
            "§nCrafting:§r\n\n" +
            "§n   | C | A§r\n" +
            "§n   | B | C§r\n" +
            "D |    |   \n\n" +
            "A: Wetter-Element\n" +
            "B: Mana-Crystal\n" +
            "C: Mana-Dust\n" +
            "D: Stick")
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