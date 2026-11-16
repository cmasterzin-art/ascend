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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun HabitCard(h:Habit,r:HabitRecord?,onProgress:()->Unit,onDetail:()->Unit,onFocus:()->Unit){
    val complete=r?.complete==true;val progress=r?.progress?:0;val target=r?.target?:h.target;val color=habitColor(h.color)
    Row(Modifier.fillMaxWidth().border(1.dp,if(complete)Green.copy(alpha=.4f) else color.copy(alpha=.18f),CutCornerShape(5.dp)).materialSurface(accent=if(complete)Green else color,leather=true).padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
        Box(Modifier.size(48.dp).background(color.copy(alpha=.1f),CutCornerShape(7.dp)).border(1.dp,color.copy(alpha=.35f),CutCornerShape(7.dp)),contentAlignment=Alignment.Center){Text(glyphs[h.icon],color=color,fontSize=24.sp)}
        Column(Modifier.weight(1f).clickable(onClickLabel="Ver detalhes de ${h.name}",onClick=onDetail),verticalArrangement=Arrangement.spacedBy(4.dp)){
            Text(if(LocalNarrativeNames.current&&h.narrativeName.isNotBlank())h.narrativeName else h.name,color=Ink,fontSize=16.sp,fontFamily=SagaFont);if(LocalNarrativeNames.current&&h.narrativeName.isNotBlank())Text(h.name,color=Muted,fontSize=12.sp)
            Text("${attributes[h.attribute]} · +${r?.xp?:h.xp} XP · ${difficultyNames[h.difficulty.coerceIn(0,2)]}",color=color,fontSize=11.sp)
            if(h.type!=0){Meter(progress.toFloat()/target,color);Text("$progress / $target ${r?.unit?:h.unit}",color=Muted,fontSize=11.sp)}
            Text(if(complete)"✓ Juramento cumprido" else if(progress>0)"Ritual em andamento" else "Ritual pendente",color=if(complete)Green else Muted,fontSize=10.sp)
            if(h.paused||h.archived)Text(if(h.archived)"Arquivado" else "Em pausa",color=Gold,fontSize=11.sp)
        }
        if(h.type==2 && !complete)IconButton(onFocus,Modifier.size(48.dp)){Icon(Icons.Default.PlayArrow,"Iniciar foco",tint=color)}
        IconButton(onProgress,Modifier.size(48.dp).materialSurface(accent=if(complete)Green else color).border(1.dp,if(complete)Green else color.copy(alpha=.65f),CutCornerShape(8.dp))){Icon(if(complete)Icons.Default.Check else Icons.Default.Add,if(complete)"Editar conclusão" else "Registrar progresso",tint=if(complete)Green else Ink)}
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
@Composable fun HabitsScreen(s:GameState,vm:GameViewModel,onAdd:()->Unit,onEdit:(Habit)->Unit,onProgress:(Habit,HabitRecord?)->Unit,onFocus:(Habit)->Unit,onExplore:()->Unit={}){
    var tab by remember{mutableIntStateOf(0)};var showArchived by remember{mutableStateOf(false)};var menu by remember{mutableStateOf<Habit?>(null)};var delete by remember{mutableStateOf<Habit?>(null)};var routine by remember{mutableStateOf<Routine?>(null)};var newRoutine by remember{mutableStateOf(false)};var date by remember{mutableStateOf(today())}
    var search by remember{mutableStateOf("")};var status by remember{mutableIntStateOf(0)};var attr by remember{mutableIntStateOf(0)}
    Page{
        Title("Livro de hábitos","A constância também conhece o descanso.");ChipRow(listOf("Hábitos","Calendário","Rotinas","Histórico"),tab,{tab=it})
        when(tab){
            0->{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Action("+ Criar hábito",onAdd,Modifier.weight(1f));OutlinedButton(onExplore,Modifier.weight(1f).heightIn(min=48.dp)){Text("Explorar hábitos")}}
                FormField(search,{search=it},"Buscar por nome ou categoria")
                ChipRow(listOf("Todos","Pendentes","Concluídos","Em pausa"),status,{status=it})
                ChipRow(listOf("Todos os atributos")+attributes,attr,{attr=it})
                Toggle("Mostrar arquivados",showArchived,{showArchived=it})
                val hs=s.habits.filter{h->!h.deleted&&(showArchived||!h.archived)&&(search.isBlank()||h.name.contains(search,true)||h.category.contains(search,true))&&(attr==0||h.attribute==attr-1)&&when(status){1->!h.paused&&s.record(h.id)?.complete!=true;2->s.record(h.id)?.complete==true;3->h.paused;else->true}}.sortedBy{it.position}
                Text("${hs.size} hábitos encontrados",color=Muted,fontSize=12.sp)
                if(hs.isEmpty())Empty("Nenhum hábito por aqui. Crie um novo passo ou ajuste os filtros.")
                hs.forEach{h->HabitCard(h,s.record(h.id),{onProgress(h,s.record(h.id))},{menu=h},{onFocus(h)});Text(if(h.trackStreak)"Sequência: ${streak(h,s.records,restDates=s.restDates)} dias · ${h.time}" else "Sem contagem de sequência · ${h.time}",color=Muted,fontSize=11.sp)}
            }
            1->{FormField(date,{date=it},"Consultar data · AAAA-MM-DD");val parsed=runCatching{LocalDate.parse(date)}.getOrNull();if(parsed!=null){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){IconButton({date=parsed.minusMonths(1).toString()}){Icon(Icons.Default.ChevronLeft,"Mês anterior")};TextButton({date=today()}){Text("Voltar para hoje")};IconButton({date=parsed.plusMonths(1).toString()}){Icon(Icons.Default.ChevronRight,"Próximo mês")}};val start=parsed.withDayOfMonth(1);Eyebrow(start.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.forLanguageTag("pt-BR"))));val offset=start.dayOfWeek.value-1;val len=start.lengthOfMonth();Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Row(Modifier.fillMaxWidth()){weekdays.forEach{Text(it,Modifier.weight(1f),color=Muted)}};for(row in 0 until (offset+len+6)/7){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){for(col in 0..6){val n=row*7+col-offset+1;val d=if(n in 1..len)start.withDayOfMonth(n).toString() else "";val complete=s.records.count{it.date==d && it.complete};Box(Modifier.weight(1f).aspectRatio(1f).background(if(d==date)Gold.copy(alpha=.22f)else if(complete>0)Green.copy(alpha=.18f) else Surface,RoundedCornerShape(8.dp)).clickable(enabled=d.isNotBlank()){date=d},contentAlignment=Alignment.Center){if(d.isNotBlank())Text(n.toString(),color=if(complete>0)Green else Ink,fontSize=13.sp)}}}}};Toggle("Descanso planejado nesta data",date in s.restDates,{enabled->vm.run{vm.repo.rest(date,enabled)}});Text("O descanso preserva suas sequências. Registros e recompensas existentes continuam guardados.",color=Muted,fontSize=12.sp);val hs=s.due(date);Text("${s.records.count{it.date==date && it.complete}} conclusões · ${hs.size} hábitos programados",color=Muted,fontSize=12.sp);if(hs.isEmpty())Empty("Descanso ou nenhum hábito previsto nesta data.");hs.forEach{h->HabitCard(h,s.record(h.id,date),{onProgress(h,s.record(h.id,date)?:HabitRecord("${h.id}@$date",h.id,date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name))},{menu=h},{onFocus(h)})}}else Text("Informe uma data válida.",color=Gold)}
            2->{OutlinedButton(onExplore){Text("Explorar modelos de rotina")};Action("+ Criar rotina",{newRoutine=true});if(s.routines.isEmpty())Empty("Agrupe hábitos em um ritual. As recompensas continuam ligadas ao registro único de cada dia.");s.routines.forEach{r->Panel{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(r.name,style=MaterialTheme.typography.titleLarge);IconButton({routine=r}){Icon(Icons.Default.Edit,"Editar rotina")}};Text("${r.time} · ${r.habitIds.split(',').count{s.record(it)?.complete==true}}/${r.habitIds.split(',').size}",color=Gold);r.habitIds.split(',').mapNotNull{id->s.habits.find{it.id==id&&!it.deleted}}.forEach{h->HabitCard(h,s.record(h.id),{onProgress(h,s.record(h.id))},{menu=h},{onFocus(h)})};TextButton({vm.run{vm.repo.deleteRoutine(r.id)}}){Text("Excluir rotina",color=Muted)}}}}
            3->{val rs=s.records.filter{it.progress>0}.sortedByDescending{it.date};if(rs.isEmpty())Empty("Seu histórico aparecerá após o primeiro registro.");rs.forEach{r->Panel(accent=Color(palette[r.attribute])){Eyebrow(r.date);Text(r.name,style=MaterialTheme.typography.titleLarge);Text("${r.progress}/${r.target} ${r.unit} · ${Progression.reward(r.xp,r.progress,r.target,r.proportional)} XP · ◈ ${Progression.reward(r.coins,r.progress,r.target,r.proportional)}",color=Muted);TextButton({s.habits.find{it.id==r.habitId}?.let{onProgress(it,r)}}){Text("Editar ou desfazer registro")}}}}
        }
    }
    menu?.let{h->HabitDetails(h,s,{menu=null},{menu=null;onEdit(h)},{r->menu=null;onProgress(h,r)}){
        Eyebrow("Gerenciar hábito")
        TextButton({vm.run{vm.repo.habit(h.copy(id=uid(),templateId="",name="${h.name} (cópia)",position=s.habits.size,created=today(),deleted=false,archived=false))};menu=null}){Text("Duplicar")}
        TextButton({vm.run{vm.repo.habit(h.copy(paused=!h.paused))};menu=null}){Text(if(h.paused)"Reativar" else "Pausar")}
        TextButton({vm.run{vm.repo.habit(h.copy(archived=!h.archived))};menu=null}){Text(if(h.archived)"Desarquivar" else "Arquivar")}
        Row{TextButton({vm.run{vm.repo.reorder(h,-1)};menu=null}){Text("↑ Subir")};TextButton({vm.run{vm.repo.reorder(h,1)};menu=null}){Text("↓ Descer")}}
        TextButton({menu=null;delete=h}){Text("Excluir hábito",color=MaterialTheme.colorScheme.error)}
    }}
    delete?.let{h->AlertDialog(onDismissRequest={delete=null},title={Text("Excluir ${h.name}?")},text={Text("O hábito sairá das listas. Os registros e recompensas anteriores serão preservados no histórico.")},confirmButton={TextButton({vm.run{vm.repo.habit(h.copy(deleted=true,reminder=false))};delete=null}){Text("Excluir")}},dismissButton={TextButton({delete=null}){Text("Cancelar")}})}
    if(newRoutine||routine!=null)RoutineEditor(routine,s.habits,{newRoutine=false;routine=null},{r->vm.run{vm.repo.routine(r);newRoutine=false;routine=null}})
}
@Composable fun MissionsScreen(s:GameState,vm:GameViewModel,bosses:Boolean){
    var tab by remember{mutableIntStateOf(0)};var editor by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<Quest?>(null)}
    Page{
        Title("Contratos da jornada","O livro das ações que devolvem luz ao mundo.")
        SagaCover(s.claims.size)
        ChipRow(listOf("Diárias","Semanais","Épicas","Chefes","Resgates","Sagas"),tab,{tab=it});if(tab!=5)Action("+ Missão personalizada",{editor=true})
        if(tab==5)SagaBook(s,vm)
        if(tab==3){Toggle("Ativar chefes opcionais",bosses,{vm.setting("bosses",it)});Text("Cada hábito completo enfraquece a névoa. Sem perdas por ausência.",color=Muted,fontSize=13.sp)}
        if(tab==4){if(s.claims.isEmpty())Empty("Seu livro ainda aguarda os primeiros resgates.");s.claims.sortedByDescending{it.date}.forEach{c->Manuscript{Text(s.quests.find{it.id==c.questId}?.name?:"Missão",fontFamily=FontFamily.Serif,fontSize=22.sp);Text("${c.date} · ${c.occurrence}",fontSize=12.sp);Text("Recompensa registrada no histórico",fontSize=12.sp)}}}
        else if(tab<4&&(tab!=3||bosses)){s.quests.filter{it.period==tab&&it.sagaStart.isBlank()}.forEach{q->
            val value=questProgress(q,s);val claimed=s.claims.any{it.id=="${q.id}@${occurrence(q)}"}
            Manuscript{
                Text(if(claimed)"CONTRATO RESGATADO" else if(value>=q.target)"OBJETIVO ALCANÇADO" else "MISSÃO ATIVA",color=Sepia,fontSize=10.sp,letterSpacing=2.sp)
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RuneSeal(if(claimed)4 else tab,Sepia,Modifier.size(36.dp));Text(q.name,Modifier.weight(1f),fontFamily=SagaFont,fontSize=23.sp,lineHeight=30.sp,color=Sepia)}
                if(tab==3){BossPortrait();Text("Vida: ${(100*(1-value.toFloat()/q.target)).coerceIn(0f,100f).toInt()} / 100",color=Sepia)}
                Text(listOf("DIÁRIO · BREVE","SEMANAL · PROLONGADO","ÉPICO · LONGO CURSO","CHEFE · RITUAL")[tab],color=Sepia,fontSize=11.sp)
                Text(q.description,color=Sepia,fontSize=15.sp,lineHeight=23.sp)
                HorizontalDivider(color=Sepia.copy(alpha=.3f))
                Text("Contribuem: "+s.habits.filter{!it.deleted&&(q.habitIds.isBlank()||it.id in q.habitIds.split(','))&&(q.attribute<0||it.attribute==q.attribute)&&(q.metric!=1||it.type==2)&&(q.metric!=3||it.unit.contains("página",true))}.joinToString{it.name}.ifBlank{"Nenhum hábito compatível"},color=Sepia,fontSize=12.sp)
                Text("Objetivo",fontFamily=FontFamily.Serif,fontSize=21.sp,color=Sepia)
                Text("${value.coerceAtMost(q.target.toLong())} de ${q.target} · ${listOf("conclusões","minutos","XP de hábitos","páginas")[q.metric]}",color=Sepia,fontSize=14.sp)
                Meter(value.toFloat()/q.target,Color(0xFF4E4937),trackColor=Sepia.copy(alpha=.16f))
                if(q.deadline.isNotBlank())Text("Prazo: ${q.deadline}",color=Sepia,fontSize=12.sp)
                Text("Recompensas",fontFamily=FontFamily.Serif,fontSize=21.sp,color=Sepia)
                Row(Modifier.fillMaxWidth().border(1.dp,Sepia.copy(alpha=.3f),CutCornerShape(4.dp)).padding(12.dp),horizontalArrangement=Arrangement.spacedBy(18.dp),verticalAlignment=Alignment.CenterVertically){RuneSeal(2,Sepia,Modifier.size(32.dp));Column{Text("${q.xp} XP",color=Sepia,fontFamily=SagaFont,fontSize=20.sp);Text("◈ ${q.coins} fragmentos",color=Sepia,fontSize=12.sp)}}
                if(q.special.isNotBlank())Text("Troféu: ${q.special}",color=Sepia,fontSize=13.sp)
                Action(if(claimed)"Recompensa resgatada" else "Resgatar recompensa",{vm.run{vm.repo.claim(q)}},enabled=!claimed&&value>=q.target,completed=claimed)
                TextButton({editing=q;editor=true},colors=ButtonDefaults.textButtonColors(contentColor=Sepia)){Text("Editar missão")}
            }
        }}
    }
    if(editor)QuestEditor(editing,s.habits,{editor=false;editing=null},{q->vm.run{vm.repo.quest(q);editor=false;editing=null}})
}
@Composable fun BossPortrait(){
    Box(Modifier.fillMaxWidth().height(190.dp).background(Brush.radialGradient(listOf(Color(0xFF49302F),Color(0xFF10151C)))).border(1.dp,Ember,CutCornerShape(6.dp))){
        Image(painterResource(R.drawable.nordic_wraith),null,Modifier.matchParentSize().clip(CutCornerShape(6.dp)),contentScale=ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xFF080C12).copy(alpha=.65f)))))
        Text("PRESENÇA SOB A NÉVOA",Modifier.align(Alignment.BottomCenter).padding(12.dp),color=WornIvory,fontSize=9.sp,letterSpacing=2.sp)
    }
}
