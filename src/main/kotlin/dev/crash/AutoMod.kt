package dev.crash

import dev.crash.player.crashPlayer
import dev.crash.player.CrashPlayer
import dev.crash.player.Warn
import net.kyori.adventure.text.Component
import org.bukkit.BanList
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
    val crashPlayer = player.crashPlayer()
    crashPlayer.playerLinkCounter.add(System.currentTimeMillis() + 1000 * 60 * 60)
    crashPlayer.playerLinkCounter.checkCounter()
    if (crashPlayer.playerLinkCounter.size >= 3) {
        tempbanOnlineUser(player, "Du wurdest gebannt wegen Senden von Links!\nDauer: 3 Tage", Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3))
    }else crashPlayer.warn( "Senden von Links", "Automod")
}

fun addOffense(player: Player) {
    val crashPlayer = player.crashPlayer()
    crashPlayer.playerOffenseCounter.add(System.currentTimeMillis() + 1000 * 60 * 60)
    crashPlayer.warn("Beleidigung", "Automod")
    crashPlayer.playerOffenseCounter.checkCounter()
    if (crashPlayer.playerOffenseCounter.size >= 3) {
        tempbanOnlineUser(player, "Du wurdest gebannt wegen Beleidigung im Chat!\nDauer: 3 Tage", Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3))
    }
}

fun addAFKCounter(player: Player) {
    val crashPlayer = player.crashPlayer()
    crashPlayer.playerAfkCounter.add(System.currentTimeMillis() + 1000 * 60)
    if (crashPlayer.playerAfkCounter.size >= 30) {
        crashPlayer.playerAfkCounter.checkCounter()
        if (crashPlayer.playerAfkCounter.size >= 30) {
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

fun CrashPlayer.warn(reason: String, modName: String){
    if(player.isOp) return
    player.sendMessage("Du wurdest wegen $reason gewarnt!")
    warns.add(Warn(reason, modName, System.currentTimeMillis()))
    checkWarns()
}

fun CrashPlayer.removeAllWarns(){
    this.warns = mutableListOf()
}

fun CrashPlayer.removeWarn(number: Int) {
    warns.removeAt(number)
}

fun CrashPlayer.getWarnsString(): String {
    val warnList = StringBuilder()
    var i = 0
    warns.forEach {
        warnList.append(i).append(": ").append(it.reason)
            .append(" by ").append(it.modName).append("\n")
        i++
    }
    return warnList.toString()
}

fun CrashPlayer.mute(seconds: Long) {
    player.sendMessage("§4Du wurdest temporär gemuted!")
    mutedUntil = System.currentTimeMillis()+(seconds*1000)
}

fun CrashPlayer.unmute() {
    player.sendMessage("§2Du wurdest entmuted!")
    mutedUntil = System.currentTimeMillis()
}

fun CrashPlayer.checkWarns(){
    val crashPlayer = this
    var lastWeekWarnings = 0
    warns.forEach {
        if(Instant.ofEpochMilli(it.time).isAfter(Instant.now().minus(7, ChronoUnit.DAYS))){
            lastWeekWarnings++
        }
    }
    if(lastWeekWarnings > 5) tempbanOnlineUser(crashPlayer.player, "Too many Infractions", Date.from(Instant.now().plus(3, ChronoUnit.DAYS)))
}