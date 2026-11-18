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

enum class ForgeMaterial(val resId:Int){
 IronNordic(R.drawable.mat_iron_nordic),
 BronzeAncient(R.drawable.mat_bronze_ancient),
 SilverOxidized(R.drawable.mat_silver_oxidized),
 SlateVolcanic(R.drawable.mat_slate_volcanic),
 LeatherCarved(R.drawable.mat_leather_carved),
 VellumAged(R.drawable.mat_vellum_aged)
}

/** 1.6.9.19 — A Biblioteca da Forja: high-definition original Nordic material studies. Crop, never tile/stretch; no per-frame noise. */
@Composable fun Modifier.forgeMaterial(kind:SurfaceKind=SurfaceKind.Operational):Modifier{
 val appearance=LocalAppearance.current;val dark=appearance.dark;val minimal=appearance.minimal
 val paper=kind==SurfaceKind.Parchment
 val metal=if(minimal)null else ImageBitmap.imageResource(when(kind){
  SurfaceKind.Parchment->R.drawable.mat_vellum_aged
  SurfaceKind.Ceremonial->R.drawable.mat_leather_carved
  SurfaceKind.Heroic->R.drawable.mat_iron_nordic
  SurfaceKind.Artifact->R.drawable.mat_silver_oxidized
  SurfaceKind.Operational->R.drawable.mat_slate_volcanic
 })
 val bronzeSpine=if(minimal||kind!=SurfaceKind.Ceremonial)null else ImageBitmap.imageResource(R.drawable.mat_bronze_ancient)
 val edge=when(kind){SurfaceKind.Parchment->Sepia;SurfaceKind.Ceremonial->Gold;SurfaceKind.Heroic->Color(0xFF9EB4D3);SurfaceKind.Artifact->Color(0xFFB4A1C7);else->ColdIron}
 return drawWithCache{
  val ratio=size.width/size.height.coerceAtLeast(1f)
  val sw=if(metal==null)1 else minOf(metal.width,(metal.height*ratio).toInt()).coerceAtLeast(1)
  val sh=if(metal==null)1 else minOf(metal.height,(metal.width/ratio).toInt()).coerceAtLeast(1)
  onDrawBehind{
   if(metal!=null)drawImage(metal,srcOffset=IntOffset((metal.width-sw)/2,(metal.height-sh)/2),srcSize=IntSize(sw,sh),dstSize=IntSize(size.width.toInt().coerceAtLeast(1),size.height.toInt().coerceAtLeast(1)),alpha=if(paper).84f else if(!dark).12f else if(kind==SurfaceKind.Operational).58f else if(kind==SurfaceKind.Artifact).32f else .74f,filterQuality=FilterQuality.Medium)
   val d=1.dp.toPx();val w=size.width;val h=size.height
   when(kind){
    SurfaceKind.Ceremonial->{
     if(bronzeSpine!=null)drawImage(bronzeSpine,srcOffset=IntOffset(0,0),srcSize=IntSize(32,bronzeSpine.height),dstSize=IntSize((12*d).toInt().coerceAtLeast(1),h.toInt().coerceAtLeast(1)),alpha=if(dark).55f else .25f)
     drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha=.52f),Color.Transparent)),size=Size(16*d,h))
     drawLine(edge.copy(alpha=.48f),Offset(5*d,0f),Offset(5*d,h),d)
     drawLine(edge.copy(alpha=.22f),Offset(8*d,6*d),Offset(8*d,h-6*d),d)
     drawLine(Color.Black.copy(alpha=.65f),Offset(11*d,0f),Offset(11*d,h),2*d)
     drawLine(edge.copy(alpha=.32f),Offset(14*d,d),Offset(w-8*d,d),d)
    }
    SurfaceKind.Parchment->{
     drawLine(edge.copy(alpha=.22f),Offset(5*d,8*d),Offset(5*d,h-8*d),d)
     drawLine(edge.copy(alpha=.22f),Offset(w-5*d,8*d),Offset(w-5*d,h-8*d),d)
     drawLine(edge.copy(alpha=.14f),Offset(w-8*d,12*d),Offset(w-8*d,h-12*d),d)
     drawLine(edge.copy(alpha=.18f),Offset(10*d,3*d),Offset(w-10*d,3*d),d)
    }
    SurfaceKind.Heroic->{
     drawRect(Brush.verticalGradient(listOf(edge.copy(alpha=if(dark).08f else .04f),Color.Transparent,Color.Black.copy(alpha=.38f))))
     drawLine(edge.copy(alpha=.38f),Offset(8*d,d),Offset(w-8*d,d),d)
     drawLine(edge.copy(alpha=.22f),Offset(12*d,h-2.5f*d),Offset(w-12*d,h-2.5f*d),d)
    }
    SurfaceKind.Artifact->{
     drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.46f),Color.Transparent,Color.Black.copy(alpha=.52f))))
     drawCircle(Brush.radialGradient(listOf(edge.copy(alpha=if(dark).1f else .04f),Color.Transparent),center=Offset(w*.5f,h*.42f),radius=w*.55f),radius=w*.55f,center=Offset(w*.5f,h*.42f))
     drawLine(edge.copy(alpha=.42f),Offset(8*d,d),Offset(w-8*d,d),d)
     drawLine(edge.copy(alpha=.45f),Offset(8*d,h-2*d),Offset(w-8*d,h-2*d),d)
    }
    SurfaceKind.Operational->{
     drawLine(Color.White.copy(alpha=if(dark).13f else .42f),Offset(6*d,d),Offset(w-6*d,d),d)
     drawLine(edge.copy(alpha=.22f),Offset(3*d,8*d),Offset(3*d,2*d),d)
     drawLine(edge.copy(alpha=.22f),Offset(w-3*d,h-8*d),Offset(w-3*d,h-2*d),d)
    }
   }
   drawLine(Color.Black.copy(alpha=.38f),Offset(0f,h-d),Offset(w,h-d),2*d)
  }
 }
}

