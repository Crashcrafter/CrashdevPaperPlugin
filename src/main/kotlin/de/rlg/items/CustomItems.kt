package de.rlg.items

import de.rlg.INSTANCE
import de.rlg.basicmagicbook
import de.rlg.beginnerbook
import de.rlg.shopbook
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType

object CustomItems {
    fun defaultCustomItem(m: Material, displayName: String, lore: MutableList<String>, cmd: Int=0, data: Pair<String, String>?=null): ItemStack{
        val itemStack = ItemStack(m)
        val itemMeta = itemStack.itemMeta
        itemMeta.displayName(Component.text(displayName))
        val componentArray = ArrayList<Component>()
        lore.forEach {
            componentArray.add(Component.text(it))
        }
        itemMeta.lore(componentArray)
        itemMeta.setCustomModelData(cmd)
        if(data != null) itemMeta.persistentDataContainer.set(NamespacedKey(INSTANCE, data.first), PersistentDataType.STRING, data.second)
        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun katzeRose(): ItemStack = defaultCustomItem(Material.POPPY, "Katze's Liebesrosen", arrayListOf("Das Original"))

    fun manaShard(): ItemStack = defaultCustomItem(Material.GOLD_NUGGET, "Mana Shard", arrayListOf("Basismaterial für Magie"), 1)

    fun manaDust(): ItemStack = defaultCustomItem(Material.GOLD_NUGGET, "Mana Dust", arrayListOf("Basismaterial für Magie"), 2)

    fun manaCrystal(): ItemStack = defaultCustomItem(Material.GOLD_NUGGET, "Mana Crystal", arrayListOf("Basismaterial für Magie"), 3)

    fun weatherElement(): ItemStack = defaultCustomItem(Material.LIGHT_GRAY_DYE, "Wetter-Element", arrayListOf("Element der Magie"), 1)
    fun chaosElement(): ItemStack = defaultCustomItem(Material.BLACK_DYE, "Chaos-Element", arrayListOf("Element der Magie"), 1)
    fun waterElement(): ItemStack = defaultCustomItem(Material.BLUE_DYE, "Wasser-Element", arrayListOf("Element der Magie"), 1)
    fun natureElement(): ItemStack = defaultCustomItem(Material.GREEN_DYE, "Natur-Element", arrayListOf("Element der Magie"), 1)
    fun fireElement(): ItemStack = defaultCustomItem(Material.RED_DYE, "Feuer-Element", arrayListOf("Element der Magie"), 1)

    fun natureStaff1(): ItemStack = defaultCustomItem(Material.WOODEN_HOE, "§r§l§2Natur-Stab", arrayListOf(
        "", "§fLeft-Click §1(30)§f:", "§2Entfernt an dem Ort alle Natur-Blöcke und vergiftet die Mobs",
        "", "§fRight-Click auf Block §1(15)§f:", "§2Wende einen stärkeren Bonemeal-Effekt an", "",
        "§2Immun gegen Gift §1(1)", ""
    ), 1)

    fun fireStaff1(): ItemStack = defaultCustomItem(Material.WOODEN_HOE, "§r§l§4Feuer-Stab", arrayListOf(
        "", "§fRight-Click in Luft §1(30)§f:", "§4Schießt eine Salve Feuerkugeln", "",
        "§fRight-Click auf Block §1(5)§f:", "§4Zündet den Block mit einer Feuerkugel an", "",
        "§4Immun gegen Feuer-Schaden §1(1)", ""
    ), 2)

    fun weatherStaff1(): ItemStack = defaultCustomItem(Material.WOODEN_HOE, "§r§l§7Wetter-Stab", arrayListOf(
        "", "§fLeft-Click §1(20)§f:", "§7Beschwört an der Stelle einen Blitz", "",
        "§fRight-Click §1(30)§f:", "§7Schleudert die Gegner in dem Bereich hoch", "",
        "§7Immun gegen Blitze §1(1)", ""
    ), 3)

    fun chaosStaff1(): ItemStack = defaultCustomItem(Material.WOODEN_HOE, "§r§l§0Chaos-Stab", arrayListOf(
        "", "§fLeft-Click §1(75)§f:", "§8Verdirb alle Mobs in dem Bereich", "",
        "§fRight-Click §1(30)§f:", "§8Macht dich unsichtbar und schnell", "",
        "§8Immun gegen Wither-Effekt §1(1)", ""
    ), 4)

    fun waterStaff1(): ItemStack = defaultCustomItem(Material.WOODEN_HOE, "§r§l§1Wasser-Stab", arrayListOf(
        "", "§fLeft-Click in Luft §1(70)§f:", "§9Gibt dir ResitemStacktenz und erzeugt Wasser vor dir", "",
        "§fRight-Click §1(15)§f:", "§9Platziert wo du hinschaust Wasser #WaterMLG", "",
        "§9Kann nicht ertrinken §1(1)", ""
    ), 5)

    fun excalibur(): ItemStack {
        val itemStack = defaultCustomItem(Material.DIAMOND_SWORD, "Excalibur", arrayListOf())
        itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10000)
        return itemStack
    }

