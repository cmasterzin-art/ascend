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
    val records=s.records.filter{it.habitId==h.id&&it.progress>0}.sortedByDescending{it.date}
    val rests=s.restDates;val color=habitColor(h.color)
    FullForm("Detalhes do hábito",onDismiss,onEdit,"Editar"){
        Eyebrow("${h.category} · ${attributes[h.attribute]}",color)
        Text("${glyphs[h.icon]} ${h.name}",style=MaterialTheme.typography.headlineMedium)
        if(h.description.isNotBlank())Text(h.description,color=Muted)
        Panel(accent=color){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Stat("${streak(h,s.records,restDates=rests)}","SEQUÊNCIA",color);Stat("${bestStreak(h,s.records,restDates=rests)}","RECORDE",Green);Stat("${records.count{it.complete}}","CONCLUSÕES",Blue)}
            Text("Sequências contam somente os dias programados. Descansos planejados são preservados.",color=Muted,fontSize=12.sp)
        }
        Panel(accent=color){Eyebrow("Sua programação",color);Text("${h.days.split(',').mapNotNull{it.toIntOrNull()}.joinToString(" · "){listOf("Seg","Ter","Qua","Qui","Sex","Sáb","Dom")[it-1]}} às ${h.time}",color=Ink);Text("Meta: ${h.target} ${h.unit} · ${if(h.proportional)"recompensa proporcional" else "recompensa ao atingir a meta"}",color=Muted,fontSize=13.sp);Text("+${h.xp} XP · ◈ ${h.coins} · +${h.points} ${attributes[h.attribute]}",color=color,fontSize=13.sp)}
        Eyebrow("Últimos 14 dias",color)
        val days=(13 downTo 0).map{LocalDate.now().minusDays(it.toLong()).toString()}
        days.chunked(7).forEach{week->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){week.forEach{date->val r=s.record(h.id,date);val label=when{date in rests->"☽";r?.complete==true->"✓";!scheduled(h,date,rests)->"—";else->"·"};Box(Modifier.weight(1f).background(CardColor,RoundedCornerShape(10.dp)).padding(vertical=10.dp),contentAlignment=Alignment.Center){Text("${date.takeLast(2)}\n$label",color=if(r?.complete==true)Green else Muted,fontSize=12.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}}}
        Eyebrow("Registros e recompensas",Blue)
        if(records.isEmpty())Empty("O histórico começa com a sua primeira ação.")
        records.take(30).forEach{r->Panel(accent=Blue){Text(r.date,color=Muted,fontSize=12.sp);Text("${r.progress}/${r.target} ${r.unit}",fontFamily=FontFamily.Serif,fontSize=21.sp);Text("${Progression.reward(r.xp,r.progress,r.target,r.proportional)} XP · recompensa original ${r.xp} XP",color=Blue,fontSize=12.sp);TextButton({onRecord(r)}){Text("Editar ou desfazer registro")}}}
        if(records.size>30)Text("Mostrando os 30 registros mais recentes. Consulte todos no Histórico.",color=Muted,fontSize=12.sp)
        actions()
    }
}
