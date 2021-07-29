package de.rlg

import de.rlg.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

//region Domains
val domainEndings = listOf(
    "http:",
    "https:",
    "www.",
    ".de",
    ".xyz",
    ".net",
    ".buy",
    ".org",
    ".online",
    ".eu",
    ".one",
    ".at",
    ".co",
    ".ca",
    ".blog",
    ".as",
    ".asia",
    ".ag",
    ".ae",
    ".dk",
    ".es",
    ".fi",
    ".fr",
    ".fun",
    ".gg",
    ".gmbh",
    ".gratis",
    ".guide",
    ".host",
    ".holiday",
    ".info",
    ".ink",
    ".in",
    ".immo",
    ".immobilien",
    ".international",
    ".it",
    ".irish",
    ".je",
    ".kaufen",
    ".land",
    ".li",
    ".link",
    ".live",
    ".lol",
    ".ltd",
    ".market",
    ".me",
    ".media",
    ".money",
    ".menu",
    ".movie",
    ".name",
    ".nl",
    ".no",
    ".nu",
    ".onl",
    ".ooo",
    ".pl",
    ".plus",
    ".pub",
    ".re",
    ".rent",
    ".rest",
    ".rip",
    ".run",
    ".sale",
    ".se",
    ".site",
    ".store",
    ".team",
    ".tf",
    ".tools",
    ".tech",
    ".tv",
    ".uk",
    ".uno",
    ".vet",
    ".vin",
    ".watch",
    ".wiki",
    ".wtf",
    ".yt",
    ".zone"
)
//endregion

//region Beleidigung
val offenses = listOf(
    "arschloch",
    "hurensohn",
    "huso",
    "wixer",
    "idiot",
    "nutte",
    "nude ",
    "vollidiot",
    "hure",
    "behindert",
    "wixkind",
    "fotze",
    "figgo",
    "scheißhaufen",
    "anal",
    "muschis",
    "take your knife",
    "drown yourself",
    "bitch",
    "fuck",
    "whore",
    "cock",
    "fick",
    "pussy"
)
//endregion

fun checkMessage(message: String, player: Player): Boolean {
    if(player.isOp) return false
    for (domain in domainEndings) {
        if (message.contains(domain)) {
            addLinkSend(player)
            player.sendMessage("§4Links in Minecraft zu senden ist verboten!")
            println("§4" + player.name + " hat einen Link gesendet")
            return true
        }
    }
    for (offense in offenses) {
        if (message.contains(" $offense")) {
            addOffense(player)
            player.sendMessage("§4EY, nicht beleidigen!")
            println("§4" + player.name + " hat versucht zu beleidigen")
            return true
        }
    }
    return false
}

fun tempbanUser(reason: String, date: Date?, player: Player, playername: String) {
    val banList1 = Bukkit.getBanList(BanList.Type.NAME)
    banList1.addBan(playername, "$reason Name:$playername", date, player.name)
}

fun tempbanOnlineUser(target: Player, reason: String, date: Date?) {
    if(target.isOp) return
    val banList = Bukkit.getBanList(BanList.Type.IP)
    banList.addBan(Objects.requireNonNull(target.address).hostName, reason + " " + target.name, date, "System")
    val banList1 = Bukkit.getBanList(BanList.Type.NAME)
    banList1.addBan(target.name, reason + " Name:" + target.name, date, "System")
    Bukkit.getScheduler().runTask(INSTANCE, Runnable {
        target.kick(Component.text(reason))
    })
}

fun addLinkSend(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    rlgPlayer.playerLinkCounter.add(System.currentTimeMillis() + 1000 * 60 * 60)
    rlgPlayer.playerLinkCounter.checkCounter()
    if (rlgPlayer.playerLinkCounter.size >= 3) {
        tempbanOnlineUser(player, "Du wurdest gebannt wegen Senden von Links!\nDauer: 3 Tage", Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3))
    }else warnPlayer(player, "Senden von Links")
}

