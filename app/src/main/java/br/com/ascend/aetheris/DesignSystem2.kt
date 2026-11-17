package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

object AetherTokens{
 val page=20.dp;val gap=16.dp;val inset=18.dp;val small=8.dp;val touch=48.dp
 val shape=RoundedCornerShape(topStart=4.dp,topEnd=16.dp,bottomEnd=4.dp,bottomStart=16.dp)
 const val quick=140;const val scene=240
}
enum class SurfaceKind{Operational,Heroic,Ceremonial,Parchment,Artifact}
@Composable fun AetherSurface(kind:SurfaceKind=SurfaceKind.Operational,modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){
 val dark=LocalAppearance.current.dark
 val accent=when(kind){SurfaceKind.Heroic->Blue;SurfaceKind.Ceremonial->Gold;SurfaceKind.Parchment->Sepia;SurfaceKind.Artifact->Violet;else->ColdIron}
 val base=when(kind){SurfaceKind.Parchment->if(dark)Color(0xFFD0C3A5) else Color(0xFFF2E5C7);SurfaceKind.Ceremonial->if(dark)Color(0xFF29211C) else Color(0xFFE1D5BF);SurfaceKind.Artifact->if(dark)Color(0xFF211D25) else Color(0xFFE8E0D5);else->Surface}
 val shades=if(kind==SurfaceKind.Parchment)listOf(if(dark)Color(0xFFB7AA8D) else Color(0xFFE0CDA6),base,if(dark)Color(0xFFDBCEB0) else Color(0xFFF2E5C7)) else listOf(base,base.copy(alpha=.7f))
 val shape=androidx.compose.foundation.shape.CutCornerShape(if(kind==SurfaceKind.Operational)3.dp else 6.dp)
 Column(modifier.fillMaxWidth().clip(shape).background(Brush.linearGradient(shades)).forgeMaterial(kind).border(1.dp,accent.copy(alpha=.18f),shape).padding(AetherTokens.inset),verticalArrangement=Arrangement.spacedBy(12.dp)){
  if(kind!=SurfaceKind.Operational&&kind!=SurfaceKind.Parchment)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Text(when(kind){SurfaceKind.Heroic->"SAGA PESSOAL";SurfaceKind.Ceremonial->"MARCA DE AETHERIS";SurfaceKind.Parchment->"ARQUIVO DO CRONISTA";else->"COLEÇÃO DE ARTEFATOS"},color=accent,fontSize=9.sp,letterSpacing=2.sp)}
  CompositionLocalProvider(LocalContentColor provides if(kind==SurfaceKind.Parchment)Sepia else Ink){content()}
 }
}
@Composable fun MetricPair(a:String,al:String,b:String,bl:String){val muted=if(LocalContentColor.current==Sepia)Sepia.copy(alpha=.75f) else Muted;Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)){Column(Modifier.weight(1f)){Text(a,fontSize=30.sp,lineHeight=36.sp,fontFamily=SagaFont);Text(al,color=muted,fontSize=12.sp,lineHeight=18.sp)};Column(Modifier.weight(1f)){Text(b,fontSize=30.sp,lineHeight=36.sp,fontFamily=SagaFont);Text(bl,color=muted,fontSize=12.sp,lineHeight=18.sp)}}}
@Composable fun SectionHeader(title:String,sub:String=""){
 Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,style=MaterialTheme.typography.titleLarge);if(sub.isNotBlank())Text(sub,color=Muted,fontSize=12.sp,lineHeight=19.sp)}
}
@Composable fun CompactHero(s:GameState,onPortrait:()->Unit={}){
 val p=requireNotNull(s.player)
 Box(Modifier.fillMaxWidth().clip(CutCornerShape(6.dp)).border(1.dp,ColdIron.copy(alpha=.3f),CutCornerShape(6.dp))){
  if(!LocalAppearance.current.minimal)Image(androidx.compose.ui.res.painterResource(R.drawable.nordic_fjord),null,Modifier.matchParentSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
  Column(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Surface.copy(alpha=.25f),Surface.copy(alpha=.94f)))).padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
    Box(Modifier.clickable(onClickLabel="Ampliar retrato",onClick=onPortrait)){ForgePortrait(s,64.dp)}
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){
     Text(p.name,style=MaterialTheme.typography.headlineMedium)
     Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){ClassEmblem(p.heroClass,Modifier.size(20.dp));Text(classes[p.heroClass],color=Blue,fontSize=12.sp)}
     Text(p.title,color=Muted,fontSize=12.sp)
    }
    ForgeMedallion(s.level)
   }
   Column(Modifier.fillMaxWidth().background(Surface.copy(alpha=.8f)).padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("${Progression.current(s.xp)}/${Progression.requirement(s.level)} XP",color=Blue,fontSize=11.sp);Text("${s.coins} fragmentos",fontSize=11.sp,color=Muted)}
    Meter(Progression.current(s.xp).toFloat()/Progression.requirement(s.level),Blue)
   }
  }
 }
}

