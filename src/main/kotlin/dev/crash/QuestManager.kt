package dev.crash

import dev.crash.items.CustomItems
import dev.crash.items.ciName
import dev.crash.permission.rankData
import dev.crash.player.crashPlayer
import dev.crash.player.CrashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

var dailyquests = HashMap<Int, Quest>()
var weeklyquests = HashMap<Int, Quest>()
private var blanckinv: Inventory? = null

class Reward {
    constructor(credits: Long, xp: Int) {
        this.credits = credits
        this.xp = xp
    }

    constructor(credits: Long, xp: Int, itemStack: ItemStack?) {
        this.credits = credits
        this.xp = xp
        this.itemStack = itemStack
    }

    var credits: Long
    var xp: Int
    var itemStack: ItemStack? = null
}

class Quest {
    constructor(qid: Int, reward: Reward, name: String, needed: Int, targetdesc: String, isDaily: Boolean) {
        this.qid = qid
        this.reward = reward
        this.name = name
        this.needed = needed
        this.targetdesc = targetdesc
        this.isDaily = isDaily
    }

    constructor(qid: Int, uuid: String, daily: Boolean, status: Int, progress: Int) {
        val quest: Quest = if (daily) {
            dailyquests[qid] ?: dailyquests[Random().nextInt(dailyquests.size-1)]!!
        } else {
            weeklyquests[qid] ?: weeklyquests[Random().nextInt(weeklyquests.size-1)]!!
        }
        this.uuid = uuid
        this.status = status
        isDaily = daily
        counter = progress
        this.qid = quest.qid
        reward = quest.reward
        name = quest.name
        needed = quest.needed
        targetdesc = quest.targetdesc
    }

    var status: Int = 0
    var qid: Int
    var reward: Reward
    var name: String
    var targetdesc: String
    var needed: Int
    var uuid: String = ""
    fun changeCounter(amount: Int) {
        counter += amount
    }

    var counter = 0
    var isDaily: Boolean
}

internal fun initQuests() {
    dailyquests[1] = Quest(1, Reward(3000, 1250), "Uncommon Collector", 3, "Complete 3 Uncommon drops", true)
    dailyquests[2] = Quest(2, Reward(1600, 1250), "Monster Hunter", 100, "Kill 100 hostile mobs", true)
    dailyquests[3] = Quest(3, Reward(1750, 1500), "Killing Streak", 50, "Kill 50 Zombies", true)
    dailyquests[4] = Quest(4, Reward(5000, 1000), "Epic!", 1, "Complete 1 Epic drop alone", true)
    dailyquests[5] = Quest(5, Reward(1750, 1500), "Fisherman", 10, "Fish 10 Cod", true)
    dailyquests[6] = Quest(6, Reward(2500, 1000), "Unboxing!", 3, "Open 3 crates", true)
    dailyquests[7] = Quest(7, Reward(4500, 2000), "Traveller", 5, "Complete 5 drops", true)
    dailyquests[8] = Quest(8, Reward(2750, 1000), "Servervoter", 4, "Vote 4 times for the server(/vote)", true)
    dailyquests[9] = Quest(9, Reward(3000, 1000), "Head Hunter", 3, "Kill 3 other player", true)
    dailyquests[10] = Quest(10, Reward(5000, 1000), "Epic Drop-Hunter", 2, "Complete 2 epic drops", true)
    dailyquests[11] = Quest(11, Reward(2500, 750), "Nametag-Fishing", 1, "Fish a nametag", true)
    dailyquests[12] = Quest(12, Reward(1500, 1000), "Trader", 2500, "Sell items for 2.500 Credits at the shop", true)
    dailyquests[13] = Quest(13, Reward(1500, 500), "Enchanter", 3, "Enchant 3 items", true)
    dailyquests[14] = Quest(14, Reward(1250, 500), "Meat...", 30, "Kill 30 Cows", true)
    dailyquests[15] = Quest(15, Reward(2250, 1000), "Common Collector", 5, "Complete 5 common drops", true)
    dailyquests[16] = Quest(16, Reward(1600, 1000), "Pillager-Hunter", 15, "Kill 15 Pillager", true)
    val is1 = ItemStack(Material.NAME_TAG)
    val im1 = is1.itemMeta
    im1.setCustomModelData(1)
    is1.itemMeta = im1
    weeklyquests[1] = Quest(1, Reward(7500, 2000, is1), "Free the End!", 1, "Kill the Enderdragon once", false)
    weeklyquests[2] = Quest(2, Reward(2500, 1000), "Way of Magician", 1, "Craft a staff", false)
    weeklyquests[3] = Quest(3, Reward(5000, 5000, is1), "Way to Top-Voter", 20, "Vote 20 times for the server (/vote)", false)
    weeklyquests[4] = Quest(4, Reward(3500, 2000), "Corrupted", 1, "Kill the wither once", false)
    weeklyquests[5] = Quest(5, Reward(5000, 4500, is1), "Drop-Collector", 25, "Complete 25 drops", false)
    weeklyquests[6] = Quest(6, Reward(4000, 1500), "Monster Hunter", 500, "Kill 500 hostile mobs", false)
    weeklyquests[7] = Quest(7, Reward(5000, 3000), "Hero of the Village!", 5, "Win 5 raids", false)
    weeklyquests[8] = Quest(8, Reward(3000, 2750, is1), "Smart Trader", 25000, "Get 25.000 from the shop", false)
    val inventory = Bukkit.createInventory(null, 27, Component.text("Quests"))
    val itemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    val im = itemStack.itemMeta
    im.displayName(Component.text(" "))
    itemStack.itemMeta = im
    for (j in 0 until inventory.size) {
        inventory.setItem(j, itemStack)
    }
    blanckinv = inventory
}

