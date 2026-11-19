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

@Composable fun RealmPainting(atlasRes:Int,tile:Int,label:String,modifier:Modifier=Modifier,aspect:Float=1.25f){
 Crossfade(tile,modifier.fillMaxWidth(),animationSpec=tween(if(LocalAppearance.current.reducedMotion)0 else AetherTokens.scene),label="névoa"){index->
  ArtTile(
   atlasRes,
   index,
   3,
   Modifier
    .fillMaxWidth()
    .aspectRatio(aspect)
    .clip(CutCornerShape(7.dp))
    .border(1.dp,ColdIron.copy(alpha=.35f),CutCornerShape(7.dp))
    .semantics{contentDescription=label}
  )
 }
}

@Composable fun ForestPainting(tile:Int,label:String,modifier:Modifier=Modifier,aspect:Float=1.25f){
 RealmPainting(R.drawable.forest_atlas,tile,label,modifier,aspect)
}

@Composable private fun RegionNpcCard(npc:RegionNpcDef,atlasRes:Int,s:GameState){
 val (baseLine,specFlavor)=npcDialogue(npc,s)
 AetherSurface(SurfaceKind.Ceremonial){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
   ArtTile(
    atlasRes,
    npc.portraitTile,
    3,
    Modifier
     .size(68.dp)
     .clip(CutCornerShape(5.dp))
     .border(1.dp,OldBronze.copy(alpha=.7f),CutCornerShape(5.dp))
     .semantics{contentDescription="${npc.name}, ${npc.role}"}
   )
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
    Eyebrow(npc.role,Gold)
    Text(npc.name,style=MaterialTheme.typography.titleLarge)
   }
  }
  Text("“$baseLine”",fontFamily=SagaFont,fontSize=15.sp,lineHeight=23.sp,color=Ink)
  if(specFlavor!=null){
   Hairline()
   Text(specFlavor,color=Blue,fontSize=12.sp,lineHeight=19.sp)
  }
 }
}

