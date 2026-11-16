package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import java.time.LocalDate

@Composable fun WeeklyScreen(s:GameState){
 val firstWeek=weekOf((listOfNotNull(s.player?.created)+s.habits.map{it.created}+s.records.map{it.date}+s.ledger.map{it.date}).minOrNull()?:today())
 var week by rememberSaveable{mutableStateOf(weekOf(today()))};val c=weeklyChronicle(s,week)
 Page{Title("Crônica da Semana","As marcas permanecem abertas a correções.")
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Anterior",{week=LocalDate.parse(week).minusWeeks(1).toString()},Modifier.weight(1f),enabled=week>firstWeek,secondary=true);SagaButton("Seguinte",{week=LocalDate.parse(week).plusWeeks(1).toString()},Modifier.weight(1f),enabled=week<weekOf(today()),secondary=true)}
 Manuscript{Eyebrow("${c.start} a ${c.end}",Sepia);RuneSeal(2,Sepia,Modifier.size(40.dp));Text(c.narrative,color=Sepia,fontSize=18.sp,lineHeight=28.sp)
 SagaDivider(color=Sepia)
 Text("${c.summary.completed} hábitos concluídos\n${c.summary.planned} programados · ${c.summary.rate?.let{"$it% de conclusão"}?:"sem metas previstas"}",color=Sepia)
 Text("${c.summary.xp} XP · ${c.coins} Fragmentos conquistados",color=Sepia,fontFamily=SagaFont,fontSize=20.sp)
 Text("O percentual considera apenas conclusões programadas; atividades extras continuam nos totais. Semana atual até hoje.",color=Sepia,fontSize=12.sp)
 Text("Atributos",color=Sepia,fontFamily=SagaFont,fontSize=20.sp);c.points.forEachIndexed{i,v->Text("${attributes[i]} · $v pontos",color=Sepia,fontSize=14.sp)}
 listOf("Missões resgatadas" to c.missions,"Sagas avançadas" to c.sagasAdvanced,"Sagas resgatadas" to c.sagasCompleted,"Conquistas" to c.achievements,"Reconstrução" to c.restored).forEach{(label,items)->SagaDivider(color=Sepia);Text(label,color=Sepia,fontFamily=SagaFont,fontSize=19.sp);Text(items.joinToString("\n").ifBlank{"Nenhum registro neste período."},color=Sepia,fontSize=14.sp)}
 }
 }
}
@Composable fun MemoryHall(s:GameState){
 var filter by rememberSaveable{mutableIntStateOf(0)};val categories=listOf("Todas","Conquistas","Sagas","Mundo","Personagem","História");val memories=hallMemories(s).filter{filter==0||it.category==categories[filter]}
 Page{Title("Salão das Memórias","${s.player?.name} · inscrições de uma jornada real")
 AetherisTabs(categories,filter,{filter=it})
 if(memories.isEmpty())Empty(if(filter==5)"As vozes ainda aguardam uma conversa. Memórias reconhecidas aparecerão aqui." else "Ainda não há inscrições nesta parte do salão. O lugar permanece reservado.")
 memories.forEach{m->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
 Column(horizontalAlignment=Alignment.CenterHorizontally){RuneSeal(m.symbol,Blue,Modifier.size(36.dp));Box(Modifier.width(1.dp).height(50.dp).background(ColdIron.copy(alpha=.4f)))}
 Column(Modifier.weight(1f).materialSurface(accent=ColdIron,leather=m.category=="História").padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Eyebrow(m.category,Blue);Text(m.title,style=MaterialTheme.typography.titleLarge);Text(m.date?.let{if(m.id.startsWith("memory:"))"Conversa guardada em $it" else it}?:"Data não disponível",color=Muted,fontSize=12.sp);Text(m.detail,color=Ink,fontSize=14.sp,lineHeight=23.sp)}
 }}
 }
}

@Composable fun PortraitScreen(s:GameState){
 Page{Title(s.player!!.name,"${classes[s.player.heroClass]} · ${s.player.title} · nível ${s.level}")
 ClassEmblem(s.player.heroClass,Modifier.size(44.dp))
 BoxWithConstraints(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Avatar(s.player,s,(maxWidth-24.dp).coerceAtMost(320.dp))}
 val frame=s.assets.firstOrNull{it.kind=="frame"&&it.equipped}?.let{a->shopItems.find{it.id==a.id}?.name}?:listOf("Simples","Solar","Estrelada")[s.player.frame]
 Text("Moldura · $frame",color=Muted);Text(classDescriptions[s.player.heroClass],color=Ink)
 }
}
