package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

val mainRoutes=listOf("journey","habits","missions","world","character")
val mainNames=listOf("Jornada","Hábitos","Missões","Mundo","Herói")
/** The five areas share the carved glyph family. Decorative canvas; text provides tab semantics. */
@Composable fun NavigationSigil(index:Int,color:Color,modifier:Modifier=Modifier){CarvedGlyph(listOf(Glyph.Journey,Glyph.Habits,Glyph.Missions,Glyph.World,Glyph.Hero)[index%5],color,modifier,26.dp)}
@Composable fun AetherisNavigation(route:String,onSelect:(String)->Unit){
 val selected=if(route.startsWith("region/")||route.startsWith("location/")||route.startsWith("expedition/"))"world" else when(route){"grimoire","planner","analysis","focus_history"->"habits";"settings","journal","memories","portrait"->"character";"weekly"->"world";else->route}
 val dark=LocalAppearance.current.dark;val motion=LocalAppearance.current.reducedMotion
 val bronze=Gold;val base=if(dark)Color(0xFF0F1110) else Color(0xFFE2DCD0)
 // One continuous basalt strip: no per-tab boxes, selection is a lit notch and a warmed glyph.
 Column(Modifier.fillMaxWidth().background(base).forgeMaterial().navigationBarsPadding()){
  Canvas(Modifier.fillMaxWidth().height(3.dp)){
   drawLine(Color.Black.copy(alpha=.7f),Offset.Zero,Offset(size.width,0f),2.dp.toPx())
   drawLine(bronze.copy(alpha=.22f),Offset(0f,2.dp.toPx()),Offset(size.width,2.dp.toPx()),1.dp.toPx())
  }
  Row(Modifier.fillMaxWidth().padding(horizontal=6.dp,vertical=2.dp)){
   mainRoutes.forEachIndexed{i,r->val active=selected==r
    val light by animateFloatAsState(if(active)1f else 0f,tween(if(motion)0 else AetherTokens.quick),label="aba")
    Box(Modifier.weight(1f).testTag("nav:$r").heightIn(min=66.dp).selectable(active,onClick={onSelect(r)},role=Role.Tab).semantics(mergeDescendants=true){}){
     Canvas(Modifier.matchParentSize()){
      if(light>0f){
       val d=1.dp.toPx();val w=size.width
       drawCircle(Brush.radialGradient(listOf(bronze.copy(alpha=.16f*light),Color.Transparent),center=Offset(w/2,size.height*.36f),radius=w*.42f),radius=w*.42f,center=Offset(w/2,size.height*.36f))
       val notch=Path().apply{moveTo(w*.5f-13*d,0f);lineTo(w*.5f+13*d,0f);lineTo(w*.5f+9*d,3*d);lineTo(w*.5f-9*d,3*d);close()}
       drawPath(notch,bronze.copy(alpha=.95f*light))
      }
     }
     Column(Modifier.fillMaxWidth().padding(top=11.dp,bottom=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)){
      NavigationSigil(i,if(active)Gold else Muted.copy(alpha=.85f),Modifier.size(25.dp))
      Text(mainNames[i],color=if(active)Gold else Muted,fontSize=10.sp,lineHeight=14.sp,letterSpacing=.4.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
     }
    }
   }
  }
 }
}
/** Compact carved strip: whole options wrap in rows of one continuous surface; nothing is clipped or hidden. */
@Composable fun AetherisTabs(values:List<String>,selected:Int,onSelect:(Int)->Unit){
 val glyphs=values.map{glyphForLabel(it)};val carved=glyphs.all{it!=null}
 val dark=LocalAppearance.current.dark;val gold=Gold
 BoxWithConstraints(Modifier.fillMaxWidth()){
  val n=values.size.coerceAtLeast(1)
  val perRow=(if(n<=4&&maxWidth/n>=78.dp)n else if(maxWidth>=380.dp)4 else if(maxWidth>=300.dp)3 else 2).coerceAtMost(n)
  val showGlyph=carved&&(maxWidth/perRow>=108.dp)
  val frame=CutCornerShape(4.dp);val seam=Color.Black.copy(alpha=if(dark).45f else .14f)
  Column(Modifier.fillMaxWidth().clip(frame).background(if(dark)Color(0xFF121413) else Color(0xFFDDD7CB)).forgeMaterial().border(1.dp,ColdIron.copy(alpha=.24f),frame)){
   values.chunked(perRow).forEachIndexed{row,labels->
    if(row>0)Box(Modifier.fillMaxWidth().height(1.dp).background(seam))
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)){
     labels.forEachIndexed{col,label->val index=row*perRow+col;val active=index==selected
      if(col>0)Box(Modifier.fillMaxHeight().width(1.dp).background(seam))
      Box(Modifier.weight(1f).heightIn(min=48.dp).selectable(active,onClick={onSelect(index)},role=Role.Tab).drawBehind{
        if(active){val d=1.dp.toPx()
         drawRect(Brush.verticalGradient(listOf(Color.Transparent,gold.copy(alpha=.13f))))
         drawLine(gold.copy(alpha=.9f),Offset(size.width*.22f,size.height-1.5f*d),Offset(size.width*.78f,size.height-1.5f*d),2*d)}
       }.padding(horizontal=4.dp,vertical=6.dp),contentAlignment=Alignment.Center){
       Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp,Alignment.CenterHorizontally)){
        if(showGlyph)CarvedGlyph(glyphs[index]!!,if(active)gold else Muted,size=15.dp)
        Text(label,Modifier.weight(1f,fill=false),color=if(active)gold else Ink,fontSize=12.sp,lineHeight=16.sp,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
       }
      }
     }
     repeat(perRow-labels.size){Box(Modifier.fillMaxHeight().width(1.dp).background(seam));Spacer(Modifier.weight(1f))}
    }
   }
  }
 }
}
