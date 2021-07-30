package de.rlg.items

import de.rlg.INSTANCE
import de.rlg.customItemsMap
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe

object CraftingRecipes {
    fun initRecipes() {
        //region Mana Dust
        val manadustKey = NamespacedKey(INSTANCE, "manadust")
        val manadustRecipe = ShapedRecipe(manadustKey, customItemsMap["mana_dust"]!!)
        manadustRecipe.shape("AAA", "ABA", "AAA")
        manadustRecipe.setIngredient('B', Material.REDSTONE)
        manadustRecipe.setIngredient('A', customItemsMap["mana_shard"]!!)
        Bukkit.addRecipe(manadustRecipe)
        //endregion
        //region Mana Crystal
        val manacrystalKey = NamespacedKey(INSTANCE, "manacrystal")
        val manacrystalRecipe = ShapedRecipe(manacrystalKey, customItemsMap["mana_crystal"]!!)
        manacrystalRecipe.shape("ABA", "BCB", "ABA")
        manacrystalRecipe.setIngredient('A', customItemsMap["mana_dust"]!!)
        manacrystalRecipe.setIngredient('B', Material.DIAMOND)
        manacrystalRecipe.setIngredient('C', Material.END_CRYSTAL)
        Bukkit.addRecipe(manacrystalRecipe)
        //endregion
        //region Nature Staff
        val naturestaffKey = NamespacedKey(INSTANCE, "naturestaff")
        val naturestaffRecipe = ShapedRecipe(naturestaffKey, customItemsMap["nature_staff"]!!)
        naturestaffRecipe.shape(" DC", " AD", "B  ")
        naturestaffRecipe.setIngredient('B', Material.STICK)
        naturestaffRecipe.setIngredient('A', customItemsMap["mana_crystal"]!!) //Crystal
        naturestaffRecipe.setIngredient('D', customItemsMap["mana_dust"]!!) //Dust
        naturestaffRecipe.setIngredient('C', customItemsMap["nature_element"]!!) //Element
        Bukkit.addRecipe(naturestaffRecipe)
        //endregion
        //region Fire Staff
        val firestaffKey = NamespacedKey(INSTANCE, "firestaff")
        val firestaffrecipe = ShapedRecipe(firestaffKey, customItemsMap["fire_staff"]!!)
        firestaffrecipe.shape(" DC", "EAD", "BE ")
        firestaffrecipe.setIngredient('B', Material.BLAZE_ROD)
        firestaffrecipe.setIngredient('A', customItemsMap["mana_crystal"]!!) //Crystal
        firestaffrecipe.setIngredient('D', customItemsMap["mana_dust"]!!) //Dust
        firestaffrecipe.setIngredient('C', customItemsMap["fire_element"]!!) //Element
        firestaffrecipe.setIngredient('E', Material.NETHER_BRICKS)
        Bukkit.addRecipe(firestaffrecipe)
        //endregion
        //region Weather Staff
        val weatherstaffKey = NamespacedKey(INSTANCE, "weatherstaff")
        val weatherstaffrecipe = ShapedRecipe(weatherstaffKey, customItemsMap["weather_staff"]!!)
        weatherstaffrecipe.shape(" DC", " AD", "B  ")
        weatherstaffrecipe.setIngredient('B', Material.STICK)
        weatherstaffrecipe.setIngredient('A', customItemsMap["mana_crystal"]!!) //Crystal
        weatherstaffrecipe.setIngredient('D', customItemsMap["mana_dust"]!!) //Dust
        weatherstaffrecipe.setIngredient('C', customItemsMap["weather_element"]!!) //Element
        Bukkit.addRecipe(weatherstaffrecipe)
        //endregion
        //region Chaos Staff
        val chaosstaffKey = NamespacedKey(INSTANCE, "chaosstaff")
        val chaosstaffrecipe = ShapedRecipe(chaosstaffKey, customItemsMap["chaos_staff"]!!)
        chaosstaffrecipe.shape(" DC", "EAD", "BE ")
        chaosstaffrecipe.setIngredient('B', Material.STICK)
        chaosstaffrecipe.setIngredient('A', customItemsMap["mana_crystal"]!!) //Crystal
        chaosstaffrecipe.setIngredient('D', customItemsMap["mana_dust"]!!) //Dust
        chaosstaffrecipe.setIngredient('C', customItemsMap["chaos_element"]!!) //Element
        chaosstaffrecipe.setIngredient('E', Material.OBSIDIAN)
        Bukkit.addRecipe(chaosstaffrecipe)
        //endregion
        //region Water Staff
        val waterstaffKey = NamespacedKey(INSTANCE, "waterstaff")
        val waterstaffrecipe = ShapedRecipe(waterstaffKey, customItemsMap["water_staff"]!!)
        waterstaffrecipe.shape(" DC", "EAD", "BE ")
        waterstaffrecipe.setIngredient('B', Material.STICK)
        waterstaffrecipe.setIngredient('A', customItemsMap["mana_crystal"]!!) //Crystal
        waterstaffrecipe.setIngredient('D', customItemsMap["mana_dust"]!!) //Dust
        waterstaffrecipe.setIngredient('C', customItemsMap["water_element"]!!) //Element
        waterstaffrecipe.setIngredient('E', Material.NAUTILUS_SHELL)
        Bukkit.addRecipe(waterstaffrecipe)
        //endregion
    }
}