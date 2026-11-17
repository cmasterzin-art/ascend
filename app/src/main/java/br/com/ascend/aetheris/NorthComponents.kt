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

@Composable fun SagaButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,secondary:Boolean=false,destructive:Boolean=false,loading:Boolean=false,completed:Boolean=false){
 val interaction=remember{MutableInteractionSource()};val pressed by interaction.collectIsPressedAsState();val reduced=LocalAppearance.current.reducedMotion
 val depth by animateFloatAsState(if(pressed).6f else 0f,tween(if(reduced)0 else 90),label="pressão")
 // Bronze is reserved for the primary act; iron for everything secondary. Ember and moss mark consequence and completion.
 val accent=if(destructive)MaterialTheme.colorScheme.error else if(completed)Green else if(secondary)ColdIron else Gold
 val bronze=!secondary&&!destructive&&!completed
 val base=if(!enabled)listOf(Color(0xFF2E3134),Color(0xFF1F2225)) else if(destructive)listOf(Color(0xFF4A2A27),Color(0xFF241716)) else if(bronze)listOf(Color(0xFF5C4A30),Color(0xFF2E2416)) else listOf(Color(0xFF2A2E31),Color(0xFF15181A))
 val shape=CutCornerShape(topStart=7.dp,topEnd=2.dp,bottomEnd=7.dp,bottomStart=2.dp)
 Button(onClick,modifier.heightIn(min=48.dp).offset{IntOffset(0,depth.dp.roundToPx())}.clip(shape).background(Brush.verticalGradient(base)).plateMaterial(bronze=bronze,enabled=enabled).border(1.dp,accent.copy(alpha=if(enabled).42f else .12f),shape).drawWithCache{onDrawBehind{
  val d=1.dp.toPx()
  if(destructive)drawRect(Brush.horizontalGradient(listOf(accent.copy(alpha=.22f),Color.Transparent)))
  if(completed)drawLine(accent.copy(alpha=.7f),Offset(10*d,size.height-3*d),Offset(size.width-10*d,size.height-3*d),d)
  if(bronze&&enabled)drawLine(accent.copy(alpha=.6f),Offset(size.width*.5f-14*d,size.height-3*d),Offset(size.width*.5f+14*d,size.height-3*d),d)
  if(pressed)drawRect(Color.Black.copy(alpha=.25f))
 }},enabled=enabled&&!loading,shape=shape,interactionSource=interaction,contentPadding=PaddingValues(horizontal=16.dp,vertical=12.dp),colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent,contentColor=Color(0xFFEAE2CF),disabledContainerColor=Color.Transparent,disabledContentColor=Color(0xFF8E959E))){
 if(loading){CircularProgressIndicator(Modifier.size(16.dp),strokeWidth=2.dp);Spacer(Modifier.width(8.dp))};Text(if(completed)"◆ $text" else text,fontSize=14.sp,letterSpacing=.3.sp)
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