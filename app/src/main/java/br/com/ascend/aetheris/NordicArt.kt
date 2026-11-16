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
                val background=assets.find{it.kind=="background"}?.id
                if(background=="scholar-bg"){
                    listOf(5f,79f).forEach{x->drawRect(Color(0xFF171719).copy(alpha=.9f),Offset(x,8f),Size(16f,55f));repeat(7){i->drawRect(Brush.horizontalGradient(listOf(OldBronze,Color(0xFF27313D))),Offset(x+1+i*2,12f),Size(1.4f,44f))};drawLine(OldBronze,Offset(x,58f),Offset(x+16f,58f),1f)}
                } else if(background!=null){drawCircle(Color(0xFFABC6DA).copy(alpha=.65f),7f,Offset(82f,16f));drawCircle(Color(0xFF14222E),6f,Offset(85f,14f));repeat(15){i->drawCircle(WornIvory.copy(alpha=.6f),.25f,Offset((i*13%90+5).toFloat(),(i*7%22+5).toFloat()))}}
                val outfit=assets.find{it.kind=="outfit"}?.id
                val cloth=when(outfit){"cloak"->Color(0xFF655268);"armor"->Color(0xFF596570);"dawn-outfit"->Color(0xFF506151);else->when(p.outfit){1->Color(0xFF495646);2->Color(0xFF7A5A3A);else->color.copy(alpha=.45f)}}
                // Outer shoulder panels leave the face, fur and clasp visible.
                val left=Path().apply{moveTo(0f,94f);quadraticTo(15f,85f,26f,93f);quadraticTo(17f,118f,20f,150f);lineTo(0f,150f);close()}
                val right=Path().apply{moveTo(100f,94f);quadraticTo(85f,85f,74f,93f);quadraticTo(83f,118f,80f,150f);lineTo(100f,150f);close()}
                val clothAlpha=if(outfit!=null||p.outfit>0).43f else .08f
                listOf(left,right).forEach{drawPath(it,Brush.horizontalGradient(listOf(cloth.copy(alpha=clothAlpha),Color.Transparent,cloth.copy(alpha=clothAlpha))));drawPath(it,color.copy(alpha=.3f),style=Stroke(.35f))}
                repeat(15){i->val y=96f+i*3.2f;drawLine(Color.Black.copy(alpha=.07f),Offset(1f,y),Offset(18f,y+4f),.4f);drawLine(WornIvory.copy(alpha=.12f),Offset(83f,y),Offset(99f,y+2f),.3f)}
                if(outfit=="armor")repeat(5){i->val y=99f+i*9;drawPath(poly(2f,y,21f,y-6,19f,y+1,2f,y+7),Brush.verticalGradient(listOf(ColdIron,Color(0xFF28323D)),startY=y,endY=y+6));drawCircle(WornIvory, .6f,Offset(6f,y))}
                val head=assets.find{it.kind=="head"}?.id
                if(head!=null){
                    val metal=Brush.horizontalGradient(listOf(Color(0xFF34373B),if(head=="will-helm")Color(0xFFC2A36A) else Color(0xFFA7B0B7),Color(0xFF42474F)),startX=31f,endX=69f)
                    val band=poly(31f,36f,34f,32f,50f,34f,66f,32f,69f,36f,66f,39f,50f,37f,34f,39f)
                    drawPath(band,metal);drawPath(band,Color(0xFFD8CCB0).copy(alpha=.6f),style=Stroke(.45f))
                    if(head=="will-helm")drawPath(poly(36f,34f,37f,27f,44f,32f,50f,24f,56f,32f,63f,27f,64f,34f),metal)
                    else {drawPath(poly(48.5f,36f,51.5f,36f,52f,56f,50f,58f,48f,56f),metal);drawLine(WornIvory,Offset(50f,37f),Offset(50f,55f),.4f)}
                    repeat(7){i->drawCircle(Color(0xFF28231F),.55f,Offset(35f+i*5,35.5f))}
                }
                val accessory=assets.find{it.kind=="accessory"}?.id
                if(accessory!=null){
                    drawPath(Path().apply{moveTo(36f,80f);quadraticTo(50f,115f,64f,80f)},OldBronze,style=Stroke(1.1f))
                    val gem=if(accessory=="ember-amulet")Color(0xFFD58C58) else Color(0xFF8BA2C7)
                    drawCircle(Brush.radialGradient(listOf(gem.copy(alpha=.45f),Color.Transparent),Offset(50f,101f),9f),9f,Offset(50f,101f))
                    drawPath(poly(50f,94f,55f,100f,50f,108f,45f,100f),OldBronze)
                    drawPath(poly(50f,96f,53f,100f,50f,105f,47f,100f),gem);drawLine(WornIvory,Offset(50f,96f),Offset(50f,104f),.5f)
                }
                if(assets.any{it.kind=="weapon"}){
                    rotate(-12f,Offset(15f,120f)){
                        drawPath(poly(13f,124f,12f,78f,15f,67f,18f,78f,17f,124f),Brush.horizontalGradient(listOf(ColdIron,Color(0xFFD2D9DC),Color(0xFF515D68)),startX=12f,endX=18f))
                        drawLine(WornIvory,Offset(15f,72f),Offset(15f,122f),.6f)
                        drawPath(poly(5f,123f,15f,121f,25f,123f,24f,126f,15f,125f,6f,126f),OldBronze)
                        drawLine(Color(0xFF755239),Offset(15f,125f),Offset(15f,139f),3.5f);repeat(5){i->drawLine(Color(0xFF322721),Offset(13f,127f+i*2),Offset(17f,128f+i*2),.65f)}
                        drawCircle(OldBronze,2.3f,Offset(15f,140f))
                    }
                }
                drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color(0xFF070A0F).copy(alpha=.5f)),startY=128f,endY=150f))
                drawRect(border.copy(alpha=.7f),Offset(1.5f,1.5f),Size(97f,147f),style=Stroke(.65f))
                drawRect(border.copy(alpha=.25f),Offset(3f,3f),Size(94f,144f),style=Stroke(.4f))
                repeat(if(p.frame>0||assets.any{it.kind=="frame"})12 else 4){i->val y=8f+i*11.6f;drawPath(poly(2f,y,5f,y+3,2f,y+6),border,style=Stroke(.6f));drawPath(poly(98f,y,95f,y+3,98f,y+6),border,style=Stroke(.6f))}
                drawRect(border.copy(alpha=flash.value))
            }
        }
        assets.find{it.kind=="companion"}?.let{companion->RelicArt("companion",if(companion.id=="lyra")Color(0xFFC6A77C) else if(companion.id=="corvus")Color(0xFF9BB0C6) else Color(0xFFC48B72),Modifier.align(Alignment.BottomEnd).padding(7.dp).size(size*.3f),companion.id)}
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
