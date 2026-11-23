package br.com.ascend.aetheris

import androidx.compose.animation.AnimatedVisibility
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

@OptIn(ExperimentalLayoutApi::class)
@Composable fun Journey(s:GameState,onProgress:(Habit,HabitRecord?)->Unit,onEdit:(Habit)->Unit,onFocus:(Habit)->Unit,onAdd:()->Unit,onJournal:()->Unit,vm:GameViewModel,relaxed:Boolean,onPlanner:()->Unit={},onAnalysis:()->Unit={},onHistory:()->Unit={},onPortrait:()->Unit={},onMissions:()->Unit={},onRegion:()->Unit={},onPaths:()->Unit={},onInbox:()->Unit={},onPending:()->Unit={},onOpenPath:(String)->Unit={},onMimirReview:()->Unit={},onOdin:()->Unit={},onTimeForge:()->Unit={},onCouncilNine:()->Unit={}){
 val now=currentLocalMinute();val day=now.toLocalDate().toString();val rested=day in s.restDates
 val due=if(relaxed&&!rested)s.habits.filter{!it.deleted&&!it.archived&&!it.paused}.sortedBy{it.position} else s.due(day)
 val done=due.count{s.record(it.id,day)?.complete==true};var pendingOnly by remember{mutableStateOf(false)};var detail by remember{mutableStateOf<Habit?>(null)}
  val inboxCount=s.inboxItems.count{!it.archived}
  val dueCardsCount=remember(s.notebooks,s.flashcards,day){dueFlashcards(s,day).size}
  val rumos=remember(s,now){rumosDoDia(s,day,now.toLocalTime())}
  val hasRumos=rumos.nextEvent!=null||rumos.nextBlock!=null||rumos.untimedEvents.isNotEmpty()||rumos.priorityTasks.isNotEmpty()||rumos.upcomingPaths.isNotEmpty()||rumos.inboxCount>0
  var rumosExpanded by androidx.compose.runtime.saveable.rememberSaveable{mutableStateOf(false)}
  val listState = androidx.compose.foundation.lazy.rememberLazyListState()
  LaunchedEffect(s.focus?.sessionId) {
   if (s.focus != null) listState.scrollToItem(0)
  }
  val nb = LocalNorthborn.current
  EngravedBackdrop {
   androidx.compose.foundation.lazy.LazyColumn(
       Modifier.fillMaxSize().testTag("journey_list").semantics{contentDescription="Central da Jornada"},
       state=listState,
       contentPadding=PaddingValues(horizontal=20.dp, vertical=16.dp),
       verticalArrangement=Arrangement.spacedBy(11.dp)
   ){
    s.focus?.let { focus -> item(key="active-focus") { FocusCard(focus,s,vm) } }
    item {
     Column {
      CompactHero(s, day, onPortrait)
      DayLedger(done, due.size, rested, relaxed)
     }
    }
    item {
     Column {
      RebirthRule()
      FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
       listOf(
        Triple("Mesa de Odin", Engraving.Council, onOdin),
        Triple("Forja do Tempo", Engraving.Forge, onTimeForge),
        Triple("Planejador", Engraving.Calendar, onPlanner)
       ).forEach { (label, glyph, action) ->
        TextButton(action, Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
         EngravedIcon(glyph, nb.accentBronze, size = 21.dp)
         Spacer(Modifier.width(4.dp))
         Text(label, color = nb.inkPrimary, style = MaterialTheme.typography.labelMedium)
        }
       }
      }
      RebirthRule()
     }
    }
    item {
     val destinations = listOf(
      Triple("Caminhos", Engraving.Cairns, onPaths),
      Triple("Conselho 9", Engraving.Council, onCouncilNine),
      Triple("Entrada${if(inboxCount>0)" ($inboxCount)" else ""}", Engraving.Tally, onInbox),
      Triple("Pendências", Engraving.Banner, onPending),
      Triple("Análises", Engraving.Analysis, onAnalysis),
      Triple("Foco", Engraving.Focus, onHistory)
     )
     Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
      listOf(destinations.first(), destinations[4], destinations[5]).forEach { (label, _, action) ->
       TextButton(action, Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(horizontal = 6.dp)) {
        Text(label, color = nb.inkPrimary)
       }
      }
      var showDestinations by remember { mutableStateOf(false) }
      Box {
       IconButton({ showDestinations = true }) { Icon(Icons.Default.MoreVert, "Todos os destinos") }
       DropdownMenu(showDestinations, { showDestinations = false }) {
        destinations.forEach { (label, engraving, action) ->
         DropdownMenuItem(
          text = { Text(label) },
          onClick = { showDestinations = false; action() },
          leadingIcon = { EngravedIcon(engraving, nb.accentBronze, size = 20.dp) }
         )
        }
       }
      }
     }
    }
   item{RumosDoDiaPanel(rumos,s,vm,expanded=rumosExpanded||hasRumos&&rumosExpanded,hasRumos=hasRumos,dueCardsCount=dueCardsCount,onToggle={rumosExpanded=!rumosExpanded},onPaths=onPaths,onOpenPath=onOpenPath,onPlanner=onPlanner,onInbox=onInbox,onPending=onPending,onMimirReview=onMimirReview)}
   item{
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
     NorthbornEditorialHeader(
      title = "Seus próximos passos",

      action = {
       IconButton(
        onClick = onAdd,
        modifier = Modifier
         .size(48.dp)
         .clip(NorthbornShape.md)
         .background(nb.accentBronze.copy(alpha = if (nb.dark) 0.20f else 0.14f))
         .border(1.dp, nb.accentBronze.copy(alpha = 0.65f), NorthbornShape.md)
         .semantics { contentDescription = "Criar hábito" }
       ){
        EngravedIcon(Engraving.Add, if (nb.dark) nb.accentPaleGold else nb.accentBronze, size = 24.dp)
       }
      }
     )
     if(due.size>1||done>0)ChipRow(listOf("Todos (${due.size})","Pendentes (${due.size-done})"),if(pendingOnly)1 else 0,{pendingOnly=it==1})
    }
   }
   val visible=due.filter{!pendingOnly||s.record(it.id,day)?.complete!=true}.sortedBy{s.record(it.id,day)?.complete==true}
   if(visible.isEmpty())item{Empty(if(rested)"Este dia está reservado para descanso. Seus registros e recompensas anteriores continuam guardados." else if(due.isEmpty())"Nenhum hábito para hoje. Abra o Planejador ou crie seu primeiro passo." else "Tudo feito por hoje. Aetheris pode esperar pelo seu próximo passo.")}
   items(visible.size,key={visible[it].id}){i->val h=visible[i];HabitCard(h,s.record(h.id,day),{if(h.type==0&&s.record(h.id,day)?.complete!=true)vm.run{vm.repo.progress(h,day,1)}else onProgress(h,s.record(h.id,day))},{detail=h},{onFocus(h)},editorial = true)}
   item{WeekStrip(s,day)}
   item{
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
     EngravedDivider(EngravedRail.Council)
     NorthbornEditorialHeader("Além da rotina", kicker = "Horizonte")
     Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
      NorthbornButton("Missões",onMissions,role=NorthbornButtonRole.Secondary,modifier=Modifier.weight(1f),leadingIcon=NorthbornGlyph.Missions)
      NorthbornButton("Região atual",onRegion,role=NorthbornButtonRole.Secondary,modifier=Modifier.weight(1f),leadingIcon=NorthbornGlyph.World)
     }
    }
   }
   item{
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
     NarrativePanel(s,vm)
     NorthbornButton("Escrever no diário", onJournal, role = NorthbornButtonRole.Tertiary)
    }
   }
  }
  }
 detail?.let{h->HabitDetails(h,s,{detail=null},{detail=null;onEdit(h)},{r->detail=null;onProgress(h,r)})}
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun RumosDoDiaPanel(r:RumosDoDia,s:GameState,vm:GameViewModel,expanded:Boolean,hasRumos:Boolean,dueCardsCount:Int=0,onToggle:()->Unit,onPaths:()->Unit,onOpenPath:(String)->Unit,onPlanner:()->Unit,onInbox:()->Unit,onPending:()->Unit,onMimirReview:()->Unit={}){
 val nb=LocalNorthborn.current
 Column(Modifier.fillMaxWidth().testTag("rumos_do_dia")) {
  RebirthRule()
  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
   Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
    Text("Rumos do Dia", style = MaterialTheme.typography.titleMedium, color = nb.inkPrimary)
    val summary = when {
     expanded && hasRumos -> "${r.pendingTodayCount} hoje · ${r.overdueCount} atrasadas · ${r.plannedBlocksCount} blocos · ${r.inboxCount} na entrada"
     !hasRumos -> "Agenda livre"
     r.overdueCount > 0 -> "${r.overdueCount} ${if (r.overdueCount == 1) "tarefa atrasada" else "tarefas atrasadas"}"
     else -> "${r.plannedBlocksCount} blocos · ${r.pendingTodayCount} pendentes"
    }
    Text(summary, color = if (r.overdueCount > 0) nb.accentEmber else nb.inkSecondary, fontSize = 12.sp)
   }
   TextButton(onToggle, Modifier.heightIn(min = 48.dp)) { Text(if (expanded) "Recolher" else "Abrir rumos") }
  }
  if (dueCardsCount > 0) {
   TextButton(onMimirReview, Modifier.heightIn(min = 48.dp), contentPadding = PaddingValues(end = 8.dp)) {
    Text("Runas da Memória · $dueCardsCount ${if (dueCardsCount == 1) "revisão" else "revisões"}", color = nb.accentFjord)
   }
  }
  val reduced = LocalAppearance.current.reducedMotion
  AnimatedVisibility(expanded, enter = craftSectionEnter(reduced), exit = craftSectionExit(reduced)) {
   Column {
    NorthbornDivider(NorthbornDividerStyle.Hairline)
    if(!hasRumos){
     Text("Sua agenda e caminhos estão leves hoje. Capture uma ideia rápida ou planeje um novo caminho.",color=nb.inkSecondary,fontSize=12.5.sp)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
      NorthbornButton("Abrir Caminhos",onPaths,role=NorthbornButtonRole.Secondary,modifier=Modifier.weight(1f))
      NorthbornButton("Capturar",onInbox,role=NorthbornButtonRole.Secondary,modifier=Modifier.weight(1f))
     }
    }else{
     r.nextEvent?.let{ev->
      Row(Modifier.fillMaxWidth().clickable(onClick=onPlanner),horizontalArrangement=Arrangement.SpaceBetween){
       Text("${if(r.eventInProgress) "Compromisso em andamento" else "Próximo compromisso"}: ${ev.title}",Modifier.weight(1f),color=nb.inkPrimary,fontSize=13.sp)
       if(ev.startTime.isNotBlank())Text(ev.startTime,color=nb.accentFjord,fontSize=12.sp)
      }
     }
     r.nextBlock?.let{b->
      Row(Modifier.fillMaxWidth().clickable(onClick=onPlanner),horizontalArrangement=Arrangement.SpaceBetween){
       Text("${if(r.blockInProgress) "Bloco em andamento" else "Próximo bloco"}: ${b.title}",Modifier.weight(1f),color=nb.inkPrimary,fontSize=13.sp)
       Text("${b.startTime}–${b.endTime}",color=Violet,fontSize=12.sp)
      }
     }
     if (r.untimedEvents.isNotEmpty()) {
      TextButton(onPlanner) { Text("Compromissos sem horário (${r.untimedEvents.size})", color = nb.accentBronze) }
     }
     if(r.priorityTasks.isNotEmpty()){
      Eyebrow("Tarefas prioritárias",nb.accentFjord)
      r.priorityTasks.forEach{t->
       val parent=s.paths.find{it.id==t.pathId}
       Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
        Checkbox(checked=t.completed,onCheckedChange = { checked -> vm.run { vm.repo.toggleTask(t.id, checked) } },modifier=Modifier.size(48.dp).semantics{contentDescription="Alternar ${t.title}"})
        Column(Modifier.weight(1f).clickable(enabled=parent!=null){parent?.let{onOpenPath(it.id)}}){
         Text(t.title,color=nb.inkPrimary,fontSize=13.sp)
         Text("${parent?.name?:"Avulsa"}${if(t.deadline.isNotBlank())" · até ${displayDate(t.deadline)}" else ""}",color=if(isTaskOverdue(t))nb.accentEmber else nb.inkSecondary,fontSize=11.5.sp)
        }
       }
      }
     }
     if(r.upcomingPaths.isNotEmpty()){
      Eyebrow("Caminhos em curso",nb.accentBronze)
      r.upcomingPaths.forEach{p->
       val prog=pathProgress(s,p.id)
       Row(Modifier.fillMaxWidth().clickable{onOpenPath(p.id)}.padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
        Text(p.name,Modifier.weight(1f),color=nb.inkPrimary,fontSize=13.sp)
        Text("${prog.done}/${prog.total}",color=nb.accentBronze,fontSize=12.sp)
       }
      }
     }
     FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
      NordicCompactAction("Agenda 3.0", onPlanner, accent = nb.accentFjord)
      NordicCompactAction("Pendências", onPending, accent = nb.accentBronze)
      NordicCompactAction("Caminhos", onPaths, accent = nb.inkPrimary)
      NordicCompactAction("Entrada (${r.inboxCount})", onInbox, accent = nb.inkSecondary)
     }
    }
   }
  }
 }
}
@Composable fun WeekStrip(s:GameState,day:String){
    val nb=LocalNorthborn.current
    val end=LocalDate.parse(day);val start=end.with(DayOfWeek.MONDAY)
    Column(Modifier.fillMaxWidth(), verticalArrangement=Arrangement.spacedBy(10.dp)){
        NorthbornDivider(NorthbornDividerStyle.Hairline)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Eyebrow("Sua semana", nb.accentPatina)
            Text("Consistência, sem pressa",color=nb.inkSecondary,fontSize=11.5.sp)
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
            repeat(7){i->
                val d=start.plusDays(i.toLong()).toString();val due=s.due(d);val done=due.count{s.record(it.id,d)?.complete==true};val rest=d in s.restDates;val isToday=d==day
                val color=if(isToday)nb.accentBronze else if(done>0)nb.accentPatina else nb.inkTertiary
                val cellShape=NorthbornShape.sm
                Column(
                    Modifier
                        .weight(1f)
                        .clip(cellShape)
                        .background(if(isToday)nb.accentBronze.copy(alpha=if(nb.dark).16f else .14f) else if(done>0)nb.accentPatina.copy(alpha=.10f) else nb.surfaceBase)
                        .border(if(isToday)1.2.dp else 1.dp,if(isToday)nb.accentBronze else if(done>0)nb.accentPatina.copy(alpha=.42f) else nb.hairline,cellShape)
                        .padding(vertical=10.dp)
                        .semantics(mergeDescendants=true){contentDescription="$d: $done de ${due.size} hábitos${if(rest)", descanso" else ""}"},
                    horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(6.dp)
                ){
                    Text(weekdays[i],color=if(isToday)nb.accentBronze else nb.inkSecondary,fontSize=10.sp)
                    Text(start.plusDays(i.toLong()).dayOfMonth.toString(),color=if(isToday)nb.inkPrimary else color,fontWeight=FontWeight.SemiBold,fontSize=14.sp)
                    Text(if(rest)"☽" else if(done==due.size&&done>0)"✓" else if(done>0)"·" else "—",color=color,fontSize=12.sp)
                }
            }
        }
    }
}
