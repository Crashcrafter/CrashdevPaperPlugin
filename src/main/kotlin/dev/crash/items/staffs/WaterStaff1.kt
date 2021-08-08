package dev.crash.items.staffs

import dev.crash.permission.isClaimed
import dev.crash.player.crashPlayer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object WaterStaff1 {
    private var spell1Cooldown = HashMap<Player, Long>()
    private var spell2Cooldown = HashMap<Player, Long>()

    fun handleClick(e: PlayerInteractEvent) {
        val player = e.player
        val crashPlayer = player.crashPlayer()
        val action = e.action
        val mana: Int = crashPlayer.mana
        when (action) {
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> try {
                if (mana >= 15) {
                    if (spell1Cooldown.containsKey(player) && spell1Cooldown[player]!! <= System.currentTimeMillis() || !spell1Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell1Cooldown.remove(player)
                        val result = player.rayTraceBlocks(15.0)!!
                        val block = result.hitBlock!!
                        val block1 = block.getRelative(Objects.requireNonNull(result.hitBlockFace)!!)
                        if (!block.chunk.isClaimed() || player.isOp) {
                            crashPlayer.changeMana(20)
                            block1.type = Material.WATER
                            spell1Cooldown[player] = System.currentTimeMillis() + 1000 * 15
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
            }
            Action.LEFT_CLICK_AIR -> try {
                if (mana >= 70) {
                    if (spell2Cooldown.containsKey(player) && spell2Cooldown[player]!! <= System.currentTimeMillis() || !spell2Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell2Cooldown.remove(player)
                        val location = player.eyeLocation
                        val vector = location.direction
                        val blockLocation = location.add(vector.multiply(5))
                        val block = blockLocation.block
                        if (!block.chunk.isClaimed() || player.isOp) {
                            block.type = Material.WATER
                            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 200, 1))
                            player.addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, 200, 0))
                            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 100, 0))
                            spell2Cooldown[player] = System.currentTimeMillis() + 1000 * 60
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
            }
            else -> return
        }
    }
}