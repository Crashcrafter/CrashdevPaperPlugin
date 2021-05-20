package de.rlg.listener

import de.rlg.*
import de.rlg.permission.invSeeECs
import de.rlg.permission.invSeeInventories
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent

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
            playershopinventories.containsKey(inventory) -> {
                playershopinventories.remove(inventory)
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
        if (e.inventory.holder is Chest && keyChests.containsKey((e.inventory.holder as Chest?)!!.block) && !lotteryI.contains(e.inventory)) {
            val player = e.player as Player
            val playerInventory = player.inventory
            val itemStack = playerInventory.itemInMainHand
            try {
                val token = itemStack.itemMeta.lore!![0].split(" ").toTypedArray()[1]
                val type: Int = keyChests[(e.inventory.holder as Chest).block]!!
                if (itemStack.type == Material.NAME_TAG && token.isNotEmpty()) {
                    if (getKeyType(token) == type) {
                        createLottery(player, e.inventory, type)
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
        } else if (playershopinventories.containsKey(e.inventory)) {
            e.isCancelled = true
            try {
                shopInvClickHandler(player, e.clickedInventory!!.getItem(e.slot)!!, playershopinventories[e.clickedInventory]!!)
            }catch (ex: NullPointerException) {}
        } else if (questinventories.contains(e.clickedInventory)) {
            e.isCancelled = true
            questClickHandler(e.whoClicked as Player, e.clickedInventory!!, e.slot)
        }
    }
}