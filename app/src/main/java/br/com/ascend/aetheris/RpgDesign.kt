package br.com.ascend.aetheris

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

val Parchment=Color(0xFFBDB196)
val Sepia=Color(0xFF29251E)
@Composable fun Manuscript(content:@Composable ColumnScope.()->Unit){
    val minimal=LocalAppearance.current.minimal
    Box(Modifier.fillMaxWidth().clip(CutCornerShape(topStart=3.dp,topEnd=9.dp,bottomStart=7.dp,bottomEnd=2.dp)).background(Brush.linearGradient(listOf(Color(0xFF827766),Parchment,Color(0xFFB0A58B),Color(0xFF8D806A)))).border(1.dp,OldBronze,CutCornerShape(4.dp))){
        Canvas(Modifier.matchParentSize()){
            if(!minimal){
                repeat(500){i->val x=((i*137)%997)/997f*size.width;val y=((i*83)%991)/991f*size.height;drawLine(Sepia.copy(alpha=.07f),Offset(x,y),Offset(x+2+(i%7),y+.5f),.7f)}
                repeat(22){i->val x=(i*43%991)/991f*size.width;val y=size.height*i/21f;drawOval(Sepia.copy(alpha=.025f),Offset(x-30,y-10),Size(100f,35f))}
            }
            drawRect(Brush.horizontalGradient(listOf(Color.Black.copy(alpha=.23f),Color.Transparent),endX=22.dp.toPx()))
            drawRect(Sepia.copy(alpha=.2f),Offset(10.dp.toPx(),9.dp.toPx()),Size(size.width-20.dp.toPx(),size.height-18.dp.toPx()),style=Stroke(1f))
            repeat((size.height/15.dp.toPx()).toInt()){i->val y=16.dp.toPx()+i*15.dp.toPx();drawLine(Sepia.copy(alpha=.45f),Offset(6.dp.toPx(),y),Offset(9.dp.toPx(),y+3f),1f)}
            listOf(Offset(17.dp.toPx(),17.dp.toPx()),Offset(size.width-17.dp.toPx(),17.dp.toPx())).forEach{drawCircle(Color(0xFF4A4236),2.7.dp.toPx(),it);drawCircle(Color(0xFFAB9775),1.dp.toPx(),it-Offset(1f,1f))}
        }
        CompositionLocalProvider(LocalContentColor provides Sepia){Column(Modifier.padding(horizontal=24.dp,vertical=26.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)}
    }
}
@Composable fun SagaCover(count:Int){
    Column(Modifier.fillMaxWidth().materialSurface(accent=OldBronze,leather=true).padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
            RuneSeal(4,Gold,Modifier.size(62.dp))
            Column{Eyebrow("CÓDICE DE AETHERIS");Text("Livro de missões",style=MaterialTheme.typography.headlineMedium)}
        }
        SagaDivider()
        Text("Juramentos gravados.\nUma saga escrita em gestos.",fontFamily=SagaFont,fontSize=16.sp,lineHeight=25.sp,color=Ink)
        Text("$count recompensas resgatadas",color=Muted,fontSize=12.sp)
    }
}
@Composable fun rarityColor(n:Int)=listOf(Muted,Green,Blue,Violet,Gold)[n]
@Composable fun RelicArt(kind:String,color:Color,modifier:Modifier=Modifier,itemId:String?=null){
    Canvas(modifier.size(70.dp)){
        scale(size.width/100f,size.height/100f,Offset.Zero){
            fun poly(vararg xy:Float)=Path().apply{moveTo(xy[0],xy[1]);for(i in 2 until xy.size step 2)lineTo(xy[i],xy[i+1]);close()}
            val metal=Brush.horizontalGradient(listOf(Color(0xFF333C45),color,Color(0xFFD4D3C6),color,Color(0xFF252E38)),startX=18f,endX=82f)
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha=.16f),Color.Transparent),Offset(50f,50f),48f),48f,Offset(50f,50f))
            when(kind){
                "weapon"->{rotate(22f,Offset(50f,50f)){
                    drawPath(poly(47f,70f,46f,20f,50f,7f,54f,20f,53f,70f),metal);drawLine(WornIvory,Offset(50f,13f),Offset(50f,68f),1f)
                    drawPath(poly(30f,69f,50f,65f,70f,69f,67f,74f,50f,71f,33f,74f),OldBronze)
                    drawLine(Color(0xFF63452F),Offset(50f,72f),Offset(50f,91f),6f);repeat(6){i->drawLine(Color(0xFF211D18),Offset(47f,74f+i*2.5f),Offset(53f,76f+i*2.5f),1f)};drawCircle(OldBronze,4f,Offset(50f,93f))
                }}
                "head"->{
                    val helm=Path().apply{moveTo(23f,70f);lineTo(24f,42f);cubicTo(27f,10f,73f,10f,76f,42f);lineTo(77f,70f);lineTo(62f,80f);lineTo(58f,55f);lineTo(42f,55f);lineTo(38f,80f);close()}
                    drawPath(helm,metal);drawPath(helm,WornIvory.copy(alpha=.7f),style=Stroke(.8f))
                    drawPath(poly(29f,49f,46f,50f,43f,60f,30f,57f),Color(0xFF10161C));drawPath(poly(71f,49f,54f,50f,57f,60f,70f,57f),Color(0xFF10161C));drawPath(poly(47f,28f,53f,28f,54f,76f,50f,82f,46f,76f),metal)
                    drawLine(OldBronze,Offset(25f,43f),Offset(75f,43f),3f);repeat(7){i->drawCircle(WornIvory,.9f,Offset(29f+i*7,43f))}
                    if(itemId=="will-helm")drawPath(poly(29f,30f,28f,17f,41f,24f,50f,10f,59f,24f,72f,17f,71f,30f),OldBronze)
                }
                "outfit"->{
                    drawPath(poly(36f,16f,64f,16f,79f,33f,88f,88f,61f,81f,50f,91f,39f,81f,12f,88f,21f,33f),Brush.horizontalGradient(listOf(Color(0xFF23272C),color.copy(alpha=.75f),Color(0xFF151C23),color.copy(alpha=.65f))))
                    repeat(15){i->drawLine(Color(0xFF7B7B70),Offset(22f+i*4,28f),Offset(20f+i*4,40f+(i%3)*3),2.2f)}
                    drawPath(poly(39f,16f,50f,28f,61f,16f,57f,43f,43f,43f),Color(0xFF111820));drawLine(OldBronze,Offset(61f,40f),Offset(32f,84f),4f);drawCircle(OldBronze,3.5f,Offset(58f,44f));repeat(5){i->drawLine(Color.Black.copy(alpha=.3f),Offset(24f+i*13,48f),Offset(20f+i*14,80f),1.4f)}
                }
                "companion"->{if(itemId=="corvus"){
                    drawPath(poly(15f,49f,4f,26f,26f,35f,42f,49f,47f,26f,62f,20f,72f,31f,90f,39f,69f,42f,64f,68f,52f,79f,38f,65f,20f,74f,28f,57f),Brush.linearGradient(listOf(Color(0xFF151D26),color,Color(0xFF232A33))));drawCircle(WornIvory,2f,Offset(62f,31f));drawLine(OldBronze,Offset(45f,76f),Offset(43f,87f),2f);drawLine(OldBronze,Offset(57f,75f),Offset(60f,87f),2f)
                    }else{
                    drawPath(poly(22f,44f,18f,12f,39f,31f,61f,31f,82f,12f,78f,44f,85f,64f,62f,84f,50f,91f,38f,84f,15f,64f),Brush.horizontalGradient(listOf(Color(0xFF333338),color,Color(0xFF44474C))))
                    drawPath(poly(24f,23f,30f,43f,37f,37f),Color(0xFF332B29));drawPath(poly(76f,23f,70f,43f,63f,37f),Color(0xFF332B29));drawPath(poly(32f,62f,50f,70f,68f,62f,57f,83f,43f,83f),WornIvory.copy(alpha=.7f));drawPath(poly(44f,73f,56f,73f,50f,81f),Color(0xFF141A20));drawLine(Color(0xFF12171B),Offset(29f,50f),Offset(39f,54f),2f);drawLine(Color(0xFF12171B),Offset(71f,50f),Offset(61f,54f),2f)
                    if(itemId=="ignis"){drawPath(poly(19f,56f,3f,47f,10f,81f,30f,72f),Ember);drawPath(poly(81f,56f,97f,47f,90f,81f,70f,72f),Ember)}
                }}
                "background"->{repeat(5){i->val x=18f+i*13;drawRect(Brush.horizontalGradient(listOf(Color(0xFF342A23),color,Color(0xFF43332C)),startX=x,endX=x+10),Offset(x,22f+(i%2)*5),Size(10f,57f-(i%2)*5));repeat(3){j->drawLine(OldBronze,Offset(x,30f+j*16),Offset(x+10,30f+j*16),1f)}};drawLine(OldBronze,Offset(12f,81f),Offset(86f,81f),4f)}
                "frame"->{drawOval(metal,Offset(18f,10f),Size(64f,80f),style=Stroke(5f));drawOval(color.copy(alpha=.4f),Offset(24f,16f),Size(52f,68f),style=Stroke(1f));repeat(12){i->val a=i*PI/6;val x=50+cos(a).toFloat()*34;val y=50+sin(a).toFloat()*42;drawPath(poly(x,y-3,x+3,y,x,y+3,x-3,y),color)}}
                else->{
                    drawPath(Path().apply{moveTo(28f,12f);quadraticTo(50f,67f,72f,12f)},OldBronze,style=Stroke(2f))
                    drawPath(poly(50f,42f,69f,60f,50f,90f,31f,60f),metal);drawPath(poly(50f,49f,62f,61f,50f,81f,38f,61f),color);drawPath(poly(50f,49f,50f,81f,38f,61f),Color.White.copy(alpha=.25f));drawLine(WornIvory,Offset(50f,49f),Offset(62f,61f),1f)
                }
            }
        }
    }
}

