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
    Page{
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Title("Atlas de Aetheris","O mundo recorda cada retorno.");Text("◈ ${s.coins}",color=Gold,modifier=Modifier.padding(top=14.dp))}
        ChipRow(listOf("Mapa","Vila","Crônicas"),tab,{tab=it})
        when(tab){
            0->{
                BoxWithConstraints(Modifier.fillMaxWidth().height(390.dp).border(1.dp,Gold.copy(alpha=.2f),RoundedCornerShape(20.dp)).background(Surface,RoundedCornerShape(20.dp))){
                    val xs=listOf(.23f,.65f,.32f,.74f,.25f,.68f);val ys=listOf(.83f,.7f,.53f,.38f,.22f,.08f)
                    Canvas(Modifier.matchParentSize()){
                        val path=Path().apply{moveTo(size.width*xs[0],size.height*ys[0]);for(i in 1..5)quadraticTo(size.width*xs[i-1],size.height*ys[i],size.width*xs[i],size.height*ys[i])};drawPath(path,Gold.copy(alpha=.28f),style=androidx.compose.ui.graphics.drawscope.Stroke(3f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(8f,9f))))
                        repeat(34){i->val x=((i*137+61)%997)/997f*size.width;val y=((i*73+23)%971)/971f*size.height;val p=Path().apply{moveTo(x-6,y+8);lineTo(x,y-7);lineTo(x+6,y+8);close()};drawPath(p,Green.copy(alpha=.12f))}
                    }
                    for(i in regions.indices){val open=s.level>=regionLevels[i];Column(Modifier.offset(x=maxWidth*xs[i]-44.dp,y=390.dp*ys[i]-20.dp).width(88.dp).clickable{region=i},horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(40.dp).border(if(region==i)2.dp else 1.dp,if(open)Gold else Muted.copy(alpha=.25f),CircleShape).background(if(open)CardColor else Night,CircleShape),contentAlignment=Alignment.Center){Text(if(open)listOf("⌂","♧","▤","▲","☽","♜")[i] else "◇",color=if(open)Gold else Muted,fontSize=22.sp)};Text(if(open)regions[i].substringBefore(" ") else "Nível ${regionLevels[i]}",color=if(open)Ink else Muted,fontSize=10.sp)} }
                }
                Panel(accent=if(s.level>=regionLevels[region])Gold else Muted){Eyebrow(if(s.level>=regionLevels[region])"Região revelada" else "Na névoa · nível ${regionLevels[region]}");Text(regions[region],style=MaterialTheme.typography.titleLarge);Text(regionDescriptions[region],color=Muted,fontSize=14.sp);if(region==0){val stage=s.assets.filter{it.kind=="building"}.sumOf{it.stage};Meter(stage/10f,Green);Text("$stage de 10 etapas recuperadas",color=Green,fontSize=12.sp);Action("Visitar a vila",{tab=1})}else{val progress=(s.level.toFloat()/regionLevels[region]).coerceAtMost(1f);Meter(progress,Blue);Text(if(s.level>=regionLevels[region])"Região revelada. Seus marcos são registrados nas crônicas." else "Faltam ${(regionLevels[region]-s.level).coerceAtLeast(0)} níveis para revelar esta região.",color=Muted,fontSize=12.sp)}}
            }
            1->{Landscape(s,large=true);Eyebrow("Vila do Despertar");Text("Fragmentos restauram pedras. Seus hábitos devolvem sentido a elas.",color=Muted);buildings.forEachIndexed{i,name->val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Panel(accent=if(stage==2)Green else Gold){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(name,style=MaterialTheme.typography.titleLarge);Text(listOf("▤","◆","♧","♜","◇")[i],color=Gold,fontSize=24.sp)};Text(listOf("Ruínas · portas fechadas e memórias dispersas.","Estrutura recuperada · a vida começa a voltar.","Restauração completa · uma nova luz na vila.")[stage],color=Muted,fontSize=13.sp);Meter(stage/2f,Green);if(stage<2)Action("Restaurar · ◈ ${(stage+1)*30}",{restore=i},enabled=s.coins>=(stage+1)*30)else Eyebrow("Vida restaurada",Green)}}}
            2->{Panel{Eyebrow("O Cronista");Text("“Registrei o que caiu. Agora registro aquilo que se levanta.”",style=MaterialTheme.typography.titleLarge)};chapterNames.forEachIndexed{i,name->val open=chapterUnlocked(i,s);Panel(accent=if(open)Gold else Muted){Text(name,style=MaterialTheme.typography.titleLarge);Text(when(i){0->"O encontro que inicia sua jornada.";1->"Liberado ao concluir seu primeiro hábito.";2->"Liberado ao restaurar a primeira etapa de uma construção.";3->"Liberado no nível 3.";else->"Liberado no nível 5."},color=Muted,fontSize=12.sp);Action(if(open)"Abrir capítulo" else "Ainda sob a névoa",{chapter=i},enabled=open)}}}
        }
    }
    chapter?.let{i->FullForm(chapterNames[i],{chapter=null},{chapter=null},"Fechar"){Eyebrow("Crônicas da Ascensão");Text(chapterTexts[i],fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=20.sp,lineHeight=32.sp)}}
    restore?.let{i->AlertDialog(onDismissRequest={restore=null},title={Text("Restaurar ${buildings[i]}?")},text={val stage=s.assets.find{it.id=="building:$i"}?.stage?:0;Text("Esta etapa custa ${(stage+1)*30} Fragmentos de Aether. A vila mudará com a restauração.")},confirmButton={TextButton({vm.run{vm.repo.restore(i)};restore=null}){Text("Restaurar")}},dismissButton={TextButton({restore=null}){Text("Voltar")}})}
}
