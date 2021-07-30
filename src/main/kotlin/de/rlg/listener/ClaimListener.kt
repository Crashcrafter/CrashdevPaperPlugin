package de.rlg.listener

import de.rlg.*
import de.rlg.commands.admin.removeKeyChest
import de.rlg.commands.admin.removePortal
import de.rlg.permission.eventCancel
import de.rlg.permission.heventCancel
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

class ClaimListener : Listener {
    @EventHandler
    fun onBlockExplode(e: BlockExplodeEvent){
        e.blockList().forEach {
            if(eventCancel(it.chunk)){
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent){
        e.blockList().forEach {
            if (eventCancel(it.chunk)) {
                if (e.entity.type == EntityType.CREEPER) {
                    val creeper = e.entity as Creeper
                    creeper.clearLootTable()
                    creeper.explosionRadius = 0
                    creeper.damage(1000.0)
                }
                e.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun onHit(e: EntityDamageByEntityEvent) {
        if (e.damager is Player && heventCancel(e.damager.location.chunk, e.damager as Player)) {
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onBucketEmpty(e: PlayerBucketEmptyEvent) {
        if (eventCancel(e.blockClicked.chunk, e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onBucketFill(e: PlayerBucketFillEvent) {
        if (eventCancel(e.blockClicked.chunk, e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val block = e.block
        when {
            eventCancel(block.chunk, e.player) -> e.isCancelled = true
            portals.containsKey(block) -> removePortal(block)
            keyChests.containsKey(block) -> removeKeyChest(block)
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (eventCancel(e.blockPlaced.chunk, e.player) || eventCancel(e.block.chunk, e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onProjectile(e: ProjectileLaunchEvent) {
        if (e.entity.shooter is Player) {
            val player = e.entity.shooter as Player
            if(e.entity is Snowball) {
                val data = player.inventory.itemInMainHand.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgItemData"), PersistentDataType.STRING)
                if(data == "mudBall") {
                    e.entity.persistentDataContainer.set(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING, "mudBall")
                    (e.entity as Snowball).item = customItemsMap["mud_ball"]!!
                }
            }
            if (player.inventory.itemInMainHand.type != Material.FIREWORK_ROCKET) {
                if (eventCancel(e.entity.chunk, player)) {
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onBlockFromTo(e: BlockFromToEvent) {
        if (e.block.type == Material.LAVA || e.block.type == Material.WATER) {
            val location = e.toBlock.location
            object : BukkitRunnable() {
                override fun run() {
                    if (location.block.type == Material.COBBLESTONE || location.block.type == Material.STONE) location.block.type =
                        Material.STRUCTURE_VOID
                }
            }.runTaskLater(INSTANCE, 2)
        }
    }

    @EventHandler
    fun onArmorStand(e: PlayerArmorStandManipulateEvent) {
        if (eventCancel(e.player.chunk, e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onVehicleMove(e: VehicleEntityCollisionEvent) {
        if(e.entity is Player && eventCancel(e.vehicle.chunk, e.entity as Player)){
            e.isCollisionCancelled = true
            e.isCancelled = true
        }
    }
}