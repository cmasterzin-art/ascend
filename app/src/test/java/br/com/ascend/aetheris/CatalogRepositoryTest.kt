package br.com.ascend.aetheris

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import com.google.gson.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.security.MessageDigest

/** Builds the actual exported legacy schema, rather than relabeling a current database. */
fun legacyDatabase(context:Context,name:String,version:Int,state:GameState){
    context.deleteDatabase(name);context.getDatabasePath(name).parentFile!!.mkdirs()
    val schema=JsonParser.parseString(java.io.File("schemas/br.com.ascend.aetheris.GameDatabase/$version.json").readText()).asJsonObject["database"].asJsonObject
    val tables=mapOf("player" to listOfNotNull(state.player),"habits" to state.habits,"records" to state.records,"ledger" to state.ledger,"quests" to state.quests,"claims" to state.claims,"assets" to state.assets,"achievements" to state.achievements,"diary" to state.journal,"routines" to state.routines,"focus" to listOfNotNull(state.focus),"rest_days" to state.restDays,"plan_exceptions" to state.plans,"focus_sessions" to state.focusSessions,"paths" to state.paths,"path_stages" to state.pathStages,"path_tasks" to state.pathTasks,"path_subtasks" to state.pathSubtasks,"path_habit_links" to state.pathHabitLinks,"time_blocks" to state.timeBlocks,"agenda_events" to state.agendaEvents,"inbox_items" to state.inboxItems,"knowledge_notebooks" to state.notebooks,"knowledge_pages" to state.knowledgePages,"knowledge_page_links" to state.knowledgeLinks,"knowledge_references" to state.knowledgeReferences,"flashcards" to state.flashcards,"flashcard_reviews" to state.flashcardReviews,"equipment_loadouts" to state.loadouts,"heraldry_profile" to listOfNotNull(state.heraldry))
    SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(name),null).use{db->
        schema["entities"].asJsonArray.forEach{element->val e=element.asJsonObject;val table=e["tableName"].asString
            db.execSQL(e["createSql"].asString.replace("\${TABLE_NAME}",table))
            e["indices"].asJsonArray.forEach{db.execSQL(it.asJsonObject["createSql"].asString.replace("\${TABLE_NAME}",table))}
            tables[table].orEmpty().forEach{row->val obj=Gson().toJsonTree(row).asJsonObject;val values=ContentValues()
                e["fields"].asJsonArray.forEach{field->val f=field.asJsonObject;val key=f["columnName"].asString;val v=obj[f["fieldPath"].asString]
                    if(v==null||v.isJsonNull)values.putNull(key) else if(v.asJsonPrimitive.isBoolean)values.put(key,if(v.asBoolean)1 else 0) else if(v.asJsonPrimitive.isNumber)values.put(key,v.asLong) else values.put(key,v.asString)
                };db.insertOrThrow(table,null,values)
            }
        }
        schema["setupQueries"].asJsonArray.forEach{db.execSQL(it.asString)};db.version=version
    }
}
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class CatalogRepositoryTest {
    private val context get()=RuntimeEnvironment.getApplication() as AscendApp
    private lateinit var db:GameDatabase;private lateinit var repo:GameRepository
    @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(context,GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Astrid"),emptyList())}
    @After fun close(){db.close()}
    @Test fun addCustomizeEditRewardSnapshotAndReload()=runBlocking{
        val h=habitTemplates.first().instantiate().copy(name="Meu estudo",xp=73,target=15,days="1,2,3,4,5,6,7",category="Minha categoria",coins=9,points=8)
        repo.addCatalogHabit(h);assertEquals(0L,repo.state.value!!.xp);assertEquals(0L,repo.state.value!!.coins)
        repo.progress(h,today(),15);assertEquals(73L,repo.state.value!!.xp)
        repo.habit(repo.state.value!!.habits.single().copy(xp=99,name="Estudo revisado"))
        assertEquals(73,repo.state.value!!.records.single().xp)
        val fresh=GameRepository(db);fresh.refresh();assertEquals(99,fresh.state.value!!.habits.single().xp);assertEquals("Os Tomos da Justiça",fresh.state.value!!.habits.single().narrativeName)
        repo.importState(Backup.decode(Backup.encode(fresh.state.value!!)));assertEquals(fresh.state.value,repo.state.value)
    }
    @Test fun concurrentAddsAndRenamingCannotDuplicateTemplate()=runBlocking{
        val t=habitTemplates.first()
        val result=coroutineScope{(1..8).map{async{runCatching{repo.addCatalogHabit(t.instantiate())}}}.awaitAll()}
        assertEquals(1,result.count{it.isSuccess});assertEquals(1,repo.state.value!!.habits.size)
        repo.habit(repo.state.value!!.habits.single().copy(name="Outro nome",archived=true))
        assertTrue(runCatching{repo.addCatalogHabit(t.instantiate())}.isFailure)
        assertEquals(0L,repo.state.value!!.xp)
    }
    @Test fun manualHabitsAreReusedAndRoutinesAreAtomicAndIdempotent()=runBlocking{
        val r=routineTemplates.first();val manual=r.choices.first().instantiate().copy(templateId="",xp=83)
        repo.habit(manual)
        val chosen=r.choices.take(3).map{it.instantiate()}
        repo.addRoutineTemplate(r.id,chosen);repo.addRoutineTemplate(r.id,chosen)
        assertEquals(3,repo.state.value!!.habits.size);assertEquals(1,repo.state.value!!.routines.size);assertEquals(83,repo.state.value!!.habits.first{it.id==manual.id}.xp)
        assertEquals(0L,repo.state.value!!.xp);assertEquals(0L,repo.state.value!!.coins)
        val before=repo.state.value
        assertTrue(runCatching{repo.addRoutineTemplate("knowledge",routineTemplates[1].choices.map{it.instantiate()}.mapIndexed{i,h->if(i==3)h.copy(xp=-1) else h})}.isFailure)
        assertEquals(before,repo.state.value)
        repo.addRoutineTemplate(r.id,listOf(r.choices.last().instantiate()));assertEquals(4,repo.state.value!!.routines.single().habitIds.split(',').size)
    }
    @Test fun oldBackupWithoutMetadataUsesSafeDefaults()=runBlocking{
        repo.habit(Habit(name="Antigo"));val gson=Gson();val payload=gson.toJsonTree(repo.state.value).asJsonObject
        payload["habits"].asJsonArray.forEach{val h=it.asJsonObject;h.remove("templateId");h.remove("narrativeName");h.remove("trackStreak")}
        val data=gson.toJson(payload);val hash=MessageDigest.getInstance("SHA-256").digest(data.toByteArray()).joinToString(""){"%02x".format(it)}
        val old=Backup.decode(gson.toJson(BackupEnvelope(version=3,sha256=hash,payload=data)))
        assertTrue(old.habits.single().trackStreak);assertEquals("",old.habits.single().templateId);repo.importState(old)
        assertEquals("Antigo",repo.state.value!!.habits.single().name)
    }
    @Test fun migrateActualV2PreservesAllProgressAndAllowsCatalog()=runBlocking{
        val h=Habit(name="Anterior",xp=500,coins=300);repo.habit(h);repo.progress(h,today(),1);repo.buy(shopItems.first{it.id=="cloak"});repo.rest(today(),true);repo.journal(Journal(text="Minha memória"));repo.routine(Routine(name="Minha rotina",habitIds=h.id));repo.restore(0)
        val before=repo.state.value!!;val name="migration-catalog.db";legacyDatabase(context,name,2,before)
        val disk=Room.databaseBuilder(context,GameDatabase::class.java,name).addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10,MIGRATION_10_11).build()
        try{val migrated=GameRepository(disk);migrated.refresh();assertEquals(before,migrated.state.value);migrated.addCatalogHabit(habitTemplates.first().instantiate());assertEquals(before.xp,migrated.state.value!!.xp);assertEquals(before.coins,migrated.state.value!!.coins)}finally{disk.close();context.deleteDatabase(name)}
    }
    @Test fun disabledStreakDoesNotRemoveHabitRewards()=runBlocking{
        val h=habitTemplates.first{it.id=="rest_break"}.instantiate();repo.addCatalogHabit(h);repo.progress(h,today(),h.target)
        assertEquals(h.xp.toLong(),repo.state.value!!.xp);assertEquals(0,streak(h,repo.state.value!!.records));assertEquals(0,bestStreak(h,repo.state.value!!.records))
    }
}
