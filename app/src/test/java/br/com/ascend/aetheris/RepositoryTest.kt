package br.com.ascend.aetheris

import androidx.room.Room
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28], application=AscendApp::class)
class RepositoryTest {
    private lateinit var db:GameDatabase
    private lateinit var repo:GameRepository
    private val h=Habit(id="direito",name="Estudar Direito",type=2,target=60,unit="minutos",xp=60,points=5,coins=5,proportional=true)
    @Before fun setUp()=runBlocking{db=Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(),GameDatabase::class.java).allowMainThreadQueries().build();repo=GameRepository(db);repo.refresh();repo.create(Player(name="Maga",heroClass=1),listOf(h))}
    @After fun tearDown(){db.close()}
    private fun state()=repo.state.value!!
    @Test fun proportionalProgressIsIdempotentAndUndoReversesXP()=runBlocking{
        repo.progress(h,today(),30);assertEquals(30L,state().xp)
        repo.progress(h,today(),30);assertEquals(30L,state().xp)
        repo.progress(h,today(),60);assertEquals(60L,state().xp);assertEquals(5L,state().points(0));assertEquals(1,state().records.size)
        repo.progress(h,today(),0);assertEquals(0L,state().xp);assertEquals(0L,state().points(0));assertEquals(1,state().ledger.count{it.id.startsWith("habit:")})
    }
    @Test fun habitEditPreservesPreviousRewardSnapshot()=runBlocking{
        repo.progress(h,today(),30);repo.habit(h.copy(xp=600,points=50));repo.progress(h.copy(xp=600,points=50),today(),60)
        assertEquals(60L,state().xp);assertEquals(5L,state().points(0))
    }
    @Test fun explicitRecordCorrectionRecomputesLevel()=runBlocking{
        repo.progress(h,today(),60);val r=state().records.single();repo.progress(h,today(),60,r.copy(xp=300))
        assertEquals(3,state().level);assertEquals(300L,state().xp)
        repo.progress(h,today(),0);assertEquals(1,state().level);assertEquals(0L,state().xp)
    }
    @Test fun manualCompletionAndFocusCannotDuplicatePayment()=runBlocking{
        repo.startFocus(h,60);repo.progress(h,today(),60)
        db.dao().focus(state().focus!!.copy(elapsed=3600,startedAt=0));repo.endFocus(true)
        assertEquals(60L,state().xp);assertEquals(1,state().records.size);assertNull(state().focus)
    }
    @Test fun pausedFocusPersistsAcrossRepositoryRecreation()=runBlocking{
        repo.startFocus(h,60);db.dao().focus(state().focus!!.copy(elapsed=123,startedAt=0))
        val restored=GameRepository(db);restored.refresh();assertEquals(123L,restored.state.value!!.focus!!.seconds())
    }
    @Test fun questClaimOnlyOnceAndInvalidationAfterUndo()=runBlocking{
        val q=Quest(id="test",name="Primeira sessão",target=1,xp=50,coins=10);repo.quest(q);repo.progress(h,today(),60);repo.claim(q)
        assertEquals(110L,state().xp);assertTrue(runCatching{repo.claim(q)}.isFailure)
        repo.progress(h,today(),0);assertEquals(0L,state().xp)
        repo.progress(h,today(),60);assertEquals(110L,state().xp);assertEquals(1,state().claims.size)
    }
    @Test fun purchasesAreAtomicAndCannotOverdraw()=runBlocking{
        repo.progress(h,today(),60,state().records.firstOrNull())
        val r=state().records.single();repo.progress(h,today(),60,r.copy(coins=70))
        val item=shopItems.first{it.id=="cloak"};coroutineScope{listOf(async{runCatching{repo.buy(item)}},async{runCatching{repo.buy(item)}}).awaitAll()}
        assertEquals(1,state().assets.count{it.id=="cloak"});assertEquals(1,state().ledger.count{it.id=="buy:cloak"});assertTrue(state().rawCoins>=0)
        assertTrue(runCatching{repo.buy(shopItems.first{it.id=="ignis"})}.isFailure)
    }
    @Test fun restorationAndInventorySurviveDatabaseReload()=runBlocking{
        repo.progress(h,today(),60);repo.progress(h,today(),60,state().records.single().copy(coins=100));repo.restore(0)
        val restored=GameRepository(db);restored.refresh();val s=restored.state.value!!;assertEquals(1,s.assets.first{it.id=="building:0"}.stage);assertTrue(chapterUnlocked(2,s));assertEquals(1,s.ledger.count{it.id=="restore:0:1"})
    }
    @Test fun backupRoundTripAndCorruptionRejection()=runBlocking{
        repo.progress(h,today(),60);repo.journal(Journal(title="Um dia",text="Aprendi algo."));val original=state();val encoded=Backup.encode(original);val decoded=Backup.decode(encoded);assertEquals(original,decoded)
        assertTrue(runCatching{Backup.decode(encoded.replace("Maga","Outro"))}.isFailure)
        repo.importState(decoded);assertEquals(60L,state().xp);assertEquals("Aprendi algo.",state().journal.single().text)
    }
    @Test fun invalidImportLeavesDataUntouched()=runBlocking{
        val original=state();assertTrue(runCatching{repo.importState(original.copy(records=listOf(HabitRecord("bad","missing",today(),1,1,1,1,1,0,false,0,"","",""))))}.isFailure)
        assertEquals(original,state())
    }
    @Test fun editingQuestDoesNotRewriteClaimedRewards()=runBlocking{
        val q=Quest(id="snapshot",name="Missão",target=1,xp=50,coins=10)
        repo.quest(q);repo.progress(h,today(),60);repo.claim(q);repo.quest(q.copy(xp=500,coins=100,target=10))
        assertEquals(110L,state().xp)
        repo.progress(h,today(),0);assertEquals(0L,state().xp)
    }
    @Test fun databaseFilePersistsAfterCloseAndReopen()=runBlocking{
        val context=(RuntimeEnvironment.getApplication() as AscendApp);context.deleteDatabase("persistence-test.db")
        var disk=Room.databaseBuilder(context,GameDatabase::class.java,"persistence-test.db").build()
        val r1=GameRepository(disk);r1.refresh();r1.create(Player(name="Persistente"),listOf(h));r1.progress(h,today(),60);disk.close()
        disk=Room.databaseBuilder(context,GameDatabase::class.java,"persistence-test.db").build()
        try{val r2=GameRepository(disk);r2.refresh();assertEquals("Persistente",r2.state.value!!.player!!.name);assertEquals(60L,r2.state.value!!.xp);assertEquals(1,r2.state.value!!.records.size)}finally{disk.close();context.deleteDatabase("persistence-test.db")}
    }

}
