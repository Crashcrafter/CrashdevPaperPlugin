package dev.crash

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.crash.items.CustomItems
import dev.crash.permission.rankData
import dev.crash.player.getPlayerData
import dev.crash.player.modifyPlayerData
import dev.crash.player.rlgPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

var prices = HashMap<Material, HashMap<Int, Long>>()
var amountmap = hashMapOf(0 to 1, 1 to 8, 2 to 16, 3 to 64)
var shopinventories: MutableList<Inventory> = ArrayList()
var creditsScoreBoard = ""

internal fun initTradingInventories() {
    val overview: Inventory = Bukkit.createInventory(null, 9, Component.text("Shop"))
    overview.setItem(0, CustomItems.defaultCustomItem(Material.WRITTEN_BOOK, "§1Buch-Shop", arrayListOf(), 1, hashMapOf("rlgAction" to "book")))
    overview.setItem(1, CustomItems.defaultCustomItem(Material.OAK_LOG, "§cHolz-Shop", arrayListOf(), 0, hashMapOf("rlgAction" to "wood")))
    overview.setItem(2, CustomItems.defaultCustomItem(Material.POLISHED_ANDESITE, "§6Bau-Shop", arrayListOf(), 0, hashMapOf("rlgAction" to "build")))
    overview.setItem(3, CustomItems.defaultCustomItem(Material.STICK, "§eCrypto-Shop", arrayListOf(), 1, hashMapOf("rlgAction" to "crypto")))
    TradingInventories.overview = overview

    val bookOverview: Inventory = Bukkit.createInventory(null, 9, Component.text("Buch-Shop"))
    bookOverview.setItem(0, CustomItems.defaultCustomItem(Material.WRITTEN_BOOK, "Kaufe 1 Einsteiger-Buch für 100 Credits", arrayListOf(), 2, hashMapOf("rlgAction" to "book beginner")))
    TradingInventories.bookOverview = bookOverview

    val woodMaterials: List<Material> = arrayListOf(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG)
    val woodOverview: Inventory = Bukkit.createInventory(null, 54, Component.text("Holz-Shop"))
    for (j in woodMaterials.indices) {
        for (i in 0 until amountmap.size) {
            woodOverview.setItem(i + 9 * j, CustomItems.defaultCustomItem(woodMaterials[j],
                "Kaufe " + amountmap[i] + " " + woodMaterials[j].toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " für " + (amountmap[i]!! * 16) + " Credits", arrayListOf(),0,
                hashMapOf("rlgAction" to "wood ${woodMaterials[j].name.lowercase()} ${amountmap[i]}")))
        }
    }
    TradingInventories.woodOverview = woodOverview

    val buildOverview: Inventory = Bukkit.createInventory(null, 54, Component.text("Bau-Shop"))
    for (i in 0 until amountmap.size) {
        buildOverview.setItem(i, CustomItems.defaultCustomItem(Material.STONE,
            "Kaufe " + amountmap[i] + " " + Material.STONE.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " für " + (amountmap[i]!! * 16) + " Credits", arrayListOf(), 0,
        hashMapOf("rlgAction" to "build stone ${amountmap[i]}")))
    }
    val buildMaterials: List<Material> = ArrayList(
        listOf(
            Material.POLISHED_ANDESITE,
            Material.POLISHED_DIORITE,
            Material.POLISHED_GRANITE,
            Material.BRICKS,
            Material.WHITE_CONCRETE
        )
    )
    for (j in buildMaterials.indices) {
        for (i in 0 until amountmap.size) {
            buildOverview.setItem(i + (9 * j) + 9, CustomItems.defaultCustomItem(buildMaterials[j],
                "Kaufe " + amountmap[i] + " " + buildMaterials[j].toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " für " + (amountmap[i]!! * 32) + " Credits", arrayListOf(), 0,
                hashMapOf("rlgAction" to "build ${buildMaterials[j].name.lowercase()} ${amountmap[i]}")))
        }
    }
    TradingInventories.buildOverview = buildOverview

    val inventory: Inventory = Bukkit.createInventory(null, 27, Component.text("Normal Shop"))
    val itemStack = CustomItems.defaultCustomItem(Material.GRAY_STAINED_GLASS_PANE, " ", arrayListOf())
    for (i in 0 until inventory.size) {
        inventory.setItem(i, itemStack)
    }
    ShopInventories.normalView = inventory

    val blackMarketInv: Inventory = Bukkit.createInventory(null, 9, Component.text("Schwarzmarkt"))
    blackMarketInv.setItem(0, CustomItems.defaultCustomItem(Material.NAME_TAG, "Keys-Shop", arrayListOf(), 1, hashMapOf("rlgAction" to "key")))
    BlackMarketInventories.blackMarketOverview = blackMarketInv

    val blackMarketKeysInv: Inventory = Bukkit.createInventory(null, 9, Component.text("Schwarzmarkt"))
    blackMarketKeysInv.setItem(0, CustomItems.defaultCustomItem(Material.NAME_TAG, "Kaufe 1 CommonKey für 5000 Credits", arrayListOf(), 1, hashMapOf("rlgAction" to "key common")))
    blackMarketKeysInv.setItem(1, CustomItems.defaultCustomItem(Material.NAME_TAG, "Kaufe 1 EpicKey für 20000 Credits", arrayListOf(), 2, hashMapOf("rlgAction" to "key epic")))
    blackMarketKeysInv.setItem(2, CustomItems.defaultCustomItem(Material.NAME_TAG, "Kaufe 1 SupremeKey für 50000 Credits", arrayListOf(), 3, hashMapOf("rlgAction" to "key supreme")))
    BlackMarketInventories.blackMarketKeyOverview = blackMarketKeysInv
}

