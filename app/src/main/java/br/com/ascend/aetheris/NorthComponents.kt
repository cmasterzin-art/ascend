package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable fun SagaButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,secondary:Boolean=false,destructive:Boolean=false,loading:Boolean=false,completed:Boolean=false,ceremonial:Boolean=false){
 val interaction=remember{MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val reduced=LocalAppearance.current.reducedMotion
 val depth by animateFloatAsState(if(pressed).8f else 0f,tween(if(reduced)0 else 90),label="pressão")
 // Bronze is reserved for primary and ceremonial acts; blackened iron and slate for secondary actions; ember and moss for consequence and completion.
 val accent=if(destructive)MaterialTheme.colorScheme.error else if(completed)Green else if(secondary)ColdIron else if(ceremonial)Color(0xFFD9B878) else Gold
 val bronze=!secondary&&!destructive&&!completed
 val base=when{
  !enabled->listOf(Color(0xFF282B2E),Color(0xFF1A1D20))
  destructive->listOf(Color(0xFF4C2926),Color(0xFF241514))
  completed->listOf(Color(0xFF273328),Color(0xFF161D17))
  ceremonial->listOf(Color(0xFF6B5434),Color(0xFF362917))
  bronze->listOf(Color(0xFF5A482E),Color(0xFF2B2215))
  else->listOf(Color(0xFF272B2F),Color(0xFF14171A))
 }
 val shape=CutCornerShape(topStart=if(ceremonial)8.dp else 7.dp,topEnd=2.dp,bottomEnd=if(ceremonial)8.dp else 7.dp,bottomStart=2.dp)
 val borderAlpha=if(!enabled).14f else if(pressed).78f else if(ceremonial).68f else if(bronze).5f else .38f
 Button(onClick,modifier.heightIn(min=48.dp).offset{IntOffset(0,depth.dp.roundToPx())}.clip(shape).background(Brush.verticalGradient(base)).plateMaterial(bronze=bronze,enabled=enabled,ceremonial=ceremonial).border(1.dp,accent.copy(alpha=borderAlpha),shape).drawWithCache{onDrawBehind{
  val d=1.dp.toPx();val w=size.width;val h=size.height;val cx=w*.5f
  if(destructive)drawRect(Brush.horizontalGradient(listOf(accent.copy(alpha=.24f),Color.Transparent)))
  if(completed)drawLine(accent.copy(alpha=.72f),Offset(10*d,h-2.5f*d),Offset(w-10*d,h-2.5f*d),1.2f*d)
  if(bronze&&enabled){
   val span=if(ceremonial)24*d else 16*d
   drawLine(accent.copy(alpha=if(ceremonial).8f else .62f),Offset(cx-span,h-2.5f*d),Offset(cx+span,h-2.5f*d),d)
   val dm=Path().apply{moveTo(cx,h-4.5f*d);lineTo(cx+2.2f*d,h-2.5f*d);lineTo(cx,h-.5f*d);lineTo(cx-2.2f*d,h-2.5f*d);close()}
   drawPath(dm,accent.copy(alpha=.85f))
   if(ceremonial){
    listOf(10*d,w-10*d).forEach{rx->
     val sideDm=Path().apply{moveTo(rx,h*.5f-2.8f*d);lineTo(rx+2.8f*d,h*.5f);lineTo(rx,h*.5f+2.8f*d);lineTo(rx-2.8f*d,h*.5f);close()}
     drawPath(sideDm,accent.copy(alpha=.72f))
    }
   }
  }
  if(secondary&&enabled)drawLine(ColdIron.copy(alpha=.4f),Offset(cx-12*d,h-2.5f*d),Offset(cx+12*d,h-2.5f*d),d)
  if(pressed)drawRect(Color.Black.copy(alpha=.28f))
 }},enabled=enabled&&!loading,shape=shape,interactionSource=interaction,contentPadding=PaddingValues(horizontal=if(ceremonial)32.dp else 16.dp,vertical=11.dp),colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent,contentColor=if(secondary)Color(0xFFD9DEE4) else Color(0xFFEFE6D5),disabledContainerColor=Color.Transparent,disabledContentColor=Color(0xFF889099))){
 if(loading){CircularProgressIndicator(Modifier.size(16.dp),strokeWidth=2.dp);Spacer(Modifier.width(8.dp))}
 Text(if(completed)"◆ $text" else text,fontSize=14.sp,letterSpacing=if(ceremonial).5.sp else .3.sp,fontFamily=if(ceremonial)SagaFont else null)
 }
}
/** Five carved class medallions (raster), each with its own motif; vector engraving in minimal mode. */
@Composable fun ClassEmblem(index:Int,modifier:Modifier=Modifier){
 Box(modifier.size(36.dp).semantics{contentDescription="Emblema ${classes[index]}"}){
  if(LocalAppearance.current.minimal)ClassEngraving(index,Modifier.matchParentSize())
  else AtlasCell(R.drawable.carved_emblems,index,5,2,Modifier.matchParentSize())
 }
}
@Composable private fun ClassEngraving(index:Int,modifier:Modifier){
 val colors=listOf(Ember,Blue,Green,Blue,Gold);val color=colors[index]
 Canvas(modifier){
 val w=size.width;val h=size.height;val p=Path()
 when(index){
 0,2->{p.moveTo(w*.2f,h*.18f);p.lineTo(w*.8f,h*.18f);p.lineTo(w*.74f,h*.65f);p.lineTo(w*.5f,h*.9f);p.lineTo(w*.26f,h*.65f);p.close()}
 1->{p.moveTo(w*.18f,h*.25f);p.lineTo(w*.5f,h*.34f);p.lineTo(w*.82f,h*.25f);p.lineTo(w*.82f,h*.8f);p.lineTo(w*.5f,h*.88f);p.lineTo(w*.18f,h*.8f);p.close()}
 3->{p.moveTo(w*.5f,h*.08f);p.lineTo(w*.85f,h*.82f);p.lineTo(w*.5f,h*.67f);p.lineTo(w*.15f,h*.82f);p.close()}
 else->{p.moveTo(w*.24f,h*.18f);p.lineTo(w*.8f,h*.18f);p.lineTo(w*.8f,h*.4f);p.lineTo(w*.58f,h*.4f);p.lineTo(w*.58f,h*.86f);p.lineTo(w*.4f,h*.86f);p.lineTo(w*.4f,h*.4f);p.lineTo(w*.24f,h*.4f);p.close()}}
 drawPath(p,color.copy(alpha=.12f));drawPath(p,color,style=Stroke(1.5.dp.toPx()));if(index==0||index==1||index==2)drawLine(color,Offset(w*.5f,h*.34f),Offset(w*.5f,h*.7f),1.4.dp.toPx())
 }
}
@Composable fun NarrativePanel(s:GameState,vm:GameViewModel){nextNarrative(s)?.let{line->AetherSurface(SurfaceKind.Parchment){
 Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
  when{line.speaker.startsWith("Elyra")->NavigationSigil(1,Sepia,Modifier.size(36.dp));line.speaker.startsWith("Darian")->ClassEmblem(4);line.speaker.startsWith("Seren")->ClassEmblem(2);else->RuneSeal(3,Sepia,Modifier.size(36.dp))}
  Column(Modifier.weight(1f)){Eyebrow("Uma voz de Aetheris",Sepia);Text(line.speaker,style=MaterialTheme.typography.titleMedium)}
 }
 SagaDivider(color=ColdIron)
 Text("“${line.text}”",color=Sepia,fontSize=15.sp,lineHeight=25.sp)
 SagaButton("Guardar esta memória",{vm.run{vm.repo.rememberNarrative(line.id)}},secondary=true)
}}}