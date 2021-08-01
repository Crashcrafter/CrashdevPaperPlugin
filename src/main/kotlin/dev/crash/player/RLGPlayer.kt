package dev.crash.player

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crash.*
import dev.crash.permission.rankData
import dev.crash.permission.ranks
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.io.File
import java.util.Random
import kotlin.properties.Delegates

class RLGPlayer {

    var player by Delegates.notNull<Player>()
    var rank by Delegates.notNull<Int>()
    var remainingClaims by Delegates.notNull<Int>()
    var addedClaims by Delegates.notNull<Int>()
    var homes: HashMap<String, Block> = HashMap()
    var remainingHomes by Delegates.notNull<Int>()
    var addedHomes by Delegates.notNull<Int>()
    var balance by Delegates.notNull<Long>()
    var lastKeys by Delegates.notNull<Long>()
    var weeklyKeys = HashMap<Int, Int>()
    var xpLevel by Delegates.notNull<Int>()
    var xp by Delegates.notNull<Long>()
    var vxpLevel by Delegates.notNull<Int>()
    var guildId = 0
    var isMod = false
    var mana = 0
    var managen: Job? = null
    var lastDailyQuest by Delegates.notNull<Long>()
    var lastWeeklyQuest by Delegates.notNull<Long>()
    var quests :MutableList<Quest> = mutableListOf()
    var hasDaily by Delegates.notNull<Boolean>()
    var hasWeekly by Delegates.notNull<Boolean>()
    var deathPos: Location? = null
    var disabledMovement = false
    var dropCoolDown by Delegates.notNull<Long>()
    var elytraCoolDown = System.currentTimeMillis() + 1000*30
    var mutedUntil = System.currentTimeMillis()
    val playerLinkCounter = mutableListOf<Long>()
    val playerOffenseCounter = mutableListOf<Long>()
    val playerAfkCounter = mutableListOf<Long>()
    var warns = mutableListOf<Warn>()
    var chunks = mutableListOf<String>()

    constructor(player: Player) {
        val saveFile = File("${INSTANCE.dataFolder.path}/player/${player.uniqueId}.json")
        val saveObj = jacksonObjectMapper().readValue<PlayerSaveData>(saveFile)
        this.player = player
        this.rank = saveObj.rank
        this.remainingClaims = saveObj.remainingClaims
        val homes = hashMapOf<String, Block>()
        saveObj.homepoints.forEach {
            homes[it.key] = getBlockByPositionString(it.value)
        }
        this.homes = homes
        this.addedClaims = saveObj.addedClaims
        this.addedHomes = saveObj.addedHomes
        this.remainingHomes = saveObj.remainingHomes
        this.balance = saveObj.balance
        this.lastKeys = saveObj.lastKeys
        this.weeklyKeys = saveObj.leftKeys
        this.xpLevel = saveObj.xpLevel
        this.xp = saveObj.xp
        this.vxpLevel = saveObj.vxpLevel
        while(xpLevel > vxpLevel){
            if(isSpace(player.inventory)){
                player.inventory.addItem(genKey(5))
                vxpLevel++
            }else break
        }
        this.isMod = ranks[rank]!!.isMod
        saveObj.quests.forEach {
            this.quests.add(Quest(it.qid, player.uniqueId.toString(), it.isDaily, it.status, it.progress))
        }
        this.lastDailyQuest = saveObj.lastDailyQuest
        this.lastWeeklyQuest = saveObj.lastWeeklyQuest
        this.hasDaily = saveObj.hasDaily
        this.hasWeekly = saveObj.hasWeekly
        this.dropCoolDown = System.currentTimeMillis() + 1000 * 60 * (10+ Random().nextInt(10))
        this.guildId = saveObj.guildId
        this.warns = saveObj.warns.toMutableList()
        this.chunks = saveObj.chunks.toMutableList()
        changeMana(0)
        this.setName()
        check()
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
        while(xpLevel > vxpLevel){
            if(isSpace(player.inventory)){
                player.inventory.addItem(genKey(5))
                vxpLevel++
            }else break
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
            for(i in 0 until it.value){
                if(isSpace(playerInv)){
                    playerInv.addItem(genKey(it.key))
                    weeklyKeys[it.key] = weeklyKeys[it.key]!!-1
                    if(weeklyKeys[it.key]!! == 0){
                        weeklyKeys.remove(it.key)
                        break
                    }
                }else return@forEach
            }
        }
        lastKeys = System.currentTimeMillis()
        if(weeklyKeys.size == 0) println(player.name + " hat alle Keys bekommen")
    }

    fun setHome(keyWord: String){
        homes[keyWord] = player.location.block
        remainingHomes--
        player.sendMessage("§2Dein Homepoint $keyWord wurde gesetzt!")
    }

    fun delHome(keyWord: String){
        homes.remove(keyWord)
        remainingHomes++
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

    fun save(){
        val uuid = player.uniqueId.toString()
        val questList = mutableListOf<QuestSaveObj>()
        quests.forEach {
            questList.add(QuestSaveObj(it.qid, it.status, it.counter, it.isDaily))
        }
        val homeList = hashMapOf<String, String>()
        homes.forEach {
            homeList[it.key] = it.value.toPositionString()
        }
        val playerSaveObj = PlayerSaveData(uuid, rank, remainingClaims, mutableListOf(), remainingHomes, addedClaims, addedHomes, balance, questList,
            hasDaily, hasWeekly, lastDailyQuest, lastWeeklyQuest, xpLevel, xp, vxpLevel, guildId, lastKeys, weeklyKeys, homeList, warns, chunks)
        val file = File("${INSTANCE.dataFolder.path}/player/${player.uniqueId}.json")
        if(!file.exists()) file.createNewFile()
        jacksonObjectMapper().writeValue(file, playerSaveObj)
    }

    fun check(){
        if(rankData().claims + addedClaims - chunks.size != remainingClaims){
            remainingClaims = rankData().claims + addedClaims - chunks.size
        }
        if(rankData().homes + addedHomes - homes.size != remainingHomes){
            remainingHomes = rankData().homes + addedHomes - homes.size
        }
        save()
    }
}