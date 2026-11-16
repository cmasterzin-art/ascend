package br.com.ascend.aetheris

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlin.math.*

val OldBronze=Color(0xFF8C6B3F)
val WornIvory=Color(0xFFD8CCB0)
val ColdIron=Color(0xFF68717D)
val Ember=Color(0xFF8D4B43)

/** Cached, deterministic grain: no bitmap textures, timers or moving particles. */
@Composable fun Modifier.materialSurface(accent:Color=Gold,leather:Boolean=false):Modifier {
    val dark=LocalAppearance.current.dark
    val top=if(dark)if(leather)Color(0xFF302923) else Color(0xFF222A34) else if(leather)Color(0xFFE0D6C3) else Color(0xFFEAE7DE)
    val bottom=if(dark)if(leather)Color(0xFF151413) else Color(0xFF111720) else Color(0xFFD8D7CE)
    val minimal=LocalAppearance.current.minimal
    return clip(CutCornerShape(6.dp)).background(Brush.linearGradient(listOf(top,bottom)))
        .drawWithCache {
            val grain=Path().apply{repeat(if(minimal)0 else 110){i->val x=(i*137%991)/991f*size.width;val y=(i*73%983)/983f*size.height;moveTo(x,y);lineTo(x+if(leather)13f else 3f,y+1f)}}
            onDrawBehind {
                drawPath(grain,if(dark)Color.White.copy(alpha=.018f) else Color.Black.copy(alpha=.045f),style=Stroke(1f))
                drawLine(accent.copy(alpha=.48f),Offset(10.dp.toPx(),0f),Offset(size.width-10.dp.toPx(),0f),1.dp.toPx())
                drawLine(Color.Black.copy(alpha=.22f),Offset(0f,size.height-2f),Offset(size.width,size.height-2f),2f)
                listOf(Offset(7.dp.toPx(),7.dp.toPx()),Offset(size.width-7.dp.toPx(),7.dp.toPx())).forEach{p->drawCircle(ColdIron.copy(alpha=.7f),1.6.dp.toPx(),p);drawCircle(Color.White.copy(alpha=.2f),.6.dp.toPx(),p-Offset(.5f,.5f))}
            }
        }.border(1.dp,accent.copy(alpha=.22f),CutCornerShape(6.dp))
}
@Composable fun SagaDivider(modifier:Modifier=Modifier,color:Color=Gold){
    Canvas(modifier.fillMaxWidth().height(12.dp)){
        val y=size.height/2;val x=size.width/2
        drawLine(color.copy(alpha=.4f),Offset(0f,y),Offset(x-8.dp.toPx(),y),1f)
        drawLine(color.copy(alpha=.4f),Offset(x+8.dp.toPx(),y),Offset(size.width,y),1f)
        drawPath(Path().apply{moveTo(x,y-4.dp.toPx());lineTo(x+4.dp.toPx(),y);lineTo(x,y+4.dp.toPx());lineTo(x-4.dp.toPx(),y);close()},color,style=Stroke(1f))
    }
}
@Composable fun RuneSeal(rank:Int,color:Color,modifier:Modifier=Modifier){
    Canvas(modifier.size(40.dp)){
        val r=size.minDimension*.44f;val center=Offset(size.width/2,size.height/2)
        drawCircle(color.copy(alpha=.1f),r,center)
        drawCircle(color.copy(alpha=.65f),r,center,style=Stroke(1.dp.toPx()))
        if(rank>=2)drawCircle(color.copy(alpha=.3f),r*.78f,center,style=Stroke(1f))
        val x=center.x;val y=center.y;val a=r*.47f
        drawLine(color,Offset(x,y-a),Offset(x,y+a),1.5.dp.toPx())
        drawLine(color,Offset(x-a*.7f,y-a*.4f),Offset(x,y-a),1.5.dp.toPx())
        drawLine(color,Offset(x,y-a),Offset(x+a*.7f,y-a*.4f),1.5.dp.toPx())
        if(rank>0)drawLine(color,Offset(x-a*.7f,y+a*.2f),Offset(x+a*.7f,y-a*.2f),1.3.dp.toPx())
        if(rank>=3)repeat(4){i->rotate(i*90f,center){drawLine(color,Offset(x,y-r-2f),Offset(x,y-r+5f),2f)}}
        if(rank==4)drawCircle(color.copy(alpha=.4f),r*1.08f,center,style=Stroke(2f))
    }
}

