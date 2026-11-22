package br.com.ascend.aetheris

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*

@OptIn(ExperimentalLayoutApi::class)
@Composable fun WorldScreen(s:GameState,vm:GameViewModel,onWeekly:()->Unit={},onMemories:()->Unit={},onRegion:(Int)->Unit={},onMimir:()->Unit={},onForge:()->Unit={},onCodex:()->Unit={}){
    var tab by rememberSaveable{mutableIntStateOf(0)};var region by remember{mutableIntStateOf(0)};var chapter by remember{mutableStateOf<Int?>(null)};var restore by remember{mutableStateOf<Int?>(null)}
    val Gold=Gold;val Green=Green
    val context=androidx.compose.ui.platform.LocalContext.current
    val effects by vm.soundEnabled.collectAsStateWithLifecycle();val volume by vm.soundVolume.collectAsStateWithLifecycle()
    val pageAudio=remember{AetherisAudio(context)};SideEffect{pageAudio.enabled=effects;pageAudio.volume=volume};DisposableEffect(pageAudio){onDispose{pageAudio.close()}}
    LaunchedEffect(chapter){if(chapter!=null)pageAudio.play("page")}
    Page{
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Title("Atlas de Aetheris","Entre fiordes, ruínas e caminhos velados.")};Text("◈ ${s.coins}",color=Gold,modifier=Modifier.padding(top=14.dp))}
        AetherisTabs(listOf("Mapa","Vila","Crônicas"),tab,{tab=it})
        when(tab){
            0->{
                val rev=regionLevels.count{it<=s.level}
                Text("$rev de ${regions.size} ${if(rev==1)"região revelada" else "regiões reveladas"}",color=Blue,fontSize=12.sp)
                AetherisMap(s,region,{region=it})
                // 6-state visual legend
                val nbW = LocalNorthborn.current
                FlowRow(
                    Modifier.fillMaxWidth().testTag("atlas_legend"),
                    horizontalArrangement=Arrangement.spacedBy(6.dp),
                    verticalArrangement=Arrangement.spacedBy(6.dp)
                ){
                    RegionAtlasState.entries.forEach{st->
                        val col=regionStateColor(st)
                        Row(
                            Modifier
                                .clip(NorthbornShape.xs)
                                .background(col.copy(alpha=if(nbW.dark).10f else .12f))
                                .border(.8.dp,col.copy(alpha=.42f),NorthbornShape.xs)
                                .padding(horizontal=7.dp,vertical=3.dp),
                            verticalAlignment=Alignment.CenterVertically,
                            horizontalArrangement=Arrangement.spacedBy(4.dp)
                        ){
                            Text(st.symbol,color=col,fontSize=10.sp)
                            Text(st.label,color=nbW.inkPrimary,fontSize=11.sp)
                        }
                    }
                }
                // Selected region detail panel
                val st=regionAtlasState(s,region)
                val stColor=regionStateColor(st)
                val explReg=explorableRegion(region)
                val isOpen=s.level>=regionLevels[region]
                NorthbornSurface(role=NorthbornSurfaceRole.Strategic,material=NorthbornMaterial.VolcanicSlate,accent=stColor){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        if(explReg!=null) RealmOrnament(explReg.ornamentCell,size=38.dp)
                        else RealmOrnament(if(region==0)5 else 4,size=34.dp)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
                            Eyebrow(if(isOpen) st.label else "Na névoa · nível ${regionLevels[region]}",stColor)
                            Text(regions[region],style=MaterialTheme.typography.headlineSmall,color=nbW.inkPrimary)
                        }
                    }
                    Text(regionDescriptions[region],color=nbW.inkSecondary,fontSize=14.sp,lineHeight=22.sp)
                    if(explReg!=null && explReg.distinctionNote.isNotBlank()){
                        Text(explReg.distinctionNote,color=nbW.accentFjord,fontSize=12.sp,lineHeight=18.sp)
                    }
                    when{
                        region==0->{
                            val stage=s.assets.filter{it.kind=="building"}.sumOf{it.stage}
                            Meter(stage/10f,nbW.accentPatina,slim=true)
                            Text("$stage de 10 etapas recuperadas na Vila do Despertar",color=nbW.accentPatina,fontSize=12.sp)
                            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                NorthbornButton("Explorar região",{onRegion(region)},role=NorthbornButtonRole.Primary,modifier=Modifier.weight(1f),enabled=isOpen)
                                NorthbornButton("Visitar a vila",{tab=1},role=NorthbornButtonRole.Secondary,modifier=Modifier.weight(1f))
                            }
                        }
                        explReg!=null->{
                            val prog=regionProgress(s,region)
                            val marks=regionConfirmedMarks(s,region)
                            val total=regionTotalMarks(region)
                            val rem=(explReg.unlockLevel-s.level).coerceAtLeast(0)
                            Text(
                                if(isOpen) "Exploração · $prog% · $marks de $total marcos · ${explReg.expeditions.size} locais"
                                else "Nível ${explReg.unlockLevel} necessário · ${explReg.expeditions.size} locais exploráveis",
                                color=if(isOpen) nbW.accentFjord else nbW.inkMuted,
                                fontSize=12.sp
                            )
                            Meter(if(isOpen) prog/100f else (s.level.toFloat()/explReg.unlockLevel).coerceAtMost(1f),if(prog>=100) nbW.accentPatina else nbW.accentFjord,slim=true)
                            if(!isOpen){
                                Text("${if(rem==1)"Falta 1 nível" else "Faltam $rem níveis"} para revelar esta região.",color=nbW.inkMuted,fontSize=12.sp)
                            }else if(explReg.npc!=null){
                                Text("Guia local: ${explReg.npc.name}, ${explReg.npc.role}.",color=nbW.accentBronze,fontSize=12.sp)
                            }
                            NorthbornButton("Explorar região",{onRegion(region)},role=NorthbornButtonRole.Primary,modifier=Modifier.fillMaxWidth(),enabled=isOpen)
                        }
                        else->{
                            val progress=(s.level.toFloat()/regionLevels[region]).coerceAtMost(1f)
                            val rem=(regionLevels[region]-s.level).coerceAtLeast(0)
                            Text("Sem conteúdo interativo ainda · reservada para futuras crônicas.",color=Violet,fontSize=12.sp)
                            Meter(progress,Violet,slim=true)
                            Text(
                                if(isOpen) "Região revelada no Atlas. Ainda não possui expedições próprias nesta versão."
                                else "${if(rem==1)"Falta 1 nível" else "Faltam $rem níveis"} para revelar esta região.",
                                color=nbW.inkMuted,
                                fontSize=12.sp
                            )
                            NorthbornButton("Explorar região",{onRegion(region)},role=NorthbornButtonRole.Primary,modifier=Modifier.fillMaxWidth(),enabled=isOpen)
                        }
                    }
                }
                // Compact Exploration Journal em seção editorial aberta
                val journal=explorationJournal(s)
                Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)){
                    NorthbornDivider(NorthbornDividerStyle.DiamondKnot)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                        Eyebrow("Diário de Exploração",nbW.accentBronze)
                        Text("${journal.totalConfirmedMarks}/${journal.totalAvailableMarks} marcos",color=nbW.accentFjord,fontSize=12.sp)
                    }
                    if(journal.activeExpeditions.isEmpty()){
                        Text(
                            journal.nextAvailable?.let{"Próxima expedição disponível: ${it.name} (${regions[it.regionIndex]})."}
                                ?: if(journal.totalConfirmedMarks>=journal.totalAvailableMarks) "Todas as expedições exploráveis foram registradas no Salão das Memórias."
                                else "Alcance o nível 3 para abrir a Floresta da Persistência, nível 5 para a Biblioteca das Eras e nível 8 para as Montanhas da Disciplina.",
                            color=nbW.inkSecondary,fontSize=13.sp,lineHeight=20.sp
                        )
                    }else{
                        journal.activeExpeditions.take(3).forEach{(exp,phase,q)->
                            val pr=questProgress(q,s).coerceAtMost(q.target.toLong())
                            Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)){
                                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
                                    Text("${exp.name} · ${exp.phases[phase].name}",fontSize=13.sp,color=nbW.inkPrimary,modifier=Modifier.weight(1f))
                                    Text("$pr/${q.target}",fontSize=12.sp,color=nbW.accentBronze)
                                }
                                Meter(pr.toFloat()/q.target.coerceAtLeast(1),nbW.accentBronze,slim=true)
                            }
                        }
                    }
                    journal.lastDiscovery?.let{(title,excerpt)->
                        Hairline()
                        Text("Última descoberta · $title",color=Green,fontSize=11.sp)
                        Text(excerpt,color=Muted,fontSize=12.sp,lineHeight=18.sp)
                    }
                }
                // Accessible Region List (full parity with map without needing gestures)
                Column(Modifier.fillMaxWidth().testTag("atlas_accessible_list"),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Eyebrow("Regiões de Aetheris · lista acessível",Muted)
                    regions.forEachIndexed{i,name->
                        val rState=regionAtlasState(s,i)
                        val rColor=regionStateColor(rState)
                        val rOpen=s.level>=regionLevels[i]
                        val rProg=when(i){
                            0->"${s.assets.filter{it.kind=="building"}.sumOf{it.stage}}/10 vila"
                            1,2,3->"${regionProgress(s,i)}%"
                            else->if(rOpen)"Sem conteúdo interativo ainda" else "Nível ${regionLevels[i]}"
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(if(region==i) Gold.copy(alpha=.10f) else Surface.copy(alpha=.55f),CutCornerShape(4.dp))
                                .border(if(region==i) 1.dp else .7.dp,if(region==i) Gold.copy(alpha=.65f) else ColdIron.copy(alpha=.35f),CutCornerShape(4.dp))
                                .clickable{region=i}
                                .padding(horizontal=12.dp,vertical=10.dp),
                            verticalAlignment=Alignment.CenterVertically,
                            horizontalArrangement=Arrangement.spacedBy(10.dp)
                        ){
                            Text(rState.symbol,color=rColor,fontSize=14.sp)
                            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
                                Text(name,fontSize=14.sp,color=if(region==i) Gold else Ink)
                                Text("${rState.label} · $rProg",fontSize=11.sp,color=Muted)
                            }
                            if(rOpen){
                                SagaButton(
                                    text="Abrir",
                                    onClick={region=i;onRegion(i)},
                                    style=VikingButtonStyle.CompactoRunico
                                )
                            }else{
                                Text("Nv. ${regionLevels[i]}",color=Muted,fontSize=11.sp)
                            }
                        }
                    }
                }
            }
            1->{Landscape(s,large=true);Eyebrow("Vila do Despertar");Text("Fragmentos restauram pedras. Seus hábitos devolvem sentido a elas.",color=Muted);buildings.forEachIndexed{i,name->val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Panel(accent=if(stage==2)Green else Gold){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,style=MaterialTheme.typography.titleLarge);Text(listOf("▤","◆","♧","♜","◇")[i],color=Gold,fontSize=24.sp)};Crossfade(stage,animationSpec=tween(if(LocalAppearance.current.reducedMotion)0 else 450),label="restauração"){shown->BuildingScene(i,shown)};Text(listOf("Ruínas · portas fechadas e memórias dispersas.","Estrutura recuperada · a vida começa a voltar.","Restauração completa · uma nova luz na vila.")[stage],color=Muted,fontSize=13.sp);Meter(stage/2f,Green,slim=true);val dialogue=villagerDialogue(i,stage);Text(dialogue.first,color=Gold,fontSize=12.sp);Text("“${dialogue.second}”",fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=17.sp);if(i==1){SagaButton(if(stage==0)"Visitar A Forja de Darian (requer etapa 1)" else "Abrir A Forja de Darian",onForge,Modifier.fillMaxWidth(),style=if(stage>0)VikingButtonStyle.BronzeForjado else VikingButtonStyle.FerroRunico,leadingRune=HistoricalFutharkRune.Fehu)};if(i<3){val q=narrativeQuests[i];if(s.quests.none{it.id==q.id})SagaButton("Aceitar missão de ${dialogue.first.substringBefore(" ·")}",{vm.run{vm.repo.quest(q)}},Modifier.fillMaxWidth(),secondary=true)else Text("Missão no livro: ${q.name}",color=Muted,fontSize=12.sp)};if(stage<2)Action("Restaurar · ◈ ${(stage+1)*30}",{restore=i},enabled=s.coins>=(stage+1)*30,ceremonial=s.coins>=(stage+1)*30)else Eyebrow("Vida restaurada",Green)}}}
            2->{
                val nbCount=s.notebooks.count{!it.archived}
                val dueCount=dueFlashcards(s,currentDay()).size
                AetherSurface(SurfaceKind.Operational){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
                        MimirCodexArt(0,Modifier.size(54.dp),accent=Gold)
                        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
                            Eyebrow("A Biblioteca de Mímir",Gold)
                            Text("Conhecimento e Runas da Memória",style=MaterialTheme.typography.titleMedium)
                            Text("$nbCount ${if(nbCount==1)"caderno" else "cadernos"} · ${s.knowledgePages.count{!it.archived}} páginas${if(dueCount>0)" · $dueCount revisões pendentes" else ""}",color=if(dueCount>0)Blue else Muted,fontSize=12.sp)
                        }
                        HeraldicCrest(resolvedHeraldry(s),size=36.dp)
                    }
                    Action("Abrir Biblioteca de Mímir",onMimir,Modifier.fillMaxWidth())
                    SagaButton("Codex Rúnico (24 runas)",onCodex,Modifier.fillMaxWidth(),style=VikingButtonStyle.BronzeForjado,leadingRune=HistoricalFutharkRune.Ansuz)
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Action("Crônica da Semana",onWeekly,Modifier.weight(1f));SagaButton("Salão das Memórias",onMemories,Modifier.weight(1f),secondary=true)}
                NarrativePanel(s,vm);val revChap=chapterNames.indices.count{chapterUnlocked(it,s)};Text("$revChap de 9 ${if(revChap==1)"capítulo revelado" else "capítulos revelados"}",color=Blue);Meter(revChap/9f,Blue,slim=true);Column(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Eyebrow("O Cronista");Text("“Registrei o que caiu. Agora registro aquilo que se levanta.”",style=MaterialTheme.typography.titleLarge)};Hairline();chapterNames.forEachIndexed{i,name->val open=chapterUnlocked(i,s);Panel(accent=if(open)Gold else Muted){Text(name,style=MaterialTheme.typography.titleLarge);Text(when(i){0->"O encontro que inicia sua jornada.";1->"Liberado ao concluir seu primeiro hábito.";2->"Liberado ao restaurar a primeira etapa de uma construção.";3->"Liberado no nível 3.";4->"Liberado no nível 5.";5->"Liberado no nível 10.";6->"Liberado no nível 15.";7->"Liberado no nível 20.";else->"Liberado no nível 30."},color=Muted,fontSize=12.sp);Action(if(open)"Abrir capítulo" else "Ainda sob a névoa",{chapter=i},enabled=open)}}
            }
        }
    }
    chapter?.let{i->FullForm(chapterNames[i],{chapter=null},{chapter=null},"Fechar"){Eyebrow("Crônicas da Ascensão");Manuscript{RuneSeal(3,Sepia,Modifier.size(44.dp));Eyebrow("Capítulo ${i+1} de 9",Sepia)
 chapterTexts[i].split("\n\n").forEach{paragraph->Text(paragraph,fontFamily=if(paragraph.startsWith("—"))SagaFont else androidx.compose.ui.text.font.FontFamily.Default,fontSize=18.sp,lineHeight=29.sp,color=Sepia)}}}}
    restore?.let{i->AetherDialog(onDismissRequest={restore=null},title="Restaurar ${buildings[i]}?",kind=SurfaceKind.Ceremonial,seal=SealKind.Reward,confirmButton={SagaButton("Restaurar",{vm.run{vm.repo.restore(i)};restore=null},ceremonial=true)},dismissButton={SagaButton("Voltar",{restore=null},secondary=true)}){val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Text("Esta etapa custa ${(stage+1)*30} Fragmentos de Aether. A vila mudará com a restauração.",color=Muted)}}
}