    fun knockBackStick(): ItemStack {
        val itemStack = defaultCustomItem(Material.STICK, "Knüppel der Vernichtung", arrayListOf())
        itemStack.addUnsafeEnchantment(Enchantment.KNOCKBACK, 10000)
        return itemStack
    }

    fun ironKatana(): ItemStack {
        val itemStack = defaultCustomItem(Material.IRON_SWORD, "§7§l§oKatana", arrayListOf(), 1)
        itemStack.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 4)
        return itemStack
    }

    fun diaKatana(): ItemStack {
        val itemStack = defaultCustomItem(Material.IRON_SWORD, "§b§l§oKatana", arrayListOf(), 1)
        itemStack.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 4)
        return itemStack
    }

    fun netherKatana(): ItemStack {
        val itemStack = defaultCustomItem(Material.IRON_SWORD, "§8§l§oKatana", arrayListOf(), 1)
        itemStack.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 4)
        return itemStack
    }

    fun bitcoin(): ItemStack = defaultCustomItem(Material.STICK, "§eBitcoin", arrayListOf(), 1)

    fun ethereum(): ItemStack = defaultCustomItem(Material.STICK, "§7Ethereum", arrayListOf(), 2)

    fun litecoin(): ItemStack = defaultCustomItem(Material.STICK, "§1Litecoin", arrayListOf(), 3)

    fun dogecoin(): ItemStack = defaultCustomItem(Material.STICK, "§eDogecoin", arrayListOf(), 4)

    fun nano(): ItemStack = defaultCustomItem(Material.STICK, "§bNano", arrayListOf(), 5)

    fun throwableFireBall(): ItemStack = defaultCustomItem(Material.FIRE_CHARGE, "§6Fireball", arrayListOf("§7Rechtsclick zum Schießen"), 0,
    Pair("rlgItemData", "throwFireball"))

    fun mudBall(): ItemStack = defaultCustomItem(Material.SNOWBALL, "§6Mudball", arrayListOf("§7Rechtsclick zum Werfen", "", "§7Verlangsamt Gegner"), 1,
    Pair("rlgItemData", "mudBall"))

    private fun bookCustomItem(title: String, author: String, pages: Array<String>, cmd: Int=0): ItemStack {
        val itemStack = ItemStack(Material.WRITTEN_BOOK)
        val bm = itemStack.itemMeta as BookMeta
        bm.displayName(Component.text(title))
        bm.setCustomModelData(cmd)
        bm.author(Component.text(author))
        bm.title(Component.text(title))
        val componentArray = ArrayList<Component>()
        pages.forEach {
            componentArray.add(Component.text(it))
        }
        bm.pages(componentArray)
        itemStack.itemMeta = bm
        return itemStack
    }

    fun magicBook(): ItemStack = bookCustomItem("Basiswissen Magie", "Magieorden", basicmagicbook, 1)

    fun beginnerBook(): ItemStack = bookCustomItem("Beginner-Guide", "Server Team", beginnerbook, 2)

    fun shopBook(): ItemStack = bookCustomItem("Shop-Book", "Server Team", shopbook, 3)

}