@Composable fun AetherisMap(s:GameState,selected:Int,onSelect:(Int)->Unit){
    val xs=listOf(.24f,.72f,.27f,.73f,.26f,.69f);val ys=listOf(.78f,.66f,.49f,.36f,.23f,.12f)
    val labels=listOf("Despertar","Bosque","Arquivo","Montanhas","Lago","Cidadela")
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(2f/3f).clip(CutCornerShape(6.dp)).border(1.dp,OldBronze,CutCornerShape(6.dp))){
        val mapWidth=maxWidth;val mapHeight=maxHeight
        Image(painterResource(R.drawable.nordic_atlas),null,Modifier.matchParentSize(),contentScale=ContentScale.FillBounds)
        Canvas(Modifier.matchParentSize()){
            regions.indices.filter{s.level<regionLevels[it]}.forEach{i->val c=Offset(size.width*xs[i],size.height*ys[i]);val r=size.width*.23f;drawCircle(Brush.radialGradient(listOf(Color(0xFFB7C3CC).copy(alpha=.16f),Color.Transparent),c,r),r,c)}
        }
        regions.forEachIndexed{i,name->val open=s.level>=regionLevels[i];val chosen=selected==i
            Column(Modifier.offset(x=mapWidth*xs[i]-42.dp,y=mapHeight*ys[i]-28.dp).width(84.dp).heightIn(min=56.dp).clickable(onClickLabel="Explorar $name"){onSelect(i)}.semantics(mergeDescendants=true){contentDescription="$name, ${if(open)"revelada" else "nível ${regionLevels[i]}"}"},horizontalAlignment=Alignment.CenterHorizontally){
                Box(Modifier.size(if(chosen)34.dp else 28.dp).background(Color(0xFF111821).copy(alpha=.92f),CircleShape).border(if(chosen)2.dp else 1.dp,if(open)WornIvory else ColdIron,CircleShape),contentAlignment=Alignment.Center){Text(if(open)"◇" else "·",color=if(open)WornIvory else Color(0xFFBDC7D3),fontSize=20.sp)}
                Text(if(open)labels[i] else "Nível ${regionLevels[i]}",Modifier.background(Color(0xFF0A1019).copy(alpha=.91f),CutCornerShape(2.dp)).padding(horizontal=6.dp,vertical=3.dp),color=if(open)WornIvory else Color(0xFFBDC7D3),fontSize=10.sp)
            }
        }
    }
}
