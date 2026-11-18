package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.*
import androidx.compose.ui.layout.layout
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
 val base=when(kind){SurfaceKind.Parchment->if(dark)Color(0xFFD0C3A5) else Color(0xFFF2E5C7);SurfaceKind.Ceremonial->if(dark)Color(0xFF29211C) else Color(0xFFE1D5BF);SurfaceKind.Heroic->if(dark)Color(0xFF1A2026) else Color(0xFFE3E7EC);SurfaceKind.Artifact->if(dark)Color(0xFF211D25) else Color(0xFFE8E0D5);else->Surface}
 val shades=if(kind==SurfaceKind.Parchment)listOf(if(dark)Color(0xFFB7AA8D) else Color(0xFFE0CDA6),base,if(dark)Color(0xFFDBCEB0) else Color(0xFFF2E5C7)) else listOf(base,base.copy(alpha=.74f))
 val shape=androidx.compose.foundation.shape.CutCornerShape(if(kind==SurfaceKind.Operational)4.dp else 6.dp)
 val borderAlpha=when(kind){SurfaceKind.Ceremonial->.32f;SurfaceKind.Heroic->.28f;SurfaceKind.Artifact->.26f;SurfaceKind.Parchment->.24f;else->.2f}
 Column(modifier.fillMaxWidth().clip(shape).background(Brush.linearGradient(shades)).forgeMaterial(kind).border(1.dp,accent.copy(alpha=borderAlpha),shape).padding(AetherTokens.inset),verticalArrangement=Arrangement.spacedBy(12.dp)){
  if(kind!=SurfaceKind.Operational&&kind!=SurfaceKind.Parchment)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Box(Modifier.width(12.dp).height(1.dp).background(accent.copy(alpha=.55f)))
   Text(when(kind){SurfaceKind.Heroic->"SAGA PESSOAL";SurfaceKind.Ceremonial->"MARCA DE AETHERIS";SurfaceKind.Parchment->"ARQUIVO DO CRONISTA";else->"COLEÇÃO DE ARTEFATOS"},color=accent,fontSize=9.sp,letterSpacing=2.sp,fontWeight=FontWeight.SemiBold)
  }
  CompositionLocalProvider(LocalContentColor provides if(kind==SurfaceKind.Parchment)Sepia else Ink){content()}
 }
}
/** Shared Nordic dialog plate: preserves AlertDialog window semantics for Robolectric while replacing M3 rounded cards with forged stone/iron/ceremonial surfaces. */
@Composable fun AetherDialog(onDismissRequest:()->Unit,title:String,kind:SurfaceKind=SurfaceKind.Operational,seal:SealKind?=null,confirmButton:@Composable ()->Unit,dismissButton:(@Composable ()->Unit)?=null,content:@Composable ColumnScope.()->Unit){
 val dark=LocalAppearance.current.dark
 val accent=when(kind){SurfaceKind.Ceremonial->Gold;SurfaceKind.Heroic->Blue;SurfaceKind.Artifact->Violet;SurfaceKind.Parchment->Sepia;else->ColdIron}
 val container=when(kind){SurfaceKind.Ceremonial->if(dark)Color(0xFF231C17) else Color(0xFFE8DEC9);SurfaceKind.Artifact->if(dark)Color(0xFF1D1A22) else Color(0xFFE8E2D8);else->if(dark)Color(0xFF16191B) else Color(0xFFECE6DA)}
 val shape=CutCornerShape(topStart=8.dp,topEnd=3.dp,bottomEnd=8.dp,bottomStart=3.dp)
 AlertDialog(
  onDismissRequest=onDismissRequest,
  modifier=Modifier.pictorialFrame(R.drawable.ui_frame_dialog,capXRatio=.18f,capYRatio=.18f,alpha=if(dark).9f else .18f).border(1.dp,accent.copy(alpha=.45f),shape),
  shape=shape,
  containerColor=container,
  titleContentColor=Ink,
  textContentColor=Ink,
  title={
   Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
     if(seal!=null)CeremonialSeal(seal,true,Modifier.size(34.dp))
     else Box(Modifier.width(3.dp).height(20.dp).background(accent))
     Text(title,style=MaterialTheme.typography.titleLarge,color=if(kind==SurfaceKind.Ceremonial)Gold else Ink)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(accent.copy(alpha=.45f),accent.copy(alpha=.12f),Color.Transparent))))
   }
  },
  text={Column(verticalArrangement=Arrangement.spacedBy(10.dp),content=content)},
  confirmButton=confirmButton,
  dismissButton=dismissButton
 )
}
@Composable fun MetricPair(a:String,al:String,b:String,bl:String){val muted=if(LocalContentColor.current==Sepia)Sepia.copy(alpha=.75f) else Muted;Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(16.dp)){Column(Modifier.weight(1f)){Text(a,fontSize=30.sp,lineHeight=36.sp,fontFamily=SagaFont);Text(al,color=muted,fontSize=12.sp,lineHeight=18.sp)};Column(Modifier.weight(1f)){Text(b,fontSize=30.sp,lineHeight=36.sp,fontFamily=SagaFont);Text(bl,color=muted,fontSize=12.sp,lineHeight=18.sp)}}}
@Composable fun SectionHeader(title:String,sub:String=""){
 Column(verticalArrangement=Arrangement.spacedBy(5.dp)){Text(title,style=MaterialTheme.typography.titleLarge);if(sub.isNotBlank())Text(sub,color=Muted,fontSize=12.sp,lineHeight=19.sp)}
}
/** The fjord is not a card: it bleeds to the screen edges and dissolves into the page through mist.
 *  1.6.9.10: longer mist falloff, side vignettes, a cold key light behind the portrait and one editorial identity block. */
