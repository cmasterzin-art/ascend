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
 val dark=LocalAppearance.current.dark;val accent=if(destructive)MaterialTheme.colorScheme.error else if(completed)Green else if(secondary)ColdIron else Gold
 val tones=if(destructive)listOf(Color(0xFF603D39),Color(0xFF302021)) else if(secondary)listOf(Color(0xFF303944),Color(0xFF151C25)) else if(dark)listOf(Color(0xFFA68B59),Color(0xFF7A623E),Color(0xFF665134)) else listOf(Color(0xFFD5C39E),Color(0xFFB49A6A),Color(0xFFCFBB92))
 Button(onClick,modifier.heightIn(min=48.dp).offset{IntOffset(0,depth.dp.roundToPx())}.clip(CutCornerShape(7.dp)).background(Brush.verticalGradient(if(enabled)tones else listOf(Color(0xFF34383D),Color(0xFF25292E)))).forgeMaterial(if(secondary)SurfaceKind.Operational else SurfaceKind.Ceremonial).drawWithCache{onDrawBehind{drawLine(Color.White.copy(alpha=if(pressed).06f else .18f),Offset(10.dp.toPx(),2.dp.toPx()),Offset(size.width-10.dp.toPx(),2.dp.toPx()),1.dp.toPx());drawLine(Color.Black.copy(alpha=.5f),Offset(8.dp.toPx(),size.height-3.dp.toPx()),Offset(size.width-8.dp.toPx(),size.height-3.dp.toPx()),2.dp.toPx());drawRect(accent.copy(alpha=.30f),style=Stroke(.7.dp.toPx()))}},enabled=enabled&&!loading,shape=CutCornerShape(7.dp),interactionSource=interaction,colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent,contentColor=if(secondary||destructive)Color(0xFFE7DECB) else if(dark)Color(0xFFFFEACA) else Color(0xFF201B12),disabledContainerColor=Color.Transparent,disabledContentColor=Color(0xFFA4ABB5))){
 if(loading){CircularProgressIndicator(Modifier.size(16.dp),strokeWidth=2.dp);Spacer(Modifier.width(8.dp))};Text(if(completed)"◆ $text" else text,fontSize=14.sp)
 }
}
@Composable fun ClassEmblem(index:Int,modifier:Modifier=Modifier){
 val colors=listOf(Ember,Blue,Green,Blue,Gold);val color=colors[index]
 Canvas(modifier.size(36.dp).semantics{contentDescription="Emblema ${classes[index]}"}){
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
