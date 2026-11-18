package br.com.ascend.aetheris

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

/**
 * 1.6.9 — O Último Entalhe. One carved language for every symbol: a 24-unit grid, a single
 * chisel weight, a faint inlay and a cut shadow offset down-right (highlight in light theme).
 * Decorative only: the visible label always carries the meaning and semantics.
 */
private fun ring(cx:Float,cy:Float,r:Float)="M${cx-r} ${cy}a$r $r 0 1 0 ${2*r} 0a$r $r 0 1 0 ${-2*r} 0Z"
private class Cut(val kind:Char,val d:String,val turn:Float=0f)
private fun o(d:String,turn:Float=0f)=Cut('o',d,turn)
private fun l(d:String,turn:Float=0f)=Cut('l',d,turn)
private fun s(d:String,turn:Float=0f)=Cut('s',d,turn)

enum class Glyph(internal val cuts:List<Any>){
 Journey(listOf(o("M2 19L9 6L13 13L16 9L22 19Z"),l("M7 9.7L9 11.2L10.7 9.3"),l("M11.5 22Q9.5 19.5 12 18Q14.5 16.6 12.6 15"))),
 Habits(listOf(l(ring(12f,12.4f,9.2f)),o("M12 13C7.6 10 9 3.6 12 3.3C15 3.6 16.4 10 12 13Z"),o("M12 13C7.6 10 9 3.6 12 3.3C15 3.6 16.4 10 12 13Z",120f),o("M12 13C7.6 10 9 3.6 12 3.3C15 3.6 16.4 10 12 13Z",240f))),
 Missions(listOf(o("M5 3.5L17 3.5Q19 3.5 19 5.5L19 20.5L7 20.5Q5 20.5 5 18.5Z"),l("M8 3.5L8 20.5"),l("M8 18L19 18"),s("M13.5 6.6L15.4 8.5L13.5 10.4L11.6 8.5Z"),l("M16 11.5L21 11.5L21 14L16 14"))),
 World(listOf(l(ring(12f,12f,7.6f)),o("M12 1.8L13.6 10.4L22.2 12L13.6 13.6L12 22.2L10.4 13.6L1.8 12L10.4 10.4Z"),s(ring(12f,12f,1.3f)))),
 Hero(listOf(o("M4.5 12Q4.5 3 12 3Q19.5 3 19.5 12Z"),l("M12 3L12 12"),l("M4.5 12L4.5 15Q7 17.6 9.6 15.4L12 13.2L14.4 15.4Q17 17.6 19.5 15L19.5 12"),l("M12 13.2L12 19.6"),l("M6.4 16.8L6.4 21"),l("M17.6 16.8L17.6 21"))),
 Planner(listOf(o("M4 5L20 5L20 21L4 21Z"),l("M8 2.8L8 7"),l("M16 2.8L16 7"),l("M4 9.2L20 9.2"),s("M8 12.4l.9 .9l-.9 .9l-.9-.9Z"),s("M16 12.4l.9 .9l-.9 .9l-.9-.9Z"),s("M8 17.6l.9 .9l-.9 .9l-.9-.9Z"),s("M16 17.6l.9 .9l-.9 .9l-.9-.9Z"),s("M12 13.6L14 15.6L12 17.6L10 15.6Z"))),
 Analysis(listOf(o("M3.5 21L4 14.5L7.5 13.5L8 21Z"),o("M10 21L10.3 10L13.7 9L14 21Z"),o("M16 21L16.3 5.5L19.8 4L20.5 21Z"),l("M2 21L22 21"))),
 Focus(listOf(l("M5 3L19 3"),l("M5 21L19 21"),o("M7 3Q7 9 12 12Q17 9 17 3Z"),o("M7 21Q7 15 12 12Q17 15 17 21Z"),s("M9 21Q12 16.4 15 21Z"),l("M12 12.6L12 17"))),
 Inventory(listOf(o("M3.5 11L20.5 11L20.5 20.5L3.5 20.5Z"),o("M3.5 11Q3.5 5 12 5Q20.5 5 20.5 11Z"),l("M8 6L8 20.5"),l("M16 6L16 20.5"),s("M10.6 10.2L13.4 10.2L13.4 13.4L12 14.8L10.6 13.4Z"))),
 Shop(listOf(o("M8 8Q3 13 4.5 18Q6 21.5 12 21.5Q18 21.5 19.5 18Q21 13 16 8Z"),l("M7.5 8L16.5 8"),l("M9 8L7.6 4.4"),l("M12 8L12 3.4"),l("M15 8L16.4 4.4"),l(ring(12f,15.2f,2.7f)),s("M12 14l1.2 1.2l-1.2 1.2l-1.2-1.2Z"))),
 Achievements(listOf(o("M9.2 13.2L6.6 22L9 20.6L10.6 22L12 14.6Z"),o("M14.8 13.2L17.4 22L15 20.6L13.4 22L12 14.6Z"),o(ring(12f,8.8f,6.4f)),s("M12 5L13 7.8L15.8 8.8L13 9.8L12 12.6L11 9.8L8.2 8.8L11 7.8Z"))),
 Chronicles(listOf(o("M6 6.5L18 6.5L18 19.5L6 19.5Z"),o("M4 3L18 3Q20 3 20 5Q20 7 18 7L4 7Q2 7 2 5Q2 3 4 3Z"),o("M6 18L20 18Q22 18 22 19.8Q22 21.6 20 21.6L6 21.6"),l("M9 10.6L15 10.6"),l("M9 13.4L15 13.4"),l("M9 16.2L12.6 16.2"))),
 Sagas(listOf(o("M12 2.4L13.5 5L13.5 15.4L10.5 15.4L10.5 5Z"),l("M12 5.6L12 14.4"),l("M6.4 15.4Q12 17.8 17.6 15.4"),l("M12 16.6L12 19.8"),s(ring(12f,21.2f,1.4f)))),
 Settings(listOf(l(ring(12f,12f,8.2f)),o(ring(12f,12f,3.6f)),l("M12 3.8L12 8.4M12 15.6L12 20.2M3.8 12L8.4 12M15.6 12L20.2 12M6.2 6.2L9.5 9.5M14.5 14.5L17.8 17.8M17.8 6.2L14.5 9.5M9.5 14.5L6.2 17.8"),s("M11 1.2L13 1.2L12.6 3.4L11.4 3.4Z"),s("M11 1.2L13 1.2L12.6 3.4L11.4 3.4Z",90f),s("M11 1.2L13 1.2L12.6 3.4L11.4 3.4Z",180f),s("M11 1.2L13 1.2L12.6 3.4L11.4 3.4Z",270f))),
 Attributes(listOf(o("M12 2.6L21 9.2L17.6 20L6.4 20L3 9.2Z"),l("M12 2.6L12 13M21 9.2L12 13M17.6 20L12 13M6.4 20L12 13M3 9.2L12 13"),s(ring(12f,13f,1.5f)))),
 Skills(listOf(l("M12 21.5L12 8"),l("M12 14L7 8.6"),l("M12 11.4L17 6.4"),l("M12 17L16.4 13.4"),o(ring(12f,6f,1.9f)),o(ring(6.4f,7.6f,1.9f)),o(ring(17.6f,5.4f,1.9f)),o(ring(16.8f,12.8f,1.7f)))),
 History(listOf(l("M5 7.4A8.4 8.4 0 1 1 3.6 12.4"),s("M2.6 4L6.6 4.4L5.4 8.4Z"),l("M12 7L12 12.4L15.6 14.6"))),
 Village(listOf(o("M3 20L3 12L12 4.6L21 12L21 20Z"),l("M10 20L10 15L14 15L14 20"),l("M9.4 2.8L12 5.2L14.6 2.8"),l("M2 20L22 20"))),
 Map(listOf(o("M3 5.6L9 3.6L15 5.6L21 3.6L21 18.4L15 20.4L9 18.4L3 20.4Z"),l("M9 3.6L9 18.4"),l("M15 5.6L15 20.4"),l("M16.6 10L19 12.4M19 10L16.6 12.4"),l("M5 15Q7.5 12 10.5 13.5Q13 14.8 15.8 11.6"))),
 Routines(listOf(o("M12 6Q8 4 3 5L3 19Q8 18 12 20Q16 18 21 19L21 5Q16 4 12 6Z"),l("M12 6L12 20"),l("M5.6 9L9.6 9.6M5.6 12.4L9.6 13M14.4 9.6L18.4 9M14.4 13L18.4 12.4"))),
 Memories(listOf(o("M7 21L6.5 8Q7 3 12 3Q17 3 17.5 8L17 21Z"),l(ring(12f,9.6f,3f)),l("M12 12.6L12 18"),l("M4 21L20 21"))),
 Daily(listOf(l("M2 18.5L22 18.5"),o("M6.6 18.5A5.4 5.4 0 0 1 17.4 18.5Z"),l("M12 4.2L12 7.4M4.6 8.4L6.8 10.6M19.4 8.4L17.2 10.6M2.4 14L5.4 14.8M21.6 14L18.6 14.8"))),
 Weekly(listOf(o("M14 3C7 3.5 4 9 4 13C4 18 8.5 21.5 13.5 21C9 19 7.5 15.5 7.8 12C8 7.5 10.5 4.5 14 3Z"),s("M17.5 7l.8 1.8l1.8 .8l-1.8 .8l-.8 1.8l-.8-1.8l-1.8-.8l1.8-.8Z"))),
 Epic(listOf(l("M6 2.5L6 22"),o("M6 4L19 4L16 8.5L19 13L6 13Z"),s("M11.5 6.2L13.8 8.5L11.5 10.8L9.2 8.5Z"))),
 Boss(listOf(o("M7 9Q7 20 12 21Q17 20 17 9Q12 6 7 9Z"),l("M7 9Q3 7 3.6 2.6"),l("M17 9Q21 7 20.4 2.6"),l("M8.8 12.2L11 13.4M15.2 12.2L13 13.4"),l("M10.4 17.4L13.6 17.4"))),
 Claims(listOf(o(ring(12f,12f,8.6f)),l(ring(12f,12f,5.4f)),s("M12 8.6L13.2 10.8L15.4 12L13.2 13.2L12 15.4L10.8 13.2L8.6 12L10.8 10.8Z")));
}

