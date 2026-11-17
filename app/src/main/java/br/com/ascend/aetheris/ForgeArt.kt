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
 val bronze=Gold;val frost=Blue
 val ink=if(paper)Sepia else if(kind==SurfaceKind.Heroic)Blue else if(kind==SurfaceKind.Artifact)ColdIron else Gold
 return drawWithCache{
  val grain=Path().apply{repeat(if(minimal)0 else 240){i->val x=(i*137%997)/997f*size.width;val y=(i*83%991)/991f*size.height;moveTo(x,y);lineTo(x+if(paper)1.8f else if(kind==SurfaceKind.Ceremonial)12f else 4f,y+1f)}}
  onDrawBehind{
   drawPath(grain,if(dark&&!paper)Color.White.copy(alpha=.055f) else Color.Black.copy(alpha=.065f),style=Stroke(.7f))
   val inset=6.dp.toPx()
   drawRect(ink.copy(alpha=.24f),Offset(inset,inset),Size((size.width-2*inset).coerceAtLeast(0f),(size.height-2*inset).coerceAtLeast(0f)),style=Stroke(1f))
   drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha=if(paper).12f else .28f),Color.Transparent,Color.Black.copy(alpha=.12f))))
   if(!minimal){
    val w=size.width;val h=size.height;val d=1.dp.toPx()
    when(kind){
     SurfaceKind.Heroic->{
      // Cold-forged folded corner plates; geometry stays in dp, never stretched.
      for(sx in listOf(1f,-1f)){val x=if(sx>0)inset else w-inset
       val plate=Path().apply{moveTo(x,10*d);lineTo(x+sx*20*d,10*d);lineTo(x+sx*8*d,17*d);lineTo(x+sx*8*d,35*d);close()}
       drawPath(plate,ColdIron.copy(alpha=.24f));drawPath(plate,frost.copy(alpha=.5f),style=Stroke(d))
      }
     }
     SurfaceKind.Ceremonial->{
      // Saddle stitches bind the leather cover; bronze clasps terminate the binding.
      val y=h-9*d
      for(i in 0 until (w/(9*d)).toInt()){val x=18*d+i*9*d;if(x<w-18*d)drawLine(bronze.copy(alpha=.35f),Offset(x,y),Offset(x+3*d,y-2*d),d)}
      drawLine(Color.Black.copy(alpha=.35f),Offset(18*d,9*d),Offset(w-18*d,9*d),3*d)
     }
     SurfaceKind.Parchment->{
      drawLine(Sepia.copy(alpha=.2f),Offset(17*d,24*d),Offset(17*d,h-24*d),d)
      drawLine(Sepia.copy(alpha=.3f),Offset(28*d,h-12*d),Offset(w-28*d,h-12*d),d)
     }
     SurfaceKind.Artifact->{
      // Recessed shelf and metal lip, deliberately different from a manuscript.
      drawLine(Color.Black.copy(alpha=.65f),Offset(9*d,h-13*d),Offset(w-9*d,h-13*d),4*d)
      drawLine(ColdIron.copy(alpha=.45f),Offset(9*d,h-10*d),Offset(w-9*d,h-10*d),d)
      for(x in listOf(9*d,w-13*d))drawRect(ColdIron.copy(alpha=.28f),Offset(x,12*d),Size(4*d,14*d))
     }
     else->Unit
    }
    // Paired ribbon paths cross with alternating gaps, rather than unrelated runes.
    for(side in 0..1){val x=if(side==0)12.dp.toPx() else size.width-12.dp.toPx();val len=minOf(if(kind==SurfaceKind.Operational)26.dp.toPx() else 60.dp.toPx(),(size.height-24.dp.toPx()).coerceAtLeast(0f))
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
