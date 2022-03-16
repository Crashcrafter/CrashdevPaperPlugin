package dev.crash.listener

import dev.crash.*
import dev.crash.permission.canBack
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import java.util.*

class DeathListener : Listener {

    @EventHandler
    fun onPlayerDeath(deathEvent: PlayerDeathEvent){
        val player: Player = deathEvent.entity.player!!
        try {
            if (deathEvent.entity.lastDamageCause!!.cause == DamageCause.ENTITY_ATTACK) {
                val killer = player.killer
                if (killer != null) questCount(killer, 9, 1, true)
            }
        } catch (_: NullPointerException) { }
        val location: Location = deathEvent.entity.location
        if(canBack(location.chunk, player)){
            player.crashPlayer().deathPos = location
        }
        deathEvent.deathMessage(Component.text("§4").append(deathEvent.deathMessage()!!))
        player.inventory.armorContents?.forEach {
            val durability = (it?.itemMeta as? Damageable ?: return@forEach).damage
            val maxDurability = it.type.maxDurability
            val newDurability = (durability + maxDurability / 4)
            if (newDurability >= maxDurability) {
                deathEvent.drops.remove(it)
            } else {
                (it.itemMeta as Damageable).damage = newDurability
            }
        }
        val deaths: Int = INSTANCE.config.getInt("Players." + player.uniqueId.toString() + ".Deaths")
        INSTANCE.config.set("Players." + player.uniqueId.toString() + ".Deaths", deaths + 1)
        INSTANCE.saveConfig()
        player.updateScoreboard()
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent) {
        val type = e.entityType
        if (type == EntityType.DROPPED_ITEM) {
            val item = e.entity as Item
            val itemStack = item.itemStack
            if (itemStack.hasItemMeta()) {
                val token = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "keyToken"), PersistentDataType.STRING)
                if(token != null) redeemKey(token)
            }
        } else if(e.entity.killer != null) {
            val player = e.entity.killer!!
            when(type) {
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.HUSK, EntityType.ENDERMAN, EntityType.ZOMBIE_VILLAGER -> {
                    if (type == EntityType.ZOMBIE || type == EntityType.HUSK || type == EntityType.ZOMBIE_VILLAGER) {
                        questCount(player, 3, 1, true)
                    }
                    questCount(player, 2, 1, true)
                    questCount(player, 6, 1, false)
                }
                EntityType.ENDER_DRAGON -> {
                    questCount(player, 1, 1, false)
                    e.drops.add(customItemsMap["dragon_scale"]!!.asQuantity(Random().nextInt(3)-1))
                }
                EntityType.COW -> questCount(player, 14, 1, true)
                EntityType.WITHER -> questCount(player, 4, 1, false)
                EntityType.PILLAGER -> questCount(player, 16, 1, true)
                else -> return
            }
        }
    }
}