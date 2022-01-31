package dev.crash.permission

import dev.crash.ChunkTable
import dev.crash.PlayerTable
import dev.crash.guild
import dev.crash.player.crashPlayer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.HashMap

data class ChunkClass(val x: Int, val z: Int, val world: String, val owner_uuid: String,val name: String, val shared: MutableList<String>)

val chunks: HashMap<String, HashMap<Long, ChunkClass>> = HashMap()
val chunkClassList: MutableList<ChunkClass> = mutableListOf()

fun Chunk.chunkData(): ChunkClass? {
    return chunks[this.world.name]?.get(this.chunkKey)
}

fun removeAllClaims(player: Player) {
    chunkClassList.filter { it.owner_uuid == player.uniqueId.toString() }.forEach {
        Bukkit.getWorld(it.world)!!.getChunkAt(it.x, it.z).unClaim()
    }
}

fun Chunk.isClaimed(): Boolean = chunks.containsKey(this.world.name) && chunks[this.world.name]!!.containsKey(this.chunkKey)

fun Chunk.claim(uuid: String, name: String, player: Player? = null): Boolean {
    val chunk = this
    if(!chunk.isClaimed()){
        transaction {
            ChunkTable.insert {
                it[ChunkTable.uuid] = uuid
                it[ChunkTable.name] = name
                it[shared] = ""
                it[world] = chunk.world.name
                it[x] = chunk.x
                it[z] = chunk.z
            }
        }
        if(player != null && uuid.length >= 3) {
            player.crashPlayer().remainingClaims--
        }
        val chunkClass = ChunkClass(chunk.x, chunk.z, chunk.world.name, uuid, name, ArrayList())
        if(chunks.containsKey(chunk.world.name)){
            chunks[chunkClass.world]!![chunk.chunkKey] = chunkClass
        }else {
            chunks[chunkClass.world] = hashMapOf(chunk.chunkKey to chunkClass)
        }
        chunkClassList.add(chunkClass)
        player?.sendMessage("§2The chunk has been claimed!")
    }else player?.sendMessage("§4The chunk belongs to ${chunk.chunkData()!!.name}!")
    return false
}

fun Chunk.unClaim() {
    val chunk = this
    if(!chunk.isClaimed()) return
    transaction {
        ChunkTable.deleteWhere {
            ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))
        }
        val chunkClass = chunks[chunk.world.name]!![chunk.chunkKey]!!
        val ownerUuid = chunkClass.owner_uuid
        if(ownerUuid.length > 3){
            val owner = Bukkit.getPlayer(UUID.fromString(ownerUuid))
            if(owner != null) {
                owner.crashPlayer().remainingClaims++
            }else {
                val remainingClaims = PlayerTable.select(where = {PlayerTable.uuid eq ownerUuid}).first()[PlayerTable.remainingClaims]
                PlayerTable.update(where = {PlayerTable.uuid eq ownerUuid}){
                    it[PlayerTable.remainingClaims] = remainingClaims + 1
                }
            }
        }
        chunkClassList.remove(chunkClass)
        chunks[world.name]!!.remove(chunkKey)
        if(chunks[world.name]!!.size == 0){
            chunks.remove(world.name)
        }
    }
}

fun Chunk.claim(player: Player): Boolean = claim(player.uniqueId.toString(), player.name, player)

fun getRemainingClaims(uuid: String): Int {
    var remainingClaims = 0
    transaction {
        remainingClaims = PlayerTable.select(where = {PlayerTable.uuid eq uuid}).first()[PlayerTable.remainingClaims]
    }
    return remainingClaims
}

fun changeAddedClaims(player: Player, amount: Int){
    val crashPlayer = player.crashPlayer()
    crashPlayer.remainingClaims += amount
    crashPlayer.addedClaims += amount
}