@Composable fun RegionScreen(index:Int,s:GameState,onLocation:(String)->Unit){
 val explReg=explorableRegion(index)
 LazyColumn(Modifier.fillMaxSize().testTag("exploration_list"),contentPadding=PaddingValues(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){
  item{Title(regions[index],"Nível ${regionLevels[index]} · ${if(s.level>=regionLevels[index])"Revelada" else "Sob a névoa"}")}
  if(explReg!=null){
   val stage=if(index==1) forestStage(s) else regionRestorationStage(s,index)
   val stageName=listOf("abandonada","em recuperação","restaurada")[stage.coerceIn(0,2)]
   val paintingLabel=if(index==1) "Floresta · estado $stage" else "${regions[index]} · $stageName"
   item{
    RealmPainting(explReg.atlasRes,stage,paintingLabel)
    ReadingBlock(regionDescriptions[index])
   }
   if(explReg.distinctionNote.isNotBlank()){
    item{
     Panel(accent=Blue){
      Eyebrow("Região do Atlas de Aetheris",Blue)
      Text(explReg.distinctionNote,color=Muted,fontSize=13.sp,lineHeight=20.sp)
     }
    }
   }
   explReg.npc?.let{npc->
    item{RegionNpcCard(npc,explReg.atlasRes,s)}
   }
   item{
    val marks=regionConfirmedMarks(s,index)
    val total=regionTotalMarks(index)
    val prog=regionProgress(s,index)
    val summaryTrail=when(index){
     1->"Trilha · Santuário · Vigília. Cada marco vem das suas atividades; correções podem rever o progresso."
     2->"Pórtico · Acervo · Câmara de Mímir. Progresso real via revisões na Biblioteca de Mímir, Caminhos ou hábitos."
     else->"Trilha de Ferro · Torre da Vigília · Observatório. Progresso real via Foco, Caminhos ou hábitos; dias de descanso nunca punem."
    }
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
     Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
      Column{
       Eyebrow("Exploração",Blue)
       Text("$marks de $total ${if(marks==1)"marco confirmado" else "marcos confirmados"}",color=Muted,fontSize=13.sp)
      }
      Text("$prog%",fontFamily=SagaFont,fontSize=32.sp,color=if(prog>=100) Green else Blue)
     }
     Meter(prog/100f,if(prog>=100) Green else Blue,slim=true)
     Text(summaryTrail,color=Muted,fontSize=12.sp,lineHeight=20.sp)
     Hairline()
    }
   }
   items(explReg.expeditions,key={it.id}){e->
    val available=expeditionAvailable(s,e)
    val complete=expeditionComplete(s,e)
    val expIndex=explReg.expeditions.indexOf(e)
    Panel(accent=if(complete) Green else ColdIron){
     Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
      if(index==1) RegionStone(expIndex,available,complete)
      else RealmOrnament(explReg.ornamentCell,size=38.dp)
      Column(Modifier.weight(1f)){
       Eyebrow(if(complete)"Descoberta confirmada" else if(available)"Vestígios conhecidos" else "Local velado",if(complete) Green else Muted)
       Text(if(available||complete) e.location else "Inscrição sob a névoa",style=MaterialTheme.typography.titleLarge)
      }
     }
     Text(if(available||complete) e.name else "Siga os marcos da aventura anterior para encontrar este lugar.",color=Muted,fontSize=14.sp,lineHeight=23.sp)
     if(available||complete){
      StatusSeal(expeditionState(s,e),if(complete) Green else Blue)
      Action("Visitar ${e.location}",{onLocation(e.id)})
     }else{
      Text("Requer a expedição anterior resgatada e nível ${explReg.unlockLevel}.",color=Muted,fontSize=12.sp)
     }
    }
   }
   item{
    val finished=expeditionComplete(s,explReg.expeditions.last())
    val footer=when(index){
     1->if(finished)"Conquista regional · Testemunha da Floresta. O relato da Vigília está no Salão das Memórias." else "As descobertas confirmadas serão inscritas no Salão das Memórias."
     2->if(finished)"Conquista regional · Biblioteca das Eras · Título desbloqueado: ${explReg.titleReward}. Relato inscrito no Salão das Memórias." else "Complete a Câmara de Mímir para desbloquear o título ${explReg.titleReward} e o Medalhão da Memória Eterna."
     else->if(finished)"Conquista regional · Montanhas da Disciplina · Título desbloqueado: ${explReg.titleReward}. Relato inscrito no Salão das Memórias." else "Complete o Observatório do Norte para desbloquear o título ${explReg.titleReward} e a Bússola do Firmamento."
    }
    Text(footer,color=if(finished) Gold else Muted,fontSize=13.sp,lineHeight=23.sp)
   }
  }else{
   item{Image(painterResource(R.drawable.nordic_atlas),null,Modifier.fillMaxWidth().height(240.dp).clip(CutCornerShape(7.dp)),contentScale=ContentScale.Crop);ReadingBlock(regionDescriptions[index])}
   item{Empty(if(s.level<regionLevels[index])"A região permanece velada até o nível ${regionLevels[index]}." else "Sem conteúdo interativo ainda. Esta região está revelada no Atlas; suas crônicas e os recursos anteriores permanecem disponíveis no Mundo.")}
  }
 }
}

private fun locationTileFor(e:ExpeditionDef,s:GameState):Int{
 if(e.regionIndex==1){
  val idx=forestExpeditions.indexOf(e)
  return when(idx){0->6;1->3+sanctuaryStage(s);else->7}
 }
 return e.locationTile
}

private fun locationLabelFor(e:ExpeditionDef,s:GameState):String{
 if(e.regionIndex==1 && e.id=="sanctuary"){
  return "Santuário · ${listOf("abandonado","em recuperação","restaurado")[sanctuaryStage(s)]}"
 }
 return e.location
}

@Composable fun LocationScreen(e:ExpeditionDef,s:GameState,onExpedition:()->Unit){
 val reg=expeditionRegion(e)
 val available=expeditionAvailable(s,e)
 val known=s.quests.any{it.id==expeditionQuestId(e,0)}
 Page{
  Title(if(available||known) e.location else "Local velado",regions[e.regionIndex])
  if(!available&&!known){Empty("A névoa guarda este lugar. Recupere o caminho anterior antes de entrar.");return@Page}
  RealmPainting(reg.atlasRes,locationTileFor(e,s),locationLabelFor(e,s))
  StatusSeal(expeditionState(s,e),if(expeditionComplete(s,e)) Green else Blue)
  ReadingBlock(e.premise,"Vestígios do lugar")
  if(e.guardianName.isNotBlank()){
   Eyebrow(e.guardianName,MaterialTheme.colorScheme.error)
   Text("Três provações narrativas. Não há combate, mortes ou perdas por ausência.",color=Muted,fontSize=13.sp)
  }
  Column(
   Modifier
    .fillMaxWidth()
    .background(Blue.copy(alpha=.06f),CutCornerShape(5.dp))
    .border(1.dp,Blue.copy(alpha=.28f),CutCornerShape(5.dp))
    .padding(16.dp),
   verticalArrangement=Arrangement.spacedBy(10.dp)
  ){
   Eyebrow("O próximo juramento",Blue)
   val objectiveSummary=if(e.integratedMetric!=EXPEDITION_METRIC_HABITS){
    "${e.phases.sumOf{it.altTarget}} ${expeditionMetricUnit(e.integratedMetric,e.phases.sumOf{it.altTarget})} (ou ${e.phases.sumOf{it.target}} conclusões de hábitos) · ${e.phases.size} ${if(e.phases.size==1)"etapa" else "etapas"}"
   }else{
    "${e.phases.sumOf{it.target}} conclusões · ${e.phases.size} ${if(e.phases.size==1)"etapa" else "etapas"}"
   }
   Text(objectiveSummary,color=Ink,fontSize=16.sp)
   Text(
    if(e.integratedMetric!=EXPEDITION_METRIC_HABITS) "Você pode avançar via ${e.integratedLabel} ou escolher hábitos ativos."
    else "Você escolhe os hábitos ativos que vão contribuir.",
    color=Muted,
    fontSize=13.sp
   )
   Action("Abrir expedição",onExpedition)
  }
  if(expeditionComplete(s,e)){SagaDivider();ReadingBlock(e.consequence,"Uma marca preservada")}
 }
}

