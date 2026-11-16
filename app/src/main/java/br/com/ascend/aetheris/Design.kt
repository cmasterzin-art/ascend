package br.com.ascend.aetheris

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*
import kotlin.math.*

data class Appearance(val dark:Boolean=true,val reducedMotion:Boolean=false,val minimal:Boolean=false)
val LocalAppearance=staticCompositionLocalOf { Appearance() }
val Night:Color @Composable get()=MaterialTheme.colorScheme.background
val Surface:Color @Composable get()=MaterialTheme.colorScheme.surface
val CardColor:Color @Composable get()=MaterialTheme.colorScheme.surfaceVariant
val Gold:Color @Composable get()=MaterialTheme.colorScheme.primary
val Blue:Color @Composable get()=MaterialTheme.colorScheme.secondary
val Violet:Color @Composable get()=MaterialTheme.colorScheme.tertiary
val Green:Color @Composable get()=if(LocalAppearance.current.dark)Color(0xFF79CBA4) else Color(0xFF236848)
val Ink:Color @Composable get()=MaterialTheme.colorScheme.onSurface
val Muted:Color @Composable get()=MaterialTheme.colorScheme.onSurfaceVariant
@Composable fun habitColor(index:Int):Color=if(LocalAppearance.current.dark)Color(palette[index]) else listOf(Color(0xFF866023),Color(0xFF385FA6),Color(0xFF7551AD),Color(0xFF236848),Color(0xFFA44936))[index]
@Composable fun AscendTheme(mode:String="dark",reducedMotion:Boolean=false,minimal:Boolean=false,content:@Composable ()->Unit){
    val dark=when(mode){"light"->false;"system"->isSystemInDarkTheme();else->true}
    val scheme=if(dark)darkColorScheme(primary=Color(0xFFE1BC79),onPrimary=Color(0xFF201B12),secondary=Color(0xFF94AFF2),tertiary=Color(0xFFB6A0EA),background=Color(0xFF0D131B),surface=Color(0xFF17212C),surfaceVariant=Color(0xFF23303E),onBackground=Color(0xFFF1EEE7),onSurface=Color(0xFFF1EEE7),onSurfaceVariant=Color(0xFFACB9C9),outline=Color(0xFF46566B))
        else lightColorScheme(primary=Color(0xFF79551D),onPrimary=Color.White,secondary=Color(0xFF385FA6),tertiary=Color(0xFF7551AD),background=Color(0xFFF6F3EC),surface=Color(0xFFFFFDF7),surfaceVariant=Color(0xFFEAE7DF),onBackground=Color(0xFF222B35),onSurface=Color(0xFF222B35),onSurfaceVariant=Color(0xFF596573),outline=Color(0xFF9BA4AD))
    CompositionLocalProvider(LocalAppearance provides Appearance(dark,reducedMotion,minimal)) {
        MaterialTheme(colorScheme=scheme,typography=Typography(
            headlineLarge=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=34.sp,fontWeight=FontWeight.Medium),
            headlineMedium=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=28.sp,fontWeight=FontWeight.Medium),
            titleLarge=androidx.compose.ui.text.TextStyle(fontFamily=FontFamily.Serif,fontSize=22.sp,fontWeight=FontWeight.Medium)),content=content)
    }
}
@Composable fun Eyebrow(text:String,color:Color=Gold){Text(text.uppercase(),color=color,fontSize=10.sp,letterSpacing=2.sp,fontWeight=FontWeight.Bold)}
@Composable fun Title(text:String,sub:String="") {Column(Modifier.padding(vertical=6.dp)){Text(text,style=MaterialTheme.typography.headlineMedium);if(sub.isNotEmpty())Text(sub,color=Muted,fontSize=13.sp)}}
@Composable fun Panel(modifier:Modifier=Modifier,accent:Color=Gold,content:@Composable ColumnScope.()->Unit){Column(modifier.fillMaxWidth().border(1.dp,accent.copy(alpha=.18f),RoundedCornerShape(18.dp)).background(Surface,RoundedCornerShape(18.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}
@Composable fun Page(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(top=14.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(16.dp),content=content)}
@Composable fun Meter(value:Float,color:Color=Blue,modifier:Modifier=Modifier){val fraction by animateFloatAsState(value.coerceIn(0f,1f),tween(if(LocalAppearance.current.reducedMotion)0 else 650),label="progresso");LinearProgressIndicator(progress={fraction},modifier=modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),color=color,trackColor=CardColor)}
@Composable fun Stat(value:String,label:String,color:Color=Gold){Column{Text(value,color=color,fontSize=24.sp,fontFamily=FontFamily.Serif);Text(label,color=Muted,fontSize=11.sp)}}
@Composable fun Action(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true){Button(onClick,modifier.heightIn(min=48.dp),enabled=enabled,shape=RoundedCornerShape(12.dp)){Text(text)}}
@Composable fun Empty(text:String){Panel(accent=Muted){Text("✧",color=Gold,fontSize=26.sp);Text(text,color=Muted,fontSize=14.sp)}}
@Composable fun ChipRow(values:List<String>,selected:Int,onSelect:(Int)->Unit){Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)){values.forEachIndexed{i,v->FilterChip(selected==i,{onSelect(i)},label={Text(v)})}}}
@Composable fun FormField(value:String,onChange:(String)->Unit,label:String,number:Boolean=false,multi:Boolean=false){OutlinedTextField(value,onChange,Modifier.fillMaxWidth(),label={Text(label)},singleLine=!multi,minLines=if(multi)3 else 1,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=if(number)androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text),shape=RoundedCornerShape(12.dp))}
@Composable fun Toggle(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text(label,Modifier.weight(1f),fontSize=14.sp);Switch(checked,onChange)}}
@Composable fun Avatar(p:Player,s:GameState?=null,size:Dp=100.dp){
    val color=habitColor(p.color);val Night=Night;val Gold=Gold;val Violet=Violet;val Green=Green;val Blue=Blue;val companion=s?.assets?.find{it.kind=="companion" && it.equipped};val astral=s?.assets?.any{it.id=="frame"&&it.equipped}==true
    val outfit=s?.assets?.find{it.kind=="outfit"&&it.equipped}?.id
    Canvas(Modifier.size(size).semanticsAvatar(p)){
        val w=this.size.width;val h=this.size.height
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha=.24f),Night)),w*.47f)
        drawCircle(if(astral) Violet else color.copy(alpha=.65f),w*.46f,style=Stroke(if(astral)4f else 2f))
        repeat(if(p.frame>0||astral)16 else 8){i->val a=i*2*PI/(if(p.frame>0||astral)16 else 8);drawCircle(Gold,w*.012f,Offset(w/2+cos(a).toFloat()*w*.46f,h/2+sin(a).toFloat()*h*.46f))}
        val robe=when(outfit){"cloak"->Violet;"armor"->Gold;else->if(p.outfit==1)Green else if(p.outfit==2)Gold else color}
        val path=Path().apply{moveTo(w*.2f,h*.84f);quadraticTo(w*.22f,h*.53f,w*.43f,h*.49f);lineTo(w*.57f,h*.49f);quadraticTo(w*.78f,h*.53f,w*.8f,h*.84f);close()}
        drawPath(path,Brush.verticalGradient(listOf(robe,robe.copy(alpha=.3f))))
        drawLine(Gold,Offset(w*.5f,h*.55f),Offset(w*.5f,h*.84f),w*.018f)
        drawOval(when(p.face){1->Color(0xFFA67557);2->Color(0xFFD7A981);else->Color(0xFFE9C7A3)},Offset(w*.37f,h*.26f),Size(w*.26f,h*.3f))
        val hood=Path().apply{moveTo(w*.3f,h*.38f);lineTo(w*.5f,h*.13f);lineTo(w*.7f,h*.38f);lineTo(w*.6f,h*.31f);lineTo(w*.4f,h*.31f);close()}
        drawPath(hood,robe.copy(alpha=.95f));drawLine(Night,Offset(w*.43f,h*.4f),Offset(w*.46f,h*.4f),w*.025f);drawLine(Night,Offset(w*.54f,h*.4f),Offset(w*.57f,h*.4f),w*.025f)
        if(p.heroClass==1){drawLine(Gold,Offset(w*.78f,h*.75f),Offset(w*.78f,h*.3f),w*.025f);drawCircle(Blue,w*.045f,Offset(w*.78f,h*.26f))}
        else if(p.heroClass==0){drawLine(Gold,Offset(w*.78f,h*.7f),Offset(w*.78f,h*.32f),w*.035f);drawLine(Gold,Offset(w*.7f,h*.56f),Offset(w*.86f,h*.56f),w*.025f)}
        else {drawCircle(color,w*.07f,Offset(w*.75f,h*.66f),style=Stroke(w*.02f))}
        if(companion!=null){val c=if(companion.id=="lyra")Gold else if(companion.id=="corvus")Violet else Green;drawCircle(c,w*.07f,Offset(w*.25f,h*.72f));drawPath(Path().apply{moveTo(w*.18f,h*.68f);lineTo(w*.18f,h*.59f);lineTo(w*.24f,h*.66f);lineTo(w*.3f,h*.59f);lineTo(w*.32f,h*.7f);close()},c)}
    }
}
private fun Modifier.semanticsAvatar(p:Player)=this.then(Modifier.semantics { contentDescription="Avatar de ${p.name}, ${classes[p.heroClass]}" })
@Composable fun Landscape(s:GameState,modifier:Modifier=Modifier,large:Boolean=false){
    val Night=Color(0xFF0D131B);val Gold=Color(0xFFE1BC79);val Ink=Color(0xFFF1EEE7)
    Canvas(modifier.fillMaxWidth().height(if(large)240.dp else 150.dp).clip(RoundedCornerShape(16.dp))){
        val w=size.width;val h=size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1B2440),Color(0xFF111D29))))
        repeat(45){i->val x=((i*73+19)%997)/997f*w;val y=((i*137+31)%491)/491f*h*.6f;drawCircle(Ink.copy(alpha=.3f),if(i%3==0)1.6f else .8f,Offset(x,y))}
        drawCircle(Gold.copy(alpha=.15f),h*.16f,Offset(w*.8f,h*.23f));drawCircle(Gold.copy(alpha=.75f),h*.065f,Offset(w*.8f,h*.23f))
        fun mountain(x:Float,y:Float,width:Float,height:Float,c:Color){drawPath(Path().apply{moveTo(x-width/2,y);lineTo(x,y-height);lineTo(x+width/2,y);close()},c)}
        repeat(7){i->mountain(w*i/5,h*.8f,w*.4f,h*(.4f+(i%3)*.08f),Color(0xFF263249))}
        drawOval(Color(0xFF1C302E),Offset(-w*.2f,h*.65f),Size(w*1.4f,h*.6f))
        drawPath(Path().apply{moveTo(w*.5f,h);lineTo(w*.53f,h*.72f);lineTo(w*.57f,h*.72f);lineTo(w*.69f,h);close()},Gold.copy(alpha=.12f))
        repeat(5){i->
            val x=w*(.12f+i*.18f);val y=h*(.83f-(i%2)*.1f);val bw=w*.1f;val bh=h*(if(i==3).36f else .22f);val stage=s.assets.find{it.id=="building:$i"}?.stage?:0
            val stone=if(stage>0)Color(0xFF566075) else Color(0xFF323E50)
            drawRect(stone,Offset(x-bw/2,y-bh),Size(bw,bh))
            if(stage>0)mountain(x,y-bh,bw*1.35f,bh*.4f,if(stage==2)Gold.copy(alpha=.65f) else Color(0xFF4A5670))
            else {drawRect(Color(0xFF1B2440),Offset(x,y-bh),Size(bw*.3f,bh*.3f))}
            if(stage==2)drawCircle(Brush.radialGradient(listOf(Gold.copy(alpha=.25f),Color.Transparent),center=Offset(x,y-bh*.5f),radius=bw*.8f),bw*.8f,Offset(x,y-bh*.5f))
            drawRect(if(stage==2)Gold else Night,Offset(x-bw*.16f,y-bh*.65f),Size(bw*.32f,bh*.3f))
            drawRect(Night,Offset(x-bw*.13f,y-bh*.28f),Size(bw*.26f,bh*.28f))
        }
        repeat(8){i->val x=w*i/7;mountain(x,h*.97f,w*.06f,h*.18f,Color(0xFF101F24))}
    }
}
