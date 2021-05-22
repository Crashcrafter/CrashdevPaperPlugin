package de.rlg

import de.rlg.items.CustomItems
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private const val allowedSymbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

var lotteryI: MutableCollection<Inventory> = ArrayList()

fun createLottery(player: Player, inventory: Inventory, type: Int) {
    allJobs.add(GlobalScope.launch {
        lotteryI.add(inventory)
        val resultSet = HashMap<Int, ItemStack>()
        val planes = HashMap<Int, ItemStack>()
        var items: List<Pair<ItemStack, ItemStack>> = listOf()
        when (type) {
            1 -> items = LOOTTABLES.Common.newloottable
            2 -> items = LOOTTABLES.Epic.newloottable
            3 -> items = LOOTTABLES.Supreme.newloottable
            4 -> items = LOOTTABLES.Vote.newloottable
            5 -> items = LOOTTABLES.Level.newloottable
        }
        val size = items.size
        for (i in 9..59) {
            val random = Random()
            val chosen = random.nextInt(size)
            resultSet[i] = items[chosen].first
            planes[i] = items[chosen].second
        }
        var reward = resultSet[54]!!
        if (reward.type == Material.NAME_TAG && reward.itemMeta.hasCustomModelData()) {
            reward = genKey(reward.itemMeta.customModelData)
        }
        if (Bukkit.getOnlinePlayers().size < 12) {
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
            inventory.viewers.forEach {
                it.closeInventory()
            }
        })
    })
}

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
    im.displayName(Component.text(
        when(type){
            1 -> "§7§l§oCommon Key"
            2 -> "§5§l§oEpic Key"
            3 -> "§e§l§oSupreme Key"
            4 -> "§4§l§oVote Key"
            5 -> "§b§l§oLevel Key"
            else -> ""
        }
    ))
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

