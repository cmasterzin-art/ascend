package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun HabitCard(h:Habit,r:HabitRecord?,onProgress:()->Unit,onDetail:()->Unit,onFocus:()->Unit){
 val nb=LocalNorthborn.current
 val target=r?.target?:h.target;val amount=r?.progress?:0;val complete=r?.complete==true;val inProgress=amount>0&&!complete;val color=habitColor(h.color)
 val rawUnit=r?.unit?:h.unit;val unitLabel=if(h.type==2&&(rawUnit.isBlank()||rawUnit=="vez"))"minutos" else rawUnit
 val stateColor=if(complete)nb.accentPatina else if(inProgress)color else nb.hairlineStrong
 val actShape=NorthbornShape.sm
 NorthbornSurface(
  role = NorthbornSurfaceRole.Operational,
  accent = stateColor,
  modifier = Modifier.testTag("habit:${h.id}").drawWithContent{
   drawContent()
   val d=1.dp.toPx()
   if(complete||inProgress)drawRect(stateColor.copy(alpha=if(complete).85f else .75f), androidx.compose.ui.geometry.Offset(0f, 8*d), androidx.compose.ui.geometry.Size(3*d, (size.height-16*d).coerceAtLeast(0f)))
  }
 ){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
   Box(Modifier.size(34.dp)){AttributeEmblem(h.attribute,Modifier.matchParentSize());Text(glyphs[h.icon],Modifier.align(Alignment.BottomEnd).background(nb.canvas.copy(alpha=.9f),CircleShape).padding(horizontal=2.dp),color=color,fontSize=9.sp,lineHeight=11.sp)}
   Text("${attributes[h.attribute]} · ${if(h.type==2)"Cronometrado" else difficultyNames[h.difficulty]}",Modifier.weight(1f),color=nb.inkSecondary,fontSize=12.sp)
   Text(if(complete)"◆ Concluído" else if(inProgress)"◈ Em curso" else "Pendente",color=if(complete)nb.accentPatina else if(inProgress)color else nb.inkTertiary,fontSize=11.5.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Medium)
  }
  Column(Modifier.fillMaxWidth().clickable(onClickLabel="Ver detalhes de ${h.name}",onClick=onDetail),verticalArrangement=Arrangement.spacedBy(3.dp)){
   Text(if(LocalNarrativeNames.current&&h.narrativeName.isNotBlank())h.narrativeName else h.name,style=MaterialTheme.typography.titleMedium,color=nb.inkPrimary)
   if(LocalNarrativeNames.current&&h.narrativeName.isNotBlank())Text(h.name,color=nb.inkSecondary,fontSize=12.sp)
  }
  if(h.type!=0)Meter(amount.toFloat()/target,if(complete)nb.accentPatina else color,slim=true)
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Column(Modifier.weight(1f)){
    Text(if(h.type==0)"+${r?.xp?:h.xp} XP" else "$amount/$target $unitLabel",color=if(complete)nb.accentPatina else color,fontSize=13.5.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Medium)
    if(h.type!=0)Text("+${r?.xp?:h.xp} XP ao atingir a meta",color=nb.inkSecondary,fontSize=11.5.sp)
   }
   if(h.type==2){
    NordicCompactAction(
     text = "Foco",
     onClick = onFocus,
     accent = Violet,
     modifier = Modifier.heightIn(min=48.dp).semantics{contentDescription="Iniciar foco em ${h.name}"}
    )
   }
   IconButton(
    onClick = onProgress,
    modifier = Modifier
     .size(48.dp)
     .clip(actShape)
     .background(if(complete)nb.accentPatina.copy(alpha=if(nb.dark).22f else .16f) else color.copy(alpha=if(nb.dark).16f else .12f))
     .border(1.dp,(if(complete)nb.accentPatina else color).copy(alpha=.65f),actShape)
   ){
    Icon(
     if(complete)Icons.Default.Check else Icons.Default.Add,
     if(complete)"Editar conclusão" else "Registrar progresso",
     tint = if(complete)nb.accentPatina else color
    )
   }
  }
 }
}
@Composable fun FocusCard(f:Focus,s:GameState,vm:GameViewModel){
    var now by remember{mutableLongStateOf(System.currentTimeMillis())};LaunchedEffect(f){while(f.startedAt>0){now=System.currentTimeMillis();delay(1000)}}
    val seconds=f.seconds(now);val remaining=(f.duration*60L-seconds).coerceAtLeast(0);val violet=Violet;val gold=Gold
    val label=s.habits.find{it.id==f.habitId}?.name ?: f.targetLabel.ifBlank{s.pathTasks.find{it.id==f.taskId}?.title ?: s.paths.find{it.id==f.pathId}?.name ?: "Sessão"}
    val largePlate = LocalConfiguration.current.screenWidthDp >= 400 && LocalDensity.current.fontScale <= 1.1f
    val footer = if (f.habitId.isBlank()) "Foco de projeto ou tarefa, sem XP adicional."
        else "Minutos completos são registrados no hábito."
    NorthbornSurface(role = NorthbornSurfaceRole.Strategic, accent = violet, modifier = Modifier.testTag("focus_card")) {
        Eyebrow("Foco · $label", violet)
        Text("${seconds/60} ${if(seconds/60==1L)"minuto efetivo" else "minutos efetivos"} · ${displayDate(f.date)}", color=Muted, fontSize=12.sp)
        Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){
            Canvas(Modifier.size(144.dp)){
                val cx=size.width/2;val cy=size.height/2;val r=size.minDimension/2;val d=1.dp.toPx()
                drawCircle(gold.copy(alpha=.24f),radius=r-2*d,style=androidx.compose.ui.graphics.drawscope.Stroke(d))
                for(i in 0 until 12){
                    val ang=Math.toRadians((i*30-90).toDouble())
                    val r1=r-(if(i%3==0)10*d else 6*d);val r2=r-2*d
                    drawLine(if(i%3==0)gold.copy(alpha=.60f) else violet.copy(alpha=.35f),androidx.compose.ui.geometry.Offset(cx+(r1*kotlin.math.cos(ang)).toFloat(),cy+(r1*kotlin.math.sin(ang)).toFloat()),androidx.compose.ui.geometry.Offset(cx+(r2*kotlin.math.cos(ang)).toFloat(),cy+(r2*kotlin.math.sin(ang)).toFloat()),if(i%3==0)1.5f*d else d)
                }
            }
            DayDial(seconds.toFloat()/(f.duration*60),"%02d:%02d".format(remaining/60,remaining%60),124.dp)
        }
        Meter(seconds.toFloat()/(f.duration*60),violet,slim=true)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){SagaButton(if(f.startedAt>0)"Pausar" else "Retomar",{vm.run{vm.repo.pauseFocus()}},Modifier.weight(1f),secondary=true);Action("Registrar",{vm.run{vm.repo.endFocus(true)}},Modifier.weight(1f),ceremonial=largePlate)}
        TextButton({ vm.run { vm.repo.endFocus(false) } }, Modifier.align(Alignment.End)) {
            Text("Descartar sessão", color = Muted, fontSize = 12.sp)
        }
        Text(footer, color = Muted, fontSize = 11.5.sp, lineHeight = 16.sp)
    }
}
@Composable fun HabitsScreen(s:GameState,vm:GameViewModel,onAdd:()->Unit,onEdit:(Habit)->Unit,onProgress:(Habit,HabitRecord?)->Unit,onFocus:(Habit)->Unit,onExplore:()->Unit={},onPlanner:()->Unit={},onAnalysis:()->Unit={},onHistory:()->Unit={}){
    var tab by rememberSaveable{mutableIntStateOf(0)};var showArchived by remember{mutableStateOf(false)};var menu by remember{mutableStateOf<Habit?>(null)};var delete by remember{mutableStateOf<Habit?>(null)};var routine by remember{mutableStateOf<Routine?>(null)};var newRoutine by remember{mutableStateOf(false)};var date by remember{mutableStateOf(today())}
    var search by rememberSaveable{mutableStateOf("")};var status by rememberSaveable{mutableIntStateOf(0)};var attr by rememberSaveable{mutableIntStateOf(0)};var order by rememberSaveable{mutableIntStateOf(0)}
    val nb=LocalNorthborn.current
    Page{
        Title("Livro de hábitos","Constância diária, calendário, rotinas e histórico.")
        ShortcutStrip(listOf(Triple("Planejador",Glyph.Planner,onPlanner),Triple("Análises",Glyph.Analysis,onAnalysis),Triple("Foco",Glyph.Focus,onHistory)))
        AetherisTabs(listOf("Hábitos","Calendário","Rotinas","Histórico"),tab,{tab=it})
        when(tab){
            0->{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Action("+ Criar hábito",onAdd,Modifier.weight(1f));SagaButton("Explorar hábitos",onExplore,Modifier.weight(1f),secondary=true)}
                FormField(search,{search=it},"Buscar por nome ou categoria")
                Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow("Filtros e ordem",nb.inkSecondary);Text("Deslize para atributos e ordem ›",color=nb.accentBronze,fontSize=11.sp)}
                    val scroll=rememberScrollState()
                    Box(Modifier.fillMaxWidth()){
                        Row(Modifier.fillMaxWidth().horizontalScroll(scroll).padding(end=18.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){ChipRow(listOf("Todos","Pendentes","Concluídos","Em pausa"),status,{status=it},scrollable=false);Box(Modifier.width(1.dp).height(22.dp).background(nb.hairlineStrong));ChipRow(listOf("Todos os atributos")+attributes,attr,{attr=it},scrollable=false);Box(Modifier.width(1.dp).height(22.dp).background(nb.hairlineStrong));ChipRow(listOf("Minha ordem","Nome","Horário"),order,{order=it},scrollable=false)}
                        if(scroll.canScrollForward)Box(Modifier.align(Alignment.CenterEnd).width(24.dp).height(34.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,nb.canvas))),contentAlignment=Alignment.CenterEnd){Text("›",color=nb.accentBronze,fontSize=16.sp)}
                    }
                }
                Toggle("Mostrar arquivados",showArchived,{showArchived=it})
                val hs=s.habits.filter{h->!h.deleted&&(showArchived||!h.archived)&&(search.isBlank()||h.name.contains(search,true)||h.category.contains(search,true))&&(attr==0||h.attribute==attr-1)&&when(status){1->!h.paused&&s.record(h.id)?.complete!=true;2->s.record(h.id)?.complete==true;3->h.paused;else->true}}.let{rows->when(order){1->rows.sortedBy{it.name.lowercase()};2->rows.sortedBy{it.time.ifBlank{"99:99"}};else->rows.sortedBy{it.position}}}
                Text("${hs.size} ${if(hs.size==1)"hábito encontrado" else "hábitos encontrados"}",color=nb.inkSecondary,fontSize=12.sp)
                if(hs.isEmpty())Empty("Nenhum hábito por aqui. Crie um novo passo ou ajuste os filtros.")
                hs.forEach{h->val st=streak(h,s.records,restDates=s.restDates,plans=s.plans);val tSuffix=if(h.time.isNotBlank())" · ${h.time}" else "";Column(verticalArrangement=Arrangement.spacedBy(4.dp)){HabitCard(h,s.record(h.id),{onProgress(h,s.record(h.id))},{menu=h},{onFocus(h)});Text(if(h.trackStreak)"Sequência: $st ${if(st==1)"dia" else "dias"}$tSuffix" else "Sem contagem de sequência$tSuffix",color=nb.inkTertiary,fontSize=11.5.sp)}}
            }
            1->{FormField(date,{date=it},"Consultar data · AAAA-MM-DD");val parsed=runCatching{LocalDate.parse(date)}.getOrNull();if(parsed!=null){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){IconButton({date=parsed.minusMonths(1).toString()}){Icon(Icons.Default.ChevronLeft,"Mês anterior")};TextButton({date=today()}){Text("Voltar para hoje")};IconButton({date=parsed.plusMonths(1).toString()}){Icon(Icons.Default.ChevronRight,"Próximo mês")}};val start=parsed.withDayOfMonth(1);Eyebrow(start.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.forLanguageTag("pt-BR"))));val offset=start.dayOfWeek.value-1;val len=start.lengthOfMonth();val cellShape=NorthbornShape.sm;Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Row(Modifier.fillMaxWidth()){weekdays.forEach{Text(it,Modifier.weight(1f),color=nb.inkSecondary,fontSize=11.sp)}};for(row in 0 until (offset+len+6)/7){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)){for(col in 0..6){val n=row*7+col-offset+1;val d=if(n in 1..len)start.withDayOfMonth(n).toString() else "";val complete=s.records.count{it.date==d && it.complete};Box(Modifier.weight(1f).aspectRatio(1f).clip(cellShape).background(if(d==date)nb.accentBronze.copy(alpha=.20f)else if(complete>0)nb.accentPatina.copy(alpha=.16f) else nb.surfaceBase).border(1.dp,if(d==date)nb.accentBronze else if(complete>0)nb.accentPatina.copy(alpha=.42f) else if(d.isNotBlank())nb.hairline else Color.Transparent,cellShape).clickable(enabled=d.isNotBlank()){date=d},contentAlignment=Alignment.Center){if(d.isNotBlank())Text(n.toString(),color=if(complete>0)nb.accentPatina else nb.inkPrimary,fontSize=13.sp)}}}}};Toggle("Descanso planejado nesta data",date in s.restDates,{enabled->vm.run{vm.repo.rest(date,enabled)}});Text("O descanso preserva suas sequências. Registros e recompensas existentes continuam guardados.",color=nb.inkSecondary,fontSize=12.sp);val hs=s.due(date);val compCount=s.records.count{it.date==date && it.complete};Text("$compCount ${if(compCount==1)"conclusão" else "conclusões"} · ${hs.size} ${if(hs.size==1)"hábito programado" else "hábitos programados"}",color=nb.inkSecondary,fontSize=12.sp);if(hs.isEmpty())Empty("Descanso ou nenhum hábito previsto nesta data.");hs.forEach{h->HabitCard(h,s.record(h.id,date),{onProgress(h,s.record(h.id,date)?:HabitRecord("${h.id}@$date",h.id,date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name))},{menu=h},{onFocus(h)})}}else Text("Informe uma data válida.",color=nb.accentBronze)}
            2->{
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Action("+ Criar rotina",{newRoutine=true},Modifier.weight(1f))
                    SagaButton("Explorar modelos de rotina",onExplore,Modifier.weight(1f),secondary=true)
                }
                if(s.routines.isEmpty())Empty("Agrupe hábitos em um ritual. As recompensas continuam ligadas ao registro único de cada dia.")
                s.routines.forEachIndexed{idx,r->
                    if(idx>0)NorthbornDivider(NorthbornDividerStyle.DiamondKnot)
                    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                            Column(Modifier.weight(1f)){
                                Text(r.name,style=MaterialTheme.typography.titleLarge,color=nb.inkPrimary)
                                Text("${r.time} · ${r.habitIds.split(',').count{s.record(it)?.complete==true}}/${r.habitIds.split(',').size} concluídos",color=nb.accentBronze,fontSize=12.5.sp)
                            }
                            Row{
                                IconButton({routine=r}){Icon(Icons.Default.Edit,"Editar rotina",tint=nb.inkSecondary)}
                                TextButton({vm.run{vm.repo.deleteRoutine(r.id)}}){Text("Excluir rotina",color=nb.accentEmber,fontSize=12.sp)}
                            }
                        }
                        r.habitIds.split(',').mapNotNull{id->s.habits.find{it.id==id&&!it.deleted}}.forEach{h->HabitCard(h,s.record(h.id),{onProgress(h,s.record(h.id))},{menu=h},{onFocus(h)})}
                    }
                }
            }
            3->{
                val rs=s.records.filter{it.progress>0}.sortedByDescending{it.date}
                if(rs.isEmpty())Empty("Seu histórico aparecerá após o primeiro registro.")
                rs.forEachIndexed{idx,r->
                    if(idx>0)NorthbornDivider(NorthbornDividerStyle.Hairline)
                    Row(Modifier.fillMaxWidth().padding(vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                        Box(Modifier.width(3.dp).height(40.dp).background(habitColor(r.attribute)))
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
                            Text(displayDate(r.date),color=nb.inkTertiary,fontSize=11.sp)
                            Text(r.name,style=MaterialTheme.typography.titleMedium,color=nb.inkPrimary)
                            Text("${r.progress}/${r.target} ${r.unit} · ${Progression.reward(r.xp,r.progress,r.target,r.proportional)} XP · ◈ ${Progression.reward(r.coins,r.progress,r.target,r.proportional)}",color=nb.inkSecondary,fontSize=12.sp)
                        }
                        NordicCompactAction("Editar",{s.habits.find{it.id==r.habitId}?.let{onProgress(it,r)}},accent=nb.accentBronze)
                    }
                }
            }
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
    delete?.let{h->AetherDialog(onDismissRequest={delete=null},title="Excluir ${h.name}?",confirmButton={SagaButton("Excluir",{vm.run{vm.repo.habit(h.copy(deleted=true,reminder=false))};delete=null},destructive=true)},dismissButton={TextButton({delete=null}){Text("Cancelar")}}){Text("O hábito sairá das listas. Os registros e recompensas anteriores serão preservados no histórico.")}}
    if(newRoutine||routine!=null)RoutineEditor(routine,s.habits,{newRoutine=false;routine=null},{r->vm.run{vm.repo.routine(r);newRoutine=false;routine=null}})
}
@Composable fun MissionsScreen(s:GameState,vm:GameViewModel,bosses:Boolean){
    var tab by rememberSaveable{mutableIntStateOf(0)};var editor by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<Quest?>(null)}
    Page{
        Title("Contratos da jornada","O livro das ações que devolvem luz ao mundo.")
        SagaCover(s.claims.size)
        AetherisTabs(listOf("Diárias","Semanais","Épicas","Chefes","Resgates","Sagas"),tab,{tab=it});if(tab!=5)Action("+ Missão personalizada",{editor=true})
        if(tab==5)SagaBook(s,vm)
        if(tab==3){Toggle("Ativar chefes opcionais",bosses,{vm.setting("bosses",it)});Text("Cada hábito completo enfraquece a névoa. Sem perdas por ausência.",color=Muted,fontSize=13.sp)}
        if(tab==4){if(s.claims.isEmpty())Empty("Seu livro ainda aguarda os primeiros resgates.");s.claims.sortedByDescending{it.date}.forEach{c->Manuscript{Text(s.quests.find{it.id==c.questId}?.name?:"Missão",fontFamily=FontFamily.Serif,fontSize=22.sp);Text("${displayDate(c.date)} · ${if(c.occurrence=="once")"Resgate único" else displayDate(c.occurrence)}",fontSize=12.sp);Text("Recompensa registrada no histórico",fontSize=12.sp)}}}
        else if(tab<4&&(tab!=3||bosses)){s.quests.filter{it.period==tab&&it.sagaStart.isBlank()}.forEach{q->
            val value=questProgress(q,s);val claimed=s.claims.any{it.id=="${q.id}@${occurrence(q)}"}
            MissionCard2(q,s,vm,tab,value,claimed){editing=q;editor=true}
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
