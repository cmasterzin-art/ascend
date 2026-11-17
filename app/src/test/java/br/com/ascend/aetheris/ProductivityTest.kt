package br.com.ascend.aetheris

import androidx.room.Room
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
import java.time.*
import com.google.gson.Gson
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=AscendApp::class)
class ProductivityTest{
 private lateinit var db:GameDatabase;private lateinit var repo:GameRepository
 private val context get()=RuntimeEnvironment.getApplication() as AscendApp
 private val h=Habit(id="focus",name="Estudo real",type=2,target=60,xp=60,coins=6,points=6,proportional=true,created=LocalDate.now().minusDays(14).toString())
 private fun s()=repo.state.value!!
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(context,GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Iona",created=h.created),listOf(h))}
 @After fun close(){db.close()}
 @Test fun includeExcludeAndResetShareOneScheduleWithoutRewards()=runBlocking{
  repo.habit(h.copy(days=LocalDate.now().plusDays(1).dayOfWeek.value.toString()));assertTrue(s().due().isEmpty());val before=s().xp
  repo.plan(h.id,today(),1);assertEquals(1,s().due().size);assertEquals(1,widgetDay(s()).due);assertEquals(1,periodSummary(s(),today(),today()).planned)
  repo.plan(h.id,today(),2);assertTrue(s().due().isEmpty());repo.plan(h.id,today(),0);assertTrue(s().plans.isEmpty());assertEquals(before,s().xp);assertTrue(s().records.isEmpty())
 }
 @Test fun restAndInactiveStateHavePriorityAndHistoryIsPreserved()=runBlocking{
  repo.plan(h.id,today(),1);repo.rest(today(),true);assertTrue(s().due().isEmpty());repo.rest(today(),false)
  val yesterday=LocalDate.now().minusDays(1).toString();repo.progress(h,yesterday,60)
  for(v in listOf(h.copy(paused=true),h.copy(archived=true),h.copy(deleted=true))){repo.habit(v);assertTrue(s().due().isEmpty());assertEquals(1,s().due(yesterday).size)}
 }
 @Test fun pastExceptionsAreRejectedAndExistingRecordKeepsItsSnapshot()=runBlocking{
  repo.progress(h,today(),30);val r=s().records.single();val xp=s().xp
  assertTrue(runCatching{repo.plan(h.id,LocalDate.now().minusDays(1).toString(),2)}.isFailure)
  repo.plan(h.id,today(),2);assertEquals(r,s().records.single());assertEquals(xp,s().xp);assertNull(periodSummary(s(),today(),today()).rate)
  repo.plan(h.id,today(),0);assertEquals(r,s().records.single())
 }
 @Test fun futureWeekAndPersistenceUseStableExceptionIds()=runBlocking{
  val date=LocalDate.parse(weekOf(today())).plusWeeks(1).toString();repo.plan(h.id,date,2);repo.plan(h.id,date,2)
  val reload=GameRepository(db);reload.refresh();assertEquals(listOf(PlanException("${h.id}@$date",h.id,date,false)),reload.state.value!!.plans);assertTrue(reload.state.value!!.due(date).isEmpty())
 }
 @Test fun widgetCompletionHonorsIncludeAndRefusesDispense()=runBlocking{
  val simple=h.copy(type=0,target=1,xp=17);repo.habit(simple);repo.plan(h.id,today(),2);assertTrue(runCatching{repo.widgetComplete(h.id,today())}.isFailure)
  repo.plan(h.id,today(),1);coroutineScope{(1..8).map{async{repo.widgetComplete(h.id,today())}}.awaitAll()};assertEquals(17L,s().xp);assertEquals(1,s().records.size);assertEquals(100,periodSummary(s(),today(),today()).rate)
 }
 @Test fun remindersUseTheSameExceptionalSchedule()=runBlocking{
  val tomorrow=LocalDate.now().plusDays(1);val reminder=h.copy(reminder=true,days=tomorrow.dayOfWeek.value.toString(),time="23:00");repo.habit(reminder);repo.plan(h.id,today(),1)
  val now=LocalDate.now().atStartOfDay(ZoneId.systemDefault());assertEquals(LocalDate.now(),nextReminderAt(reminder,s().restDates,now,s().plans)!!.toLocalDate())
  repo.plan(h.id,today(),2);assertEquals(tomorrow,nextReminderAt(reminder,s().restDates,now,s().plans)!!.toLocalDate())
 }
 @Test fun focusHistoryIsAtomicAndDoesNotAddAnotherReward()=runBlocking{
  repo.startFocus(h,25);val id=s().focus!!.sessionId;assertTrue(id.isNotBlank());db.dao().focus(s().focus!!.copy(elapsed=120,startedAt=0))
  coroutineScope{(1..8).map{async{repo.endFocus(true)}}.awaitAll()};assertEquals(2L,s().xp);assertEquals(1,s().records.size);assertEquals(1,s().focusSessions.size);assertEquals(id,s().focusSessions.single().id);assertEquals("interrupted",s().focusSessions.single().status)
  repo.progress(h,today(),1);assertEquals(1L,s().xp);assertEquals(120L,s().focusSessions.single().elapsedSeconds)
 }
 @Test fun cancelledAndCompletedSessionsHaveExplicitStates()=runBlocking{
  repo.startFocus(h,1);db.dao().focus(s().focus!!.copy(elapsed=60,startedAt=0));repo.endFocus(true);assertEquals("completed",s().focusSessions.single().status)
  repo.startFocus(h,25);db.dao().focus(s().focus!!.copy(elapsed=15,startedAt=0));val before=s().xp;repo.endFocus(false);assertEquals(before,s().xp);assertEquals(2,s().focusSessions.size);assertTrue(s().focusSessions.any{it.status=="cancelled"&&!it.registered})
 }
 @Test fun pausedSessionRetainsIdentityAcrossRepositoryRecreation()=runBlocking{
  repo.startFocus(h,25);val f=s().focus!!;db.dao().focus(f.copy(elapsed=65,startedAt=0));val reload=GameRepository(db);reload.refresh();assertEquals(f.sessionId,reload.state.value!!.focus!!.sessionId);assertEquals(65L,reload.state.value!!.focus!!.seconds());reload.endFocus(true);assertEquals(65L,reload.state.value!!.focusSessions.single().elapsedSeconds)
 }
 @Test fun archiveDeleteAndRenameDoNotEraseFinishedSessions()=runBlocking{
  repo.startFocus(h,1);db.dao().focus(s().focus!!.copy(elapsed=60,startedAt=0));repo.endFocus(true);val saved=s().focusSessions
  repo.habit(h.copy(name="Outro nome",archived=true,deleted=true));assertEquals(saved,s().focusSessions);repo.importState(Backup.decode(Backup.encode(s())));assertEquals(saved,s().focusSessions)
 }
 @Test fun backupEightRoundTripAndInvalidRestoreAreAtomic()=runBlocking{
  repo.plan(h.id,today(),2);repo.startFocus(h,25);db.dao().focus(s().focus!!.copy(elapsed=65,startedAt=0));repo.endFocus(true);val before=s();val data=Backup.encode(before);assertTrue(data.contains("\"version\":8"));assertEquals(before,Backup.decode(data))
  val invalid=before.copy(plans=listOf(PlanException("wrong",h.id,today(),true)));assertTrue(runCatching{repo.importState(invalid)}.isFailure);assertEquals(before,s());repo.importState(Backup.decode(data));assertEquals(before,s())
 }
 @Test fun v7BackupHasNoInventedSessionsOrExceptions()=runBlocking{
  repo.progress(h,today(),30);val gson=Gson();val obj=gson.toJsonTree(s()).asJsonObject;obj.remove("plans");obj.remove("focusSessions");val data=gson.toJson(obj);val hash=MessageDigest.getInstance("SHA-256").digest(data.toByteArray()).joinToString(""){"%02x".format(it)}
  val restored=Backup.decode(gson.toJson(BackupEnvelope(version=7,sha256=hash,payload=data)));assertTrue(restored.plans.isEmpty());assertTrue(restored.focusSessions.isEmpty());assertEquals(30L,restored.xp)
 }
 @Test fun schemaFiveMigrationPreservesAllOldDataAndRunningLegacyFocus()=runBlocking{
  repo.progress(h,today(),30);val before=s().copy(focus=Focus(habitId=h.id,elapsed=60,startedAt=0,baseline=30));val name="v160-v5.db";legacyDatabase(context,name,5,before)
  val disk=Room.databaseBuilder(context,GameDatabase::class.java,name).addMigrations(MIGRATION_5_6).build()
  try{val r=GameRepository(disk);r.refresh();assertEquals(before,r.state.value);r.endFocus(true);assertTrue(r.state.value!!.focusSessions.isEmpty());assertEquals(31L,r.state.value!!.xp)}finally{disk.close();context.deleteDatabase(name)}
 }
 @Test fun analysisPeriodsCorrectionsAndEmptyInsightsAreDeterministic()=runBlocking{
  val p=analysisPeriod(0);assertEquals(7,p.days);assertEquals(7,p.previous.days);assertEquals(weekOf(today()),analysisPeriod(3).start);assertEquals(LocalDate.now().withDayOfMonth(1).toString(),analysisPeriod(4).start);assertTrue(localInsights(s(),p).single().contains("Ainda não"))
  repo.progress(h,today(),60);assertEquals(1,analyzeHabit(s(),h,p).complete);repo.progress(h,today(),30);assertEquals(0,analyzeHabit(s(),h,p).complete);assertEquals(30L,analyzeHabit(s(),h,p).xp);assertTrue(localInsights(s(),p).isNotEmpty())
 }
 @Test fun newPlanningAndSessionTablesSurviveClosingAndReopeningTheDatabaseFile()=runBlocking{
  val name="productivity-disk.db";context.deleteDatabase(name);var disk=Room.databaseBuilder(context,GameDatabase::class.java,name).build()
  try{val r=GameRepository(disk);r.refresh();r.create(Player(name="Persistente",created=h.created),listOf(h));r.plan(h.id,today(),2);r.startFocus(h,25);disk.dao().focus(r.state.value!!.focus!!.copy(elapsed=60,startedAt=0));r.endFocus(true);val before=r.state.value!!;disk.close()
   disk=Room.databaseBuilder(context,GameDatabase::class.java,name).build();val reload=GameRepository(disk);reload.refresh();assertEquals(before,reload.state.value);assertEquals(1,reload.state.value!!.plans.size);assertEquals(1,reload.state.value!!.focusSessions.size)
  }finally{disk.close();context.deleteDatabase(name)}
 }

}
