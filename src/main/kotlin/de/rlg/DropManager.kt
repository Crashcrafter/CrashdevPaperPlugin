package de.rlg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.permission.*
import de.rlg.player.rlgPlayer
import kotlinx.coroutines.*
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.lang.Runnable
import java.util.*
import kotlin.collections.HashMap

var drops = HashMap<Chunk, Drop>()
var canDropStart = true

fun setDrop(chunk: Chunk, givenType: Int? = null): Boolean {
    if (!chunk.isClaimed()) {
        val world = chunk.world
        val x = chunk.x * 16 + 8
        val z = chunk.z * 16 + 8
        var y = world.getHighestBlockYAt(x, z)
        if(y < 0) return false
        val block0 = world.getBlockAt(x, y, z)
        val type = givenType ?: getDropType(world.name, block0.type == Material.WATER)
        if(type == -1) return false
        val dropType = dropTypeMap[type]!!
        if(dropType.spawnUnderWater){
            y = world.getHighestSolidBlockYAt(x, z)
        }
        val m: Material = Material.valueOf(dropType.glassPaneMaterial)
        if(dropType.spawnBeacon) {
            val localy = y-dropType.beaconHeight
            if(localy < 0) return false
            world.getBlockAt(x, localy, z).type = Material.BEACON
            for (i in 0 until dropType.beaconHeight) world.getBlockAt(x, localy + 1 + i, z).type = m
            for (xPoint in x - 1..x + 1) {
                for (zPoint in z - 1..z + 1) {
                    world.getBlockAt(xPoint, localy - 1, zPoint).type = Material.IRON_BLOCK
                }
            }
        }
        val block = world.getBlockAt(x, y + 1, z)
        block.location.add(0.0, -1.0, 0.0).block.type = m
        block.type = Material.CHEST
        if (block.state is Chest) {
            val chest = block.state as Chest
            val inventory = chest.blockInventory
            for (i in 0..13) {
                val slot = Random().nextInt(2)
                val nextInt = Random().nextInt(dropLootTableMap[type]!!.size)
                val itemStack = dropLootTableMap[type]!![nextInt].toItemstack()
                try { inventory.setItem(slot + i * 2, itemStack) } catch (ignored: Exception) { }
            }
            drops[chunk] = Drop(dropType, block.location)
            chunk.claim((type+1).toString(), "Server-Team")
            println("Set ${dropType.name} Drop, Type=" + type + ",Chunk:" + block.chunk.x + "/" + block.chunk.z)
            return true
        }
    } else {
        println("Drop wäre im Claim gespawnt")
    }
    return false
}

fun unsetDrop(chunk: Chunk, alsoLoot: Boolean) {
    val drop = drops[chunk] ?: return
    val world = chunk.world
    val x = chunk.x * 16 + 8
    val z = chunk.z * 16 + 8
    val y = world.getHighestBlockYAt(x, z) - (1 + drop.data.beaconHeight)
    val replaceBlock = Material.STONE
    world.getBlockAt(x, y, z).type = replaceBlock
    for (i in 0 until drop.data.beaconHeight) {
        world.getBlockAt(x, y + 1 + i, z).type = world.getBlockAt(x, y + 1 + i, z + 1).type
    }
    for (xPoint in x - 1..x + 1) {
        for (zPoint in z - 1..z + 1) {
            world.getBlockAt(xPoint, y - 1, zPoint).type = replaceBlock
        }
    }
    if (alsoLoot) {
        val block = world.getBlockAt(x, y + (1 + drop.data.beaconHeight), z)
        if (block.type == Material.CHEST) {
            (block.state as Chest).blockInventory.clear()
            block.type = Material.AIR
        }
    }
    chunk.unClaim()
    drop.musicJobs.forEach {
        it.cancel()
    }
    drop.participatingPlayer.forEach {
        it.stopSound("rlg.drop.music", SoundCategory.AMBIENT)
    }
    try { drop.waveManager!!.cancel() }catch (ex: NullPointerException) {}
    drops.remove(chunk)
}