fun questCount(player: Player, qid: Int, amount: Int, daily: Boolean) {
    val crashPlayer = player.crashPlayer()
    if (crashPlayer.quests.size > 0) {
        for (quest in crashPlayer.quests) {
            if (quest.qid == qid && quest.status == 1 && quest.isDaily == daily) {
                quest.changeCounter(amount)
                player.updateScoreboard()
                break
            }
        }
    }
}

fun questCompleted(quest: Quest, player: Player) {
    quest.status = 2
    giveBalance(player, quest.reward.credits, "Quest")
    player.crashPlayer().changeXP(quest.reward.xp.toLong())
    if (quest.reward.itemStack != null) {
        if (quest.reward.itemStack!!.type == Material.NAME_TAG && quest.reward.itemStack!!.itemMeta.hasCustomModelData()) {
            player.inventory.addItem(genKey(quest.reward.itemStack!!.itemMeta.customModelData))
        } else {
            player.inventory.addItem(quest.reward.itemStack!!)
        }
    }
    player.sendMessage("§2You successfully completed the quest ${quest.name}")
    player.updateScoreboard()
}

fun getQuestLore(quest: Quest, canStart: Boolean): MutableList<String> {
    val list: MutableList<String> = ArrayList()
    if (quest.isDaily) {
        list.add("§eDaily Quest")
    } else {
        list.add("§eWeekly Quest")
    }
    list.add("")
    val color: String = when(quest.status){
        0 -> "§c"
        1 -> "§e"
        else -> "§a"
    }
    list.add("§6" + quest.targetdesc + "§f: " + color + "(" + quest.counter + "/" + quest.needed + ")")
    list.add("")
    list.add("§2Reward:")
    list.add("§a-" + quest.reward.credits + " Credits")
    list.add("§a-" + quest.reward.xp + " XP")
    if (quest.reward.itemStack != null) {
        if (quest.reward.itemStack!!.itemMeta.hasCustomModelData()) {
            var name = ciName(quest.reward.itemStack!!.type, quest.reward.itemStack!!.itemMeta.customModelData)!!
            while (name.startsWith("§")){
                name = name.drop(2)
            }
            list.add("§a-$name")
        } else {
            list.add("§a-" + quest.reward.itemStack!!.type.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial())
        }
    }
    if (canStart) {
        list.add("")
        if (quest.status == 0) {
            list.add("§aStart Now!")
        } else if (quest.status == 1) {
            if (quest.counter >= quest.needed) {
                list.add("§2Collect rewards now!")
            } else {
                list.add("§6Ongoing...")
            }
        } else {
            list.add("§aCompleted!")
        }
    }
    list.add("")
    list.add("QID:" + quest.qid)
    return list
}

fun getDailyLore(player: Player): MutableList<String> {
    var hascompleted = true
    val crashPlayer = player.crashPlayer()
    for (quest in crashPlayer.quests) {
        if (quest.isDaily && quest.status != 2) {
            hascompleted = false
            break
        }
    }
    val list: MutableList<String> = ArrayList()
    list.add("§eOnce per day")
    list.add("")
    list.add("§2Complete all daily quests!")
    list.add("")
    list.add("§2Reward:")
    list.add("§a-3500 Credits")
    list.add("§a-5000 XP")
    list.add("§a-Common Key")
    if(hascompleted){
        list.add("")
        if (!crashPlayer.hasDaily) {
            list.add("§2Collect rewards now!")
        } else {
            list.add("§aCompleted!")
        }
    }
    return list
}

