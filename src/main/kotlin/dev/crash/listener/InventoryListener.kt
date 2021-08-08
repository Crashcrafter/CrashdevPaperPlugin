package dev.crash.listener

import dev.crash.*
import dev.crash.permission.invSeeECs
import dev.crash.permission.invSeeInventories
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.persistence.PersistentDataType

class InventoryListener : Listener {

    @EventHandler
    fun onInventoryClose(e: InventoryCloseEvent) {
        val inventory = e.inventory
        when{
            invSeeInventories.containsKey(inventory) -> invSeeInventories[inventory]!!.updateInventory()
            invSeeECs.containsKey(inventory) -> invSeeECs[inventory]!!.updateInventory()
            tradingInventoryCopies.contains(inventory) -> {
                tradingInventoryCopies.remove(inventory)
                inventory.clear()
            }
            shopinventories.contains(inventory) -> {
                shopinventories.remove(inventory)
                inventory.clear()
            }
            questinventories.contains(inventory) -> {
                questinventories.remove(inventory)
                inventory.clear()
            }
        }
    }

    @EventHandler
    fun onInventoryOpen(e: InventoryOpenEvent) {
        if (e.inventory.holder is ShulkerBox && keyChests.containsKey((e.inventory.holder as ShulkerBox?)!!.block) && !lotteryI.contains(e.inventory)) {
            val player = e.player as Player
            val playerInventory = player.inventory
            val itemStack = playerInventory.itemInMainHand
            try {
                if (itemStack.type == Material.NAME_TAG) {
                    val token = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "crashKeyToken"), PersistentDataType.STRING)!!
                    val type: Int = keyChests[(e.inventory.holder as ShulkerBox).block]!!
                    if (getKeyType(token) == type) {
                        createNewLottery(player, e.inventory, type)
                        redeemKey(playerInventory, itemStack, token)
                    } else {
                        e.isCancelled = true
                    }
                } else {
                    e.isCancelled = true
                }
            } catch (exception: NullPointerException) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        if (lotteryI.contains(e.inventory)) {
            e.isCancelled = true
        } else if (tradingInventoryCopies.contains(e.inventory)) {
            e.isCancelled = true
            if (e.slot == 11) {
                player.closeInventory()
            } else if (e.slot == 15) {
                sellItem(player)
            }
        } else if (shopinventories.contains(e.inventory)) {
            e.isCancelled = true
            try {
                clickHandler(e.clickedInventory!!.getItem(e.slot)!!, player)
            } catch (ignored: NullPointerException) { }
            catch (ignored: ArrayIndexOutOfBoundsException) { }
        } else if (questinventories.contains(e.clickedInventory)) {
            e.isCancelled = true
            questClickHandler(e.whoClicked as Player, e.clickedInventory!!, e.slot)
        }
    }
}