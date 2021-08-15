package dev.crash

import com.fasterxml.jackson.databind.JsonNode
import dev.crash.permission.rankData
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow


fun CommandSender.asPlayer() : Player = if (this is Player) this else throw Exception("Only a player can execute that command!")

fun updateTabOfPlayers(leave: Boolean = false) {
    Bukkit.getOnlinePlayers().forEach {
        it.sendPlayerListHeader(Component.text("§e§l---------------  ${CONFIG.scoreBoardTitle}§r§e§l  ---------------§r§6\nPlayer online: §a${Bukkit.getOnlinePlayers().size - if(leave) 1 else 0}"))
        it.sendPlayerListFooter(Component.text(CONFIG.playerListFooter))
        it.updateScoreboard()
    }
}

fun Player.updateScoreboard(){
    val player = this
    val list: MutableList<String> = ArrayList()
    list.add("             ")
    list.add(CONFIG.scoreBoardNews)
    list.add("-------------------  ")
    list.add("§aYour Balance:")
    list.add("§6" + player.crashPlayer().balance.withPoints() + " Credits")
    val quests: List<Quest> = getActiveQuests(player)
    if (quests.isNotEmpty()) {
        list.add("-------------------")
        list.add("§5Quests:")
        for (quest in quests) {
            var color = "§6"
            if (quest.counter >= quest.needed) {
                color = "§2"
            }
            list.add("§a" + quest.name + color + " (" + quest.counter.withPoints() + "/" + quest.needed.withPoints() + ")")
        }
    }
    list.add("------------------- ")
    list.add("§4Deaths:")
    list.add(INSTANCE.config.getInt("Players." + player.uniqueId.toString() + ".Deaths").toString())
    Bukkit.getScheduler().runTask(INSTANCE, Runnable {
        val scoreboard = Bukkit.getScoreboardManager().newScoreboard
        scoreboard.getServerTeams()
        val objective = scoreboard.registerNewObjective("scoreboard", "scoreboard", Component.text("scoreboard"))
        objective.displaySlot = DisplaySlot.SIDEBAR
        objective.displayName(Component.text(CONFIG.scoreBoardTitle))
        for (i in list.indices) {
            val score = objective.getScore(list[i])
            score.score = list.size - i
        }
        player.scoreboard = scoreboard
    })
}

fun Scoreboard.getServerTeams(){
    Bukkit.getOnlinePlayers().forEach {
        val crashPlayer = it.crashPlayer()
        val team = this.registerNewTeam(it.name)
        team.setAllowFriendlyFire(true)
        team.setCanSeeFriendlyInvisibles(false)
        team.prefix(Component.text("${crashPlayer.rankData().prefix}§r "))
        if(crashPlayer.guildId != 0){
            team.suffix(Component.text(" [§6${crashPlayer.guild()!!.suffix}§r]"))
        }
        team.addEntry(it.name)
    }
}

fun sendModchatMessage(message: String, sender: Player){
    val msg = "§2[Teamchat]§f ${sender.crashPlayer().rankData().prefix} ${sender.name}> $message"
    moderator.forEach {
        it.sendMessage(msg)
    }
}

fun sendModchatMessage(message: String){
    val msg = "§2[Teamchat] §fServer>§r $message"
    moderator.forEach {
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

fun isSpace(inventory: Inventory): Boolean = inventory.firstEmpty() != -1

fun getEXPForLevel(level: Int): Long = (15 + 7 * level).toDouble().pow(2.0).toLong()

fun getBlockByPositionString(input: String): Block {
    val parts = input.split("/").toTypedArray()
    return Objects.requireNonNull(Bukkit.getWorld(parts[0]))!!
        .getBlockAt(parts[1].toInt(), parts[2].toInt(), parts[3].toInt())
}

fun Block.toPositionString(): String = "${this.world.name}/${this.x}/${this.y}/${this.z}"

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
    "minutes" -> 60
    "hours" -> 60 * 60
    "days" -> 60 * 60 * 24
    "weeks" -> 60 * 60 * 24 * 7
    "months" -> 60 * 60 * 24 * 30
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

fun getCryptoPrice(input: String): Long {
    return when(input) {
        "bitcoin" -> prices[Material.STICK]!![1]!!
        "ethereum" -> prices[Material.STICK]!![2]!!
        "litecoin" -> prices[Material.STICK]!![3]!!
        "nano" -> prices[Material.STICK]!![5]!!
        "dogecoin" -> prices[Material.STICK]!![4]!!
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

fun LootTableItem.toItemstack(): ItemStack {
    val itemStack = itemStringToItem(this.itemString)
    itemStack.amount = this.amount
    this.enchantments?.forEach {
        val enchantment = Enchantment.getByKey(NamespacedKey.fromString(it.key)!!)!!
        if(itemStack.type == Material.ENCHANTED_BOOK){
            (itemStack.itemMeta as EnchantmentStorageMeta).addStoredEnchant(enchantment, it.value, true)
        }else {
            itemStack.addUnsafeEnchantment(enchantment, it.value)
        }
    }
    return itemStack
}

fun World.getHighestSolidBlockYAt(x: Int, z: Int): Int {
    val initBlock = this.getHighestBlockAt(x, z)
    var result = initBlock.y
    do {
        val nextBlock = this.getBlockAt(x, result, z)
        if(nextBlock.type != Material.WATER && nextBlock.type != Material.LAVA) {
            return result
        }
        result--
    }while (result > 0)
    return -1
}

fun HashMap<Int,Int>.copy(): HashMap<Int,Int> {
    val result = hashMapOf<Int, Int>()
    this.forEach {
        result[it.key] = it.value
    }
    return result
}

fun Inventory.getItem(itemStack: ItemStack): ItemStack? {
    this.contents.forEach {
        if(it != null && it.type == itemStack.type && if (itemStack.hasItemMeta() && itemStack.itemMeta.hasCustomModelData())
                try {it.itemMeta.customModelData == itemStack.itemMeta.customModelData}catch (ex:Exception){false} else true){
            return it
        }
    }
    return null
}

fun copyDirectory(sourceDirectory: File, destinationDirectory: File) {
    if (!destinationDirectory.exists()) {
        destinationDirectory.mkdir()
    }
    for (f in sourceDirectory.list()!!) {
        copyDirectoryCompatibityMode(File(sourceDirectory, f), File(destinationDirectory, f))
    }
}

private fun copyDirectoryCompatibityMode(source: File, destination: File) {
    if (source.isDirectory) {
        copyDirectory(source, destination)
    } else {
        copyFile(source, destination)
    }
}

private fun copyFile(sourceFile: File, destinationFile: File) {
    FileInputStream(sourceFile).use { `in` ->
        FileOutputStream(destinationFile).use { out ->
            val buf = ByteArray(1024)
            var length: Int
            while (`in`.read(buf).also { length = it } > 0) {
                out.write(buf, 0, length)
            }
        }
    }
}

fun JsonNode.getStringOrDefault(name: String, default: String): String {
    return this[name].asText().ifEmpty { default }
}