@Composable private fun ExpeditionObjective(e:ExpeditionDef,phase:Int,q:Quest,s:GameState,vm:GameViewModel){
 val isGuardian=e.guardianName.isNotBlank()
 val color=if(isGuardian) MaterialTheme.colorScheme.error else Blue
 Panel(accent=color){
  Eyebrow("Objetivo desta etapa",color)
  val progress=questProgress(q,s)
  val unit=expeditionMetricUnit(q.metric,q.target)
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){
   Column(Modifier.weight(1f)){
    Text(e.phases[phase].name,style=MaterialTheme.typography.titleLarge)
    Text("${progress.coerceAtMost(q.target.toLong())} / ${q.target} $unit",color=color,fontSize=14.sp)
   }
   CeremonialSeal(SealKind.Expedition,progress>=q.target,Modifier.size(38.dp))
  }
  Meter(progress.toFloat()/q.target.coerceAtLeast(1),color,slim=true)
  ReadingBlock(e.phases[phase].text)
  val contribText=when(q.metric){
   EXPEDITION_METRIC_FOCUS_SESSIONS->"Contribuem: sessões de Foco registradas com pelo menos 5 min após o início desta etapa (sessões canceladas ou curtas não contam)."
   EXPEDITION_METRIC_FLASHCARD_DAYS->"Contribuem: dias distintos com revisões de flashcards na Biblioteca de Mímir após o início desta etapa."
   EXPEDITION_METRIC_PATH_TASKS->"Contribuem: tarefas concluídas em Caminhos após o início desta etapa (arquivar tarefas concluídas preserva o progresso)."
   else->"Contribuem: "+s.habits.filter{it.id in q.habitIds.split(',')}.joinToString{it.name}
  }
  Text(contribText,color=Muted,fontSize=12.sp,lineHeight=20.sp)
  if(s.claims.any{it.questId==q.id}&&!validClaim(s.claims.first{it.questId==q.id},s)){
   Text("Registro corrigido: recupere a condição para revalidar este mesmo marco, sem novo resgate.",color=Muted,fontSize=12.sp)
  }
  val isFinal=phase==e.phases.lastIndex
  Action(
   if(isFinal)"Resgatar expedição" else "Confirmar marco",
   {vm.run{vm.repo.advanceExpedition(e.id)}},
   Modifier.fillMaxWidth(),
   enabled=progress>=q.target,
   ceremonial=isFinal&&progress>=q.target
  )
 }
}

