package de.rlg.listener

import de.rlg.*
import de.rlg.items.staffs.*
import de.rlg.permission.changeAddedClaims
import de.rlg.permission.chunks
import de.rlg.permission.eventCancel
import de.rlg.permission.isClaimed
import de.rlg.player.rlgPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
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
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

class InteractListener : Listener {

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val block = e.clickedBlock
        val player = e.player
        if (keyChests.containsKey(block)) {
            try {
                if (!player.inventory.itemInMainHand.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgKeyToken"), PersistentDataType.STRING)
                    && !lotteryI.contains((block!!.state as ShulkerBox).inventory)) {
                    e.isCancelled = true
                    return
                }
            }catch (ex: NullPointerException) {
                e.isCancelled = true
                return
            }
        } else if (e.hasBlock()) {
            val chunk = Objects.requireNonNull(block)!!.chunk
            if (player.inventory.itemInMainHand.type != Material.FIREWORK_ROCKET && chunk.isClaimed()) {
                val uuid: String = chunks[chunk]!!.owner_uuid
                if (uuid.length <= 3 && !uuid.contentEquals("0") && block!!.type == Material.CHEST) {
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
            } else if (e.action == Action.RIGHT_CLICK_BLOCK && block!!.state is Sign) {
                val sign = block.state as Sign
                if (signs.containsKey(sign.block)) {
                    signClickHandler(player, sign)
                    return
                } else if (player.isSneaking && !eventCancel(block.chunk, player)) {
                    sign.isEditable = true
                    player.openSign((block.state as Sign))
                    return
                }
            }else if (player.inventory.itemInMainHand.type == Material.FISHING_ROD && e.clickedBlock!!.type == Material.NOTE_BLOCK) {
                addAFKCounter(player)
            }
        }
        val itemStack = player.inventory.itemInMainHand
        val type = itemStack.type
        if (itemStack.hasItemMeta() && itemStack.itemMeta.hasCustomModelData()) {
            val cmd = itemStack.itemMeta.customModelData
            when(type) {
                Material.WRITTEN_BOOK -> {
                    val bm = itemStack.itemMeta as BookMeta
                    bm.pages(when(bm.customModelData){
                        1 -> basicmagicbook.toMutableList().toComponentList()
                        2 -> beginnerbook.toMutableList().toComponentList()
                        else -> bm.pages()
                    })
                    itemStack.setItemMeta(bm)
                }
                Material.WOODEN_HOE -> {
                    when(cmd){
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
                    if(!eventCancel(player.location.chunk, player)){
                        e.isCancelled = true
                        val fireball = player.launchProjectile(Fireball::class.java, player.velocity)
                        fireball.persistentDataContainer.set(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING, data)
                        player.inventory.itemInMainHand.amount--
                    }
                    return
                }
                Material.STICK -> {
                    val data = itemStack.itemMeta.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgItemData"), PersistentDataType.STRING) ?: return
                    if(data == "addClaim" && !itemStack.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "cheated"), PersistentDataType.STRING)){
                        e.isCancelled = true
                        changeAddedClaims(player, 1)
                        player.inventory.itemInMainHand.amount--
                        player.sendMessage("§2Herzlichen Glückwunsch! §6Du hast einen zusätzlichen Claim erhalten!")
                    }
                    return
                }
            }
            if(customRangeMap.containsKey(type) && customRangeMap[type]!!.containsKey(cmd)){
                val range = customRangeMap[type]!![cmd]!!
                val target = player.getTargetEntity(range, false)
                if(target != null && target is LivingEntity){
                    var damage = itemStack.itemMeta.attributeModifiers?.get(Attribute.GENERIC_ATTACK_DAMAGE)?.first()?.amount ?: type.getItemAttributes(EquipmentSlot.HAND).get(Attribute.GENERIC_ATTACK_DAMAGE).first().amount
                    itemStack.enchantments.forEach {
                        damage += it.key.getDamageIncrease(it.value, target.category)
                    }
                    target.damage(damage, player)
                    val event = EntityDamageByEntityEvent(player, target, EntityDamageEvent.DamageCause.ENTITY_ATTACK, damage)
                    Bukkit.getPluginManager().callEvent(event)
                    return
                }
                /*val targetBlock = player.rayTraceBlocks(range.toDouble())?.hitBlock
                if(targetBlock != null){
                    targetBlock.
                }*/
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
            val player = e.player
            val rlgPlayer = player.rlgPlayer()
            try {
                if (shop.persistentDataContainer.get(NamespacedKey(INSTANCE, "rlgEntityData"), PersistentDataType.STRING) == "blackmarket") {
                    e.isCancelled = true
                    if(rlgPlayer.xpLevel >= 25){
                        showTradingInventory(e.player, BlackMarketInventories.blackMarketOverview, "Schwarzmarkt")
                    }else {
                        player.sendActionBar(Component.text("§6Du benötigst Level 25, um auf den Schwarzmarkt zugreifen zu können!"))
                    }
                    return
                }
            } catch (ignored: NullPointerException) { }
        }
        if (eventCancel(e.player.chunk, e.player)) {
            e.isCancelled = true
        }
    }
}