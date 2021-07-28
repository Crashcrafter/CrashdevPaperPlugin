package de.rlg.permission

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.rlg.*
import de.rlg.player.RLGPlayer
import java.io.File

fun getRankByString(rankString: String?): Rank? {
    ranks.values.forEach {
        if(it.name == rankString){
            return it
        }
    }
    return null
}

fun RLGPlayer.rankData(): Rank = ranks[this.rank]!!

val ranks = hashMapOf<Int, Rank>()
data class Rank(val id: Int, val prefix: String, val name: String, val claims: Int, val homes: Int, val shopMultiplier: Double, val isMod: Boolean,
                      val isAdmin: Boolean, val perms: HashMap<String, Boolean>, val permLevel: Int, val weeklyKeys: HashMap<Int, Int>)

fun loadRanks(){
    val file = File(INSTANCE.dataFolder.path + "/ranks.json")
    if(file.exists()){
        val rankList = jacksonObjectMapper().readValue<List<Rank>>(file)
        rankList.forEach {
            ranks[it.id] = it
        }
    }else {
        file.createNewFile()
        jacksonObjectMapper().writeValue(file, mutableListOf<Rank>())
    }
}