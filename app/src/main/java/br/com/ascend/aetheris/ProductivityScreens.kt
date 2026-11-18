package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import java.time.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

@Composable fun PlannerScreen(s:GameState,vm:GameViewModel,onRecord:(Habit,HabitRecord?)->Unit){
 var week by rememberSaveable{mutableStateOf(weekOf(today()))};var selected by rememberSaveable{mutableStateOf(today())};var editing by remember{mutableStateOf<Habit?>(null)}
 val start=LocalDate.parse(week);val days=(0..6).map{start.plusDays(it.toLong()).toString()};val due=s.due(selected).sortedWith(compareBy<Habit>{it.time.ifBlank{"99:99"}}.thenBy{it.position});val done=due.count{s.record(it.id,selected)?.complete==true}
 LazyColumn(Modifier.fillMaxSize().testTag("planner_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{Title("Planejador Semanal","Sua agenda real, um dia de cada vez.")}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("‹",{week=start.minusWeeks(1).toString();selected=week},Modifier.weight(1f).semantics{contentDescription="Semana anterior"},secondary=true);SagaButton("Hoje",{week=weekOf(today());selected=today()},Modifier.weight(1f),secondary=true);SagaButton("›",{week=start.plusWeeks(1).toString();selected=week},Modifier.weight(1f).semantics{contentDescription="Próxima semana"},secondary=true)}}
  item{SectionHeader(if(week==weekOf(today()))"Semana atual" else "Semana selecionada",displayPeriod(week,days.last()));Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){days.forEach{date->val d=LocalDate.parse(date);val active=selected==date;Column(Modifier.width(68.dp).heightIn(min=84.dp).background(if(active)Blue.copy(alpha=.18f) else Surface,RoundedCornerShape(8.dp)).border(1.dp,if(active)Blue else ColdIron.copy(alpha=.2f),RoundedCornerShape(8.dp)).clickable{selected=date}.padding(10.dp).semantics(mergeDescendants=true){contentDescription="Selecionar ${displayDate(date)}";this.selected=active},horizontalAlignment=Alignment.CenterHorizontally){Text(listOf("SEG","TER","QUA","QUI","SEX","SÁB","DOM")[d.dayOfWeek.value-1],fontSize=11.sp,color=Muted);Text(d.dayOfMonth.toString(),fontFamily=SagaFont,fontSize=23.sp);Text(if(date in s.restDates)"Descanso" else "${s.due(date).count{s.record(it.id,date)?.complete==true}}/${s.due(date).size}",fontSize=10.sp,color=if(active)Blue else Muted)}}}}
  item{Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){SectionHeader(displayDate(selected),"${days.sumOf{date->s.due(date).count{s.record(it.id,date)?.complete==true}}}/${days.sumOf{s.due(it).size}} atividades na semana selecionada");MetricPair("$done/${due.size}","concluídas neste dia",if(due.isEmpty())"—" else "${done*100/due.size}%","cumprimento");Meter(if(due.isEmpty())0f else done.toFloat()/due.size,Blue,slim=true);if(selected>=today())Toggle("Descanso global neste dia",selected in s.restDates,{v->vm.run{vm.repo.rest(selected,v)}});if(selected in s.restDates)Text("O descanso tem prioridade sobre inclusões pontuais. As exceções ficam guardadas.",color=Muted,fontSize=13.sp);Hairline()}}
  item{SectionHeader("Agenda do dia",if(selected<today())"Histórico de programação · sem ajustes retroativos" else "Horários do hábito · ajustes válidos só nesta data")}
  if(due.isEmpty())item{Empty("Nenhuma atividade prevista. Você pode incluir um hábito ativo ou reservar este dia para descanso.")}
  items(due,key={it.id}){h->AetherSurface{Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Text(h.time.ifBlank{"Sem horário"},color=Blue,fontSize=12.sp);Column(Modifier.weight(1f)){Text(h.name,style=MaterialTheme.typography.titleMedium);Text(if(s.record(h.id,selected)?.complete==true)"Concluído" else "${h.target} ${h.unit} · ${h.xp} XP",fontSize=12.sp,color=Muted)}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){if(selected<=today())SagaButton("Registrar",{onRecord(h,s.record(h.id,selected)?:HabitRecord("${h.id}@$selected",h.id,selected,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name))},Modifier.weight(1f));if(selected>=today())SagaButton("Ajustar",{editing=h},Modifier.weight(1f),secondary=true)}}}
  if(selected>=today()){
   item{SectionHeader("Ajustes pontuais","Incluir, dispensar ou voltar à programação normal. Não concede XP.")}
   items(s.habits.filter{!it.paused&&!it.archived&&!it.deleted&&it.created<=selected},key={"adjust:${it.id}"}){h->val exception=s.plans.find{it.habitId==h.id&&it.date==selected};Row(Modifier.fillMaxWidth().heightIn(min=56.dp).clickable{editing=h},verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(h.name,fontSize=14.sp);Text(when(exception?.include){true->"Incluído nesta data";false->"Dispensado nesta data";null->"Programação normal"},color=Muted,fontSize=12.sp)};TextButton({editing=h}){Text("Ajustar ${h.name}")}}}
  }
 }
 editing?.let{h->AlertDialog(onDismissRequest={editing=null},title={Text(h.name)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Somente ${displayDate(selected)}. Pausa, arquivo, exclusão e descanso continuam com prioridade.",color=Muted);listOf("Programação normal","Incluir nesta data","Dispensar nesta data").forEachIndexed{i,label->SagaButton(label,{vm.run{vm.repo.plan(h.id,selected,i);editing=null}},Modifier.fillMaxWidth(),secondary=i==0)}}},confirmButton={TextButton({editing=null}){Text("Cancelar")}})}
}

@Composable fun AnalysisContent(s:GameState,requested:Habit?=null,onHabit:(Habit)->Unit={}){
 val habit=requested?.let{h->s.habits.find{it.id==h.id}?:h}
 var choice by rememberSaveable{mutableIntStateOf(0)};val day=currentDay();val p=remember(choice,day){analysisPeriod(choice,day)}
 val scoped=remember(s,habit?.id){if(habit==null)s else s.copy(habits=listOf(habit),records=s.records.filter{it.habitId==habit.id},ledger=s.ledger.filter{it.id.startsWith("habit:${habit.id}@")})}
 var snapshot by remember{mutableStateOf<AnalyticsSnapshot?>(null)}
 LaunchedEffect(scoped,p){snapshot=null;snapshot=withContext(Dispatchers.Default){analyticsSnapshot(scoped,p)}}

 Title(if(habit==null)"Central de Análises" else "Análise · ${habit.name}","Informações reais, sem previsões ou julgamentos.")
 AetherisTabs(listOf("7 dias","30 dias","90 dias","Semana civil","Mês civil"),choice,{choice=it})
 Text("${p.label} · ${displayPeriod(p.start,p.end)}",color=Blue,fontSize=12.sp)
 val data=snapshot?.takeIf{it.source===scoped&&it.period==p}
 if(data==null){LinearProgressIndicator(Modifier.fillMaxWidth());Text("Reunindo os registros do período…",color=Muted);return}
 val summary=data.summary;val previous=data.previous

 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){MetricPair("${summary.completed}","atividades concluídas",summary.rate?.let{"$it%"}?:"—","taxa das previstas");Text("${summary.scheduledCompleted}/${summary.planned} previstas · extras: ${summary.completed-summary.scheduledCompleted} · ${data.plannedDays} dias com atividades previstas",color=Muted,fontSize=12.sp);Hairline();MetricPair("${summary.xp}","XP no período","${summary.minutes}","minutos em hábitos");Text("${previous.completed} conclusões no período anterior de ${p.days} dias · ${summary.completed-previous.completed} de diferença",color=Muted,fontSize=13.sp)}
 val daily=data.daily
 AetherSurface{SectionHeader("Evolução de XP","${displayDate(p.start)} → ${displayDate(p.end)} · saldo diário real");XpChart(daily);Text("Escala: 0 a ${(daily.maxOrNull()?:0f).toLong().coerceAtLeast(1)} XP",color=Muted,fontSize=12.sp);var table by remember{mutableStateOf(false)};TextButton({table=!table}){Text(if(table)"Ocultar valores" else "Consultar valores por data")};if(table)daily.forEachIndexed{i,v->Text("${displayDate(LocalDate.parse(p.start).plusDays(i.toLong()).toString())} · ${v.toLong()} XP",fontSize=12.sp)}}
 if(habit!=null){val a=data.habits.first{it.habit.id==habit.id};AetherSurface{SectionHeader("Consistência do hábito");MetricPair("${streak(habit,s.records,restDates=s.restDates,plans=s.plans)}","sequência atual","${bestStreak(habit,s.records,restDates=s.restDates,plans=s.plans)}","melhor sequência");Text("${habit.days.split(',').size} dias recorrentes por semana; exceções e descansos entram na taxa do período.",color=Muted,fontSize=13.sp);Text("${a.minutes} minutos registrados · ${a.xp} XP do hábito",color=Blue);SectionHeader("Progresso recente");s.records.filter{it.habitId==habit.id&&it.date in p.start..p.end}.take(30).forEach{r->Text("${displayDate(r.date)} · ${r.progress}/${r.target} ${r.unit}",fontSize=13.sp)}}}
 else{
  Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)){SectionHeader("Distribuição por atributo");attributes.forEachIndexed{i,name->val n=scoped.records.count{it.complete&&it.attribute==i&&it.date in p.start..p.end};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,color=habitColor(i));Text("$n",color=Muted)};Meter(n.toFloat()/summary.completed.coerceAtLeast(1),habitColor(i),slim=true)}}
  Hairline()
  val rows=data.habits.filter{it.planned>0}.sortedByDescending{it.rate}
  Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)){SectionHeader("Cumprimento por hábito","As dez maiores taxas. Toque para consultar o histórico individual.");if(rows.isEmpty())Empty("Nenhuma atividade prevista neste período.");rows.take(10).forEach{a->Column(Modifier.fillMaxWidth().clickable{onHabit(a.habit)}.padding(vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text(a.habit.name,style=MaterialTheme.typography.titleMedium);Text("${a.scheduledComplete}/${a.planned} previstas · ${a.rate}%",color=Muted,fontSize=12.sp);Meter((a.rate?:0)/100f,Blue,slim=true)}};rows.lastOrNull()?.let{Text("Menor taxa no período: ${it.habit.name} (${it.rate}%). Esse indicador é descritivo, sem penalidade.",color=Muted,fontSize=12.sp)}}
 }
 Hairline()
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){SectionHeader("Leitura dos seus dados");data.insights.forEach{ReadingBlock(it)};Text("Dias dispensados e descansos ficam fora da taxa. Atividades extras conservam recompensas e aparecem nos totais.",color=Muted,fontSize=12.sp)}
}
@Composable fun AnalysisScreen(s:GameState){var selected by remember{mutableStateOf<Habit?>(null)};Page{AnalysisContent(s,onHabit={selected=it})};selected?.let{h->FullForm("Análise do hábito",{selected=null},{selected=null},"Fechar"){AnalysisContent(s,h)}}}
@Composable fun FocusHistoryScreen(s:GameState){
 var choice by rememberSaveable{mutableIntStateOf(0)};var detail by remember{mutableStateOf<FocusSession?>(null)};val p=analysisPeriod(choice,currentDay());val sessions=remember(s.focusSessions,p){s.focusSessions.filter{it.date in p.start..p.end}}
 LazyColumn(Modifier.fillMaxSize().testTag("focus_history_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{Title("Histórico de Foco","Sessões reais registradas desde a versão 1.6.");AetherisTabs(listOf("7 dias","30 dias","90 dias","Semana civil","Mês civil"),choice,{choice=it})}
  item{AetherSurface{MetricPair("${sessions.sumOf{it.elapsedSeconds}/60}","minutos efetivos","${sessions.size}","sessões no período");Text("Média: ${if(sessions.isEmpty())"—" else "${sessions.sumOf{it.elapsedSeconds}/sessions.size/60} min"} · ${sessions.count{it.status=="completed"}} concluídas",color=Muted,fontSize=13.sp);val prev=s.focusSessions.filter{it.date in p.previous.start..p.previous.end};Text("Período anterior: ${prev.sumOf{it.elapsedSeconds}/60} min. Sessões canceladas são visíveis e não concedem XP.",color=Muted,fontSize=12.sp)}}
  if(s.focus!=null)item{StatusSeal("Há uma sessão em andamento. Retome pela Jornada.",Blue)}
  if(sessions.isEmpty())item{Empty("Nenhuma sessão detalhada neste período. Seus registros antigos de minutos continuam nas análises de hábitos.")}
  item{sessions.groupBy{it.habitId}.forEach{(_,rows)->Text("${rows.first().habitName} · ${rows.sumOf{it.elapsedSeconds}/60} min",Modifier.padding(vertical=4.dp),color=Blue,fontSize=13.sp)}}
  items(sessions.sortedByDescending{it.endedAt},key={it.id}){f->AetherSurface(modifier=Modifier.clickable{detail=f}){SectionHeader(f.habitName,displayDate(f.date));MetricPair("${f.elapsedSeconds/60} min","tempo efetivo","${f.plannedMinutes} min","duração prevista");StatusSeal(when(f.status){"completed"->"Concluída";"cancelled"->"Cancelada · sem registro";else->"Encerrada antes do tempo"},if(f.registered)Green else Muted);TextButton({detail=f}){Text("Detalhes da sessão")}}}
 }
 detail?.let{f->AlertDialog(onDismissRequest={detail=null},title={Text(f.habitName)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text(displayDate(f.date));val format=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");Text("Início: ${Instant.ofEpochMilli(f.startedAt).atZone(ZoneId.systemDefault()).format(format)}");Text("Término: ${Instant.ofEpochMilli(f.endedAt).atZone(ZoneId.systemDefault()).format(format)}");Text("${f.elapsedSeconds} segundos efetivos. Pausas não contam. O histórico não concede XP adicional.");Text("ID: ${f.id}",fontSize=11.sp,color=Muted)}},confirmButton={TextButton({detail=null}){Text("Fechar")}})}
}