private val labelGlyphs=mapOf("Perfil" to Glyph.Hero,"Atributos" to Glyph.Attributes,"Inventário" to Glyph.Inventory,"Loja" to Glyph.Shop,"Conquistas" to Glyph.Achievements,"Estatísticas" to Glyph.Analysis,"Habilidades" to Glyph.Skills,
 "Hábitos" to Glyph.Habits,"Calendário" to Glyph.Planner,"Rotinas" to Glyph.Routines,"Histórico" to Glyph.History,"Modelos de rotina" to Glyph.Routines,
 "Diárias" to Glyph.Daily,"Semanais" to Glyph.Weekly,"Épicas" to Glyph.Epic,"Chefes" to Glyph.Boss,"Resgates" to Glyph.Claims,"Sagas" to Glyph.Sagas,
 "Mapa" to Glyph.Map,"Vila" to Glyph.Village,"Crônicas" to Glyph.Chronicles,"Planejador" to Glyph.Planner,"Análises" to Glyph.Analysis,"Foco" to Glyph.Focus,"Mundo" to Glyph.World,"Jornada" to Glyph.Journey,"Missões" to Glyph.Missions,"Herói" to Glyph.Hero,
 "Caminhos" to Glyph.Map,"Entrada" to Glyph.Chronicles,"Pendências" to Glyph.Claims,"Dia" to Glyph.Daily,"Semana" to Glyph.Weekly,"Mês" to Glyph.Planner,
 "Ativos" to Glyph.Journey,"Todos" to Glyph.Missions,"Hoje" to Glyph.Daily,"Próximos dias" to Glyph.Planner,"Atrasadas" to Glyph.Boss,"Sem data" to Glyph.Routines,"Alta prioridade" to Glyph.Epic,"Concluídas" to Glyph.Achievements)
