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

 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){MetricPair("${summary.completed}","atividades concluídas",summary.rate?.let{"$it%"}?:"—","taxa das previstas");Text("${summary.scheduledCompleted}/${summary.planned} previstas · extras: ${summary.completed-summary.scheduledCompleted} · ${data.plannedDays} ${if(data.plannedDays==1)"dia com atividades previstas" else "dias com atividades previstas"}",color=Muted,fontSize=12.sp);Hairline();MetricPair("${summary.xp}","XP no período","${summary.minutes}","minutos em hábitos");Text("${previous.completed} ${if(previous.completed==1)"conclusão" else "conclusões"} no período anterior de ${p.days} dias · ${summary.completed-previous.completed} de diferença",color=Muted,fontSize=13.sp)}
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
 fun sessionName(f:FocusSession)=f.habitName.ifBlank{f.targetLabel.ifBlank{"Sessão de projeto"}}
 LazyColumn(Modifier.fillMaxSize().testTag("focus_history_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{Title("Histórico de Foco","Sessões reais registradas em hábitos e caminhos.");AetherisTabs(listOf("7 dias","30 dias","90 dias","Semana civil","Mês civil"),choice,{choice=it})}
  item{AetherSurface{MetricPair("${sessions.sumOf{it.elapsedSeconds}/60}","minutos efetivos","${sessions.size}","sessões no período");Text("Média: ${if(sessions.isEmpty())"—" else "${sessions.sumOf{it.elapsedSeconds}/sessions.size/60} min"} · ${sessions.count{it.status=="completed"}} concluídas",color=Muted,fontSize=13.sp);val prev=s.focusSessions.filter{it.date in p.previous.start..p.previous.end};Text("Período anterior: ${prev.sumOf{it.elapsedSeconds}/60} min. Sessões canceladas são visíveis e não concedem XP.",color=Muted,fontSize=12.sp)}}
  if(s.focus!=null)item{StatusSeal("Há uma sessão em andamento. Retome pela Jornada.",Blue)}
  if(sessions.isEmpty())item{Empty("Nenhuma sessão detalhada neste período. Seus registros antigos de minutos continuam nas análises de hábitos.")}
  item{sessions.groupBy{it.habitId.ifBlank{it.taskId.ifBlank{it.pathId.ifBlank{it.targetLabel}}}}.forEach{(_,rows)->Text("${sessionName(rows.first())} · ${rows.sumOf{it.elapsedSeconds}/60} min",Modifier.padding(vertical=4.dp),color=Blue,fontSize=13.sp)}}
  items(sessions.sortedByDescending{it.endedAt},key={it.id}){f->val label=sessionName(f);AetherSurface(modifier=Modifier.clickable{detail=f}){SectionHeader(label,"${displayDate(f.date)}${if(f.habitId.isBlank())" · Caminho/Tarefa (sem XP)" else ""}");MetricPair("${f.elapsedSeconds/60} min","tempo efetivo","${f.plannedMinutes} min","duração prevista");StatusSeal(when(f.status){"completed"->"Concluída";"cancelled"->"Cancelada · sem registro";else->"Encerrada antes do tempo"},if(f.registered)Green else Muted);TextButton({detail=f}){Text("Detalhes da sessão")}}}
 }
 detail?.let{f->val label=sessionName(f);AetherDialog(onDismissRequest={detail=null},title=label,confirmButton={TextButton({detail=null}){Text("Fechar")}}){Text(displayDate(f.date));if(f.habitId.isBlank())Text("Sessão vinculada a caminho ou tarefa · sem XP extra.",color=Muted,fontSize=12.sp);val format=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");Text("Início: ${Instant.ofEpochMilli(f.startedAt).atZone(ZoneId.systemDefault()).format(format)}");Text("Término: ${Instant.ofEpochMilli(f.endedAt).atZone(ZoneId.systemDefault()).format(format)}");Text("${f.elapsedSeconds} segundos efetivos. Pausas não contam. O histórico não concede XP adicional.");Text("ID: ${f.id}",fontSize=11.sp,color=Muted)}}
}
