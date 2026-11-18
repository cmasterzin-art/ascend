package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*

/** 1.6.9.10 · Presentation-only character sheet pieces. They read existing GameState values; no rules live here. */

private val hairline:Color @Composable get()=if(LocalAppearance.current.dark)Color.White.copy(alpha=.07f) else Color.Black.copy(alpha=.1f)
@Composable fun Hairline(modifier:Modifier=Modifier){val h=hairline;Box(modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,h,h,Color.Transparent))))}

/** Portrait and identity share one stage: fjord mist behind, cold key light on the portrait, inscriptions to its side. */
@Composable fun HeroStage(s:GameState,onPortrait:()->Unit){
 val hero=requireNotNull(s.player);val minimal=LocalAppearance.current.minimal;val night=Night;val dark=LocalAppearance.current.dark
 val cold=if(dark)Color(0xFFB8C7D9) else Color.White
 BoxWithConstraints(Modifier.fillMaxWidth().layout{m,c->val bleed=20.dp.roundToPx();val placeable=m.measure(c.copy(minWidth=c.maxWidth+2*bleed,maxWidth=c.maxWidth+2*bleed));layout(c.maxWidth,placeable.height){placeable.place(-bleed,0)}}){
  val narrow=maxWidth<360.dp;val portrait=if(narrow)92.dp else 116.dp
  if(!minimal)Image(painterResource(R.drawable.nordic_fjord),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop,alignment=Alignment.TopCenter,alpha=.52f)
  // Every edge of the painting dissolves into the page: no visible rectangle.
  Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to night,.28f to night.copy(alpha=.45f),.68f to night.copy(alpha=.55f),1f to night)))
  Box(Modifier.matchParentSize().background(Brush.horizontalGradient(0f to night.copy(alpha=.7f),.22f to Color.Transparent,.72f to night.copy(alpha=.35f),1f to night.copy(alpha=.85f))))
  Row(Modifier.fillMaxWidth().padding(horizontal=20.dp).padding(top=14.dp,bottom=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
   Box(Modifier.drawBehind{if(!minimal){val r=size.maxDimension*.85f;drawCircle(Brush.radialGradient(listOf(cold.copy(alpha=.24f),Color.Transparent),center=Offset(size.width/2,size.height*.38f),radius=r),r,Offset(size.width/2,size.height*.38f))}}.clickable(onClickLabel="Ampliar retrato",onClick=onPortrait)){ForgePortrait(s,portrait)}
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Eyebrow("Saga pessoal",Muted.copy(alpha=.8f))
    Text(hero.name,style=MaterialTheme.typography.headlineMedium,letterSpacing=1.sp,maxLines=2,overflow=TextOverflow.Ellipsis)
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){ClassEmblem(hero.heroClass,Modifier.size(20.dp));Text(classes[hero.heroClass],color=Blue,fontSize=13.sp)}
    Text(hero.title,color=Muted,fontSize=12.sp)
    Row(Modifier.padding(top=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
     Box(Modifier.semantics(mergeDescendants=true){contentDescription="Nível ${s.level}"}){ForgeMedallion(s.level,Modifier.size(40.dp))}
     Column{Text("${s.coins}",fontFamily=SagaFont,fontSize=18.sp,lineHeight=22.sp);Text("fragmentos",color=Muted,fontSize=11.sp)}
    }
   }
  }
 }
}

/** Evolution as one engraved line: XP hairline plus its exact figures. */
@Composable fun HeroEvolution(s:GameState){
 Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){Text("Nível ${s.level}",fontFamily=SagaFont,fontSize=16.sp,color=Ink);Text("${Progression.current(s.xp)}/${Progression.requirement(s.level)} XP neste nível",color=Blue,fontSize=11.sp)}
  Meter(Progression.current(s.xp).toFloat()/Progression.requirement(s.level),Blue,slim=true)
  Text("${s.xp} XP total",color=Muted,fontSize=11.sp)
 }
}

/** Attributes inscribed like a ledger page: emblem, name, rank, hairline progress. No independent cards. */
@Composable fun AttributeInscriptions(s:GameState){
 Column(Modifier.fillMaxWidth()){
  Eyebrow("Inscrições dos atributos",Muted.copy(alpha=.85f))
  attributes.forEachIndexed{i,a->val pts=s.points(i);val col=habitColor(i)
   Row(Modifier.fillMaxWidth().padding(vertical=8.dp).semantics(mergeDescendants=true){},verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Box(Modifier.size(34.dp).drawBehind{drawCircle(Brush.radialGradient(listOf(col.copy(alpha=.2f),Color.Transparent)))},contentAlignment=Alignment.Center){AttributeEmblem(i,Modifier.size(30.dp))}
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
     Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){Text(a,fontFamily=SagaFont,fontSize=16.sp,color=Ink);Text("nível ${1+pts/50} · ${pts%50}/50",color=col,fontSize=12.sp)}
     Meter((pts%50)/50f,col,slim=true)
    }
   }
   if(i<attributes.lastIndex)Hairline()
  }
 }
}