fun glyphForLabel(label:String):Glyph?=labelGlyphs[label]

@Composable fun CarvedGlyph(glyph:Glyph,color:Color,modifier:Modifier=Modifier,size:Dp=24.dp){
 val appearance=LocalAppearance.current
 val parts=remember(glyph){glyph.cuts.map{it as Cut}.map{Triple(it.kind,PathParser().parsePathString(it.d).toPath(),it.turn)}}
 val cut=if(appearance.dark)Color.Black.copy(alpha=.72f) else Color.White.copy(alpha=.75f)
 Canvas(modifier.size(size)){
  val unit=this.size.minDimension/24f
  fun pass(c:Color,inlay:Float,dx:Float,dy:Float)=withTransform({translate(dx,dy);scale(unit,unit,Offset.Zero)}){
   parts.forEach{(kind,path,turn)->rotate(turn,Offset(12f,12.4f)){
    val chisel=Stroke(1.55f,cap=StrokeCap.Round,join=StrokeJoin.Round)
    when(kind){'o'->{if(inlay>0f)drawPath(path,c.copy(alpha=c.alpha*inlay));drawPath(path,c,style=chisel)};'l'->drawPath(path,c,style=chisel);else->drawPath(path,c)}
   }}
  }
  if(!appearance.minimal)pass(cut,0f,.55f*unit,.75f*unit)
  pass(color,.17f,0f,0f)
 }
}

