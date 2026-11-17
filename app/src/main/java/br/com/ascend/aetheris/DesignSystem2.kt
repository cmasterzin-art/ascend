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
 val shape=androidx.compose.foundation.shape.CutCornerShape(if(kind==SurfaceKind.Operational)4.dp else 10.dp)
 Column(modifier.fillMaxWidth().clip(shape).background(Brush.linearGradient(shades)).forgeMaterial(kind).border(1.dp,accent.copy(alpha=.32f),shape).padding(AetherTokens.inset),verticalArrangement=Arrangement.spacedBy(12.dp)){
  if(kind!=SurfaceKind.Operational)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){Box(Modifier.width(26.dp).height(2.dp).background(accent));Text(when(kind){SurfaceKind.Heroic->"SAGA PESSOAL";SurfaceKind.Ceremonial->"MARCA DE AETHERIS";SurfaceKind.Parchment->"ARQUIVO DO CRONISTA";else->"COLEÇÃO DE ARTEFATOS"},color=accent,fontSize=10.sp,letterSpacing=1.5.sp)}
  CompositionLocalProvider(LocalContentColor provides if(kind==SurfaceKind.Parchment)Sepia else Ink){content()}
 }
}
@Composable fun MetricPair(a:String,al:String,b:String,bl:String){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)){Column(Modifier.weight(1f)){Text(a,fontSize=30.sp,lineHeight=36.sp,fontFamily=SagaFont);Text(al,color=Muted,fontSize=12.sp,lineHeight=18.sp)};Column(Modifier.weight(1f)){Text(b,fontSize=30.sp,lineHeight=36.sp,fontFamily=SagaFont);Text(bl,color=Muted,fontSize=12.sp,lineHeight=18.sp)}}}
@Composable fun SectionHeader(title:String,sub:String=""){
 Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,style=MaterialTheme.typography.titleLarge);if(sub.isNotBlank())Text(sub,color=Muted,fontSize=12.sp,lineHeight=19.sp)}
}
@Composable fun CompactHero(s:GameState,onPortrait:()->Unit={}){
 val p=requireNotNull(s.player)
 Box(Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.CutCornerShape(9.dp))){
  if(!LocalAppearance.current.minimal)Image(androidx.compose.ui.res.painterResource(R.drawable.nordic_fjord),null,Modifier.matchParentSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop)
  Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Surface.copy(alpha=.6f),Surface.copy(alpha=.94f)))).forgeMaterial(SurfaceKind.Heroic).padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   Box(Modifier.clickable(onClickLabel="Ampliar retrato",onClick=onPortrait)){ForgePortrait(s,64.dp)}
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(p.name,style=MaterialTheme.typography.titleLarge);Text("${classes[p.heroClass]} · nível ${s.level} · ${p.title}",color=Muted,fontSize=11.sp,lineHeight=16.sp);Meter(Progression.current(s.xp).toFloat()/Progression.requirement(s.level),Blue);Text("${Progression.current(s.xp)}/${Progression.requirement(s.level)} XP · ${s.coins} fragmentos",fontSize=11.sp,color=Blue)}
   ForgeMedallion(s.level)
  }
 }
}

@Composable fun MissionCard2(q:Quest,s:GameState,vm:GameViewModel,category:Int,value:Long,claimed:Boolean,onEdit:()->Unit){
 AetherSurface(if(category>=2)SurfaceKind.Ceremonial else SurfaceKind.Operational){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow(listOf("CONTRATO DIÁRIO","JURAMENTO SEMANAL","CONTRATO ÉPICO","DESAFIO RITUAL")[category],if(category>=2)Gold else Blue);RuneSeal(if(claimed)4 else category,if(claimed)Green else Gold,Modifier.size(32.dp))}
  Text(q.name,style=MaterialTheme.typography.titleLarge)
  StatusSeal(if(claimed)"Resgatado" else if(value>=q.target)"Recompensa disponível" else "Em andamento",if(claimed)Green else Blue)
  if(category==3){BossPortrait();Text("Vida: ${(100*(1-value.toFloat()/q.target)).coerceIn(0f,100f).toInt()} / 100",color=Muted)}
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){Text("${value.coerceAtMost(q.target.toLong())} / ${q.target}",fontSize=26.sp,fontFamily=SagaFont);Text(listOf("conclusões","minutos","XP de hábitos","páginas")[q.metric],color=Muted,fontSize=12.sp)}
  Meter(value.toFloat()/q.target,if(claimed)Green else Blue)
  ReadingBlock(q.description)
  val related=s.habits.filter{!it.deleted&&(q.habitIds.isBlank()||it.id in q.habitIds.split(','))&&(q.attribute<0||it.attribute==q.attribute)&&(q.metric!=1||it.type==2)&&(q.metric!=3||it.unit.contains("página",true))}
  Text("Contribuem: "+related.joinToString{it.name}.ifBlank{"Nenhum hábito compatível"},color=Muted,fontSize=12.sp)
  if(q.deadline.isNotBlank())Text("Prazo: ${displayDate(q.deadline)}",color=Muted,fontSize=12.sp)
  MetricPair("${q.xp} XP","recompensa final","${q.coins}","fragmentos")
  if(q.special.isNotBlank())Text("Troféu: ${q.special}",color=Gold,fontSize=13.sp)
  Action(if(claimed)"Recompensa resgatada" else "Resgatar recompensa",{vm.run{vm.repo.claim(q)}},Modifier.fillMaxWidth(),enabled=!claimed&&value>=q.target,completed=claimed)
  TextButton(onEdit){Text("Editar missão")}
 }
}
