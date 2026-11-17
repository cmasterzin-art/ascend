package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/** A live device-local date also updates a screen left open across midnight. */
@Composable fun currentDay():String {
    var date by remember{mutableStateOf(today())}
    LaunchedEffect(Unit){while(true){delay(15_000);date=today()}}
    return date
}
@Composable fun Journey(s:GameState,onProgress:(Habit,HabitRecord?)->Unit,onEdit:(Habit)->Unit,onFocus:(Habit)->Unit,onAdd:()->Unit,onJournal:()->Unit,vm:GameViewModel,relaxed:Boolean,onPlanner:()->Unit={},onAnalysis:()->Unit={},onHistory:()->Unit={},onPortrait:()->Unit={},onMissions:()->Unit={},onRegion:()->Unit={}){
 val day=currentDay();val rested=day in s.restDates
 val due=if(relaxed&&!rested)s.habits.filter{!it.deleted&&!it.archived&&!it.paused}.sortedBy{it.position} else s.due(day)
 val done=due.count{s.record(it.id,day)?.complete==true};var pendingOnly by remember{mutableStateOf(false)};var detail by remember{mutableStateOf<Habit?>(null)}
 androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize().testTag("journey_list").semantics{contentDescription="Central da Jornada"},contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Eyebrow("ASCEND  /  MINHA JORNADA");Text(displayDate(day),color=Muted,fontSize=12.sp)};ClassEmblem(s.player!!.heroClass,Modifier.size(28.dp))}}
  item{Column(Modifier.fillMaxWidth()){CompactHero(s,onPortrait);NorthDivider();Column(Modifier.fillMaxWidth().padding(top=4.dp,bottom=12.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
   DayDial(if(due.isEmpty())0f else done.toFloat()/due.size,if(due.isEmpty())"—" else "${done*100/due.size}%")
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){Eyebrow(if(rested)"Dia de descanso" else "Jornada de hoje",Green);Text("$done de ${due.size}",style=MaterialTheme.typography.titleLarge);Text("${due.size-done} pendentes",fontSize=12.sp,color=Muted)}
  };if(relaxed)Text("Modo livre · o widget mantém a agenda programada.",fontSize=11.sp,color=Muted)}}}
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("Planejador" to onPlanner,"Análises" to onAnalysis,"Foco" to onHistory).forEachIndexed{i,(label,action)->
   Column(Modifier.weight(1f).clip(CutCornerShape(3.dp)).background(Surface).forgeMaterial().clickable(onClick=action).padding(vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
    Icon(listOf(Icons.Default.DateRange,Icons.Default.BarChart,Icons.Default.Timer)[i],null,Modifier.size(18.dp),tint=Gold);Text(label,color=Ink,fontSize=11.sp)
   }
  }}}
  s.focus?.let{f->item{FocusCard(f,s,vm)}}
  item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){SectionHeader("Seus próximos passos","Conclua com um toque; detalhes ao selecionar o nome.")};IconButton(onAdd){Icon(Icons.Default.Add,"Criar hábito")}};if(due.size>1||done>0)ChipRow(listOf("Todos (${due.size})","Pendentes (${due.size-done})"),if(pendingOnly)1 else 0,{pendingOnly=it==1})}
  val visible=due.filter{!pendingOnly||s.record(it.id,day)?.complete!=true}.sortedBy{s.record(it.id,day)?.complete==true}
  if(visible.isEmpty())item{Empty(if(rested)"Este dia está reservado para descanso. Seus registros e recompensas anteriores continuam guardados." else if(due.isEmpty())"Nenhum hábito para hoje. Abra o Planejador ou crie seu primeiro passo." else "Tudo feito por hoje. Aetheris pode esperar pelo seu próximo passo.")}
  items(visible.size,key={visible[it].id}){i->val h=visible[i];HabitCard(h,s.record(h.id,day),{if(h.type==0&&s.record(h.id,day)?.complete!=true)vm.run{vm.repo.progress(h,day,1)}else onProgress(h,s.record(h.id,day))},{detail=h},{onFocus(h)})}
  item{WeekStrip(s,day)}
  item{SectionHeader("Além da rotina");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Missões",onMissions,Modifier.weight(1f),secondary=true);SagaButton("Região atual",onRegion,Modifier.weight(1f),secondary=true)}}
  item{NarrativePanel(s,vm);TextButton(onJournal){Text("Escrever no diário")}}
 }
 detail?.let{h->HabitDetails(h,s,{detail=null},{detail=null;onEdit(h)},{r->detail=null;onProgress(h,r)})}
}
@Composable fun WeekStrip(s:GameState,day:String){
    val end=LocalDate.parse(day);val start=end.with(DayOfWeek.MONDAY)
    Panel(accent=Green){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Eyebrow("Sua semana",Green);Text("Consistência, sem pressa",color=Muted,fontSize=10.sp)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
            repeat(7){i->val d=start.plusDays(i.toLong()).toString();val due=s.due(d);val done=due.count{s.record(it.id,d)?.complete==true};val rest=d in s.restDates;val color=if(d==day)Gold else if(done>0)Green else Muted
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if(d==day)Gold.copy(alpha=.1f) else CardColor.copy(alpha=.45f)).padding(vertical=10.dp).semantics(mergeDescendants=true){contentDescription="$d: $done de ${due.size} hábitos${if(rest)", descanso" else ""}"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)){
                    Text(weekdays[i],color=Muted,fontSize=10.sp);Text(start.plusDays(i.toLong()).dayOfMonth.toString(),color=color,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
                    Text(if(rest)"☽" else if(done==due.size&&done>0)"✓" else if(done>0)"·" else "—",color=color,fontSize=12.sp)
                }
            }
        }
    }
}
