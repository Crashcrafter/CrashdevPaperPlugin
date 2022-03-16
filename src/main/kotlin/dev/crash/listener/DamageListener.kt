package dev.crash.listener

import dev.crash.permission.damageEventCancel
import dev.crash.permission.eventCancel
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.vehicle.VehicleDamageEvent

class DamageListener : Listener {

    @EventHandler
    fun onEntityDamage(e: EntityDamageEvent) {
        if (e.entityType == EntityType.PLAYER) {
            val player = e.entity as Player
            val chunk = player.location.chunk
            if (e.cause != EntityDamageEvent.DamageCause.VOID && damageEventCancel(chunk, player)) {
                e.isCancelled = true
                return
            }
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
}