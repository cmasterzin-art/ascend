package br.com.ascend.aetheris

import androidx.room.Room
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class VoicesTest{
 private lateinit var db:GameDatabase;private lateinit var repo:GameRepository
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Eydis"),emptyList())}
 @After fun close(){db.close()}
 @Test fun weeklySummaryUsesCivilWeekAndCorrections(){
 val monday="2026-11-09";val h=Habit(id="h",name="Livro",created=monday,xp=40,coins=8)
 val record=HabitRecord("h@$monday","h",monday,1,1,40,8,5,0,false,0,"Leitura","vez","Livro")
 val s=GameState(player=Player(name="Eydis",created=monday),habits=listOf(h),records=listOf(record),ledger=listOf(Reward("habit:${record.id}",40,8,5,0,monday)))
 val a=weeklyChronicle(s,"2026-11-15","2026-11-16");assertEquals(monday,a.start);assertEquals("2026-11-15",a.end);assertEquals(7,a.summary.planned);assertEquals(1,a.summary.completed);assertEquals(14,a.summary.rate);assertEquals(8L,a.coins);assertEquals(5L,a.points[0])
 val b=weeklyChronicle(s.copy(records=listOf(record.copy(progress=0)),ledger=emptyList()),"2026-11-15","2026-11-16");assertEquals(0,b.summary.completed);assertEquals(0L,b.summary.xp);assertTrue(b.narrative.contains("silêncio"))
 assertEquals("2026-11-16",weeklyChronicle(s,"2026-11-16","2026-11-16").start)
 }
 @Test fun abandonedSagaFreezesAndCannotPayThenRestartsFresh()=runBlocking{
 val h=Habit(name="Foco",type=2,target=60,xp=0);repo.habit(h);val q=Quest(name="Juramento",habitIds=h.id,metric=1,target=40);repo.saga(q);repo.progress(h,today(),20)
 repo.abandonSaga(q.id);val abandoned=repo.state.value!!.quests.first{it.id==q.id};assertEquals(1,abandoned.sagaState);assertEquals(20L,questProgress(abandoned,repo.state.value!!))
 repo.progress(h,today(),60);assertEquals(20L,questProgress(abandoned,repo.state.value!!));assertTrue(runCatching{repo.claim(abandoned)}.isFailure)
 repo.archiveSaga(q.id);assertEquals(3,repo.state.value!!.quests.first{it.id==q.id}.sagaState)
 repo.restartSaga(q.id);val attempt=repo.state.value!!.quests.first{it.id!=q.id&&it.sagaStart.isNotBlank()};assertNotEquals(q.id,attempt.id);assertEquals(0L,questProgress(attempt,repo.state.value!!));assertEquals(0L,repo.state.value!!.xp)
 assertTrue(runCatching{repo.restartSaga(q.id)}.isFailure)
 }
 @Test fun completedArchiveKeepsSingleClaimAndNewAttemptExcludesOldWork()=runBlocking{
 val h=Habit(name="Caminhar",xp=0);repo.habit(h);val q=Quest(name="Trilha",target=1,habitIds=h.id,xp=27);repo.saga(q);repo.progress(h,today(),1);val saved=repo.state.value!!.quests.first{it.id==q.id};repo.claim(saved);val xp=repo.state.value!!.xp
 repo.archiveSaga(q.id);val archived=repo.state.value!!.quests.first{it.id==q.id};assertEquals(2,archived.sagaState);assertTrue(runCatching{repo.claim(archived)}.isFailure);assertTrue(runCatching{repo.abandonSaga(q.id)}.isFailure)
 repo.restartSaga(q.id);val attempt=repo.state.value!!.quests.first{it.id!=q.id&&it.sagaStart.isNotBlank()};assertEquals(0L,questProgress(attempt,repo.state.value!!));assertTrue(runCatching{repo.claim(attempt)}.isFailure);assertEquals(xp,repo.state.value!!.xp)
 repo.importState(Backup.decode(Backup.encode(repo.state.value!!)));assertEquals(2,repo.state.value!!.quests.first{it.id==q.id}.sagaState)
 }
 @Test fun recognizedConversationsAreSnapshotsAndMemoriesDoNotPay()=runBlocking{
 val h=Habit(name="Primeira",xp=0);repo.habit(h);repo.progress(h,today(),1);val line=nextNarrative(repo.state.value!!)!!;val before=repo.state.value!!.xp;repo.rememberNarrative(line.id)
 val saved=repo.state.value!!;assertTrue(hallMemories(saved).any{it.detail==line.text});assertEquals(before,repo.state.value!!.xp)
 repo.progress(h,today(),0);assertTrue(hallMemories(repo.state.value!!).any{it.detail==line.text})
 val backup=Backup.decode(Backup.encode(repo.state.value!!));repo.importState(backup);assertTrue(hallMemories(repo.state.value!!).any{it.detail==line.text});assertTrue(hallMemories(GameState(player=Player(name="Antigo"),ledger=listOf(Reward("xp",xp=500)))).any{it.id=="region:1"&&it.date==null})
 }
 @Test fun realV4MigrationAndLegacyBackupPreserveProgress()=runBlocking{
 val h=Habit(name="Antigo");repo.habit(h);val q=Quest(name="Saga antiga",habitIds=h.id);repo.saga(q);val old=repo.state.value!!
 val ctx=RuntimeEnvironment.getApplication() as AscendApp;val name="voices-migration.db";legacyDatabase(ctx,name,4,old)
 val disk=Room.databaseBuilder(ctx,GameDatabase::class.java,name).addMigrations(MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9).build()
 try{val migrated=GameRepository(disk);migrated.refresh();assertEquals(old,migrated.state.value);migrated.abandonSaga(q.id);assertEquals(1,migrated.state.value!!.quests.first{it.id==q.id}.sagaState)}finally{disk.close();ctx.deleteDatabase(name)}
 val gson=com.google.gson.Gson();val payload=gson.toJsonTree(old).asJsonObject;payload["quests"].asJsonArray.forEach{r->listOf("sagaState","sagaEnd","sagaStopped").forEach{r.asJsonObject.remove(it)}}
 val data=gson.toJson(payload);val digest=java.security.MessageDigest.getInstance("SHA-256").digest(data.toByteArray()).joinToString(""){"%02x".format(it)}
 assertEquals(old,Backup.decode(gson.toJson(BackupEnvelope(version=5,sha256=digest,payload=data))))
 }
 @Test fun soundPolicyAndLocalPreferences()=runBlocking{
 assertFalse(effectsAllowed(false,2,5));assertFalse(effectsAllowed(true,0,5));assertFalse(effectsAllowed(true,1,5));assertFalse(effectsAllowed(true,2,0));assertTrue(effectsAllowed(true,2,5))
 assertEquals("iron",effectFor(GameEvent("Item equipado")));assertEquals("bell",effectFor(GameEvent("Feito",level=2)));assertNull(effectFor(GameEvent("Saga abandonada")))
 val ctx=RuntimeEnvironment.getApplication() as AscendApp;ctx.preferences.edit{it[booleanPreferencesKey("sound_enabled")]=true;it[floatPreferencesKey("sound_volume")]=.2f};val prefs=ctx.preferences.data.first();assertTrue(prefs[booleanPreferencesKey("sound_enabled")]!!);assertEquals(.2f,prefs[floatPreferencesKey("sound_volume")]!!)
 }
 @Test fun weeklySagasAndRestDaysUseRecordedDates(){
 val h=Habit(id="h",name="Estudo",created="2026-11-09",type=2,target=60)
 fun record(date:String,n:Int)=HabitRecord("h@$date","h",date,n,60,0,0,0,0,false,2,"Estudo","minutos","Estudo")
 val q=Quest(id="s",name="Saga",habitIds="h",metric=1,target=90,sagaStart="2026-11-09")
 val c=Claim("s@once","s","once",date="2026-11-15",target=90,metric=1,habitIds="h",sagaStart="2026-11-09")
 val s=GameState(habits=listOf(h),records=listOf(record("2026-11-09",60),record("2026-11-11",30),record("2026-11-16",20)),quests=listOf(q),claims=listOf(c),restDays=listOf(RestDay("2026-11-10")))
 val w=weeklyChronicle(s,"2026-11-15","2026-11-16");assertEquals(6,w.summary.planned);assertEquals(listOf("Saga"),w.sagasAdvanced);assertEquals(listOf("Saga"),w.sagasCompleted);assertEquals(90L,sagaWeekDelta(q,s,w.start,w.end))
 val correction=s.copy(records=s.records.filter{it.date!="2026-11-11"});assertTrue(weeklyChronicle(correction,"2026-11-15","2026-11-16").sagasCompleted.isEmpty())
 assertEquals(0L,sagaWeekDelta(q,s,"2026-11-16","2026-11-16"));assertEquals(20L,sagaWeekDelta(q.copy(target=200),s,"2026-11-16","2026-11-16"))
 }

}
