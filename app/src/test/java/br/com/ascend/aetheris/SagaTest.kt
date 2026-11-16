package br.com.ascend.aetheris

import androidx.room.Room
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class SagaTest{
 private lateinit var db:GameDatabase;private lateinit var repo:GameRepository
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Ivar"),emptyList())}
 @After fun close(){db.close()}
 private suspend fun begin(h:Habit,metric:Int=1,target:Int=40):Quest{val q=Quest(name="Meu juramento",habitIds=h.id,metric=metric,target=target,xp=71,coins=9);repo.saga(q);return repo.state.value!!.quests.first{it.id==q.id}}
 @Test fun baselineMilestonesCorrectionsAndUniqueClaim()=runBlocking{
 val h=Habit(name="Estudo",type=2,target=100,xp=0);repo.habit(h);repo.progress(h,today(),20)
 val q=begin(h);assertEquals(0L,questProgress(q,repo.state.value!!))
 for((v,m) in listOf(30 to 1,40 to 2,50 to 3,60 to 4)){repo.progress(h,today(),v);assertEquals(m,sagaMilestone(q,repo.state.value!!))}
 coroutineScope{val outcomes=(1..6).map{async{runCatching{repo.claim(q)}}}.awaitAll();assertEquals(1,outcomes.count{it.isSuccess})}
 assertEquals(71L,repo.state.value!!.xp)
 repo.progress(h,today(),35);assertEquals(15L,questProgress(q,repo.state.value!!));assertEquals(0L,repo.state.value!!.xp)
 repo.progress(h,today(),0);assertEquals(0L,questProgress(q,repo.state.value!!))
 repo.progress(h,today(),60);assertEquals(71L,repo.state.value!!.xp);assertEquals(1,repo.state.value!!.claims.count{it.questId==q.id})
 assertTrue(runCatching{repo.saga(q.copy(target=1))}.isFailure)
 }
 @Test fun completionAndPageMetricsExcludePriorRecords()=runBlocking{
 val h=Habit(name="Livro",type=1,target=50,unit="páginas",xp=0);repo.habit(h);repo.progress(h,today(),25)
 val q=begin(h,3,25);val c=begin(h,0,1);repo.progress(h,today(),50)
 assertEquals(25L,questProgress(q,repo.state.value!!));assertEquals(1L,questProgress(c,repo.state.value!!))
 repo.progress(h,today(),0);assertEquals(0L,questProgress(c,repo.state.value!!))
 }
 @Test fun persistenceBackupAndFrozenTerms()=runBlocking{
 val h=Habit(name="Foco",type=2,target=60,xp=0);repo.habit(h);val q=begin(h);repo.progress(h,today(),30)
 assertTrue(runCatching{repo.saga(q.copy(habitIds="missing"))}.isFailure)
 repo.saga(q.copy(name="Outro título",xp=88));val fresh=GameRepository(db);fresh.refresh();assertEquals(30L,questProgress(q,fresh.state.value!!))
 val decoded=Backup.decode(Backup.encode(fresh.state.value!!));repo.importState(decoded);assertEquals(decoded,repo.state.value)
 }
 @Test fun realV3MigrationPreservesEntireState()=runBlocking{
 val h=Habit(name="Antigo");repo.habit(h);repo.progress(h,today(),1);val old=repo.state.value!!
 val ctx=(RuntimeEnvironment.getApplication() as AscendApp);val name="north-migration.db";legacyDatabase(ctx,name,3,old)
 val disk=Room.databaseBuilder(ctx,GameDatabase::class.java,name).addMigrations(MIGRATION_3_4,MIGRATION_4_5).build()
 try{val migrated=GameRepository(disk);migrated.refresh();assertEquals(old,migrated.state.value);migrated.saga(Quest(name="Nova",habitIds=h.id));assertEquals(old.xp,migrated.state.value!!.xp)}finally{disk.close();ctx.deleteDatabase(name)}
 }
 @Test fun consolidatedRewardsUseActualDeltasAndNarrativesAreAcknowledged()=runBlocking{
 val h=Habit(name="Primeiro",xp=150,coins=8,points=7);repo.habit(h);val old=repo.state.value!!;repo.progress(h,today(),1);val next=repo.state.value!!
 val e=rewardEvent("Feito",old,next);assertEquals(150L,e.xp);assertEquals(next.coins-old.coins,e.coins);assertEquals(7L,e.points[0]);assertEquals(2,e.level)
 assertEquals(1,e.highlights.count{it.startsWith("Nível")});assertTrue(narrativeCandidates(old).isEmpty())
 val line=nextNarrative(next)!!;repo.rememberNarrative(line.id);assertNotEquals(line.id,nextNarrative(repo.state.value!!)?.id)
 val again=rewardEvent("Repetido",next,next);assertEquals(0L,again.xp);assertTrue(again.highlights.isEmpty())
 assertEquals(9,chapterTexts.size);assertTrue(chapterUnlocked(0,next));assertFalse(chapterUnlocked(8,next))
 }
 @Test fun legacyV4BackupWithoutSagaFieldsRemainsReadable()=runBlocking{
 val h=Habit(name="Antigo");repo.habit(h);repo.progress(h,today(),1)
 val q=Quest(name="Contrato antigo",target=1);repo.quest(q);repo.claim(q)
 val gson=com.google.gson.Gson();val payload=gson.toJsonTree(repo.state.value).asJsonObject
 listOf("quests","claims").forEach{table->payload[table].asJsonArray.forEach{row->row.asJsonObject.remove("sagaStart");row.asJsonObject.remove("sagaBaseline")}}
 val data=gson.toJson(payload);val digest=java.security.MessageDigest.getInstance("SHA-256").digest(data.toByteArray()).joinToString(""){"%02x".format(it)}
 val restored=Backup.decode(gson.toJson(BackupEnvelope(version=4,sha256=digest,payload=data)))
 assertTrue(restored.quests.all{it.sagaStart.isBlank()});assertTrue(restored.claims.all{it.sagaBaseline.isBlank()});repo.importState(restored);assertEquals(restored,repo.state.value)
 }

}