@Composable fun CompactHero(s:GameState,onPortrait:()->Unit={}){
 val p=requireNotNull(s.player);val minimal=LocalAppearance.current.minimal;val night=Night;val dark=LocalAppearance.current.dark
 val cold=if(dark)Color(0xFFB8C7D9) else Color(0xFFFFFFFF)
 BoxWithConstraints(Modifier.fillMaxWidth().layout{m,c->val bleed=20.dp.roundToPx();val placeable=m.measure(c.copy(minWidth=c.maxWidth+2*bleed,maxWidth=c.maxWidth+2*bleed));layout(c.maxWidth,placeable.height){placeable.place(-bleed,0)}}){
  val narrow=maxWidth<360.dp;val portrait=if(narrow)54.dp else 70.dp
  if(!minimal)Image(androidx.compose.ui.res.painterResource(R.drawable.nordic_fjord),null,Modifier.matchParentSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop,alignment=Alignment.TopCenter,alpha=.92f)
  // Mist: sky stays readable, the valley sinks into the page colour with no visible edge.
  Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to night.copy(alpha=.78f),.18f to night.copy(alpha=.12f),.48f to night.copy(alpha=.3f),.78f to night.copy(alpha=.86f),1f to night)))
  Box(Modifier.matchParentSize().background(Brush.horizontalGradient(0f to night.copy(alpha=.55f),.22f to Color.Transparent,.78f to Color.Transparent,1f to night.copy(alpha=.6f))))
  Column(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(top=if(narrow)66.dp else 74.dp,bottom=4.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(if(narrow)10.dp else 14.dp)){
    Box(Modifier.drawBehind{if(!minimal){val r=size.maxDimension*.95f;drawCircle(Brush.radialGradient(listOf(cold.copy(alpha=.2f),Color.Transparent),center=Offset(size.width/2,size.height*.4f),radius=r),r,Offset(size.width/2,size.height*.4f))}}.clickable(onClickLabel="Ampliar retrato",onClick=onPortrait)){ForgePortrait(s,portrait)}
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
     Text(p.name,style=if(narrow)MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,letterSpacing=1.sp,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
     Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){ClassEmblem(p.heroClass,Modifier.size(18.dp));Text(classes[p.heroClass],color=Blue,fontSize=12.sp);if(!narrow){Text("·",color=Muted,fontSize=12.sp);Text(p.title,color=Muted,fontSize=12.sp,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)}}
     if(narrow)Text(p.title,color=Muted,fontSize=11.sp,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
     Spacer(Modifier.height(3.dp))
     Meter(Progression.current(s.xp).toFloat()/Progression.requirement(s.level),Blue,slim=true)
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("${Progression.current(s.xp)}/${Progression.requirement(s.level)} XP",color=Blue.copy(alpha=.9f),fontSize=if(narrow)10.sp else 11.sp,letterSpacing=if(narrow)0.sp else .4.sp,maxLines=1);Text("${s.coins} ${if(narrow)"frag." else "fragmentos"}",fontSize=if(narrow)10.sp else 11.sp,color=Muted,maxLines=1)}
    }
    Box(Modifier.semantics(mergeDescendants=true){contentDescription="Nível ${s.level}"}){ForgeMedallion(s.level,Modifier.size(if(narrow)40.dp else 48.dp))}
   }
  }
 }
}
/** 1.6.9.10 · Daily progress read as a line of a chronicle, not as a panel. */
@Composable fun DayLedger(done:Int,total:Int,rested:Boolean,relaxed:Boolean,dial:Dp){
 val pending=(total-done).coerceAtLeast(0)
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
  DayDial(if(total==0)0f else done.toFloat()/total,if(total==0)"—" else "${done*100/total}%",dial)
  Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
   Eyebrow(if(rested)"Dia de descanso" else "Jornada de hoje",Green)
   Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(8.dp)){Text("$done de $total",style=MaterialTheme.typography.titleLarge);Text("$pending ${if(pending==1)"pendente" else "pendentes"}",fontSize=12.sp,color=Muted,modifier=Modifier.padding(bottom=3.dp))}
   if(relaxed)Text("Modo livre · o widget mantém a agenda programada.",fontSize=11.sp,color=Muted)
  }
 }
}