fun addOffense(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    rlgPlayer.playerOffenseCounter.add(System.currentTimeMillis() + 1000 * 60 * 60)
    warnPlayer(player, "Beleidigung")
    rlgPlayer.playerOffenseCounter.checkCounter()
    if (rlgPlayer.playerOffenseCounter.size >= 3) {
        tempbanOnlineUser(player, "Du wurdest gebannt wegen Beleidigung im Chat!\nDauer: 3 Tage", Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3))
    }
}

fun addAFKCounter(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    rlgPlayer.playerAfkCounter.add(System.currentTimeMillis() + 1000 * 60)
    if (rlgPlayer.playerAfkCounter.size >= 30) {
        rlgPlayer.playerAfkCounter.checkCounter()
        if (rlgPlayer.playerAfkCounter.size >= 30) {
            player.kick(Component.text("AFK-Farming ist verboten!"))
        }
    }
}

private fun MutableList<Long>.checkCounter(){
    val currentTime = System.currentTimeMillis()
    val remove: MutableList<Long> = ArrayList()
    for (time in this) {
        if (time <= currentTime) {
            remove.add(time)
        }
    }
    for (removeTime in remove) {
        this.remove(removeTime)
    }
}

fun warnPlayer(player: Player, reason: String) = warnPlayer(player, reason, "AutoMod")

fun warnPlayer(player: Player, reason: String, modName: String){
    if(player.isOp) return
    player.sendMessage("Du wurdest wegen $reason gewarnt!")
    transaction {
        WarnTable.insert {
            it[uuid] = player.uniqueId.toString()
            it[WarnTable.modName] = modName
            it[time] = Instant.now()
            it[WarnTable.reason] = reason
            it[name] = player.name
        }
    }
    player.checkWarns()
}

fun removeAllWarns(player: Player) {
    transaction {
        WarnTable.deleteWhere {
            PlayersTable.uuid eq player.uniqueId.toString()
        }
    }
}

fun removeWarn(player: Player, number: Int) {
    transaction {
        var i = 0
        var warnDate: Instant? = null
        WarnTable.select(where = {WarnTable.uuid eq player.uniqueId.toString()}).forEach {
            if(i == number) {
                warnDate = it[WarnTable.time]
            }
            i++
        }
        if(warnDate != null){
            WarnTable.deleteWhere {
                WarnTable.uuid eq player.uniqueId.toString() and(WarnTable.time eq warnDate!!)
            }
        }
    }
}

fun getWarns(player: Player): String {
    val warnList = StringBuilder()
    transaction {
        var i = 0
        WarnTable.select(where = {WarnTable.uuid eq player.uniqueId.toString()}).forEach{
            warnList.append(i).append(": [").append(it[WarnTable.time]).append("]: ").append(it[WarnTable.reason])
                .append(" von ").append(it[WarnTable.modName]).append("\n")
            i++
        }
    }
    return warnList.toString()
}

fun Player.mute(seconds: Long) {
    this.sendMessage("§4Du wurdest temporär gemuted!")
    this.rlgPlayer().mutedUntil = System.currentTimeMillis()+(seconds*1000)
}

fun Player.unmute() {
    this.sendMessage("§2Du wurdest entmuted!")
    this.rlgPlayer().mutedUntil = System.currentTimeMillis()
}

fun Player.checkWarns(){
    val player = this
    transaction {
        var lastWeekWarnings = 0
        val lastWeekTime = Instant.now().minus(7, ChronoUnit.DAYS)
        WarnTable.select(where = {WarnTable.uuid eq player.uniqueId.toString()}).forEach {
            if(it[WarnTable.time].isAfter(lastWeekTime)){
                lastWeekWarnings++
            }
        }
        if(lastWeekWarnings > 5) tempbanOnlineUser(player, "Too many Infractions", Date.from(Instant.now().plus(3, ChronoUnit.DAYS)))
    }
}