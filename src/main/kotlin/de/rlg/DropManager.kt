package de.rlg

import de.rlg.items.CustomItems
import de.rlg.permission.*
import de.rlg.player.rlgPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.collections.HashMap

var drops = HashMap<Chunk, Drop>()
private var waves = HashMap<Int, HashMap<Int, List<EntityType>>>()
var canDropStart = true

fun setDrop(chunk: Chunk, type: Int) {
    if (!chunks.containsKey(chunk)) {
        val world = chunk.world
        if (world.name.contentEquals(Bukkit.getWorlds()[0].name)) {
            val x = chunk.x * 16 + 8
            val z = chunk.z * 16 + 8
            val y = world.getHighestBlockYAt(x, z) - 29
            val block0 = world.getBlockAt(x, y + 29, z)
            if (block0.type == Material.WATER) return
            val m: Material = when (type) {
                0 -> Material.GRAY_STAINED_GLASS
                1 -> Material.LIGHT_BLUE_STAINED_GLASS
                2 -> Material.BLUE_STAINED_GLASS
                3 -> Material.PURPLE_STAINED_GLASS
                4 -> Material.YELLOW_STAINED_GLASS
                else -> return
            }
            world.getBlockAt(x, y, z).type = Material.BEACON
            for (i in 0..29) world.getBlockAt(x, y + 1 + i, z).type = m
            for (xPoint in x - 1..x + 1) {
                for (zPoint in z - 1..z + 1) {
                    world.getBlockAt(xPoint, y - 1, zPoint).type = Material.IRON_BLOCK
                }
            }
            val block = world.getBlockAt(x, y + 30, z)
            block.type = Material.CHEST
            if (block.state is Chest) {
                val chest = block.state as Chest
                val inventory = chest.blockInventory
                for (i in 0..13) {
                    val random0 = Random()
                    val slot = random0.nextInt(2)
                    val random1 = Random()
                    val nextint = random1.nextInt(loottables[type]!!.size)
                    val itemStack = loottables[type]!![nextint]
                    try {
                        inventory.setItem(slot + i * 2, itemStack)
                    } catch (ignored: Exception) {
                    }
                }
                drops[chunk] = Drop(type, block.location)
                chunk.claim((type+1).toString(), "Server-Team", null)
            }
        } else {
            println("Drop wäre in falscher Welt gespawnt")
        }
    } else {
        println("Drop wäre im Claim gespawnt")
    }
}

fun unsetDrop(chunk: Chunk, alsoLoot: Boolean) {
    val world = chunk.world
    val x = chunk.x * 16 + 8
    val z = chunk.z * 16 + 8
    val y = world.getHighestBlockYAt(x, z) - 30
    val replaceBlock = Material.STONE
    world.getBlockAt(x, y, z).type = replaceBlock
    for (i in 0..28) {
        world.getBlockAt(x, y + 1 + i, z).type = world.getBlockAt(x, y + 1 + i, z + 1).type
    }
    for (xPoint in x - 1..x + 1) {
        for (zPoint in z - 1..z + 1) {
            world.getBlockAt(xPoint, y - 1, zPoint).type = replaceBlock
        }
    }
    if (alsoLoot) {
        val block = world.getBlockAt(x, y + 30, z)
        if (block.type == Material.CHEST) {
            (block.state as Chest).blockInventory.clear()
            block.type = Material.AIR
        }
    }
    chunk.unClaim()
    try { drops[chunk]!!.waveManager!!.cancel() }catch (ex: NullPointerException) {}
    drops.remove(chunk)
}

fun initDrops() {
    DropLoottables.setupCommon()
    DropLoottables.setupUncommon()
    DropLoottables.setupRare()
    DropLoottables.setupEpic()
    DropLoottables.setupSupreme()
    loottables[0] = DropLoottables.Common
    loottables[1] = DropLoottables.Uncommon
    loottables[2] = DropLoottables.Rare
    loottables[3] = DropLoottables.Epic
    loottables[4] = DropLoottables.Supreme
    Waves.setupCommonWaves()
    Waves.setupUncommonWaves()
    Waves.setupRareWaves()
    Waves.setupEpicWaves()
    Waves.setupSupremeWaves()
    canDropStart = true
}

