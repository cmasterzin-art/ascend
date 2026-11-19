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
private val mainVikingIcons=listOf(VikingIcon.Hearth,VikingIcon.OakTree,VikingIcon.QuillScroll,VikingIcon.CompassRaven,VikingIcon.Helmet)
/** The five areas share the carved glyph family. Decorative canvas; text provides tab semantics. */
@Composable fun NavigationSigil(index:Int,color:Color,modifier:Modifier=Modifier){CarvedGlyph(listOf(Glyph.Journey,Glyph.Habits,Glyph.Missions,Glyph.World,Glyph.Hero)[index%5],color,modifier,26.dp)}
@Composable fun AetherisNavigation(route:String,onSelect:(String)->Unit){
 val selected=if(route=="mimir"||route.startsWith("mimir_")||route=="codex_runico"||route.startsWith("region/")||route.startsWith("location/")||route.startsWith("expedition/"))"world" else if(route.startsWith("path/")||route in listOf("paths","inbox","pending"))"journey" else when(route){"grimoire","planner","analysis","focus_history"->"habits";"settings","journal","memories","portrait","heraldry"->"character";"weekly"->"world";else->route}
 val dark=LocalAppearance.current.dark;val motion=LocalAppearance.current.reducedMotion;val minimal=LocalAppearance.current.minimal
 val bronze=Gold;val base=if(dark)Color(0xFF0E1011) else Color(0xFFE2DCD0)
 val inactiveIcon=if(dark)Color(0xFFC8D0DA) else Color(0xFF3E4752)
 val inactiveLabel=if(dark)Color(0xFFB8C1CC) else Color(0xFF47515C)
 // 1.7.3.1 DS 3.1: continuous basalt strip, forged top seam, high-contrast Viking functional icons, and crisp active runic indicator.
 Column(Modifier.fillMaxWidth().background(base).forgeMaterial().navigationBarsPadding()){
  Canvas(Modifier.fillMaxWidth().height(3.dp)){
   drawLine(Color.Black.copy(alpha=.74f),Offset.Zero,Offset(size.width,0f),2.dp.toPx())
   drawLine(bronze.copy(alpha=.38f),Offset(0f,2.dp.toPx()),Offset(size.width,2.dp.toPx()),1.dp.toPx())
  }
  Row(Modifier.fillMaxWidth().padding(horizontal=6.dp,vertical=2.dp)){
   mainRoutes.forEachIndexed{i,r->val active=selected==r
    val light by animateFloatAsState(if(active)1f else 0f,tween(if(motion)0 else AetherTokens.quick),label="aba")
    Box(Modifier.weight(1f).testTag("nav:$r").heightIn(min=68.dp).selectable(active,onClick={onSelect(r)},role=Role.Tab).semantics(mergeDescendants=true){}){
     Canvas(Modifier.matchParentSize()){
      if(light>0f){
       val d=1.dp.toPx();val w=size.width
       drawCircle(Brush.radialGradient(listOf(bronze.copy(alpha=.24f*light),Color.Transparent),center=Offset(w/2,size.height*.36f),radius=w*.48f),radius=w*.48f,center=Offset(w/2,size.height*.36f))
       val notch=Path().apply{moveTo(w*.5f-16*d,0f);lineTo(w*.5f+16*d,0f);lineTo(w*.5f+11*d,4f*d);lineTo(w*.5f-11*d,4f*d);close()}
       drawPath(notch,bronze.copy(alpha=light))
      }
     }
     Column(Modifier.fillMaxWidth().padding(top=9.dp,bottom=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){
      Box(Modifier.size(32.dp),contentAlignment=Alignment.Center){
       if(active&&!minimal){
        VikingNavAmulet(Modifier.size(32.dp),alpha=if(dark).56f else .30f)
       }
       VikingFunctionalIcon(mainVikingIcons[i],if(active)Gold else inactiveIcon,size=24.dp,active=active)
      }
      Text(mainNames[i],color=if(active)Gold else inactiveLabel,fontSize=10.sp,lineHeight=14.sp,letterSpacing=.4.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
     }
    }
   }
  }
 }
}
/** 1.6.9.15 · Inscription ledger: options read as carved words on stone, compacting gracefully on 320dp/130% when 6+ tabs exist.
 *  No per-option boxes; the active word is warmed and underlined with a central chisel mark. Everything stays visible and selectable. */
@OptIn(ExperimentalLayoutApi::class)
@Composable fun AetherisTabs(values:List<String>,selected:Int,onSelect:(Int)->Unit){
 val glyphs=values.map{glyphForLabel(it)}
 val dark=LocalAppearance.current.dark;val gold=Gold;val muted=Muted;val motion=LocalAppearance.current.reducedMotion
 val hair=if(dark)Color.White.copy(alpha=.08f) else Color.Black.copy(alpha=.12f)
 BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical=2.dp)){
  val compact=maxWidth<360.dp&&values.size>=6
  val hPad=if(compact)6.dp else 11.dp
  val minH=if(compact)38.dp else 46.dp
  val fSize=if(compact)11.sp else 13.sp
  Column(Modifier.fillMaxWidth()){
   Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,hair,hair,Color.Transparent))))
   FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Start,verticalArrangement=Arrangement.spacedBy(0.dp)){
    values.forEachIndexed{index,label->val active=index==selected
     val light by animateFloatAsState(if(active)1f else 0f,tween(if(motion)0 else AetherTokens.quick),label="inscrição")
     Row(verticalAlignment=Alignment.CenterVertically){
      Box(Modifier.heightIn(min=minH).selectable(active,onClick={onSelect(index)},role=Role.Tab).drawBehind{
        if(light>0f){val d=1.dp.toPx();val cy=size.height-7*d;val cx=size.width*.5f
         drawCircle(Brush.radialGradient(listOf(gold.copy(alpha=.14f*light),Color.Transparent),center=Offset(cx,size.height*.52f),radius=size.width*.58f),size.width*.58f,Offset(cx,size.height*.52f))
         drawLine(gold.copy(alpha=.92f*light),Offset(size.width*.16f,cy),Offset(size.width*.84f,cy),1.5f*d,cap=StrokeCap.Round)
         val dm=Path().apply{moveTo(cx,cy-2.2f*d);lineTo(cx+2.2f*d,cy);lineTo(cx,cy+2.2f*d);lineTo(cx-2.2f*d,cy);close()}
         drawPath(dm,gold.copy(alpha=light))
        }
       }.padding(horizontal=hPad),contentAlignment=Alignment.Center){
       Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(if(compact)3.dp else 5.dp)){
        if(active&&glyphs[index]!=null)CarvedGlyph(glyphs[index]!!,gold,size=if(compact)11.dp else 13.dp)
        Text(label,color=if(active)gold else muted,fontSize=fSize,lineHeight=(fSize.value+4).sp,letterSpacing=if(compact).1.sp else .3.sp,maxLines=1)
       }
      }
     }
    }
   }
   Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,hair,hair,Color.Transparent))))
  }
 }
}
/** 1.6.9.10 · Productivity shortcuts as one slim carved band with hairline seams instead of three heavy cards. */
@Composable fun ShortcutStrip(items:List<Triple<String,Glyph,()->Unit>>){
 val dark=LocalAppearance.current.dark;val hair=if(dark)Color.White.copy(alpha=.08f) else Color.Black.copy(alpha=.12f)
 BoxWithConstraints(Modifier.fillMaxWidth()){val roomy=maxWidth/items.size.coerceAtLeast(1)>=112.dp
 Column(Modifier.fillMaxWidth()){
  Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,hair,hair,Color.Transparent))))
  Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)){
   items.forEachIndexed{i,(label,glyph,action)->
    if(i>0)Box(Modifier.width(1.dp).fillMaxHeight().padding(vertical=12.dp).background(hair))
    Row(Modifier.weight(1f).heightIn(min=48.dp).clickable(onClick=action).padding(horizontal=4.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp,Alignment.CenterHorizontally)){
     // Narrow screens keep the full word and drop the glyph rather than truncating the label.
     if(roomy)CarvedGlyph(glyph,Gold,size=16.dp);Text(label,color=Ink,fontSize=12.sp,letterSpacing=.3.sp,maxLines=1,overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
   }
  }
  Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,hair,hair,Color.Transparent))))
 }
 }
}
