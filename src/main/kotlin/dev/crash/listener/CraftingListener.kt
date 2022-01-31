package dev.crash.listener

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent
import dev.crash.INSTANCE
import dev.crash.getItem
import dev.crash.questCount
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.inventory.CraftingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

class CraftingListener : Listener {

    @EventHandler
    fun onCrafting(e: CraftItemEvent) {
        e.inventory.contents!!.forEach {
            if(it != null && it.hasItemMeta() && it.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "crashCheated"), PersistentDataType.STRING)){
                e.result = Event.Result.DENY
                e.isCancelled = true
                e.whoClicked.closeInventory()
                return
            }
        }
        if (e.recipe.result.hasItemMeta() && e.recipe.result.itemMeta.hasCustomModelData()) {
            val modelData = e.recipe.result.itemMeta.customModelData
            val material = e.recipe.result.type
            if (material == Material.WOODEN_HOE && modelData in 1..5) {
                questCount(e.whoClicked as Player, 2, 1, false)
            }
        }
        if(e.inventory.result != null){
            val item = e.inventory.result!!
            val im = item.itemMeta
            im.persistentDataContainer.set(NamespacedKey(INSTANCE, "craftedBy"), PersistentDataType.STRING, (e.whoClicked as Player).name)
        }
    }

    @EventHandler
    fun onRecipe(e: PlayerRecipeBookClickEvent){
        val craftingInv = e.player.openInventory.topInventory as CraftingInventory
        val recipe = Bukkit.getRecipe(e.recipe)!!
        if(!recipe.result.hasItemMeta() || !recipe.result.itemMeta.hasCustomModelData()){
            return
        }
        if(recipe is ShapedRecipe){
            val ingredients = recipe.ingredientMap
            val result = mutableListOf<ItemStack?>()
            recipe.shape.forEach { shape ->
                shape.forEach {
                    result.add(ingredients[it])
                }
            }
            val player = e.player
            var hasAnyItem = false
            craftingInv.contents!!.forEach {
                player.inventory.addItem(it ?: return@forEach)
            }
            craftingInv.clear()
            result.indices.forEach { index ->
                val item = result[index]
                if(item != null){
                    val playerItem = player.inventory.getItem(item)
                    if(playerItem != null){
                        playerItem.amount--
                        craftingInv.setItem(index+1, item)
                        hasAnyItem = true
                    }
                }
            }
            e.isCancelled = hasAnyItem
            return
        }
    }
}