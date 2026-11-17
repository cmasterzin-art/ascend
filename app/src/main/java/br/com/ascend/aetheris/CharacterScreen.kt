package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*
import java.time.*

@Composable fun CharacterScreen(s:GameState,vm:GameViewModel,onSettings:()->Unit,onJournal:()->Unit,onMemories:()->Unit={},onPortrait:()->Unit={}){
    val hero=requireNotNull(s.player)
    val tabStates=rememberSaveableStateHolder()
    var tab by rememberSaveable{mutableIntStateOf(0)};var playerEdit by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize()){
        Column(Modifier.padding(horizontal=20.dp).padding(top=14.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Title("Seu aventureiro")};IconButton(onSettings){Icon(Icons.Default.Settings,"Configurações",tint=Gold)}}
        AetherisTabs(listOf("Perfil","Atributos","Inventário","Loja","Conquistas","Estatísticas","Habilidades"),tab,{tab=it})
        }
        val reduce=LocalAppearance.current.reducedMotion
        Crossfade(targetState=tab,modifier=Modifier.weight(1f),animationSpec=tween(if(reduce)0 else 220),label="abas do herói"){visible->tabStates.SaveableStateProvider(visible){Page{
        when(visible){
            0->{AetherSurface(SurfaceKind.Heroic){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){Avatar(hero,s,110.dp);Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){ClassEmblem(hero.heroClass,Modifier.size(30.dp));Text(hero.name,style=MaterialTheme.typography.headlineMedium);Text(classes[hero.heroClass],color=Blue,fontSize=13.sp);Text(hero.title,color=Muted,fontSize=12.sp)}};MetricPair("${s.level}","nível do aventureiro","${s.coins}","fragmentos");Meter(Progression.current(s.xp).toFloat()/Progression.requirement(s.level),Blue);Text("${s.xp} XP total · ${Progression.current(s.xp)}/${Progression.requirement(s.level)} XP neste nível",color=Muted,fontSize=12.sp)};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Ampliar retrato",onPortrait,Modifier.weight(1f),secondary=true);SagaButton("Salão das Memórias",onMemories,Modifier.weight(1f),secondary=true)};AetherSurface{SectionHeader("Sua identidade");ReadingBlock(classDescriptions[hero.heroClass]);if(hero.goals.isNotBlank())ReadingBlock(hero.goals,"Seus propósitos");Action("Personalizar personagem",{playerEdit=true},Modifier.fillMaxWidth())};EquippedNiches(s);s.assets.find{it.kind=="companion"&&it.equipped}?.let{a->Panel(accent=Violet){val item=shopItems.find{it.id==a.id};Eyebrow("Companheiro",Violet);Text(item?.name?:"Companheiro",style=MaterialTheme.typography.titleLarge);Text(when(a.id){"lyra"->"Lyra se acomoda ao lado do seu caderno. Parece aprovar a página de hoje.";"corvus"->"Corvus observa o horizonte. Há sempre outra trilha entre as árvores.";else->"Ignis protege uma pequena brasa sob as asas. Por enquanto, isso basta."},color=Muted)}};Action("Abrir diário do aventureiro",onJournal,Modifier.fillMaxWidth());Panel{Eyebrow("Títulos conquistados");val titles=listOf(1 to "O Desperto",5 to "Aprendiz Errante",10 to "Aventureiro",15 to "Guardião do Conhecimento",20 to "Mestre da Vontade",30 to "Herói de Aetheris",50 to "Lenda Ascendente");(titles+specializationTitles(s).map{1 to it}).filter{it.first<=s.level}.forEach{(_,title)->TextButton({vm.run{vm.repo.player(hero.copy(title=title))}}){Text(if(hero.title==title)"✦ $title" else title)}}}}
            1->{attributes.forEachIndexed{i,a->val pts=s.points(i);Panel(accent=habitColor(i)){Eyebrow("Atributo · nível ${1+pts/50}",habitColor(i));Text(a,style=MaterialTheme.typography.titleLarge);Meter((pts%50)/50f,habitColor(i));Text("${pts%50}/50 pontos neste nível · $pts acumulados",color=Muted,fontSize=12.sp);s.ledger.filter{it.attribute==i && it.points!=0L}.sortedByDescending{it.date}.take(8).forEach{Text("${displayDate(it.date)} · ${it.label.removePrefix("Hábito · ")} · +${it.points}",color=Muted,fontSize=11.sp)}}}}
            2->Equipment(s,vm)
            3->Equipment(s,vm,store=true)
            4->{achievementDefs.forEach{a->val unlocked=s.achievements.find{it.id==a.id};val value=achievementProgress(a,s);val color=when(a.rarity){"Lendária"->Gold;"Épica"->Violet;"Rara"->Blue;else->Muted};Panel(accent=color){Eyebrow("${a.rarity} · ${if(unlocked!=null)"Conquistada" else "Em busca"}",color);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RuneSeal(if(unlocked!=null)4 else 1,color,Modifier.size(40.dp));Text("${a.icon} ${a.name}",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge)};Text(a.description,color=Muted,fontSize=13.sp);Meter(value.toFloat()/a.target,color);Text(unlocked?.let{"Registrada em ${displayDate(it.date)} · ◈ 5"}?:"${value.coerceAtMost(a.target)}/${a.target}",color=color,fontSize=12.sp)}}}
            5->Statistics(s)
            6->SkillTree(s,vm)
        }
    }
    }}}
    if(playerEdit)PlayerEditor(hero,{playerEdit=false},{p->vm.run{vm.repo.player(p);playerEdit=false}})
}
@Composable fun PlayerEditor(p:Player,onDismiss:()->Unit,onSave:(Player)->Unit){
    var name by remember{mutableStateOf(p.name)};var cls by remember{mutableIntStateOf(p.heroClass)};var face by remember{mutableIntStateOf(p.face)};var color by remember{mutableIntStateOf(p.color)};var outfit by remember{mutableIntStateOf(p.outfit)};var frame by remember{mutableIntStateOf(p.frame)};var goals by remember{mutableStateOf(p.goals)}
    val next=p.copy(name=name,heroClass=cls,face=face,color=color,outfit=outfit,frame=frame,goals=goals)
    FullForm("Personalizar",onDismiss,{onSave(next)}){Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Avatar(next,size=130.dp)};FormField(name,{name=it.take(40)},"Nome");ChipRow(classes,cls,{cls=it});ClassEmblem(cls);Text(classDescriptions[cls],color=Muted);ChipRow(listOf("Pele clara","Pele escura","Pele média"),face,{face=it});ChipRow(listOf("Ouro","Azul","Violeta","Verde","Coral"),color,{color=it});ChipRow(listOf("Túnica","Veste de viagem","Cerimonial"),outfit,{outfit=it});ChipRow(listOf("Simples","Solar","Estrelada"),frame,{frame=it});FormField(goals,{goals=it},"Seus propósitos",multi=true)}
}
@Composable fun Statistics(s:GameState){var h by remember{mutableStateOf<Habit?>(null)};AnalysisContent(s,onHabit={h=it});h?.let{habit->FullForm("Análise do hábito",{h=null},{h=null},"Fechar"){AnalysisContent(s,habit)}}}
@Composable fun XpChart(values:List<Float>){
 val peak=(values.maxOrNull()?:0f).toLong().coerceAtLeast(1)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
  Column(Modifier.width(42.dp).height(150.dp),verticalArrangement=Arrangement.SpaceBetween){Text("$peak",color=Muted,fontSize=10.sp);Text("${peak/2}",color=Muted,fontSize=10.sp);Text("0 XP",color=Muted,fontSize=10.sp)}
  Box(Modifier.weight(1f)){XpPlot(values)}
 }
}
@Composable private fun XpPlot(values:List<Float>){
    val blue=Blue;val grid=CardColor
    val description="XP por dia: "+values.mapIndexed{i,x->"dia ${i+1}: ${x.toLong()}"}.joinToString(", ")
    Canvas(Modifier.fillMaxWidth().height(150.dp).semantics{contentDescription=description}){
        val margin=8.dp.toPx();val height=size.height-margin*2;val width=size.width-margin*2
        repeat(4){i->val y=margin+height*i/3;drawLine(grid,Offset(margin,y),Offset(size.width-margin,y),1.dp.toPx())}
        val max=(values.maxOrNull()?:0f).coerceAtLeast(1f)
        val points=values.mapIndexed{i,v->Offset(margin+width*i/(values.size-1).coerceAtLeast(1),margin+height*(1-v/max))}
        val line=Path().apply{points.forEachIndexed{i,p->if(i==0)moveTo(p.x,p.y) else lineTo(p.x,p.y)}}
        val area=Path().apply{addPath(line);lineTo(points.last().x,size.height-margin);lineTo(points.first().x,size.height-margin);close()}
        drawPath(area,Brush.verticalGradient(listOf(blue.copy(alpha=.27f),blue.copy(alpha=.01f))))
        drawPath(line,blue,style=androidx.compose.ui.graphics.drawscope.Stroke(2.5.dp.toPx(),cap=StrokeCap.Round,join=StrokeJoin.Round))
        if(values.size<=7)points.forEach{drawCircle(blue,3.5.dp.toPx(),it)}
    }
}
@Composable fun JournalScreen(s:GameState,vm:GameViewModel){
    var search by remember{mutableStateOf("")};var editor by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<Journal?>(null)};var deleting by remember{mutableStateOf<Journal?>(null)}
    Page{Title("Diário do aventureiro","Nem tudo o que importa cabe em uma barra de XP.");Action("+ Escrever uma página",{editor=true});FormField(search,{search=it},"Pesquisar suas páginas");val entries=s.journal.filter{it.title.contains(search,true)||it.text.contains(search,true)};if(entries.isEmpty())Empty("As páginas estão em silêncio. Escreva quando quiser.");entries.forEach{j->Manuscript{Eyebrow(displayDate(j.date),Sepia);Text(j.title,style=MaterialTheme.typography.titleLarge);Text(j.text,color=Sepia,fontSize=15.sp,lineHeight=25.sp);Row{TextButton({editing=j;editor=true}){Text("Editar")};TextButton({deleting=j}){Text("Excluir",color=Muted)}}}}}
    if(editor)JournalEditor(editing,{editor=false;editing=null},{j->vm.run{vm.repo.journal(j);editor=false;editing=null}})
    deleting?.let{j->AlertDialog(onDismissRequest={deleting=null},title={Text("Excluir esta página?")},text={Text("A página será removida do diário local.")},confirmButton={TextButton({vm.run{vm.repo.deleteJournal(j.id)};deleting=null}){Text("Excluir")}},dismissButton={TextButton({deleting=null}){Text("Cancelar")}})}
}