@Composable fun MissionCard2(q:Quest,s:GameState,vm:GameViewModel,category:Int,value:Long,claimed:Boolean,onEdit:()->Unit){
 val contractInk=if(category==0)Sepia else Ink;val contractMuted=if(category==0)Sepia.copy(alpha=.75f) else Muted
 AetherSurface(if(category>=1)SurfaceKind.Ceremonial else SurfaceKind.Parchment){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow(listOf("CONTRATO DIÁRIO","JURAMENTO SEMANAL","CONTRATO ÉPICO","DESAFIO RITUAL")[category],if(category==0)Sepia else Gold);RuneSeal(if(claimed)4 else category,if(category==0)Sepia else if(claimed)Green else Gold,Modifier.size(32.dp))}
  Text(q.name,color=contractInk,style=MaterialTheme.typography.titleLarge)
  StatusSeal(if(claimed)"Resgatado" else if(value>=q.target)"Recompensa disponível" else "Em andamento",if(category==0)Sepia else if(claimed)Green else Blue)
  if(category==3){BossPortrait();Text("Vida: ${(100*(1-value.toFloat()/q.target)).coerceIn(0f,100f).toInt()} / 100",color=contractMuted)}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){Text("${value.coerceAtMost(q.target.toLong())} / ${q.target}",fontSize=26.sp,fontFamily=SagaFont);Text(listOf("conclusões","minutos","XP de hábitos","páginas")[q.metric],color=contractMuted,fontSize=12.sp)}
  Meter(value.toFloat()/q.target,if(claimed)Green else Blue)
  Text(q.description,color=contractInk,fontSize=15.sp,lineHeight=25.sp)
  val related=s.habits.filter{!it.deleted&&(q.habitIds.isBlank()||it.id in q.habitIds.split(','))&&(q.attribute<0||it.attribute==q.attribute)&&(q.metric!=1||it.type==2)&&(q.metric!=3||it.unit.contains("página",true))}
  Text("Contribuem: "+related.joinToString{it.name}.ifBlank{"Nenhum hábito compatível"},color=contractMuted,fontSize=12.sp)
  if(q.deadline.isNotBlank())Text("Prazo: ${displayDate(q.deadline)}",color=contractMuted,fontSize=12.sp)
  MetricPair("${q.xp} XP","recompensa final","${q.coins}","fragmentos")
  if(q.special.isNotBlank())Text("Troféu: ${q.special}",color=Gold,fontSize=13.sp)
  Action(if(claimed)"Recompensa resgatada" else "Resgatar recompensa",{vm.run{vm.repo.claim(q)}},Modifier.fillMaxWidth(),enabled=!claimed&&value>=q.target,completed=claimed)
  TextButton(onEdit){Text("Editar missão")}
 }
}