fun getWeeklyLore(player: Player): MutableList<String> {
    var hascompleted = true
    val crashPlayer = player.crashPlayer()
    for (quest in crashPlayer.quests) {
        if (!quest.isDaily && quest.status != 2) {
            hascompleted = false
            break
        }
    }
    val list: MutableList<String> = ArrayList()
    list.add("§eOnce per week")
    list.add("")
    list.add("§2Complete all weekly quests!")
    list.add("")
    list.add("§2Reward:")
    list.add("§a-20000 Credits")
    list.add("§a-10000 XP")
    list.add("§a-Epic Key")
    if(hascompleted){
        list.add("")
        if (!crashPlayer.hasWeekly) {
            list.add("§2Collect rewards now!")
        } else {
            list.add("§2Completed!")
        }
    }
    return list
}

fun getActiveQuests(player: Player): List<Quest> {
    val quests: MutableList<Quest> = ArrayList()
    for (quest in player.crashPlayer().quests) {
        if (quest.status == 1) {
            quests.add(quest)
        }
    }
    return quests
}

fun showAvailableQuests(player: Player) {
    val cloned = Bukkit.createInventory(null, blanckinv!!.size, Component.text("Quests"))
    val original = blanckinv!!.contents
    val clone = original.copyOf()
    cloned.contents = clone
    val quests: List<Quest?> = player.crashPlayer().quests
    for (i in 0..2) {
        if (quests.size > i && quests[i] != null) {
            var quest = quests[i]
            var count = 1
            while (!quest!!.isDaily) {
                if (i + count >= 6) break
                quest = quests[i + count]
                count++
            }
            cloned.setItem(10 + i, quest.getQuestRole())
        }
    }
    for (i in 3..5) {
        if (quests.size > i && quests[i] != null) {
            var quest = quests[i]
            var count = 1
            while (quest!!.isDaily) {
                if (i + count >= 8) break
                quest = quests[i + count]
                count++
            }
            cloned.setItem(11 + i, quest.getQuestRole())
        }
    }
    cloned.setItem(20, CustomItems.defaultCustomItem(Material.PAPER, "§eDaily Bonus", getDailyLore(player), 1, hashMapOf("crashAction" to "daily")))
    cloned.setItem(24, CustomItems.defaultCustomItem(Material.PAPER, "§eWeekly Bonus", getWeeklyLore(player), 1, hashMapOf("crashAction" to "weekly")))
    player.closeInventory()
    questinventories.add(cloned)
    player.openInventory(cloned)
}

fun Quest.getQuestRole() : ItemStack = CustomItems.defaultCustomItem(Material.PAPER, "§e" + this.name, getQuestLore(this, true),
    1, hashMapOf("crashAction" to "${this.status} ${this.qid} ${this.isDaily}"))

fun showQuests(player: Player) {
    val crashPlayer = player.crashPlayer()
    if (crashPlayer.quests.size != 0) {
        val cloned = Bukkit.createInventory(null, blanckinv!!.size, Component.text("Quests"))
        val original = blanckinv!!.contents
        val clone = original.copyOf()
        cloned.contents = clone
        val processedQuests: MutableList<Int> = ArrayList()
        for (i in 0..2) {
            var quest: Quest? = null
            for (j in 0 until crashPlayer.quests.size) {
                val quest1: Quest = crashPlayer.quests[j]
                if (quest1.status == 1 && !processedQuests.contains(j)) {
                    processedQuests.add(j)
                    quest = quest1
                    break
                }
            }
            if (quest != null) {
                cloned.setItem(i + 12, CustomItems.defaultCustomItem(Material.PAPER, "§e" + quest.name, getQuestLore(quest, false), 1))
            }
        }
        player.closeInventory()
        questinventories.add(cloned)
        player.openInventory(cloned)
    } else {
        player.sendMessage("§4You have no ongoing quests!")
    }
}

