package br.com.ascend.aetheris

import androidx.room.*
import java.time.LocalDate

@Entity(tableName="plan_exceptions",foreignKeys=[ForeignKey(entity=Habit::class,parentColumns=["id"],childColumns=["habitId"],onDelete=ForeignKey.RESTRICT)],indices=[Index("habitId")])
data class PlanException(@PrimaryKey val id:String,val habitId:String,val date:String,val include:Boolean)
@Entity(tableName="focus_sessions",foreignKeys=[ForeignKey(entity=Habit::class,parentColumns=["id"],childColumns=["habitId"],onDelete=ForeignKey.RESTRICT)],indices=[Index("habitId")])
data class FocusSession(@PrimaryKey val id:String,val habitId:String,val habitName:String,val date:String,val startedAt:Long,val endedAt:Long,val elapsedSeconds:Long,val plannedMinutes:Int,val status:String,val registered:Boolean)

data class AnalysisPeriod(val start:String,val end:String,val label:String){
 val days get()=java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(start),LocalDate.parse(end)).toInt()+1
 val previous get()=AnalysisPeriod(LocalDate.parse(start).minusDays(days.toLong()).toString(),LocalDate.parse(start).minusDays(1).toString(),"Período anterior")
}
fun analysisPeriod(choice:Int,day:String=today()):AnalysisPeriod{
 val end=LocalDate.parse(day);val start=when(choice){3->LocalDate.parse(weekOf(day));4->end.withDayOfMonth(1);else->end.minusDays(listOf(7,30,90)[choice.coerceIn(0,2)]-1L)}
 return AnalysisPeriod(start.toString(),day,listOf("Últimos 7 dias","Últimos 30 dias","Últimos 90 dias","Semana civil até hoje","Mês civil até hoje")[choice.coerceIn(0,4)])
}
data class HabitAnalysis(val habit:Habit,val planned:Int,val complete:Int,val scheduledComplete:Int,val minutes:Long,val xp:Long){val rate:Int? get()=if(planned==0)null else 100*scheduledComplete/planned}
fun analyzeHabit(s:GameState,h:Habit,p:AnalysisPeriod):HabitAnalysis{
 val start=LocalDate.parse(p.start);val dates=(0 until p.days).map{start.plusDays(it.toLong()).toString()};val rows=s.records.filter{it.habitId==h.id&&it.date in p.start..p.end}
 return HabitAnalysis(h,dates.count{scheduled(h,it,s.restDates,s.plans)},rows.count{it.complete},rows.count{it.complete&&scheduled(h,it.date,s.restDates,s.plans)},rows.filter{it.type==2}.sumOf{it.progress.toLong()},rows.sumOf{Progression.reward(it.xp,it.progress,it.target,it.proportional)})
}
fun localInsights(s:GameState,p:AnalysisPeriod):List<String>{
 val rows=s.habits.map{analyzeHabit(s,it,p)};val best=rows.filter{it.planned>0}.maxByOrNull{it.rate?:0}
 val current=periodSummary(s,p.start,p.end);val previous=periodSummary(s,p.previous.start,p.previous.end)
 return buildList{
  if(s.records.none{it.date in p.start..p.end&&it.progress>0}){add("Ainda não há registros neste período. As análises aparecerão após suas atividades.");return@buildList}
  best?.let{add("Maior cumprimento: ${it.habit.name}, ${it.scheduledComplete}/${it.planned} atividades previstas (${it.rate}%).")}
  if(previous.completed>0)add("${current.completed} conclusões neste período; ${previous.completed} no período anterior de mesma duração.") else add("O período anterior ainda não tem conclusões para comparação.")
  val byDay=(1..7).map{d->val days=(0 until p.days).map{LocalDate.parse(p.start).plusDays(it.toLong())}.filter{it.dayOfWeek.value==d};val planned=days.sumOf{day->s.due(day.toString()).size};val done=days.sumOf{day->s.due(day.toString()).count{s.record(it.id,day.toString())?.complete==true}};Triple(d,planned,done)}.filter{it.second>0}.maxByOrNull{it.third.toDouble()/it.second}
  byDay?.let{add("Maior taxa por dia: ${listOf("segunda","terça","quarta","quinta","sexta","sábado","domingo")[it.first-1]}, ${it.third}/${it.second} atividades previstas.")}
  val categories=s.records.filter{it.type==2&&it.date in p.start..p.end}.groupBy{it.category}.mapValues{it.value.sumOf{r->r.progress.toLong()}}
  categories.maxByOrNull{it.value}?.let{add("Tempo registrado em ${it.key}: ${it.value} minutos. Esse total usa registros de hábitos, sem somar sessões novamente.")}
 }
}
