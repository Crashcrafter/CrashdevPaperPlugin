package de.rlg.player

import de.rlg.*
import de.rlg.permission.rankData
import de.rlg.permission.ranks
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Random
import kotlin.properties.Delegates

class RLGPlayer() {

    var player by Delegates.notNull<Player>()
    var rank by Delegates.notNull<Int>()
    var remainingClaims by Delegates.notNull<Int>()
    var homes: HashMap<String, Block> = HashMap()
    var remainingHomes by Delegates.notNull<Int>()
    var balance by Delegates.notNull<Long>()
    private val weeklyKeys = HashMap<Int, Int>()
    var xpLevel by Delegates.notNull<Int>()
    var xp by Delegates.notNull<Long>()
    var vxpLevel by Delegates.notNull<Int>()
    var guildId = 0
    var isMod = false
    var mana = 0
    var managen: Job? = null

    var quests :ArrayList<Quest> = ArrayList()
    var hasDaily by Delegates.notNull<Boolean>()
    var hasWeekly by Delegates.notNull<Boolean>()

    var deathPos: Location? = null
    var lastDamage: Long? = null

    var disabledMovement = false
    var dropCoolDown by Delegates.notNull<Long>()
    var elytraCoolDown = System.currentTimeMillis() + 1000*30
    var mutedUntil = System.currentTimeMillis()
    val playerLinkCounter = mutableListOf<Long>()
    val playerOffenseCounter = mutableListOf<Long>()
    val playerAfkCounter = mutableListOf<Long>()

