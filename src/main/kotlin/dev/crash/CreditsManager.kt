package dev.crash

import dev.crash.items.CustomItems
import dev.crash.permission.rankData
import dev.crash.player.crashPlayer
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
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.floor

var prices = HashMap<Material, HashMap<Int, Long>>()
var amountMap = hashMapOf(0 to 1, 1 to 8, 2 to 16, 3 to 64)
var shopInvs: MutableList<Inventory> = ArrayList()
var creditsScoreBoard = ""

internal fun initTradingInventories() {
    val overview: Inventory = Bukkit.createInventory(null, 9, Component.text("Shop"))
    overview.setItem(0, CustomItems.defaultCustomItem(Material.OAK_LOG, "§cWood-Shop", arrayListOf(), 0, hashMapOf("crashAction" to "wood")))
    overview.setItem(1, CustomItems.defaultCustomItem(Material.POLISHED_ANDESITE, "§6Build-Shop", arrayListOf(), 0, hashMapOf("crashAction" to "build")))
    TradingInventories.overview = overview

    val woodMaterials: List<Material> = arrayListOf(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG)
    val woodOverview: Inventory = Bukkit.createInventory(null, 54, Component.text("Wood-Shop"))
    for (j in woodMaterials.indices) {
        for (i in 0 until amountMap.size) {
            woodOverview.setItem(i + 9 * j, CustomItems.defaultCustomItem(woodMaterials[j],
                "Buy " + amountMap[i] + " " + woodMaterials[j].toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " for " + (amountMap[i]!! * 16) + " Credits", arrayListOf(),0,
                hashMapOf("crashAction" to "wood ${woodMaterials[j].name.lowercase()} ${amountMap[i]}")))
        }
    }
    TradingInventories.woodOverview = woodOverview

    val buildOverview: Inventory = Bukkit.createInventory(null, 54, Component.text("Build-Shop"))
    for (i in 0 until amountMap.size) {
        buildOverview.setItem(i, CustomItems.defaultCustomItem(Material.STONE,
            "Buy " + amountMap[i] + " " + Material.STONE.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " for " + (amountMap[i]!! * 16) + " Credits", arrayListOf(), 0,
        hashMapOf("crashAction" to "build stone ${amountMap[i]}")))
    }
    val buildMaterials: List<Material> = ArrayList(
        listOf(
            Material.POLISHED_ANDESITE,
            Material.POLISHED_DIORITE,
            Material.POLISHED_GRANITE,
            Material.BRICKS,
            Material.DEEPSLATE_BRICKS
        )
    )
    for (j in buildMaterials.indices) {
        for (i in 0 until amountMap.size) {
            buildOverview.setItem(i + (9 * j) + 9, CustomItems.defaultCustomItem(buildMaterials[j],
                "Buy " + amountMap[i] + " " + buildMaterials[j].toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " for " + (amountMap[i]!! * 32) + " Credits", arrayListOf(), 0,
                hashMapOf("crashAction" to "build ${buildMaterials[j].name.lowercase()} ${amountMap[i]}")))
        }
    }
    TradingInventories.buildOverview = buildOverview

    val inventory: Inventory = Bukkit.createInventory(null, 27, Component.text("Normal Shop"))
    val itemStack = CustomItems.defaultCustomItem(Material.GRAY_STAINED_GLASS_PANE, " ", arrayListOf())
    for (i in 0 until inventory.size) {
        inventory.setItem(i, itemStack)
    }
    ShopInventories.normalView = inventory

    val blackMarketInv: Inventory = Bukkit.createInventory(null, 9, Component.text("Blackmarket"))
    blackMarketInv.setItem(0, CustomItems.defaultCustomItem(Material.NAME_TAG, "Keys-Shop", arrayListOf(), 1, hashMapOf("crashAction" to "key")))
    BlackMarketInventories.blackMarketOverview = blackMarketInv

    val blackMarketKeysInv: Inventory = Bukkit.createInventory(null, 9, Component.text("Blackmarket"))
    blackMarketKeysInv.setItem(0, CustomItems.defaultCustomItem(Material.NAME_TAG, "Buy 1 CommonKey for 5.000 Credits", arrayListOf(), 1, hashMapOf("crashAction" to "key common")))
    blackMarketKeysInv.setItem(1, CustomItems.defaultCustomItem(Material.NAME_TAG, "Buy 1 EpicKey for 20.000 Credits", arrayListOf(), 2, hashMapOf("crashAction" to "key epic")))
    blackMarketKeysInv.setItem(2, CustomItems.defaultCustomItem(Material.NAME_TAG, "Buy 1 SupremeKey for 50.000 Credits", arrayListOf(), 3, hashMapOf("crashAction" to "key supreme")))
    BlackMarketInventories.blackMarketKeyOverview = blackMarketKeysInv
}

