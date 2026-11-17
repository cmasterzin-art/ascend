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
class StabilityTest{
 private lateinit var db:GameDatabase
 private lateinit var repo:GameRepository
 private val h=Habit(id="study",name="Estudar",type=2,target=60,xp=60,coins=6,points=6,proportional=true,created=LocalDate.now().minusDays(10).toString())
 private fun state()=repo.state.value!!
 @Before fun setup()=runBlocking{db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Eira",created=h.created),listOf(h))}
 @After fun close(){db.close()}
 @Test fun pendingRecordUsesCurrentHabitButPreservesExistingSnapshots()=runBlocking{
  repo.habit(h.copy(xp=120,coins=12,points=12,target=30));repo.progress(h,today(),30)
  assertEquals(120L,state().xp);assertEquals(30,state().records.single().target)
  repo.habit(h.copy(xp=600));repo.progress(h,today(),10)
  assertEquals(40L,state().xp);assertEquals(120,state().records.single().xp)
 }
 @Test fun invalidAmountCannotEraseAnExistingRecord()=runBlocking{
  repo.progress(h,today(),30);val before=state();assertTrue(runCatching{repo.progress(h,today(),-1)}.isFailure);assertEquals(before,state())
 }
 @Test fun focusStartRejectsAStaleHabitChangedToSimple()=runBlocking{
  repo.habit(h.copy(type=0,target=1));assertTrue(runCatching{repo.startFocus(h,25)}.isFailure);assertNull(state().focus)
 }
 @Test fun focusRespectsRestDayInItsHistoricalSnapshot()=runBlocking{
  repo.rest(today(),true);repo.startFocus(h,25);db.dao().focus(state().focus!!.copy(elapsed=60,startedAt=0));repo.endFocus(true)
  assertFalse(state().records.single().scheduled);assertEquals(1L,state().xp)
 }
 @Test fun invalidFocusBackupIsRejectedBeforeReplacingProgress()=runBlocking{
  repo.progress(h,today(),10);val before=state()
  val invalid=before.copy(focus=Focus(habitId=h.id,date="not-a-date",duration=25,elapsed=60))
  assertTrue(runCatching{Backup.decode(Backup.encode(invalid))}.isFailure)
  assertTrue(runCatching{repo.importState(invalid)}.isFailure);assertEquals(before,state())
 }
 @Test fun invalidCharacterDateIsRejectedByBackupValidation()=runBlocking{
  val invalid=state().copy(player=state().player!!.copy(created="not-a-date"))
  assertTrue(runCatching{Backup.decode(Backup.encode(invalid))}.isFailure)
 }
 @Test fun deletedHabitCannotStartANewFocusFromAnOldScreen()=runBlocking{
  repo.habit(h.copy(deleted=true));assertTrue(runCatching{repo.startFocus(h,25)}.isFailure);assertNull(state().focus)
 }
 @Test fun runningAndPausedFocusSurviveReloadWithoutDoublePayment()=runBlocking{
  repo.startFocus(h,25);val f=state().focus!!;db.dao().focus(f.copy(startedAt=System.currentTimeMillis()-125000))
  repo.pauseFocus();val seconds=state().focus!!.elapsed;assertTrue(seconds in 124..126);val reload=GameRepository(db);reload.refresh();assertEquals(seconds,reload.state.value!!.focus!!.seconds())
  reload.pauseFocus();reload.pauseFocus();reload.endFocus(true);reload.endFocus(true)
  assertEquals(2L,reload.state.value!!.xp);assertEquals(1,reload.state.value!!.records.size);assertNull(reload.state.value!!.focus)
 }
 @Test fun restoreFailureRollsBackDeletedTablesAndAValidBackupStillRestores()=runBlocking{
  repo.progress(h,today(),30);repo.journal(Journal(title="Registro local",text="Texto privado de teste"));val before=state();val incoming=before.copy(player=before.player!!.copy(name="Restaurada"))
  db.openHelper.writableDatabase.execSQL("CREATE TRIGGER fail_restore BEFORE INSERT ON player BEGIN SELECT RAISE(ABORT, 'restore fixture'); END")
  try{assertTrue(runCatching{repo.importState(incoming)}.isFailure);val reload=GameRepository(db);reload.refresh();assertEquals(before,reload.state.value);assertEquals(before,state())}
  finally{db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_restore")}
  repo.importState(Backup.decode(Backup.encode(incoming)));assertEquals(incoming,state())
 }
 @Test fun complexWidgetRequestsRecheckDateTypeAndAvailability()=runBlocking{
  repo.habit(h);assertEquals(h.id,widgetRegistrationHabit(state(),h.id,null)?.id)
  assertNull(widgetRegistrationHabit(state(),h.id,LocalDate.now().minusDays(1).toString()))
  for(updated in listOf(h.copy(type=0),h.copy(paused=true),h.copy(archived=true),h.copy(deleted=true))){repo.habit(updated);assertNull(widgetRegistrationHabit(state(),h.id,today()))}
  repo.habit(h);repo.rest(today(),true);assertNull(widgetRegistrationHabit(state(),h.id,today()))
 }
 @Test fun singleDayPeriodsAreBrazilianWithoutRepeatingTheDate(){
  assertEquals("16/11/2026",displayPeriod("2026-11-16","2026-11-16"));assertEquals("16/11/2026 a 22/11/2026",displayPeriod("2026-11-16","2026-11-22"))
 }

}