class LOOTTABLES {
    object Common {
        var newloottable: MutableList<Pair<ItemStack, ItemStack>> = ArrayList()
        fun setupCommon() {
            newloottable.add(Pair(CustomItems.defaultCustomItem(Material.NAME_TAG, "§5§l§oEpic Key", arrayListOf(), 2), getPlane(1)))
            val is0 = ItemStack(Material.DIAMOND_PICKAXE)
            is0.addEnchantment(Enchantment.DIG_SPEED, 5)
            is0.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3)
            is0.addEnchantment(Enchantment.DURABILITY, 3)
            newloottable.add(Pair(is0, getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.GOLDEN_APPLE), getPlane(1)))
            val is1 = ItemStack(Material.IRON_CHESTPLATE)
            is1.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
            newloottable.add(Pair(is1, getPlane(1)))
            val is2 = ItemStack(Material.IRON_LEGGINGS)
            is2.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
            newloottable.add(Pair(is2, getPlane(1)))
            val is3 = ItemStack(Material.IRON_BOOTS)
            is3.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
            newloottable.add(Pair(is3, getPlane(1)))
            val is4 = ItemStack(Material.IRON_HELMET)
            is4.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3)
            newloottable.add(Pair(is4, getPlane(1)))
            newloottable.add(Pair(CustomItems.ironKatana(), getPlane(1)))
            Epic.newloottable.add(Pair(CustomItems.nano(), getPlane(1)))
            for (i in 0..1) {
                newloottable.add(Pair(ItemStack(Material.EXPERIENCE_BOTTLE, 15), getPlane(2)))
                val is5 = ItemStack(Material.BOW)
                is5.addEnchantment(Enchantment.ARROW_DAMAGE, 1)
                newloottable.add(Pair(is5, getPlane(2)))
                val is6 = ItemStack(Material.FISHING_ROD)
                is6.addEnchantment(Enchantment.LURE, 2)
                newloottable.add(Pair(is6, getPlane(2)))
                val is7 = ItemStack(Material.STONE_AXE)
                is7.addEnchantment(Enchantment.DIG_SPEED, 4)
                newloottable.add(Pair(is7, getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.HEART_OF_THE_SEA), getPlane(2)))
                newloottable.add(Pair(CustomItems.throwableFireBall(), getPlane(2)))
            }
            for (i in 0..2) {
                newloottable.add(Pair(ItemStack(Material.SLIME_BALL, 7), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.EMERALD, 5), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.DIAMOND, 5), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.PURPLE_SHULKER_BOX), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.ENCHANTING_TABLE), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.ENDER_PEARL, 5), getPlane(3)))
            }
            for (i in 0..3) {
                newloottable.add(Pair(ItemStack(Material.CROSSBOW), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.LAPIS_LAZULI, 15), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.GLOWSTONE_DUST, 16), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.TURTLE_HELMET), getPlane(4)))
            }
            for (i in 0..4) {
                newloottable.add(Pair(ItemStack(Material.REDSTONE, 32), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.OBSIDIAN, 10), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.IRON_INGOT, 32), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.WHITE_CONCRETE, 64), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.WHITE_TERRACOTTA, 64), getPlane(5)))
                newloottable.add(Pair(CustomItems.mudBall().asQuantity(8), getPlane(5)))
            }
        }
    }

    object Epic {
        var newloottable: MutableList<Pair<ItemStack, ItemStack>> = ArrayList()
        fun setupEpic() {
            newloottable.add(Pair(CustomItems.defaultCustomItem(Material.NAME_TAG, "§e§l§oSupreme Key", arrayListOf(), 3), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.SPAWNER), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.VILLAGER_SPAWN_EGG), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.END_PORTAL_FRAME), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.BEACON), getPlane(1)))
            newloottable.add(Pair(CustomItems.diaKatana(), getPlane(1)))
            newloottable.add(Pair(CustomItems.nano().asQuantity(8), getPlane(1)))
            for (i in 0..1) {
                newloottable.add(Pair(ItemStack(Material.CHICKEN_SPAWN_EGG), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.COW_SPAWN_EGG), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.PIG_SPAWN_EGG), getPlane(2)))
                val is0 = ItemStack(Material.DIAMOND_PICKAXE)
                is0.addEnchantment(Enchantment.DIG_SPEED, 5)
                is0.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3)
                is0.addEnchantment(Enchantment.DURABILITY, 3)
                newloottable.add(Pair(is0, getPlane(2)))
                val itemStack = ItemStack(Material.ENCHANTED_BOOK)
                val im = itemStack.itemMeta
                im.addEnchant(Enchantment.MENDING, 1, true)
                itemStack.itemMeta = im
                newloottable.add(Pair(itemStack, getPlane(2)))
                val is1 = ItemStack(Material.DIAMOND_SWORD)
                is1.addEnchantment(Enchantment.DAMAGE_ALL, 4)
                is1.addEnchantment(Enchantment.SWEEPING_EDGE, 3)
                is1.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3)
                newloottable.add(Pair(is1, getPlane(2)))
                val is2 = ItemStack(Material.DIAMOND_CHESTPLATE)
                is2.addEnchantment(Enchantment.DURABILITY, 1)
                is2.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5)
                newloottable.add(Pair(is2, getPlane(2)))
                val is3 = ItemStack(Material.DIAMOND_LEGGINGS)
                is3.addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 5)
                newloottable.add(Pair(is3, getPlane(2)))
                val is4 = ItemStack(Material.DIAMOND_BOOTS)
                is4.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2)
                is4.addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 5)
                newloottable.add(Pair(is4, getPlane(2)))
                val is5 = ItemStack(Material.DIAMOND_HELMET)
                is5.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 5)
                newloottable.add(Pair(is5, getPlane(2)))
                newloottable.add(Pair(CustomItems.throwableFireBall(), getPlane(2)))
            }
            for (i in 0..2) {
                newloottable.add(Pair(ItemStack(Material.ENCHANTED_GOLDEN_APPLE), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.EXPERIENCE_BOTTLE, 32), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.ENCHANTED_GOLDEN_APPLE), getPlane(3)))
                val itemStack = ItemStack(Material.IRON_PICKAXE)
                itemStack.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2)
                itemStack.addEnchantment(Enchantment.DURABILITY, 2)
                newloottable.add(Pair(itemStack, getPlane(3)))
            }
            for (i in 0..3) {
                newloottable.add(Pair(ItemStack(Material.GOLDEN_APPLE), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.DIAMOND, 32), getPlane(4)))
                val itemStack = ItemStack(Material.IRON_LEGGINGS)
                itemStack.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                newloottable.add(Pair(itemStack, getPlane(4)))
                val is1 = ItemStack(Material.IRON_CHESTPLATE)
                is1.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                newloottable.add(Pair(is1, getPlane(4)))
                val is2 = ItemStack(Material.IRON_BOOTS)
                is2.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                newloottable.add(Pair(is2, getPlane(4)))
                val is3 = ItemStack(Material.IRON_HELMET)
                is3.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                newloottable.add(Pair(is3, getPlane(4)))
                newloottable.add(Pair(CustomItems.dogecoin().asQuantity(32), getPlane(4)))
            }
            for (i in 0..4) {
                newloottable.add(Pair(ItemStack(Material.SEA_LANTERN, 64), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.IRON_INGOT, 64), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.GOLDEN_CARROT, 64), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.SLIME_BALL, 32), getPlane(5)))
                newloottable.add(Pair(CustomItems.throwableFireBall().asQuantity(3), getPlane(5)))
                newloottable.add(Pair(CustomItems.nano().asQuantity(1), getPlane(5)))
            }
        }
    }

    object Supreme {
        var newloottable: MutableList<Pair<ItemStack, ItemStack>> = ArrayList()
        fun setupSupreme() {
            run {
                val itemStack = ItemStack(Material.ELYTRA)
                itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 4)
                newloottable.add(Pair(itemStack, getPlane(1)))
                val is1 = ItemStack(Material.TRIDENT)
                is1.addEnchantment(Enchantment.LOYALTY, 3)
                is1.addUnsafeEnchantment(Enchantment.DURABILITY, 4)
                is1.addEnchantment(Enchantment.IMPALING, 5)
                is1.addEnchantment(Enchantment.CHANNELING, 1)
                newloottable.add(Pair(is1, getPlane(1)))
                val is2 = ItemStack(Material.DIAMOND_PICKAXE)
                is2.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3)
                is2.addEnchantment(Enchantment.DIG_SPEED, 5)
                is2.addUnsafeEnchantment(Enchantment.DURABILITY, 4)
                is2.addEnchantment(Enchantment.MENDING, 1)
                newloottable.add(Pair(is2, getPlane(1)))
                val is3 = ItemStack(Material.NETHERITE_BOOTS)
                is3.addEnchantment(Enchantment.DURABILITY, 3)
                is3.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                is3.addEnchantment(Enchantment.DEPTH_STRIDER, 3)
                newloottable.add(Pair(is3, getPlane(1)))
                val is4 = ItemStack(Material.NETHERITE_HELMET)
                is4.addEnchantment(Enchantment.DURABILITY, 3)
                is4.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                is4.addEnchantment(Enchantment.WATER_WORKER, 1)
                newloottable.add(Pair(is4, getPlane(1)))
                val is5 = ItemStack(Material.NETHERITE_LEGGINGS)
                is5.addEnchantment(Enchantment.DURABILITY, 3)
                is5.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                newloottable.add(Pair(is5, getPlane(1)))
                val is6 = ItemStack(Material.NETHERITE_CHESTPLATE)
                is6.addEnchantment(Enchantment.DURABILITY, 3)
                is6.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
                newloottable.add(Pair(is6, getPlane(1)))
                newloottable.add(Pair(CustomItems.netherKatana(), getPlane(1)))
                newloottable.add(Pair(CustomItems.litecoin(), getPlane(1)))
            }
            for (i in 0..1) {
                val itemStack = ItemStack(Material.DIAMOND_SWORD)
                itemStack.addEnchantment(Enchantment.DAMAGE_ALL, 4)
                itemStack.addEnchantment(Enchantment.SWEEPING_EDGE, 3)
                itemStack.addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3)
                newloottable.add(Pair(itemStack, getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 3), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.VILLAGER_SPAWN_EGG), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.SPAWNER), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.END_PORTAL_FRAME, 3), getPlane(2)))
            }
            for (i in 0..2) {
                val is0 = ItemStack(Material.TRIDENT)
                is0.addEnchantment(Enchantment.RIPTIDE, 3)
                is0.addEnchantment(Enchantment.DURABILITY, 3)
                newloottable.add(Pair(is0, getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.PIG_SPAWN_EGG), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.COW_SPAWN_EGG), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.CHICKEN_SPAWN_EGG), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.END_CRYSTAL, 2), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.BEACON, 3), getPlane(3)))
                newloottable.add(Pair(ItemStack(Material.EXPERIENCE_BOTTLE, 64), getPlane(3)))
            }
            for (i in 0..3) {
                newloottable.add(Pair(ItemStack(Material.NETHER_STAR), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.GUNPOWDER, 64), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.FIREWORK_ROCKET, 32), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.EMERALD, 32), getPlane(4)))
                newloottable.add(Pair(CustomItems.nano().asQuantity(3), getPlane(4)))
            }
            for (i in 0..4) {
                val is0 = ItemStack(Material.ENCHANTED_BOOK)
                val im0 = is0.itemMeta
                im0.addEnchant(Enchantment.MENDING, 1, true)
                is0.itemMeta = im0
                newloottable.add(Pair(is0, getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.DIAMOND, 20), getPlane(5)))
                newloottable.add(Pair(CustomItems.throwableFireBall().asQuantity(4), getPlane(5)))
                newloottable.add(Pair(CustomItems.mudBall().asQuantity(16), getPlane(5)))
                newloottable.add(Pair(CustomItems.dogecoin().asQuantity(64), getPlane(5)))
            }
        }
    }

    object Vote {
        var newloottable: MutableList<Pair<ItemStack, ItemStack>> = ArrayList()
        fun setupVote() {
            newloottable.add(Pair(CustomItems.defaultCustomItem(Material.NAME_TAG, "§7§l§oCommon Key", arrayListOf(), 1), getPlane(1)))
            newloottable.add(Pair(CustomItems.ironKatana(), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.DIAMOND, 4), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.IRON_INGOT, 15), getPlane(1)))
            newloottable.add(Pair(ItemStack(Material.GUNPOWDER, 30), getPlane(1)))
            for (i in 0..1) {
                newloottable.add(Pair(ItemStack(Material.GOLD_INGOT, 10), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.GUNPOWDER, 15), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.ENDER_PEARL, 3), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.EMERALD, 12), getPlane(2)))
                newloottable.add(Pair(CustomItems.nano().asQuantity(2), getPlane(5)))
            }
            for (i in 0..2) {
                newloottable.add(Pair(ItemStack(Material.DIAMOND_SWORD), getPlane(3)))
                newloottable.add(Pair(CustomItems.throwableFireBall(), getPlane(3)))
                newloottable.add(Pair(CustomItems.throwableFireBall().asQuantity(2), getPlane(3)))
                newloottable.add(Pair(CustomItems.mudBall().asQuantity(12), getPlane(3)))
            }
            for (i in 0..3) {
                newloottable.add(Pair(ItemStack(Material.GUNPOWDER, 9), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.FIREWORK_ROCKET, 7), getPlane(4)))
                newloottable.add(Pair(ItemStack(Material.COAL, 15), getPlane(4)))
                newloottable.add(Pair(CustomItems.dogecoin().asQuantity(15), getPlane(4)))
            }
            for (i in 0..4) {
                newloottable.add(Pair(ItemStack(Material.MAGMA_CREAM, 16), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.GOLD_INGOT, 5), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.IRON_ORE, 6), getPlane(5)))
                newloottable.add(Pair(CustomItems.mudBall().asQuantity(6), getPlane(5)))
                newloottable.add(Pair(CustomItems.nano().asQuantity(1), getPlane(5)))
                newloottable.add(Pair(CustomItems.dogecoin().asQuantity(5), getPlane(5)))
            }
        }
    }

    object Level {
        var newloottable: MutableList<Pair<ItemStack, ItemStack>> = ArrayList()
        fun setupLevel() {
            newloottable.add(Pair(CustomItems.defaultCustomItem(Material.NAME_TAG, "§7§l§oCommon Key", arrayListOf(), 1), getPlane(1)))
            newloottable.add(Pair(CustomItems.diaKatana(), getPlane(1)))
            newloottable.add(Pair(CustomItems.manaShard().asQuantity(2), getPlane(1)))
            newloottable.add(Pair(CustomItems.litecoin(), getPlane(1)))
            for(i in 0..1) {
                newloottable.add(Pair(CustomItems.throwableFireBall().asQuantity(4), getPlane(2)))
                newloottable.add(Pair(CustomItems.manaShard(), getPlane(2)))
                newloottable.add(Pair(ItemStack(Material.END_CRYSTAL), getPlane(2)))
            }
            for(i in 0..2) {
                newloottable.add(Pair(CustomItems.nano().asQuantity(3), getPlane(3)))
                newloottable.add(Pair(CustomItems.ironKatana(), getPlane(3)))
            }
            for(i in 0..3) {
                newloottable.add(Pair(CustomItems.dogecoin().asQuantity(12), getPlane(4)))
                newloottable.add(Pair(CustomItems.nano(), getPlane(4)))
            }
            for (i in 0..4) {
                newloottable.add(Pair(CustomItems.mudBall().asQuantity(16), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.DIAMOND, 4), getPlane(5)))
                newloottable.add(Pair(ItemStack(Material.IRON_INGOT, 16), getPlane(5)))
            }
        }
    }
}

fun initLootTables() {
    LOOTTABLES.Common.setupCommon()
    LOOTTABLES.Epic.setupEpic()
    LOOTTABLES.Supreme.setupSupreme()
    LOOTTABLES.Vote.setupVote()
    LOOTTABLES.Level.setupLevel()
}