fun transferBalance(player: Player, target: Player, amount: Long): Boolean {
    val playerCrashPlayer = player.crashPlayer()
    if (playerCrashPlayer.balance < amount) {
        player.sendMessage("§4You don't have enough Credits to start this transaction!")
        return false
    }
    playerCrashPlayer.balance -= amount
    val onePercent = amount/100
    val newAmount = amount - onePercent
    player.sendMessage("§2${newAmount.withPoints()} Credits have been sent to " + target.name)
    val targetCrashPlayer = target.crashPlayer()
    targetCrashPlayer.balance += newAmount
    target.sendMessage("§2You received " + newAmount.withPoints() + " Credits from " + player.name)
    player.updateScoreboard()
    target.updateScoreboard()
    return true
}

fun giveBalance(target: Player, amount: Long, reason: String) {
    val crashPlayer = target.crashPlayer()
    crashPlayer.balance += amount
    if (amount < 0) {
        target.sendMessage("§4You spent " + abs(amount).withPoints() + " Credits for " + reason)
    } else {
        target.sendMessage("§2You received " + abs(amount).withPoints() + " Credits for " + reason)
    }
    target.updateScoreboard()
}

fun pay(target: Player, amount: Long, reason: String): Boolean {
    val crashPlayer = target.crashPlayer()
    return if (crashPlayer.balance >= amount) {
        crashPlayer.balance -= amount
        target.sendMessage("§2You spent ${amount.withPoints()} Credits for $reason!")
        target.updateScoreboard()
        true
    } else {
        target.sendMessage("§4You don't have enough Credits!")
        false
    }
}

fun sellItem(player: Player) {
    val itemStack = player.inventory.itemInMainHand
    if (itemStack.hasItemMeta() && itemStack.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "cheated"), PersistentDataType.STRING)) {
        return
    }
    val crashPlayer = player.crashPlayer()
    val multiplier = crashPlayer.rankData().shopMultiplier
    val cmd = if(!itemStack.itemMeta.hasCustomModelData()) 0 else itemStack.itemMeta.customModelData
    val value = (prices[itemStack.type]!![cmd]!! * itemStack.amount * multiplier).toLong()
    giveBalance(player, value, "Shop")
    questCount(player, 12, value.toInt(), true)
    questCount(player, 8, value.toInt(), false)
    crashPlayer.changeXP(floor(value / 10.0).toLong())
    player.inventory.removeItem(itemStack)
    player.closeInventory()
    player.updateScoreboard()
}

fun tradingInventory(player: Player) {
    val crashPlayer = player.crashPlayer()
    try {
        if (player.inventory.itemInMainHand.type == Material.AIR) {
            val overview: Inventory = Bukkit.createInventory(null, 9, Component.text("Shop"))
            overview.contents = TradingInventories.overview!!.contents!!.copyOf()
            showTradingInventory(player, overview, "Shop")
            return
        }
    } catch (e: NullPointerException) {return}
    val isGiven = player.inventory.itemInMainHand
    if(isGiven.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "cheated"), PersistentDataType.STRING)) return
    val multiplier = crashPlayer.rankData().shopMultiplier
    val cmd = if(!isGiven.itemMeta.hasCustomModelData()) 0 else isGiven.itemMeta.customModelData
    val price: Long = try {
        (prices[isGiven.type]!![cmd]!!.toLong() * isGiven.amount * multiplier).toLong()
    } catch (e: NullPointerException) {
        player.sendMessage("§4This item can't be sold!")
        return
    }
    val result = Bukkit.createInventory(null, 27, Component.text("Sell for ${price.withPoints()} Credits"))
    result.setItem(11, CustomItems.defaultCustomItem(Material.RED_WOOL, "§4Decline", arrayListOf()))
    result.setItem(13, CustomItems.defaultCustomItem(isGiven.type, "Sell for ${price.withPoints()} Credits", arrayListOf(), cmd).asQuantity(isGiven.amount))
    result.setItem(15, CustomItems.defaultCustomItem(Material.GREEN_WOOL, "§2Accept", arrayListOf()))
    tradingInventoryCopies.add(result)
    player.openInventory(result)
}

