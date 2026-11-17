package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*
import kotlin.math.*

/** Original, resolution independent Aetheris engravings. Fixed seeds; no runtime animation. */
@Composable fun Modifier.forgeMaterial(kind:SurfaceKind=SurfaceKind.Operational):Modifier{
 val dark=LocalAppearance.current.dark;val minimal=LocalAppearance.current.minimal
 val paper=kind==SurfaceKind.Parchment
 val ink=if(paper)Sepia else if(kind==SurfaceKind.Heroic)Blue else if(kind==SurfaceKind.Artifact)ColdIron else Gold
 return drawWithCache{
  val grain=Path().apply{repeat(if(minimal)0 else 240){i->val x=(i*137%997)/997f*size.width;val y=(i*83%991)/991f*size.height;moveTo(x,y);lineTo(x+if(paper)1.8f else if(kind==SurfaceKind.Ceremonial)12f else 4f,y+1f)}}
  onDrawBehind{
   drawPath(grain,if(dark&&!paper)Color.White.copy(alpha=.055f) else Color.Black.copy(alpha=.065f),style=Stroke(.7f))
   val inset=6.dp.toPx()
   drawRect(ink.copy(alpha=.24f),Offset(inset,inset),Size((size.width-2*inset).coerceAtLeast(0f),(size.height-2*inset).coerceAtLeast(0f)),style=Stroke(1f))
   drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha=if(paper).12f else .28f),Color.Transparent,Color.Black.copy(alpha=.12f))))
   if(!minimal){
    // Paired ribbon paths cross with alternating gaps, rather than unrelated runes.
    for(side in 0..1){val x=if(side==0)12.dp.toPx() else size.width-12.dp.toPx();val len=if(kind==SurfaceKind.Operational)26.dp.toPx() else 60.dp.toPx()
     repeat(2){strand->val p=Path();for(n in 0..40){val t=n/40f;val xx=x+sin(t*PI*4+strand*PI).toFloat()*4.dp.toPx();val yy=12.dp.toPx()+t*len;if(n==0)p.moveTo(xx,yy) else p.lineTo(xx,yy)};drawPath(p,ink.copy(alpha=.43f),style=Stroke(1.dp.toPx()))}
    }
    listOf(Offset(12.dp.toPx(),size.height-12.dp.toPx()),Offset(size.width-12.dp.toPx(),size.height-12.dp.toPx())).forEach{p->drawCircle(Color.Black.copy(alpha=.6f),2.5.dp.toPx(),p);drawCircle(ink.copy(alpha=.65f),1.3.dp.toPx(),p-Offset(.5f,.5f))}
   }
  }
 }
}
@Composable fun ForgeMedallion(level:Int,modifier:Modifier=Modifier){val color=Gold
 Box(modifier.size(44.dp),contentAlignment=Alignment.Center){Canvas(Modifier.matchParentSize()){
  val r=size.minDimension*.46f;drawCircle(Brush.radialGradient(listOf(Color(0xFF63533B),Color(0xFF141A20))),r);drawCircle(color.copy(alpha=.7f),r,style=Stroke(1.5.dp.toPx()));drawCircle(color.copy(alpha=.35f),r*.79f,style=Stroke(1f));repeat(12){val a=it*PI/6;drawLine(color.copy(alpha=.65f),center+Offset(cos(a).toFloat()*r*.85f,sin(a).toFloat()*r*.85f),center+Offset(cos(a).toFloat()*r*.97f,sin(a).toFloat()*r*.97f),1f)}};Text("$level",color=Color(0xFFE7DECB),fontFamily=SagaFont,fontSize=17.sp)}
}
@Composable fun ForgePortrait(s:GameState,size:androidx.compose.ui.unit.Dp){Box(Modifier.forgeMaterial(SurfaceKind.Heroic).padding(8.dp)){Avatar(s.player!!,s,size)}}
