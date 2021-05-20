package de.rlg.items

import de.rlg.INSTANCE
import de.rlg.toStringList
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

object CraftingRecipes {
    fun initRecipes() {
        //region Mana Dust
        val manadust = CustomItems.manaDust()
        val manadustKey = NamespacedKey(INSTANCE, "manadust")
        val manadustRecipe = ShapedRecipe(manadustKey, manadust)
        manadustRecipe.shape("AAA", "ABA", "AAA")
        manadustRecipe.setIngredient('B', Material.REDSTONE)
        manadustRecipe.setIngredient('A', Material.GOLD_NUGGET)
        Bukkit.addRecipe(manadustRecipe)
        //endregion
        //region Mana Crystal
        val manacrystal = CustomItems.manaCrystal()
        val manacrystalKey = NamespacedKey(INSTANCE, "manacrystal")
        val manacrystalRecipe = ShapedRecipe(manacrystalKey, manacrystal)
        manacrystalRecipe.shape("ABA", "BCB", "ABA")
        manacrystalRecipe.setIngredient('A', Material.GOLD_NUGGET)
        manacrystalRecipe.setIngredient('B', Material.DIAMOND)
        manacrystalRecipe.setIngredient('C', Material.END_CRYSTAL)
        Bukkit.addRecipe(manacrystalRecipe)
        //endregion
        //region Nature Staff
        val naturestaff = CustomItems.natureStaff1()
        val naturestaffKey = NamespacedKey(INSTANCE, "naturestaff")
        val naturestaffRecipe = ShapedRecipe(naturestaffKey, naturestaff)
        naturestaffRecipe.shape(" DC", " AD", "B  ")
        naturestaffRecipe.setIngredient('B', Material.STICK)
        naturestaffRecipe.setIngredient('A', Material.GOLD_NUGGET) //Crystal
        naturestaffRecipe.setIngredient('D', Material.GOLD_NUGGET) //Dust
        naturestaffRecipe.setIngredient('C', Material.GREEN_DYE) //Element
        Bukkit.addRecipe(naturestaffRecipe)
        //endregion
        //region Fire Staff
        val firestaff = CustomItems.fireStaff1()
        val firestaffKey = NamespacedKey(INSTANCE, "firestaff")
        val firestaffrecipe = ShapedRecipe(firestaffKey, firestaff)
        firestaffrecipe.shape(" DC", "EAD", "BE ")
        firestaffrecipe.setIngredient('B', Material.BLAZE_ROD)
        firestaffrecipe.setIngredient('A', Material.GOLD_NUGGET) //Crystal
        firestaffrecipe.setIngredient('D', Material.GOLD_NUGGET) //Dust
        firestaffrecipe.setIngredient('C', Material.RED_DYE) //Element
        firestaffrecipe.setIngredient('E', Material.NETHER_BRICKS)
        Bukkit.addRecipe(firestaffrecipe)
        //endregion
        //region Weather Staff
        val weatherstaff = CustomItems.weatherStaff1()
        val weatherstaffKey = NamespacedKey(INSTANCE, "weatherstaff")
        val weatherstaffrecipe = ShapedRecipe(weatherstaffKey, weatherstaff)
        weatherstaffrecipe.shape(" DC", " AD", "B  ")
        weatherstaffrecipe.setIngredient('B', Material.STICK)
        weatherstaffrecipe.setIngredient('A', Material.GOLD_NUGGET) //Crystal
        weatherstaffrecipe.setIngredient('D', Material.GOLD_NUGGET) //Dust
        weatherstaffrecipe.setIngredient('C', Material.LIGHT_GRAY_DYE) //Element
        Bukkit.addRecipe(weatherstaffrecipe)
        //endregion
        //region Chaos Staff
        val chaosstaff = CustomItems.chaosStaff1()
        val chaosstaffKey = NamespacedKey(INSTANCE, "chaosstaff")
        val chaosstaffrecipe = ShapedRecipe(chaosstaffKey, chaosstaff)
        chaosstaffrecipe.shape(" DC", "EAD", "BE ")
        chaosstaffrecipe.setIngredient('B', Material.STICK)
        chaosstaffrecipe.setIngredient('A', Material.GOLD_NUGGET) //Crystal
        chaosstaffrecipe.setIngredient('D', Material.GOLD_NUGGET) //Dust
        chaosstaffrecipe.setIngredient('C', Material.BLACK_DYE) //Element
        chaosstaffrecipe.setIngredient('E', Material.OBSIDIAN) //Element
        Bukkit.addRecipe(chaosstaffrecipe)
        //endregion
        //region Water Staff
        val waterstaff = CustomItems.waterStaff1()
        val waterstaffKey = NamespacedKey(INSTANCE, "waterstaff")
        val waterstaffrecipe = ShapedRecipe(waterstaffKey, waterstaff)
        waterstaffrecipe.shape(" DC", "EAD", "BE ")
        waterstaffrecipe.setIngredient('B', Material.STICK)
        waterstaffrecipe.setIngredient('A', Material.GOLD_NUGGET) //Crystal
        waterstaffrecipe.setIngredient('D', Material.GOLD_NUGGET) //Dust
        waterstaffrecipe.setIngredient('C', Material.BLUE_DYE) //Element
        waterstaffrecipe.setIngredient('E', Material.NAUTILUS_SHELL) //Element
        Bukkit.addRecipe(waterstaffrecipe)
        //endregion
    }

    fun isManaDustRecipe(matrix: Array<ItemStack>): Boolean {
        for (itemStack in matrix) {
            if (itemStack.itemMeta.hasLore() && itemStack.itemMeta.lore()!!.toStringList().contains("Aus Creative-Inventar")) {
                return false
            }
            if (itemStack.type == Material.GOLD_NUGGET) {
                val im = itemStack.itemMeta
                if (!im.hasCustomModelData()) {
                    return false
                } else if (im.customModelData != 1) {
                    return false
                }
            }
        }
        return true
    }

    fun isManaCrystalRecipe(matrix: Array<ItemStack>): Boolean {
        for (itemStack in matrix) {
            if (itemStack.itemMeta.hasLore() && itemStack.itemMeta.lore()!!.toStringList().contains("Aus Creative-Inventar")) {
                return false
            }
            if (itemStack.type == Material.GOLD_NUGGET) {
                val im = itemStack.itemMeta
                if (!im.hasCustomModelData()) {
                    return false
                } else if (im.customModelData != 2) {
                    return false
                }
            }
        }
        return true
    }

    fun isStaff1Recipe(matrix: Array<ItemStack>): Boolean {
        for (i in matrix.indices) {
            val itemStack = matrix[i]
            if (itemStack.itemMeta.hasLore() && itemStack.itemMeta.lore()!!.toStringList().contains("Aus Creative-Inventar")) {
                return false
            }
            val im = itemStack.itemMeta
            if (itemStack.type == Material.GOLD_NUGGET) {
                if (!im.hasCustomModelData()) {
                    return false
                }
                if (i == 1 || i == 5) {
                    if (im.customModelData != 2) {
                        return false
                    }
                } else if (i == 4) {
                    if (im.customModelData != 3) {
                        return false
                    }
                }
            }
            if (i == 2) {
                if (!im.hasCustomModelData() || im.customModelData != 1) {
                    return false
                }
            }
        }
        return true
    }
}