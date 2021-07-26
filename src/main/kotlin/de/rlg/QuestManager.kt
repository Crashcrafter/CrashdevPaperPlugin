package de.rlg

import de.rlg.items.CustomItems
import de.rlg.items.ciName
import de.rlg.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate
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
            dailyquests[qid]!!
        } else {
            weeklyquests[qid]!!
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

fun initQuests() {
    dailyquests[1] = Quest(1, Reward(1000, 1250), "Ungewöhnlicher Sammler", 3, "Schließe 3 Ungewöhnliche Drops ab", true)
    dailyquests[2] = Quest(2, Reward(625, 1250), "Monster Hunter", 100, "Töte 100 feindliche Mobs", true)
    dailyquests[3] = Quest(3, Reward(750, 1500), "Killing Streak", 60, "Töte 60 Zombies", true)
    dailyquests[4] = Quest(4, Reward(1000, 1000), "Supreme!", 1, "Absolviere einen Epic-Drop alleine", true)
    dailyquests[5] = Quest(5, Reward(750, 1500), "Angler", 20, "Angle 20 mal Kabeljau(Cod)", true)
    dailyquests[6] = Quest(6, Reward(1000, 1000), "Unboxing!", 3, "Öffne 3 Crates", true)
    dailyquests[7] = Quest(7, Reward(1500, 2000), "Reisender", 10, "Absolviere 10 Drops", true)
    dailyquests[8] = Quest(8, Reward(750, 1000), "Servervoter", 4, "Vote 4 mal für den Server(/vote)", true)
    dailyquests[9] = Quest(9, Reward(1000, 1000), "Kopfjäger", 3, "Töte 3 andere Spieler", true)
    dailyquests[10] = Quest(10, Reward(1000, 1000), "Epische Dropjäger", 2, "Absolviere 2 epische Drops", true)
    dailyquests[11] = Quest(11, Reward(500, 750), "Namenangler", 1, "Angle einen Nametag", true)
    dailyquests[12] = Quest(12, Reward(500, 1000), "Händler", 2500, "Verkaufe Items für 2.500 Credits an den Shop", true)
    dailyquests[13] = Quest(13, Reward(500, 500), "Enchanter", 3, "Verzaubere 3 Items", true)
    dailyquests[14] = Quest(14, Reward(250, 500), "Fleisch-Lieferant", 30, "Töte 30 Kühe", true)
    dailyquests[15] = Quest(15, Reward(750, 1000), "Gewöhnlicher Sammler", 5, "Beende 5 gewöhnliche Drops", true)
    dailyquests[16] = Quest(16, Reward(600, 1000), "Pillager-Jäger", 15, "Töte 15 Pillager", true)
    dailyquests[17] = Quest(17, Reward(750, 1500), "Mana-Manufaktur", 1, "Stelle 1 Mana-Dust her", true)
    val is1 = ItemStack(Material.NAME_TAG)
    val im1 = is1.itemMeta
    im1.setCustomModelData(1)
    is1.itemMeta = im1
    weeklyquests[1] = Quest(1, Reward(2500, 1000, is1), "Besiege das End!", 1, "Besiege den Enderdrache einmal", false)
    weeklyquests[2] = Quest(2, Reward(2500, 1000), "Weg des Magiers", 1, "Crafte einen Zauberstab", false)
    weeklyquests[3] = Quest(3, Reward(5000, 5000, is1), "Weg zum Top-Voter", 20, "Vote 20-mal für den Server", false)
    weeklyquests[4] = Quest(4, Reward(2500, 2000), "Verdorben", 1, "Töte den Wither einmal", false)
    weeklyquests[5] = Quest(5, Reward(5000, 4500, is1), "Drop-Sammler", 25, "Schließe 25 Drops ab", false)
    weeklyquests[6] = Quest(6, Reward(2000, 1500), "Monsterjäger", 500, "Töte 500 Monster", false)
    weeklyquests[7] = Quest(7, Reward(3000, 3000), "Held der Dörfer!", 5, "Gewinne 5 Raids", false)
    weeklyquests[8] = Quest(8, Reward(2000, 2250), "Schlauer Händler", 12500, "Verdiene 12.500 Credits durch den Shop", false)
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
    val rlgPlayer = player.rlgPlayer()
    if (rlgPlayer.quests.size > 0) {
        for (quest in rlgPlayer.quests) {
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
    player.rlgPlayer().changeXP(quest.reward.xp.toLong())
    if (quest.reward.itemStack != null) {
        if (quest.reward.itemStack!!.type == Material.NAME_TAG && quest.reward.itemStack!!.itemMeta.hasCustomModelData()) {
            player.inventory.addItem(genKey(quest.reward.itemStack!!.itemMeta.customModelData))
        } else {
            player.inventory.addItem(quest.reward.itemStack!!)
        }
    }
    player.sendMessage("§2Du hast die Quest " + quest.name + " erfolgreich abgeschlossen")
    player.updateScoreboard()
}

fun getQuestLore(quest: Quest, canStart: Boolean): MutableList<String> {
    val list: MutableList<String> = ArrayList()
    if (quest.isDaily) {
        list.add("§eTägliche Quest")
    } else {
        list.add("§eWöchentliche Quest")
    }
    list.add("")
    val color: String = when(quest.status){
        0 -> "§c"
        1 -> "§e"
        else -> "§a"
    }
    list.add("§6" + quest.targetdesc + "§f: " + color + "(" + quest.counter + "/" + quest.needed + ")")
    list.add("")
    list.add("§2Belohnung:")
    list.add("§a-" + quest.reward.credits + " Credits")
    list.add("§a-" + quest.reward.xp + " XP")
    if (quest.reward.itemStack != null) {
        if (quest.reward.itemStack!!.itemMeta.hasCustomModelData()) {
            list.add("§a-" + ciName(quest.reward.itemStack!!.type, quest.reward.itemStack!!.itemMeta.customModelData))
        } else {
            list.add("§a-" + quest.reward.itemStack!!.type.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial())
        }
    }
    if (canStart) {
        list.add("")
        if (quest.status == 0) {
            list.add("§aJetzt starten!")
        } else if (quest.status == 1) {
            if (quest.counter >= quest.needed) {
                list.add("§2Jetzt Belohnung abholen!")
            } else {
                list.add("§6Im Gange...")
            }
        } else {
            list.add("§aAbgeschlossen!")
        }
    }
    list.add("")
    list.add("QID:" + quest.qid)
    return list
}

fun getDailyLore(player: Player): MutableList<String> {
    var hascompleted = true
    val rlgPlayer = player.rlgPlayer()
    for (quest in rlgPlayer.quests) {
        if (quest.isDaily && quest.status != 2) {
            hascompleted = false
            break
        }
    }
    val list: MutableList<String> = ArrayList()
    list.add("§eEinmal pro Tag")
    list.add("")
    if (hascompleted) {
        list.add("§2Schließe alle täglichen Quests ab!")
        list.add("")
        list.add("§2Belohnung:")
        list.add("§a-3500 Credits")
        list.add("§a-5000 XP")
        list.add("§a-Common Key")
        list.add("")
        if (!rlgPlayer.hasDaily) {
            list.add("§2Jetzt Belohnung abholen!")
        } else {
            list.add("§aAbgeschlossen!")
        }
    } else {
        list.add("§6Schließe alle täglichen Quests ab!")
        list.add("")
        list.add("§2Belohnung:")
        list.add("§a-3500 Credits")
        list.add("§a-5000 XP")
        list.add("§a-Common Key")
    }
    return list
}

fun getWeeklyLore(player: Player): MutableList<String> {
    var hascompleted = true
    val rlgPlayer = player.rlgPlayer()
    for (quest in rlgPlayer.quests) {
        if (!quest.isDaily && quest.status != 2) {
            hascompleted = false
            break
        }
    }
    val list: MutableList<String> = ArrayList()
    list.add("§eEinmal pro Woche")
    list.add("")
    if (hascompleted) {
        list.add("§2Schließe alle wöchentlichen Quests ab!")
        list.add("")
        list.add("§2Belohnung:")
        list.add("§a-20000 Credits")
        list.add("§a-10000 XP")
        list.add("§a-Common Key")
        list.add("")
        if (!rlgPlayer.hasWeekly) {
            list.add("§2Jetzt Belohnung abholen!")
        } else {
            list.add("§aAbgeschlossen!")
        }
    } else {
        list.add("§6Schließe alle wöchentlichen Quests ab!")
        list.add("")
        list.add("§2Belohnung:")
        list.add("§a-15000 Credits")
        list.add("§a-10000 XP")
        list.add("§a-Common Key")
    }
    return list
}

fun getActiveQuests(player: Player): List<Quest> {
    val quests: MutableList<Quest> = ArrayList()
    for (quest in player.rlgPlayer().quests) {
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
    val quests: List<Quest?> = player.rlgPlayer().quests
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
    cloned.setItem(20, CustomItems.defaultCustomItem(Material.PAPER, "§eTäglicher Bonus", getDailyLore(player), 1, Pair("rlgAction", "daily")))
    cloned.setItem(24, CustomItems.defaultCustomItem(Material.PAPER, "§eWöchentlicher Bonus", getWeeklyLore(player), 1, Pair("rlgAction", "weekly")))
    player.closeInventory()
    questinventories.add(cloned)
    player.openInventory(cloned)
}

fun Quest.getQuestRole() : ItemStack = CustomItems.defaultCustomItem(Material.PAPER, "§e" + this.name, getQuestLore(this, true),
    1, Pair("rlgAction", "${this.status} ${this.qid} ${this.isDaily}"))

fun showQuests(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    if (rlgPlayer.quests.size != 0) {
        val cloned = Bukkit.createInventory(null, blanckinv!!.size, Component.text("Quests"))
        val original = blanckinv!!.contents
        val clone = original.copyOf()
        cloned.contents = clone
        val processedQuests: MutableList<Int> = ArrayList()
        for (i in 0..2) {
            var quest: Quest? = null
            for (j in 0 until rlgPlayer.quests.size) {
                val quest1: Quest = rlgPlayer.quests[j]
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
        player.sendMessage("§4Du hast keine offenen Quests!")
    }
}

fun questClickHandler(player: Player, inventory: Inventory, slot: Int) {
    val itemStack = inventory.getItem(slot)!!
    val data = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgAction"), PersistentDataType.STRING) ?: return
    val rlgPlayer = player.rlgPlayer()
    val dataArray = data.split(" ")
    when(dataArray[0]){
        "0" -> {
            if(player.canGetQuest()){
                val qid = dataArray[1].toInt()
                val isDaily = dataArray[2].toBoolean()
                val quest = rlgPlayer.quests.first { it.qid == qid && it.isDaily == isDaily }
                quest.status = 1
                player.sendMessage("§2Du hast die Quest " + quest.name + " erfolgreich gestarted!")
                player.closeInventory()
                player.updateScoreboard()
            }else player.sendMessage("§4Du kannst nicht mehr Quests gleichzeitig haben!")
        }
        "1" -> {
            val qid = dataArray[1].toInt()
            val isDaily = dataArray[2].toBoolean()
            val quest = rlgPlayer.quests.first { it.qid == qid && it.isDaily == isDaily }
            if(quest.counter >= quest.needed){
                questCompleted(quest, player)
                player.closeInventory()
            }else {
                player.sendMessage("§4Schließe die Quest ab, um die Belohnung zu erhalten!")
            }
        }
        "daily" -> {
            if(!rlgPlayer.hasDaily){
                var hascompleted = true
                for (quest in rlgPlayer.quests) {
                    if (quest.isDaily && quest.status != 2) {
                        hascompleted = false
                        break
                    }
                }
                if(hascompleted) {
                    rlgPlayer.hasDaily = true
                    giveBalance(player, 3500, "Täglicher Bonus")
                    rlgPlayer.changeXP(5000)
                    player.inventory.addItem(genKey(1))
                    player.sendMessage("§2Du hast deinen täglichen Bonus erhalten!")
                    player.closeInventory()
                }
            }
        }
        "weekly" -> {
            if(!rlgPlayer.hasWeekly){
                var hascompleted = true
                for (quest in rlgPlayer.quests) {
                    if (!quest.isDaily && quest.status != 2) {
                        hascompleted = false
                        break
                    }
                }
                if(hascompleted) {
                    rlgPlayer.hasWeekly = true
                    giveBalance(player, 15000, "Täglicher Bonus")
                    rlgPlayer.changeXP(10000)
                    player.inventory.addItem(genKey(1))
                    player.sendMessage("§2Du hast deinen wöchentlichen Bonus erhalten!")
                    player.closeInventory()
                }
            }
        }
    }
}

fun dailyQuestCreation(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    val dailyChosen: MutableList<Int> = ArrayList()
    for (i in 0..2) {
        dailyChosen.add(createDailyQuest(i, player, dailyChosen))
        rlgPlayer.hasDaily = false
    }
    qidUpdate(player, true)
    rlgPlayer.changeXP(40)
    giveBalance(player, 100, "Täglicher Login")
}

fun qidUpdate(player: Player, onlyDaily: Boolean){
    val rlgPlayer = player.rlgPlayer()
    val quests: List<Quest> = rlgPlayer.quests
    val questStatusBuilder = StringBuilder()
    val questProgressBuilder = StringBuilder()
    val questIdsBuilder = StringBuilder()
    for (i in 0..5) {
        val quest = quests[i]
        questStatusBuilder.append(quest.status.toString() + " ")
        questProgressBuilder.append(quest.counter.toString() + " ")
        questIdsBuilder.append(quest.qid).append(" ")
        if (i == 2) {
            questStatusBuilder.append(if (rlgPlayer.hasDaily) 2 else 0).append(" ")
        } else if (i == 5) {
            questStatusBuilder.append(if (rlgPlayer.hasWeekly) 2 else 0).append(" ")
        }
    }
    val questIds = questIdsBuilder.toString().substring(0, questIdsBuilder.toString().length - 1)
    val questStatus = questStatusBuilder.toString().substring(0, questStatusBuilder.toString().length - 1)
    val questProgress = questProgressBuilder.toString().substring(0, questProgressBuilder.toString().length - 1)
    transaction { 
        PlayersTable.update(where = {PlayersTable.uuid eq player.uniqueId.toString()}){
            it[PlayersTable.quests] = questIds
            it[PlayersTable.questStatus] = questStatus
            it[PlayersTable.questProgress] = questProgress
            it[lastDailyQuest] = LocalDate.now()
            if(!onlyDaily) it[lastWeeklyQuest] = LocalDate.now()
        }
    }
}

private fun createDailyQuest(i: Int, player: Player, dailyChosen:MutableList<Int>): Int{
    val random = Random()
    var randomid = random.nextInt(dailyquests.size) + 1
    while (dailyChosen.contains(randomid)) {
        randomid = random.nextInt(dailyquests.size) + 1
    }
    dailyChosen.add(randomid)
    player.rlgPlayer().quests[i] = Quest(
        randomid, player.uniqueId.toString(), true,
        0, 0
    )
    return randomid
}

fun weeklyQuestCreation(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    dailyQuestCreation(player)
    val weeklyChosen: MutableList<Int> = ArrayList()
    for (i in 3..5) {
        val random = Random()
        var randomid = random.nextInt(weeklyquests.size) + 1
        while (weeklyChosen.contains(randomid)) {
            randomid = random.nextInt(weeklyquests.size) + 1
        }
        weeklyChosen.add(randomid)
        if (rlgPlayer.quests.size <= i) {
            rlgPlayer.quests.add(
                i, Quest(
                    randomid, player.uniqueId.toString(), false,
                    0, 0
                )
            )
        } else {
            rlgPlayer.quests[i] = Quest(
                randomid, player.uniqueId.toString(), false,
                0, 0
            )
        }
    }
    rlgPlayer.hasDaily = false
    rlgPlayer.hasWeekly = false
    qidUpdate(player, false)
    player.updateScoreboard()
}

fun Player.canGetQuest(): Boolean {
    var count = 0
    val rlgPlayer = this.rlgPlayer()
    for (quest in rlgPlayer.quests) {
        if (quest.status == 1) {
            count++
        }
    }
    var limit = 2
    if (rlgPlayer.rank > 0) {
        limit = 3
    }
    val can = count < limit
    if (!can) {
        this.sendMessage("§4Du kannst maximal $limit Quests gleichzeitig machen!")
    }
    return can
}