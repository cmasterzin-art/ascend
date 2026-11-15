package br.com.ascend.aetheris
import org.junit.Assert.*
import org.junit.Test

class ProgressionTest {
    @Test fun levelBoundariesAndMultipleLevels(){
        assertEquals(1,Progression.level(0));assertEquals(1,Progression.level(99));assertEquals(2,Progression.level(100));assertEquals(2,Progression.level(249));assertEquals(3,Progression.level(250))
        for(n in 1..1000){assertEquals(n,Progression.level(Progression.threshold(n)));assertEquals(Progression.requirement(n),Progression.threshold(n+1)-Progression.threshold(n))}
        assertEquals(20,Progression.level(Progression.threshold(20)+1))
    }
    @Test fun proportionalRewardsAreAbsoluteAndOverflowSafe(){
        assertEquals(30L,Progression.reward(60,30,60,true));assertEquals(60L,Progression.reward(60,60,60,true));assertEquals(0L,Progression.reward(60,30,60,false));assertEquals(60L,Progression.reward(60,61,60,true))
        assertEquals(1_000_000L,Progression.reward(1_000_000,1_000_000,1_000_000,true))
        val first=Progression.reward(60,30,60,true);val last=Progression.reward(60,60,60,true);assertEquals(30L,last-first)
    }
    private fun record(h:Habit,date:String)=HabitRecord("${h.id}@$date",h.id,date,1,1,50,5,5,0,false,0,"Estudo","vez",h.name)
    @Test fun streakIgnoresUnscheduledDays(){
        val h=Habit(id="study",name="Estudar",days="1,3,5",created="2026-11-01")
        val rs=listOf("2026-11-09","2026-11-11","2026-11-13").map{record(h,it)}
        assertEquals(3,streak(h,rs,"2026-11-15"));assertEquals(3,streak(h,rs,"2026-11-16"));assertEquals(0,streak(h,rs,"2026-11-17"))
    }
    @Test fun pausesAndScheduleChangesPreserveHistory(){
        val h=Habit(id="a",name="A",created="2026-11-01",days="2,4",scheduleHistory="2026-11-01|1,3,5|false;2026-11-10|1,3,5|true;2026-11-13|2,4|false")
        assertTrue(scheduled(h,"2026-11-09"));assertFalse(scheduled(h,"2026-11-11"));assertFalse(scheduled(h,"2026-11-13"));assertTrue(scheduled(h,"2026-11-17"))
    }
    @Test fun questOccurrencesFilterRealRecords(){
        val h=Habit(id="a",name="A",created="2026-11-01")
        val s=GameState(habits=listOf(h),records=listOf(record(h,"2026-11-08"),record(h,"2026-11-09"),record(h,"2026-11-15")))
        assertEquals(2L,questProgress(Quest(name="Week",period=1),s,"2026-11-09"));assertEquals(1L,questProgress(Quest(name="Day",period=0),s,"2026-11-15"));assertEquals(3L,questProgress(Quest(name="Epic"),s))
        assertEquals("2026-11-09",weekOf("2026-11-15"))
    }
    @Test fun availableCoinsNeverNegativeAfterReversal(){
        val s=GameState(ledger=listOf(Reward("earned",coins=5),Reward("spent",coins=-10)))
        assertEquals(0L,s.coins);assertEquals(5L,s.debt)
    }
}
