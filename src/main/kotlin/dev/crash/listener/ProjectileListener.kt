package dev.crash.listener

import dev.crash.INSTANCE
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ProjectileListener : Listener {

    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        if(e.hitEntity!=null && e.hitEntity is LivingEntity){
            if(e.entity.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING)){
                val entity = e.hitEntity as LivingEntity
                when(e.entity.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING)){
                    "mudBall" -> {
                        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 50, 2))
                        entity.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 100, 4))
                    }
                }
            }
        }else {
            if(e.entity.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING)){
                val loc = e.entity.location
                when(e.entity.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING)) {
                    "throwSmallFireball" -> {
                        loc.world.createExplosion(loc, 1f)
                    }
                    "throwMediumFireball" -> {
                        loc.world.createExplosion(loc, 2.5f)
                    }
                    "throwBigFireball" -> {
                        loc.world.createExplosion(loc, 5f)
                    }
                }
            }
        }
    }
}