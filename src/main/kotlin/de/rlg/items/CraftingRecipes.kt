package de.rlg.items

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.INSTANCE
import de.rlg.itemStringToItem
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import java.io.File

object CraftingRecipes {
    data class CustomRecipe(val result: String, val shape: List<String>, val ingredients: HashMap<Char, String>)

    fun loadRecipes() {
        val file = File(INSTANCE.dataFolder.path + "/recipes.json")
        if(file.exists()){
            val customItems = jacksonObjectMapper().readValue<HashMap<String, CustomRecipe>>(file)
            customItems.forEach { (name, recipeObj) ->
                val namespacedKey = NamespacedKey(INSTANCE, name)
                val recipe = ShapedRecipe(namespacedKey, itemStringToItem(recipeObj.result))
                val shape = recipeObj.shape
                try {
                    recipe.shape(shape[0], shape[1], shape[2])
                }catch (ex: ArrayIndexOutOfBoundsException){println("Out of index at recipe $name")}
                recipeObj.ingredients.forEach {
                    recipe.setIngredient(it.key, itemStringToItem(it.value))
                }
                Bukkit.addRecipe(recipe)
            }
        }else {
            file.createNewFile()
            jacksonObjectMapper().writeValue(file, hashMapOf<String, CustomRecipe>())
        }
    }
}