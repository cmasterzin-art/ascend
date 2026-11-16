package br.com.ascend.aetheris

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*

@Composable fun ForestPainting(tile:Int,label:String,modifier:Modifier=Modifier){
 Crossfade(tile,modifier.fillMaxWidth(),animationSpec=tween(if(LocalAppearance.current.reducedMotion)0 else 400),label="névoa"){index->ArtTile(R.drawable.forest_atlas,index,3,Modifier.fillMaxWidth().aspectRatio(1.25f).clip(CutCornerShape(7.dp)).semantics{contentDescription=label})}
}
@Composable fun RegionScreen(index:Int,s:GameState,onLocation:(String)->Unit){
 LazyColumn(Modifier.fillMaxSize().testTag("exploration_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  item{Title(regions[index],"Nível ${regionLevels[index]} · ${if(s.level>=regionLevels[index])"Revelada" else "Sob a névoa"}")}
  if(index==1){
   item{ForestPainting(forestStage(s),"Floresta · estado ${forestStage(s)}");Text(regionDescriptions[1],color=Muted)}
   item{Eyebrow("Exploração · ${forestProgress(s)}%",Blue);Meter(forestProgress(s)/100f,Blue);Text("Sete marcos confirmados compõem a exploração: 1 na trilha, 3 no santuário e 3 na Vigília. Correções de registros podem rever um marco.",color=Muted,fontSize=12.sp)}
   items(forestExpeditions,key={it.id}){e->val available=expeditionAvailable(s,e);val complete=expeditionComplete(s,e)
    Panel(accent=if(complete)Green else ColdIron){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RegionStone(forestExpeditions.indexOf(e),available,complete);Column(Modifier.weight(1f)){Eyebrow(if(complete)"Descoberta confirmada" else if(available)"Vestígios conhecidos" else "Local velado",if(complete)Green else Blue);Text(if(available||complete)e.location else "Inscrição sob a névoa",style=MaterialTheme.typography.titleLarge)}}
     Text(if(available||complete)e.name else "Siga os marcos da aventura anterior para encontrar este lugar.",color=Muted)
     if(available||complete){Text(expeditionState(s,e),color=Blue);Action("Visitar ${e.location}",{onLocation(e.id)})}else Text("Requer a expedição anterior resgatada e nível 3.",color=Muted,fontSize=12.sp)
    }
   }
   item{Text(if(expeditionComplete(s,forestExpeditions.last()))"Conquista regional · Testemunha da Floresta. O relato da Vigília está no Salão das Memórias." else "As descobertas confirmadas serão inscritas no Salão das Memórias.",color=Muted)}
  }else{
   item{Image(painterResource(R.drawable.nordic_atlas),null,Modifier.fillMaxWidth().height(240.dp).clip(CutCornerShape(7.dp)),contentScale=ContentScale.Crop);Text(regionDescriptions[index],color=Muted)}
   item{Empty(if(s.level<regionLevels[index])"A região permanece velada até o nível ${regionLevels[index]}." else "Esta região está revelada. Ainda não possui expedições próprias nesta versão; suas crônicas e os recursos anteriores permanecem disponíveis no Mundo.")}
  }
 }
}
@Composable fun LocationScreen(e:ExpeditionDef,s:GameState,onExpedition:()->Unit){
 val index=forestExpeditions.indexOf(e);val available=expeditionAvailable(s,e);val known=s.quests.any{it.id==expeditionQuestId(e,0)}
 Page{Title(if(available||known)e.location else "Local velado","Floresta da Persistência")
 if(!available&&!known){Empty("A névoa guarda este lugar. Recupere o caminho anterior antes de entrar.");return@Page}
 ForestPainting(when(index){0->6;1->3+sanctuaryStage(s);else->7},when(index){1->"Santuário · ${listOf("abandonado","em recuperação","restaurado")[sanctuaryStage(s)]}";else->e.location})
 Text(e.premise,color=Ink,fontSize=16.sp,lineHeight=26.sp)
 if(index==2){Eyebrow("O Guardião da Névoa",Ember);Text("Três provações narrativas. Não há combate, mortes ou perdas por ausência.",color=Muted)}
 Eyebrow(expeditionState(s,e),Blue);Text("Objetivo · ${e.phases.sumOf{it.target}} conclusões distribuídas por ${e.phases.size} etapa(s). Você escolhe os hábitos ativos.",color=Muted)
 Action("Abrir expedição",onExpedition)
 if(expeditionComplete(s,e)){SagaDivider();Text(e.consequence,fontFamily=SagaFont,fontSize=18.sp,lineHeight=28.sp)}
 }
}
@Composable fun ExpeditionScreen(e:ExpeditionDef,s:GameState,vm:GameViewModel){
 var selected by remember(e.id){mutableStateOf(emptySet<String>())};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)}
 val phase=expeditionPhaseIndex(s,e);val q=s.quests.find{it.id==expeditionQuestId(e,phase)};val complete=expeditionComplete(s,e);val available=expeditionAvailable(s,e);val index=forestExpeditions.indexOf(e)
 LazyColumn(Modifier.fillMaxSize().testTag("exploration_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
 item{Title(e.name,"${e.location} · Floresta da Persistência")}
 item{ForestPainting(if(index==2)7 else if(index==1)3+sanctuaryStage(s) else 6,if(index==2)"O Guardião da Névoa" else e.location)}
 item{Text(e.premise,color=Muted);Eyebrow("${listOf("Breve","Prolongada","Três provações")[index]} · ${e.phases.sumOf{it.target}} conclusões",Blue);Text("Recompensa final · ${e.xp} XP · ◈ ${e.coins}${if(e.item.isNotBlank())" · Broche da Vigília" else ""}",color=Gold);Text("Marcos intermediários não concedem XP. O santuário e a trilha mudam ao confirmar as etapas. A recompensa própria do hábito continua única.",color=Muted,fontSize=12.sp)}
 if(complete)item{Panel(accent=Green){Eyebrow("Expedição resgatada",Green);Text(e.consequence,fontFamily=SagaFont,fontSize=18.sp);Text("Registro preservado nas Memórias. Nenhuma nova recompensa por reabrir.",color=Muted)}}
 else if(!available)item{Empty("Reveja os marcos anteriores ou alcance o nível 3. Uma correção de histórico pode ter alterado uma condição.")}
 else if(q==null){
  item{Eyebrow("Hábitos que podem contribuir");Text("Qualquer hábito ativo pode contribuir com uma conclusão real. Hábitos de tempo/quantidade contam ao atingir sua meta; nada é criado automaticamente.",color=Muted,fontSize=13.sp)}
  val active=s.habits.filter{!it.deleted&&!it.archived&&!it.paused}
  if(active.isEmpty())item{Empty("Nenhum hábito ativo. Escolha rituais no Grimório ou crie um hábito antes de partir.")}
  items(active,key={"select:${it.id}"}){h->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(h.id in selected,{selected=if(it)selected+h.id else selected-h.id},modifier=Modifier.semantics{contentDescription="Vincular ${h.name}"});Text(h.name,Modifier.weight(1f));Text("Meta ${h.target} ${h.unit}",color=Muted,fontSize=12.sp)}}
  item{error?.let{Text(it,color=MaterialTheme.colorScheme.error)};Action(if(busy)"Inscrevendo…" else "Iniciar expedição",{busy=true;vm.run{try{vm.repo.startExpedition(e.id,selected.toList())}catch(ex:IllegalArgumentException){error=ex.message}finally{busy=false}}},enabled=selected.isNotEmpty()&&!busy);Text("A fotografia do progresso atual fica fixada ao iniciar. Seleção de hábitos não muda depois do início.",color=Muted,fontSize=12.sp)}
 }else{
  item{Panel(accent=if(index==2)Ember else Blue){Eyebrow(if(index==2)"Fase ${phase+1} de 3 · ${e.phases[phase].name}" else "Etapa ${phase+1} de ${e.phases.size}",if(index==2)Ember else Blue);Text(e.phases[phase].name,style=MaterialTheme.typography.titleLarge);val progress=questProgress(q,s);Text("${progress.coerceAtMost(q.target.toLong())} / ${q.target} conclusões",color=Blue);Meter(progress.toFloat()/q.target,if(index==2)Ember else Blue);Text(e.phases[phase].text,color=Ink);Text("Contribuem: "+s.habits.filter{it.id in q.habitIds.split(',')}.joinToString{it.name},color=Muted,fontSize=12.sp);if(s.claims.any{it.questId==q.id}&&!validClaim(s.claims.first{it.questId==q.id},s))Text("Registro corrigido: recupere a condição para revalidar este mesmo marco, sem novo resgate.",color=Muted)
   Action(if(phase==e.phases.lastIndex)"Resgatar expedição" else "Confirmar marco",{vm.run{vm.repo.advanceExpedition(e.id)}},enabled=progress>=q.target)
  }}
 }
 items(e.phases.indices.toList(),key={"phase:$it"}){n->val reached=expeditionClaimValid(s,e,n);val revealed=n<=phase||complete
  Row(Modifier.fillMaxWidth().materialSurface(accent=if(reached)Green else ColdIron).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){
   RuneSeal(n,if(reached)Green else ColdIron,Modifier.size(32.dp));Column(Modifier.weight(1f)){Text(if(revealed)e.phases[n].name else "Marco velado",style=MaterialTheme.typography.titleMedium);Text(if(reached)e.phases[n].text else "${e.phases[n].target} conclusões após o início desta etapa.",color=Muted,fontSize=12.sp)}
  }
 }
 }
}