fun showTradingInventory(player: Player, inventory: Inventory?, iName: String) {
    val cloned = Bukkit.createInventory(null, inventory!!.size, Component.text(iName))
    cloned.contents = inventory.contents!!.copyOf()
    shopInvs.add(cloned)
    player.closeInventory()
    player.openInventory(cloned)
}

fun clickHandler(item: ItemStack, player: Player) {
    val data = item.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "crashAction"), PersistentDataType.STRING) ?: return
    val dataArray = data.split(" ")
    when(dataArray[0]) {
        "wood" -> {
            if(dataArray.size == 1) {
                showTradingInventory(player, TradingInventories.woodOverview, "Wood-Shop")
                return
            }
            val material = Material.valueOf(dataArray[1].uppercase())
            buyItem(material, dataArray[2].toInt(), 16, player)
        }
        "build" -> {
            if(dataArray.size == 1) {
                showTradingInventory(player, TradingInventories.buildOverview, "Build-Shop")
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
    }
}

fun buyItem(m: Material, amount: Int, pricePerUnit: Int, player: Player) {
    if (isSpace(player.inventory)) {
        if (pay(
                player,
                amount.toLong() * pricePerUnit,
                m.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial()
            )
        ) {
            val itemStack = ItemStack(m)
            itemStack.amount = amount
            player.inventory.addItem(itemStack)
        }
    } else {
        player.sendMessage("§4Your Inventory is full!")
        player.closeInventory()
    }
}

fun buyKey(type: Int, player: Player, price: Long) {
    if (isSpace(player.inventory)) {
        if (pay(player, price, "Blackmarket")) {
            player.inventory.addItem(genKey(type))
        }
    } else {
        player.sendMessage("§4Your Inventory is full!")
        player.closeInventory()
    }
}

object BlackMarketInventories {
    var blackMarketOverview: Inventory? = null
    var blackMarketKeyOverview: Inventory? = null
}

object TradingInventories {
    var overview: Inventory? = null
    var woodOverview: Inventory? = null
    var buildOverview: Inventory? = null
}

object ShopInventories {
    var normalView: Inventory? = null
}

fun shopVillager(location: Location) {
    val shop: Villager = location.world.spawnEntity(location, EntityType.VILLAGER) as Villager
    shop.setAI(false)
    shop.isInvulnerable = true
    shop.villagerLevel = 3
    shop.profession = Villager.Profession.LIBRARIAN
    shop.customName(Component.text("§6§l§nShop"))
    shop.isCustomNameVisible = true
    shop.isSilent = true
    shop.removeWhenFarAway = false
    shop.persistentDataContainer.set(NamespacedKey(INSTANCE, "entityData"), PersistentDataType.STRING, "shop")
}

fun blackMarketVillager(location: Location) {
    val blackMarket: WanderingTrader = location.world.spawnEntity(location, EntityType.WANDERING_TRADER) as WanderingTrader
    blackMarket.setAI(false)
    blackMarket.isInvulnerable = true
    blackMarket.customName(Component.text("§0§lBlackmarket"))
    blackMarket.isCustomNameVisible = true
    blackMarket.isSilent = true
    blackMarket.removeWhenFarAway = false
    blackMarket.persistentDataContainer.set(NamespacedKey(INSTANCE, "entityData"), PersistentDataType.STRING, "blackmarket")
}