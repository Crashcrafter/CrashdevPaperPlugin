package de.rlg.player

import de.rlg.*
import de.rlg.permission.rankData
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
    private var leftWeeklyKeys by Delegates.notNull<String>()
    private var hasWeeklyKeys by Delegates.notNull<Boolean>()
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
    var randomTpCoolDown = System.currentTimeMillis()
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
        var weeklyStatusString = "0 0 0"
        if(rank != 0){
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
        this.leftWeeklyKeys = weeklyStatusString
        this.hasWeeklyKeys = weeklyStatusString == "0 0 0"
        this.xpLevel = xpLevel
        this.xp = xp
        this.vxpLevel = vxpLevel
        this.isMod = rankData[rank]!!.isMod

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
        if(rank == 0) {
            player.sendMessage("§cDu kannst keine wöchentlichen Keys claimen!")
            return
        }
        if(leftWeeklyKeys == "0 0 0") {
            player.sendMessage("§cDu hast schon alle wöchentlichen Keys bekommen!")
            return
        }
        val playerInv: Inventory = player.inventory
        val keys = leftWeeklyKeys.split(" ").toTypedArray()
        println(player.name + " is getting his Keys: " + leftWeeklyKeys)
        var common = keys[0].toInt()
        val commonv = common
        for (i in 0 until commonv) {
            if (isSpace(playerInv, 1)) {
                playerInv.addItem(genKey(1))
                common--
            } else {
                changeLeftKeys(common, keys[1].toInt(), keys[2].toInt())
                return
            }
        }
        var epic = keys[1].toInt()
        val epicv = epic
        for (i in 0 until epicv) {
            if (isSpace(playerInv, 1)) {
                playerInv.addItem(genKey(2))
                epic--
            } else {
                changeLeftKeys(common, epic, keys[2].toInt())
                return
            }
        }
        var supreme = keys[2].toInt()
        val supremev = supreme
        for (i in 0 until supremev) {
            if (isSpace(playerInv, 1)) {
                playerInv.addItem(genKey(3))
                supreme--
            } else {
                changeLeftKeys(common, epic, supreme)
                return
            }
        }
        println(player.name + " hat alle Keys bekommen")
        changeLeftKeys(common, epic, supreme)
    }

    private fun changeLeftKeys(common: Int, epic: Int, supreme: Int) {
        leftWeeklyKeys = "$common $epic $supreme"
        player.sendMessage(if(leftWeeklyKeys != "0 0 0") "§6Du hast einen Teil deiner wöchentlichen Keys erhalten!\nUm die Restlichen zu erhalten, musst du Platz im Inventar haben und neu joinen!"
        else "§aDu hast alle wöchentlichen Keys erhalten!")
        transaction {
            ProcessedTable.update(where = {ProcessedTable.uuid eq player.uniqueId.toString()}){
                it[leftKeys] = leftWeeklyKeys
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
            Component.text("${rankData[rank]!!.prefix} ${player.name}")
        }else {
            Component.text("${rankData[rank]!!.prefix} ${player.name} §8[§6${this.guild()!!.suffix}§8]§r")
        }
        player.playerListName(playerTextComponent)
        player.displayName(playerTextComponent)
        player.customName(playerTextComponent)
        player.isCustomNameVisible = true
    }
}