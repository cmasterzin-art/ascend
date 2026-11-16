package br.com.ascend.aetheris

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
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlin.math.*

val Parchment=Color(0xFFE5C58B)
val Sepia=Color(0xFF442C19)
@Composable fun Manuscript(content:@Composable ColumnScope.()->Unit){
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Brush.linearGradient(listOf(Color(0xFFC69B60),Parchment,Color(0xFFD4AE73)))).border(1.dp,Color(0xFF916332),RoundedCornerShape(4.dp))){
        Canvas(Modifier.matchParentSize()){
            repeat(480){i->val x=((i*137)%997)/997f*size.width;val y=((i*83)%991)/991f*size.height;drawCircle(Sepia.copy(alpha=.055f),if(i%3==0)1.4f else .6f,Offset(x,y))}
            drawRect(Sepia.copy(alpha=.28f),Offset(7.dp.toPx(),7.dp.toPx()),Size(size.width-14.dp.toPx(),size.height-14.dp.toPx()),style=Stroke(1f))
            listOf(Offset(14.dp.toPx(),14.dp.toPx()),Offset(size.width-14.dp.toPx(),14.dp.toPx()),Offset(14.dp.toPx(),size.height-14.dp.toPx()),Offset(size.width-14.dp.toPx(),size.height-14.dp.toPx())).forEach{drawCircle(Sepia.copy(alpha=.4f),5.dp.toPx(),it,style=Stroke(1f))}
        }
        CompositionLocalProvider(LocalContentColor provides Sepia){Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp),content=content)}
    }
}
@Composable fun rarityColor(n:Int)=listOf(Muted,Green,Blue,Violet,Gold)[n]
@Composable fun RelicArt(kind:String,color:Color,modifier:Modifier=Modifier){
    Canvas(modifier.size(58.dp)){
        val w=size.width;val h=size.height
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha=.2f),Color.Transparent)),w*.48f)
        fun path(vararg xy:Float):Path=Path().apply{moveTo(xy[0]*w,xy[1]*h);for(i in 2 until xy.size step 2)lineTo(xy[i]*w,xy[i+1]*h);close()}
        when(kind){
            "weapon"->{drawPath(path(.46f,.68f,.44f,.2f,.5f,.08f,.57f,.2f,.54f,.68f),color);drawLine(Color(0xFFD6AD67),Offset(w*.28f,h*.65f),Offset(w*.72f,h*.65f),w*.06f);drawLine(color,Offset(w*.5f,h*.65f),Offset(w*.5f,h*.9f),w*.06f)}
            "head"->{drawPath(path(.2f,.72f,.16f,.27f,.35f,.43f,.5f,.15f,.65f,.43f,.84f,.27f,.8f,.72f),color);drawLine(Color(0xFF392A20),Offset(w*.25f,h*.61f),Offset(w*.75f,h*.61f),w*.035f)}
            "outfit"->{drawPath(path(.4f,.18f,.6f,.18f,.84f,.83f,.52f,.72f,.16f,.83f),color);drawLine(Color(0xFFE8D0A1),Offset(w*.5f,h*.23f),Offset(w*.5f,h*.72f),w*.025f)}
            "companion"->{drawPath(path(.2f,.6f,.15f,.14f,.4f,.35f,.6f,.35f,.85f,.14f,.8f,.6f,.5f,.85f),color);drawCircle(Color(0xFF111722),w*.035f,Offset(w*.36f,h*.49f));drawCircle(Color(0xFF111722),w*.035f,Offset(w*.64f,h*.49f))}
            "background"->{repeat(3){i->drawRect(color.copy(alpha=.45f+i*.2f),Offset(w*(.18f+i*.23f),h*.22f),Size(w*.17f,h*.56f))};drawLine(color,Offset(w*.1f,h*.8f),Offset(w*.9f,h*.8f),w*.05f)}
            "frame"->{drawCircle(color,w*.32f,style=Stroke(w*.035f));repeat(8){i->val a=i*PI/4;drawCircle(color,w*.035f,Offset(w*.5f+cos(a).toFloat()*w*.39f,h*.5f+sin(a).toFloat()*h*.39f))}}
            else->{drawPath(path(.5f,.15f,.73f,.43f,.5f,.84f,.27f,.43f),color);drawPath(path(.5f,.15f,.5f,.84f,.27f,.43f),Color.White.copy(alpha=.22f))}
        }
    }
}
@Composable fun AetherisMap(s:GameState,selected:Int,onSelect:(Int)->Unit){
    val xs=listOf(.24f,.67f,.32f,.72f,.23f,.65f);val ys=listOf(.81f,.68f,.52f,.37f,.22f,.09f)
    BoxWithConstraints(Modifier.fillMaxWidth().height(480.dp).clip(RoundedCornerShape(6.dp)).background(Parchment).border(2.dp,Color(0xFF8E673E))){
        Canvas(Modifier.matchParentSize()){
            val w=size.width;val h=size.height
            drawRect(Brush.radialGradient(listOf(Parchment,Color(0xFFB28C55)),center=Offset(w*.5f,h*.5f),radius=h*.8f))
            repeat(900){i->drawCircle(Sepia.copy(alpha=.09f),.8f,Offset(((i*137)%997)/997f*w,((i*97)%991)/991f*h))}
            // Ink ridges, woodland and river are original deterministic vector illustrations.
            repeat(25){i->val x=if(i%2==0)w*(.03f+(i%5)*.032f) else w*(.85f+(i%5)*.028f);val y=h*(.05f+i*.034f);val p=Path().apply{moveTo(x-15,y+22);lineTo(x,y-22);lineTo(x+19,y+22);close()};drawPath(p,Color(0xFF9E9064));drawPath(p,Sepia.copy(alpha=.55f),style=Stroke(1.2f));drawLine(Sepia.copy(alpha=.3f),Offset(x,y-22),Offset(x+7,y+18),1f)}
            val river=Path().apply{moveTo(w*.48f,0f);cubicTo(w*.9f,h*.22f,w*.12f,h*.43f,w*.52f,h*.6f);cubicTo(w*.9f,h*.8f,w*.15f,h*.85f,w*.1f,h)}
            drawPath(river,Sepia.copy(alpha=.45f),style=Stroke(14f));drawPath(river,Color(0xFF7F9B95),style=Stroke(10f));drawPath(river,Color(0xFFB0BDB0),style=Stroke(3f))
            repeat(70){i->val x=w*(.38f+((i*37)%101)/101f*.42f);val y=h*(.55f+((i*61)%103)/103f*.3f);drawLine(Sepia,Offset(x,y),Offset(x,y+10),1.5f);drawOval(Color(0xFF65774B).copy(alpha=.8f),Offset(x-6,y-8),Size(12f,15f));drawOval(Sepia.copy(alpha=.35f),Offset(x-6,y-8),Size(12f,15f),style=Stroke(1f))}
            val trail=Path().apply{moveTo(w*xs[0],h*ys[0]);for(i in 1..5)quadraticTo(w*xs[i-1],h*ys[i],w*xs[i],h*ys[i])}
            drawPath(trail,Color(0xFF8C4D2B),style=Stroke(2.dp.toPx(),pathEffect=PathEffect.dashPathEffect(floatArrayOf(6f,7f))))
            // Compass rose.
            translate(w*.83f,h*.9f){repeat(8){i->rotate(i*45f,Offset.Zero){drawPath(Path().apply{moveTo(0f,-22f);lineTo(4f,0f);lineTo(0f,10f);lineTo(-4f,0f);close()},Sepia.copy(alpha=if(i%2==0).75f else .35f))}}}
        }
        regions.forEachIndexed{i,name->val open=s.level>=regionLevels[i]
            Column(Modifier.offset(x=maxWidth*xs[i]-43.dp,y=480.dp*ys[i]-20.dp).width(86.dp).clip(RoundedCornerShape(8.dp)).background(if(selected==i)Color(0xFFF5DEAA) else Parchment.copy(alpha=.85f)).border(if(selected==i)2.dp else 1.dp,if(open)Sepia else Color(0xFF8C806D),RoundedCornerShape(8.dp)).clickable(onClickLabel="Explorar $name"){onSelect(i)}.padding(5.dp).semantics(mergeDescendants=true){contentDescription="$name, ${if(open)"revelada" else "nível ${regionLevels[i]}"}"},horizontalAlignment=Alignment.CenterHorizontally){
                Text(if(open)listOf("⌂","♧","▤","▲","☽","♜")[i] else "◇",color=Sepia,fontSize=25.sp)
                Text(if(open)name.substringBefore(" ") else "Nível ${regionLevels[i]}",color=Sepia,fontSize=10.sp)
            }
        }
    }
}
