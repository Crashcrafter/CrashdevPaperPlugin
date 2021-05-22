package de.rlg.listener

import de.rlg.*
import de.rlg.items.staffs.*
import de.rlg.permission.chunks
import de.rlg.permission.eventCancel
import de.rlg.permission.isClaimed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Sign
import org.bukkit.entity.Fireball
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

class InteractListener : Listener {

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock
        val player = e.player
        if (keyChests.containsKey(block)) {
            if (player.inventory.itemInMainHand.type != Material.NAME_TAG) {
                e.isCancelled = true
                return
            }
        } else {
            if (e.hasBlock()) {
                if (player.inventory.itemInMainHand.type != Material.FIREWORK_ROCKET) {
                    val chunk = Objects.requireNonNull(e.clickedBlock)!!.chunk
                    if (chunk.isClaimed()) {
                        val uuid: String = chunks[chunk]!!.owner_uuid
                        if (uuid.length == 1 && !uuid.contentEquals("0")) {
                            if (e.clickedBlock!!.type == Material.CHEST) {
                                waveManager(chunk)
                                return
                            }
                        }
                    }
                    if (eventCancel(chunk, player)) {
                        e.isCancelled = true
                        return
                    }
                } else {
                    val chunk = Objects.requireNonNull(e.clickedBlock)!!.chunk
                    if (eventCancel(chunk, player)) {
                        e.isCancelled = true
                        return
                    }
                }
                if (e.action == Action.RIGHT_CLICK_BLOCK && block!!.state is Sign) {
                    val sign = block.state as Sign
                    if (signs.containsKey(sign.block)) {
                        signClickHandler(player, sign)
                        return
                    } else if (player.isSneaking) {
                        if (!eventCancel(block.chunk, player)) {
                            sign.isEditable = true
                            player.openSign((block.state as Sign))
                            return
                        }
                    }
                }
            }
        }
        try {
            if (player.inventory.itemInMainHand.type == Material.FISHING_ROD && e.clickedBlock!!.type == Material.NOTE_BLOCK) {
                addAFKCounter(player)
            }
        } catch (ignored: NullPointerException) { }
        val itemStack = player.inventory.itemInMainHand
        if (itemStack.hasItemMeta() && itemStack.itemMeta.hasCustomModelData()) {
            when(itemStack.type) {
                Material.WRITTEN_BOOK -> {
                    val bm = itemStack.itemMeta as BookMeta
                    bm.pages(when(bm.customModelData){
                        1 -> basicmagicbook.toMutableList().toComponentList()
                        2 -> beginnerbook.toMutableList().toComponentList()
                        3 -> shopbook.toMutableList().toComponentList()
                        else -> bm.pages()
                    })
                    itemStack.setItemMeta(bm)
                }
                Material.WOODEN_HOE -> {
                    when(itemStack.itemMeta.customModelData){
                        1 -> NatureStaff1.handleClick(e)
                        2 -> FireStaff1.handleClick(e)
                        3 -> WeatherStaff1.handleClick(e)
                        4 -> ChaosStaff1.handleClick(e)
                        5 -> WaterStaff1.handleClick(e)
                    }
                    e.isCancelled = true
                }
                Material.FIRE_CHARGE -> {
                    val data = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgItemData"), PersistentDataType.STRING) ?: return
                    if(data == "throwFireball") {
                        e.isCancelled = true
                        player.launchProjectile(Fireball::class.java, player.velocity)
                        player.inventory.itemInMainHand.amount--
                    }
                }
                else -> return
            }
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(e: PlayerInteractEntityEvent) {
        if (e.rightClicked is Villager) {
            val shop = e.rightClicked as Villager
            try {
                when(shop.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING)) {
                    "shop" -> {
                        tradingInventory(e.player)
                        e.isCancelled = true
                        return
                    }
                    "quester" -> {
                        showAvailableQuests(e.player)
                        e.isCancelled = true
                        return
                    }
                }
            } catch (ignored: NullPointerException) {}
        } else if (e.rightClicked is WanderingTrader) {
            val shop = e.rightClicked as WanderingTrader
            try {
                if (shop.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING) == "blackmarket") {
                    e.isCancelled = true
                    showTradingInventory(e.player, BlackMarketInventories.blackmarketoverview, "Schwarzmarkt")
                    return
                }
            } catch (ignored: NullPointerException) { }
        }
        if (eventCancel(e.player.chunk, e.player)) {
            e.isCancelled = true
        }
    }
}