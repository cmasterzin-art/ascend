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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

val mainRoutes=listOf("journey","habits","missions","world","character")
val mainNames=listOf("Jornada","Hábitos","Missões","Mundo","Herói")
/** Five original, single-weight engravings. Decorative canvas; text provides tab semantics. */
@Composable fun NavigationSigil(index:Int,color:Color,modifier:Modifier=Modifier){Canvas(modifier.size(26.dp)){
 val w=size.width;val h=size.height;val stroke=1.5.dp.toPx();fun line(x:Float,y:Float,xx:Float,yy:Float)=drawLine(color,Offset(w*x,h*y),Offset(w*xx,h*yy),stroke)
 val p=Path()
 when(index%5){
 0->{p.moveTo(w*.1f,h*.85f);p.lineTo(w*.45f,h*.15f);p.lineTo(w*.64f,h*.52f);p.lineTo(w*.75f,h*.3f);p.lineTo(w*.94f,h*.85f);p.close();drawPath(p,color.copy(alpha=.16f));drawPath(p,color,style=Stroke(stroke,join=StrokeJoin.Round));line(.32f,.61f,.45f,.39f);line(.45f,.39f,.56f,.61f)}
 1->{p.moveTo(w*.22f,h*.15f);p.lineTo(w*.83f,h*.1f);p.lineTo(w*.83f,h*.85f);p.lineTo(w*.22f,h*.9f);p.close();drawPath(p,color.copy(alpha=.14f));drawPath(p,color,style=Stroke(stroke,cap=StrokeCap.Round,join=StrokeJoin.Round));line(.32f,.15f,.32f,.88f);repeat(3){i->line(.45f,.32f+i*.17f,.7f,.3f+i*.17f)}}
 2->{line(.25f,.1f,.25f,.94f);p.moveTo(w*.28f,h*.13f);p.lineTo(w*.83f,h*.18f);p.lineTo(w*.76f,h*.72f);p.lineTo(w*.51f,h*.61f);p.lineTo(w*.28f,h*.69f);drawPath(p,color.copy(alpha=.14f));drawPath(p,color,style=Stroke(stroke,cap=StrokeCap.Round,join=StrokeJoin.Round));line(.55f,.27f,.55f,.52f)}
 3->{drawCircle(color,size.minDimension*.33f,style=Stroke(stroke));line(.5f,.04f,.5f,.96f);line(.04f,.5f,.96f,.5f);p.moveTo(w*.64f,h*.26f);p.lineTo(w*.58f,h*.58f);p.lineTo(w*.32f,h*.74f);p.close();drawPath(p,color.copy(alpha=.14f));drawPath(p,color,style=Stroke(stroke,cap=StrokeCap.Round,join=StrokeJoin.Round))}
 4->{drawArc(color,180f,180f,false,Offset(w*.19f,h*.12f),Size(w*.62f,h*.95f),style=Stroke(stroke));line(.18f,.59f,.18f,.85f);line(.82f,.59f,.82f,.85f);line(.5f,.12f,.5f,.9f);line(.2f,.54f,.4f,.62f);line(.6f,.62f,.8f,.54f);line(.18f,.85f,.34f,.9f);line(.66f,.9f,.82f,.85f)}
 }
}}
@Composable fun AetherisNavigation(route:String,onSelect:(String)->Unit){
 val selected=if(route.startsWith("region/")||route.startsWith("location/")||route.startsWith("expedition/"))"world" else when(route){"grimoire","planner","analysis","focus_history"->"habits";"settings","journal","memories","portrait"->"character";"weekly"->"world";else->route}
 val dark=LocalAppearance.current.dark;val motion=LocalAppearance.current.reducedMotion
 val bronze=Gold;val base=if(dark)Color(0xFF151714) else Color(0xFFE4DED2)
 Column(Modifier.fillMaxWidth().background(base).forgeMaterial().navigationBarsPadding()){
  Canvas(Modifier.fillMaxWidth().height(4.dp)){
   drawLine(bronze.copy(alpha=.5f),Offset.Zero,Offset(size.width,0f),1.dp.toPx())
   drawLine(Color.Black.copy(alpha=.5f),Offset(0f,size.height),Offset(size.width,size.height),2.dp.toPx())
  }
  Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=4.dp)){
   mainRoutes.forEachIndexed{i,r->val active=selected==r
    val light by animateFloatAsState(if(active)1f else 0f,tween(if(motion)0 else AetherTokens.quick),label="aba")
    Box(Modifier.weight(1f).testTag("nav:$r").heightIn(min=68.dp).selectable(active,onClick={onSelect(r)},role=Role.Tab).semantics(mergeDescendants=true){}){
     Canvas(Modifier.matchParentSize()){
      if(light>0f){
       val d=1.dp.toPx();val w=size.width;val h=size.height
       val shield=Path().apply{moveTo(8*d,0f);lineTo(w-8*d,0f);lineTo(w-8*d,h-12*d);quadraticTo(w*.5f,h-5*d,w*.5f,h-3*d);quadraticTo(w*.5f,h-5*d,8*d,h-12*d);close()}
       drawPath(shield,Brush.verticalGradient(listOf(bronze.copy(alpha=.28f*light),bronze.copy(alpha=.04f*light))))
       drawCircle(Brush.radialGradient(listOf(bronze.copy(alpha=.14f*light),Color.Transparent)),radius=w*.48f,center=Offset(w/2,h*.4f))
       drawLine(bronze.copy(alpha=.9f*light),Offset(w*.32f,0f),Offset(w*.68f,0f),2*d)
      }
     }
     Column(Modifier.fillMaxWidth().padding(vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(6.dp)){
      NavigationSigil(i,if(active)Gold else Muted,Modifier.size(25.dp))
      Text(mainNames[i],color=if(active)Gold else Ink,fontSize=10.sp,lineHeight=14.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
     }
    }
   }
  }
 }
}
/** Whole options wrap; no clipped horizontal sections or hidden last tab. */
@Composable fun AetherisTabs(values:List<String>,selected:Int,onSelect:(Int)->Unit){
 BoxWithConstraints(Modifier.fillMaxWidth()){
 val columns=minOf(values.size.coerceAtLeast(1),if(maxWidth<350.dp)2 else 3)
 Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
 values.chunked(columns).forEachIndexed{row,labels->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){
 labels.forEachIndexed{col,label->val index=row*columns+col;val active=index==selected
 Row(Modifier.weight(1f).heightIn(min=48.dp).background(if(active)Gold.copy(alpha=.12f) else Surface,CutCornerShape(3.dp)).forgeMaterial().border(1.dp,if(active)Gold.copy(alpha=.5f) else ColdIron.copy(alpha=.14f),CutCornerShape(3.dp)).selectable(active,onClick={onSelect(index)},role=Role.Tab).padding(horizontal=10.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
 if(active)Box(Modifier.size(4.dp).background(Gold,CutCornerShape(1.dp)))
 Text(label,Modifier.weight(1f),color=if(active)Gold else Ink,fontSize=12.sp,lineHeight=17.sp)
 }
 };repeat(columns-labels.size){Spacer(Modifier.weight(1f))}
 }}
 }
 }
}
