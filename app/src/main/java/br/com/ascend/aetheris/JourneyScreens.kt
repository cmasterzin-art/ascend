package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun HabitCard(h:Habit,r:HabitRecord?,onProgress:()->Unit,onDetail:()->Unit,onFocus:()->Unit){
    val complete=r?.complete==true;val progress=r?.progress?:0;val target=r?.target?:h.target;val color=Color(palette[h.color])
    Row(Modifier.fillMaxWidth().border(1.dp,if(complete)Green.copy(alpha=.4f) else color.copy(alpha=.18f),RoundedCornerShape(14.dp)).background(if(complete)Green.copy(alpha=.06f) else Surface,RoundedCornerShape(14.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Box(Modifier.size(44.dp).background(color.copy(alpha=.12f),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Text(glyphs[h.icon],color=color,fontSize=24.sp)}
        Column(Modifier.weight(1f).clickable(onClick=onDetail),verticalArrangement=Arrangement.spacedBy(4.dp)){
            Text(h.name,color=Ink,fontSize=15.sp)
            Text("${attributes[h.attribute]} · +${r?.xp?:h.xp} XP",color=color,fontSize=11.sp)
            if(h.type!=0){Meter(progress.toFloat()/target,color);Text("$progress / $target ${r?.unit?:h.unit}",color=Muted,fontSize=11.sp)}
            if(h.paused||h.archived)Text(if(h.archived)"Arquivado" else "Em pausa",color=Gold,fontSize=11.sp)
        }
        if(h.type==2 && !complete)IconButton(onFocus,Modifier.size(40.dp)){Icon(Icons.Default.PlayArrow,"Iniciar foco",tint=color)}
        IconButton(onProgress,Modifier.size(44.dp).background(if(complete)Green.copy(alpha=.18f) else CardColor,CircleShape)){Icon(if(complete)Icons.Default.Check else Icons.Default.Add,if(complete)"Editar conclusão" else "Registrar progresso",tint=if(complete)Green else Ink)}
    }
}
@Composable fun Journey(s:GameState,onProgress:(Habit,HabitRecord?)->Unit,onEdit:(Habit)->Unit,onFocus:(Habit)->Unit,onAdd:()->Unit,onJournal:()->Unit,vm:GameViewModel,relaxed:Boolean){
    val p=s.player!!;val due=if(relaxed)s.habits.filter{!it.deleted&&!it.archived&&!it.paused}.sortedBy{it.position}else s.due();val done=due.count{s.record(it.id)?.complete==true};val levelXP=Progression.current(s.xp);val next=Progression.requirement(s.level)
    Page{
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Eyebrow(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM",Locale.forLanguageTag("pt-BR"))));Text(when(LocalTime.now().hour){in 5..11->"Uma nova manhã.";in 12..17->"A jornada continua.";else->"Sob o céu de Aetheris."},color=Muted,fontSize=13.sp)};Text("◈ ${s.coins}",color=Gold)}
        Panel{
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
                Avatar(p,s,88.dp);Column(Modifier.weight(1f)){Eyebrow("${classes[p.heroClass]} · Nível ${s.level}");Text(p.name,style=MaterialTheme.typography.headlineMedium);Text(p.title,color=Gold,fontFamily=FontFamily.Serif,fontSize=14.sp)}
            }
            Meter(levelXP.toFloat()/next);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("$levelXP / $next XP",color=Blue,fontSize=11.sp);Text("${next-levelXP} até o próximo nível",color=Muted,fontSize=11.sp)}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){attributes.forEachIndexed{i,a->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(listOf("✧","◆","❖","☽","✦")[i],color=Color(palette[i]));Text("${1+s.points(i)/50}",fontSize=13.sp);Text(a.take(3).uppercase(),color=Muted,fontSize=9.sp)}}}
        }
        s.focus?.let{FocusCard(it,s,vm)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Title("As missões da vida",if(due.isEmpty())"Hoje pode ser um dia de descanso." else "$done de ${due.size} hábitos concluídos hoje");IconButton(onAdd){Icon(Icons.Default.Add,"Criar hábito",tint=Gold)}}
        if(due.isEmpty())Empty("Nenhum hábito programado para hoje. Explore o mundo, escreva no diário ou prepare sua próxima jornada.")
        else {Meter(done.toFloat()/due.size,Green);due.forEach{h->HabitCard(h,s.record(h.id),{if(h.type==0&&s.record(h.id)?.complete!=true)vm.run{vm.repo.progress(h,today(),1)}else onProgress(h,s.record(h.id))},{onEdit(h)},{onFocus(h)})}}
        s.quests.firstOrNull{it.period==0}?.let{q->val n=questProgress(q,s);Panel(accent=Violet){Eyebrow("Missão em destaque",Violet);Text(q.name,style=MaterialTheme.typography.titleLarge);Text(q.description,color=Muted,fontSize=13.sp);Meter(n.toFloat()/q.target,Violet);Text("${n.coerceAtMost(q.target.toLong())}/${q.target} · +${q.xp} XP · ◈ ${q.coins}",color=Violet,fontSize=12.sp)}}
        Landscape(s)
        Panel{Eyebrow("O Cronista");Text(when{ s.records.none{it.complete}->"“O primeiro caminho não aparece no mapa. Ele começa onde você está.”";s.assets.any{it.kind=="building"}->"“Reconheço esta vila. Mas a luz nas janelas é nova.”";done==due.size && due.isNotEmpty()->"“Feche o livro por hoje, se quiser. As páginas estarão aqui amanhã.”";else->"“Há marcas novas na estrada. Alguém voltou a passar por aqui.”"},fontFamily=FontFamily.Serif,fontSize=18.sp);val milestone=listOf(5,10,15,20,30,50).firstOrNull{it>s.level};Text(milestone?.let{"Próximo título no nível $it"}?:"Sua lenda já ilumina Aetheris",color=Muted,fontSize=12.sp);TextButton(onJournal){Text("Escrever no diário")}}
    }
}
@Composable fun FocusCard(f:Focus,s:GameState,vm:GameViewModel){
    var now by remember{mutableLongStateOf(System.currentTimeMillis())};LaunchedEffect(f){while(true){now=System.currentTimeMillis();delay(1000)}}
    val seconds=f.seconds(now);val remaining=(f.duration*60L-seconds).coerceAtLeast(0)
    Panel(accent=Violet){Eyebrow("Foco · ${s.habits.find{it.id==f.habitId}?.name?:"Sessão"}",Violet);Text("%02d:%02d".format(remaining/60,remaining%60),color=Ink,fontFamily=FontFamily.Serif,fontSize=42.sp);Meter(seconds.toFloat()/(f.duration*60),Violet);Text("${seconds/60} minutos efetivos · sessão de ${f.date}",color=Muted,fontSize=12.sp)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton({vm.run{vm.repo.pauseFocus()}}){Text(if(f.startedAt>0)"Pausar" else "Retomar")};Action("Registrar",{vm.run{vm.repo.endFocus(true)}})};TextButton({vm.run{vm.repo.endFocus(false)}}){Text("Descartar sessão",color=Muted)}
        Text("O tempo continua fora desta tela. Ao registrar, conta apenas minutos completos e preserva o progresso manual já registrado.",color=Muted,fontSize=11.sp)
    }
}
@Composable fun HabitsScreen(s:GameState,vm:GameViewModel,onAdd:()->Unit,onEdit:(Habit)->Unit,onProgress:(Habit,HabitRecord?)->Unit,onFocus:(Habit)->Unit){
    var tab by remember{mutableIntStateOf(0)};var showArchived by remember{mutableStateOf(false)};var menu by remember{mutableStateOf<Habit?>(null)};var delete by remember{mutableStateOf<Habit?>(null)};var routine by remember{mutableStateOf<Routine?>(null)};var newRoutine by remember{mutableStateOf(false)};var date by remember{mutableStateOf(today())}
    Page{
        Title("Livro de hábitos","A constância também conhece o descanso.");ChipRow(listOf("Hábitos","Calendário","Rotinas","Histórico"),tab,{tab=it})
        when(tab){
            0->{Action("+ Criar hábito",onAdd,Modifier.fillMaxWidth());Toggle("Mostrar arquivados",showArchived,{showArchived=it});val hs=s.habits.filter{!it.deleted && (showArchived||!it.archived)}.sortedBy{it.position};if(hs.isEmpty())Empty("Seu livro está aberto. Crie um hábito com a recompensa que fizer sentido para você.");hs.forEach{h->HabitCard(h,s.record(h.id),{onProgress(h,s.record(h.id))},{menu=h},{onFocus(h)});Text("Sequência: ${streak(h,s.records)} dias programados · ${h.time}",color=Muted,fontSize=11.sp)}}
            1->{FormField(date,{date=it},"Consultar data · AAAA-MM-DD");val parsed=runCatching{LocalDate.parse(date)}.getOrNull();if(parsed!=null){val start=parsed.withDayOfMonth(1);Eyebrow(start.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.forLanguageTag("pt-BR"))));val offset=start.dayOfWeek.value-1;val len=start.lengthOfMonth();Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Row(Modifier.fillMaxWidth()){weekdays.forEach{Text(it,Modifier.weight(1f),color=Muted)}};for(row in 0 until (offset+len+6)/7){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){for(col in 0..6){val n=row*7+col-offset+1;val d=if(n in 1..len)start.withDayOfMonth(n).toString() else "";val complete=s.records.count{it.date==d && it.complete};Box(Modifier.weight(1f).aspectRatio(1f).background(if(d==date)Gold.copy(alpha=.22f)else if(complete>0)Green.copy(alpha=.18f) else Surface,RoundedCornerShape(8.dp)).clickable(enabled=d.isNotBlank()){date=d},contentAlignment=Alignment.Center){if(d.isNotBlank())Text(n.toString(),color=if(complete>0)Green else Ink,fontSize=13.sp)}}}}};val hs=s.due(date);Text("${s.records.count{it.date==date && it.complete}} conclusões · ${hs.size} hábitos na programação atual",color=Muted,fontSize=12.sp);if(hs.isEmpty())Empty("Descanso ou nenhum hábito previsto nesta data.");hs.forEach{h->HabitCard(h,s.record(h.id,date),{onProgress(h,s.record(h.id,date)?:HabitRecord("${h.id}@$date",h.id,date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name))},{menu=h},{onFocus(h)})}}else Text("Informe uma data válida.",color=Gold)}
            2->{Action("+ Criar rotina",{newRoutine=true});if(s.routines.isEmpty())Empty("Agrupe hábitos em um ritual. As recompensas continuam ligadas ao registro único de cada dia.");s.routines.forEach{r->Panel{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(r.name,style=MaterialTheme.typography.titleLarge);IconButton({routine=r}){Icon(Icons.Default.Edit,"Editar rotina")}};Text("${r.time} · ${r.habitIds.split(',').count{s.record(it)?.complete==true}}/${r.habitIds.split(',').size}",color=Gold);r.habitIds.split(',').mapNotNull{id->s.habits.find{it.id==id&&!it.deleted}}.forEach{h->HabitCard(h,s.record(h.id),{onProgress(h,s.record(h.id))},{menu=h},{onFocus(h)})};TextButton({vm.run{vm.repo.deleteRoutine(r.id)}}){Text("Excluir rotina",color=Muted)}}}}
            3->{val rs=s.records.filter{it.progress>0}.sortedByDescending{it.date};if(rs.isEmpty())Empty("Seu histórico aparecerá após o primeiro registro.");rs.forEach{r->Panel(accent=Color(palette[r.attribute])){Eyebrow(r.date);Text(r.name,style=MaterialTheme.typography.titleLarge);Text("${r.progress}/${r.target} ${r.unit} · ${Progression.reward(r.xp,r.progress,r.target,r.proportional)} XP · ◈ ${Progression.reward(r.coins,r.progress,r.target,r.proportional)}",color=Muted);TextButton({s.habits.find{it.id==r.habitId}?.let{onProgress(it,r)}}){Text("Editar ou desfazer registro")}}}}
        }
    }
    menu?.let{h->AlertDialog(onDismissRequest={menu=null},title={Text(h.name)},text={Column{
        TextButton({menu=null;onEdit(h)}){Text("Editar todos os detalhes")}
        TextButton({vm.run{vm.repo.habit(h.copy(id=uid(),name="${h.name} (cópia)",position=s.habits.size,created=today(),deleted=false,archived=false))};menu=null}){Text("Duplicar")}
        TextButton({vm.run{vm.repo.habit(h.copy(paused=!h.paused))};menu=null}){Text(if(h.paused)"Reativar" else "Pausar")}
        TextButton({vm.run{vm.repo.habit(h.copy(archived=!h.archived))};menu=null}){Text(if(h.archived)"Desarquivar" else "Arquivar")}
        Row{TextButton({vm.run{vm.repo.reorder(h,-1)};menu=null}){Text("↑ Subir")};TextButton({vm.run{vm.repo.reorder(h,1)};menu=null}){Text("↓ Descer")}}
        TextButton({menu=null;delete=h}){Text("Excluir hábito",color=MaterialTheme.colorScheme.error)}
    }},confirmButton={TextButton({menu=null}){Text("Fechar")}})}
    delete?.let{h->AlertDialog(onDismissRequest={delete=null},title={Text("Excluir ${h.name}?")},text={Text("O hábito sairá das listas. Os registros e recompensas anteriores serão preservados no histórico.")},confirmButton={TextButton({vm.run{vm.repo.habit(h.copy(deleted=true,reminder=false))};delete=null}){Text("Excluir")}},dismissButton={TextButton({delete=null}){Text("Cancelar")}})}
    if(newRoutine||routine!=null)RoutineEditor(routine,s.habits,{newRoutine=false;routine=null},{r->vm.run{vm.repo.routine(r);newRoutine=false;routine=null}})
}
@Composable fun MissionsScreen(s:GameState,vm:GameViewModel,bosses:Boolean){
    var tab by remember{mutableIntStateOf(0)};var editor by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<Quest?>(null)}
    Page{
        Title("Contratos da jornada","Pequenas ações, histórias que permanecem.");ChipRow(listOf("Diárias","Semanais","Épicas","Chefes","Resgates"),tab,{tab=it});Action("+ Missão personalizada",{editor=true})
        if(tab==3){Toggle("Ativar chefes opcionais",bosses,{vm.setting("bosses",it)});Text("Sem punições e sem perda de progresso. Cada desafio recomeça na segunda-feira.",color=Muted,fontSize=12.sp)}
        if(tab==4){if(s.claims.isEmpty())Empty("As recompensas resgatadas ficam registradas aqui.");s.claims.sortedByDescending{it.date}.forEach{c->val q=s.quests.find{it.id==c.questId};Panel{Eyebrow(c.date);Text(q?.name?:"Missão");Text("Ocorrência: ${c.occurrence}",color=Muted,fontSize=12.sp)}}}
        else if(tab!=3||bosses){s.quests.filter{it.period==tab}.forEach{q->val value=questProgress(q,s);val claimed=s.claims.any{it.id=="${q.id}@${occurrence(q)}"};Panel(accent=if(tab==3)Violet else Gold){
            if(tab==3){Text("◈",color=Violet,fontSize=40.sp);Eyebrow("Desafio semanal · ${((1-value.toFloat()/q.target).coerceIn(0f,1f)*100).toInt()} de vida",Violet)}
            Text(q.name,style=MaterialTheme.typography.titleLarge);Text(q.description,color=Muted,fontSize=13.sp);Meter(value.toFloat()/q.target,if(tab==3)Violet else Blue);Text("${value.coerceAtMost(q.target.toLong())}/${q.target} · +${q.xp} XP · ◈ ${q.coins}",color=Gold,fontSize=12.sp)
            if(q.deadline.isNotBlank())Text("Prazo: ${q.deadline}",color=Muted,fontSize=12.sp)
            Action(if(claimed)"Recompensa resgatada" else "Resgatar recompensa",{vm.run{vm.repo.claim(q)}},enabled=!claimed && value>=q.target)
            if(tab==3&&!claimed)TextButton({editing=q;editor=true}){Text("Configurar meta e hábitos")}
        }}}
    }
    if(editor)QuestEditor(editing,s.habits,{editor=false;editing=null},{q->vm.run{vm.repo.quest(q);editor=false;editing=null}})
}
