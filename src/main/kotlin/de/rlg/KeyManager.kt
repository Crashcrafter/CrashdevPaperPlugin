package de.rlg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.items.CustomItems
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import kotlin.collections.HashMap

private const val allowedSymbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

val lotteryI: MutableList<Inventory> = ArrayList()

fun getPlane(possibility: Int): ItemStack {
    return when (possibility) {
        5 -> CustomItems.defaultCustomItem(Material.GRAY_STAINED_GLASS_PANE, "Common", arrayListOf())
        4 -> CustomItems.defaultCustomItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "Uncommon", arrayListOf())
        3 -> CustomItems.defaultCustomItem(Material.BLUE_STAINED_GLASS_PANE, "Rare", arrayListOf())
        2 -> CustomItems.defaultCustomItem(Material.PURPLE_STAINED_GLASS_PANE, "Epic", arrayListOf())
        1 -> CustomItems.defaultCustomItem(Material.YELLOW_STAINED_GLASS_PANE, "Supreme", arrayListOf())
        else -> ItemStack(Material.STRUCTURE_VOID)
    }
}

fun genKey(type: Int): ItemStack {
    val itemStack = ItemStack(Material.NAME_TAG)
    val im = itemStack.itemMeta
    val lore: MutableList<Component> = ArrayList()
    var token: String
    do {
        token = getToken()
    } while (tokenExists(token))
    lore.add(Component.text("Token: $token"))
    im.lore(lore)
    im.displayName(Component.text(keysData[type]!!.displayName))
    im.setCustomModelData(type)
    im.persistentDataContainer.set(NamespacedKey(INSTANCE, "rlgKeyToken"), PersistentDataType.STRING, token)
    itemStack.itemMeta = im
    insertKey(token, type)
    return itemStack
}

fun tokenExists(token: String): Boolean{
    var result = true
    transaction {
        result = !KeyIndexTable.select(where = {KeyIndexTable.token eq token}).empty()
    }
    return result
}

private fun insertKey(token: String, type: Int){
    transaction {
        KeyIndexTable.insert {
            it[KeyIndexTable.token] = token
            it[KeyIndexTable.type] = type
        }
    }
}

fun redeemKey(playerInventory: Inventory, itemStack: ItemStack, token: String): Boolean {
    if (redeemKey(token)) {
        playerInventory.remove(itemStack)
        return true
    }
    return false
}

fun redeemKey(token: String): Boolean {
    var result = false
    transaction { 
        if(!KeyIndexTable.select(where = {KeyIndexTable.token eq token}).empty()){
            result = true
        }
        KeyIndexTable.deleteWhere {
            KeyIndexTable.token eq token
        }
    }
    return result
}

fun getKeyType(token: String): Int {
    var type = 0
    transaction {
        type = KeyIndexTable.select(where = {KeyIndexTable.token eq token}).first()[KeyIndexTable.type]
    }
    return type
}

private fun getToken(): String {
    val symbols = allowedSymbols.toCharArray()
    val random = Random()
    val token = StringBuilder()
    val length = symbols.size
    for (i in 0..2) {
        for (j in 0..3) {
            token.append(symbols[random.nextInt(length)])
        }
        if (i <= 1) {
            token.append("-")
        }
    }
    return token.toString()
}

val keysData = hashMapOf<Int, Key>()
val lootTables = hashMapOf<Int, List<LootTableItem>>()

data class Key(val id: Int, val name: String, val displayName: String, val crateName: String, val lootTable: List<LootTableItem>){
    companion object {
        fun byName(name: String): Key? {
            val list = keysData.values.filter { it.name == name }
            if(list.isEmpty()) return null
            return list.first()
        }

        fun getNames(): List<String> {
            val result = mutableListOf<String>()
            keysData.forEach {
                result.add(it.value.name)
            }
            return result
        }
    }
}
data class LootTableItem(val itemString: String, val probability: Int, val amount: Int = 1, val enchantments: HashMap<String, Int>? = null)

