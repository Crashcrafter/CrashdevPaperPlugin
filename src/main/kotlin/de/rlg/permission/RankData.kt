package de.rlg.permission

val rankData = HashMap<Int, RankObj>()

data class RankObj(val prefix: String, val name: String, val claims: Int, val isMod: Boolean, val homes: Int, val shopMultiplier: Double)

fun getRankByString(rankString: String?): Int {
    for (i in 0 until rankData.size) {
        if (rankData[i]!!.name.contentEquals(rankString)) {
            return i
        }
    }
    return rankData.size + 1
}