package dev.crash.listener

import dev.crash.dropWardenName
import dev.crash.permission.chunks
import dev.crash.permission.isClaimed
import org.bukkit.entity.EntityType
import org.bukkit.entity.Vex
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class MobSpawnListener : Listener {

    @EventHandler
    fun onMobSpawn(e: EntitySpawnEvent) {
        val entity = e.entity
        val chunk = entity.location.chunk
        if (entity.type == EntityType.PHANTOM) {
            e.isCancelled = true
        } else if (entity.type == EntityType.WITHER) {
            val location = entity.location
            if (location.x > -200 && location.x < 800 || location.z > -600 && location.z < 400) {
                if (location.world.name.contentEquals("newworld")) e.isCancelled = true
            }
        } else if (entity.type != EntityType.DROPPED_ITEM && entity.type != EntityType.FALLING_BLOCK && entity.type != EntityType.FIREWORK) {
            if(!chunk.isClaimed()) return
            if (chunks[chunk.chunkKey]!![chunk.world.name]!!.owner_uuid.contentEquals("0")) {
                if (e.entityType != EntityType.VILLAGER && e.entityType != EntityType.ARMOR_STAND && e.entityType != EntityType.WANDERING_TRADER && e.entityType != EntityType.ITEM_FRAME && e.entityType != EntityType.EVOKER_FANGS && e.entityType != EntityType.PAINTING) {
                    if (e.entity.entitySpawnReason != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
                        e.isCancelled = true
                    }
                }
            }
        } else if (entity.type == EntityType.VEX) {
            val vex = entity as Vex
            if(chunk.isClaimed() && chunks[chunk.chunkKey]!![chunk.world.name]!!.owner_uuid.length < 3){
                vex.addPotionEffect(PotionEffect(PotionEffectType.INCREASE_DAMAGE, 1, 0))
                if (vex.entitySpawnReason == CreatureSpawnEvent.SpawnReason.DEFAULT) {
                    vex.customName = dropWardenName
                    vex.isCustomNameVisible = false
                }
            }
        }
    }
}