fun loadLootTables(){
    val file = File(INSTANCE.dataFolder.path + "/keys.json")
    if(file.exists()){
        val keys = jacksonObjectMapper().readValue<List<Key>>(file)
        keys.forEach { key ->
            keysData[key.id] = key
            val lootTable = mutableListOf<LootTableItem>()
            key.lootTable.forEach {
                for(i in 0..it.probability){
                    lootTable.add(it)
                }
            }
            lootTables[key.id] = lootTable
        }
    }else {
        file.createNewFile()
        jacksonObjectMapper().writeValue(file, listOf<Key>())
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun createNewLottery(player: Player, inventory: Inventory, type: Int) {
    allJobs.add(GlobalScope.launch {
        lotteryI.add(inventory)
        val resultSet = HashMap<Int, ItemStack>()
        val planes = HashMap<Int, ItemStack>()
        val items = lootTables[type]!!
        val size = items.size
        for (i in 9..59) {
            val random = Random()
            val chosen = random.nextInt(size)
            val item = items[chosen]
            val itemStack = item.toItemstack()
            resultSet[i] = itemStack
            planes[i] = getPlane(item.probability)
        }
        var reward = resultSet[54]!!
        if (reward.type == Material.NAME_TAG && reward.itemMeta.hasCustomModelData()) {
            reward = genKey(reward.itemMeta.customModelData)
        }
        if (Bukkit.getOnlinePlayers().size < 20) {
            for (h in 0 until resultSet.size - 9) {
                for (i in 9..17) {
                    inventory.setItem(i, resultSet[i + h])
                    inventory.setItem(i - 9, planes[i + h])
                    inventory.setItem(i + 9, planes[i + h])
                }
                delay(if(h<30)100 else 300)
            }
            delay(3000)
        } else {
            player.world.spawnEntity(player.location, EntityType.FIREWORK)
        }
        if (!reward.itemMeta.hasDisplayName()) {
            player.sendMessage(
                "§2Herzlichen Glückwunsch! §6Du hast ${reward.amount} " + reward.type.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " erhalten!"
            )
            println(
                player.name + " hat " + reward.amount + " " + reward.type.toString().lowercase(Locale.ROOT).toStartUppercaseMaterial() + " erhalten!"
            )
        } else {
            player.sendMessage(
                "§2Herzlichen Glückwunsch! §6Du hast ${reward.amount} ${(reward.itemMeta.displayName() as TextComponent).content()}§r§6 erhalten!"
            )
            println(player.name + " hat " + reward.amount + " " + (reward.itemMeta.displayName() as TextComponent).content() + "§r erhalten!")
        }
        player.inventory.addItem(reward)
        inventory.clear()
        lotteryI.remove(inventory)
        questCount(player, 6, 1, true)
        Bukkit.getScheduler().runTask(INSTANCE, Runnable {
            val viewers = mutableListOf<HumanEntity>()
            inventory.viewers.forEach {
                viewers.add(it)
            }
            viewers.forEach {
                it.closeInventory()
            }
        })
    })
}

fun itemStringToItem(input: String): ItemStack{
    return when{
        input.startsWith("mc:") -> {
            val item = input.removePrefix("mc:").uppercase()
            ItemStack(Material.valueOf(item))
        }
        input.startsWith("ci:") -> {
            val item = input.removePrefix("ci:")
            customItemsMap[item]!!
        }
        input.startsWith("key:") -> {
            val keyId = input.removePrefix("key:").toInt()
            CustomItems.defaultCustomItem(Material.NAME_TAG, keysData[keyId]!!.displayName, mutableListOf(), keyId)
        }
        else -> {
            println("Could not find $input, good job DasIschBims#1248")
            CustomItems.defaultCustomItem(Material.DIRT, "Item not found: $input", mutableListOf())
        }
    }
}