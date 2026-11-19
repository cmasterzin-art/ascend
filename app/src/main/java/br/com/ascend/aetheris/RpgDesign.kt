package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import kotlin.math.*

val Parchment=Color(0xFFBDB196)
val Sepia=Color(0xFF29251E)
@Composable fun Manuscript(content:@Composable ColumnScope.()->Unit){AetherSurface(SurfaceKind.Parchment){content()}}
/** 1.6.9.10 · The codex binding: one slim leather spine with the saga seal, instead of a full ceremonial panel. */
@Composable fun SagaCover(count:Int){Row(Modifier.fillMaxWidth().clip(CutCornerShape(4.dp)).background(if(LocalAppearance.current.dark)Color(0xFF231B16) else Color(0xFFE1D5BF)).forgeMaterial(SurfaceKind.Ceremonial).padding(start=20.dp,end=14.dp,top=10.dp,bottom=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.size(44.dp),contentAlignment=Alignment.Center){Image(painterResource(R.drawable.forge_brooch),null,Modifier.matchParentSize());CeremonialSeal(SealKind.Saga,true,Modifier.size(24.dp))};Column(Modifier.weight(1f)){Text("Livro de missões",style=MaterialTheme.typography.titleLarge);Text("$count ${if(count==1)"recompensa resgatada" else "recompensas resgatadas"} · contratos e sagas",color=Muted,fontSize=12.sp)}}}
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

private val regionalRelicAtlasIndex = mapOf(
 "eternal-memory-medallion" to 0,
 "firmament-compass" to 1,
 "scribes-ring" to 2,
 "boreal-vigil-brooch" to 3
)

@Composable fun RelicArt(kind:String,color:Color,modifier:Modifier=Modifier,itemId:String?=null,dim:Boolean=false){
    val index=when(kind){"head"->0;"outfit"->1;"accessory"->2;"weapon"->3;"companion"->when(itemId){"lyra"->5;"ignis"->6;else->4};"frame"->7;else->8}
    val variant=when(itemId){"will-helm"->0;"armor"->1;"ember-amulet"->2;"dawn-outfit"->3;"rune-frame"->4;"night"->5;"lantern"->6;"craft-blade"->7;"traveler-helm"->8;else->null}
    val realmRelicCell = itemId?.let { regionalRelicAtlasIndex[it] }
    // 1.6.9.15 + 1.7.2: niches are lit from above like a stone alcove; regional relics sample realm_relics_atlas (2x2).
    Box(modifier.size(70.dp).clip(CutCornerShape(3.dp)).background(Brush.verticalGradient(listOf(Color(0xFF2A3036),Color(0xFF131618),Color(0xFF0A0C0D))))){
        Box(Modifier.matchParentSize().padding(3.dp).alpha(if(dim).66f else 1f)){
            when {
                realmRelicCell != null -> AtlasCell(R.drawable.realm_relics_atlas, realmRelicCell, 2, 2, Modifier.matchParentSize())
                itemId=="mist-clasp" -> ArtTile(R.drawable.forest_atlas,8,3,Modifier.matchParentSize())
                else -> ArtTile(if(variant!=null)R.drawable.relic_variants else R.drawable.relic_atlas,variant?:index,3,Modifier.matchParentSize())
            }
        }
        val tier=when(color){Gold->3;Violet->2;Blue,Green->1;else->0}
        ArtifactFrame(Modifier.matchParentSize(),rarity=tier)
        Canvas(Modifier.matchParentSize()){
            drawRect(Brush.radialGradient(listOf(Color(0xFFD5E1EE).copy(alpha=if(dim).18f else .2f),Color.Transparent),center=Offset(size.width/2,0f),radius=size.width*.92f))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent,Color.Black.copy(alpha=.28f))))
            if(!dim)drawRect(Brush.radialGradient(listOf(color.copy(alpha=.2f),Color.Transparent),center=Offset(size.width/2,size.height),radius=size.width*.7f))
            drawRect(color.copy(alpha=if(dim).36f else .5f),style=Stroke(1.dp.toPx()))
            drawLine(color.copy(alpha=if(dim).48f else .85f),Offset(5.dp.toPx(),size.height-1.dp.toPx()),Offset(size.width-5.dp.toPx(),size.height-1.dp.toPx()),1.5.dp.toPx())
        }
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

