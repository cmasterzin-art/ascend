package br.com.ascend.aetheris

import androidx.room.Room
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class ExplorationTest{
 private lateinit var db:GameDatabase;private lateinit var repo:GameRepository
 private var days=0
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Nara",created=LocalDate.now().minusDays(30).toString()),emptyList());val h=Habit(id="gate",name="Chegar à floresta",created=LocalDate.now().minusDays(30).toString(),xp=250);repo.habit(h);repo.progress(h,today(),1)}
 @After fun close(){db.close()}
 private suspend fun habits():List<Habit>{return (1..8).map{Habit(id="h$it",name="Ritual $it",xp=0).also{h->repo.habit(h)}}}
 private fun current(e:ExpeditionDef)=repo.state.value!!.quests.first{it.id==expeditionQuestId(e,expeditionPhaseIndex(repo.state.value!!,e))}
 private suspend fun satisfy(e:ExpeditionDef,hs:List<Habit>){val q=current(e);hs.filter{(parseBaseline(q.sagaBaseline)["${it.id}@${today()}"]?:0)==0&&repo.state.value!!.record(it.id)?.complete!=true}.take(q.target).forEach{repo.progress(it,today(),1)}}
 @Test fun originalRegionsAndLevelGateArePreserved(){assertEquals(listOf(1,3,5,8,12,20),regionLevels);assertEquals(6,regions.size);val e=forestExpeditions.first();assertFalse(expeditionAvailable(GameState(ledger=listOf(Reward("xp",xp=249))),e));assertTrue(expeditionAvailable(GameState(ledger=listOf(Reward("xp",xp=250))),e));assertEquals(0,forestProgress(repo.state.value!!));assertFalse(expeditionAvailable(repo.state.value!!,forestExpeditions[1]))}
 @Test fun snapshotCorrectsRecordsAndRewardIsUnique()=runBlocking{
 val hs=habits();repo.progress(hs[0],today(),1);val e=forestExpeditions.first();repo.startExpedition(e.id,hs.map{it.id});assertEquals(0L,questProgress(current(e),repo.state.value!!));satisfy(e,hs)
 val before=repo.state.value!!.xp;val results=coroutineScope{(1..8).map{async{runCatching{repo.advanceExpedition(e.id)}}}.awaitAll()};assertEquals(1,results.count{it.isSuccess});assertEquals(before+40,repo.state.value!!.xp);assertEquals(1,forestStage(repo.state.value!!));assertTrue(expeditionComplete(repo.state.value!!,e))
 repo.progress(hs[1],today(),0);assertFalse(expeditionComplete(repo.state.value!!,e));assertEquals(before,repo.state.value!!.xp)
 repo.progress(hs[1],today(),1);assertEquals(before+40,repo.state.value!!.xp);assertEquals(1,repo.state.value!!.claims.count{it.questId==expeditionQuestId(e,0)})
 val old=current(e);assertTrue(runCatching{repo.saga(old.copy(xp=99999))}.isFailure);assertTrue(runCatching{repo.claim(old)}.isFailure);assertTrue(runCatching{repo.restartSaga(old.id)}.isFailure)
 }
 @Test fun threeExpeditionsAndBossPhasesHaveDistinctBaselinesAndConsequences()=runBlocking{
 // New independent habits are performed per stage; no stage consumes the preceding activity again.
 var counter=0
 for(e in forestExpeditions){val first=(1..e.phases.sumOf{it.target}).map{Habit(id="stage${counter++}",name="Atividade $counter",xp=0).also{repo.habit(it)}};repo.startExpedition(e.id,first.map{it.id})
 for(n in e.phases.indices){val q=current(e);assertEquals(n,expeditionPhaseIndex(repo.state.value!!,e));assertEquals(0L,questProgress(q,repo.state.value!!));satisfy(e,first);repo.advanceExpedition(e.id);if(e.id=="sanctuary")assertEquals(if(n==2)2 else 1,sanctuaryStage(repo.state.value!!))}
 assertTrue(expeditionComplete(repo.state.value!!,e))
 }
 val s=repo.state.value!!;assertEquals(100,forestProgress(s));assertEquals(2,forestStage(s));assertTrue(s.assets.any{it.id=="mist-clasp"});assertTrue(s.achievements.any{it.id=="forest"});assertTrue(specializationTitles(s).contains("Testemunha das Raízes"));assertEquals(7,s.claims.count{expeditionRef(it.questId)!=null});assertTrue(hallMemories(s).any{it.category=="Mundo"&&it.detail.contains("Floresta da Persistência")})
 val fresh=GameRepository(db);fresh.refresh();assertEquals(s,fresh.state.value);repo.importState(Backup.decode(Backup.encode(s)));assertEquals(s,repo.state.value)
 assertTrue(runCatching{repo.buy(shopItems.first{it.id=="mist-clasp"})}.isFailure)
 }
 @Test fun sagaAndExpeditionShareActivityButHabitRewardIsSingle()=runBlocking{
 val hs=(1..5).map{Habit(name="Hábito $it",xp=10).also{repo.habit(it)}};val personal=Quest(name="Minha saga",habitIds=hs.joinToString(","){it.id},target=5,xp=20);repo.saga(personal);repo.startExpedition("roots",hs.map{it.id});val before=repo.state.value!!.xp
 hs.forEach{repo.progress(it,today(),1);repo.progress(it,today(),1)};assertEquals(before+50,repo.state.value!!.xp);repo.advanceExpedition("roots");repo.claim(repo.state.value!!.quests.first{it.id==personal.id});assertEquals(before+110,repo.state.value!!.xp)
 repo.habit(hs[0].copy(name="Outro nome",archived=true));assertTrue(expeditionComplete(repo.state.value!!,forestExpeditions[0]));repo.progress(hs[0],today(),0);assertFalse(expeditionComplete(repo.state.value!!,forestExpeditions[0]))
 }
 @Test fun v5DatabaseAndV6BackupDoNotInventExploration()=runBlocking{
 val old=repo.state.value!!;val ctx=RuntimeEnvironment.getApplication() as AscendApp;val name="exp-existing.db";legacyDatabase(ctx,name,5,old)
 val disk=Room.databaseBuilder(ctx,GameDatabase::class.java,name).addMigrations(MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10,MIGRATION_10_11).build();try{val r=GameRepository(disk);r.refresh();assertEquals(old,r.state.value);assertEquals(0,forestProgress(r.state.value!!))}finally{disk.close();ctx.deleteDatabase(name)}
 val gson=com.google.gson.Gson();val payload=gson.toJson(old);val digest=java.security.MessageDigest.getInstance("SHA-256").digest(payload.toByteArray()).joinToString(""){"%02x".format(it)};assertEquals(old,Backup.decode(gson.toJson(BackupEnvelope(version=6,sha256=digest,payload=payload))))
 }
 @Test fun widgetSimpleCompletionIsScheduledLocalAndIdempotent()=runBlocking{
 val h=Habit(name="Simples",xp=37,coins=4);repo.habit(h);val before=repo.state.value!!.xp;coroutineScope{(1..8).map{async{repo.widgetComplete(h.id,today())}}.awaitAll()};assertEquals(before+37,repo.state.value!!.xp);assertEquals(1,repo.state.value!!.records.count{it.habitId==h.id});assertEquals(2,widgetDay(repo.state.value!!).complete)
 assertTrue(runCatching{repo.widgetComplete(h.id,LocalDate.now().minusDays(1).toString())}.isFailure)
 val complex=Habit(name="Minutos",type=2,target=20);repo.habit(complex);assertTrue(runCatching{repo.widgetComplete(complex.id,today())}.isFailure)
 repo.rest(today(),true);assertTrue(runCatching{repo.widgetComplete(h.id,today())}.isFailure);assertEquals(0,widgetDay(repo.state.value!!).due)
 }
}