fun transferBalance(player: Player, target: Player, amount: Long): Boolean {
    val playerRlgPlayer = player.rlgPlayer()
    if (playerRlgPlayer.balance < amount) {
        player.sendMessage("Du hast nicht genügend Credits, um diese Transaktion durchzuführen")
        return false
    }
    playerRlgPlayer.balance -= amount
    val onePercent = amount/100
    val newAmount = amount - onePercent
    player.sendMessage("${newAmount.withPoints()} Credits wurden an " + target.name + " gesendet")
    val targetRlgPlayer = target.rlgPlayer()
    targetRlgPlayer.balance += newAmount
    target.sendMessage("Du hast " + newAmount.withPoints() + " Credits von " + player.name + " erhalten")
    player.updateScoreboard()
    target.updateScoreboard()
    return true
}

fun giveBalance(target: Player, amount: Long, reason: String) {
    val rlgPlayer = target.rlgPlayer()
    rlgPlayer.balance += amount
    if (amount < 0) {
        target.sendMessage("§4Du hast " + abs(amount).withPoints() + " Credits für " + reason + " ausgegeben")
    } else {
        target.sendMessage("§2Du hast " + abs(amount).withPoints() + " Credits durch " + reason + " erhalten")
    }
    target.updateScoreboard()
}

fun pay(target: Player, amount: Long, reason: String): Boolean {
    val rlgPlayer = target.rlgPlayer()
    return if (rlgPlayer.balance >= amount) {
        rlgPlayer.balance -= amount
        target.sendMessage("§2Du hast ${amount.withPoints()} Credits für $reason ausgegeben!")
        target.updateScoreboard()
        true
    } else {
        target.sendMessage("§4Du hast nicht genügend Credits!")
        false
    }
}

fun sellItem(player: Player) {
    val itemStack = player.inventory.itemInMainHand
    if (itemStack.hasItemMeta() && itemStack.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgCheated"), PersistentDataType.STRING)) {
        return
    }
    val rlgPlayer = player.rlgPlayer()
    val multiplier = rlgPlayer.rankData().shopMultiplier
    val cmd = if(!itemStack.itemMeta.hasCustomModelData()) 0 else itemStack.itemMeta.customModelData
    val isNotCrypto = (itemStack.type == Material.STICK && cmd !in 1..5) || itemStack.type != Material.STICK
    val value = if(isNotCrypto) (prices[itemStack.type]!![cmd]!! * itemStack.amount * multiplier).toLong() else prices[itemStack.type]!![cmd]!! * itemStack.amount
    giveBalance(player, value, "Shop")
    if(isNotCrypto){
        questCount(player, 12, value.toInt(), true)
        questCount(player, 8, value.toInt(), false)
        rlgPlayer.changeXP(floor(value / 10.0).toLong())
    }
    player.inventory.removeItem(itemStack)
    player.closeInventory()
    player.updateScoreboard()
}

