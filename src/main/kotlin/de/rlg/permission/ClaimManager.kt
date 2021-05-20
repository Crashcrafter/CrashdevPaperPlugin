package de.rlg.permission

import de.rlg.ChunkTable
import de.rlg.PlayersTable
import de.rlg.player.rlgPlayer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class ChunkClass(val x: Int, val z: Int, val world: String, val owner_uuid: String,val name: String, val shared: MutableList<String>)

val chunkList: ArrayList<ChunkClass> = ArrayList()
val chunks: HashMap<Chunk, ChunkClass> = HashMap()

fun removeAllClaims(player: Player) {
    transaction {
        ChunkTable.deleteWhere {
            ChunkTable.uuid eq player.uniqueId.toString()
        }
    }
}

fun Chunk.isClaimed(): Boolean = chunks.containsKey(this)

fun Chunk.claim(uuid: String, name: String, player: Player?): Boolean {
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
        val chunkClass = ChunkClass(chunk.x, chunk.z, chunk.world.name, uuid, name, ArrayList())
        chunks[chunk] = chunkClass
        chunkList.add(chunkClass)
        player?.sendMessage("§2Der Chunk wurde geclaimt!")
    }else player?.sendMessage("§4Der Chunk gehört ${chunks[chunk]!!.name}!")
    return false
}

fun Chunk.unClaim() {
    val chunk = this
    transaction {
        ChunkTable.deleteWhere {
            ChunkTable.x eq chunk.x and(ChunkTable.z eq chunk.z and(ChunkTable.world eq chunk.world.name))
        }
        val ownerUuid = chunks[chunk]!!.owner_uuid
        if(ownerUuid.length > 2){
            PlayersTable.update(where = {PlayersTable.uuid eq ownerUuid}){
                it[remainingClaims] = getRemainingClaims(ownerUuid) + 1
            }
            val owner = Bukkit.getPlayer(UUID.fromString(ownerUuid))
            if(owner != null) {
                owner.rlgPlayer().remainingClaims++
            }
        }
        chunkList.remove(chunks[chunk])
        chunks.remove(chunk)
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
    val chunkClass = chunks[chunk]!!
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
    val chunkClass = chunks[chunk]!!
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
        executor?.sendMessage("§2Dem Spieler wurde Zugang zum Chunk gewärt!")
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
    if(player.isOp && player.gameMode == GameMode.CREATIVE) return false
    val chunkClass = chunks[chunk]!!
    if(chunkClass.owner_uuid == "0" && player.rlgPlayer().isMod && player.gameMode == GameMode.CREATIVE) return false
    if(chunkClass.owner_uuid == "0") return true
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return false
    if(chunkClass.shared.contains(player.uniqueId.toString())) return false
    return true
}

fun deventCancel(chunk: Chunk, player: Player): Boolean {
    if(!chunk.isClaimed()) return false
    val chunkClass = chunks[chunk]!!
    if(chunkClass.owner_uuid == "0") return true
    if(chunkClass.owner_uuid == player.uniqueId.toString()) return true
    if(chunkClass.shared.contains(player.uniqueId.toString())) return true
    return false
}