@Composable fun regionStateColor(state: RegionAtlasState): Color = when (state) {
 RegionAtlasState.Locked -> Color(0xFF8B949E)
 RegionAtlasState.Revealed -> Blue
 RegionAtlasState.Available -> Gold
 RegionAtlasState.InProgress -> Blue
 RegionAtlasState.Completed -> Green
 RegionAtlasState.NoInteractiveYet -> Violet
}

/**
 * 1.7.2 — Atlas de Aetheris 2.0: interactive Nordic cartography with bounded pinch-zoom (1.0x–2.4x),
 * clamped pan (zero blank void), accessible +/-/Centralizar controls, synchronized markers, and
 * 6 clearly differentiated region states. Passes single-finger vertical scrolls through when zoom == 1f.
 */
@Composable fun AetherisMap(s:GameState,selected:Int,onSelect:(Int)->Unit){
    val xs=listOf(.24f,.72f,.27f,.73f,.26f,.69f);val ys=listOf(.78f,.66f,.49f,.36f,.23f,.12f)
    val labels=listOf("Despertar","Bosque","Biblioteca","Montanhas","Santuário","Fortaleza")
    var zoom by remember{mutableFloatStateOf(1f)}
    var offset by remember{mutableStateOf(Offset.Zero)}
    var viewportSize by remember{mutableStateOf(IntSize.Zero)}

    fun clampOffset(candidate: Offset, targetZoom: Float): Offset {
        if (targetZoom <= 1.01f || viewportSize.width <= 0 || viewportSize.height <= 0) return Offset.Zero
        val maxX = ((targetZoom - 1f) * viewportSize.width) / 2f
        val maxY = ((targetZoom - 1f) * viewportSize.height) / 2f
        return Offset(candidate.x.coerceIn(-maxX, maxX), candidate.y.coerceIn(-maxY, maxY))
    }

    fun applyZoom(newZoom: Float, panDelta: Offset = Offset.Zero) {
        val clampedZoom = newZoom.coerceIn(1f, 2.4f)
        zoom = clampedZoom
        offset = clampOffset(offset + panDelta, clampedZoom)
    }

    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        // Accessible zoom & recenter controls (all >= 48dp touch targets)
        Row(
            Modifier.fillMaxWidth().testTag("atlas_controls"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RealmOrnament(5, size = 26.dp)
            Text(
                "Zoom ${(zoom * 100).toInt()}%",
                color = Muted,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            SagaButton(
                text = "−",
                onClick = { applyZoom(zoom - 0.35f) },
                enabled = zoom > 1.01f,
                style = VikingButtonStyle.CompactoRunico,
                modifier = Modifier.widthIn(min = 48.dp).semantics { contentDescription = "Reduzir mapa" }
            )
            SagaButton(
                text = "Centralizar",
                onClick = { zoom = 1f; offset = Offset.Zero },
                enabled = zoom > 1.01f || offset != Offset.Zero,
                style = VikingButtonStyle.CompactoRunico,
                modifier = Modifier.semantics { contentDescription = "Centralizar mapa" }
            )
            SagaButton(
                text = "+",
                onClick = { applyZoom(zoom + 0.35f) },
                enabled = zoom < 2.39f,
                style = VikingButtonStyle.CompactoRunico,
                modifier = Modifier.widthIn(min = 48.dp).semantics { contentDescription = "Ampliar mapa" }
            )
        }

        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(CutCornerShape(6.dp))
                .border(1.2.dp, OldBronze.copy(alpha = .85f), CutCornerShape(6.dp))
                .testTag("atlas_viewport")
                .onSizeChanged { viewportSize = it }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val pointers = event.changes.count { it.pressed }
                            if (pointers >= 2) {
                                val zChange = event.calculateZoom()
                                val panChange = event.calculatePan()
                                if (zChange != 1f || panChange != Offset.Zero) {
                                    applyZoom(zoom * zChange, panChange)
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } else if (zoom > 1.01f && pointers == 1) {
                                val panChange = event.calculatePan()
                                if (panChange != Offset.Zero) {
                                    offset = clampOffset(offset + panChange, zoom)
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            val mapWidth = maxWidth
            val mapHeight = maxHeight
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = zoom
                        scaleY = zoom
                        translationX = offset.x
                        translationY = offset.y
                    }
            ) {
                Image(painterResource(R.drawable.nordic_atlas), null, Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
                Canvas(Modifier.matchParentSize()) {
                    // Subtle carved cartographic trails linking adjacent regions
                    for (idx in 0 until regions.lastIndex) {
                        val a = Offset(size.width * xs[idx], size.height * ys[idx])
                        val b = Offset(size.width * xs[idx + 1], size.height * ys[idx + 1])
                        val openEdge = s.level >= regionLevels[idx + 1]
                        drawLine(
                            color = (if (openEdge) Color(0xFFD9B878) else Color(0xFF9AA39D)).copy(alpha = if (openEdge) .42f else .18f),
                            start = a,
                            end = b,
                            strokeWidth = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                        )
                    }
                    // Fog over locked regions; subtle warm halo over completed regions
                    regions.indices.forEach { i ->
                        val c = Offset(size.width * xs[i], size.height * ys[i])
                        val st = regionAtlasState(s, i)
                        if (st == RegionAtlasState.Locked) {
                            val r = size.width * .22f
                            drawCircle(Brush.radialGradient(listOf(Color(0xFFB7C3CC).copy(alpha = .22f), Color.Transparent), c, r), r, c)
                        } else if (st == RegionAtlasState.Completed) {
                            val r = size.width * .16f
                            drawCircle(Brush.radialGradient(listOf(Color(0xFFA8B69A).copy(alpha = .24f), Color.Transparent), c, r), r, c)
                        } else if (selected == i) {
                            val r = size.width * .17f
                            drawCircle(Brush.radialGradient(listOf(Color(0xFFC4A36A).copy(alpha = .26f), Color.Transparent), c, r), r, c)
                        }
                    }
                }
                regions.forEachIndexed { i, name ->
                    val open = s.level >= regionLevels[i]
                    val chosen = selected == i
                    val st = regionAtlasState(s, i)
                    val stColor = regionStateColor(st)
                    val prog = when (i) {
                        0 -> s.assets.filter { it.kind == "building" }.sumOf { it.stage } * 10
                        1, 2, 3 -> regionProgress(s, i)
                        else -> 0
                    }
                    Column(
                        Modifier
                            .offset(x = mapWidth * xs[i] - 46.dp, y = mapHeight * ys[i] - 30.dp)
                            .width(92.dp)
                            .heightIn(min = 58.dp)
                            .clickable(onClickLabel = "Explorar $name") { onSelect(i) }
                            .semantics(mergeDescendants = true) {
                                // Preserves exact legacy contentDescription contract for automated tests + TalkBack
                                contentDescription = "$name, ${if (open) "revelada" else "nível ${regionLevels[i]}"}"
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            RegionStone(i, open, chosen)
                            Box(
                                Modifier
                                    .background(Color(0xFF0A1018).copy(alpha = .88f), CircleShape)
                                    .border(.8.dp, stColor, CircleShape)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(st.symbol, color = stColor, fontSize = 8.sp, lineHeight = 10.sp)
                            }
                        }
                        if (i in 1..3 && prog > 0) {
                            Text(
                                "$prog% · ${if (i == 1) "trilha" else if (i == 2) "acervo" else "cumes"}",
                                color = WornIvory,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                        Text(
                            if (open) labels[i] else "Nível ${regionLevels[i]}",
                            Modifier
                                .background(Color(0xFF0A1019).copy(alpha = .82f), CutCornerShape(3.dp))
                                .border(if (chosen) 1.dp else .6.dp, if (chosen) Gold else stColor.copy(alpha = .65f), CutCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            color = if (open) WornIvory else Color(0xFFBDC7D3),
                            fontSize = 10.sp,
                            fontWeight = if (chosen) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}