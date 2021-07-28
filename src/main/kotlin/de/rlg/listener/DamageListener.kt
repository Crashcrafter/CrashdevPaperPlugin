package de.rlg.listener

import de.rlg.permission.deventCancel
import de.rlg.permission.eventCancel
import de.rlg.player.rlgPlayer
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.vehicle.VehicleDamageEvent

class DamageListener : Listener {

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.entityType == EntityType.PLAYER) {
            val player = e.entity as Player
            val rlgPlayer = player.rlgPlayer()
            if (e is EntityDamageByEntityEvent && e.damager is Player) {
                rlgPlayer.lastDamage = System.currentTimeMillis() + (1000*10)
            }
            val chunk = player.location.chunk
            if (e.cause != EntityDamageEvent.DamageCause.VOID && deventCancel(chunk, player)) {
                e.isCancelled = true
                return
            }
            val cause = e.cause
            var itemStack = player.inventory.itemInMainHand
            if (rlgPlayer.mana >= 1) {
                if (cause == EntityDamageEvent.DamageCause.POISON) {
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 1) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                    itemStack = player.inventory.itemInOffHand
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 1) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                } else if (cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 2) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                    itemStack = player.inventory.itemInOffHand
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 2) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                } else if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 3) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                    itemStack = player.inventory.itemInOffHand
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 3) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                } else if (cause == EntityDamageEvent.DamageCause.WITHER) {
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 4) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                    itemStack = player.inventory.itemInOffHand
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 4) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                } else if (cause == EntityDamageEvent.DamageCause.DROWNING) {
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 5) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                    itemStack = player.inventory.itemInOffHand
                    if (itemStack.type == Material.WOODEN_HOE && itemStack.itemMeta.hasCustomModelData() && itemStack.itemMeta.customModelData == 5) {
                        e.isCancelled = true
                        rlgPlayer.changeMana(1)
                    }
                }
            }
            return
        } else if (e.entityType == EntityType.ENDER_DRAGON || e.entityType == EntityType.WITHER) {
            val damage = e.damage
            e.damage = damage / 2
            return
        }
    }

    @EventHandler
    fun onVehicleDamageEvent(e: VehicleDamageEvent){
        if(e.attacker is Player){
            if(eventCancel(e.vehicle.chunk, e.attacker as Player)){
                e.isCancelled = true
            }
        }else if(eventCancel(e.vehicle.chunk)){
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        if (e.hitEntity != null) {
            val entity = e.hitEntity
            try {
                if (e.entity.customName.contentEquals("firestaff1")) {
                    if (entity is LivingEntity) {
                        if (entity is Player) {
                            (entity as LivingEntity).damage(3.0)
                        } else {
                            entity.damage(10.0)
                        }
                    }
                }
            } catch (ignored: NullPointerException) { }
        }
    }
}