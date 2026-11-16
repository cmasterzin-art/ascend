package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*

@Composable fun WorldScreen(s:GameState,vm:GameViewModel){
    var tab by remember{mutableIntStateOf(0)};var region by remember{mutableIntStateOf(0)};var chapter by remember{mutableStateOf<Int?>(null)};var restore by remember{mutableStateOf<Int?>(null)}
    val Gold=Gold;val Green=Green
    Page{
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Title("Atlas de Aetheris","Entre fiordes, ruínas e caminhos velados.")};Text("◈ ${s.coins}",color=Gold,modifier=Modifier.padding(top=14.dp))}
        ChipRow(listOf("Mapa","Vila","Crônicas"),tab,{tab=it})
        when(tab){
            0->{
                AetherisMap(s,region,{region=it})
                Panel(accent=if(s.level>=regionLevels[region])Gold else Muted){Eyebrow(if(s.level>=regionLevels[region])"Região revelada" else "Na névoa · nível ${regionLevels[region]}");Text(regions[region],style=MaterialTheme.typography.titleLarge);Text(regionDescriptions[region],color=Muted,fontSize=14.sp);if(region==0){val stage=s.assets.filter{it.kind=="building"}.sumOf{it.stage};Meter(stage/10f,Green);Text("$stage de 10 etapas recuperadas",color=Green,fontSize=12.sp);Action("Visitar a vila",{tab=1})}else{val progress=(s.level.toFloat()/regionLevels[region]).coerceAtMost(1f);Meter(progress,Blue);Text(if(s.level>=regionLevels[region])"Região revelada. Seus marcos são registrados nas crônicas." else "Faltam ${(regionLevels[region]-s.level).coerceAtLeast(0)} níveis para revelar esta região.",color=Muted,fontSize=12.sp)}}
            }
            1->{Landscape(s,large=true);Eyebrow("Vila do Despertar");Text("Fragmentos restauram pedras. Seus hábitos devolvem sentido a elas.",color=Muted);buildings.forEachIndexed{i,name->val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Panel(accent=if(stage==2)Green else Gold){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,style=MaterialTheme.typography.titleLarge);Text(listOf("▤","◆","♧","♜","◇")[i],color=Gold,fontSize=24.sp)};Text(listOf("Ruínas · portas fechadas e memórias dispersas.","Estrutura recuperada · a vida começa a voltar.","Restauração completa · uma nova luz na vila.")[stage],color=Muted,fontSize=13.sp);Meter(stage/2f,Green);val dialogue=villagerDialogue(i,stage);Text(dialogue.first,color=Gold,fontSize=12.sp);Text("“${dialogue.second}”",fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=17.sp);if(i<3){val q=narrativeQuests[i];if(s.quests.none{it.id==q.id})TextButton({vm.run{vm.repo.quest(q)}}){Text("Aceitar missão de ${dialogue.first.substringBefore(" ·")}")}else Text("Missão no livro: ${q.name}",color=Muted,fontSize=12.sp)};if(stage<2)Action("Restaurar · ◈ ${(stage+1)*30}",{restore=i},enabled=s.coins>=(stage+1)*30)else Eyebrow("Vida restaurada",Green)}}}
            2->{Panel{Eyebrow("O Cronista");Text("“Registrei o que caiu. Agora registro aquilo que se levanta.”",style=MaterialTheme.typography.titleLarge)};chapterNames.forEachIndexed{i,name->val open=chapterUnlocked(i,s);Panel(accent=if(open)Gold else Muted){Text(name,style=MaterialTheme.typography.titleLarge);Text(when(i){0->"O encontro que inicia sua jornada.";1->"Liberado ao concluir seu primeiro hábito.";2->"Liberado ao restaurar a primeira etapa de uma construção.";3->"Liberado no nível 3.";4->"Liberado no nível 5.";5->"Liberado no nível 10.";6->"Liberado no nível 15.";7->"Liberado no nível 20.";else->"Liberado no nível 30."},color=Muted,fontSize=12.sp);Action(if(open)"Abrir capítulo" else "Ainda sob a névoa",{chapter=i},enabled=open)}}}
        }
    }
    chapter?.let{i->FullForm(chapterNames[i],{chapter=null},{chapter=null},"Fechar"){Eyebrow("Crônicas da Ascensão");Manuscript{RuneSeal(3,Sepia,Modifier.size(44.dp));Text(chapterTexts[i],fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=19.sp,lineHeight=31.sp)}}}
    restore?.let{i->AlertDialog(onDismissRequest={restore=null},title={Text("Restaurar ${buildings[i]}?")},text={val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Text("Esta etapa custa ${(stage+1)*30} Fragmentos de Aether. A vila mudará com a restauração.")},confirmButton={TextButton({vm.run{vm.repo.restore(i)};restore=null}){Text("Restaurar")}},dismissButton={TextButton({restore=null}){Text("Voltar")}})}
}
