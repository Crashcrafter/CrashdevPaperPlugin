package dev.crash

import dev.crash.listener.addMessageListener
import dev.crash.player.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.collections.HashMap

data class Guild(var id: Int, val name: String, val suffix: String, val owner_uuid: String, val owner_name: String, val member_names: MutableList<String>, val member_uuids: MutableList<String>)
data class SetupGuild(var name: String, var suffix: String, val owner_uuid: String, val owner_name: String)

private val guilds = HashMap<Int, Guild>()
val guildSetupProgress = HashMap<Player, SetupGuild>()

fun RLGPlayer.guild(): Guild? {
    if(!guilds.containsKey(this.guildId)){
        this.guildId = 0
        return null
    }
    return guilds[this.guildId]!!
}

fun SetupGuild.finalize(): Guild = Guild(0, this.name, this.suffix, this.owner_uuid, this.owner_name, arrayListOf(this.owner_name), arrayListOf(this.owner_uuid))

internal fun initGuilds(){
    transaction {
        GuildTable.selectAll().forEach {
            val id = it[GuildTable.id].value
            val memberNames = it[GuildTable.member_names].split(" ").toMutableList()
            val memberUuids = it[GuildTable.member_uuids].split(" ").toMutableList()
            guilds[id] = Guild(id, it[GuildTable.name], it[GuildTable.suffix], it[GuildTable.owner_uuid], it[GuildTable.owner_name], memberNames, memberUuids)
        }
    }
}

fun guildSetup(player: Player, msg: String){
    val rlgPlayer = player.rlgPlayer()
    if(rlgPlayer.guildId != 0){
        player.sendMessage("§4Du bist schon in einer Guild!")
        return
    }
    if(rlgPlayer.balance < 50000){
        player.sendMessage("§4Du benötigst 50.000 Credits, um eine Guild zu gründen!\nDu musst das Setup von vorne anfangen!")
        guildSetupProgress.remove(player)
        return
    }
    if(!guildSetupProgress.containsKey(player)){
        val guild = SetupGuild("", "", player.uniqueId.toString(), player.name)
        guildSetupProgress[player] = guild
        player.sendMessage("§6§lGuild Setup§r\n\n§cDas erstellen einer Guild kostet dich 25.000 Credits!§r\nWenn du das Setup abbrechen möchtest, gib /guild setup cancel ein.\n" +
                "§2Gib nun den Namen der Guild ein:")
        player.addMessageListener {event, message ->
            guildSetup(player, message)
            event.isCancelled = true
            false
        }
        return
    }
    if(guildSetupProgress[player]!!.name == ""){
        if(msg.length > 20){
            player.sendMessage("§4Der Name darf maximal 20 Zeichen lang sein!")
        }else{
            transaction {
                if(!GuildTable.select(where = {GuildTable.name eq msg}).empty()){
                    player.sendMessage("§4Eine Guild mit diesem Namen existiert schon! Bitte gib einen anderen ein!")
                }else {
                    player.sendMessage("§2Name wurde gesetzt §r($msg)§2!\nGib jetzt das Kürzel der Guild ein:")
                    guildSetupProgress[player]!!.name = msg
                    player.addMessageListener {event, message ->
                        guildSetup(player, message)
                        event.isCancelled = true
                        true
                    }
                }
            }
        }
        return
    }
    if(guildSetupProgress[player]!!.suffix == ""){
        if(msg.length > 4){
            player.sendMessage("§4Das Kürzel darf maximal 4 Zeichen lang sein!")
        }else{
            var ret = false
            transaction {
                if(!GuildTable.select(where = {GuildTable.suffix eq msg}).empty()){
                    player.sendMessage("§4Eine Guild mit dem Kürzel existiert schon! Gib ein anderes ein!")
                    ret = true
                }
            }
            if(ret) return
            player.sendMessage("§2Kürzel wurde gesetzt! §r[§6$msg§r]\n\n§2Deine Guild wurde erstellt! Solltest du Fragen haben, kannst du auf unserem Discord (/discord) diese stellen!")
            val setupGuild = guildSetupProgress[player]!!
            setupGuild.suffix = msg
            val final = setupGuild.finalize()
            transaction {
                val id = GuildTable.insertAndGetId {
                    it[name] = final.name
                    it[suffix] = final.suffix
                    it[owner_name] = final.owner_name
                    it[owner_uuid] = final.owner_uuid
                    it[member_names] = final.member_names.joinToString(" ")
                    it[member_uuids] = final.member_uuids.joinToString(" ")
                }.value
                final.id = id
                guilds[id] = final
                rlgPlayer.guildId = id
            }
            rlgPlayer.setName()
            pay(player, 50000, "Gründen der Guild")
            guildSetupProgress.remove(player)
            updateTabOfPlayers()
        }
        return
    }
}

fun RLGPlayer.removeFromGuild(reason: String){
    val guildId = this.guildId
    if(guildId == 0) return
    val guild = this.guild()!!
    val uuid = this.player.uniqueId.toString()
    if(guild.owner_uuid == uuid){
        this.player.sendMessage("§4Du bist der Owner der Guild!\nWenn du die Guild löschen möchtest, nutze /guild delete.")
        return
    }
    guild.member_names.remove(this.player.name)
    guild.member_uuids.remove(uuid)
    guild.saveMembers(guildId)
    guild.sendMessage(reason)
    this.guildId = 0
    this.setName()
    updateTabOfPlayers()
}

fun Guild.saveMembers(guildId: Int){
    val guild = this
    transaction {
        GuildTable.update(where = {GuildTable.id eq guildId}){
            it[member_names] = guild.member_names.joinToString(" ")
            it[member_uuids] = guild.member_uuids.joinToString(" ")
        }
    }
}

fun RLGPlayer.deleteGuild(){
    val guildId = this.guildId
    if(guildId == 0) return
    val guild = this.guild()!!
    if(guild.owner_uuid != this.player.uniqueId.toString()){
        player.sendMessage("§4Du bist nicht der Owner der Guild!")
        return
    }
    this.guildId = 0
    transaction {
        guild.member_uuids.forEach { it2 ->
            PlayerTable.update(where = {PlayerTable.uuid eq it2}){
                it[PlayerTable.guildId] = 0
            }
            val player = Bukkit.getPlayer(UUID.fromString(it2))
            if(player != null){
                player.rlgPlayer().guildId = 0
                player.rlgPlayer().setName()
                player.sendMessage("§4Deine Guild wurde aufgelöst!")
            }
        }
        GuildTable.deleteWhere{
            GuildTable.owner_uuid eq guild.owner_uuid
        }
        guilds.remove(guildId)
    }
    this.setName()
    updateTabOfPlayers()
}

fun RLGPlayer.joinGuild(id: Int) {
    if(this.guildId == 0){
        this.guildId = id
        this.setName()
        val guild = this.guild()!!
        guild.member_names.add(this.player.name)
        guild.member_uuids.add(this.player.uniqueId.toString())
        guild.sendMessage("§a${this.player.name} ist der Guild beigetreten!")
        guild.saveMembers(guildId)
    }else {
        this.player.sendMessage("§4Du bist bereits in einer Guild!")
    }
}

fun Guild.sendMessage(message: String, sender: Player? = null){
    this.member_uuids.forEach {
        Bukkit.getPlayer(UUID.fromString(it))?.sendMessage("§8[§6${this.suffix}§8]§r ${sender?.name ?: ""}> $message")
    }
}