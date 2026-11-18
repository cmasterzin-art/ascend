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

@Composable fun ForestPainting(tile:Int,label:String,modifier:Modifier=Modifier,aspect:Float=1.25f){
 Crossfade(tile,modifier.fillMaxWidth(),animationSpec=tween(if(LocalAppearance.current.reducedMotion)0 else AetherTokens.scene),label="névoa"){index->ArtTile(R.drawable.forest_atlas,index,3,Modifier.fillMaxWidth().aspectRatio(aspect).clip(CutCornerShape(7.dp)).border(1.dp,ColdIron.copy(alpha=.3f),CutCornerShape(7.dp)).semantics{contentDescription=label})}
}
@Composable fun RegionScreen(index:Int,s:GameState,onLocation:(String)->Unit){
 LazyColumn(Modifier.fillMaxSize().testTag("exploration_list"),contentPadding=PaddingValues(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){
  item{Title(regions[index],"Nível ${regionLevels[index]} · ${if(s.level>=regionLevels[index])"Revelada" else "Sob a névoa"}")}
  if(index==1){
   item{ForestPainting(forestStage(s),"Floresta · estado ${forestStage(s)}");ReadingBlock(regionDescriptions[1])}
   item{val marks=forestExpeditions.sumOf{e->e.phases.indices.count{expeditionClaimValid(s,e,it)}}
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Column{Eyebrow("Exploração",Blue);Text("$marks de 7 marcos confirmados",color=Muted,fontSize=13.sp)};Text("${forestProgress(s)}%",fontFamily=SagaFont,fontSize=32.sp,color=Blue)};Meter(forestProgress(s)/100f,Blue,slim=true);Text("Trilha · Santuário · Vigília. Cada marco vem das suas atividades; correções podem rever o progresso.",color=Muted,fontSize=12.sp,lineHeight=20.sp);Hairline()}
   }
   items(forestExpeditions,key={it.id}){e->val available=expeditionAvailable(s,e);val complete=expeditionComplete(s,e)
    Panel(accent=if(complete)Green else ColdIron){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RegionStone(forestExpeditions.indexOf(e),available,complete);Column(Modifier.weight(1f)){Eyebrow(if(complete)"Descoberta confirmada" else if(available)"Vestígios conhecidos" else "Local velado",if(complete)Green else Muted);Text(if(available||complete)e.location else "Inscrição sob a névoa",style=MaterialTheme.typography.titleLarge)}}
     Text(if(available||complete)e.name else "Siga os marcos da aventura anterior para encontrar este lugar.",color=Muted,fontSize=14.sp,lineHeight=23.sp)
     if(available||complete){StatusSeal(expeditionState(s,e),if(complete)Green else Blue);Action("Visitar ${e.location}",{onLocation(e.id)})}else Text("Requer a expedição anterior resgatada e nível 3.",color=Muted,fontSize=12.sp)
    }
   }
   item{Text(if(expeditionComplete(s,forestExpeditions.last()))"Conquista regional · Testemunha da Floresta. O relato da Vigília está no Salão das Memórias." else "As descobertas confirmadas serão inscritas no Salão das Memórias.",color=Muted,fontSize=13.sp,lineHeight=23.sp)}
  }else{
   item{Image(painterResource(R.drawable.nordic_atlas),null,Modifier.fillMaxWidth().height(240.dp).clip(CutCornerShape(7.dp)),contentScale=ContentScale.Crop);ReadingBlock(regionDescriptions[index])}
   item{Empty(if(s.level<regionLevels[index])"A região permanece velada até o nível ${regionLevels[index]}." else "Esta região está revelada. Ainda não possui expedições próprias nesta versão; suas crônicas e os recursos anteriores permanecem disponíveis no Mundo.")}
  }
 }
}
@Composable fun LocationScreen(e:ExpeditionDef,s:GameState,onExpedition:()->Unit){
 val index=forestExpeditions.indexOf(e);val available=expeditionAvailable(s,e);val known=s.quests.any{it.id==expeditionQuestId(e,0)}
 Page{Title(if(available||known)e.location else "Local velado","Floresta da Persistência")
 if(!available&&!known){Empty("A névoa guarda este lugar. Recupere o caminho anterior antes de entrar.");return@Page}
 ForestPainting(when(index){0->6;1->3+sanctuaryStage(s);else->7},when(index){1->"Santuário · ${listOf("abandonado","em recuperação","restaurado")[sanctuaryStage(s)]}";else->e.location})
 StatusSeal(expeditionState(s,e),if(expeditionComplete(s,e))Green else Blue)
 ReadingBlock(e.premise,"Vestígios do lugar")
 if(index==2){Eyebrow("O Guardião da Névoa",MaterialTheme.colorScheme.error);Text("Três provações narrativas. Não há combate, mortes ou perdas por ausência.",color=Muted,fontSize=13.sp)}
 Column(Modifier.fillMaxWidth().background(Blue.copy(alpha=.06f),CutCornerShape(5.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Eyebrow("O próximo juramento",Blue);Text("${e.phases.sumOf{it.target}} conclusões · ${e.phases.size} ${if(e.phases.size==1)"etapa" else "etapas"}",color=Ink,fontSize=16.sp);Text("Você escolhe os hábitos ativos que vão contribuir.",color=Muted,fontSize=13.sp);Action("Abrir expedição",onExpedition)}
 if(expeditionComplete(s,e)){SagaDivider();ReadingBlock(e.consequence,"Uma marca preservada")}
 }
}
@Composable private fun ExpeditionObjective(e:ExpeditionDef,phase:Int,q:Quest,s:GameState,vm:GameViewModel){
 val color=if(e.id=="vigil")MaterialTheme.colorScheme.error else Blue
 Panel(accent=color){Eyebrow("Objetivo desta etapa",color);val progress=questProgress(q,s)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(e.phases[phase].name,style=MaterialTheme.typography.titleLarge);Text("${progress.coerceAtMost(q.target.toLong())} / ${q.target} conclusões",color=color,fontSize=14.sp)};CeremonialSeal(SealKind.Expedition,progress>=q.target,Modifier.size(38.dp))}
 Meter(progress.toFloat()/q.target,color,slim=true)
 ReadingBlock(e.phases[phase].text)
 Text("Contribuem: "+s.habits.filter{it.id in q.habitIds.split(',')}.joinToString{it.name},color=Muted,fontSize=12.sp,lineHeight=20.sp)
 if(s.claims.any{it.questId==q.id}&&!validClaim(s.claims.first{it.questId==q.id},s))Text("Registro corrigido: recupere a condição para revalidar este mesmo marco, sem novo resgate.",color=Muted,fontSize=12.sp)
 Action(if(phase==e.phases.lastIndex)"Resgatar expedição" else "Confirmar marco",{vm.run{vm.repo.advanceExpedition(e.id)}},Modifier.fillMaxWidth(),enabled=progress>=q.target)
 }
}
@Composable fun ExpeditionScreen(e:ExpeditionDef,s:GameState,vm:GameViewModel){
 var selected by remember(e.id){mutableStateOf(emptySet<String>())};var error by remember{mutableStateOf<String?>(null)};var busy by remember{mutableStateOf(false)}
 val phase=expeditionPhaseIndex(s,e);val q=s.quests.find{it.id==expeditionQuestId(e,phase)};val complete=expeditionComplete(s,e);val available=expeditionAvailable(s,e);val index=forestExpeditions.indexOf(e)
 LazyColumn(Modifier.fillMaxSize().testTag("exploration_list"),contentPadding=PaddingValues(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){
 item{Title(e.name,"${e.location} · Floresta da Persistência")}
 item{if(q!=null&&!complete)StatusSeal(if(index==2)"Fase ${phase+1} de 3 · ${e.phases[phase].name}" else "Etapa ${phase+1} de ${e.phases.size}",if(index==2)MaterialTheme.colorScheme.error else Blue) else StatusSeal(expeditionState(s,e),if(complete)Green else Blue)}
 item{ForestPainting(if(index==2)7 else if(index==1)3+sanctuaryStage(s) else 6,if(index==2)"O Guardião da Névoa" else e.location,aspect=if(index==2)1.25f else 1.6f)}
 if(complete)item{Panel(accent=Green){Eyebrow("Expedição resgatada",Green);ReadingBlock(e.consequence);Text("Registro preservado nas Memórias. Nenhuma nova recompensa por reabrir.",color=Muted,fontSize=12.sp,lineHeight=20.sp)}}
 else if(!available)item{Empty("Reveja os marcos anteriores ou alcance o nível 3. Uma correção de histórico pode ter alterado uma condição.")}
 else if(q!=null)item{ExpeditionObjective(e,phase,q,s,vm)}
 item{ReadingBlock(e.premise,"Vozes do caminho")}
 item{Spoils(e.xp,e.coins,if(e.item.isNotBlank())"Broche da Vigília" else "");Text("${listOf("Breve","Prolongada","Três provações")[index]} · ${e.phases.sumOf{it.target}} conclusões no total. Marcos intermediários não concedem XP.",Modifier.padding(top=10.dp),color=Muted,fontSize=12.sp,lineHeight=20.sp)}
 if(!complete&&available&&q==null){
  item{Eyebrow("Hábitos que podem contribuir");Text("Escolha os rituais que levará na jornada. Tempo e quantidade contam ao atingir a meta; nenhum hábito é criado automaticamente.",color=Muted,fontSize=14.sp,lineHeight=23.sp)}
  val active=s.habits.filter{!it.deleted&&!it.archived&&!it.paused}
  if(active.isEmpty())item{Empty("Nenhum hábito ativo. Escolha rituais no Grimório ou crie um hábito antes de partir.")}
  items(active,key={"select:${it.id}"}){h->Row(Modifier.fillMaxWidth().background(Blue.copy(alpha=if(h.id in selected).09f else .025f),CutCornerShape(5.dp)).padding(end=12.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(h.id in selected,{selected=if(it)selected+h.id else selected-h.id},modifier=Modifier.semantics{contentDescription="Vincular ${h.name}"});Column(Modifier.weight(1f)){Text(h.name,fontSize=15.sp);Text("Meta ${h.target} ${h.unit}",color=Muted,fontSize=12.sp)}}}
  item{error?.let{Text(it,color=MaterialTheme.colorScheme.error)};Action(if(busy)"Inscrevendo…" else "Iniciar expedição",{busy=true;vm.run{try{vm.repo.startExpedition(e.id,selected.toList())}catch(ex:IllegalArgumentException){error=ex.message}finally{busy=false}}},Modifier.fillMaxWidth(),enabled=selected.isNotEmpty()&&!busy);Text("${selected.size} ${if(selected.size==1)"hábito selecionado" else "hábitos selecionados"}. A seleção e a fotografia inicial ficam preservadas ao partir.",Modifier.padding(top=8.dp),color=Muted,fontSize=12.sp,lineHeight=20.sp)}
 }
 item{Eyebrow("Marcos da expedição",Muted)}
 items(e.phases.indices.toList(),key={"phase:$it"}){n->val reached=expeditionClaimValid(s,e,n);val revealed=n<=phase||complete
  Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)){
   Column(Modifier.width(34.dp).fillMaxHeight(),horizontalAlignment=Alignment.CenterHorizontally){
    CeremonialSeal(SealKind.Expedition,reached,Modifier.size(34.dp))
    if(n<e.phases.lastIndex)Box(Modifier.width(1.dp).weight(1f).padding(top=4.dp).background(if(reached)Green.copy(alpha=.55f) else ColdIron.copy(alpha=.35f)))
   }
   Column(Modifier.weight(1f).padding(bottom=8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Eyebrow(if(reached)"Marco confirmado" else if(n==phase)"Marco atual" else "Sob a névoa",if(reached)Green else Muted);Text(if(revealed)e.phases[n].name else "Marco velado",style=MaterialTheme.typography.titleMedium);Text(if(reached)e.phases[n].text else "${e.phases[n].target} conclusões após o início desta etapa.",color=Muted,fontSize=13.sp,lineHeight=22.sp)}
  }
 }
 }
}