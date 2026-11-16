package br.com.ascend.aetheris

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Scheduled days form the denominator; extra activities still earn their normal rewards. */
data class PeriodSummary(val xp:Long, val completed:Int, val planned:Int, val scheduledCompleted:Int, val minutes:Long) {
    val rate:Int? get() = if(planned==0) null else (100L*scheduledCompleted/planned).toInt()
}
fun periodSummary(s:GameState,start:String,end:String):PeriodSummary {
    val from=LocalDate.parse(start);val to=LocalDate.parse(end)
    require(!to.isBefore(from) && ChronoUnit.DAYS.between(from,to)<36600)
    val rests=s.restDates
    val habits=s.habits.associateBy{it.id}
    val records=s.records.filter{it.date in start..end}
    var planned=0
    var date=from
    while(!date.isAfter(to)) { planned+=s.habits.count{scheduled(it,date.toString(),rests)};date=date.plusDays(1) }
    val complete=records.filter{it.complete}
    return PeriodSummary(s.ledger.filter{it.date in start..end}.sumOf{it.xp},complete.size,planned,
        complete.count{r->habits[r.habitId]?.let{scheduled(it,r.date,rests)}==true},
        records.filter{it.type==2}.sumOf{it.progress.toLong()})
}
fun bestStreak(h:Habit,records:List<HabitRecord>,end:String=today(),restDates:Set<String> = emptySet()):Int {
    if(!h.trackStreak) return 0
    val dates=records.filter{it.habitId==h.id && it.complete}.map{it.date}.toSet()
    var day=LocalDate.parse(h.created);val last=LocalDate.parse(end);var count=0;var best=0
    repeat(36600) {
        if(day.isAfter(last)) return best
        val date=day.toString()
        if(scheduled(h,date,restDates)) { count=if(date in dates) count+1 else 0;best=maxOf(best,count) }
        day=day.plusDays(1)
    }
    return best
}