    constructor(player: Player, rank: Int, remainingClaims: Int, homes: HashMap<String, Block>, remainingHomes: Int, balance: Long,
                questsString: String,questsStatusString:String, questsProgressString: String, xpLevel: Int, xp: Long, vxpLevel: Int, guildId: Int) : this() {
        this.player = player
        this.rank = rank
        this.remainingClaims = remainingClaims
        this.homes = homes
        this.remainingHomes = remainingHomes
        this.balance = balance
        var weeklyStatusString = ""
        if(rankData().weeklyKeys.isNotEmpty()){
            transaction {
                val statusQuery = ProcessedTable.select(where = { ProcessedTable.uuid eq player.uniqueId.toString()})
                when {
                    statusQuery.empty() -> {
                        weeklyStatusString = getKeysPerRank(rank)
                        ProcessedTable.insert {
                            it[uuid] = player.uniqueId.toString()
                            it[leftKeys] = weeklyStatusString
                            it[lastTime] = LocalDate.now()
                        }
                        player.sendMessage("§6Du kannst deine wöchentlichen Keys abholen!\nNutze dafür /weekly")
                    }
                    statusQuery.first()[ProcessedTable.lastTime].isBefore(LocalDate.now().minus(6, ChronoUnit.DAYS)) -> {
                        weeklyStatusString = getKeysPerRank(rank)
                        ProcessedTable.update(where = {ProcessedTable.uuid eq player.uniqueId.toString()}){
                            it[leftKeys] = weeklyStatusString
                            it[lastTime] = LocalDate.now()
                        }
                        player.sendMessage("§6Du kannst deine wöchentlichen Keys abholen!\nNutze dafür /weekly")
                    }
                    else -> {
                        weeklyStatusString = statusQuery.first()[ProcessedTable.leftKeys]
                    }
                }
            }
        }
        val weeklyKeysArgs = weeklyStatusString.split(" ")
        keysData.keys.forEach {
            weeklyKeys[it] = try {
                weeklyKeysArgs[it-1].toInt()
            }catch (ex: Exception){
                0
            }
        }
        this.xpLevel = xpLevel
        this.xp = xp
        this.vxpLevel = vxpLevel
        this.isMod = ranks[rank]!!.isMod

        val questsArray = questsString.split(" ")
        val questStatusArray = questsStatusString.split(" ")
        val questProgressArray = questsProgressString.split(" ")

        for(i in 0..5){
            val daily = i < 3
            val j = when{ daily -> i else -> i+1}
            quests.add(Quest(questsArray[i].toInt(), player.uniqueId.toString(), daily, questStatusArray[j].toInt(), questProgressArray[i].toInt()))
            if(i == 2) this.hasDaily = questStatusArray[3] == "2"
            if(i == 5) this.hasWeekly = questStatusArray[7] == "2"
        }
        changeMana(0)
        this.dropCoolDown = System.currentTimeMillis() + 1000 * 60 * (10+ Random().nextInt(10))
        this.guildId = guildId
        this.setName()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun changeMana(amount: Int) {
        mana -= amount
        if (managen != null) {
            managen!!.cancel()
        }
        player.sendActionBar(Component.text("§1Mana: $mana"))
        val job = GlobalScope.launch {
            try {
                delay(2000)
                player.sendActionBar(Component.text("§1Mana: $mana"))
                delay(2000)
                player.sendActionBar(Component.text("§1Mana: $mana"))
                delay(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            while (mana < 100) {
                mana++
                player.sendActionBar(Component.text("§1Mana: $mana"))
                if (mana == 100) break
                try {
                    delay(350)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        allJobs.add(job)
        this.managen = job
    }

    fun changeXP(amount: Long) {
        xp += amount
        if(amount < 0){
            player.sendMessage("§4$amount XP")
            var xpForLevel = getEXPForLevel(xpLevel-1)
            while (xp < 0){
                xp += xpForLevel
                xpLevel--
                xpForLevel = getEXPForLevel(xpLevel-1)
            }
            player.sendMessage("§4Dir wurden Level abgezogen!")
            return
        }
        player.sendMessage("§a+$amount XP")
        if (amount > 10) player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
        println("Level Change for " + player.name + ": " + amount)
        var needed: Long = getEXPForLevel(xpLevel)
        if (xp >= needed) {
            while (xp >= needed) {
                levelUp()
                xp -= needed
                needed = getEXPForLevel(xpLevel)
            }
            player.sendMessage("§2Level Up!\n§6Du bist jetzt Level $xpLevel!")
        }
        if (amount > 100) {
            val builder = StringBuilder()
            builder.append("§6Level ").append(xpLevel).append(": ")
            val percent: Double = xp.toDouble().div(getEXPForLevel(xpLevel))
            builder.getExpDisplay(percent)
            player.sendMessage(builder.toString())
        }
    }

    private fun levelUp(){
        xpLevel++
        if(xpLevel > vxpLevel){
            if(isSpace(player.inventory, 1)){
                player.inventory.addItem(genKey(5))
                vxpLevel++
            }
        }
    }

    fun weeklyKeys() {
        if(rankData().weeklyKeys.isEmpty()) {
            player.sendMessage("§cDu kannst keine wöchentlichen Keys claimen!")
            return
        }
        if(weeklyKeys.isEmpty()) {
            player.sendMessage("§cDu hast schon alle wöchentlichen Keys bekommen!")
            return
        }
        val playerInv: Inventory = player.inventory
        val weeklyKeysCopy = weeklyKeys.copy()
        weeklyKeysCopy.forEach {
            println("${it.key} to ${it.value}")
            for(i in 0 until it.value){
                if(isSpace(playerInv, 1)){
                    playerInv.addItem(genKey(it.key))
                    weeklyKeys[it.key] = weeklyKeys[it.key]!!-1
                    if(weeklyKeys[it.key]!! == 0){
                        weeklyKeys.remove(it.key)
                        break
                    }
                }else return@forEach
            }
        }
        if(weeklyKeys.isEmpty()) println(player.name + " hat alle Keys bekommen")
        changeLeftKeys()
    }

    private fun changeLeftKeys() {
        val leftWeeklyKeys = StringBuilder()
        keysData.forEach {
            leftWeeklyKeys.append(weeklyKeys[it.key] ?: 0).append(" ")
        }
        transaction {
            ProcessedTable.update(where = {ProcessedTable.uuid eq player.uniqueId.toString()}){
                it[leftKeys] = leftWeeklyKeys.toString().removeSuffix(" ")
                it[lastTime] = LocalDate.now()
            }
        }
    }

    fun setHome(keyWord: String){
        val playerPos = player.location.block
        transaction {
            HomepointTable.insert {
                it[keyword] = keyWord
                it[uuid] = player.uniqueId.toString()
                it[homePos] = playerPos.toSQLString()
            }
        }
        homes[keyWord] = playerPos
        player.sendMessage("§2Dein Homepoint $keyWord wurde gesetzt!")
    }

    fun delHome(keyWord: String){
        transaction {
            HomepointTable.deleteWhere {
                HomepointTable.uuid eq player.uniqueId.toString() and(HomepointTable.keyword eq keyWord)
            }
        }
        player.sendMessage("§2Dein Homepoint $keyWord wurde entfernt!")
    }

    fun setName(){
        val playerTextComponent = if(guildId == 0){
            Component.text("${rankData().prefix} ${player.name}")
        }else {
            Component.text("${rankData().prefix} ${player.name} §8[§6${this.guild()!!.suffix}§8]§r")
        }
        player.playerListName(playerTextComponent)
        player.displayName(playerTextComponent)
        player.customName(playerTextComponent)
        player.isCustomNameVisible = true
    }
}