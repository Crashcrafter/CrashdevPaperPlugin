package dev.crash.listener

import com.google.common.base.Functions
import com.google.common.collect.ImmutableMap
import dev.crash.*
import dev.crash.permission.*
import dev.crash.player.crashPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.block.Barrel
import org.bukkit.block.Chest
import org.bukkit.block.ShulkerBox
import org.bukkit.block.Sign
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import java.util.*

class InteractListener : Listener {

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock
        val player = e.player
        val itemInHand = player.inventory.itemInMainHand
        if (keyChests.containsKey(block)) {
            val inventory = when(val state = block?.state){
                is ShulkerBox -> state.inventory
                is Chest -> state.inventory
                is Barrel -> state.inventory
                else -> return
            }
            if (!itemInHand.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "keyToken"), PersistentDataType.STRING)
                && !lotteryI.contains(inventory)) {
                e.isCancelled = true
                return
            }
        } else if (e.hasBlock()) {
            val chunk = Objects.requireNonNull(block)!!.chunk
            if (itemInHand.type != Material.FIREWORK_ROCKET && chunk.isClaimed()) {
                val uuid: String = chunk.chunkData()!!.owner_uuid
                if (uuid.length <= 3 && uuid != "0" && block!!.type == Material.CHEST) {
                    waveManager(chunk)
                    if(player.gameMode == GameMode.SURVIVAL) e.isCancelled = true
                    return
                }
                else if (eventCancel(chunk, player)) {
                    e.isCancelled = true
                    return
                }
            } else if (eventCancel(chunk, player)) {
                e.isCancelled = true
                return
            } else if (e.action == Action.RIGHT_CLICK_BLOCK && block!!.state is Sign && player.isSneaking && !eventCancel(block.chunk, player)) {
                val sign = block.state as Sign
                sign.isEditable = true
                player.openSign((block.state as Sign))
                return
            }else if (player.inventory.itemInMainHand.type == Material.FISHING_ROD && e.clickedBlock!!.type == Material.NOTE_BLOCK) {
                addAFKCounter(player)
            }
        }
        val type = itemInHand.type
        if (itemInHand.hasItemMeta() && itemInHand.itemMeta.hasCustomModelData()) {
            when(type) {
                Material.FIRE_CHARGE -> {
                    val data = itemInHand.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "itemData"), PersistentDataType.STRING) ?: return
                    if(!eventCancel(player.location.chunk, player)){
                        e.isCancelled = true
                        val fireball = player.launchProjectile(Fireball::class.java, player.velocity)
                        fireball.persistentDataContainer.set(NamespacedKey(INSTANCE, "entityData"), PersistentDataType.STRING, data)
                        if(player.gameMode != GameMode.CREATIVE) player.inventory.itemInMainHand.amount--
                    }
                    return
                }
                Material.STICK -> {
                    val data = itemInHand.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "itemData"), PersistentDataType.STRING) ?: return
                    if(itemInHand.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "cheated"), PersistentDataType.STRING)){
                       return
                    }
                    when(data){
                        "addClaim" -> {
                            e.isCancelled = true
                            changeAddedClaims(player, 1)
                            player.inventory.itemInMainHand.amount--
                            player.sendMessage("§2Congratulation! §6You received an additional claim!")
                        }
                        "addHome" -> {
                            e.isCancelled = true
                            changeAddedHomes(player, 1)
                            player.inventory.itemInMainHand.amount--
                            player.sendMessage("§2Congratulation! §6You received an additional homepoint!")
                        }
                    }
                    return
                }
                else -> {}
            }
            if(itemInHand.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "range"), PersistentDataType.STRING) && e.action == Action.LEFT_CLICK_AIR){
                val range = itemInHand.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "range"), PersistentDataType.STRING)!!.toInt()
                val target = player.getTargetEntity(range, false)
                if(target != null && target is LivingEntity){
                    var damage = itemInHand.itemMeta.attributeModifiers?.get(Attribute.GENERIC_ATTACK_DAMAGE)?.first()?.amount ?:
                    type.getDefaultAttributeModifiers(EquipmentSlot.HAND).get(Attribute.GENERIC_ATTACK_DAMAGE).first().amount

                    itemInHand.enchantments.forEach {
                        damage += it.key.getDamageIncrease(it.value, target.category)
                    }
                    val event = EntityDamageByEntityEvent(player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK,
                        EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage)),
                        EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damage))),
                        false)
                    Bukkit.getPluginManager().callEvent(event)
                    if(!event.isCancelled){
                        target.damage(damage, player)
                    }
                    return
                }
            }
        }
    }

    @EventHandler
    fun onPlayerInteractEntity(e: PlayerInteractEntityEvent) {
        if (e.rightClicked is Villager) {
            val shop = e.rightClicked as Villager
            try {
                when(shop.persistentDataContainer.get(NamespacedKey(INSTANCE, "entityData"), PersistentDataType.STRING)) {
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
            val player = e.player
            val crashPlayer = player.crashPlayer()
            try {
                if (shop.persistentDataContainer.get(NamespacedKey(INSTANCE, "entityData"), PersistentDataType.STRING) == "blackmarket") {
                    e.isCancelled = true
                    if(crashPlayer.xpLevel >= 25){
                        showTradingInventory(e.player, BlackMarketInventories.blackMarketOverview, "Blackmarket")
                    }else {
                        player.sendActionBar(Component.text("§6You need level 25 to access the blackmarket!"))
                    }
                    return
                }
            } catch (ignored: NullPointerException) { }
        }
        if (eventCancel(e.player.chunk, e.player)) {
            e.isCancelled = true
            return
        }
    }
}