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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Reads the device clock rather than advancing an in-memory clock. */
@Composable private fun currentLocalMinute(): LocalDateTime {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var now by remember { mutableStateOf(LocalDateTime.now().withSecond(0).withNano(0)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) now = LocalDateTime.now().withSecond(0).withNano(0)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now().withSecond(0).withNano(0)
            delay(1_000)
        }
    }
    return now
}

@Composable fun currentDay(): String = currentLocalMinute().toLocalDate().toString()

@Composable fun Journey(s:GameState,onProgress:(Habit,HabitRecord?)->Unit,onEdit:(Habit)->Unit,onFocus:(Habit)->Unit,onAdd:()->Unit,onJournal:()->Unit,vm:GameViewModel,relaxed:Boolean,onPlanner:()->Unit={},onAnalysis:()->Unit={},onHistory:()->Unit={},onPortrait:()->Unit={},onMissions:()->Unit={},onRegion:()->Unit={},onPaths:()->Unit={},onInbox:()->Unit={},onPending:()->Unit={},onOpenPath:(String)->Unit={},onMimirReview:()->Unit={},onOdin:()->Unit={},onTimeForge:()->Unit={},onCouncilNine:()->Unit={}){
 val now=currentLocalMinute();val day=now.toLocalDate().toString();val rested=day in s.restDates
 val due=if(relaxed&&!rested)s.habits.filter{!it.deleted&&!it.archived&&!it.paused}.sortedBy{it.position} else s.due(day)
 val done=due.count{s.record(it.id,day)?.complete==true};var pendingOnly by remember{mutableStateOf(false)};var detail by remember{mutableStateOf<Habit?>(null)}
  val dialSize=if(LocalConfiguration.current.screenWidthDp<=340)56.dp else 66.dp
  val inboxCount=s.inboxItems.count{!it.archived}
  val dueCardsCount=remember(s.notebooks,s.flashcards,day){dueFlashcards(s,day).size}
  val rumos=remember(s,now){rumosDoDia(s,day,now.toLocalTime())}
  val hasRumos=rumos.nextEvent!=null||rumos.nextBlock!=null||rumos.untimedEvents.isNotEmpty()||rumos.priorityTasks.isNotEmpty()||rumos.upcomingPaths.isNotEmpty()||rumos.inboxCount>0
  var rumosExpanded by androidx.compose.runtime.saveable.rememberSaveable{mutableStateOf(false)}
  val listState = androidx.compose.foundation.lazy.rememberLazyListState()
  LaunchedEffect(s.focus?.sessionId) {
   if (s.focus != null) listState.scrollToItem(0)
  }
  androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize().testTag("journey_list").semantics{contentDescription="Central da Jornada"},state=listState,contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
   s.focus?.let { focus -> item(key="active-focus") { FocusCard(focus,s,vm) } }
   item{Box(Modifier.fillMaxWidth()){
    CompactHero(s,onPortrait)
    // 1.7.2 DS 3.0: floating fjord header with subtle historical Raido rune accent.
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){FutharkRuneGlyph(HistoricalFutharkRune.Raido,Gold,size=13.dp);Eyebrow("ASCEND  /  MINHA JORNADA")};Text(displayDate(day),color=Muted,fontSize=12.sp)};ClassEmblem(s.player!!.heroClass,Modifier.size(26.dp))}
   }}
   item{Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)){
    DayLedger(done,due.size,rested,relaxed,dialSize)
    ShortcutStrip(listOf(Triple("Planejador",Glyph.Planner,onPlanner),Triple("Análises",Glyph.Analysis,onAnalysis),Triple("Foco",Glyph.Focus,onHistory)))
    ShortcutStrip(listOf(Triple("Caminhos",Glyph.Map,onPaths),Triple("Entrada${if(inboxCount>0)" ($inboxCount)" else ""}",Glyph.Chronicles,onInbox),Triple("Pendências",Glyph.Claims,onPending)))
    ShortcutStrip(listOf(Triple("Mesa de Odin",Glyph.World,onOdin),Triple("Forja Tempo",Glyph.Focus,onTimeForge),Triple("Conselho 9",Glyph.Hero,onCouncilNine)))
   }}
  item{RumosDoDiaPanel(rumos,s,vm,expanded=rumosExpanded||hasRumos&&rumosExpanded,hasRumos=hasRumos,dueCardsCount=dueCardsCount,onToggle={rumosExpanded=!rumosExpanded},onPaths=onPaths,onOpenPath=onOpenPath,onPlanner=onPlanner,onInbox=onInbox,onPending=onPending,onMimirReview=onMimirReview)}
  item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){SectionHeader("Seus próximos passos","Conclua com um toque; detalhes ao selecionar o nome.")};IconButton(onAdd,Modifier.size(48.dp).clip(CutCornerShape(5.dp)).background(Gold.copy(alpha=.12f)).atlasCellNineSlice(R.drawable.viking_button_atlas,index=VikingButtonStyle.BronzeForjado.cellIndex,columns=2,rows=3,capXRatio=.2f,capYRatio=.28f,alpha=.72f).border(1.dp,Gold.copy(alpha=.52f),CutCornerShape(5.dp))){Icon(Icons.Default.Add,"Criar hábito",tint=Color(0xFFF3EBDC))}};if(due.size>1||done>0)ChipRow(listOf("Todos (${due.size})","Pendentes (${due.size-done})"),if(pendingOnly)1 else 0,{pendingOnly=it==1})}
  val visible=due.filter{!pendingOnly||s.record(it.id,day)?.complete!=true}.sortedBy{s.record(it.id,day)?.complete==true}
  if(visible.isEmpty())item{Empty(if(rested)"Este dia está reservado para descanso. Seus registros e recompensas anteriores continuam guardados." else if(due.isEmpty())"Nenhum hábito para hoje. Abra o Planejador ou crie seu primeiro passo." else "Tudo feito por hoje. Aetheris pode esperar pelo seu próximo passo.")}
  items(visible.size,key={visible[it].id}){i->val h=visible[i];HabitCard(h,s.record(h.id,day),{if(h.type==0&&s.record(h.id,day)?.complete!=true)vm.run{vm.repo.progress(h,day,1)}else onProgress(h,s.record(h.id,day))},{detail=h},{onFocus(h)})}
  item{WeekStrip(s,day)}
  item{VikingKnotDivider(variant=7,alpha=.72f,height=16.dp);SectionHeader("Além da rotina");Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Missões",onMissions,Modifier.weight(1f),style=VikingButtonStyle.FerroRunico,leadingRune=HistoricalFutharkRune.Fehu);SagaButton("Região atual",onRegion,Modifier.weight(1f),style=VikingButtonStyle.FerroRunico,leadingRune=HistoricalFutharkRune.Ehwaz)}}
  item{NarrativePanel(s,vm);TextButton(onJournal){Text("Escrever no diário")}}
 }
 detail?.let{h->HabitDetails(h,s,{detail=null},{detail=null;onEdit(h)},{r->detail=null;onProgress(h,r)})}
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun RumosDoDiaPanel(r:RumosDoDia,s:GameState,vm:GameViewModel,expanded:Boolean,hasRumos:Boolean,dueCardsCount:Int=0,onToggle:()->Unit,onPaths:()->Unit,onOpenPath:(String)->Unit,onPlanner:()->Unit,onInbox:()->Unit,onPending:()->Unit,onMimirReview:()->Unit={}){
 AetherSurface(if(r.overdueCount>0)SurfaceKind.Heroic else SurfaceKind.Operational,Modifier.testTag("rumos_do_dia")){
  Row(Modifier.fillMaxWidth().clickable(onClick=onToggle),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
    Eyebrow("Rumos do Dia",Gold)
    Text(
     if(!hasRumos)"Sem compromissos, blocos ou tarefas urgentes hoje."
     else "${r.pendingTodayCount} pendentes hoje · ${r.overdueCount} atrasadas · ${r.plannedBlocksCount} blocos · ${r.inboxCount} entrada",
     color=if(r.overdueCount>0)Gold else Muted,
     fontSize=12.sp
    )
   }
   TextButton(onToggle){Text(if(expanded)"Recolher" else "Abrir rumos",fontSize=12.sp)}
  }
  if(dueCardsCount>0){
   Row(Modifier.fillMaxWidth().clickable(onClick=onMimirReview).padding(top=2.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
    Text("◈ Runas da Memória · $dueCardsCount ${if(dueCardsCount==1)"revisão pendente" else "revisões pendentes"}",color=Blue,fontSize=12.sp,modifier=Modifier.weight(1f))
    Text("Revisar",color=Gold,fontSize=12.sp)
   }
  }
  if(expanded){
   Hairline()
   if(!hasRumos){
    Text("Sua agenda e caminhos estão leves hoje. Capture uma ideia rápida ou planeje um novo caminho.",color=Muted,fontSize=12.sp)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
     SagaButton("Abrir Caminhos",onPaths,Modifier.weight(1f),secondary=true)
     SagaButton("Capturar",onInbox,Modifier.weight(1f),secondary=true)
    }
   }else{
    r.nextEvent?.let{ev->
     Row(Modifier.fillMaxWidth().clickable(onClick=onPlanner),horizontalArrangement=Arrangement.SpaceBetween){
      Text("${if(r.eventInProgress) "Compromisso em andamento" else "Próximo compromisso"}: ${ev.title}",Modifier.weight(1f),color=Ink,fontSize=13.sp)
      if(ev.startTime.isNotBlank())Text(ev.startTime,color=Blue,fontSize=12.sp)
     }
    }
    r.nextBlock?.let{b->
     Row(Modifier.fillMaxWidth().clickable(onClick=onPlanner),horizontalArrangement=Arrangement.SpaceBetween){
      Text("${if(r.blockInProgress) "Bloco em andamento" else "Próximo bloco"}: ${b.title}",Modifier.weight(1f),color=Ink,fontSize=13.sp)
      Text("${b.startTime}–${b.endTime}",color=Violet,fontSize=12.sp)
     }
    }
    if (r.untimedEvents.isNotEmpty()) {
     TextButton(onPlanner) { Text("Compromissos sem horário (${r.untimedEvents.size})") }
    }
    if(r.priorityTasks.isNotEmpty()){
     Eyebrow("Tarefas prioritárias",Blue)
     r.priorityTasks.forEach{t->
      val parent=s.paths.find{it.id==t.pathId}
      Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
       Checkbox(checked=t.completed,onCheckedChange = { checked -> vm.run { vm.repo.toggleTask(t.id, checked) } },modifier=Modifier.size(48.dp).semantics{contentDescription="Alternar ${t.title}"})
       Column(Modifier.weight(1f).clickable(enabled=parent!=null){parent?.let{onOpenPath(it.id)}}){
        Text(t.title,color=Ink,fontSize=13.sp)
        Text("${parent?.name?:"Avulsa"}${if(t.deadline.isNotBlank())" · até ${displayDate(t.deadline)}" else ""}",color=if(isTaskOverdue(t))MaterialTheme.colorScheme.error else Muted,fontSize=11.sp)
       }
      }
     }
    }
    if(r.upcomingPaths.isNotEmpty()){
     Eyebrow("Caminhos em curso",Gold)
     r.upcomingPaths.forEach{p->
      val prog=pathProgress(s,p.id)
      Row(Modifier.fillMaxWidth().clickable{onOpenPath(p.id)}.padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
       Text(p.name,Modifier.weight(1f),color=Ink,fontSize=13.sp)
       Text("${prog.done}/${prog.total}",color=Gold,fontSize=12.sp)
      }
     }
    }
    FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
     TextButton(onPlanner){Text("Agenda 2.0",fontSize=12.sp)}
     TextButton(onPending){Text("Pendências",fontSize=12.sp)}
     TextButton(onPaths){Text("Caminhos",fontSize=12.sp)}
     TextButton(onInbox){Text("Entrada (${r.inboxCount})",fontSize=12.sp)}
    }
   }
  }
 }
}
@Composable fun WeekStrip(s:GameState,day:String){
    val end=LocalDate.parse(day);val start=end.with(DayOfWeek.MONDAY)
    Panel(accent=Green){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Eyebrow("Sua semana",Green);Text("Consistência, sem pressa",color=Muted,fontSize=10.sp)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
            repeat(7){i->val d=start.plusDays(i.toLong()).toString();val due=s.due(d);val done=due.count{s.record(it.id,d)?.complete==true};val rest=d in s.restDates;val isToday=d==day;val color=if(isToday)Gold else if(done>0)Green else Muted
                val cellShape=CutCornerShape(topStart=5.dp,topEnd=2.dp,bottomEnd=5.dp,bottomStart=2.dp)
                Column(Modifier.weight(1f).clip(cellShape).background(if(isToday)Gold.copy(alpha=.14f) else if(done>0)Green.copy(alpha=.08f) else CardColor.copy(alpha=.45f)).border(1.dp,if(isToday)Gold.copy(alpha=.65f) else if(done>0)Green.copy(alpha=.35f) else ColdIron.copy(alpha=.22f),cellShape).padding(vertical=10.dp).semantics(mergeDescendants=true){contentDescription="$d: $done de ${due.size} hábitos${if(rest)", descanso" else ""}"},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)){
                    Text(weekdays[i],color=if(isToday)Gold else Muted,fontSize=10.sp);Text(start.plusDays(i.toLong()).dayOfMonth.toString(),color=color,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
                    Text(if(rest)"☽" else if(done==due.size&&done>0)"✓" else if(done>0)"·" else "—",color=color,fontSize=12.sp)
                }
            }
        }
    }
}
