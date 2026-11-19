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

@Composable fun CharacterScreen(s:GameState,vm:GameViewModel,onSettings:()->Unit,onJournal:()->Unit,onMemories:()->Unit={},onPortrait:()->Unit={},onMimir:()->Unit={},onMimirReview:()->Unit={},onPaths:()->Unit={},onForge:()->Unit={},onHeraldry:()->Unit={},onCodex:()->Unit={}){
    val hero=requireNotNull(s.player)
    val tabStates=rememberSaveableStateHolder()
    var tab by rememberSaveable{mutableIntStateOf(0)};var playerEdit by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize()){
        Column(Modifier.padding(horizontal=20.dp).padding(top=14.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Title("Seu aventureiro")};IconButton(onSettings,Modifier.semantics{contentDescription="Configurações"}){CarvedGlyph(Glyph.Settings,Gold)}}
        AetherisTabs(listOf("Perfil","Atributos","Inventário","Loja","Conquistas","Estatísticas","Habilidades"),tab,{tab=it})
        }
        val reduce=LocalAppearance.current.reducedMotion
        Crossfade(targetState=tab,modifier=Modifier.weight(1f),animationSpec=tween(if(reduce)0 else AetherTokens.scene),label="abas do herói"){visible->tabStates.SaveableStateProvider(visible){Page{
        when(visible){
            0->{HeroStage(s,onPortrait);HeroEvolution(s);Hairline();ArchetypeAscensionPanel(s,vm,onMimir,onMimirReview,onPaths,{tab=6});Hairline();AttributeInscriptions(s);Hairline();Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Ampliar retrato",onPortrait,Modifier.weight(1f),secondary=true);SagaButton("Salão das Memórias",onMemories,Modifier.weight(1f),secondary=true)};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Heráldica Rúnica",onHeraldry,Modifier.weight(1f),style=VikingButtonStyle.BronzeForjado,leadingRune=HistoricalFutharkRune.Othala);SagaButton("Codex Rúnico",onCodex,Modifier.weight(1f),style=VikingButtonStyle.FerroRunico,leadingRune=HistoricalFutharkRune.Ansuz)};AetherSurface{SectionHeader("Sua identidade");ReadingBlock(classDescriptions[hero.heroClass]);if(hero.goals.isNotBlank())ReadingBlock(hero.goals,"Seus propósitos");Action("Personalizar personagem",{playerEdit=true},Modifier.fillMaxWidth())};EquippedNiches(s);s.assets.find{it.kind=="companion"&&it.equipped}?.let{a->Panel(accent=Violet){val item=shopItems.find{it.id==a.id};Eyebrow("Companheiro",Violet);Text(item?.name?:"Companheiro",style=MaterialTheme.typography.titleLarge);Text(when(a.id){"lyra"->"Lyra se acomoda ao lado do seu caderno. Parece aprovar a página de hoje.";"corvus"->"Corvus observa o horizonte. Há sempre outra trilha entre as árvores.";else->"Ignis protege uma pequena brasa sob as asas. Por enquanto, isso basta."},color=Muted)}};Action("Abrir diário do aventureiro",onJournal,Modifier.fillMaxWidth());Panel{Eyebrow("Títulos conquistados");val baseTitles=listOf(1 to "O Desperto",5 to "Aprendiz Errante",10 to "Aventureiro",15 to "Guardião do Conhecimento",20 to "Mestre da Vontade",30 to "Herói de Aetheris",50 to "Lenda Ascendente");(baseTitles+specializationTitles(s).map{1 to it}+completedCollectionTitles(s).map{1 to it}).filter{it.first<=s.level}.distinctBy{it.second}.forEach{(_,title)->TextButton({vm.run{vm.repo.player(hero.copy(title=title))}}){Text(if(hero.title==title)"✦ $title" else title)}}}}
            1->{attributes.forEachIndexed{i,a->val pts=s.points(i);if(i>0)Hairline();Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){Eyebrow("Atributo · nível ${1+pts/50}",habitColor(i));Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){AttributeEmblem(i,Modifier.size(38.dp));Text(a,style=MaterialTheme.typography.titleLarge)};Meter((pts%50)/50f,habitColor(i));Text("${pts%50}/50 pontos neste nível · $pts acumulados",color=Muted,fontSize=12.sp);s.ledger.filter{it.attribute==i && it.points!=0L}.sortedByDescending{it.date}.take(8).forEach{Text("${displayDate(it.date)} · ${it.label.removePrefix("Hábito · ")} · +${it.points}",color=Muted,fontSize=11.sp)}}}}
            2->Equipment(s,vm,onOpenForge=onForge,onOpenHeraldry=onHeraldry)
            3->Equipment(s,vm,store=true)
            4->{achievementDefs.forEach{a->val unlocked=s.achievements.find{it.id==a.id};val value=achievementProgress(a,s);val color=when(a.rarity){"Lendária"->Gold;"Épica"->Violet;"Rara"->Blue;else->Muted};Panel(accent=color){Eyebrow("${a.rarity} · ${if(unlocked!=null)"Conquistada" else "Em busca"}",color);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){CeremonialSeal(SealKind.Reward,unlocked!=null,Modifier.size(42.dp));Text("${a.icon} ${a.name}",Modifier.weight(1f),style=MaterialTheme.typography.titleLarge)};Text(a.description,color=Muted,fontSize=13.sp);Meter(value.toFloat()/a.target,color);Text(unlocked?.let{if(a.coins>0L)"Registrada em ${displayDate(it.date)} · ◈ ${a.coins}" else "Registrada em ${displayDate(it.date)} · Marco de Sabedoria"}?:"${value.coerceAtMost(a.target)}/${a.target}",color=color,fontSize=12.sp)}}}
            5->Statistics(s)
            6->SkillTree(s,vm)
        }
    }
    }}}
    if(playerEdit)PlayerEditor(hero,s,{playerEdit=false},{p->vm.run{vm.repo.player(p);playerEdit=false}})
}
@Composable fun ArchetypeAscensionPanel(
    s: GameState,
    vm: GameViewModel,
    onMimir: () -> Unit,
    onMimirReview: () -> Unit,
    onPaths: () -> Unit,
    onOpenSkills: () -> Unit
) {
    val hero = requireNotNull(s.player)
    val options = remember(hero.heroClass) { specializationsForClass(hero.heroClass) }
    val active = activeSpecialization(s)
    val classSkills = remember(hero.heroClass) { skillDefs.filter { it.heroClass == hero.heroClass } }
    val ownedClassSkills = classSkills.count { sk -> s.assets.any { it.id == "skill:${sk.id}" } }

    AetherSurface(SurfaceKind.Operational) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Eyebrow("Ascensão do Arquétipo · ${classes[hero.heroClass]}", Gold)
                Text(
                    active?.let { "${it.name} · ${it.titleGrant}" } ?: "Escolha sua especialização",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            TextButton(onOpenSkills) { Text("Habilidades ($ownedClassSkills/${classSkills.size})", fontSize = 12.sp) }
        }
        Text(
            if (s.level < 3) "Alcance o nível 3 para escolher uma das duas especializações de ${classes[hero.heroClass]}. Trocar especialização nunca apaga habilidades já obtidas."
            else "Especializações moldam sua identidade, emblema e atalhos temáticos sem alterar XP ou moedas.",
            color = Muted,
            fontSize = 12.sp
        )
        options.forEach { spec ->
            val selected = active?.id == spec.id
            val unlocked = canSelectSpecialization(s, spec)
            val accent = if (selected) Gold else Blue
            Panel(accent = accent) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ArchetypeSealArt(spec.atlasIndex, Modifier.size(54.dp), dimmed = !unlocked && !selected)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Eyebrow(
                            if (selected) "Especialização ativa" else if (unlocked) "Disponível · Nível ${spec.unlockLevel}+" else "Bloqueada · Nível ${spec.unlockLevel}",
                            if (selected) Gold else if (unlocked) Blue else Muted
                        )
                        Text(spec.name, style = MaterialTheme.typography.titleMedium)
                        Text("“${spec.motto}”", fontFamily = FontFamily.Serif, color = Muted, fontSize = 12.sp)
                    }
                }
                Text(spec.description, color = Muted, fontSize = 12.sp)
                Text(spec.effectSummary, color = accent, fontSize = 11.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Action(
                        if (selected) "Especialização ativa" else "Escolher ${spec.name}",
                        { vm.run { vm.repo.selectSpecialization(spec.id) } },
                        Modifier.weight(1f),
                        enabled = unlocked && !selected
                    )
                    if (selected) {
                        SagaButton("Remover", { vm.run { vm.repo.selectSpecialization("") } }, secondary = true)
                    }
                }
            }
        }
        // Thematic convenience shortcuts without altering economy
        val showMimirShortcut = active?.id in setOf("erudito", "arquimago_runico") || s.assets.any { it.id in setOf("skill:mage_mimir", "skill:mage_runes") }
        val showPathsShortcut = active?.id == "cartografo" || s.assets.any { it.id == "skill:expl_compass" }
        if (showMimirShortcut || showPathsShortcut) {
            Hairline()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showMimirShortcut) {
                    SagaButton("Biblioteca de Mímir", onMimir, Modifier.weight(1f), secondary = true)
                    if (active?.id == "arquimago_runico" || s.assets.any { it.id == "skill:mage_runes" }) {
                        SagaButton("Runas da Memória", onMimirReview, Modifier.weight(1f), secondary = true)
                    }
                }
                if (showPathsShortcut) {
                    SagaButton("Meus Caminhos", onPaths, Modifier.weight(1f), secondary = true)
                }
            }
        }
    }
}
@Composable fun PlayerEditor(p:Player,s:GameState?=null,onDismiss:()->Unit,onSave:(Player)->Unit){
    var name by remember{mutableStateOf(p.name)};var cls by remember{mutableIntStateOf(p.heroClass)};var face by remember{mutableIntStateOf(p.face)};var color by remember{mutableIntStateOf(p.color)};var outfit by remember{mutableIntStateOf(p.outfit)};var frame by remember{mutableIntStateOf(p.frame)};var goals by remember{mutableStateOf(p.goals)}
    val next=p.copy(name=name,heroClass=cls,face=face,color=color,outfit=outfit,frame=frame,goals=goals)
    FullForm("Personalizar",onDismiss,{onSave(next)}){Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Avatar(next,s,size=130.dp)};FormField(name,{name=it.take(40)},"Nome");ChipRow(classes,cls,{cls=it});ClassEmblem(cls);Text(classDescriptions[cls],color=Muted);ChipRow(listOf("Pele clara","Pele escura","Pele média"),face,{face=it});ChipRow(listOf("Ouro","Azul","Violeta","Verde","Coral"),color,{color=it});ChipRow(listOf("Túnica","Veste de viagem","Cerimonial"),outfit,{outfit=it});ChipRow(listOf("Simples","Solar","Estrelada"),frame,{frame=it});FormField(goals,{goals=it},"Seus propósitos",multi=true)}
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
    val blue=Blue;val gold=Gold;val grid=ColdIron.copy(alpha=.24f)
    val description="XP por dia: "+values.mapIndexed{i,x->"dia ${i+1}: ${x.toLong()}"}.joinToString(", ")
    Canvas(Modifier.fillMaxWidth().height(150.dp).semantics{contentDescription=description}){
        val margin=8.dp.toPx();val height=size.height-margin*2;val width=size.width-margin*2;val d=1.dp.toPx()
        repeat(4){i->val y=margin+height*i/3;drawLine(if(i==3)gold.copy(alpha=.38f) else grid,Offset(margin,y),Offset(size.width-margin,y),d)}
        val max=(values.maxOrNull()?:0f).coerceAtLeast(1f)
        val points=values.mapIndexed{i,v->Offset(margin+width*i/(values.size-1).coerceAtLeast(1),margin+height*(1-v/max))}
        points.forEach{pt->drawLine(grid.copy(alpha=.14f),Offset(pt.x,margin),Offset(pt.x,size.height-margin),d)}
        val line=Path().apply{points.forEachIndexed{i,p->if(i==0)moveTo(p.x,p.y) else lineTo(p.x,p.y)}}
        val area=Path().apply{addPath(line);lineTo(points.last().x,size.height-margin);lineTo(points.first().x,size.height-margin);close()}
        drawPath(area,Brush.verticalGradient(listOf(blue.copy(alpha=.32f),gold.copy(alpha=.08f),Color.Transparent)))
        drawPath(line,blue,style=androidx.compose.ui.graphics.drawscope.Stroke(2.4.dp.toPx(),cap=StrokeCap.Round,join=StrokeJoin.Round))
        if(values.size<=7)points.forEach{
            drawCircle(blue.copy(alpha=.28f),6.dp.toPx(),it)
            drawCircle(gold,3.2.dp.toPx(),it)
            drawCircle(Color(0xFF0E1216),1.4.dp.toPx(),it)
        }
    }
}
@Composable fun JournalScreen(s:GameState,vm:GameViewModel){
    var search by remember{mutableStateOf("")};var editor by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<Journal?>(null)};var deleting by remember{mutableStateOf<Journal?>(null)}
    Page{Title("Diário do aventureiro","Nem tudo o que importa cabe em uma barra de XP.");Action("+ Escrever uma página",{editor=true});FormField(search,{search=it},"Pesquisar suas páginas");val entries=s.journal.filter{it.title.contains(search,true)||it.text.contains(search,true)};if(entries.isEmpty())Empty("As páginas estão em silêncio. Escreva quando quiser.");entries.forEach{j->Manuscript{Eyebrow(displayDate(j.date),Sepia);Text(j.title,style=MaterialTheme.typography.titleLarge);Text(j.text,color=Sepia,fontSize=15.sp,lineHeight=25.sp);Row{TextButton({editing=j;editor=true}){Text("Editar")};TextButton({deleting=j}){Text("Excluir",color=Muted)}}}}}
    if(editor)JournalEditor(editing,{editor=false;editing=null},{j->vm.run{vm.repo.journal(j);editor=false;editing=null}})
    deleting?.let{j->AetherDialog(onDismissRequest={deleting=null},title="Excluir esta página?",confirmButton={SagaButton("Excluir",{vm.run{vm.repo.deleteJournal(j.id)};deleting=null},destructive=true)},dismissButton={TextButton({deleting=null}){Text("Cancelar")}}){Text("A página será removida do diário local.")}}
}
