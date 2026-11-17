package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*

/** Local original material studies. Crop, never tile/stretch the material; no per-frame noise. */
@Composable fun Modifier.forgeMaterial(kind:SurfaceKind=SurfaceKind.Operational):Modifier{
 val appearance=LocalAppearance.current;val dark=appearance.dark;val minimal=appearance.minimal
 val paper=kind==SurfaceKind.Parchment
 val metal=if(minimal)null else ImageBitmap.imageResource(when(kind){SurfaceKind.Parchment->R.drawable.forge_vellum;SurfaceKind.Ceremonial->R.drawable.forge_leather;else->R.drawable.forge_iron})
 val edge=if(paper)Sepia else if(kind==SurfaceKind.Ceremonial)Gold else ColdIron
 return drawWithCache{
  val ratio=size.width/size.height.coerceAtLeast(1f)
  val sw=if(metal==null)1 else minOf(metal.width,(metal.height*ratio).toInt()).coerceAtLeast(1)
  val sh=if(metal==null)1 else minOf(metal.height,(metal.width/ratio).toInt()).coerceAtLeast(1)
  onDrawBehind{
   if(metal!=null)drawImage(metal,srcOffset=IntOffset((metal.width-sw)/2,(metal.height-sh)/2),srcSize=IntSize(sw,sh),dstSize=IntSize(size.width.toInt().coerceAtLeast(1),size.height.toInt().coerceAtLeast(1)),alpha=if(paper).8f else if(!dark).09f else if(kind==SurfaceKind.Operational).55f else .78f,filterQuality=FilterQuality.Medium)
   val d=1.dp.toPx();val w=size.width;val h=size.height
   // Material-specific construction replaces repeated decorative ribbons and rivets.
   when(kind){
    SurfaceKind.Ceremonial->{
     drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha=.55f),Color.Transparent)),size=Size(14*d,h))
     drawLine(edge.copy(alpha=.4f),Offset(5*d,0f),Offset(5*d,h),d)
     drawLine(Color.Black.copy(alpha=.65f),Offset(10*d,0f),Offset(10*d,h),2*d)
    }
    SurfaceKind.Parchment->{
     drawLine(edge.copy(alpha=.18f),Offset(w-5*d,8*d),Offset(w-5*d,h-8*d),d)
     drawLine(edge.copy(alpha=.15f),Offset(w-8*d,11*d),Offset(w-8*d,h-11*d),d)
    }
    SurfaceKind.Artifact->{
     drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.35f),Color.Transparent,Color.Black.copy(alpha=.45f))))
     drawLine(edge.copy(alpha=.45f),Offset(8*d,h-2*d),Offset(w-8*d,h-2*d),d)
    }
    else->{drawLine(Color.White.copy(alpha=if(dark).12f else .4f),Offset(8*d,d),Offset(w-8*d,d),d)}
   }
   drawLine(Color.Black.copy(alpha=.35f),Offset(0f,h-d),Offset(w,h-d),2*d)
  }
 }
}

@Composable fun ForgeMedallion(level:Int,modifier:Modifier=Modifier){
 Box(modifier.size(52.dp),contentAlignment=Alignment.Center){
  if(!LocalAppearance.current.minimal)Image(painterResource(R.drawable.forge_brooch),null,Modifier.matchParentSize(),contentScale=ContentScale.Fit)
  else Box(Modifier.matchParentSize().border(1.dp,Gold,CircleShape))
  Text("$level",color=if(LocalAppearance.current.minimal)Ink else Color(0xFFE7DECB),fontFamily=SagaFont,fontSize=17.sp)
 }
}
@Composable fun ForgePortrait(s:GameState,size:Dp){
 Box(Modifier.clip(CutCornerShape(topStart=12.dp,topEnd=12.dp,bottomEnd=3.dp,bottomStart=3.dp)).background(Color(0xFF10161B)).forgeMaterial(SurfaceKind.Artifact).border(1.dp,ColdIron.copy(alpha=.55f),CutCornerShape(topStart=12.dp,topEnd=12.dp,bottomEnd=3.dp,bottomStart=3.dp)).padding(4.dp)){Avatar(s.player!!,s,size)}
}

/** A recessed dial for the existing daily ratio; has no independent progress logic. */
@Composable fun DayDial(value:Float,label:String){val color=Green
 Box(Modifier.size(64.dp).semantics{progressBarRangeInfo=ProgressBarRangeInfo(value.coerceIn(0f,1f),0f..1f)},contentAlignment=Alignment.Center){Canvas(Modifier.matchParentSize()){
  val inset=5.dp.toPx();val bounds=Size(size.width-2*inset,size.height-2*inset)
  drawArc(Color.Black.copy(alpha=.6f),0f,360f,false,Offset(inset,inset),bounds,style=Stroke(5.dp.toPx()))
  drawArc(color.copy(alpha=.25f),-90f,360f,false,Offset(inset,inset),bounds,style=Stroke(1.dp.toPx()))
  drawArc(color,-90f,value.coerceIn(0f,1f)*360f,false,Offset(inset,inset),bounds,style=Stroke(3.dp.toPx(),cap=StrokeCap.Round))
 };Text(label,color=color,fontSize=17.sp,fontFamily=SagaFont)}
}
