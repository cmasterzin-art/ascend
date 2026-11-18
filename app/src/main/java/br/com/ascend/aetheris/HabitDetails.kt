package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import java.time.LocalDate

@Composable fun HabitDetails(h:Habit,s:GameState,onDismiss:()->Unit,onEdit:()->Unit,onRecord:(HabitRecord)->Unit,actions:@Composable ColumnScope.()->Unit={}){
    var analysis by remember{mutableStateOf(false)}
    val records=s.records.filter{it.habitId==h.id&&it.progress>0}.sortedByDescending{it.date}
    val rests=s.restDates;val color=habitColor(h.color)
    FullForm("Detalhes do hábito",onDismiss,onEdit,"Editar"){
        Eyebrow("${h.category} · ${attributes[h.attribute]}",color)
        Text("${glyphs[h.icon]} ${h.name}",style=MaterialTheme.typography.headlineMedium)
        if(h.narrativeName.isNotBlank())Text(h.narrativeName,color=Gold,fontFamily=SagaFont)
        if(h.description.isNotBlank())Text(h.description,color=Muted)
        Panel(accent=color){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Stat("${streak(h,s.records,restDates=rests,plans=s.plans)}","SEQUÊNCIA",color);Stat("${bestStreak(h,s.records,restDates=rests,plans=s.plans)}","RECORDE",Green);Stat("${records.count{it.complete}}","CONCLUSÕES",Blue)}
            Text(if(h.trackStreak)"Sequências contam somente os dias programados. Descansos planejados são preservados." else "Contagem de sequências desativada. Seu progresso e suas recompensas continuam registrados.",color=Muted,fontSize=12.sp)
        }
        Panel(accent=color){Eyebrow("Sua programação",color);Text("${h.days.split(',').mapNotNull{it.toIntOrNull()}.joinToString(" · "){listOf("Seg","Ter","Qua","Qui","Sex","Sáb","Dom")[it-1]}} às ${h.time}",color=Ink);Text("Meta: ${h.target} ${h.unit} · ${if(h.proportional)"recompensa proporcional" else "recompensa ao atingir a meta"}",color=Muted,fontSize=13.sp);Text("+${h.xp} XP · ◈ ${h.coins} · +${h.points} ${attributes[h.attribute]}",color=color,fontSize=13.sp)}
        Eyebrow("Últimos 14 dias",color)
        val days=(13 downTo 0).map{LocalDate.now().minusDays(it.toLong()).toString()}
        val cellShape=androidx.compose.foundation.shape.CutCornerShape(4.dp)
        days.chunked(7).forEach{week->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){week.forEach{date->val r=s.record(h.id,date);val comp=r?.complete==true;val label=when{date in rests->"☽";comp->"✓";!scheduled(h,date,rests,s.plans)->"—";else->"·"};Box(Modifier.weight(1f).background(if(comp)Green.copy(alpha=.12f) else CardColor,cellShape).border(1.dp,if(comp)Green.copy(alpha=.45f) else ColdIron.copy(alpha=.22f),cellShape).padding(vertical=10.dp),contentAlignment=Alignment.Center){Text("${date.takeLast(2)}\n$label",color=if(comp)Green else Muted,fontSize=12.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}}}
        Eyebrow("Registros e recompensas",Blue)
        if(records.isEmpty())Empty("O histórico começa com a sua primeira ação.")
        records.take(30).forEach{r->Panel(accent=Blue){Text(displayDate(r.date),color=Muted,fontSize=12.sp);Text("${r.progress}/${r.target} ${r.unit}",fontFamily=FontFamily.Serif,fontSize=21.sp);Text("${Progression.reward(r.xp,r.progress,r.target,r.proportional)} XP · recompensa original ${r.xp} XP",color=Blue,fontSize=12.sp);TextButton({onRecord(r)}){Text("Editar ou desfazer registro")}}}
        if(records.size>30)Text("Mostrando os 30 registros mais recentes. Consulte todos no Histórico.",color=Muted,fontSize=12.sp)
        SagaButton("Análises deste hábito",{analysis=true},Modifier.fillMaxWidth(),secondary=true)
        actions()
    }
    if(analysis)FullForm("Análise do hábito",{analysis=false},{analysis=false},"Fechar"){AnalysisContent(s,h)}
}
