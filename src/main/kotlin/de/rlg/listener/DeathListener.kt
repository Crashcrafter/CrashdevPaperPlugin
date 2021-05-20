package de.rlg.listener

import de.rlg.*
import de.rlg.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
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
        } catch (ignored: NullPointerException) {
        }
        val location: Location = deathEvent.entity.location
        player.rlgPlayer().deathPos = location
        val deathMessage = StringBuilder()
        deathMessage.append("§4")
        try {
            when (Objects.requireNonNull(deathEvent.entity.lastDamageCause)!!.cause) {
                DamageCause.FIRE, DamageCause.FIRE_TICK -> deathMessage.append(
                    player.name
                ).append(" wusste nicht, dass Feuer brennen kann")
                DamageCause.DRAGON_BREATH -> deathMessage.append(player.name).append(" wurde vom Drachen bespuckt")
                DamageCause.VOID -> deathMessage.append(player.name).append(" wurde vom Void verschlungen")
                DamageCause.WITHER -> deathMessage.append(player.name).append(" hat den schwarzen Tod erlitten")
                DamageCause.SUFFOCATION -> deathMessage.append(player.name).append(" ist erstickt")
                DamageCause.LAVA -> deathMessage.append(player.name)
                    .append(" hat vergessen die Badetemperatur einzustellen")
                DamageCause.FALLING_BLOCK -> deathMessage.append(player.name).append(" hat nicht nach oben geschaut")
                DamageCause.LIGHTNING -> deathMessage.append("Auf ").append(player.name)
                    .append(" wurde Thor's Zorn entladen!")
                DamageCause.MAGIC -> deathMessage.append(player.name).append(" hat zu viele Energydrinks getrunken")
                DamageCause.FALL -> deathMessage.append(player.name).append(" hat die Seife aufgehoben")
                DamageCause.DROWNING -> {
                    val random = Random()
                    val randid = random.nextInt(3)
                    if (randid == 0) {
                        deathMessage.append(player.name).append(" hat vergessen Luft zu holen")
                    } else if (randid == 1) {
                        deathMessage.append(player.name).append(" ist mit einem Toaster schwimmen gegangen")
                    } else {
                        deathMessage.append(player.name).append(" hat nach Atlantis gesucht")
                    }
                }
                DamageCause.CONTACT -> deathMessage.append(player.name).append(" hat die Nadel im Heuhaufen gefunden")
                DamageCause.ENTITY_ATTACK -> if (deathEvent.entity.lastDamageCause is EntityDamageByEntityEvent) {
                    val entityEvent = deathEvent.entity.lastDamageCause!! as EntityDamageByEntityEvent
                    when (entityEvent.damager.type) {
                        EntityType.ZOMBIE -> deathMessage.append(player.name)
                            .append(" konnte den Bissen nicht mehr standhalten")
                        EntityType.PIGLIN -> deathMessage.append(player.name).append(" hat zu wenig geboten")
                        EntityType.SPIDER -> deathMessage.append(player.name).append(" hatte Arachnophobie")
                        EntityType.CAVE_SPIDER -> deathMessage.append(player.name)
                            .append(" hat im Minenschacht zu tief gegraben")
                        EntityType.PLAYER -> {
                            val damager = entityEvent.damager as Player
                            deathMessage.append(player.name).append(" wurde von ").append(damager.name)
                                .append(" überrascht")
                        }
                        EntityType.VINDICATOR -> deathMessage.append(player.name).append(" wurde von Äxten überrannt")
                        EntityType.RAVAGER -> deathMessage.append(player.name).append(" wurde vom Vieh zertrampelt")
                        EntityType.VEX -> deathMessage.append(player.name).append(" wurde von Geistern heimgesucht")
                        EntityType.PHANTOM -> {
                            val random1 = Random()
                            val id = random1.nextInt(2)
                            if (id == 0) {
                                deathMessage.append(player.name).append(" wusste nicht, dass der Tod von oben kommt")
                            } else {
                                deathMessage.append(player.name).append(" hat zu viele Nächte durchgemacht")
                            }
                        }
                        EntityType.ZOMBIE_VILLAGER -> deathMessage.append(player.name)
                            .append(" hat den Schwächetrank vergessen")
                        EntityType.HUSK -> deathMessage.append(player.name)
                            .append(" hat in der Wüste nicht aufgepasst")
                        EntityType.SLIME -> deathMessage.append(player.name).append(" wurde zu fest gekuschelt")
                        EntityType.DROWNED -> deathMessage.append(player.name)
                            .append(" sollte Unterwasser besser aufpassen")
                        EntityType.ENDERMAN -> deathMessage.append(player.name)
                            .append(" hat den Anstarrwettbewerb gegen den Enderman verloren")
                        EntityType.WITHER_SKELETON -> deathMessage.append(player.name)
                            .append(" hat den schwarzen Tod erlitten")
                        EntityType.SILVERFISH -> deathMessage.append(player.name)
                            .append(" traut sich ab jetzt nicht mehr ins Badezimmer")
                        EntityType.ENDERMITE -> deathMessage.append(player.name)
                            .append(" wurde von einem Endermite gebissen")
                        EntityType.IRON_GOLEM -> deathMessage.append(player.name)
                            .append(" hat sich mit dem Falschen angelegt")
                        EntityType.WOLF -> deathMessage.append(player.name).append(" wurde von einem Hund zerbissen")
                        else -> deathMessage.append(deathEvent.deathMessage().toString())
                    }
                }
                DamageCause.ENTITY_EXPLOSION -> deathMessage.append(player.name)
                    .append(" hat das Zischen nicht gehört")
                else -> deathMessage.append(deathEvent.deathMessage().toString())
            }
        } catch (ignored: NullPointerException) {
            deathMessage.append(deathEvent.deathMessage().toString())
        }
        deathEvent.deathMessage(Component.text(deathMessage.toString()))
        player.inventory.armorContents.forEach {
            try {
                val durability = it.durability
                val maxDurability = it.type.maxDurability
                val newDurability = (durability + maxDurability / 4).toShort()
                if (newDurability >= maxDurability) {
                    deathEvent.drops.remove(it)
                } else {
                    it.durability = newDurability
                }
            }catch (ex: NullPointerException) {}
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
                val im = itemStack.itemMeta
                if (im.hasLore()) {
                    val lore = im.lore()!!.toStringList()
                    if (lore[0].split(" ").toTypedArray()[0].contentEquals("Token:")) {
                        val token = lore[0].split(" ").toTypedArray()[1]
                        if (tokenExists(token)) {
                            redeemKey(token)
                        }
                    }
                }
            }
        } else if(e.entity.killer != null) {
            val player = e.entity.killer!!
            when(type) {
                EntityType.ZOMBIE, EntityType.SKELETON, EntityType.CREEPER, EntityType.SPIDER, EntityType.CAVE_SPIDER -> {
                    if (type == EntityType.ZOMBIE) {
                        questCount(player, 3, 1, true)
                    }
                    questCount(player, 2, 1, true)
                    questCount(player, 6, 1, false)
                }
                EntityType.ENDER_DRAGON -> questCount(player, 1, 1, false)
                EntityType.COW -> questCount(player, 14, 1, true)
                EntityType.WITHER -> questCount(player, 4, 1, false)
                EntityType.PILLAGER -> questCount(player, 16, 1, true)
                else -> return
            }
        }
    }
}