@OptIn(DelicateCoroutinesApi::class)
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
        if (chunks.containsKey(chunk.chunkKey)) {
            val chunkClass: ChunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
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
                }
            }
        }
        for (player in drop.participatingPlayer) {
            drop.musicJobs.add(GlobalScope.launch {
                while (true){
                    player.playSound(location, "rlg.drop.music", SoundCategory.AMBIENT, 2f, 1f)
                    delay(162000)
                }
            })
        }
        val job = GlobalScope.launch {
            delay(2500)
            while (true) {
                delay(5000)
                if (drop.entities.size == 0) {
                    if (drop.wave >= drop.data.waves.size) {
                        object : BukkitRunnable() {
                            override fun run() {
                                unsetDrop(chunk, false)
                                if (drop.participatingPlayer.size == 1 && drop.data.type == 3) {
                                    questCount(drop.participatingPlayer[0], 4, 1, true)
                                }
                                drop.musicJobs.forEach {
                                    it.cancel()
                                }
                                for (player in drop.participatingPlayer) {
                                    try {
                                        player.stopSound("rlg.drop.music", SoundCategory.AMBIENT)
                                        player.playSound(drop.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.AMBIENT, 5f, 1f)
                                        when(drop.data.type){
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
    drop.data.waves[drop.wave]!!.forEach {
        try {
            Bukkit.getScheduler().runTask(INSTANCE, Runnable {
                val x = Random().nextInt(8) - 4
                val z = Random().nextInt(8) - 4
                val world = location.world
                val entity = world.spawnEntity(world.getHighestBlockAt(location.blockX + x, location.blockZ + z).location.add(0.5, 1.0, 0.5), EntityType.valueOf(it.uppercase()))
                entity.isGlowing = true
                entity.customName = dropWardenName
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
    if (chunks.containsKey(chunk.chunkKey) && chunks[chunk.chunkKey]!!.containsKey(chunk.world.name)) {
        val chunkClass: ChunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
        if (chunkClass.owner_uuid.contentEquals("0")) {
            drops.remove(chunk)
            return
        }
        val x = chunk.x * 16 + 8
        val z = chunk.z * 16 + 8
        val y = chunk.world.getHighestBlockYAt(x, z) - 2
        val block = chunk.world.getBlockAt(x, y, z)
        val type = chunkClass.owner_uuid.toInt()-1
        val dropType = dropTypeMap[type]!!
        println("Recover Drop " + type + " at " + chunk.x + "/" + chunk.z)
        val block1 = block.world.getBlockAt(x, y + 3, z)
        drops[chunk] = Drop(dropType, block1.location)
        waveManager(chunk)
    }
}

data class Drop(val data: DropObj, val location: Location, var wave: Int = 0, var started: Boolean = false,
                val participatingPlayer: MutableList<Player> = mutableListOf(),
                val entities: MutableList<Entity> = mutableListOf(), var waveManager: Job?=null, var musicJobs: MutableList<Job> = mutableListOf()
)

fun getDropType(worldName: String): Int {
    val possibleTypes = mutableListOf<DropObj>()
    dropTypeMap.values.forEach {
        if(it.allowedWorlds.contains(worldName)){
            possibleTypes.add(it)
        }
    }
    return possibleTypes.draw()?.type ?: -1
}

fun getDropType(worldName: String, water: Boolean): Int {
    val possibleTypes = mutableListOf<DropObj>()
    dropTypeMap.values.forEach {
        if(it.allowedWorlds.contains(worldName) && it.spawnInWater == water){
            possibleTypes.add(it)
        }
    }
    return possibleTypes.draw()?.type ?: -1
}

fun MutableList<DropObj>.draw(): DropObj? {
    var totalPossibility = 0
    this.forEach {
        totalPossibility += it.possibility
    }
    if(totalPossibility <= 0) return null
    var randomInt = Random().nextInt(totalPossibility)
    this.sortBy { it.possibility }
    this.forEach {
        randomInt -= it.possibility
        if(randomInt <= 0){
            return it
        }
    }
    return null
}

fun Player.canGenDrops(): Boolean {
    return if (this.gameMode == GameMode.SURVIVAL || this.isOp) {
        this.rlgPlayer().dropCoolDown <= System.currentTimeMillis()
    } else false
}

data class DropsSaveObj(val dropWardenName: String, val dropRange: Int, val drops: List<DropObj>)
data class DropObj(val type: Int, val possibility: Int, val allowedWorlds: List<String>, val name: String, val spawnInWater: Boolean, val spawnUnderWater: Boolean,
                   val spawnBeacon: Boolean, val beaconHeight: Int, val glassPaneMaterial: String, val waves: HashMap<Int, List<String>>, val lootTable: List<LootTableItem>)

val dropTypeMap = hashMapOf<Int, DropObj>()
val dropLootTableMap = hashMapOf<Int, MutableList<LootTableItem>>()

fun loadDropTables(){
    canDropStart = true
    val file = File(INSTANCE.dataFolder.path + "/drops.json")
    if(file.exists()){
        val drops = jacksonObjectMapper().readValue<DropsSaveObj>(file)
        dropWardenName = drops.dropWardenName
        dropRange = drops.dropRange
        drops.drops.forEach { dropObj ->
            val type = dropObj.type
            dropTypeMap[type] = dropObj
            dropLootTableMap[type] = mutableListOf()
            dropObj.lootTable.forEach {
                for(i in 0..it.probability){
                    dropLootTableMap[type]!!.add(it)
                }
            }
        }
    }else {
        file.createNewFile()
        jacksonObjectMapper().writeValue(file, DropsSaveObj("§4§lDrop Warden",10000, listOf()))
    }
}