@Composable fun Avatar(p:Player,s:GameState?=null,size:Dp=100.dp){
    val assets=s?.assets.orEmpty().filter{it.equipped}
    val color=habitColor(p.color)
    val border=when(assets.find{it.kind=="frame"}?.id){"rune-frame"->Color(0xFFB4A1C7);"frame"->WornIvory;else->if(p.frame==1)Color(0xFFB9965A) else if(p.frame==2)Color(0xFF8BA2C7) else OldBronze}
    val portrait=when(p.face){1->R.drawable.wanderer_dark;2->R.drawable.wanderer_medium;else->R.drawable.wanderer_light}
    val appearance=LocalAppearance.current
    val signature=assets.joinToString{it.id}+"${p.color}:${p.outfit}:${p.frame}:${p.face}"
    val flash=remember{Animatable(0f)}
    LaunchedEffect(signature){if(!appearance.reducedMotion){flash.snapTo(.28f);flash.animateTo(0f,tween(650))}else flash.snapTo(0f)}
    Box(Modifier.width(size).height(size*1.5f).clip(CutCornerShape(topStart=14.dp,topEnd=14.dp,bottomStart=4.dp,bottomEnd=4.dp)).background(Color(0xFF0A0D12)).semantics{contentDescription="Avatar de ${p.name}, ${classes[p.heroClass]}. Equipados: ${assets.filter{a->shopItems.any{it.id==a.id}}.joinToString{a->shopItems.first{it.id==a.id}.name}.ifBlank{"nenhum"}}"}){
        Image(painterResource(portrait),null,Modifier.matchParentSize(),contentScale=ContentScale.FillBounds)
        Canvas(Modifier.matchParentSize()){
            scale(this.size.width/100f,this.size.height/150f,Offset.Zero){
                fun poly(vararg xy:Float)=Path().apply{moveTo(xy[0],xy[1]);for(i in 2 until xy.size step 2)lineTo(xy[i],xy[i+1]);close()}
                // Equipment occupies real display niches outside this painting, not artificial body overlays.
                drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color(0xFF070A0F).copy(alpha=.35f)),startY=130f,endY=150f))
                drawRect(border.copy(alpha=.7f),Offset(1.5f,1.5f),Size(97f,147f),style=Stroke(.65f))
                drawRect(border.copy(alpha=.25f),Offset(3f,3f),Size(94f,144f),style=Stroke(.4f))
                repeat(if(p.frame>0||assets.any{it.kind=="frame"})12 else 4){i->val y=8f+i*11.6f;drawPath(poly(2f,y,5f,y+3,2f,y+6),border,style=Stroke(.6f));drawPath(poly(98f,y,95f,y+3,98f,y+6),border,style=Stroke(.6f))}
                drawLine(color.copy(alpha=.7f),Offset(12f,146f),Offset(88f,146f),if(p.outfit==0).6f else if(p.outfit==1)1.2f else 2f)
                drawRect(border.copy(alpha=flash.value))
            }
        }

    }
}

