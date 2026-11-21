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
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable fun SagaButton(
 text:String,
 onClick:()->Unit,
 modifier:Modifier=Modifier,
 enabled:Boolean=true,
 secondary:Boolean=false,
 destructive:Boolean=false,
 loading:Boolean=false,
 completed:Boolean=false,
 ceremonial:Boolean=false,
 selected:Boolean=false,
 style:VikingButtonStyle?=null,
 leadingRune:HistoricalFutharkRune?=null
){
 val interaction=remember{MutableInteractionSource()}
 val pressed by interaction.collectIsPressedAsState()
 val focused by interaction.collectIsFocusedAsState()
 val appearance=LocalAppearance.current
 val reduced=appearance.reducedMotion
 val dark=appearance.dark
 val goldColor=Gold
 val resolvedStyle=style?:when{
  destructive->VikingButtonStyle.Destrutivo
  completed->VikingButtonStyle.Confirmacao
  ceremonial->VikingButtonStyle.CerimonialViking
  secondary->VikingButtonStyle.FerroRunico
  else->VikingButtonStyle.BronzeForjado
 }
 val isCeremonial=resolvedStyle==VikingButtonStyle.CerimonialViking
 val isConfirm=resolvedStyle==VikingButtonStyle.Confirmacao||completed
 val isDestructive=resolvedStyle==VikingButtonStyle.Destrutivo||destructive
 val isCompact=resolvedStyle==VikingButtonStyle.CompactoRunico
 val isBronze=resolvedStyle==VikingButtonStyle.BronzeForjado||isCeremonial
 val isIron=resolvedStyle==VikingButtonStyle.FerroRunico||isCompact
 val depth by animateFloatAsState(if(pressed&&enabled)1.1f else 0f,tween(if(reduced)0 else 85),label="pressão")
 val pressScale by animateFloatAsState(if(pressed&&enabled).986f else 1f,tween(if(reduced)0 else 85),label="escala")
 val accent=when{
  isDestructive->MaterialTheme.colorScheme.error
  isConfirm->Green
  selected->Color(0xFFE5C88F)
  isCeremonial->Color(0xFFD9B878)
  isBronze->goldColor
  else->ColdIron
 }
 val base=when{
  !enabled->listOf(Color(0xFF282B2E),Color(0xFF1A1D20))
  isDestructive->listOf(Color(0xFF4C2926),Color(0xFF241514))
  isConfirm->listOf(Color(0xFF25352A),Color(0xFF151F18))
  isCeremonial->listOf(Color(0xFF6B5434),Color(0xFF332615))
  isBronze->listOf(Color(0xFF5A482E),Color(0xFF2A2115))
  isCompact->listOf(Color(0xFF23272B),Color(0xFF14171A))
  else->listOf(Color(0xFF272B2F),Color(0xFF14171A))
 }
 val shape=CutCornerShape(topStart=if(isCeremonial)8.dp else if(isCompact)5.dp else 7.dp,topEnd=2.dp,bottomEnd=if(isCeremonial)8.dp else if(isCompact)5.dp else 7.dp,bottomStart=2.dp)
 val borderWidth=if(focused||selected)1.5.dp else 1.dp
 val borderAlpha=when{
  !enabled->.16f
  focused->.95f
  selected->.88f
  pressed->.82f
  isCeremonial->.72f
  isConfirm->.66f
  isBronze->.56f
  else->.40f
 }
 val atlasImg=if(appearance.minimal)null else ImageBitmap.imageResource(R.drawable.restored_council_buttons_atlas)
 val atlasAlpha=when{
  !enabled->.32f
  !dark->if(isBronze||isCeremonial||isConfirm).86f else .80f
  isCeremonial->.94f
  isBronze||isConfirm||isDestructive->.90f
  else->.86f
 }
 val disabledFilter=if(!enabled)ColorFilter.colorMatrix(ColorMatrix().apply{setToSaturation(0.1f)}) else null
 val contentClr=when{
  !enabled->Color(0xFF9199A2)
  isIron->Color(0xFFE7ECF2)
  isConfirm->Color(0xFFEBF5EA)
  isDestructive->Color(0xFFF6E7E5)
  else->Color(0xFFF7F0E2)
 }
 Button(
  onClick=onClick,
  modifier=modifier
   .heightIn(min=48.dp)
   .graphicsLayer{scaleX=pressScale;scaleY=pressScale}
   .offset{IntOffset(0,depth.dp.roundToPx())}
   .clip(shape)
   .background(Brush.verticalGradient(base))
   .border(borderWidth,accent.copy(alpha=borderAlpha),shape)
   .drawWithCache{
    onDrawBehind{
     if(atlasImg!=null){
      drawAtlasCellNineSlice(
       image=atlasImg,
       index=resolvedStyle.cellIndex,
       columns=2,
       rows=3,
       capXRatio=if(isCeremonial).15f else .13f,
       capYRatio=.24f,
       alpha=atlasAlpha,
       colorFilter=disabledFilter
      )
     }
     val d=1.dp.toPx();val w=size.width;val h=size.height;val cx=w*.5f
     // Subtle center reading veil guarantees AAA text contrast over hammered bronze/verdigris in both themes
     val centerVeil=if(isConfirm||isBronze||isCeremonial) Color(0x420A0C10) else Color(0x220A0C10)
     drawRect(centerVeil)
     drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha=if(enabled).08f else .02f),Color.Transparent,Color.Black.copy(alpha=.34f))))
     if(isDestructive)drawRect(Brush.horizontalGradient(listOf(accent.copy(alpha=.20f),Color.Transparent,accent.copy(alpha=.16f))))
     if(isConfirm&&enabled)drawLine(accent.copy(alpha=.82f),Offset(10*d,h-2.5f*d),Offset(w-10*d,h-2.5f*d),1.4f*d)
     if(selected&&enabled){
      drawRect(goldColor.copy(alpha=.14f))
      drawLine(Color(0xFFE5C88F),Offset(12*d,2*d),Offset(w-12*d,2*d),1.5f*d)
      drawLine(Color(0xFFE5C88F),Offset(12*d,h-2*d),Offset(w-12*d,h-2*d),1.5f*d)
     }
     if(focused&&enabled){
      drawRect(Color(0xFFE5C88F).copy(alpha=.9f),topLeft=Offset(1.5f*d,1.5f*d),size=Size(w-3*d,h-3*d),style=Stroke(1.6f*d))
     }
     if(isBronze&&enabled&&!selected){
      val span=if(isCeremonial)26*d else 16*d
      drawLine(accent.copy(alpha=if(isCeremonial).84f else .62f),Offset(cx-span,h-2.5f*d),Offset(cx+span,h-2.5f*d),d)
      val dm=Path().apply{moveTo(cx,h-4.5f*d);lineTo(cx+2.2f*d,h-2.5f*d);lineTo(cx,h-.5f*d);lineTo(cx-2.2f*d,h-2.5f*d);close()}
      drawPath(dm,accent.copy(alpha=.88f))
      if(isCeremonial){
       listOf(10*d,w-10*d).forEach{rx->
        val sideDm=Path().apply{moveTo(rx,h*.5f-2.8f*d);lineTo(rx+2.8f*d,h*.5f);lineTo(rx,h*.5f+2.8f*d);lineTo(rx-2.8f*d,h*.5f);close()}
        drawPath(sideDm,accent.copy(alpha=.76f))
       }
      }
     }
     if(isIron&&enabled&&!selected)drawLine(ColdIron.copy(alpha=.45f),Offset(cx-12*d,h-2.5f*d),Offset(cx+12*d,h-2.5f*d),d)
     if(pressed&&enabled)drawRect(Color.Black.copy(alpha=.26f))
    }
   }
   .semantics{if(selected)this.selected=true},
  enabled=enabled&&!loading,
  shape=shape,
  interactionSource=interaction,
  contentPadding=PaddingValues(horizontal=if(isCeremonial)26.dp else if(isCompact)12.dp else 16.dp,vertical=if(isCompact)8.dp else 11.dp),
  colors=ButtonDefaults.buttonColors(containerColor=Color.Transparent,contentColor=contentClr,disabledContainerColor=Color.Transparent,disabledContentColor=Color(0xFF889099))
 ){
  if(loading){CircularProgressIndicator(Modifier.size(16.dp),strokeWidth=2.dp,color=contentClr);Spacer(Modifier.width(8.dp))}
  if(leadingRune!=null&&!loading){
   FutharkRuneGlyph(leadingRune,if(enabled)accent else Color(0xFF889099),size=14.dp)
   Spacer(Modifier.width(7.dp))
  }
  val displayText=when{
   completed->"✓ $text"
   selected&&!text.startsWith("◆")->"◆ $text"
   else->text
  }
  Text(displayText,fontSize=if(isCompact)13.sp else 14.sp,letterSpacing=if(isCeremonial).5.sp else .3.sp,fontFamily=if(isCeremonial)SagaFont else null)
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