@Composable fun ForgeMedallion(level:Int,modifier:Modifier=Modifier){
 Box(modifier.size(52.dp),contentAlignment=Alignment.Center){
  if(!LocalAppearance.current.minimal){
   AtlasCell(R.drawable.orn_medallions_seals,2,3,2,Modifier.matchParentSize())
   Canvas(Modifier.matchParentSize()){drawCircle(Color(0xFF101316).copy(alpha=.82f),radius=size.minDimension*.31f)}
  }else Box(Modifier.matchParentSize().border(1.dp,Gold,CircleShape))
  Text("$level",color=if(LocalAppearance.current.minimal)Ink else Color(0xFFE7DECB),fontFamily=SagaFont,fontSize=17.sp)
 }
}
@Composable fun ForgePortrait(s:GameState,size:Dp){
 Box(Modifier.width(size+12.dp).height(size*1.5f+18.dp),contentAlignment=Alignment.Center){
  Avatar(s.player!!,s,size)
  if(!LocalAppearance.current.minimal)Image(painterResource(R.drawable.north_frame),null,Modifier.matchParentSize(),contentScale=ContentScale.Fit)
 }
}

/** A recessed dial for the existing daily ratio; has no independent progress logic. */
@Composable fun DayDial(value:Float,label:String,diameter:Dp=84.dp){val color=Gold
 Box(Modifier.size(diameter).semantics{progressBarRangeInfo=ProgressBarRangeInfo(value.coerceIn(0f,1f),0f..1f)},contentAlignment=Alignment.Center){
  if(!LocalAppearance.current.minimal){
   AtlasCell(R.drawable.orn_medallions_seals,1,3,2,Modifier.matchParentSize(),alpha=.88f)
   Canvas(Modifier.matchParentSize()){drawCircle(Color(0xFF0E1114).copy(alpha=.84f),radius=size.minDimension*.35f)}
  }
  Canvas(Modifier.matchParentSize()){
   val inset=5.dp.toPx();val bounds=Size(size.width-2*inset,size.height-2*inset)
   drawArc(Color.Black.copy(alpha=.6f),0f,360f,false,Offset(inset,inset),bounds,style=Stroke(5.dp.toPx()))
   drawArc(color.copy(alpha=.25f),-90f,360f,false,Offset(inset,inset),bounds,style=Stroke(1.dp.toPx()))
   drawArc(color,-90f,value.coerceIn(0f,1f)*360f,false,Offset(inset,inset),bounds,style=Stroke(3.dp.toPx(),cap=StrokeCap.Round))
  };Text(label,color=color,fontSize=if(diameter>100.dp)30.sp else 17.sp,fontFamily=SagaFont)}
}

@Composable fun NorthDivider(silver:Boolean=false){
 if(!LocalAppearance.current.minimal)AtlasCell(R.drawable.orn_divider_forged,if(silver)1 else 0,1,2,Modifier.fillMaxWidth().height(26.dp))
}