/** Samples one cell of a uniform sprite sheet; files are never cut at build time. */
@Composable fun AtlasCell(resource:Int,index:Int,columns:Int,rows:Int,modifier:Modifier=Modifier,alpha:Float=1f,filter:ColorFilter?=null){
 val image=ImageBitmap.imageResource(resource)
 Canvas(modifier){
  val cw=image.width/columns;val ch=image.height/rows
  drawImage(image,IntOffset((index%columns)*cw,(index/columns)*ch),IntSize(cw,ch),dstSize=IntSize(size.width.toInt().coerceAtLeast(1),size.height.toInt().coerceAtLeast(1)),alpha=alpha,colorFilter=filter,filterQuality=FilterQuality.High)
 }
}
private val ashen=ColorFilter.colorMatrix(ColorMatrix().apply{setToSaturation(0f)})

/** Five attribute medallions in worn silver; distinct motifs, readable without colour. */
@Composable fun AttributeEmblem(index:Int,modifier:Modifier=Modifier){
 Box(modifier.size(34.dp).semantics{contentDescription="Símbolo ${attributes[index.coerceIn(0,4)]}"}){
  if(LocalAppearance.current.minimal)CarvedGlyph(Glyph.Attributes,habitColor(index.coerceIn(0,4)),Modifier.matchParentSize())
  else AtlasCell(R.drawable.carved_emblems,5+index.coerceIn(0,4),5,2,Modifier.matchParentSize())
 }
}

enum class SealKind{Dormant,Mission,Oath,Saga,Reward,Expedition}
/** Ceremonial seals; an unearned seal is shown ashen rather than hidden. */
@Composable fun CeremonialSeal(kind:SealKind,lit:Boolean=true,modifier:Modifier=Modifier){
 Box(modifier.size(40.dp)){
  if(LocalAppearance.current.minimal)CarvedGlyph(if(kind==SealKind.Reward)Glyph.Achievements else Glyph.Claims,if(lit)Gold else Muted,Modifier.matchParentSize())
  else AtlasCell(R.drawable.carved_seals,kind.ordinal,3,2,Modifier.matchParentSize(),alpha=if(lit)1f else .62f,filter=if(lit)null else ashen)
 }
}