/** 1.6.9.10 · A contract is one page of the codex: heading, oath, progress and reward read in a single editorial column.
 *  Daily contracts stay on vellum; weekly/epic oaths are bound in leather with a bronze spine. Same data and actions. */
@Composable fun MissionCard2(q:Quest,s:GameState,vm:GameViewModel,category:Int,value:Long,claimed:Boolean,onEdit:()->Unit){
 val contractInk=if(category==0)Sepia else Ink;val contractMuted=if(category==0)Sepia.copy(alpha=.75f) else Muted
 val grand=category==1||category==2
 AetherSurface(if(category>=1)SurfaceKind.Ceremonial else SurfaceKind.Parchment,if(category==2)Modifier.border(1.dp,Gold.copy(alpha=.5f),CutCornerShape(6.dp)) else Modifier){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
   CeremonialSeal(if(claimed)SealKind.Reward else listOf(SealKind.Mission,SealKind.Oath,SealKind.Saga,SealKind.Mission)[category],claimed||value>=q.target,Modifier.size(if(grand)46.dp else 34.dp))
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
    Eyebrow(listOf("CONTRATO DIÁRIO","JURAMENTO SEMANAL","CONTRATO ÉPICO","DESAFIO RITUAL")[category],if(category==0)Sepia else Gold)
    Text(q.name,color=contractInk,style=if(grand)MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge)
   }
  }
  if(category==3){BossPortrait();Text("Vida: ${(100*(1-value.toFloat()/q.target)).coerceIn(0f,100f).toInt()} / 100",color=contractMuted)}
  Text(q.description,color=contractInk,fontSize=15.sp,lineHeight=24.sp)
  Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){
    Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(6.dp)){Text("${value.coerceAtMost(q.target.toLong())} / ${q.target}",fontSize=22.sp,fontFamily=SagaFont,color=contractInk);Text(listOf("conclusões","minutos","XP de hábitos","páginas")[q.metric],color=contractMuted,fontSize=12.sp,modifier=Modifier.padding(bottom=3.dp))}
    StatusSeal(if(claimed)"Resgatado" else if(value>=q.target)"Recompensa disponível" else "Em andamento",if(category==0)Sepia else if(claimed)Green else Blue)
   }
   Meter(value.toFloat()/q.target,if(claimed)Green else if(category==0)Sepia else Blue,slim=!grand)
  }
  val related=s.habits.filter{!it.deleted&&(q.habitIds.isBlank()||it.id in q.habitIds.split(','))&&(q.attribute<0||it.attribute==q.attribute)&&(q.metric!=1||it.type==2)&&(q.metric!=3||it.unit.contains("página",true))}
  Text("Contribuem: "+related.joinToString{it.name}.ifBlank{"Nenhum hábito compatível"},color=contractMuted,fontSize=12.sp,lineHeight=18.sp)
  if(q.deadline.isNotBlank())Text("Prazo: ${displayDate(q.deadline)}",color=contractMuted,fontSize=12.sp)
  Box(Modifier.fillMaxWidth().height(1.dp).background((if(category==0)Sepia else Gold).copy(alpha=.22f)))
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(18.dp)){
   Column{Text("${q.xp} XP",fontSize=if(grand)22.sp else 18.sp,lineHeight=24.sp,fontFamily=SagaFont,color=contractInk);Text("recompensa final",color=contractMuted,fontSize=11.sp)}
   Column{Text("${q.coins}",fontSize=if(grand)22.sp else 18.sp,lineHeight=24.sp,fontFamily=SagaFont,color=contractInk);Text("fragmentos",color=contractMuted,fontSize=11.sp)}
  }
  if(q.special.isNotBlank())Text("Troféu: ${q.special}",color=Gold,fontSize=13.sp)
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
   Action(if(claimed)"Recompensa resgatada" else "Resgatar recompensa",{vm.run{vm.repo.claim(q)}},Modifier.weight(1f),enabled=!claimed&&value>=q.target,completed=claimed,ceremonial=!claimed&&value>=q.target)
   TextButton(onEdit){Text("Editar missão",color=contractMuted)}
  }
 }
}