@Composable fun ExpeditionScreen(e:ExpeditionDef,s:GameState,vm:GameViewModel){
 val reg=expeditionRegion(e)
 val expIndex=reg.expeditions.indexOf(e)
 var selected by remember(e.id){mutableStateOf(emptySet<String>())}
 var modeIndex by remember(e.id){mutableIntStateOf(if(e.integratedMetric!=EXPEDITION_METRIC_HABITS) 0 else 1)}
 var error by remember{mutableStateOf<String?>(null)}
 var busy by remember{mutableStateOf(false)}
 val phase=expeditionPhaseIndex(s,e)
 val q=s.quests.find{it.id==expeditionQuestId(e,phase)}
 val complete=expeditionComplete(s,e)
 val available=expeditionAvailable(s,e)
 val isGuardian=e.guardianName.isNotBlank()
 val useIntegrated=e.integratedMetric!=EXPEDITION_METRIC_HABITS && modeIndex==0
 val relicItem=if(e.item.isNotBlank()) shopItems.find{it.id==e.item} else null
 LazyColumn(Modifier.fillMaxSize().testTag("exploration_list"),contentPadding=PaddingValues(horizontal=20.dp,vertical=24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){
  item{Title(e.name,"${e.location} · ${regions[e.regionIndex]}")}
  item{
   if(q!=null&&!complete){
    StatusSeal(
     if(isGuardian) "Fase ${phase+1} de ${e.phases.size} · ${e.phases[phase].name}" else "Etapa ${phase+1} de ${e.phases.size}",
     if(isGuardian) MaterialTheme.colorScheme.error else Blue
    )
   }else{
    StatusSeal(expeditionState(s,e),if(complete) Green else Blue)
   }
  }
  item{
   val artTile=if(isGuardian && e.guardianTile>=0) e.guardianTile else locationTileFor(e,s)
   val artLabel=if(isGuardian) e.guardianName else locationLabelFor(e,s)
   RealmPainting(reg.atlasRes,artTile,artLabel,aspect=if(isGuardian) 1.25f else 1.6f)
  }
  if(complete){
   item{
    Panel(accent=Green){
     Eyebrow("Expedição resgatada",Green)
     ReadingBlock(e.consequence)
     Text("Registro preservado nas Memórias. Nenhuma nova recompensa por reabrir.",color=Muted,fontSize=12.sp,lineHeight=20.sp)
    }
   }
  }else if(!available){
   item{Empty("Reveja os marcos anteriores ou alcance o nível ${reg.unlockLevel}. Uma correção de histórico pode ter alterado uma condição.")}
  }else if(q!=null){
   item{ExpeditionObjective(e,phase,q,s,vm)}
  }
  item{ReadingBlock(e.premise,"Vozes do caminho")}
  item{
   Spoils(e.xp,e.coins,relicItem?.name ?: "")
   if(relicItem!=null){
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
     RelicArt(relicItem.kind,rarityColor(rarity(relicItem)),itemId=relicItem.id)
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
      Eyebrow("Relíquia exclusiva",Gold)
      Text(relicItem.name,style=MaterialTheme.typography.titleMedium)
      Text(relicItem.description,color=Muted,fontSize=12.sp,lineHeight=18.sp)
     }
    }
   }
   val paceLabel=listOf("Breve","Prolongada","Três provações").getOrElse(expIndex){"Expedição"}
   val countText=if(e.integratedMetric!=EXPEDITION_METRIC_HABITS){
    "$paceLabel · ${e.phases.sumOf{it.altTarget}} ${expeditionMetricUnit(e.integratedMetric,e.phases.sumOf{it.altTarget})} ou ${e.phases.sumOf{it.target}} conclusões de hábitos. Marcos intermediários não concedem XP."
   }else{
    "$paceLabel · ${e.phases.sumOf{it.target}} conclusões no total. Marcos intermediários não concedem XP."
   }
   Text(countText,Modifier.padding(top=10.dp),color=Muted,fontSize=12.sp,lineHeight=20.sp)
  }
  if(!complete&&available&&q==null){
   if(e.integratedMetric!=EXPEDITION_METRIC_HABITS){
    item{
     Eyebrow("Modo de progresso real",Gold)
     Text("Escolha como esta expedição acompanhará seus passos reais. O modo integrado não exige hábitos vinculados.",color=Muted,fontSize=13.sp,lineHeight=20.sp)
     Spacer(Modifier.height(8.dp))
     ChipRow(listOf(e.integratedLabel,"Hábitos ativos"),modeIndex,{modeIndex=it;error=null})
    }
   }
   if(useIntegrated){
    item{
     Panel(accent=Blue){
      Eyebrow("Integração ativa · ${e.integratedLabel}",Blue)
      Text(
       when(e.integratedMetric){
        EXPEDITION_METRIC_FOCUS_SESSIONS->"Sessões de Foco registradas com duração mínima efetiva de 5 minutos a partir de agora avançam automaticamente cada marco desta expedição. Sessões canceladas ou abaixo de 5 min não contam."
        EXPEDITION_METRIC_FLASHCARD_DAYS->"Cada dia distinto com revisões de flashcards na Biblioteca de Mímir a partir de agora avança esta expedição (revisões repetidas no mesmo dia não duplicam o marco)."
        else->"Tarefas concluídas em Caminhos a partir de agora avançam cada marco desta expedição sem reutilizar tarefas já concluídas. Arquivar tarefas concluídas preserva o progresso."
       },
       color=Muted,fontSize=13.sp,lineHeight=20.sp
      )
      error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
      Action(
       if(busy)"Inscrevendo…" else "Iniciar expedição",
       {
        busy=true
        vm.run{
         try{vm.repo.startExpedition(e.id,emptyList(),e.integratedMetric)}
         catch(ex:IllegalArgumentException){error=ex.message}
         finally{busy=false}
        }
       },
       Modifier.fillMaxWidth(),
       enabled=!busy
      )
     }
    }
   }else{
    item{Eyebrow("Hábitos que podem contribuir");Text("Escolha os rituais que levará na jornada. Tempo e quantidade contam ao atingir a meta; nenhum hábito é criado automaticamente.",color=Muted,fontSize=14.sp,lineHeight=23.sp)}
    val active=s.habits.filter{!it.deleted&&!it.archived&&!it.paused}
    if(active.isEmpty())item{Empty("Nenhum hábito ativo. Escolha rituais no Grimório ou crie um hábito antes de partir.")}
    items(active,key={"select:${it.id}"}){h->
     val chosen=h.id in selected
     Row(
      Modifier
       .fillMaxWidth()
       .background(if(chosen) Gold.copy(alpha=.10f) else Surface.copy(alpha=.60f),CutCornerShape(5.dp))
       .border(1.dp,if(chosen) Gold.copy(alpha=.55f) else ColdIron.copy(alpha=.30f),CutCornerShape(5.dp))
       .padding(end=12.dp),
      verticalAlignment=Alignment.CenterVertically
     ){
      Checkbox(
       chosen,
       {selected=if(it) selected+h.id else selected-h.id},
       colors=CheckboxDefaults.colors(checkedColor=Gold,uncheckedColor=ColdIron,checkmarkColor=Night),
       modifier=Modifier.semantics{contentDescription="Vincular ${h.name}"}
      )
      Column(Modifier.weight(1f)){
       Text(h.name,fontSize=15.sp,color=if(chosen) Gold else Ink)
       Text("Meta ${h.target} ${h.unit}",color=Muted,fontSize=12.sp)
      }
     }
    }
    item{
     error?.let{Text(it,color=MaterialTheme.colorScheme.error)}
     Action(
      if(busy)"Inscrevendo…" else "Iniciar expedição",
      {
       busy=true
       vm.run{
        try{vm.repo.startExpedition(e.id,selected.toList(),EXPEDITION_METRIC_HABITS)}
        catch(ex:IllegalArgumentException){error=ex.message}
        finally{busy=false}
       }
      },
      Modifier.fillMaxWidth(),
      enabled=selected.isNotEmpty()&&!busy
     )
     Text("${selected.size} ${if(selected.size==1)"hábito selecionado" else "hábitos selecionados"}. A seleção e a fotografia inicial ficam preservadas ao partir.",Modifier.padding(top=8.dp),color=Muted,fontSize=12.sp,lineHeight=20.sp)
    }
   }
  }
  item{Eyebrow("Marcos da expedição",Muted)}
  items(e.phases.indices.toList(),key={"phase:$it"}){n->
   val reached=expeditionClaimValid(s,e,n)
   val revealed=n<=phase||complete
   val phaseMetric=q?.metric ?: if(useIntegrated) e.integratedMetric else EXPEDITION_METRIC_HABITS
   val phaseTarget=if(phaseMetric==EXPEDITION_METRIC_HABITS) e.phases[n].target else e.phases[n].altTarget
   val phaseUnit=expeditionMetricUnit(phaseMetric,phaseTarget)
   Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)){
    Column(Modifier.width(34.dp).fillMaxHeight(),horizontalAlignment=Alignment.CenterHorizontally){
     CeremonialSeal(SealKind.Expedition,reached,Modifier.size(34.dp))
     if(n<e.phases.lastIndex) Box(Modifier.width(1.dp).weight(1f).padding(top=4.dp).background(if(reached) Green.copy(alpha=.55f) else ColdIron.copy(alpha=.35f)))
    }
    Column(Modifier.weight(1f).padding(bottom=8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
     Eyebrow(if(reached)"Marco confirmado" else if(n==phase)"Marco atual" else "Sob a névoa",if(reached) Green else Muted)
     Text(if(revealed) e.phases[n].name else "Marco velado",style=MaterialTheme.typography.titleMedium)
     Text(if(reached) e.phases[n].text else "$phaseTarget $phaseUnit após o início desta etapa.",color=Muted,fontSize=13.sp,lineHeight=22.sp)
    }
   }
  }
 }
}