fun tradingInventory(player: Player) {
    val rlgPlayer = player.rlgPlayer()
    try {
        if (player.inventory.itemInMainHand.type == Material.AIR) {
            val overview: Inventory = Bukkit.createInventory(null, 9, Component.text("Shop"))
            overview.contents = TradingInventories.overview!!.contents.copyOf()
            if(rlgPlayer.xpLevel <= 10){
                overview.setItem(3, CustomItems.defaultCustomItem(Material.STICK, "§eCrypto-Shop", arrayListOf("", "§4Level 10 benötigt"), 1, hashMapOf("rlgAction" to "crypto")))
            }
            showTradingInventory(player, overview, "Shop")
            return
        }
    } catch (e: NullPointerException) {return}
    val isGiven = player.inventory.itemInMainHand
    if(isGiven.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgCheated"), PersistentDataType.STRING)) return
    val multiplier = rlgPlayer.rankData().shopMultiplier
    val cmd = if(!isGiven.itemMeta.hasCustomModelData()) 0 else isGiven.itemMeta.customModelData
    val price: Long = try {
        (prices[isGiven.type]!![cmd]!!.toLong() * isGiven.amount * (if((isGiven.type == Material.STICK && cmd !in 1..5) || isGiven.type != Material.STICK) multiplier else 1.0)).toLong()
    } catch (e: NullPointerException) {
        player.sendMessage("§4Das Item steht nicht zum Verkauf!")
        return
    }
    val result = Bukkit.createInventory(null, 27, Component.text("Verkaufe für ${price.withPoints()} Credits"))
    result.setItem(11, CustomItems.defaultCustomItem(Material.RED_WOOL, "§4Ablehnen", arrayListOf()))
    result.setItem(13, CustomItems.defaultCustomItem(isGiven.type, "Verkaufen für ${price.withPoints()} Credits", arrayListOf(), cmd).asQuantity(isGiven.amount))
    result.setItem(15, CustomItems.defaultCustomItem(Material.GREEN_WOOL, "§2Annehmen", arrayListOf()))
    tradingInventoryCopies.add(result)
    player.openInventory(result)
}

fun showTradingInventory(player: Player, inventory: Inventory?, iname: String) {
    val cloned = Bukkit.createInventory(null, inventory!!.size, Component.text(iname))
    cloned.contents = inventory.contents.copyOf()
    shopinventories.add(cloned)
    player.closeInventory()
    player.openInventory(cloned)
}

fun clickHandler(item: ItemStack, player: Player) {
    val data = item.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgAction"), PersistentDataType.STRING) ?: return
    val dataArray = data.split(" ")
    when(dataArray[0]) {
        "book" -> {
            if(dataArray.size == 1) {
                showTradingInventory(player, TradingInventories.bookOverview, "Magic-Shop")
                return
            }
            when(dataArray[1]){
                "beginner" -> buyBook(player, 2)
                "magic" -> buyBook(player, 1)
                "shop" -> buyBook(player, 3)
                else -> showTradingInventory(player, TradingInventories.bookOverview, "Magic-Shop")
            }
        }
        "wood" -> {
            if(dataArray.size == 1) {
                showTradingInventory(player, TradingInventories.woodOverview, "Holz-Shop")
                return
            }
            val material = Material.valueOf(dataArray[1].uppercase())
            buyItem(material, dataArray[2].toInt(), 16, player)
        }
        "build" -> {
            if(dataArray.size == 1) {
                showTradingInventory(player, TradingInventories.buildOverview, "Bau-Shop")
                return
            }
            when(dataArray[1]){
                "stone" -> buyItem(Material.STONE, dataArray[2].toInt(), 16, player)
                else -> buyItem(Material.valueOf(dataArray[1].uppercase()), dataArray[2].toInt(), 32, player)
            }
        }
        "key" -> {
            if(dataArray.size == 1) {
                showTradingInventory(player, BlackMarketInventories.blackMarketKeyOverview, "Keys-Shop")
                return
            }
            when(dataArray[1]){
                "common" -> buyKey(1, player, 5000)
                "epic" -> buyKey(2, player, 20000)
                "supreme" -> buyKey(3, player, 50000)
                else -> showTradingInventory(player, BlackMarketInventories.blackMarketKeyOverview, "Keys-Shop")
            }
        }
        "crypto" -> {
            if(player.rlgPlayer().xpLevel < 15) return
            if(dataArray.size == 1){
                val cryptoOverview: Inventory = Bukkit.createInventory(null, 45, Component.text("Crypto Shop"))
                for (i in 0 until amountmap.size){
                    cryptoOverview.setItem(i + (9*0), CustomItems.defaultCustomItem(Material.STICK, "Kaufe ${amountmap[i]} Bitcoin für ${(amountmap[i]!! * prices[Material.STICK]!![1]!!).withPoints()} Credits", arrayListOf(), 1,
                        hashMapOf("rlgAction" to "crypto bitcoin ${amountmap[i]}")))
                    cryptoOverview.setItem(i + (9*1), CustomItems.defaultCustomItem(Material.STICK, "Kaufe ${amountmap[i]} Ethereum für ${(amountmap[i]!! * prices[Material.STICK]!![2]!!).withPoints()} Credits", arrayListOf(), 2,
                        hashMapOf("rlgAction" to "crypto ethereum ${amountmap[i]}")))
                    cryptoOverview.setItem(i + (9*2), CustomItems.defaultCustomItem(Material.STICK, "Kaufe ${amountmap[i]} Litecoin für ${(amountmap[i]!! * prices[Material.STICK]!![3]!!).withPoints()} Credits", arrayListOf(), 3,
                        hashMapOf("rlgAction" to "crypto litecoin ${amountmap[i]}")))
                    cryptoOverview.setItem(i + (9*3), CustomItems.defaultCustomItem(Material.STICK, "Kaufe ${amountmap[i]} Nano für ${(amountmap[i]!! * prices[Material.STICK]!![5]!!).withPoints()} Credits", arrayListOf(), 5,
                        hashMapOf("rlgAction" to "crypto nano ${amountmap[i]}")))
                    cryptoOverview.setItem(i + (9*4), CustomItems.defaultCustomItem(Material.STICK, "Kaufe ${amountmap[i]} Dogecoin für ${(amountmap[i]!! * prices[Material.STICK]!![4]!!).withPoints()} Credits", arrayListOf(), 4,
                        hashMapOf("rlgAction" to "crypto dogecoin ${amountmap[i]}")))
                }
                showTradingInventory(player, cryptoOverview, "Crypto-Shop")
                return
            }
            buyCrypto(dataArray[1], dataArray[2].toInt(), player)
        }
    }
}

fun buyBook(player: Player, type: Int) {
    if (isSpace(player.inventory)) {
        val price: Long = when (type) {
            2 -> 100
            else -> throw IllegalStateException("Unexpected value: $type")
        }
        if (pay(player, price, "Shop")) {
            player.inventory.addItem(when(type) {
                2 -> CustomItems.beginnerBook()
                else -> ItemStack(Material.STRUCTURE_VOID)
            })
        }
    } else {
        player.sendMessage("§4Dein Inventar ist voll!")
        player.closeInventory()
    }
}

fun buyItem(m: Material, amount: Int, priceperone: Int, player: Player) {
    if (isSpace(player.inventory)) {
        if (pay(
                player,
                amount.toLong() * priceperone,
                m.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial()
            )
        ) {
            val itemStack = ItemStack(m)
            itemStack.amount = amount
            player.inventory.addItem(itemStack)
        }
    } else {
        player.sendMessage("§4Dein Inventar ist voll!")
        player.closeInventory()
    }
}

fun buyCrypto(type: String, amount: Int, player: Player) {
    if (isSpace(player.inventory)) {
        try {
            if (pay(player, amount.toLong() * getCryptoPrice(type), type.toStartUppercaseMaterial())) {
                val itemStack = customItemsMap[type]!!
                itemStack.amount = amount
                player.inventory.addItem(itemStack)
                player.updateScoreboard()
            }
        } catch (ex: NullPointerException) {}
    } else {
        player.sendMessage("§4Dein Inventar ist voll!")
        player.closeInventory()
    }
}

fun buyKey(type: Int, player: Player, price: Long) {
    if (isSpace(player.inventory)) {
        if (pay(player, price, "Schwarzmarkt")) {
            player.inventory.addItem(genKey(type))
        }
    } else {
        player.sendMessage("§4Dein Inventar ist voll!")
        player.closeInventory()
    }
}

object BlackMarketInventories {
    var blackMarketOverview: Inventory? = null
    var blackMarketKeyOverview: Inventory? = null
}

object TradingInventories {
    var overview: Inventory? = null
    var bookOverview: Inventory? = null
    var woodOverview: Inventory? = null
    var buildOverview: Inventory? = null
}

object ShopInventories {
    var normalView: Inventory? = null
}

fun addCreditsToPlayer(uuid: String, amount: Long){
    modifyPlayerData(uuid){
        it.balance += amount
        it
    }
}

fun getCredits(uuid: String): Long{
    return getPlayerData(uuid).balance
}

@OptIn(DelicateCoroutinesApi::class)
internal fun updateCreditScore(){
    allJobs.add(GlobalScope.launch {
        while (true){
            lastUpdate = Date(System.currentTimeMillis())
            creditsScoreBoard = getCreditsScoreboard()
            val response = URL("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,dogecoin,nano,ethereum,litecoin&vs_currencies=usd").readText()
            val obj = jacksonObjectMapper().readValue<CoingeckoPriceInfo>(response)
            prices[Material.STICK]!![1] = (obj["bitcoin"]!!.usd * 100).roundToInt().toLong()
            prices[Material.STICK]!![2] = (obj["ethereum"]!!.usd * 100).roundToInt().toLong()
            prices[Material.STICK]!![3] = (obj["litecoin"]!!.usd * 100).roundToInt().toLong()
            prices[Material.STICK]!![5] = (obj["nano"]!!.usd * 100).roundToInt().toLong()
            prices[Material.STICK]!![4] = (obj["dogecoin"]!!.usd * 100).roundToInt().toLong()
            delay(300000)
        }
    })
}

fun getCreditsScoreboard(): String {
    val messageBuilder = StringBuilder()
    val time = SimpleDateFormat("HH:mm:ss").format(lastUpdate)
    messageBuilder.append("§6§l§nAktuelles Ranking:§r\n§7Letztes Update: $time\n")
    transaction {
        var count = 1
        //TODO: Offline
        /*PlayersTable.selectAll().orderBy(PlayersTable.balance, SortOrder.DESC).limit(5).forEach {
            messageBuilder.append("§7$count. §2${MojangAPI.getName(UUID.fromString(it[PlayersTable.uuid]))}: §6${it[PlayersTable.balance].withPoints()} Credits§r\n")
            count++
        }*/
    }
    messageBuilder.append("§6Diese Statistik wird alle 5 Minuten aktualisiert!")
    return messageBuilder.toString()
}

fun shopVillager(location: Location) {
    val shop: Villager = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager
    shop.setAI(false)
    shop.isInvulnerable = true
    shop.villagerLevel = 3
    shop.profession = Villager.Profession.LIBRARIAN
    shop.customName = "§6§l§nShop"
    shop.isCustomNameVisible = true
    shop.isSilent = true
    shop.removeWhenFarAway = false
    shop.persistentDataContainer.set(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING, "shop")
}

fun blackMarketVillager(location: Location) {
    val blackMarket: WanderingTrader = location.world.spawnEntity(location, EntityType.WANDERING_TRADER) as WanderingTrader
    blackMarket.setAI(false)
    blackMarket.isInvulnerable = true
    blackMarket.customName = "§0§lSchwarzmarkt"
    blackMarket.isCustomNameVisible = true
    blackMarket.isSilent = true
    blackMarket.removeWhenFarAway = false
    blackMarket.persistentDataContainer.set(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING, "blackmarket")
}