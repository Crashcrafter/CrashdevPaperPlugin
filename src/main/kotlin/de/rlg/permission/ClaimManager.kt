package de.rlg.permission

import de.rlg.ChunkTable
import de.rlg.PlayersTable
import de.rlg.guild
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.HashMap

data class ChunkClass(val x: Int, val z: Int, val world: String, val owner_uuid: String,val name: String, val shared: MutableList<String>)

val chunks: HashMap<Long, HashMap<String, ChunkClass>> = HashMap()

fun removeAllClaims(player: Player) {
    transaction {
        ChunkTable.deleteWhere {
            ChunkTable.uuid eq player.uniqueId.toString()
        }
    }
}

fun Chunk.isClaimed(): Boolean = chunks.containsKey(this.chunkKey) && chunks[this.chunkKey]!!.containsKey(this.world.name)

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
            if(player != null){
                PlayersTable.update(where = {PlayersTable.uuid eq uuid}){
                    it[remainingClaims] = getRemainingClaims(uuid)
                }
            }
        }
        if(player != null) player.rlgPlayer().remainingClaims--
        val chunkClass = ChunkClass(chunk.x, chunk.z, chunk.world.name, uuid, name, ArrayList())
        if(chunks.containsKey(chunk.chunkKey)){
            chunks[chunk.chunkKey]!![chunkClass.world] = chunkClass
        }else {
            chunks[chunk.chunkKey] = hashMapOf(chunkClass.world to chunkClass)
        }
        player?.sendMessage("§2Der Chunk wurde geclaimt!")
    }else player?.sendMessage("§4Der Chunk gehört ${chunks[chunk.chunkKey]!![chunk.world.name]!!.name}!")
    return false
}

fun Chunk.unClaim() {
    val chunk = this
    transaction {
        ChunkTable.deleteWhere {
            ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))
        }
        try {
            val ownerUuid = chunks[chunk.chunkKey]!![chunk.world.name]!!.owner_uuid
            if(ownerUuid.length > 3){
                PlayersTable.update(where = {PlayersTable.uuid eq ownerUuid}){
                    it[remainingClaims] = getRemainingClaims(ownerUuid) + 1
                }
                val owner = Bukkit.getPlayer(UUID.fromString(ownerUuid))
                if(owner != null) {
                    owner.rlgPlayer().remainingClaims++
                }
            }
            chunks[chunkKey]!!.remove(world.name)
            if(chunks[chunkKey]!!.size == 0){
                chunks.remove(chunkKey)
            }
        }catch (ex: NullPointerException){}
    }
}

fun Chunk.claim(player: Player): Boolean = this.claim(player.uniqueId.toString(), player.name, player)

fun getRemainingClaims(uuid: String): Int {
    var amount = 0
    transaction {
        amount = PlayersTable.select(where = {PlayersTable.uuid eq uuid}).first()[PlayersTable.remainingClaims]
    }
    return amount
}

fun changeAddedClaims(player: Player, amount: Int){
    player.rlgPlayer().remainingClaims += amount
    changeAddedClaims(player.uniqueId.toString(), amount)
}

fun changeAddedClaims(uuid: String, amount: Int) {
    val remainingClaims = getRemainingClaims(uuid)
    val addedClaims = getAddedClaims(uuid)
    transaction {
        PlayersTable.update(where = {PlayersTable.uuid eq uuid}){
            it[PlayersTable.remainingClaims] = remainingClaims+amount
            it[PlayersTable.addedClaims] = addedClaims+amount
        }
    }
}

fun getAddedClaims(uuid: String): Int {
    var addedClaims = 0
    transaction {
        addedClaims = PlayersTable.select(where = {PlayersTable.uuid eq uuid}).first()[PlayersTable.addedClaims]
    }
    return addedClaims
}

fun Chunk.grantChunkAccess(player: Player, executor: Player?) {this.grantChunkAccess(player.uniqueId.toString(), executor)}

