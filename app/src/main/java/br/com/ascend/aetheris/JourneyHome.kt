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
@Composable fun Journey(s:GameState,onProgress:(Habit,HabitRecord?)->Unit,onEdit:(Habit)->Unit,onFocus:(Habit)->Unit,onAdd:()->Unit,onJournal:()->Unit,vm:GameViewModel,relaxed:Boolean){
    val day=currentDay();val date=LocalDate.parse(day);val p=s.player!!;val appearance=LocalAppearance.current
    val rested=day in s.restDates
    val due=if(relaxed&&!rested)s.habits.filter{!it.deleted&&!it.archived&&!it.paused}.sortedBy{it.position}else s.due(day)
    val done=due.count{s.record(it.id,day)?.complete==true}
    val xp=Progression.current(s.xp);val next=Progression.requirement(s.level)
    val earned=s.ledger.filter{it.date==day}.sumOf{it.xp}
    var pendingOnly by remember{mutableStateOf(false)}
    var detail by remember{mutableStateOf<Habit?>(null)}
    Page {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f)) { Eyebrow("ASCEND  /  MINHA JORNADA");Text(date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM",Locale.forLanguageTag("pt-BR"))),color=Muted,fontSize=12.sp) }
            Surface(shape=CutCornerShape(5.dp),color=Gold.copy(alpha=.1f)){Text("◈ ${s.coins}",Modifier.padding(horizontal=14.dp,vertical=10.dp),color=Gold,fontWeight=FontWeight.SemiBold,fontSize=13.sp)}
        }
        HeroSaga(s)
        Panel(accent=Green){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Column{Eyebrow("Hábitos de hoje",Green);Text(if(rested)"Descanso" else "$done de ${due.size}",fontSize=26.sp,fontFamily=FontFamily.Serif,color=Ink)}
                Column(horizontalAlignment=Alignment.End){Eyebrow("XP do dia");Text("+$earned",fontSize=26.sp,fontFamily=FontFamily.Serif,color=Gold)}
            }
            Meter(if(due.isEmpty())0f else done.toFloat()/due.size,Green)
        }
        s.focus?.let{FocusCard(it,s,vm)}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
            Column(Modifier.weight(1f)){Text("Seus próximos passos",style=MaterialTheme.typography.titleLarge);Text(if(rested)"Há espaço para recuperar o fôlego." else "Um hábito de cada vez.",fontSize=12.sp,color=Muted)}
            IconButton(onAdd,Modifier.size(48.dp).clip(CutCornerShape(5.dp)).background(Gold.copy(alpha=.12f)).border(1.dp,Gold.copy(alpha=.5f),CutCornerShape(5.dp))){Icon(Icons.Default.Add,"Criar hábito")}
        }
        if(due.size>1||done>0)ChipRow(listOf("Todos (${due.size})","Pendentes (${due.size-done})"),if(pendingOnly)1 else 0,{pendingOnly=it==1})
        val visible=due.filter{!pendingOnly||s.record(it.id,day)?.complete!=true}
        if(due.isEmpty())Empty(if(rested)"Este dia está reservado para descanso. Seus registros e recompensas anteriores continuam guardados." else "Nenhum hábito para hoje. Crie seu primeiro passo ou reserve um momento para o diário.")
        else if(visible.isEmpty())Empty("Tudo feito por hoje. Aetheris pode esperar pelo seu próximo passo.")
        visible.forEach{h->HabitCard(h,s.record(h.id,day),{
            if(h.type==0 && s.record(h.id,day)?.complete!=true)vm.run{vm.repo.progress(h,day,1)}else onProgress(h,s.record(h.id,day))
        },{detail=h},{onFocus(h)})}
        WeekStrip(s,day)
        if(!appearance.minimal)s.quests.filter{it.period==0}.firstOrNull{q->s.claims.none{it.id=="${q.id}@${occurrence(q,day)}"}}?.let{q->
            val n=questProgress(q,s,occurrence(q,day));Panel(accent=Violet){Eyebrow("Contrato do dia",Violet);Text(q.name,style=MaterialTheme.typography.titleLarge);Text(q.description,color=Muted,fontSize=13.sp);Meter(n.toFloat()/q.target,Violet);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("${n.coerceAtMost(q.target.toLong())}/${q.target}",color=Muted,fontSize=12.sp);Text("+${q.xp} XP · ◈ ${q.coins}",color=Violet,fontSize=12.sp)};if(n>=q.target)Action("Resgatar recompensa",{vm.run{vm.repo.claim(q)}})}
        }
        if(!appearance.minimal){
            Eyebrow("ALÉM DA NÉVOA",Blue)
            Landscape(s)
            Text("Capítulo ${chapterNames.indices.count{chapterUnlocked(it,s)}} · ${regions[regionLevels.indexOfLast{it<=s.level}.coerceAtLeast(0)]}",color=Muted,fontSize=12.sp)
        }
        Panel {
            Eyebrow(if(appearance.minimal)"Uma pausa para refletir" else "Palavras do Cronista")
            Text(when{rested->"“A estrada não desaparece quando você para para respirar.”";done==due.size&&due.isNotEmpty()->"“Guarde a brasa. Amanhã haverá outros caminhos.”";else->"“Não é o tamanho do passo que devolve luz ao mundo. É a sua presença.”"},fontFamily=FontFamily.Serif,fontSize=19.sp,lineHeight=27.sp)
            TextButton(onJournal){Icon(Icons.Default.EditNote,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Escrever no diário")}
        }
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