fun waveManager(chunk: Chunk) {
    if (!canDropStart) {
        println("[WARN] §4Cannot start Raid! Drops disabled")
        return
    }
    val drop = drops[chunk]
    if (drop == null) {
        println("[WARN] §4Drop ist null at " + chunk.x + "/" + chunk.z)
        recoverDrop(chunk)
        return
    }
    if (!drop.started) {
        if (chunks.containsKey(chunk)) {
            val chunkClass: ChunkClass = chunks[chunk]!!
            if (chunkClass.owner_uuid.contentEquals("0")) {
                drops.remove(chunk)
                return
            }
        }
        drop.started = true
        val location = drops[chunk]!!.location
        try {
            location.add(0.0, 1.0, 0.0)
        } catch (ignored: NullPointerException) {
            recoverDrop(chunk)
            return
        }
        val players = location.getNearbyLivingEntities(100.0)
        for (entity in players) {
            if (entity is Player) {
                if (!drop.participatingPlayer.contains(entity)) {
                    drop.participatingPlayer.add(entity)
                    entity.playSound(location, "rlg.drop.music", 2f, 1f)
                }
            }
        }
        for (player in drop.participatingPlayer) {
            player.playSound(location, "rlg.drop.music", 2f, 1f)
        }
        val job = GlobalScope.launch {
            delay(2500)
            while (true) {
                delay(5000)
                if (drop.entities.size == 0) {
                    if (drop.wave > drop.dropWaves.size) {
                        object : BukkitRunnable() {
                            override fun run() {
                                unsetDrop(chunk, false)
                                if (drop.participatingPlayer.size == 1 && drop.type == 3) {
                                    questCount(drop.participatingPlayer[0], 4, 1, true)
                                }
                                for (player in drop.participatingPlayer) {
                                    try {
                                        player.stopSound("rlg.drop.musicfinal")
                                        player.stopSound("rlg.drop.music")
                                        player.playSound(drop.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, 5f, 1f)
                                        when(drop.type){
                                            0 -> questCount(player, 15, 1, true)
                                            1 -> questCount(player, 1, 1, true)
                                            3 -> questCount(player, 10, 1, true)
                                        }
                                        questCount(player, 7, 1, true)
                                        questCount(player, 5, 1, false)
                                    } catch (ignored: NullPointerException) { }
                                }
                            }
                        }.runTask(INSTANCE)
                        break
                    }
                    drop.spawnWave()
                } else {
                    val dead: MutableList<Entity> = ArrayList()
                    for (entity in drop.entities) {
                        if (entity.isDead) {
                            dead.add(entity)
                            try {
                                val player = (entity as LivingEntity).killer!!
                                if (!drop.participatingPlayer.contains(player)) drop.participatingPlayer.add(player)
                            } catch (ignored: NullPointerException) { }
                        }
                    }
                    for (entity in dead) {
                        drop.entities.remove(entity)
                    }
                }
            }
        }
        allJobs.add(job)
        drop.waveManager = job
    }
}

fun Drop.spawnWave() {
    val drop = this
    if (drop.type >= 3 && drop.dropWaves.size == drop.wave + 1) {
        for (player in drop.participatingPlayer) {
            player.stopSound("rlg.drop.music")
            player.playSound(location, "rlg.drop.musicfinal", 2f, 1f)
        }
    }
    drop.dropWaves[drop.wave]!!.forEach {
        try {
            Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                val x = Random().nextInt(8) - 4
                val z = Random().nextInt(8) - 4
                val world = location.world
                val entity = world.spawnEntity(world.getHighestBlockAt(location.blockX + x, location.blockZ + z).location.add(0.5, 1.0, 0.5), it)
                entity.isGlowing = true
                entity.customName = dropName
                entity.isCustomNameVisible = true
                entity.isPersistent = true
                (entity as LivingEntity).removeWhenFarAway = false
                drop.entities.add(entity)
            })
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }
    drop.wave++
}

fun recoverDrop(chunk: Chunk) {
    if (chunks.containsKey(chunk)) {
        val chunkClass: ChunkClass = chunks[chunk]!!
        if (chunkClass.owner_uuid.contentEquals("0")) {
            drops.remove(chunk)
            return
        }
    }
    val x = chunk.x * 16 + 8
    val z = chunk.z * 16 + 8
    val y = chunk.world.getHighestBlockYAt(x, z) - 2
    val block = chunk.world.getBlockAt(x, y, z)
    val type = when (block.type) {
        Material.GRAY_STAINED_GLASS -> 0
        Material.LIGHT_BLUE_STAINED_GLASS -> 1
        Material.BLUE_STAINED_GLASS -> 2
        Material.PURPLE_STAINED_GLASS -> 3
        Material.YELLOW_STAINED_GLASS -> 4
        else -> -1
    }
    println("Recover Drop " + type + " at " + chunk.x + "/" + chunk.z)
    val block1 = block.world.getBlockAt(x, y + 3, z)
    drops[chunk] = Drop(type, block1.location)
}