fun Chunk.grantChunkAccess(uuid: String, executor: Player?) {
    val chunk = this
    if(!chunk.isClaimed()) {
        executor?.sendMessage("§4Der Chunk is nicht geclaimt!")
        return
    }
    val chunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
    if(executor != null && (chunkClass.owner_uuid != executor.uniqueId.toString() || executor.isOp)){
        executor.sendMessage("§4Dir gehört der Chunk nicht!")
        return
    }
    transaction {
        val shared = ChunkTable.select(where = {ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))}).first()[ChunkTable.shared]
        val sharedArray = shared.split(" ").toMutableList()
        if(sharedArray.contains(uuid)){
            executor?.sendMessage("§4Der Spieler hat schon Zugriff auf diesen Chunk!")
            return@transaction
        }
        sharedArray.add(uuid)
        chunkClass.shared.add(uuid)
        updateChunkShared(chunk, sharedArray)
        executor?.sendMessage("§2Dem Spieler wurde Zugang zum Chunk gewärt!")
    }
}

fun Chunk.revokeChunkAccess(player: Player, executor: Player?) {this.revokeChunkAccess(player.uniqueId.toString(), executor)}

fun Chunk.revokeChunkAccess(uuid: String, executor: Player?){
    val chunk = this
    if(!chunk.isClaimed()){
        executor?.sendMessage("§4Der Chunk is nicht geclaimt!")
        return
    }
    val chunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
    if(executor != null && (chunkClass.owner_uuid != executor.uniqueId.toString() || executor.isOp)){
        executor.sendMessage("§4Dir gehört der Chunk nicht!")
        return
    }
    transaction {
        val shared = ChunkTable.select(where = {ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))}).first()[ChunkTable.shared]
        val sharedArray = shared.split(" ").toMutableList()
        if(!sharedArray.contains(uuid)){
            executor?.sendMessage("§4Der Spieler hat keinen Zugriff auf diesen Chunk!")
            return@transaction
        }
        sharedArray.remove(uuid)
        chunkClass.shared.remove(uuid)
        updateChunkShared(chunk, sharedArray)
        executor?.sendMessage("§2Dem Spieler wurde Zugang zum Chunk entfernt!")
    }
}

private fun Transaction.updateChunkShared(chunk: Chunk, sharedArray: MutableList<String>) = run {
    ChunkTable.update(where = {ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))}){
        it[shared] = sharedArray.joinToString(" ")
    }
}

fun Player.changeAccessAllChunks(uuid: String, grant: Boolean){
    val player = this
    transaction {
        val chunks = mutableListOf<Chunk>()
        ChunkTable.select(where = {ChunkTable.uuid eq player.uniqueId.toString()}).forEach {
            chunks.add(Bukkit.getWorld(it[ChunkTable.world])!!.getChunkAt(it[ChunkTable.x], it[ChunkTable.z]))
        }
        chunks.forEach {
            if(grant){
                it.grantChunkAccess(uuid, null)
            }else {
                it.revokeChunkAccess(uuid, null)
            }
        }
    }
}

fun eventCancel(chunk: Chunk): Boolean = chunk.isClaimed()

fun eventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    if(player.rlgPlayer().isMod && player.gameMode == GameMode.CREATIVE) return false
    val chunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
    if(chunkClass.owner_uuid.length <= 3) return true
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return false
    if(chunkClass.shared.contains(player.uniqueId.toString())) return false
    return true
}

fun heventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    if(player.rlgPlayer().isMod && player.gameMode == GameMode.CREATIVE) return false
    val chunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
    if(chunkClass.owner_uuid == "0") return true
    if(chunkClass.owner_uuid.length <= 3) return false
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return false
    if(chunkClass.shared.contains(player.uniqueId.toString())) return false
    return true
}

fun deventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    val chunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
    if(chunkClass.owner_uuid == "0") return true
    if(chunkClass.owner_uuid.length <= 3) return false
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return true
    if(chunkClass.shared.contains(player.uniqueId.toString())) return true
    return true
}

fun canBack(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return true
    val chunkClass = chunks[chunk.chunkKey]!![chunk.world.name]!!
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return true
    val guild = player.rlgPlayer().guild()
    if(guild != null && guild.member_uuids.contains(chunkClass.owner_uuid)) return true
    return chunkClass.owner_uuid.length < 3
}