@Composable fun Landscape(s:GameState,modifier:Modifier=Modifier,large:Boolean=false){
    val stages=(0..4).map{i->s.assets.find{it.id=="building:$i"}?.stage?:0}
    Box(modifier.fillMaxWidth().height(if(large)260.dp else 168.dp).clip(CutCornerShape(7.dp)).semantics{contentDescription="Vila do Despertar. ${stages.sum()} de 10 etapas recuperadas."}){
        Image(painterResource(R.drawable.nordic_fjord),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
        Canvas(Modifier.matchParentSize()){
            val w=size.width;val h=size.height
            // Restoration lights belong to the painted settlement, without opaque blocks over the art.
            val sites=listOf(.81f to .57f,.43f to .73f,.28f to .76f,.69f to .66f,.55f to .82f)
            stages.forEachIndexed{i,stage->val x=w*sites[i].first;val y=h*sites[i].second
                if(stage>0){
                    val roof=Path().apply{moveTo(x-8.dp.toPx(),y);lineTo(x,y-7.dp.toPx());lineTo(x+8.dp.toPx(),y)}
                    drawPath(roof,OldBronze.copy(alpha=.5f),style=Stroke(.8.dp.toPx()))
                    if(stage==2){
                        val c=Offset(x,y+3.dp.toPx());val radius=18.dp.toPx()
                        drawCircle(Brush.radialGradient(listOf(Color(0xFFCE904E).copy(alpha=.5f),Color.Transparent),c,radius),radius,c)
                        drawLine(Color(0xFFE1B271),Offset(x-2.dp.toPx(),y),Offset(x-2.dp.toPx(),y+5.dp.toPx()),1.5.dp.toPx())
                        drawLine(Color(0xFFB78B58),Offset(x+3.dp.toPx(),y),Offset(x+3.dp.toPx(),y+4.dp.toPx()),1.dp.toPx())
                    }
                }
            }
            if(s.assets.any{it.id=="lantern"&&it.equipped}){val c=Offset(w*.5f,h*.86f);drawCircle(Brush.radialGradient(listOf(Color(0xFFE2A95F).copy(alpha=.6f),Color.Transparent),c,h*.13f),h*.13f,c);drawCircle(WornIvory,2.dp.toPx(),c)}
            drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color(0xFF080D13).copy(alpha=.6f))))
        }
        Text("VILA DO DESPERTAR  /  ${stages.sum()} DE 10",Modifier.align(Alignment.BottomStart).padding(14.dp),color=WornIvory,fontSize=10.sp,letterSpacing=1.5.sp)
    }
}

@Composable fun HeroSaga(s:GameState){
    val p=s.player!!;val minimal=LocalAppearance.current.minimal
    val xp=Progression.current(s.xp);val next=Progression.requirement(s.level)
    Box(Modifier.fillMaxWidth().clip(CutCornerShape(10.dp)).border(1.dp,OldBronze.copy(alpha=.6f),CutCornerShape(10.dp))){
        if(!minimal)Image(painterResource(R.drawable.nordic_fjord),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color(0xFF0A1018).copy(alpha=.45f),Color(0xFF0A0D12).copy(alpha=.97f)))))
        Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow("A SAGA CONTINUA",WornIvory);RuneSeal(2,OldBronze,Modifier.size(26.dp))}
            BoxWithConstraints(Modifier.fillMaxWidth()){
                val portraitSize=(maxWidth*.42f).coerceAtMost(142.dp)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){
                        Text(p.name,color=WornIvory,style=MaterialTheme.typography.headlineLarge)
                        Text(p.title,color=Color(0xFFC4A36A),fontFamily=SagaFont,fontSize=13.sp,lineHeight=20.sp)
                        Text(classes[p.heroClass].uppercase(),color=Color(0xFFB5BECA),fontSize=10.sp,letterSpacing=1.8.sp)
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Text("${s.level}",color=WornIvory,fontFamily=SagaFont,fontSize=38.sp);Text("NÍVEL",color=Color(0xFFB5BECA),fontSize=10.sp)}
                    }
                    Avatar(p,s,if(minimal)portraitSize*.7f else portraitSize)
                }
            }
            Meter(xp.toFloat()/next,Color(0xFF9EB4D3),trackColor=Color(0xFF232934))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("$xp / $next XP",color=Color(0xFF9EB4D3),fontSize=12.sp);Text("Faltam ${next-xp} XP",color=Color(0xFFB5BECA),fontSize=11.sp)}
            if(!minimal){SagaDivider(color=OldBronze);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){attributes.forEachIndexed{i,name->Column(horizontalAlignment=Alignment.CenterHorizontally){Text("${1+s.points(i)/50}",color=WornIvory,fontFamily=SagaFont,fontSize=19.sp);Text(name.take(3).uppercase(),color=Color(0xFFB5BECA),fontSize=9.sp,letterSpacing=1.sp)}}}}
        }
    }
}
