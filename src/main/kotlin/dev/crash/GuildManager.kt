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

fun CrashPlayer.guild(): Guild? {
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
    val crashPlayer = player.crashPlayer()
    if(crashPlayer.guildId != 0){
        player.sendMessage("§4You are already in a guild!")
        return
    }
    if(crashPlayer.balance < 50000){
        player.sendMessage("§4You need 50.000 Credits to create a guild!")
        guildSetupProgress.remove(player)
        return
    }
    if(!guildSetupProgress.containsKey(player)){
        val guild = SetupGuild("", "", player.uniqueId.toString(), player.name)
        guildSetupProgress[player] = guild
        player.sendMessage("§6§lGuild Setup§r\n\n§cThe creation of a guild costs you 50.000 Credits!§r\nIf you want to cancel the setup enter /guild setup cancel\n" +
                "§2Please enter the name of your guild:")
        player.addMessageListener {event, message ->
            guildSetup(player, message)
            event.isCancelled = true
            false
        }
        return
    }
    if(guildSetupProgress[player]!!.name == ""){
        if(msg.length > 20){
            player.sendMessage("§4The name can't be longer than 20 chars!")
        }else{
            transaction {
                if(!GuildTable.select(where = {GuildTable.name eq msg}).empty()){
                    player.sendMessage("§4There is already another guild with that name!")
                }else {
                    player.sendMessage("§2Name was set! §r($msg)§2!\nPlease enter the short of your guild:")
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
            player.sendMessage("§4Your short can't be longer than 4 chars!")
        }else{
            var ret = false
            transaction {
                if(!GuildTable.select(where = {GuildTable.suffix eq msg}).empty()){
                    player.sendMessage("§4There is already another guild with that short!")
                    ret = true
                }
            }
            if(ret) return
            player.sendMessage("§2Short is set! §r[§6$msg§r]\n\n§2Your Guild have been created! If you have any question feel free to ask on the /discord!")
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
                crashPlayer.guildId = id
            }
            crashPlayer.setName()
            pay(player, 50000, "Guild-Creation")
            guildSetupProgress.remove(player)
            updateTabOfPlayers()
        }
        return
    }
}

fun CrashPlayer.removeFromGuild(reason: String){
    val guildId = this.guildId
    if(guildId == 0) return
    val guild = this.guild()!!
    val uuid = this.player.uniqueId.toString()
    if(guild.owner_uuid == uuid){
        this.player.sendMessage("§4You are the owner of the guild!\nIf you want to delete your guild use /guild delete.")
        return
    }
    guild.member_names.remove(this.player.name)
    guild.member_uuids.remove(uuid)
    guild.saveMembers()
    guild.sendMessage(reason)
    this.guildId = 0
    this.setName()
    updateTabOfPlayers()
}

fun Guild.saveMembers(){
    val guild = this
    transaction {
        GuildTable.update(where = {GuildTable.id eq guild.id}){
            it[member_names] = guild.member_names.joinToString(" ")
            it[member_uuids] = guild.member_uuids.joinToString(" ")
        }
    }
}

fun CrashPlayer.deleteGuild(){
    val guildId = this.guildId
    if(guildId == 0) return
    val guild = this.guild()!!
    if(guild.owner_uuid != this.player.uniqueId.toString()){
        player.sendMessage("§4You are the owner of the guild!")
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
                val crashPlayer = player.crashPlayer()
                crashPlayer.guildId = 0
                crashPlayer.setName()
                player.sendMessage("§4Your guild was deleted!")
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

fun CrashPlayer.joinGuild(id: Int) {
    if(this.guildId == 0){
        this.guildId = id
        this.setName()
        val guild = this.guild()!!
        guild.member_names.add(this.player.name)
        guild.member_uuids.add(this.player.uniqueId.toString())
        guild.sendMessage("§a${this.player.name} joined the guild!")
        guild.saveMembers()
    }else {
        this.player.sendMessage("§4You are already in a guild!")
    }
}

fun Guild.sendMessage(message: String, sender: Player? = null){
    this.member_uuids.forEach {
        Bukkit.getPlayer(UUID.fromString(it))?.sendMessage("§8[§6${this.suffix}§8]§r ${sender?.name ?: ""}> $message")
    }
}