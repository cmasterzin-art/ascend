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
import androidx.compose.ui.res.imageResource
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
/** A sprite atlas is sampled at draw time; original image files remain intact. */
@Composable fun ArtTile(resource:Int,index:Int,rows:Int,modifier:Modifier=Modifier){
    val art=ImageBitmap.imageResource(resource)
    Canvas(modifier){
        val cellWidth=art.width/3;val cellHeight=art.height/rows
        val aspect=size.width/size.height
        val sourceWidth=minOf(cellWidth,(cellHeight*aspect).toInt()).coerceAtLeast(1)
        val sourceHeight=minOf(cellHeight,(cellWidth/aspect).toInt()).coerceAtLeast(1)
        drawImage(art,srcOffset=IntOffset(index%3*cellWidth+(cellWidth-sourceWidth)/2,index/3*cellHeight+(cellHeight-sourceHeight)/2),srcSize=IntSize(sourceWidth,sourceHeight),dstSize=IntSize(size.width.toInt().coerceAtLeast(1),size.height.toInt().coerceAtLeast(1)),filterQuality=FilterQuality.Medium)
    }
}
@Composable fun RelicArt(kind:String,color:Color,modifier:Modifier=Modifier,itemId:String?=null){
    val index=when(kind){"head"->0;"outfit"->1;"accessory"->2;"weapon"->3;"companion"->when(itemId){"lyra"->5;"ignis"->6;else->4};"frame"->7;else->8}
    val variant=when(itemId){"will-helm"->0;"armor"->1;"ember-amulet"->2;"dawn-outfit"->3;"rune-frame"->4;"night"->5;"lantern"->6;"craft-blade"->7;"traveler-helm"->8;else->null}
    Box(modifier.size(70.dp).clip(CutCornerShape(3.dp))){
        if(itemId=="mist-clasp")ArtTile(R.drawable.forest_atlas,8,3,Modifier.matchParentSize()) else ArtTile(if(variant!=null)R.drawable.relic_variants else R.drawable.relic_atlas,variant?:index,3,Modifier.matchParentSize())
        Canvas(Modifier.matchParentSize()){drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color.Black.copy(alpha=.24f))));drawLine(color.copy(alpha=.3f),Offset(0f,size.height),Offset(size.width,size.height),1f)}
    }
}
@Composable fun BuildingScene(index:Int,stage:Int,modifier:Modifier=Modifier){
    ArtTile(if(index<3)R.drawable.village_crafts else R.drawable.village_civic,(if(index<3)index else index-3)*3+stage.coerceIn(0,2),if(index<3)3 else 2,modifier.fillMaxWidth().aspectRatio(if(index<3)1.5f else 1f).clip(CutCornerShape(5.dp)).semantics{contentDescription="${buildings[index]}: ${listOf("ruínas","em reconstrução","restaurada")[stage.coerceIn(0,2)]}"})
}
@Composable fun RegionStone(index:Int,open:Boolean,selected:Boolean){
    Canvas(Modifier.size(34.dp,42.dp)){
        val w=size.width;val h=size.height
        val stone=Path().apply{moveTo(w*.15f,h*.93f);lineTo(w*.2f,h*.22f);lineTo(w*.4f,h*.06f);lineTo(w*.74f,h*.12f);lineTo(w*.88f,h*.9f);close()}
        drawPath(stone,Brush.linearGradient(listOf(Color(0xFF222C32),Color(0xFF717770),Color(0xFF283238))))
        drawPath(stone,if(selected)WornIvory else ColdIron,style=Stroke(if(selected)1.5.dp.toPx() else .6.dp.toPx()))
        repeat(25){i->val x=w*(.24f+(i*17%51)/100f);val y=h*(.21f+(i*13%63)/100f);drawLine(Color.Black.copy(alpha=.18f),Offset(x,y),Offset(x+3,y+2),.7f)}
        val c=if(open)Color(0xFFD6BF88) else Color(0xFF9AA39D).copy(alpha=.7f)
        val x=w*.51f;val y=h*.52f
        drawLine(c,Offset(x,y-h*.2f),Offset(x,y+h*.2f),1.4.dp.toPx())
        when(index){
            0,5->{drawLine(c,Offset(x-w*.18f,y),Offset(x,y-h*.18f),1.3.dp.toPx());drawLine(c,Offset(x,y-h*.18f),Offset(x+w*.18f,y),1.3.dp.toPx());if(index==5)drawLine(c,Offset(x-w*.18f,y+h*.13f),Offset(x+w*.18f,y+h*.13f),1.3.dp.toPx())}
            1->{repeat(2){j->val yy=y+j*h*.13f;drawLine(c,Offset(x-w*.18f,yy),Offset(x,yy-h*.15f),1.3.dp.toPx());drawLine(c,Offset(x,yy-h*.15f),Offset(x+w*.18f,yy),1.3.dp.toPx())}}
            2->{drawLine(c,Offset(x-w*.16f,y-h*.15f),Offset(x-w*.16f,y+h*.15f),1.3.dp.toPx());drawLine(c,Offset(x+w*.16f,y-h*.15f),Offset(x+w*.16f,y+h*.15f),1.3.dp.toPx())}
            3->{drawLine(c,Offset(x-w*.2f,y+h*.16f),Offset(x,y-h*.16f),1.3.dp.toPx());drawLine(c,Offset(x,y-h*.16f),Offset(x+w*.2f,y+h*.16f),1.3.dp.toPx())}
            else->drawOval(c,Offset(x-w*.18f,y-h*.16f),Size(w*.36f,h*.32f),style=Stroke(1.3.dp.toPx()))
        }
        if(!open)drawLine(Color(0xFF202830),Offset(w*.15f,h*.68f),Offset(w*.86f,h*.38f),2.dp.toPx())
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
                RegionStone(i,open,chosen)
                if(i==1&&forestProgress(s)>0)Text("${forestProgress(s)}% · trilha",color=WornIvory,fontSize=9.sp)
                Text(if(open)labels[i] else "Nível ${regionLevels[i]}",Modifier.background(Color(0xFF0A1019).copy(alpha=.91f),CutCornerShape(2.dp)).padding(horizontal=6.dp,vertical=3.dp),color=if(open)WornIvory else Color(0xFFBDC7D3),fontSize=10.sp)
            }
        }
    }
}
