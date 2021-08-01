package dev.crash

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.timestamp
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.time.Instant

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

object PortalTable : Table("portals"){
    val portalPos = text("portalPos")
    val targetWorld = varchar("targetworld", 20)
}

object WarnTable : Table("warns"){
    val uuid = varchar("uuid", 36)
    val name = varchar("name", 100)
    val reason = text("reason")
    val modName = varchar("modname", 100)
    val time = timestamp("time")
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