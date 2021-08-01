package dev.crash

import dev.crash.permission.ranks
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.Instant
import java.time.LocalDate

internal fun initDatabase(){
    TransactionManager.defaultDatabase = Database.connect("jdbc:mysql://${LoginData.ip}/mcplugin", user = LoginData.user, password = LoginData.pw)
}

object ChunkTable : Table("chunks"){
    val x = integer("x")
    val z = integer("z")
    val world = varchar("world", 20)
    val uuid = varchar("uuid", 36)
    val name = varchar("name", 100)
    val shared = text("shared")
}

object KeyChestTable : Table("keychests"){
    val chestPos = text("chestPos")
    val type = integer("type")
}

object KeyIndexTable : Table("keyindex"){
    val token = varchar("token", 20)
    val type = integer("type")
    override val primaryKey = PrimaryKey(token)
}

object GuildTable : IntIdTable("guilds"){
    val suffix = varchar("suffix", 4)
    val name = varchar("name", 20)
    val owner_uuid = varchar("owner_uuid", 36)
    val owner_name = text("owner_name")
    val member_names = text("member_names")
    val member_uuids = text("member_uuids")
    val created = timestamp("created").default(Instant.now())
}

//Removed

object PlayersTable : Table("players"){
    val uuid = varchar("uuid", 36)
    val rank = integer("rank").default(0)
    val remainingClaims = integer("remainingclaims").default(ranks[0]!!.claims)
    val remainingHomes = integer("remaininghomes").default(ranks[0]!!.homes)
    val addedClaims = integer("addedClaims").default(0)
    val balance = long("balance").default(0)
    val questStatus = varchar("queststatus", 50).default("0 0 0 0 0 0 0 0")
    val quests = varchar("quests", 50).default("1 2 3 1 2 3")
    val questProgress = varchar("questprogress", 50).default("0 0 0 0 0 0")
    val lastDailyQuest = date("lastdailyquest").default(LocalDate.now())
    val lastWeeklyQuest = date("lastweeklyquest").default(LocalDate.now())
    val xpLevel = integer("xpLevel").default(0)
    val xp = long("xp").default(0)
    val vxpLevel = integer("vxpLevel").default(0)
    val guildId = integer("guildId").default(0)
    override val primaryKey = PrimaryKey(uuid)
}

object ProcessedTable : Table("processed"){
    val uuid = varchar("uuid", 36)
    val lastTime = date("lasttime")
    val leftKeys = varchar("leftkeys", 20)
    override val primaryKey = PrimaryKey(uuid)
}

object HomepointTable : Table("homepoints"){
    val uuid = varchar("uuid", 36)
    val keyword = varchar("keyword", 40)
    val homePos = text("homePos")
}