var loottables = HashMap<Int, List<ItemStack>>()

object DropLoottables {
    var Common: MutableList<ItemStack> = ArrayList()
    fun setupCommon() {
        Common.add(ItemStack(Material.IRON_PICKAXE))
        Common.add(ItemStack(Material.GOLDEN_SWORD))
        Common.add(ItemStack(Material.BOW))
        Common.add(ItemStack(Material.IRON_INGOT, 3))
        Common.add(ItemStack(Material.IRON_INGOT, 2))
        Common.add(ItemStack(Material.GOLD_INGOT))
        Common.add(ItemStack(Material.GOLD_INGOT, 2))
        Common.add(ItemStack(Material.LEATHER, 3))
        Common.add(ItemStack(Material.LEATHER, 2))
        Common.add(ItemStack(Material.BREAD, 4))
        Common.add(ItemStack(Material.BREAD, 3))
        Common.add(ItemStack(Material.BREAD, 5))
        Common.add(ItemStack(Material.BONE, 2))
        Common.add(ItemStack(Material.BONE))
        Common.add(CustomItems.katzeRose())
        Common.add(CustomItems.throwableFireBall())
        Common.add(CustomItems.dogecoin().asQuantity(5))
        Common.add(CustomItems.dogecoin().asQuantity(3))
        Common.add(CustomItems.mudBall().asQuantity(8))
    }

    var Uncommon: MutableList<ItemStack> = ArrayList()
    fun setupUncommon() {
        Uncommon.add(ItemStack(Material.IRON_AXE))
        Uncommon.add(ItemStack(Material.IRON_PICKAXE))
        Uncommon.add(ItemStack(Material.IRON_INGOT, 4))
        Uncommon.add(ItemStack(Material.IRON_INGOT, 3))
        Uncommon.add(ItemStack(Material.GOLD_INGOT, 3))
        Uncommon.add(ItemStack(Material.GOLD_INGOT, 2))
        Uncommon.add(ItemStack(Material.GLASS_BOTTLE, 3))
        Uncommon.add(ItemStack(Material.ARROW, 5))
        Uncommon.add(ItemStack(Material.ARROW, 4))
        Uncommon.add(ItemStack(Material.ARROW, 3))
        Uncommon.add(ItemStack(Material.SPECTRAL_ARROW))
        Uncommon.add(ItemStack(Material.BREAD, 6))
        Uncommon.add(ItemStack(Material.BREAD, 5))
        Uncommon.add(ItemStack(Material.BREAD, 4))
        Uncommon.add(ItemStack(Material.GUNPOWDER, 2))
        Uncommon.add(CustomItems.katzeRose())
        Uncommon.add(CustomItems.nano())
        Uncommon.add(CustomItems.dogecoin().asQuantity(12))
        Uncommon.add(CustomItems.throwableFireBall().asQuantity(3))
        Uncommon.add(CustomItems.mudBall().asQuantity(16))
    }

    var Rare: MutableList<ItemStack> = ArrayList()
    fun setupRare() {
        Rare.add(ItemStack(Material.IRON_SWORD))
        Rare.add(ItemStack(Material.IRON_PICKAXE))
        Rare.add(ItemStack(Material.IRON_INGOT, 7))
        Rare.add(ItemStack(Material.DIAMOND))
        Rare.add(ItemStack(Material.DIAMOND, 2))
        Rare.add(ItemStack(Material.LAPIS_LAZULI, 4))
        Rare.add(ItemStack(Material.LAPIS_LAZULI, 6))
        Rare.add(ItemStack(Material.GOLD_INGOT, 3))
        Rare.add(ItemStack(Material.GOLD_INGOT, 5))
        Rare.add(ItemStack(Material.EMERALD, 2))
        Rare.add(ItemStack(Material.EMERALD))
        Rare.add(ItemStack(Material.BREAD, 10))
        Rare.add(ItemStack(Material.BREAD, 9))
        Rare.add(ItemStack(Material.BREAD, 7))
        Rare.add(ItemStack(Material.IRON_HORSE_ARMOR))
        Rare.add(ItemStack(Material.CHAINMAIL_BOOTS))
        Rare.add(ItemStack(Material.CHAINMAIL_CHESTPLATE))
        Rare.add(ItemStack(Material.CHAINMAIL_HELMET))
        Rare.add(ItemStack(Material.CHAINMAIL_LEGGINGS))
        Rare.add(CustomItems.manaShard())
        Rare.add(CustomItems.nano().asQuantity(2))
        Rare.add(CustomItems.nano().asQuantity(3))
        Rare.add(CustomItems.throwableFireBall().asQuantity(6))
    }