fun changeAddedHomes(player: Player, amount: Int){
    val crashPlayer = player.crashPlayer()
    crashPlayer.remainingHomes += amount
    crashPlayer.addedHomes += amount
}

fun getAddedClaims(uuid: String): Int {
    var addedClaims = 0
    transaction {
        addedClaims = PlayerTable.select(where = {PlayerTable.uuid eq uuid}).first()[PlayerTable.addedClaims]
    }
    return addedClaims
}

fun Chunk.changeChunkAccess(player: Player, grant: Boolean, executor: Player?){this.changeChunkAccess(player.uniqueId.toString(), grant, executor)}

fun Chunk.changeChunkAccess(uuid: String, grant: Boolean, executor: Player?){
    val chunk = this
    if(!chunk.isClaimed()){
        executor?.sendMessage("§4The chunk is not claimed!")
        return
    }
    val chunkClass = chunks[chunk.world.name]!![chunk.chunkKey]!!
    if(executor != null && (chunkClass.owner_uuid != executor.uniqueId.toString() && !executor.isOp)){
        executor.sendMessage("§4You don't own the chunk!")
        return
    }
    transaction {
        val shared = ChunkTable.select(where = {ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))}).first()[ChunkTable.shared]
        val sharedArray = shared.split(" ").toMutableList()
        if(!grant && !sharedArray.contains(uuid)){
            executor?.sendMessage("§4The player has no access to this chunk!")
            return@transaction
        }
        if(grant){
            sharedArray.add(uuid)
            chunkClass.shared.add(uuid)
            executor?.sendMessage("§2The player have been granted access to the chunk!")
        }else {
            sharedArray.remove(uuid)
            chunkClass.shared.remove(uuid)
            executor?.sendMessage("§2The player's access have been revoked!")
        }
        updateChunkShared(chunk, sharedArray)
    }
}

private fun Transaction.updateChunkShared(chunk: Chunk, sharedArray: MutableList<String>) = run {
    ChunkTable.update(where = {ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))}){
        it[shared] = sharedArray.joinToString(" ")
    }
}

fun Player.changeAccessAllChunks(uuid: String, grant: Boolean, executor: Player?){
    val player = this
    transaction {
        val chunks = mutableListOf<Chunk>()
        ChunkTable.select(where = {ChunkTable.uuid eq player.uniqueId.toString()}).forEach {
            chunks.add(Bukkit.getWorld(it[ChunkTable.world])!!.getChunkAt(it[ChunkTable.x], it[ChunkTable.z]))
        }
        chunks.forEach {
            it.changeChunkAccess(uuid, grant, executor)
        }
    }
}

fun eventCancel(chunk: Chunk): Boolean = chunk.isClaimed()

fun eventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    if(player.crashPlayer().isMod && player.gameMode == GameMode.CREATIVE) return false
    val chunkClass = chunk.chunkData()!!
    if(chunkClass.owner_uuid.length <= 3) return true
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return false
    if(chunkClass.shared.contains(player.uniqueId.toString())) return false
    return true
}

fun hitEventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    if(player.crashPlayer().isMod && player.gameMode == GameMode.CREATIVE) return false
    val chunkClass = chunk.chunkData()!!
    if(chunkClass.owner_uuid == "0") return true
    if(chunkClass.owner_uuid.length <= 3) return false
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return false
    if(chunkClass.shared.contains(player.uniqueId.toString())) return false
    return true
}

fun damageEventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    val chunkClass = chunk.chunkData()!!
    if(chunkClass.owner_uuid == "0") return true
    if(chunkClass.owner_uuid.length <= 3) return false
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return true
    if(chunkClass.shared.contains(player.uniqueId.toString())) return true
    return true
}

fun canBack(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return true
    val chunkClass = chunk.chunkData()!!
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return true
    val guild = player.crashPlayer().guild()
    if(guild != null && guild.member_uuids.contains(chunkClass.owner_uuid)) return true
    return chunkClass.owner_uuid.length < 3
}