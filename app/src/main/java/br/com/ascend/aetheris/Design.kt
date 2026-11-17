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
    val scheme=if(dark)darkColorScheme(primary=Color(0xFFC4A36A),onPrimary=Color(0xFF17140E),secondary=Color(0xFF9EB4D3),tertiary=Color(0xFFB4A1C7),background=Color(0xFF090E14),surface=Color(0xFF151E27),surfaceVariant=Color(0xFF24303A),onBackground=Color(0xFFE7DECB),onSurface=Color(0xFFE7DECB),onSurfaceVariant=Color(0xFFAEB5BF),outline=Color(0xFF68717D),error=Color(0xFFD6978D))
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
@Composable fun Meter(value:Float,color:Color=Blue,modifier:Modifier=Modifier,trackColor:Color=CardColor){
    val fraction by animateFloatAsState(value.coerceIn(0f,1f),tween(if(LocalAppearance.current.reducedMotion)0 else 550),label="progresso")
    val track=trackColor
    Canvas(modifier.fillMaxWidth().height(11.dp).semantics{progressBarRangeInfo=ProgressBarRangeInfo(value.coerceIn(0f,1f),0f..1f)}){
        drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.5f),track,Color.Black.copy(alpha=.2f))));drawRect(color.copy(alpha=.3f),style=Stroke(1.dp.toPx()))
        if(fraction>0)drawRect(Brush.horizontalGradient(listOf(color.copy(alpha=.5f),color.copy(alpha=.9f),color)),size=Size(size.width*fraction,size.height))
        listOf(.25f,.5f,.75f).forEach{stop->val x=size.width*stop;drawLine(color.copy(alpha=.65f),Offset(x,0f),Offset(x,size.height),1.dp.toPx())}
        repeat(10){i->val x=size.width*i/10;drawLine(Color.Black.copy(alpha=.32f),Offset(x,0f),Offset(x,size.height),1.dp.toPx())}
        drawLine(Color.White.copy(alpha=.2f),Offset.Zero,Offset(size.width*fraction,0f),1f)
    }
}
@Composable fun Stat(value:String,label:String,color:Color=Gold){Column{Text(value,color=color,fontSize=24.sp,fontFamily=SagaFont);Text(label,color=Muted,fontSize=10.sp,letterSpacing=.6.sp)}}
@Composable fun Action(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,completed:Boolean=false){SagaButton(text,onClick,modifier,enabled=enabled,completed=completed)}
@Composable fun Empty(text:String){Row(Modifier.fillMaxWidth().materialSurface(accent=ColdIron).padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){RuneSeal(0,Muted,Modifier.size(32.dp));Text(text,Modifier.weight(1f),color=Muted,fontSize=14.sp,lineHeight=23.sp)}}
@Composable fun ChipRow(values:List<String>,selected:Int,onSelect:(Int)->Unit){Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){values.forEachIndexed{i,v->FilterChip(selected==i,{onSelect(i)},modifier=Modifier.heightIn(min=48.dp),label={Text(v,fontSize=12.sp)},shape=CutCornerShape(4.dp),colors=FilterChipDefaults.filterChipColors(selectedContainerColor=Gold.copy(alpha=.11f),selectedLabelColor=Gold,labelColor=Muted),border=FilterChipDefaults.filterChipBorder(enabled=true,selected=selected==i,borderColor=Muted.copy(alpha=.28f),selectedBorderColor=Gold.copy(alpha=.7f)))}}}
@Composable fun FormField(value:String,onChange:(String)->Unit,label:String,number:Boolean=false,multi:Boolean=false){OutlinedTextField(value,onChange,Modifier.fillMaxWidth(),label={Text(label)},textStyle=MaterialTheme.typography.bodyLarge,singleLine=!multi,minLines=if(multi)3 else 1,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=if(number)androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text),shape=CutCornerShape(5.dp))}
@Composable fun Toggle(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text(label,Modifier.weight(1f),fontSize=14.sp);Switch(checked,onChange,modifier=Modifier.semantics{contentDescription=label})}}