    var Epic: MutableList<ItemStack> = ArrayList()
    fun setupEpic() {
        Epic.add(ItemStack(Material.DIAMOND))
        Epic.add(ItemStack(Material.DIAMOND, 2))
        Epic.add(ItemStack(Material.DIAMOND, 3))
        Epic.add(ItemStack(Material.IRON_INGOT, 7))
        Epic.add(ItemStack(Material.IRON_INGOT, 6))
        Epic.add(ItemStack(Material.IRON_INGOT, 8))
        Epic.add(ItemStack(Material.IRON_BLOCK))
        Epic.add(ItemStack(Material.GOLD_BLOCK))
        Epic.add(ItemStack(Material.LAPIS_BLOCK))
        Epic.add(ItemStack(Material.APPLE, 8))
        Epic.add(ItemStack(Material.APPLE, 9))
        Epic.add(ItemStack(Material.COAL_BLOCK))
        Epic.add(ItemStack(Material.COAL_BLOCK, 2))
        Epic.add(ItemStack(Material.GOLDEN_APPLE))
        Epic.add(ItemStack(Material.DIAMOND_HORSE_ARMOR))
        Epic.add(ItemStack(Material.IRON_HORSE_ARMOR))
        Epic.add(ItemStack(Material.ANCIENT_DEBRIS))
        Epic.add(ItemStack(Material.LAVA_BUCKET))
        Epic.add(CustomItems.manaShard())
        Epic.add(CustomItems.manaShard())
        Epic.add(CustomItems.nano().asQuantity(4))
        Epic.add(CustomItems.throwableFireBall().asQuantity(8))
        Epic.add(CustomItems.additionalClaim())
    }

    var Supreme: MutableList<ItemStack> = ArrayList()
    fun setupSupreme() {
        Supreme.add(ItemStack(Material.NETHERITE_SCRAP))
        Supreme.add(ItemStack(Material.ANCIENT_DEBRIS))
        Supreme.add(ItemStack(Material.DIAMOND_HORSE_ARMOR))
        Supreme.add(ItemStack(Material.DIAMOND_HORSE_ARMOR))
        Supreme.add(ItemStack(Material.DIAMOND, 2))
        Supreme.add(ItemStack(Material.DIAMOND, 4))
        Supreme.add(ItemStack(Material.DIAMOND, 4))
        Supreme.add(ItemStack(Material.EMERALD, 2))
        Supreme.add(ItemStack(Material.EMERALD, 4))
        Supreme.add(ItemStack(Material.EMERALD, 4))
        Supreme.add(ItemStack(Material.IRON_BLOCK, 2))
        Supreme.add(ItemStack(Material.LAPIS_BLOCK, 2))
        Supreme.add(ItemStack(Material.LAPIS_BLOCK, 2))
        Supreme.add(ItemStack(Material.OBSIDIAN, 3))
        Supreme.add(ItemStack(Material.OBSIDIAN))
        Supreme.add(ItemStack(Material.OBSIDIAN))
        Supreme.add(ItemStack(Material.BOOK, 4))
        Supreme.add(ItemStack(Material.BOOK, 7))
        Supreme.add(ItemStack(Material.BOOK, 7))
        Supreme.add(ItemStack(Material.GUNPOWDER, 12))
        Supreme.add(ItemStack(Material.GUNPOWDER, 16))
        Supreme.add(ItemStack(Material.GUNPOWDER, 16))
        Supreme.add(ItemStack(Material.FIREWORK_ROCKET, 6))
        Supreme.add(ItemStack(Material.FIREWORK_ROCKET, 6))
        Supreme.add(ItemStack(Material.FIREWORK_ROCKET, 8))
        Supreme.add(CustomItems.manaShard())
        Supreme.add(CustomItems.chaosElement())
        Supreme.add(CustomItems.fireElement())
        Supreme.add(CustomItems.natureElement())
        Supreme.add(CustomItems.waterElement())
        Supreme.add(CustomItems.weatherElement())
        Supreme.add(CustomItems.throwableFireBall())
        Supreme.add(CustomItems.throwableFireBall())
        Supreme.add(CustomItems.ethereum())
        Supreme.add(CustomItems.litecoin())
        Supreme.add(CustomItems.litecoin())
        Supreme.add(CustomItems.litecoin().asQuantity(2))
        Supreme.add(CustomItems.nano().asQuantity(5))
        Supreme.add(CustomItems.nano().asQuantity(6))
        Supreme.add(CustomItems.nano().asQuantity(7))
        Supreme.add(CustomItems.dogecoin().asQuantity(26))
        Supreme.add(CustomItems.dogecoin().asQuantity(30))
        Supreme.add(CustomItems.dogecoin().asQuantity(32))
        Supreme.add(CustomItems.additionalClaim())
        Supreme.add(CustomItems.additionalClaim())
    }
}

