package br.com.ascend.aetheris

import androidx.room.*

@Dao
interface GameDao {
    @Query("SELECT * FROM rest_days ORDER BY date") suspend fun restDays():List<RestDay>
    @Upsert suspend fun restDay(v:RestDay)
    @Query("DELETE FROM rest_days WHERE date=:date") suspend fun deleteRestDay(date:String)
    @Query("DELETE FROM rest_days") suspend fun clearRestDays()
    @Query("SELECT * FROM player LIMIT 1") suspend fun player():Player?
    @Query("SELECT * FROM habits ORDER BY position") suspend fun habits():List<Habit>
    @Query("SELECT * FROM records ORDER BY date DESC") suspend fun records():List<HabitRecord>
    @Query("SELECT * FROM ledger") suspend fun ledger():List<Reward>
    @Query("SELECT * FROM quests") suspend fun quests():List<Quest>
    @Query("SELECT * FROM claims") suspend fun claims():List<Claim>
    @Query("SELECT * FROM assets") suspend fun assets():List<Asset>
    @Query("SELECT * FROM achievements") suspend fun achievements():List<Unlocked>
    @Query("SELECT * FROM diary ORDER BY date DESC") suspend fun journal():List<Journal>
    @Query("SELECT * FROM routines") suspend fun routines():List<Routine>
    @Query("SELECT * FROM focus LIMIT 1") suspend fun focus():Focus?
    @Upsert suspend fun player(v:Player)
    @Upsert suspend fun habit(v:Habit)
    @Upsert suspend fun record(v:HabitRecord)
    @Upsert suspend fun reward(v:Reward)
    @Upsert suspend fun quest(v:Quest)
    @Upsert suspend fun claim(v:Claim)
    @Upsert suspend fun asset(v:Asset)
    @Upsert suspend fun unlock(v:Unlocked)
    @Upsert suspend fun journal(v:Journal)
    @Upsert suspend fun routine(v:Routine)
    @Upsert suspend fun focus(v:Focus)
    @Query("DELETE FROM focus") suspend fun clearFocus()
    @Query("DELETE FROM diary WHERE id=:id") suspend fun deleteJournal(id:String)
    @Query("DELETE FROM routines WHERE id=:id") suspend fun deleteRoutine(id:String)
    @Query("DELETE FROM player") suspend fun clearPlayer()
    @Query("DELETE FROM records") suspend fun clearRecords()
    @Query("DELETE FROM habits") suspend fun clearHabits()
    @Query("DELETE FROM ledger") suspend fun clearLedger()
    @Query("DELETE FROM quests") suspend fun clearQuests()
    @Query("DELETE FROM claims") suspend fun clearClaims()
    @Query("DELETE FROM assets") suspend fun clearAssets()
    @Query("DELETE FROM achievements") suspend fun clearAchievements()
    @Query("DELETE FROM diary") suspend fun clearJournal()
    @Query("DELETE FROM routines") suspend fun clearRoutines()
}
val MIGRATION_1_2 = object : androidx.room.migration.Migration(1,2) {
    override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `rest_days` (`date` TEXT NOT NULL, PRIMARY KEY(`date`))")
    }
}
val MIGRATION_2_3 = object : androidx.room.migration.Migration(2,3) {
    override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE habits ADD COLUMN templateId TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE habits ADD COLUMN narrativeName TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE habits ADD COLUMN trackStreak INTEGER NOT NULL DEFAULT 1")
    }
}
@Database(entities=[Player::class,Habit::class,HabitRecord::class,Reward::class,Quest::class,Claim::class,Asset::class,Unlocked::class,Journal::class,Routine::class,Focus::class,RestDay::class],version=5,exportSchema=true)
abstract class GameDatabase:RoomDatabase(){abstract fun dao():GameDao}

val MIGRATION_3_4 = object : androidx.room.migration.Migration(3,4) {
    override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase) {
        listOf("quests","claims").forEach { table ->
            db.execSQL("ALTER TABLE $table ADD COLUMN sagaStart TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $table ADD COLUMN sagaBaseline TEXT NOT NULL DEFAULT ''")
        }
    }
}

val MIGRATION_4_5 = object : androidx.room.migration.Migration(4,5) {
 override fun migrate(db:androidx.sqlite.db.SupportSQLiteDatabase){
  db.execSQL("ALTER TABLE quests ADD COLUMN sagaState INTEGER NOT NULL DEFAULT 0")
  db.execSQL("ALTER TABLE quests ADD COLUMN sagaEnd TEXT NOT NULL DEFAULT ''")
  db.execSQL("ALTER TABLE quests ADD COLUMN sagaStopped INTEGER NOT NULL DEFAULT 0")
  db.execSQL("ALTER TABLE assets ADD COLUMN memorySpeaker TEXT NOT NULL DEFAULT ''")
  db.execSQL("ALTER TABLE assets ADD COLUMN memoryText TEXT NOT NULL DEFAULT ''")
 }
}
