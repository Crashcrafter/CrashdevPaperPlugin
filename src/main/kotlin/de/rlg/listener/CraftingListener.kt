package de.rlg.listener

import de.rlg.INSTANCE
import de.rlg.items.CraftingRecipes
import de.rlg.questCount
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.persistence.PersistentDataType

class CraftingListener : Listener {

    @EventHandler
    fun onCrafting(e: CraftItemEvent) {
        e.inventory.contents.forEach {
            if(it != null && it.hasItemMeta() && it.itemMeta.persistentDataContainer.has(NamespacedKey(INSTANCE, "rlgCheated"), PersistentDataType.STRING)){
                e.result = Event.Result.DENY
                e.isCancelled = true
                e.whoClicked.closeInventory()
                return
            }
        }
        if (e.recipe.result.hasItemMeta()) {
            if (e.recipe.result.itemMeta.hasCustomModelData()) {
                val inHand = e.whoClicked.inventory.itemInMainHand
                val modeldata = e.recipe.result.itemMeta.customModelData
                val material = e.recipe.result.type
                val craftinginv = e.inventory.matrix
                if (inHand.type == Material.WRITTEN_BOOK && inHand.itemMeta.hasCustomModelData()) {
                    var deny = false
                    when (material) {
                        Material.GOLD_NUGGET -> if (modeldata == 2) {
                            if (!CraftingRecipes.isManaDustRecipe(craftinginv)) {
                                deny = true
                            }
                        } else if (modeldata == 3) {
                            if (!CraftingRecipes.isManaCrystalRecipe(craftinginv)) {
                                deny = true
                            }
                        }
                        Material.WOODEN_HOE -> {
                            if (!CraftingRecipes.isStaff1Recipe(craftinginv)) {
                                deny = true
                            }
                            questCount(e.whoClicked as Player, 2, 1, false)
                        }
                        else -> {
                        }
                    }
                    if (deny) {
                        e.result = Event.Result.DENY
                        e.isCancelled = true
                        e.whoClicked.sendMessage("§4Das ist nicht das richtige Rezept!")
                        e.whoClicked.closeInventory()
                        return
                    } else {
                        if (material == Material.GOLD_INGOT && modeldata == 2) {
                            questCount(e.whoClicked as Player, 17, 1, true)
                        }
                    }
                } else {
                    e.whoClicked.sendMessage("§4Du brauchst das Basiswissen-Magie-Buch in der Hand, um Mana zu craften!")
                    e.result = Event.Result.DENY
                    e.isCancelled = true
                }
            }
        }
        if(e.inventory.result != null){
            val item = e.inventory.result!!
            val im = item.itemMeta
            im.persistentDataContainer.set(NamespacedKey(INSTANCE, "craftedBy"), PersistentDataType.STRING, (e.whoClicked as Player).name)
        }
    }
}