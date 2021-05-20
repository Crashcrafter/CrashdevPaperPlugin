package de.rlg.items.staffs

import de.rlg.permission.isClaimed
import de.rlg.player.rlgPlayer
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object WeatherStaff1 {
    private var spell1Cooldown = HashMap<Player, Long>()
    private var spell2Cooldown = HashMap<Player, Long>()

    fun handleClick(e: PlayerInteractEvent) {
        val player = e.player
        val rlgPlayer = player.rlgPlayer()
        val action = e.action
        val mana: Int = rlgPlayer.mana
        when (action) {
            Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> try {
                if (mana >= 30) {
                    if (spell1Cooldown.containsKey(player) && spell1Cooldown[player]!! <= System.currentTimeMillis() || !spell1Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell1Cooldown.remove(player)
                        val block: Block?
                        try {
                            block = Objects.requireNonNull(player.rayTraceBlocks(25.0))!!.hitBlock
                            assert(block != null)
                        } catch (ignored: NullPointerException) {
                            return
                        }
                        if (!block!!.chunk.isClaimed() || player.isOp) {
                            rlgPlayer.changeMana(30)
                            val entities = block.location.getNearbyLivingEntities(5.0)
                            for (entity in entities) {
                                entity.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 5, 60))
                            }
                            spell1Cooldown[player] = System.currentTimeMillis() + 1000 * 10
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (ignored: NullPointerException) {
            }
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> try {
                if (mana >= 40) {
                    if (spell2Cooldown.containsKey(player) && spell2Cooldown[player]!! <= System.currentTimeMillis() || !spell2Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell2Cooldown.remove(player)
                        val block: Block?
                        try {
                            block = Objects.requireNonNull(player.rayTraceBlocks(25.0))!!.hitBlock
                            assert(block != null)
                        } catch (ignored: NullPointerException) {
                            return
                        }
                        if (!block!!.chunk.isClaimed() || player.isOp) {
                            rlgPlayer.changeMana(40)
                            val strike = block.world.strikeLightning(block.location)
                            strike.customName = "weather1"
                            block.world.playSound(block.location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 5f, 1f)
                            block.world.playSound(block.location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 5f, 1f)
                            spell2Cooldown[player] = System.currentTimeMillis() + 1000 * 25
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (i: NullPointerException) {
                i.printStackTrace()
            }
            else -> return
        }
    }
}