/** 1.6.9.19 — Nine-slice draw helper so forged corner rivets, Urnes end-caps and L-brackets never deform when scaled. */
fun DrawScope.drawNineSlice(image:ImageBitmap,capXRatio:Float=.18f,capYRatio:Float=.26f,alpha:Float=1f,colorFilter:ColorFilter?=null){
 val sw=image.width;val sh=image.height;val dw=size.width.toInt().coerceAtLeast(4);val dh=size.height.toInt().coerceAtLeast(4)
 val sx=(sw*capXRatio).toInt().coerceIn(1,sw/3);val sy=(sh*capYRatio).toInt().coerceIn(1,sh/3)
 val dy=(dh*capYRatio).toInt().coerceIn(1,dh/3);val dx=(dy*(sx.toFloat()/sy.coerceAtLeast(1))).toInt().coerceIn(1,dw/3)
 val sxCoords=intArrayOf(0,sx,sw-sx,sw);val syCoords=intArrayOf(0,sy,sh-sy,sh)
 val dxCoords=intArrayOf(0,dx,dw-dx,dw);val dyCoords=intArrayOf(0,dy,dh-dy,dh)
 for(r in 0..2)for(c in 0..2){
  val sW=(sxCoords[c+1]-sxCoords[c]).coerceAtLeast(1);val sH=(syCoords[r+1]-syCoords[r]).coerceAtLeast(1)
  val dW=(dxCoords[c+1]-dxCoords[c]).coerceAtLeast(1);val dH=(dyCoords[r+1]-dyCoords[r]).coerceAtLeast(1)
  drawImage(image,IntOffset(sxCoords[c],syCoords[r]),IntSize(sW,sH),IntOffset(dxCoords[c],dyCoords[r]),IntSize(dW,dH),alpha=alpha,colorFilter=colorFilter,filterQuality=FilterQuality.Medium)
 }
}

@Composable fun Modifier.pictorialFrame(resource:Int,capXRatio:Float=.18f,capYRatio:Float=.26f,alpha:Float=.85f,filter:ColorFilter?=null):Modifier{
 val minimal=LocalAppearance.current.minimal
 val img=if(minimal)null else ImageBitmap.imageResource(resource)
 return drawWithCache{
  onDrawBehind{if(img!=null)drawNineSlice(img,capXRatio,capYRatio,alpha,filter)}
 }
}

/** 1.6.9.19 — Forged plate behind buttons: uses 9-slice pictorial plates (ui_plate_primary, ui_plate_secondary, ui_plate_ceremonial). */
@Composable fun Modifier.plateMaterial(bronze:Boolean,enabled:Boolean=true,ceremonial:Boolean=false):Modifier{
 val appearance=LocalAppearance.current
 val plate=if(appearance.minimal)null else ImageBitmap.imageResource(if(ceremonial)R.drawable.ui_plate_ceremonial else if(bronze)R.drawable.ui_plate_primary else R.drawable.ui_plate_secondary)
 val rim=if(bronze)Color(0xFFE5C88F) else Color(0xFFB8C7D9)
 return drawWithCache{
  onDrawBehind{
   if(plate!=null)drawNineSlice(plate,capXRatio=if(ceremonial).14f else .15f,capYRatio=if(ceremonial).34f else .28f,alpha=if(enabled)(if(ceremonial).78f else .72f) else .28f)
   val d=1.dp.toPx();val w=size.width;val h=size.height
   drawRect(Brush.verticalGradient(listOf(Color.White.copy(alpha=if(enabled).08f else .02f),Color.Transparent,Color.Black.copy(alpha=.38f))))
   drawLine(rim.copy(alpha=if(enabled)(if(ceremonial).38f else .24f) else .07f),Offset(8*d,1.2f*d),Offset(w-8*d,1.2f*d),d)
   if(enabled){
    val tick=rim.copy(alpha=if(bronze).3f else .18f)
    drawLine(tick,Offset(3*d,6*d),Offset(8*d,2*d),d)
    drawLine(tick,Offset(w-8*d,h-2*d),Offset(w-3*d,h-6*d),d)
   }
   drawLine(Color.Black.copy(alpha=.65f),Offset(0f,h-d),Offset(w,h-d),2*d)
  }
 }
}

/** 1.6.9.19 — Carved niche frames around item paintings by rarity tier (0=Iron, 1=Silver, 2=Bronze, 3=Gold); centre is transparent. */
@Composable fun ArtifactFrame(modifier:Modifier=Modifier,rarity:Int=0){
 if(!LocalAppearance.current.minimal)AtlasCell(R.drawable.orn_frames_rarity,rarity.coerceIn(0,3),2,2,modifier)
}