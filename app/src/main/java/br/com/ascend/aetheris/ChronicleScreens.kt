package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import java.time.LocalDate

@Composable fun WeeklyScreen(s:GameState){
 val firstWeek=weekOf((listOfNotNull(s.player?.created)+s.habits.map{it.created}+s.records.map{it.date}+s.ledger.map{it.date}).minOrNull()?:today())
 var week by rememberSaveable{mutableStateOf(weekOf(today()))};val c=weeklyChronicle(s,week)
 Page{Title("Crônica da Semana","As marcas permanecem abertas a correções.")
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){SagaButton("Anterior",{week=LocalDate.parse(week).minusWeeks(1).toString()},Modifier.weight(1f),enabled=week>firstWeek,secondary=true);SagaButton("Seguinte",{week=LocalDate.parse(week).plusWeeks(1).toString()},Modifier.weight(1f),enabled=week<weekOf(today()),secondary=true)}
 Manuscript{
  Eyebrow(displayPeriod(c.start,c.end),Sepia)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(18.dp)){Box(Modifier.weight(1f)){ChronicleNumber(c.summary.completed.toString(),if(c.summary.completed==1)"ritual concluído" else "rituais concluídos")};Box(Modifier.weight(1f)){ChronicleNumber(c.summary.rate?.let{"$it%"}?:"—","conclusão prevista")}}
  Text("${c.summary.planned} ${if(c.summary.planned==1)"hábito programado" else "hábitos programados"} · ${if(c.start==weekOf(today()))"semana atual até hoje" else "semana encerrada"}",color=Sepia,fontSize=12.sp,lineHeight=20.sp)
  SagaDivider(color=Sepia)
  ReadingBlock(c.narrative,color=Sepia)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(18.dp)){Box(Modifier.weight(1f)){ChronicleNumber(c.summary.xp.toString(),"XP conquistado")};Box(Modifier.weight(1f)){ChronicleNumber(c.coins.toString(),"Fragmentos")}}
  Text("O percentual considera conclusões programadas. Atividades extras continuam nos totais.",color=Sepia,fontSize=12.sp,lineHeight=20.sp)
  SagaDivider(color=Sepia);Text("Atributos desenvolvidos",color=Sepia,fontFamily=SagaFont,fontSize=20.sp)
  c.points.forEachIndexed{i,v->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(attributes[i],color=Sepia,fontSize=14.sp);Text("$v ${if(v==1L)"ponto" else "pontos"}",color=Sepia,fontSize=14.sp)}}
  val groups=listOf("Contratos e expedições" to c.missions,"Sagas avançadas" to c.sagasAdvanced,"Sagas resgatadas" to c.sagasCompleted,"Conquistas" to c.achievements,"Reconstrução" to c.restored)
  SagaDivider(color=Sepia);Eyebrow("Acontecimentos registrados · ${groups.sumOf{it.second.size}}",Sepia)
  if(groups.all{it.second.isEmpty()})Text("Nenhum acontecimento registrado nesta semana. A página permanece aberta para os próximos caminhos.",color=Sepia,fontSize=14.sp,lineHeight=24.sp)
  groups.filter{it.second.isNotEmpty()}.forEach{(label,items)->Text(label,color=Sepia,fontFamily=SagaFont,fontSize=19.sp);items.forEach{Text("· $it",color=Sepia,fontSize=14.sp,lineHeight=23.sp)}}
 }
 }
}
@Composable fun MemoryHall(s:GameState){
 var filter by rememberSaveable{mutableIntStateOf(0)};val categories=listOf("Todas","Conquistas","Sagas","Caminhos","Mundo","Personagem","História");val memories=remember(s,filter){hallMemories(s).filter{filter==0||it.category==categories[filter]}}
 LazyColumn(Modifier.fillMaxSize().testTag("memory_list"),contentPadding=PaddingValues(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
  item{Title("Salão das Memórias","${s.player?.name} · inscrições de uma jornada real")}
  item{AetherisTabs(categories,filter,{filter=it})}
  item{Eyebrow("${memories.size} ${if(memories.size==1)"inscrição" else "inscrições"} · ${categories[filter]}",Muted)}
  if(memories.isEmpty())item{Empty(if(filter==categories.indexOf("História"))"As vozes ainda aguardam uma conversa. Memórias reconhecidas aparecerão aqui." else "Ainda não há inscrições nesta parte do salão. O lugar permanece reservado.")}
  items(memories,key={it.id}){m->
   val expedition=if(m.id.startsWith("claim:"))expeditionRef(m.id.removePrefix("claim:").substringBefore('@')) else null
   val important=expedition?.let{it.second==it.first.phases.lastIndex}==true||m.category=="Sagas"||m.category=="Caminhos"
   val accent=if(important)Gold else if(m.category=="História")Blue else ColdIron
   Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min),horizontalArrangement=Arrangement.spacedBy(12.dp)){
    Column(Modifier.width(26.dp).fillMaxHeight(),horizontalAlignment=Alignment.CenterHorizontally){
     CeremonialSeal(if(important)SealKind.Reward else SealKind.entries[m.symbol.coerceIn(0,4)],important,Modifier.size(if(important)28.dp else 22.dp))
     Box(Modifier.width(1.dp).weight(1f).padding(top=4.dp).background(if(important)Gold.copy(alpha=.55f) else ColdIron.copy(alpha=.35f)))
    }
    if(important){
     AetherSurface(SurfaceKind.Ceremonial,Modifier.weight(1f).border(1.dp,Gold.copy(alpha=.4f),androidx.compose.foundation.shape.CutCornerShape(6.dp))){
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow(m.category,Gold);Text(m.date?.let{displayDate(it)}?:"Data não disponível",color=Muted,fontSize=12.sp)}
      Text(m.title,color=Ink,style=MaterialTheme.typography.headlineMedium)
      if(m.id.startsWith("memory:")&&m.date!=null)Text("Conversa guardada em ${displayDate(m.date)}",color=Muted,fontSize=12.sp)
      NorthDivider()
      ReadingBlock(m.detail,color=Ink)
     }
    }else{
     Column(Modifier.weight(1f).padding(bottom=10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Eyebrow(m.category,accent);Text(m.date?.let{displayDate(it)}?:"Data não disponível",color=Muted,fontSize=12.sp)}
      Text(m.title,color=Ink,style=MaterialTheme.typography.titleLarge)
      if(m.id.startsWith("memory:")&&m.date!=null)Text("Conversa guardada em ${displayDate(m.date)}",color=Muted,fontSize=12.sp)
      ReadingBlock(m.detail,color=Muted)
      Hairline(Modifier.padding(top=6.dp))
     }
    }
   }
  }
 }
}
@Composable fun PortraitScreen(s:GameState){
 Page{Title(s.player!!.name,"${classes[s.player.heroClass]} · ${s.player.title} · nível ${s.level}")
 Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){ClassEmblem(s.player.heroClass,Modifier.size(38.dp))}
 BoxWithConstraints(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Avatar(s.player,s,(maxWidth-32.dp).coerceAtMost(300.dp))}
 val frame=s.assets.firstOrNull{it.kind=="frame"&&it.equipped}?.let{a->shopItems.find{it.id==a.id}?.name}?:listOf("Simples","Solar","Estrelada")[s.player.frame]
 StatusSeal("Moldura · $frame",Blue);ReadingBlock(classDescriptions[s.player.heroClass])
 }
}