object Waves {
    private var commonWaves = HashMap<Int, List<EntityType>>()
    fun setupCommonWaves() {
        commonWaves[1] = listOf( EntityType.PILLAGER, EntityType.VINDICATOR)
        commonWaves[2] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR)
        waves[0] = commonWaves
    }

    private var uncommonWaves = HashMap<Int, List<EntityType>>()
    fun setupUncommonWaves() {
        uncommonWaves[1] = listOf(EntityType.VINDICATOR, EntityType.VINDICATOR, EntityType.PILLAGER)
        uncommonWaves[2] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.WITCH)
        uncommonWaves[3] = listOf(EntityType.PILLAGER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER)
        waves[1] = uncommonWaves
    }

    private var rareWaves = HashMap<Int, List<EntityType>>()
    fun setupRareWaves() {
        rareWaves[1] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.VINDICATOR)
        rareWaves[2] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER, EntityType.WITCH)
        rareWaves[3] = listOf(EntityType.PILLAGER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER)
        rareWaves[4] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER)
        waves[2] = rareWaves
    }

    private var epicWaves = HashMap<Int, List<EntityType>>()
    fun setupEpicWaves() {
        epicWaves[1] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.VINDICATOR)
        epicWaves[2] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER, EntityType.WITCH)
        epicWaves[3] = listOf(EntityType.PILLAGER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER)
        epicWaves[4] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER, EntityType.RAVAGER)
        epicWaves[5] = listOf(EntityType.EVOKER, EntityType.EVOKER, EntityType.RAVAGER, EntityType.RAVAGER, EntityType.PILLAGER)
        waves[3] = epicWaves
    }

    private var supremeWaves = HashMap<Int, List<EntityType>>()
    fun setupSupremeWaves() {
        supremeWaves[1] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.VINDICATOR)
        supremeWaves[2] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER, EntityType.WITCH)
        supremeWaves[3] = listOf(EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.VINDICATOR, EntityType.RAVAGER)
        supremeWaves[4] = listOf(EntityType.EVOKER, EntityType.PILLAGER, EntityType.VINDICATOR, EntityType.RAVAGER)
        supremeWaves[5] = listOf(EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER, EntityType.RAVAGER, EntityType.PILLAGER)
        supremeWaves[6] = listOf(EntityType.ILLUSIONER, EntityType.ILLUSIONER, EntityType.EVOKER, EntityType.EVOKER, EntityType.RAVAGER, EntityType.RAVAGER,
        EntityType.PILLAGER, EntityType.PILLAGER, EntityType.PILLAGER)
        supremeWaves[7] = listOf(EntityType.ILLUSIONER, EntityType.ILLUSIONER, EntityType.EVOKER, EntityType.EVOKER, EntityType.RAVAGER,
        EntityType.PILLAGER, EntityType.PILLAGER, EntityType.PILLAGER)
        waves[4] = supremeWaves
    }
}

data class Drop(val type: Int, val location: Location, var wave: Int = 1, var started: Boolean = false,
                val participatingPlayer: MutableList<Player> = mutableListOf(), val dropWaves: HashMap<Int, List<EntityType>> = waves[type]!!,
                val entities: MutableList<Entity> = mutableListOf(), var waveManager: Job?=null
)

fun getDropType(): Int {
    val random = Random()
    val randomInt = random.nextInt(50)
    return when{
        randomInt <= 1 -> 4
        randomInt <= 6 -> 3
        randomInt <= 12 -> 2
        randomInt <= 23 -> 1
        else -> 0
    }
}

fun Player.canGenDrops(): Boolean {
    return if (this.gameMode == GameMode.SURVIVAL || this.isOp) {
        this.rlgPlayer().dropCoolDown <= System.currentTimeMillis()
    } else false
}