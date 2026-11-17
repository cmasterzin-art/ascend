package br.com.ascend.aetheris

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.google.gson.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class Version11Test {
    private val context get()=RuntimeEnvironment.getApplication() as AscendApp
    private lateinit var db:GameDatabase
    private lateinit var repo:GameRepository
    private val h=Habit(id="study",name="Estudar",created=LocalDate.now().minusDays(10).toString(),xp=50)
    @Before fun prepare()=runBlocking{
        db=Room.inMemoryDatabaseBuilder(context,GameDatabase::class.java).build()
        repo=GameRepository(db);repo.refresh();repo.create(Player(name="Kael",created=h.created),listOf(h))
    }
    @After fun close(){db.close()}
    @Test fun restDaysArePersistentReversibleAndDoNotAlterRewards()=runBlocking{
        repo.progress(h,today(),1);val xp=repo.state.value!!.xp
        repo.rest(today(),true);repo.rest(today(),true)
        val restored=GameRepository(db);restored.refresh()
        assertEquals(1,restored.state.value!!.restDays.size)
        assertTrue(restored.state.value!!.due().isEmpty());assertEquals(xp,restored.state.value!!.xp)
        assertEquals(1,restored.state.value!!.records.size)
        repo.rest(today(),false);assertEquals(1,repo.state.value!!.due().size);assertEquals(xp,repo.state.value!!.xp)
    }
    @Test fun restDayBackupAndInvalidRestoreAreAtomic()=runBlocking{
        repo.rest(today(),true)
        val state=repo.state.value!!;val encoded=Backup.encode(state)
        assertEquals(state,Backup.decode(encoded))
        repo.rest(today(),false);repo.importState(Backup.decode(encoded));assertEquals(state,repo.state.value)
        assertTrue(runCatching{repo.importState(state.copy(restDays=listOf(RestDay("bad"))))}.isFailure)
        assertEquals(state,repo.state.value)
    }
    @Test fun versionOneBackupWithoutNewFieldsRemainsReadable()=runBlocking{
        repo.progress(h,today(),1)
        val gson=Gson();val payload=gson.toJsonTree(repo.state.value).asJsonObject
        payload.remove("restDays")
        val data=gson.toJson(payload)
        val hash=MessageDigest.getInstance("SHA-256").digest(data.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
        val old=gson.toJson(BackupEnvelope(version=1,sha256=hash,payload=data))
        val decoded=Backup.decode(old)
        assertTrue(decoded.restDays.isEmpty());assertEquals(50L,decoded.xp)
        repo.importState(decoded);assertEquals("Kael",repo.state.value!!.player!!.name)
    }
    @Test fun migrateVersionOneDatabasePreservesCharacterAndLedger()=runBlocking{
        val name="migration-v1.db";context.deleteDatabase(name)
        val fixture=Room.inMemoryDatabaseBuilder(context,GameDatabase::class.java).build()
        val first=GameRepository(fixture);first.refresh();first.create(Player(name="Kael",heroClass=1),listOf(h));first.progress(h,today(),1)
        val state=first.state.value!!;fixture.close();legacyDatabase(context,name,1,state)
        val disk=Room.databaseBuilder(context,GameDatabase::class.java,name).addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6).build()
        try{
            val migrated=GameRepository(disk);migrated.refresh()
            assertEquals("Kael",migrated.state.value!!.player!!.name);assertEquals(50L,migrated.state.value!!.xp)
            assertEquals(1,migrated.state.value!!.records.size);assertTrue(migrated.state.value!!.restDays.isEmpty())
            migrated.rest(today(),true);assertEquals(1,migrated.state.value!!.restDays.size)
        }finally{disk.close();context.deleteDatabase(name)}
    }
    @Test fun archivePauseDuplicateAndHistoricalScheduleRemainConsistent()=runBlocking{
        repo.progress(h,LocalDate.now().minusDays(1).toString(),1)
        repo.habit(h.copy(archived=true));assertTrue(repo.state.value!!.due().isEmpty())
        assertEquals(1,repo.state.value!!.due(LocalDate.now().minusDays(1).toString()).size)
        val duplicate=h.copy(id="copy",created=today(),xp=40)
        repo.habit(duplicate);repo.progress(duplicate,today(),1)
        assertEquals(90L,repo.state.value!!.xp)
        repo.habit(duplicate.copy(paused=true));assertTrue(repo.state.value!!.due().isEmpty())
    }
}