fun questClickHandler(player: Player, inventory: Inventory, slot: Int) {
    val itemStack = inventory.getItem(slot)!!
    val data = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "crashAction"), PersistentDataType.STRING) ?: return
    val crashPlayer = player.crashPlayer()
    val dataArray = data.split(" ")
    when(dataArray[0]){
        "0" -> {
            if(player.canGetQuest()){
                val qid = dataArray[1].toInt()
                val isDaily = dataArray[2].toBoolean()
                val quest = crashPlayer.quests.first { it.qid == qid && it.isDaily == isDaily }
                quest.status = 1
                player.sendMessage("§2You started the quest ${quest.name}")
                player.closeInventory()
                player.updateScoreboard()
            }else player.sendMessage("§4You can't have more quests at the same time!")
        }
        "1" -> {
            val qid = dataArray[1].toInt()
            val isDaily = dataArray[2].toBoolean()
            val quest = crashPlayer.quests.first { it.qid == qid && it.isDaily == isDaily }
            if(quest.counter >= quest.needed){
                questCompleted(quest, player)
                player.closeInventory()
            }else {
                player.sendMessage("§4Complete the quest to receive your reward!")
            }
        }
        "daily" -> {
            if(!crashPlayer.hasDaily){
                var hascompleted = true
                for (quest in crashPlayer.quests) {
                    if (quest.isDaily && quest.status != 2) {
                        hascompleted = false
                        break
                    }
                }
                if(hascompleted) {
                    crashPlayer.hasDaily = true
                    giveBalance(player, 3500, "Daily Bonus")
                    crashPlayer.changeXP(5000)
                    player.inventory.addItem(genKey(1))
                    player.sendMessage("§2You received your daily bonus!")
                    player.closeInventory()
                }
            }
        }
        "weekly" -> {
            if(!crashPlayer.hasWeekly){
                var hascompleted = true
                for (quest in crashPlayer.quests) {
                    if (!quest.isDaily && quest.status != 2) {
                        hascompleted = false
                        break
                    }
                }
                if(hascompleted) {
                    crashPlayer.hasWeekly = true
                    giveBalance(player, 15000, "Weekly Bonus")
                    crashPlayer.changeXP(10000)
                    player.inventory.addItem(genKey(2))
                    player.sendMessage("§2You received your weekly bonus!")
                    player.closeInventory()
                }
            }
        }
    }
}

fun CrashPlayer.dailyQuestCreation() {
    val dailyChosen: MutableList<Int> = ArrayList()
    for (i in 0..2) {
        dailyChosen.add(createDailyQuest(i, player, dailyChosen))
        hasDaily = false
    }
    changeXP(100)
    lastDailyQuest = System.currentTimeMillis()
    giveBalance(player, 500, "Daily Login")
}

private fun createDailyQuest(i: Int, player: Player, dailyChosen:MutableList<Int>): Int{
    val random = Random()
    var randomid = random.nextInt(dailyquests.size) + 1
    while (dailyChosen.contains(randomid)) {
        randomid = random.nextInt(dailyquests.size) + 1
    }
    dailyChosen.add(randomid)
    val crashPlayer = player.crashPlayer()
    if(crashPlayer.quests.size <= i){
        crashPlayer.quests.add(i, Quest(randomid, player.uniqueId.toString(), true, 0, 0))
    }else {
        crashPlayer.quests[i] = Quest(randomid, player.uniqueId.toString(), true, 0, 0)
    }
    return randomid
}

fun CrashPlayer.weeklyQuestCreation() {
    dailyQuestCreation()
    val weeklyChosen: MutableList<Int> = ArrayList()
    for (i in 3..5) {
        val random = Random()
        var randomid = random.nextInt(weeklyquests.size) + 1
        while (weeklyChosen.contains(randomid)) {
            randomid = random.nextInt(weeklyquests.size) + 1
        }
        weeklyChosen.add(randomid)
        if (quests.size <= i) {
            quests.add(i, Quest(randomid, player.uniqueId.toString(), false, 0, 0))
        } else {
            quests[i] = Quest(randomid, player.uniqueId.toString(), false, 0, 0)
        }
    }
    lastWeeklyQuest = System.currentTimeMillis()
    hasDaily = false
    hasWeekly = false
    player.updateScoreboard()
}

fun Player.canGetQuest(): Boolean {
    val crashPlayer = this.crashPlayer()
    val count = crashPlayer.quests.filter { it.status == 1 }.size
    val limit = crashPlayer.rankData().quests
    val can = count < limit
    if (!can) {
        this.sendMessage("§4You can't have more than $limit quests at the same time!")
    }
    return can
}