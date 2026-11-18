package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*
import kotlin.math.*

data class Appearance(val dark:Boolean=true,val reducedMotion:Boolean=false,val minimal:Boolean=false)
val LocalNarrativeNames=staticCompositionLocalOf { false }
val LocalAppearance=staticCompositionLocalOf { Appearance() }
val Night:Color @Composable get()=MaterialTheme.colorScheme.background
val Surface:Color @Composable get()=MaterialTheme.colorScheme.surface
val CardColor:Color @Composable get()=MaterialTheme.colorScheme.surfaceVariant
val Gold:Color @Composable get()=MaterialTheme.colorScheme.primary
val Blue:Color @Composable get()=MaterialTheme.colorScheme.secondary
val Violet:Color @Composable get()=MaterialTheme.colorScheme.tertiary
val Green:Color @Composable get()=if(LocalAppearance.current.dark)Color(0xFFA8B69A) else Color(0xFF236848)
val Ink:Color @Composable get()=MaterialTheme.colorScheme.onSurface
val Muted:Color @Composable get()=MaterialTheme.colorScheme.onSurfaceVariant
@Composable fun habitColor(index:Int):Color=if(LocalAppearance.current.dark)listOf(Color(0xFFB9965A),Color(0xFF8BA2C7),Color(0xFFB4A1C7),Color(0xFFA8B69A),Color(0xFFD09385))[index] else listOf(Color(0xFF866023),Color(0xFF385FA6),Color(0xFF7551AD),Color(0xFF236848),Color(0xFFA44936))[index]
@Composable fun AscendTheme(mode:String="dark",reducedMotion:Boolean=false,minimal:Boolean=false,content:@Composable ()->Unit){
    val dark=when(mode){"light"->false;"system"->isSystemInDarkTheme();else->true}
    val scheme=if(dark)darkColorScheme(primary=Color(0xFFC4A36A),onPrimary=Color(0xFF17140E),secondary=Color(0xFF9EB4D3),tertiary=Color(0xFFB4A1C7),background=Color(0xFF0D0F0E),surface=Color(0xFF1A1C1A),surfaceVariant=Color(0xFF292C28),onBackground=Color(0xFFE7DECB),onSurface=Color(0xFFE7DECB),onSurfaceVariant=Color(0xFFAEB5BF),outline=Color(0xFF68717D),error=Color(0xFFD6978D))
        else lightColorScheme(primary=Color(0xFF73501F),onPrimary=Color.White,secondary=Color(0xFF375575),tertiary=Color(0xFF655173),background=Color(0xFFE5E1D8),surface=Color(0xFFF1EDE3),surfaceVariant=Color(0xFFD8D3C7),onBackground=Color(0xFF20252D),onSurface=Color(0xFF20252D),onSurfaceVariant=Color(0xFF515B66),outline=Color(0xFF7C817F))
    CompositionLocalProvider(LocalAppearance provides Appearance(dark,reducedMotion,minimal)) {
        MaterialTheme(colorScheme=scheme,shapes=Shapes(extraSmall=CutCornerShape(3.dp),small=CutCornerShape(5.dp),medium=CutCornerShape(8.dp),large=CutCornerShape(12.dp),extraLarge=CutCornerShape(14.dp)),typography=Typography(
            headlineLarge=TextStyle(fontFamily=SagaFont,fontSize=32.sp,lineHeight=40.sp,fontWeight=FontWeight.Medium),
            headlineMedium=TextStyle(fontFamily=SagaFont,fontSize=24.sp,lineHeight=32.sp,fontWeight=FontWeight.Medium),
            titleLarge=TextStyle(fontFamily=SagaFont,fontSize=20.sp,lineHeight=27.sp,fontWeight=FontWeight.Medium),
            titleMedium=TextStyle(fontSize=16.sp,lineHeight=22.sp,fontWeight=FontWeight.Medium),
            bodyLarge=TextStyle(fontSize=16.sp,lineHeight=25.sp),bodyMedium=TextStyle(fontSize=14.sp,lineHeight=22.sp)),content=content)
    }
}
val SagaFont=FontFamily(Font(R.font.saga,FontWeight.Medium))
@Composable fun Eyebrow(text:String,color:Color=Gold){Text(text.uppercase(),color=color,fontSize=11.sp,lineHeight=17.sp,letterSpacing=1.3.sp,fontWeight=FontWeight.SemiBold)}
@Composable fun Title(text:String,sub:String="") {Column(Modifier.padding(top=8.dp,bottom=8.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.width(3.dp).height(24.dp).background(Gold));Text(text,style=MaterialTheme.typography.headlineMedium)};if(sub.isNotEmpty())Text(sub,color=Muted,fontSize=13.sp,lineHeight=21.sp)}}
@Composable fun Panel(modifier:Modifier=Modifier,accent:Color=Gold,content:@Composable ColumnScope.()->Unit){
 AetherSurface(modifier=modifier){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.width(22.dp).height(2.dp).background(accent.copy(alpha=.6f)));Spacer(Modifier.weight(1f))};content()}
}
@Composable fun Page(content:@Composable ColumnScope.()->Unit){
    val dark=LocalAppearance.current.dark
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if(dark)Color(0xFF151E29) else Color(0xFFDEDCCE),Night,Night)))){
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=14.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(16.dp),content=content)
    }
}
@Composable fun Meter(value:Float,color:Color=Blue,modifier:Modifier=Modifier,trackColor:Color=CardColor,slim:Boolean=false){
    val fraction by animateFloatAsState(value.coerceIn(0f,1f),tween(if(LocalAppearance.current.reducedMotion)0 else AetherTokens.scene),label="progresso")
    val dark=LocalAppearance.current.dark;val onParchment=LocalContentColor.current==Sepia
    val track=if(onParchment)Sepia.copy(alpha=.24f) else trackColor
    // 1.6.9.15: a hairline of forged metal with subtle quarter ticks and high contrast on both dark stone and light parchment.
    if(slim){Canvas(modifier.fillMaxWidth().height(7.dp).semantics{progressBarRangeInfo=ProgressBarRangeInfo(value.coerceIn(0f,1f),0f..1f)}){
        val w=size.width;val cy=size.height/2;val d=1.dp.toPx()
        drawLine(if(onParchment)Sepia.copy(alpha=.28f) else Color.Black.copy(alpha=if(dark).72f else .22f),Offset(0f,cy),Offset(w,cy),3.2f*d,cap=StrokeCap.Round)
        drawLine(track.copy(alpha=if(onParchment).55f else .9f),Offset(0f,cy),Offset(w,cy),1.2f*d,cap=StrokeCap.Round)
        listOf(.25f,.5f,.75f).forEach{stop->val x=w*stop;drawLine((if(onParchment)Sepia else ColdIron).copy(alpha=.38f),Offset(x,cy-2*d),Offset(x,cy+2*d),d)}
        if(fraction>0f){val fw=w*fraction
            drawLine(Brush.horizontalGradient(listOf(color.copy(alpha=.35f),color),endX=fw),Offset(0f,cy),Offset(fw,cy),2.4f*d,cap=StrokeCap.Round)
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha=.55f),Color.Transparent),center=Offset(fw,cy),radius=7*d),7*d,Offset(fw,cy))
            drawCircle(color,1.8f*d,Offset(fw,cy))}
    };return}
    val trackImg=if(LocalAppearance.current.minimal||onParchment)null else ImageBitmap.imageResource(R.drawable.orn_track_progress)
    // A groove cut into stone (orn_track_progress); the value fills it like cooled metal with a single bright leading edge.
    Canvas(modifier.fillMaxWidth().height(12.dp).semantics{progressBarRangeInfo=ProgressBarRangeInfo(value.coerceIn(0f,1f),0f..1f)}){
        val w=size.width;val h=size.height;val d=1.dp.toPx();val tip=h*.55f
        val groove=Path().apply{moveTo(tip,0f);lineTo(w-tip,0f);lineTo(w,h/2);lineTo(w-tip,h);lineTo(tip,h);lineTo(0f,h/2);close()}
        val grooveTop=if(onParchment)Sepia.copy(alpha=.32f) else Color.Black.copy(alpha=if(dark).82f else .35f)
        val grooveBot=if(onParchment)Sepia.copy(alpha=.16f) else Color.Black.copy(alpha=if(dark).42f else .18f)
        drawPath(groove,Brush.verticalGradient(listOf(grooveTop,track.copy(alpha=.85f),grooveBot)))
        if(trackImg!=null)drawImage(trackImg,IntOffset(0,0),IntSize(trackImg.width,trackImg.height/2),IntOffset(0,0),IntSize(w.toInt().coerceAtLeast(1),h.toInt().coerceAtLeast(1)),alpha=if(dark).38f else .18f)
        if(fraction>0f){val fw=(w*fraction).coerceAtLeast(tip*2)
            clipPath(groove){
                drawRect(Brush.horizontalGradient(listOf(color.copy(alpha=.42f),color.copy(alpha=.85f),color),endX=fw),size=Size(fw,h))
                if(trackImg!=null)drawImage(trackImg,IntOffset(0,trackImg.height/2),IntSize((trackImg.width*fraction).toInt().coerceAtLeast(1),trackImg.height/2),IntOffset(0,0),IntSize(fw.toInt().coerceAtLeast(1),h.toInt().coerceAtLeast(1)),alpha=if(dark).32f else .16f)
                drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha=.26f),Color.Transparent,Color.Black.copy(alpha=.34f))),size=Size(fw,h))
                if(fraction<1f)drawRect(Brush.horizontalGradient(listOf(Color.Transparent,Color.White.copy(alpha=.52f)),startX=fw-7*d,endX=fw),topLeft=Offset(fw-7*d,0f),size=Size(7*d,h))
            }
        }
        listOf(.25f,.5f,.75f).forEach{stop->val x=w*stop;drawLine((if(onParchment)Sepia else Color.Black).copy(alpha=.55f),Offset(x,h*.18f),Offset(x,h*.82f),d)}
        drawLine(grooveTop,Offset(tip,d*.5f),Offset(w-tip,d*.5f),d)
        drawLine(Color.White.copy(alpha=if(dark).12f else .55f),Offset(tip,h-d*.5f),Offset(w-tip,h-d*.5f),d)
        drawPath(groove,color.copy(alpha=if(onParchment).45f else .34f),style=Stroke(d))
    }
}
@Composable fun Stat(value:String,label:String,color:Color=Gold){Column{Text(value,color=color,fontSize=24.sp,fontFamily=SagaFont);Text(label,color=Muted,fontSize=10.sp,letterSpacing=.6.sp)}}
@Composable fun Action(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,completed:Boolean=false,ceremonial:Boolean=false,secondary:Boolean=false){SagaButton(text,onClick,modifier,enabled=enabled,completed=completed,ceremonial=ceremonial,secondary=secondary)}
@Composable fun Empty(text:String){Row(Modifier.fillMaxWidth().materialSurface(accent=ColdIron).padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){RuneSeal(0,Muted,Modifier.size(34.dp));Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){Box(Modifier.width(24.dp).height(1.dp).background(Gold.copy(alpha=.45f)));Text(text,color=Muted,fontSize=14.sp,lineHeight=23.sp)}}}
@Composable fun ChipRow(values:List<String>,selected:Int,onSelect:(Int)->Unit,scrollable:Boolean=true){
    val dark=LocalAppearance.current.dark;val gold=Gold
    Row(if(scrollable)Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()) else Modifier,horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically){
        values.forEachIndexed{i,v->val isSel=selected==i;val shape=CutCornerShape(topStart=5.dp,topEnd=2.dp,bottomEnd=5.dp,bottomStart=2.dp)
            Box(Modifier.heightIn(min=34.dp).clip(shape).background(Brush.verticalGradient(if(isSel)listOf(gold.copy(alpha=if(dark).24f else .18f),gold.copy(alpha=.08f)) else listOf(Surface.copy(alpha=.75f),Surface.copy(alpha=.45f)))).pictorialFrame(R.drawable.ui_tablet_selector,capXRatio=.2f,capYRatio=.28f,alpha=if(dark)(if(isSel).64f else .38f) else .16f).border(1.dp,if(isSel)gold.copy(alpha=.78f) else ColdIron.copy(alpha=.28f),shape).drawWithCache{onDrawBehind{
                val d=1.dp.toPx()
                drawLine(Color.White.copy(alpha=if(isSel).18f else .06f),Offset(5*d,d),Offset(size.width-5*d,d),d)
                if(isSel)drawLine(gold,Offset(size.width*.22f,size.height-1.5f*d),Offset(size.width*.78f,size.height-1.5f*d),1.5f*d)
            }}.semantics(mergeDescendants=true){this.selected=isSel;role=Role.Tab}.clickable{onSelect(i)}.padding(horizontal=12.dp,vertical=6.dp),contentAlignment=Alignment.Center){
                Text(v,color=if(isSel)gold else Muted,fontSize=12.sp,letterSpacing=.2.sp,fontWeight=if(isSel)FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}
@Composable fun FormField(value:String,onChange:(String)->Unit,label:String,number:Boolean=false,multi:Boolean=false,modifier:Modifier=Modifier){
    val dark=LocalAppearance.current.dark;val gold=Gold;val iron=ColdIron;val shape=CutCornerShape(topStart=6.dp,topEnd=2.dp,bottomEnd=6.dp,bottomStart=2.dp)
    val fieldImg=if(LocalAppearance.current.minimal)null else ImageBitmap.imageResource(R.drawable.ui_field_inscription)
    Box(modifier.fillMaxWidth().drawWithCache{onDrawBehind{
        val d=1.dp.toPx();val w=size.width;val h=size.height;val topInset=7*d
        if(fieldImg!=null&&h>topInset+8*d)withTransform({translate(0f,topInset);scale(1f,(h-topInset)/h,Offset.Zero)}){drawNineSlice(fieldImg,capXRatio=.14f,capYRatio=.28f,alpha=if(dark).58f else .14f)}
        drawLine(gold.copy(alpha=.42f),Offset(8*d,h-d),Offset(w-8*d,h-d),1.4f*d)
        drawLine(iron.copy(alpha=.35f),Offset(2*d,10*d),Offset(2*d,18*d),d)
        drawLine(iron.copy(alpha=.35f),Offset(w-2*d,h-10*d),Offset(w-2*d,h-3*d),d)
    }}){
        OutlinedTextField(value,onChange,Modifier.fillMaxWidth(),label={Text(label,letterSpacing=.3.sp)},textStyle=MaterialTheme.typography.bodyLarge,singleLine=!multi,minLines=if(multi)3 else 1,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=if(number)androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text),shape=shape,colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=gold.copy(alpha=.82f),unfocusedBorderColor=iron.copy(alpha=.42f),focusedLabelColor=gold,unfocusedLabelColor=Muted,focusedContainerColor=if(dark)Color(0xFF141719).copy(alpha=.78f) else Color(0xFFEAE4D7).copy(alpha=.88f),unfocusedContainerColor=if(dark)Color(0xFF111416).copy(alpha=.78f) else Color(0xFFE3DDD0).copy(alpha=.88f),cursorColor=gold))
    }
}
@Composable fun Toggle(label:String,checked:Boolean,onChange:(Boolean)->Unit){
    val dark=LocalAppearance.current.dark
    Row(Modifier.fillMaxWidth().padding(vertical=2.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
        Text(label,Modifier.weight(1f).padding(end=12.dp),fontSize=14.sp,color=Ink)
        Box(Modifier.pictorialFrame(R.drawable.ui_control_surface,capXRatio=.28f,capYRatio=.35f,alpha=if(dark).68f else .22f).padding(horizontal=4.dp,vertical=2.dp),contentAlignment=Alignment.Center){
            Switch(checked,onChange,modifier=Modifier.semantics{contentDescription=label},colors=SwitchDefaults.colors(
                checkedThumbColor=if(dark)Color(0xFFF0E4CC) else Color.White,
                checkedTrackColor=Gold.copy(alpha=if(dark).65f else .82f),
                checkedBorderColor=Gold,
                uncheckedThumbColor=Muted,
                uncheckedTrackColor=if(dark)Color(0xFF14181B).copy(alpha=.75f) else Color(0xFFD5CFC2),
                uncheckedBorderColor=ColdIron.copy(alpha=.48f)
            ))
        }
    }
}