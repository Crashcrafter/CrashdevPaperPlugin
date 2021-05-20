package de.rlg.items.staffs

import de.rlg.natureBlocks
import de.rlg.permission.isClaimed
import de.rlg.player.rlgPlayer
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object NatureStaff1 {
    private var spell1Cooldown = HashMap<Player, Long>()
    private var spell2Cooldown = HashMap<Player, Long>()

    fun handleClick(e: PlayerInteractEvent) {
        val player = e.player
        val action = e.action
        val rlgPlayer = player.rlgPlayer()
        val mana: Int = rlgPlayer.mana
        when (action) {
            Action.RIGHT_CLICK_BLOCK -> try {
                if (mana >= 15) {
                    if (spell1Cooldown.containsKey(player) && spell1Cooldown[player]!! <= System.currentTimeMillis() || !spell1Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell1Cooldown.remove(player)
                        val block = e.clickedBlock!!
                        if (!block.chunk.isClaimed() || player.isOp) {
                            rlgPlayer.changeMana(15)
                            var x = block.x - 3
                            while (x < block.x + 4) {
                                var z = block.z - 3
                                while (z < block.z + 4) {
                                    val block1 = player.world.getBlockAt(x, block.y, z)
                                    if (block1.type == Material.DIRT) {
                                        block1.type = Material.GRASS_BLOCK
                                    }
                                    block1.applyBoneMeal(BlockFace.UP)
                                    z++
                                }
                                x++
                            }
                            block.world.playSound(
                                block.location,
                                Sound.BLOCK_ENCHANTMENT_TABLE_USE,
                                SoundCategory.AMBIENT,
                                10f,
                                10f
                            )
                            spell1Cooldown[player] = System.currentTimeMillis() + 2500
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
                if (mana >= 30) {
                    if (spell2Cooldown.containsKey(player) && spell2Cooldown[player]!! <= System.currentTimeMillis() || !spell2Cooldown.containsKey(
                            player
                        )
                    ) {
                        spell2Cooldown.remove(player)
                        val block = Objects.requireNonNull(player.rayTraceBlocks(50.0))!!
                            .hitBlock!!
                        if (!block.chunk.isClaimed() || player.isOp) {
                            rlgPlayer.changeMana(30)
                            var x = block.x - 3
                            while (x < block.x + 4) {
                                var z = block.z - 3
                                while (z < block.z + 4) {
                                    var y = block.y - 2
                                    while (y < block.y + 3) {
                                        val block1 = player.world.getBlockAt(x, y, z)
                                        val m = block1.type
                                        if (m == Material.GRASS_BLOCK || m == Material.DIRT || m == Material.FARMLAND || m == Material.MYCELIUM) {
                                            block1.type = Material.COARSE_DIRT
                                        } else if (natureBlocks.contains(m)) {
                                            block1.type = Material.AIR
                                        }
                                        y++
                                    }
                                    z++
                                }
                                x++
                            }
                            val entities = block.world.getNearbyLivingEntities(
                                block.location, 5.0
                            )
                            for (entity in entities) {
                                entity.addPotionEffect(PotionEffect(PotionEffectType.POISON, 300, 1))
                            }
                            block.world.playSound(
                                block.location,
                                Sound.BLOCK_GRASS_BREAK,
                                SoundCategory.AMBIENT,
                                10f,
                                10f
                            )
                            spell2Cooldown[player] = System.currentTimeMillis() + 1000 * 20
                        } else {
                            player.sendMessage("§4Du kannst in geclaimten Chunks nicht Zaubern!")
                        }
                    }
                } else {
                    player.sendMessage("§4Du hast nicht genügend Mana, um diesen Zauber zu wirken!")
                }
            } catch (e1: NullPointerException) {
                e1.printStackTrace()
            }
            else -> {
            }
        }
    }
}