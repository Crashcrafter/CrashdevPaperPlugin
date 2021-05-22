package de.rlg

import de.rlg.items.CustomItems
import de.rlg.permission.rankData
import de.rlg.player.rlgPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.DisplaySlot
import java.util.*
import kotlin.math.pow

fun CommandSender.asPlayer() : Player = if (this is Player) this else throw Exception("Only a player can execute that command!")

fun updateTabOfPlayers(leave: Boolean = false) {
    Bukkit.getOnlinePlayers().forEach {
        it.sendPlayerListHeader(Component.text("§e§l---------------  MCGermany.de  ---------------§r§6\nSpieler online: §a${Bukkit.getOnlinePlayers().size - if(leave) 1 else 0}"))
        it.sendPlayerListFooter(Component.text("\n§6Komm auf unseren Discord (/discord) um Mitspieler zu finden\nund Vorschläge für neuen Content zu machen§r\n\n§2Du kannst für unseren Server voten (/vote) um Vote Keys zu erhalten!"))
    }
}

fun Player.updateScoreboard(){
    val player = this
    val list: MutableList<String> = ArrayList()
    list.add("             ")
    list.add("§6§lNEU§r: §5Level-System!")
    list.add("-------------------  ")
    list.add("§aDein Kontostand:")
    list.add("§6" + player.rlgPlayer().balance.withPoints() + " Credits")
    val quests: List<Quest> = getActiveQuests(player)
    if (quests.isNotEmpty()) {
        list.add("-------------------")
        list.add("§5Quests:")
        for (quest in quests) {
            var color = "§6"
            if (quest.counter >= quest.needed) {
                color = "§2"
            }
            list.add("§a" + quest.name + color + " (" + quest.counter.toString() + "/" + quest.needed.toString() + ")")
        }
    }
    list.add("------------------- ")
    list.add("§4Tode:")
    list.add(INSTANCE.config.getInt("Players." + player.uniqueId.toString() + ".Deaths").toString())
    val manager = Bukkit.getScoreboardManager()
    val scoreboard = manager.newScoreboard
    val objective = scoreboard.registerNewObjective("scoreboard", "scoreboard", Component.text("scoreboard"))
    objective.displaySlot = DisplaySlot.SIDEBAR
    objective.displayName(Component.text("§e§lMCGermany.de"))
    for (i in list.indices) {
        val score = objective.getScore(list[i])
        score.score = list.size - i
    }
    player.scoreboard = scoreboard
}

fun sendModchatMessage(message: String, sender: Player){
    moderator.forEach {
        val msg = "§2[Modchat]§f ${rankData[sender.rlgPlayer().rank]!!.prefix} ${sender.name}> $message"
        it.sendMessage(msg)
    }
}

fun String.toStartUppercaseMaterial(): String {
    val parts = this.split("_").toTypedArray()
    val builder = StringBuilder()
    for (s in parts) {
        val first = s.substring(0, 1)
        val resultFirst = first.uppercase(Locale.ROOT)
        builder.append(resultFirst)
        builder.append(s.substring(1))
    }
    return builder.toString()
}

fun removeItems(inventory: Inventory, type: Material, itemAmount: Int, cmd: Int) {
    var amount = itemAmount
    if (amount <= 0) return
    val size = inventory.size
    for (slot in 0 until size) {
        val itemStack = inventory.getItem(slot) ?: continue
        if (type == itemStack.type) {
            if (cmd == 0 || itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == cmd) {
                val newAmount = itemStack.amount - amount
                if (newAmount > 0) {
                    itemStack.amount = newAmount
                    break
                } else {
                    inventory.clear(slot)
                    amount = -newAmount
                    if (amount == 0) break
                }
            }
        }
    }
}

fun isSpace(inventory: Inventory, amount: Int): Boolean {
    var count = 0
    for (itemStack in inventory.contents) {
        if (itemStack == null) {
            count++
        }
        if (count == amount) {
            return true
        }
    }
    return false
}

fun getEXPForLevel(level: Int): Long = (12 + 9 * level).toDouble().pow(2.0).toLong()

fun getKeysPerRank(rank: Int): String {
    return when (rank) {
        0 -> "0 0 0"
        1 -> "2 0 0"
        2 -> "5 2 0"
        3 -> "3 4 1"
        4 -> "8 3 3"
        5 -> "20 10 15"
        else -> "1 1 1"
    }
}

fun getBlockBySQLString(input: String): Block {
    val parts = input.split("/").toTypedArray()
    return Objects.requireNonNull(Bukkit.getWorld(parts[0]))!!
        .getBlockAt(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
}

fun Block.toSQLString(): String = "${this.world.name}/${this.x}/${this.y}/${this.z}"

fun List<Component>.toStringList(): MutableList<String>{
    val result = mutableListOf<String>()
    this.forEach {
        result.add((it as TextComponent).content())
    }
    return result
}

fun List<String>.toComponentList(): MutableList<Component>{
    val result = mutableListOf<Component>()
    this.forEach {
        result.add(Component.text(it))
    }
    return result
}

fun timeMultiplierFromString(input: String): Long = when (input) {
    "Minuten" -> 60
    "Stunden" -> 60 * 60
    "Tage" -> 60 * 60 * 24
    "Wochen" -> 60 * 60 * 24 * 7
    "Monate" -> 60 * 60 * 24 * 30
    else -> 60*60
}

fun StringBuilder.getExpDisplay(percent: Double){
    this.append("§b")
    var i = percent
    while (i > 0.05) {
        this.append("█")
        i -= 0.05f
    }
    this.append("§7")
    var j = percent
    while (j <= 1) {
        this.append("█")
        j += 0.05f
    }
}

fun getCryptoPrice(input: String): Int {
    return when(input) {
        "bitcoin" -> btcPrice!!
        "ethereum" -> ethPrice!!
        "litecoin" -> ltcPrice!!
        "nano" -> nanoPrice!!
        "dogecoin" -> dogePrice!!
        else -> throw NullPointerException()
    }
}

fun getCryptoPrice(input: Int): Int {
    return when(input) {
        1 -> btcPrice!!
        2 -> ethPrice!!
        3 -> ltcPrice!!
        5 -> nanoPrice!!
        4 -> dogePrice!!
        else -> throw NullPointerException()
    }
}

fun getCryptoItem(input: String): ItemStack {
    return when(input) {
        "bitcoin" -> CustomItems.bitcoin()
        "ethereum" -> CustomItems.ethereum()
        "litecoin" -> CustomItems.litecoin()
        "nano" -> CustomItems.nano()
        "dogecoin" -> CustomItems.dogecoin()
        else -> throw NullPointerException()
    }
}

fun Long.withPoints(): String {
    val asString = this.toString()
    val builder = StringBuilder()
    var i = 0
    for(c in asString.reversed()){
        builder.append(c)
        if(i == 2) {
            builder.append(".")
            i = 0
        }else i++
    }
    return builder.toString().reversed().removePrefix(".")
}

fun Int.withPoints(): String = this.toLong().withPoints()