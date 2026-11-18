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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*

@Composable fun WorldScreen(s:GameState,vm:GameViewModel,onWeekly:()->Unit={},onMemories:()->Unit={},onRegion:(Int)->Unit={}){
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
                Column(Modifier.fillMaxWidth().padding(top=4.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Eyebrow(if(s.level>=regionLevels[region])"Região revelada" else "Na névoa · nível ${regionLevels[region]}",if(s.level>=regionLevels[region])Gold else Muted);Text(regions[region],style=MaterialTheme.typography.headlineMedium);Text(regionDescriptions[region],color=Muted,fontSize=14.sp,lineHeight=22.sp);Text(if(region==1)"Exploração · ${forestProgress(s)}% · 3 locais" else "Expedições próprias: ainda não disponíveis",color=Blue,fontSize=12.sp);if(region==0){val stage=s.assets.filter{it.kind=="building"}.sumOf{it.stage};Meter(stage/10f,Green,slim=true);Text("$stage de 10 etapas recuperadas",color=Green,fontSize=12.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Action("Explorar região",{onRegion(region)},Modifier.weight(1f),enabled=s.level>=regionLevels[region]);SagaButton("Visitar a vila",{tab=1},Modifier.weight(1f),secondary=true)}}else{val progress=(s.level.toFloat()/regionLevels[region]).coerceAtMost(1f);val rem=(regionLevels[region]-s.level).coerceAtLeast(0);Meter(progress,Blue,slim=true);Text(if(s.level>=regionLevels[region])"Região revelada. Seus marcos são registrados nas crônicas." else "${if(rem==1)"Falta 1 nível" else "Faltam $rem níveis"} para revelar esta região.",color=Muted,fontSize=12.sp);Action("Explorar região",{onRegion(region)},enabled=s.level>=regionLevels[region])}}
            }
            1->{Landscape(s,large=true);Eyebrow("Vila do Despertar");Text("Fragmentos restauram pedras. Seus hábitos devolvem sentido a elas.",color=Muted);buildings.forEachIndexed{i,name->val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Panel(accent=if(stage==2)Green else Gold){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,style=MaterialTheme.typography.titleLarge);Text(listOf("▤","◆","♧","♜","◇")[i],color=Gold,fontSize=24.sp)};Crossfade(stage,animationSpec=tween(if(LocalAppearance.current.reducedMotion)0 else 450),label="restauração"){shown->BuildingScene(i,shown)};Text(listOf("Ruínas · portas fechadas e memórias dispersas.","Estrutura recuperada · a vida começa a voltar.","Restauração completa · uma nova luz na vila.")[stage],color=Muted,fontSize=13.sp);Meter(stage/2f,Green,slim=true);val dialogue=villagerDialogue(i,stage);Text(dialogue.first,color=Gold,fontSize=12.sp);Text("“${dialogue.second}”",fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=17.sp);if(i<3){val q=narrativeQuests[i];if(s.quests.none{it.id==q.id})SagaButton("Aceitar missão de ${dialogue.first.substringBefore(" ·")}",{vm.run{vm.repo.quest(q)}},Modifier.fillMaxWidth(),secondary=true)else Text("Missão no livro: ${q.name}",color=Muted,fontSize=12.sp)};if(stage<2)Action("Restaurar · ◈ ${(stage+1)*30}",{restore=i},enabled=s.coins>=(stage+1)*30,ceremonial=s.coins>=(stage+1)*30)else Eyebrow("Vida restaurada",Green)}}}
            2->{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Action("Crônica da Semana",onWeekly,Modifier.weight(1f));SagaButton("Salão das Memórias",onMemories,Modifier.weight(1f),secondary=true)};NarrativePanel(s,vm);val revChap=chapterNames.indices.count{chapterUnlocked(it,s)};Text("$revChap de 9 ${if(revChap==1)"capítulo revelado" else "capítulos revelados"}",color=Blue);Meter(revChap/9f,Blue,slim=true);Column(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Eyebrow("O Cronista");Text("“Registrei o que caiu. Agora registro aquilo que se levanta.”",style=MaterialTheme.typography.titleLarge)};Hairline();chapterNames.forEachIndexed{i,name->val open=chapterUnlocked(i,s);Panel(accent=if(open)Gold else Muted){Text(name,style=MaterialTheme.typography.titleLarge);Text(when(i){0->"O encontro que inicia sua jornada.";1->"Liberado ao concluir seu primeiro hábito.";2->"Liberado ao restaurar a primeira etapa de uma construção.";3->"Liberado no nível 3.";4->"Liberado no nível 5.";5->"Liberado no nível 10.";6->"Liberado no nível 15.";7->"Liberado no nível 20.";else->"Liberado no nível 30."},color=Muted,fontSize=12.sp);Action(if(open)"Abrir capítulo" else "Ainda sob a névoa",{chapter=i},enabled=open)}}}
        }
    }
    chapter?.let{i->FullForm(chapterNames[i],{chapter=null},{chapter=null},"Fechar"){Eyebrow("Crônicas da Ascensão");Manuscript{RuneSeal(3,Sepia,Modifier.size(44.dp));Eyebrow("Capítulo ${i+1} de 9",Sepia)
 chapterTexts[i].split("\n\n").forEach{paragraph->Text(paragraph,fontFamily=if(paragraph.startsWith("—"))SagaFont else androidx.compose.ui.text.font.FontFamily.Default,fontSize=18.sp,lineHeight=29.sp,color=Sepia)}}}}
    restore?.let{i->AetherDialog("Restaurar ${buildings[i]}?",{restore=null},seal=SealKind.Village,confirm={SagaButton("Restaurar",{vm.run{vm.repo.restore(i)};restore=null},ceremonial=true)},dismiss={SagaButton("Voltar",{restore=null},secondary=true)}){val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Text("Esta etapa custa ${(stage+1)*30} Fragmentos de Aether. A vila mudará com a restauração.",color=Muted)}}
}

