package de.rlg.listener

import de.rlg.checkMessage
import de.rlg.setupShop1
import de.rlg.toStringList
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

class SignListener : Listener {

    @EventHandler
    fun onSign(e: SignChangeEvent) {
        val lines = e.lines().toStringList()
        for (line in lines) {
            if (checkMessage(line, e.player)) {
                e.isCancelled = true
            }
        }
        if (!e.isCancelled) {
            if (lines[0].contentEquals("[Shop]")) {
                val sign = e.block.state as Sign
                val chestBlock: Block? = getBlockBehindSign(sign)
                try {
                    if (chestBlock!!.type == Material.CHEST) {
                        for (i in 1 until lines.size) {
                            e.line(i, Component.text(""))
                        }
                        val chest = chestBlock.state as Chest
                        setupShop1(chest, sign, e.player)
                    }
                } catch (ignored: NullPointerException) { }
            }
        }
    }
}

fun getBlockBehindSign(sign: Sign): Block? {
    try {
        val signBlock = sign.block
        val signData = signBlock.state.blockData as WallSign
        val attached = signData.facing.oppositeFace
        return signBlock.getRelative(attached)
    } catch (ignored: ClassCastException) {
    }
    return null
}