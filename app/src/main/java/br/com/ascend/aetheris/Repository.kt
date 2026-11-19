package br.com.ascend.aetheris

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.*
import androidx.room.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.*
import java.security.MessageDigest

val Context.preferences by preferencesDataStore("ascend_settings")
class AscendApp:Application(){
    val database by lazy { Room.databaseBuilder(this,GameDatabase::class.java,"ascend.db").addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10).build() }
    val repository by lazy { GameRepository(database){s->HabitWidget.refresh(this,s)} }
}
data class GameEvent(val message:String,val level:Int=0,val xp:Long=0,val coins:Long=0,val points:List<Long> = emptyList(),val highlights:List<String> = emptyList())
class GameRepository(private val db:GameDatabase,private val onChanged:(GameState)->Unit={}){
    private val d=db.dao(); private val lock=Mutex()
    val state=MutableStateFlow<GameState?>(null)
    val events=MutableSharedFlow<GameEvent>(extraBufferCapacity=10)
    private suspend fun read()=GameState(d.player(),d.habits(),d.records(),d.ledger(),d.quests(),d.claims(),d.assets(),d.achievements(),d.journal(),d.routines(),d.focus(),d.restDays(),d.plans(),d.focusSessions(),d.paths(),d.pathStages(),d.pathTasks(),d.pathSubtasks(),d.pathHabitLinks(),d.timeBlocks(),d.agendaEvents(),d.inboxItems(),d.notebooks(),d.knowledgePages(),d.knowledgeLinks(),d.knowledgeReferences(),d.flashcards(),d.flashcardReviews(),d.loadouts(),d.heraldry())
    suspend fun refresh()=lock.withLock { state.value=read() }
    private suspend fun change(message:String="Salvo",block:suspend ()->Unit){
        lock.withLock {
            val old=state.value?:read()
            db.withTransaction { block(); reconcile() }
            val next=read(); state.value=next
            runCatching{onChanged(next)}
            events.emit(rewardEvent(message,old,next))
        }
    }
    private suspend fun reconcile(){
        var s=read()
        // Claims are re-evaluated against habit records, never against their own rewards.
        s.claims.forEach { claim ->
            val q=s.quests.find{it.id==claim.questId}?:return@forEach
            val valid=validClaim(claim,s)
            d.reward(Reward("quest:${claim.id}",if(valid) claim.xp.toLong() else 0,if(valid) claim.coins.toLong() else 0,date=claim.date,label="Missão · ${q.name}"))
        }
        s=read()
        s.ledger.filter{it.id.startsWith("level:")}.forEach{r -> if(r.id.substringAfter(':').toInt()>s.level) d.reward(r.copy(coins=0)) }
        for(level in 2..s.level) { val prior=s.ledger.find{it.id=="level:$level"}; d.reward(Reward("level:$level",coins=10,date=prior?.date?:today(),label="Nível $level")) }
        s=read()
        achievementDefs.forEach{a ->
            if(achievementProgress(a,s)>=a.target && s.achievements.none{it.id==a.id}){
                d.unlock(Unlocked(a.id))
                if(a.coins>0L) d.reward(Reward("achievement:${a.id}",coins=a.coins,label="Conquista · ${a.name}"))
            }
        }
        // 1.7.3.1: Idempotent Legendary Collection completion trophies (0 XP, 0 economic multipliers)
        s=read()
        legendaryCollections.forEach{col ->
            val trophyId="collection:${col.id}"
            if(isCollectionComplete(s,col) && s.assets.none{it.id==trophyId}){
                d.asset(Asset(trophyId,"trophy",stage=1,equipped=false))
            }
        }
    }
    suspend fun create(p:Player,starters:List<Habit>)=change("Sua jornada começou"){
        require(p.name.trim().isNotEmpty()){ "Escolha um nome." }; d.player(p.copy(name=p.name.trim().take(40),specialization=""))
        starters.forEach{d.habit(it.copy(scheduleHistory="${it.created}|${it.days}|false"))}
        listOf(
            Quest("daily","Uma chama por dia","Conclua três hábitos hoje.",0,0,3,xp=30,coins=8),
            Quest("dailyxp","O ofício de continuar","Receba 100 XP de hábitos hoje.",0,2,100,xp=20,coins=5),
            Quest("weekly","Sete sóis sobre Aetheris","Conclua 15 hábitos nesta semana.",1,0,15,xp=120,coins=30),
            Quest("tome","O Tomo Perdido","Leia 500 páginas em hábitos com unidade páginas.",2,3,500,xp=250,coins=60),
            Quest("forge","A Forja da Vontade","Registre 30 horas em hábitos cronometrados de Intelecto.",2,1,1800,xp=400,coins=100,attribute=0),
            Quest("architect","O Arquiteto de Aetheris","Conclua 100 hábitos.",2,0,100,xp=300,coins=80),
            Quest("boss-study","O Devorador de Horas","Cada sessão completa de Intelecto causa 20 de dano. Cinco sessões vencem o desafio.",3,0,5,xp=100,coins=30,attribute=0),
            Quest("boss-chaos","O Guardião do Caos","A disciplina reconstrói o que a pressa dispersou.",3,0,5,xp=100,coins=30,attribute=2)
        ).forEach{d.quest(it)}
    }
    suspend fun player(p:Player)=change("Personagem atualizado"){
        require(p.name.isNotBlank() && p.heroClass in 0..4)
        val validSpec = if (p.specialization.isBlank()) "" else {
            val spec = specializationById(p.specialization)
            if (spec != null && spec.heroClass == p.heroClass) spec.id else ""
        }
        d.player(p.copy(specialization=validSpec))
    }
    suspend fun selectSpecialization(specId:String)=change(if(specId.isBlank()) "Especialização removida" else "Especialização escolhida"){
        val s=read()
        val p=requireNotNull(s.player){"Personagem não encontrado."}
        if(specId.isBlank()){
            d.player(p.copy(specialization=""))
        }else{
            val spec=requireNotNull(specializationById(specId)){"Especialização desconhecida."}
            require(canSelectSpecialization(s,spec)){"Alcance o nível ${spec.unlockLevel} na classe ${classes[spec.heroClass]} para escolher ${spec.name}."}
            d.player(p.copy(specialization=spec.id))
        }
    }
    suspend fun habit(h:Habit)=change("Hábito salvo"){
        validateHabitInput(h)
        val old=d.habits().find{it.id==h.id}
        val inactive=h.paused||h.archived||h.deleted
        val history=if(old==null) "${h.created}|${h.days}|$inactive" else if(old.days!=h.days || old.paused!=h.paused || old.archived!=h.archived || old.deleted!=h.deleted){
            val previous=old.scheduleHistory.ifBlank{"${old.created}|${old.days}|${old.paused||old.archived||old.deleted}"}
            (previous.split(';').filter{!it.startsWith(today()+"|")}+"${today()}|${h.days}|$inactive").joinToString(";")
        }else old.scheduleHistory
        d.habit(h.copy(scheduleHistory=history))
    }
    suspend fun addCatalogHabit(h:Habit)=change("Ritual adicionado à jornada"){
        require(habitTemplates.any{it.id==h.templateId}){"Modelo desconhecido."}
        validateHabitInput(h)
        val habits=d.habits()
        require(habits.none{it.id==h.id} && matchingHabit(habits,h)==null){"Este hábito já existe. Edite ou reative o existente em Hábitos."}
        d.habit(h.copy(position=(habits.maxOfOrNull{it.position}?:-1)+1,scheduleHistory="${h.created}|${h.days}|false",paused=false,archived=false,deleted=false))
    }
    suspend fun addRoutineTemplate(templateId:String,selected:List<Habit>)=change("Rotina guardada"){
        val template=routineTemplates.firstOrNull{it.id==templateId}?:error("Rotina desconhecida")
        require(selected.isNotEmpty() && selected.map{it.templateId}.distinct().size==selected.size){"Selecione ao menos um hábito, sem repetições."}
        selected.forEach{h->require(template.choices.any{it.templateId==h.templateId});validateHabitInput(h)}
        var habits=d.habits()
        val ids=selected.map{h->
            val existing=matchingHabit(habits,h)
            if(existing!=null)existing.id else {
                require(habits.none{it.id==h.id}){"Identificador de hábito já utilizado."}
                val saved=h.copy(position=(habits.maxOfOrNull{it.position}?:-1)+1,scheduleHistory="${h.created}|${h.days}|false",paused=false,archived=false,deleted=false)
                d.habit(saved);habits=habits+saved;saved.id
            }
        }
        val key="routine-template:${template.id}";val old=d.routines().find{it.id==key}
        d.routine(Routine(key,old?.name?:template.name,((old?.habitIds?.split(',')?:emptyList())+ids).distinct().joinToString(","),old?.time?:template.time))
    }
    suspend fun reorder(h:Habit,step:Int)=change("Ordem atualizada"){
        val hs=d.habits().filter{!it.deleted && !it.archived}.sortedBy{it.position}.toMutableList();val i=hs.indexOfFirst{it.id==h.id};val j=(i+step).coerceIn(0,(hs.size-1).coerceAtLeast(0))
        if(i>=0){ java.util.Collections.swap(hs,i,j); hs.forEachIndexed{n,v->d.habit(v.copy(position=n))} }
    }
    suspend fun progress(h:Habit,date:String,amount:Int,correction:HabitRecord?=null)=change(if(amount>=h.target) "A chama ganhou força" else "Progresso registrado"){
        LocalDate.parse(date)
        require(amount>=0){"Informe um progresso válido, igual ou maior que zero."}
        val savedHabit=requireNotNull(d.habits().find{it.id==h.id}){"Este hábito não está mais disponível. Abra novamente a Jornada."}
        require(date<=today() && date>=savedHabit.created){"Escolha uma data entre a criação do hábito e hoje."}
        val existing=d.records().find{it.habitId==h.id && it.date==date}
        require(existing!=null||!savedHabit.deleted){"Este hábito foi excluído. Consulte o histórico para corrigir registros existentes."}
        var r=existing?:HabitRecord("${savedHabit.id}@$date",savedHabit.id,date,0,savedHabit.target,savedHabit.xp,savedHabit.coins,savedHabit.points,savedHabit.attribute,savedHabit.proportional,savedHabit.type,savedHabit.category,savedHabit.unit,savedHabit.name,scheduled(savedHabit,date,d.restDays().map{it.date}.toSet(),d.plans()))
        if(correction!=null) {
            require(correction.id==r.id && correction.target>0 && correction.xp in 0..1000000 && correction.coins in 0..1000000 && correction.points in 0..1000000){"Correção inválida."}
            r=r.copy(xp=correction.xp,coins=correction.coins,points=correction.points)
        }
        r=r.copy(progress=amount.coerceIn(0,r.target)); d.record(r)
        d.reward(Reward("habit:${r.id}",Progression.reward(r.xp,r.progress,r.target,r.proportional),Progression.reward(r.coins,r.progress,r.target,r.proportional),Progression.reward(r.points,r.progress,r.target,r.proportional),r.attribute,date,"Hábito · ${r.name}"))
    }
    suspend fun claim(q:Quest)=change("Recompensa resgatada"){
        require(!isExpedition(q)){"Avance pela tela da Expedição."}
        val s=read();require(s.quests.any{it==q}){"Missão desatualizada. Abra novamente."};val occ=occurrence(q);val id="${q.id}@$occ"
        require(s.claims.none{it.id==id}){"Esta recompensa já foi resgatada."}
        require(q.sagaState !in listOf(1,3)){"Esta saga foi abandonada."}
        require(questProgress(q,s)>=q.target){"A missão ainda está em andamento."}
        d.claim(Claim(id,q.id,occ,xp=q.xp,coins=q.coins,target=q.target,metric=q.metric,habitIds=q.habitIds,period=q.period,attribute=q.attribute,deadline=q.deadline,sagaStart=q.sagaStart,sagaBaseline=q.sagaBaseline))
        if(q.special.isNotBlank()) d.asset(Asset("quest:$id","trophy"))
    }
    suspend fun quest(q:Quest)=change("Missão salva"){
        require(!q.id.startsWith("exp:")){"Identificador reservado à exploração."}
        require(q.name.isNotBlank() && q.target in 1..1000000 && q.xp in 0..1000000 && q.coins in 0..1000000){"Revise os valores da missão."}
        if(q.deadline.isNotEmpty()) LocalDate.parse(q.deadline)
        require(d.quests().none{it.id==q.id&&it.sagaStart.isNotBlank()}){"Use o editor de sagas."}
        require(q.sagaStart.isBlank()){"Use o editor de sagas."}
        d.quest(q)
    }
    suspend fun saga(q:Quest)=change("Juramento inscrito"){
        require(!q.id.startsWith("exp:")){"Use a tela de Expedições."}
        val s=read();val old=s.quests.find{it.id==q.id}
        require(old==null || old.sagaStart.isNotBlank())
        require(s.claims.none{it.questId==q.id}){"Uma saga resgatada preserva seus termos."}
        require(old==null || old.sagaState==0){"Esta saga está no histórico."}
        validateSaga(q,s)
        val ids=q.habitIds.split(',').toSet()
        val baseline=old?.sagaBaseline ?: s.records.filter{it.habitId in ids}.joinToString(";"){"${it.id}=${it.progress}"}
        if(old!=null)require(q.habitIds==old.habitIds && q.metric==old.metric){"Hábitos e métrica são fixados ao iniciar. Crie outro juramento para mudá-los."}
        d.quest(q.copy(period=2,attribute=-1,sagaStart=old?.sagaStart?:today(),sagaBaseline=baseline,sagaState=0,sagaEnd="",sagaStopped=0))
    }
    suspend fun abandonSaga(id:String)=change("Saga abandonada · seus hábitos permanecem"){
        val s=read();val q=requireNotNull(s.quests.find{it.id==id&&isPersonalSaga(it)})
        require(q.sagaState==0 && s.claims.none{it.questId==id}){"Somente sagas não resgatadas podem ser abandonadas."}
        d.quest(q.copy(sagaState=1,sagaEnd=today(),sagaStopped=questProgress(q,s)))
    }
    suspend fun archiveSaga(id:String)=change("Saga arquivada"){
        val s=read();val q=requireNotNull(s.quests.find{it.id==id&&isPersonalSaga(it)})
        require(q.sagaState in 0..1 && (q.sagaState==1||s.claims.any{it.questId==id})){"Conclua ou abandone a saga antes de arquivar."}
        d.quest(q.copy(sagaState=if(q.sagaState==1)3 else 2))
    }
    suspend fun restartSaga(id:String)=change("Nova tentativa iniciada"){
        val s=read();val q=requireNotNull(s.quests.find{it.id==id&&isPersonalSaga(it)})
        require(q.sagaState!=0 || s.claims.any{it.questId==id}){"Encerre a tentativa atual primeiro."}
        require(s.quests.none{it.name==q.name&&it.sagaStart.isNotBlank()&&it.sagaState==0&&s.claims.none{c->c.questId==it.id}}){"Já existe uma tentativa em curso."}
        val baseline=s.records.filter{it.habitId in q.habitIds.split(',')}.joinToString(";"){"${it.id}=${it.progress}"}
        d.quest(q.copy(id=uid(),sagaStart=today(),sagaBaseline=baseline,sagaState=0,sagaEnd="",sagaStopped=0))
    }
    suspend fun startExpedition(id:String,habitIds:List<String>,metric:Int=EXPEDITION_METRIC_HABITS)=change("Expedição iniciada"){
        val s=read();val e=expeditionById(id)?:error("Expedição desconhecida")
        require(expeditionAvailable(s,e)){"Revele a região e conclua a aventura anterior."}
        require(s.quests.none{it.id==expeditionQuestId(e,0)}){"Esta expedição já foi iniciada."}
        val useIntegrated=metric!=EXPEDITION_METRIC_HABITS
        require(!useIntegrated || metric==e.integratedMetric){"Métrica incompatível com esta expedição."}
        if(!useIntegrated){
            require(habitIds.isNotEmpty()&&habitIds.distinct().size==habitIds.size&&habitIds.all{hid->s.habits.any{it.id==hid&&!it.deleted&&!it.archived&&!it.paused}}){"Selecione hábitos ativos."}
        }
        d.quest(expeditionPhaseQuest(e,0,if(useIntegrated)emptyList() else habitIds,s,metric))
    }
    suspend fun advanceExpedition(id:String)=change("Marco regional reconhecido"){
        val s=read();val e=expeditionById(id)?:error("Expedição desconhecida")
        require(expeditionAvailable(s,e)){"Uma etapa anterior precisa ser confirmada novamente pelo progresso real."}
        require(!expeditionComplete(s,e)){"A recompensa já foi resgatada."}
        val phase=expeditionPhaseIndex(s,e);val q=requireNotNull(s.quests.find{it.id==expeditionQuestId(e,phase)})
        require(questProgress(q,s)>=q.target){"A etapa ainda está em andamento."}
        val claimId="${q.id}@once"
        if(s.claims.none{it.id==claimId})d.claim(Claim(claimId,q.id,"once",xp=q.xp,coins=q.coins,target=q.target,metric=q.metric,habitIds=q.habitIds,period=2,sagaStart=q.sagaStart,sagaBaseline=q.sagaBaseline,focusRuleVersion=q.focusRuleVersion))
        if(phase<e.phases.lastIndex){
            if(s.quests.none{it.id==expeditionQuestId(e,phase+1)}){
                val ids=q.habitIds.split(',').filter{it.isNotBlank()}
                d.quest(expeditionPhaseQuest(e,phase+1,ids,s,q.metric))
            }
        }
        else if(e.item.isNotBlank()){val item=shopItems.first{it.id==e.item};if(s.assets.none{it.id==item.id})d.asset(Asset(item.id,item.kind))}
    }
    suspend fun widgetComplete(habitId:String,date:String)=change("A chama ganhou força"){
        val s=read();require(date==today()){"O dia mudou. Atualize o widget."}
        val h=requireNotNull(s.habits.find{it.id==habitId}){"Este hábito não está mais disponível. Abra a Jornada."};require(h.type==0&&!h.paused&&!h.archived&&!h.deleted&&scheduled(h,date,s.restDates,s.plans)){"Registre este hábito no aplicativo."}
        val old=s.record(h.id,date)
        if(old?.complete!=true){val r=old?:HabitRecord("${h.id}@$date",h.id,date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name,true)
            val complete=r.copy(progress=r.target);d.record(complete);d.reward(Reward("habit:${r.id}",Progression.reward(r.xp,complete.progress,r.target,r.proportional),Progression.reward(r.coins,complete.progress,r.target,r.proportional),Progression.reward(r.points,complete.progress,r.target,r.proportional),r.attribute,date,"Hábito · ${r.name}"))}
    }
    suspend fun rememberNarrative(id:String)=change("Memória guardada"){
        require(narrativeCandidates(read()).any{it.id==id})
        val line=narrativeCandidates(read()).first{it.id==id}
        d.asset(Asset("memory:$id","memory",memorySpeaker=line.speaker,memoryText=line.text))
    }
    suspend fun unlockSkill(id:String)=change("Habilidade despertada"){
        val skill=skillDefs.firstOrNull{it.id==id}?:error("Habilidade desconhecida")
        val s=read()
        require(s.assets.none{it.id=="skill:$id"}){"Habilidade já desbloqueada."}
        require(canUnlockSkill(s,skill)){skillRequirementText(s,skill)}
        d.asset(Asset("skill:$id","skill"))
        if(skill.item.isNotBlank()){
            val item=shopItems.first{it.id==skill.item}
            d.asset(Asset(item.id,item.kind))
        }
    }
    suspend fun buy(item:ShopItem)=change("${item.name} chegou ao inventário"){
        require(shopItems.any{it==item} && item.id !in expeditionRelicIds && item.id !in forgeItemIds && skillDefs.none{it.item==item.id}){"Este item é obtido por exploração, forja ou habilidade."};val s=read();require(s.assets.none{it.id==item.id}){"Você já possui este item."};require(s.rawCoins>=item.cost){"Fragmentos insuficientes."}
        d.reward(Reward("buy:${item.id}",coins=-item.cost.toLong(),label="Compra · ${item.name}"));d.asset(Asset(item.id,item.kind))
    }
    suspend fun craftForgeItem(recipeId:String)=change("Peça forjada na oficina de Darian"){
        val recipe=requireNotNull(forgeRecipeById(recipeId)){"Receita de forja desconhecida."}
        val item=requireNotNull(shopItems.find{it.id==recipe.id && it.kind==recipe.kind}){"Item de forja inválido."}
        val s=read()
        require(s.assets.none{it.id==recipe.id}){"Você já forjou ${recipe.name}."}
        val missing=forgeRecipeMissingReasons(s,recipe)
        require(missing.isEmpty()){missing.first()}
        require(s.rawCoins>=recipe.cost){"São necessários ${recipe.cost} Fragmentos de Aether."}
        d.reward(Reward("forge:${recipe.id}",xp=0L,coins=-recipe.cost.toLong(),label="Forja de Darian · ${recipe.name}"))
        d.asset(Asset(item.id,item.kind,stage=FORGE_STAGE_FORGED))
    }
    suspend fun upgradeForgeItem(itemId:String,targetStage:Int)=change("Peça aprimorada na Forja de Darian"){
        val recipe=requireNotNull(forgeRecipeById(itemId)){"Apenas peças fabricadas na Forja podem ser aprimoradas."}
        val s=read()
        val missing=forgeUpgradeMissingReasons(s,recipe,targetStage)
        require(missing.isEmpty()){missing.first()}
        val cost=forgeUpgradeCost(recipe,targetStage)
        require(s.rawCoins>=cost){"São necessários $cost Fragmentos de Aether."}
        val existing=requireNotNull(s.assets.find{it.id==recipe.id})
        val stageLabel=forgeUpgradeStageDef(targetStage).name
        d.reward(Reward("forge-upgrade:${recipe.id}:$targetStage",xp=0L,coins=-cost.toLong(),label="Forja · ${recipe.name} ($stageLabel)"))
        d.asset(existing.copy(stage=targetStage))
    }
    suspend fun claimCollectionReward(collectionId:String)=change("Coleção lendária reconhecida"){
        val col=requireNotNull(legendaryCollectionById(collectionId)){"Coleção lendária desconhecida."}
        val s=read()
        require(isCollectionComplete(s,col)){"Reúna as quatro peças de ${col.name} antes de resgatar o selo."}
        val trophyId="collection:${col.id}"
        if(s.assets.none{it.id==trophyId}){
            d.asset(Asset(trophyId,"trophy",stage=1,equipped=false))
        }
    }
    suspend fun saveHeraldry(profile:HeraldryProfile)=change("Brasão heráldico salvo"){
        validateHeraldryProfile(profile.copy(id=1,updated=today()))
        val clean=profile.copy(id=1,inscriptionRunes=parseHeraldryInscription(profile.inscriptionRunes).joinToString(","){it.name},updated=today())
        val s=read()
        require(heraldrySymbolUnlocked(s,clean.symbolId)){"Símbolo '${heraldrySymbolById(clean.symbolId).name}' ainda está bloqueado."}
        require(heraldryMetalUnlocked(s,clean.metalId)){"Metal '${heraldryMetalById(clean.metalId).name}' ainda está bloqueado."}
        require(heraldryOrnamentUnlocked(s,clean.ornamentId)){"Ornamento '${heraldryOrnamentById(clean.ornamentId).name}' ainda está bloqueado."}
        require(heraldryBannerUnlocked(s,clean.bannerId)){"Estandarte '${heraldryBannerById(clean.bannerId).name}' ainda está bloqueado."}
        d.saveHeraldry(clean)
    }
    suspend fun applyStudioSelection(selectedByKind:Map<String,String?>)=change("Aparência atualizada no Estúdio"){
        val validKinds=equipmentSlots.map{it.first}.toSet()
        require(selectedByKind.keys.all{it in validKinds}){"Espaço de equipamento inválido."}
        val s=read()
        val ownedById=s.assets.associateBy{it.id}
        selectedByKind.forEach{(kind,itemId)->
            if(!itemId.isNullOrBlank()){
                val item=requireNotNull(shopItems.find{it.id==itemId && it.kind==kind}){"Item incompatível com o espaço $kind."}
                require(item.id in ownedById){"Você ainda não possui ${item.name}. Itens bloqueados servem apenas para prévia."}
            }
        }
        selectedByKind.forEach{(kind,itemId)->
            s.assets.filter{it.kind==kind}.forEach{d.asset(it.copy(equipped=false))}
            if(!itemId.isNullOrBlank()){
                val asset=requireNotNull(ownedById[itemId])
                d.asset(asset.copy(equipped=true))
            }
        }
    }
    suspend fun saveLoadout(loadout:EquipmentLoadout)=change("Conjunto salvo"){
        val clean=loadout.copy(name=loadout.name.trim(),updated=today())
        validateLoadoutInput(clean)
        val s=read()
        val owned=s.assets.map{it.id}.toSet()
        clean.slotMap().values.forEach{id->
            val name=shopItems.find{it.id==id}?.name?:id
            require(id in owned){"Você não possui $name para salvar neste conjunto."}
        }
        d.loadout(clean)
    }
    suspend fun saveCurrentAsLoadout(loadoutId:String,customName:String?=null)=change("Conjunto atualizado"){
        require(loadoutId in validLoadoutIds){"Conjunto inválido."}
        val s=read()
        val existing=resolvedLoadouts(s).first{it.id==loadoutId}
        val finalName=(customName?:existing.name).trim()
        val equipped=s.assets.filter{it.equipped}.associate{it.kind to it.id}
        val next=EquipmentLoadout(
            id=loadoutId,
            name=finalName,
            headId=equipped["head"]?:"",
            outfitId=equipped["outfit"]?:"",
            accessoryId=equipped["accessory"]?:"",
            weaponId=equipped["weapon"]?:"",
            companionId=equipped["companion"]?:"",
            frameId=equipped["frame"]?:"",
            backgroundId=equipped["background"]?:"",
            updated=today()
        )
        validateLoadoutInput(next)
        d.loadout(next)
    }
    suspend fun renameLoadout(loadoutId:String,newName:String)=change("Conjunto renomeado"){
        require(loadoutId in validLoadoutIds){"Conjunto inválido."}
        val s=read()
        val existing=resolvedLoadouts(s).first{it.id==loadoutId}
        val next=existing.copy(name=newName.trim(),updated=today())
        validateLoadoutInput(next)
        d.loadout(next)
    }
    suspend fun applyLoadout(loadoutId:String)=change("Conjunto equipado"){
        require(loadoutId in validLoadoutIds){"Conjunto inválido."}
        val s=read()
        val loadout=resolvedLoadouts(s).first{it.id==loadoutId}
        validateLoadoutInput(loadout)
        val ownedById=s.assets.associateBy{it.id}
        val missing=loadoutMissingItems(loadout,s)
        val slotMap=loadout.slotMap()
        if(missing.isEmpty()){
            // Full valid loadout: synchronize all 7 equipment slots atomically.
            equipmentSlots.map{it.first}.forEach{kind->
                s.assets.filter{it.kind==kind}.forEach{d.asset(it.copy(equipped=false))}
            }
            slotMap.forEach{(kind,itemId)->
                val asset=ownedById[itemId]
                if(asset!=null && asset.kind==kind) d.asset(asset.copy(equipped=true))
            }
        } else {
            // 1.7.3.1 (6.3): Partial loadout with missing items MUST NOT silently wipe unrelated valid equipped slots.
            slotMap.forEach{(kind,itemId)->
                val asset=ownedById[itemId]
                if(asset!=null && asset.kind==kind){
                    s.assets.filter{it.kind==kind}.forEach{d.asset(it.copy(equipped=false))}
                    d.asset(asset.copy(equipped=true))
                }
            }
        }
    }
    suspend fun clearLoadout(loadoutId:String)=change("Conjunto limpo"){
        require(loadoutId in validLoadoutIds){"Conjunto inválido."}
        val s=read()
        val existing=resolvedLoadouts(s).first{it.id==loadoutId}
        d.loadout(EquipmentLoadout(id=loadoutId,name=existing.name,updated=today()))
    }
    suspend fun equipItem(itemId:String)=change("Item equipado"){
        val s=read()
        val owned=requireNotNull(s.assets.find{it.id==itemId}){"Você não possui este item."}
        require(shopItems.any{it.id==owned.id && it.kind==owned.kind}){"Este item não é um equipamento."}
        s.assets.filter{it.kind==owned.kind}.forEach{d.asset(it.copy(equipped=it.id==owned.id))}
    }
    suspend fun unequipItem(itemId:String)=change("Item removido"){
        val s=read()
        val owned=requireNotNull(s.assets.find{it.id==itemId}){"Você não possui este item."}
        require(shopItems.any{it.id==owned.id && it.kind==owned.kind}){"Este item não é um equipamento."}
        if(owned.equipped) d.asset(owned.copy(equipped=false))
    }
    suspend fun setEquipment(slot:String,itemId:String?)=change(if(itemId.isNullOrBlank()) "Espaço liberado" else "Item equipado"){
        require(equipmentSlots.any{it.first==slot} || slot=="decoration"){"Espaço de equipamento inválido."}
        val s=read()
        if(itemId.isNullOrBlank()){
            s.assets.filter{it.kind==slot && it.equipped}.forEach{d.asset(it.copy(equipped=false))}
        }else{
            val item=requireNotNull(shopItems.find{it.id==itemId && it.kind==slot}){"Item incompatível com o espaço $slot."}
            val owned=requireNotNull(s.assets.find{it.id==item.id && it.kind==slot}){"Você não possui ${item.name}."}
            s.assets.filter{it.kind==slot}.forEach{d.asset(it.copy(equipped=it.id==owned.id))}
        }
    }
    suspend fun equip(a:Asset)=change(if(a.equipped) "Item removido" else "Item equipado"){
        val s=read();val owned=requireNotNull(s.assets.find{it.id==a.id})
        require(shopItems.any{it.id==owned.id && it.kind==owned.kind}){"Este item não é um equipamento."}
        s.assets.filter{it.kind==owned.kind}.forEach{d.asset(it.copy(equipped=false))};d.asset(owned.copy(equipped=!owned.equipped))
    }
    suspend fun restore(index:Int)=change("Entre as ruínas, a vida voltou a florescer"){
        require(index in buildings.indices);val s=read();val key="building:$index";val stage=s.assets.find{it.id==key}?.stage?:0
        require(stage<2){"Construção completamente restaurada."};val cost=(stage+1)*30;require(s.rawCoins>=cost){"São necessários $cost fragmentos."}
        d.reward(Reward("restore:$index:${stage+1}",coins=-cost.toLong(),label="Restauração · ${buildings[index]}"));d.asset(Asset(key,"building",stage+1))
    }
    suspend fun journal(j:Journal)=change("Página guardada"){require(j.text.isNotBlank()){ "Escreva algo antes de salvar."};d.journal(j.copy(title=j.title.ifBlank{"Sem título"}))}
    suspend fun deleteJournal(id:String)=change("Página excluída"){d.deleteJournal(id)}
    suspend fun routine(r:Routine)=change("Rotina salva"){require(runCatching{LocalTime.parse(r.time)}.isSuccess && r.name.isNotBlank() && r.habitIds.isNotBlank()){"Defina um nome e selecione hábitos."};d.routine(r)}
    suspend fun deleteRoutine(id:String)=change("Rotina excluída"){d.deleteRoutine(id)}
    suspend fun rest(date:String,enabled:Boolean)=change(if(enabled) "Descanso planejado" else "Programação retomada") {
        LocalDate.parse(date)
        require(date >= (d.player()?.created ?: today())) { "Escolha uma data a partir do início da sua jornada." }
        if(enabled) d.restDay(RestDay(date)) else d.deleteRestDay(date)
    }
    suspend fun plan(habitId:String,date:String,mode:Int)=change("Planejamento atualizado"){
        LocalDate.parse(date);require(date>=today()){"Ajustes pontuais são permitidos somente de hoje em diante."}
        require(mode in 0..2);val h=requireNotNull(d.habits().find{it.id==habitId})
        require(date>=h.created && !h.paused&&!h.archived&&!h.deleted){"Escolha um hábito ativo."}
        val id="$habitId@$date"
        if(mode==0)d.deletePlan(id) else d.plan(PlanException(id,habitId,date,mode==1))
    }
    suspend fun startFocus(h:Habit,minutes:Int)=change("Sessão iniciada"){
        val savedHabit=requireNotNull(d.habits().find{it.id==h.id}){"Este hábito não está mais disponível."}
        require(savedHabit.type==2 && !savedHabit.deleted && !savedHabit.archived && !savedHabit.paused && minutes in 1..1440){"Escolha um hábito cronometrado ativo e duração de 1 a 1440 minutos."}
        require(d.focus()==null){"Encerre a sessão atual antes de iniciar outra."}
        val date=today();val r=d.records().find{it.habitId==savedHabit.id && it.date==date}
        val now=System.currentTimeMillis();d.focus(Focus(habitId=savedHabit.id,date=date,duration=minutes,startedAt=now,baseline=r?.progress?:0,sessionId=uid(),beganAt=now,targetLabel=savedHabit.name))
    }
    suspend fun startPathFocus(pathId:String="",taskId:String="",minutes:Int)=change("Sessão de caminho iniciada"){
        require(minutes in 1..1440){"Escolha uma duração de 1 a 1440 minutos."}
        require(d.focus()==null){"Encerre a sessão atual antes de iniciar outra."}
        val task=if(taskId.isNotBlank()) requireNotNull(d.pathTasks().find{it.id==taskId}){"Tarefa não encontrada."} else null
        require(task == null || !task.archived && (pathId.isBlank() || pathId == task.pathId)) {
            "A tarefa foi arquivada ou mudou de caminho. Abra a tarefa novamente."
        }
        val resolvedPathId=task?.pathId ?: pathId
        val path=if(resolvedPathId.isNotBlank()) requireNotNull(d.paths().find{it.id==resolvedPathId}){"Caminho não encontrado."} else null
        require(path!=null || task!=null){"Selecione um caminho ou tarefa."}
        val label=task?.title ?: path?.name ?: ""
        val now=System.currentTimeMillis()
        d.focus(Focus(habitId="",date=today(),duration=minutes,startedAt=now,baseline=0,sessionId=uid(),beganAt=now,pathId=resolvedPathId,taskId=taskId,targetLabel=label))
    }
    suspend fun pauseFocus()=change("Foco atualizado"){
        val f=d.focus()?:return@change;d.focus(f.copy(elapsed=f.seconds(),startedAt=if(f.startedAt==0L) System.currentTimeMillis() else 0))
    }
    suspend fun endFocus(register:Boolean)=change(if(register) "Tempo efetivo registrado" else "Sessão descartada"){
        val f=d.focus()?:return@change
        val h=if(f.habitId.isNotBlank()) d.habits().find{it.id==f.habitId} else null
        if(register && f.habitId.isNotBlank()){
            val habit=h?:error("Hábito não encontrado")
            val existing=d.records().find{it.habitId==f.habitId && it.date==f.date}
            val r=existing?:HabitRecord("${habit.id}@${f.date}",habit.id,f.date,0,habit.target,habit.xp,habit.coins,habit.points,habit.attribute,habit.proportional,habit.type,habit.category,habit.unit,habit.name,scheduled(habit,f.date,d.restDays().map{it.date}.toSet(),d.plans()))
            // Absolute baseline makes overlapping manual progress idempotent.
            val next=maxOf(r.progress,f.baseline+(f.seconds()/60).toInt()).coerceAtMost(r.target)
            d.record(r.copy(progress=next))
            d.reward(Reward("habit:${r.id}",Progression.reward(r.xp,next,r.target,r.proportional),Progression.reward(r.coins,next,r.target,r.proportional),Progression.reward(r.points,next,r.target,r.proportional),r.attribute,r.date,"Hábito · ${r.name}"))
        }
        if(f.sessionId.isNotBlank()){
            val now=System.currentTimeMillis().coerceAtLeast(f.beganAt);val seconds=f.seconds(now)
            val label=h?.name ?: f.targetLabel.ifBlank{ d.pathTasks().find{it.id==f.taskId}?.title ?: d.paths().find{it.id==f.pathId}?.name ?: "Sessão" }
            d.focusSession(FocusSession(f.sessionId,f.habitId,h?.name?:"",f.date,f.beganAt,now,seconds,f.duration,if(!register)"cancelled" else if(seconds>=f.duration*60L)"completed" else "interrupted",register,f.pathId,f.taskId,label))
        }
        d.clearFocus()
    }
    suspend fun savePath(p: PersonalPath, confirmedOpenTasks: Set<String> = emptySet()) = change("Caminho salvo") {
        val clean = p.copy(name = p.name.trim(), category = p.category.trim().ifBlank { "Pessoal" })
        persistPath(clean, d.paths().find { it.id == p.id }, confirmedOpenTasks)
    }

    suspend fun setPathState(pathId: String, newState: Int, confirmedOpenTasks: Set<String> = emptySet()) =
        change(if (newState == 3) "Caminho concluído" else "Estado do caminho atualizado") {
            val saved = requireNotNull(d.paths().find { it.id == pathId }) { "Caminho não encontrado." }
            persistPath(saved.copy(state = newState), saved, confirmedOpenTasks)
        }

    private suspend fun persistPath(path: PersonalPath, previous: PersonalPath?, confirmedOpenTasks: Set<String>) {
        if (path.state == 3 && previous?.state != 3) {
            val current = d.pathTasks().filter { it.pathId == path.id && !it.completed && !it.archived }
            require(current.all { it.id in confirmedOpenTasks }) {
                "Ainda há tarefas abertas. Revise-as ou confirme a conclusão do caminho."
            }
        }
        val completedDate = if (path.state == 3) {
            val savedDate = previous?.takeIf { it.state == 3 }?.completedDate.orEmpty()
            savedDate.ifBlank { today() }
        } else ""
        val clean = path.copy(completedDate = completedDate)
        validatePathInput(clean)
        require(completedDate.isBlank() || completedDate in clean.startDate..today()) {
            "A conclusão deve ficar entre o início e hoje."
        }
        d.path(clean)
    }
    suspend fun duplicatePath(pathId:String,includeStructure:Boolean=true)=change("Caminho duplicado"){
        val orig=requireNotNull(d.paths().find{it.id==pathId}){"Caminho não encontrado."}
        val newPathId=uid()
        val pos=(d.paths().maxOfOrNull{it.position}?:-1)+1
        d.path(orig.copy(id=newPathId,name="${orig.name} (cópia)",state=0,startDate=today(),deadline=orig.deadline.takeIf{it>=today()}.orEmpty(),completedDate="",position=pos))
        if(includeStructure){
            val stageMap=HashMap<String,String>()
            d.pathStages().filter{it.pathId==pathId}.sortedBy{it.position}.forEach{st->
                val ns=uid();stageMap[st.id]=ns
                d.pathStage(st.copy(id=ns,pathId=newPathId))
            }
            val allSubtasks=d.pathSubtasks().groupBy{it.taskId}
            d.pathTasks().filter{it.pathId==pathId&&!it.archived}.sortedBy{it.position}.forEach{t->
                val nt=uid()
                d.pathTask(t.copy(id=nt,pathId=newPathId,stageId=stageMap[t.stageId]?:"",completed=false,completedDate="",archived=false,created=today()))
                allSubtasks[t.id].orEmpty().sortedBy{it.position}.forEach{sub->
                    d.pathSubtask(sub.copy(id=uid(),taskId=nt,done=false))
                }
            }
        }
    }
    suspend fun deletePath(pathId:String)=change("Caminho excluído"){
        val p=requireNotNull(d.paths().find{it.id==pathId}){"Caminho não encontrado."}
        val tasks=d.pathTasks().filter{it.pathId==pathId}
        val taskIds=tasks.mapTo(HashSet()){it.id}
        d.focus()?.let{f->if(f.pathId==pathId || f.taskId in taskIds) d.focus(f.copy(pathId=if(f.pathId==pathId)"" else f.pathId,taskId=if(f.taskId in taskIds)"" else f.taskId,targetLabel=f.targetLabel.ifBlank{p.name}))}
        d.focusSessions().filter{it.pathId==pathId || it.taskId in taskIds}.forEach{fs->
            d.focusSession(fs.copy(pathId=if(fs.pathId==pathId)"" else fs.pathId,taskId=if(fs.taskId in taskIds)"" else fs.taskId,targetLabel=fs.targetLabel.ifBlank{p.name}))
        }
        d.timeBlocks().filter{it.pathId==pathId || it.taskId in taskIds}.forEach{tb->
            d.timeBlock(if (tb.taskId in taskIds) tb.copy(pathId="",taskId="") else tb.copy(pathId=""))
        }
        d.notebooks().filter{it.pathId==pathId}.forEach{nb->d.notebook(nb.copy(pathId=""))}
        d.knowledgePages().filter{it.pathId==pathId || it.taskId in taskIds}.forEach{pg->
            d.knowledgePage(pg.copy(pathId=if(pg.pathId==pathId)"" else pg.pathId,taskId=if(pg.taskId in taskIds)"" else pg.taskId))
        }
        d.deletePathHabitLinksByPath(pathId)
        tasks.forEach{t->d.deletePathSubtasksByTask(t.id);d.deletePathTask(t.id)}
        d.deletePathStagesByPath(pathId)
        d.deletePath(pathId)
    }
    suspend fun saveStage(stage:PathStage)=change("Etapa salva"){
        val clean=stage.copy(name=stage.name.trim())
        require(clean.id.isNotBlank() && clean.name.isNotBlank() && clean.name.length<=120 && clean.position>=0){"Informe um nome para a etapa."}
        require(d.paths().any{it.id==clean.pathId}){"Caminho não encontrado."}
        require(d.pathStages().none { it.id == clean.id && it.pathId != clean.pathId }) {
            "Uma etapa existente não pode mudar de caminho."
        }
        d.pathStage(clean)
    }
    suspend fun deleteStage(stageId:String)=change("Etapa removida"){
        d.pathTasks().filter{it.stageId==stageId}.forEach{d.pathTask(it.copy(stageId=""))}
        d.deletePathStage(stageId)
    }
    suspend fun saveTask(task:PathTask)=change("Tarefa salva"){
        val clean=task.copy(title=task.title.trim())
        validateTaskInput(clean,d.paths(),d.pathStages())
        val finalTask=if(clean.completed && clean.completedDate.isBlank()) clean.copy(completedDate=today()) else if(!clean.completed) clean.copy(completedDate="") else clean
        d.pathTask(finalTask)
    }
    suspend fun toggleTask(taskId:String, completed:Boolean?=null)=change("Tarefa atualizada"){
        val t=requireNotNull(d.pathTasks().find{it.id==taskId}){"Tarefa não encontrada."}
        val nextDone=completed ?: !t.completed
        if (nextDone == t.completed) return@change
        d.pathTask(t.copy(completed=nextDone,completedDate=if(nextDone)today() else ""))
    }
    suspend fun moveTask(taskId:String,targetPathId:String,targetStageId:String="")=change("Tarefa movida"){
        val t=requireNotNull(d.pathTasks().find{it.id==taskId}){"Tarefa não encontrada."}
        if(targetPathId.isNotBlank()) require(d.paths().any{it.id==targetPathId}){"Caminho de destino não encontrado."}
        if(targetStageId.isNotBlank()) require(d.pathStages().any{it.id==targetStageId && it.pathId==targetPathId}){"Etapa de destino incompatível com o caminho."}
        d.pathTask(t.copy(pathId=targetPathId,stageId=if(targetPathId.isBlank())"" else targetStageId))
    }
    suspend fun duplicateTask(taskId:String)=change("Tarefa duplicada"){
        val t=requireNotNull(d.pathTasks().find{it.id==taskId}){"Tarefa não encontrada."}
        val newTaskId=uid()
        val pos=(d.pathTasks().filter{it.pathId==t.pathId}.maxOfOrNull{it.position}?:-1)+1
        d.pathTask(t.copy(id=newTaskId,title="${t.title} (cópia)",completed=false,completedDate="",archived=false,position=pos,created=today()))
        d.pathSubtasks().filter{it.taskId==taskId}.sortedBy{it.position}.forEach{sub->
            d.pathSubtask(sub.copy(id=uid(),taskId=newTaskId,done=false))
        }
    }
    suspend fun deleteTask(taskId:String)=change("Tarefa excluída"){
        val t=requireNotNull(d.pathTasks().find{it.id==taskId}){"Tarefa não encontrada."}
        d.focus()?.let{f->if(f.taskId==taskId) d.focus(f.copy(taskId="",targetLabel=f.targetLabel.ifBlank{t.title}))}
        d.focusSessions().filter{it.taskId==taskId}.forEach{fs->d.focusSession(fs.copy(taskId="",targetLabel=fs.targetLabel.ifBlank{t.title}))}
        d.timeBlocks().filter{it.taskId==taskId}.forEach{tb->d.timeBlock(tb.copy(taskId="",pathId=""))}
        d.knowledgePages().filter{it.taskId==taskId}.forEach{pg->d.knowledgePage(pg.copy(taskId=""))}
        d.deletePathSubtasksByTask(taskId)
        d.deletePathTask(taskId)
    }
    suspend fun saveSubtask(sub:PathSubtask)=change("Subtarefa salva"){
        val clean=sub.copy(title=sub.title.trim())
        require(clean.id.isNotBlank() && clean.title.isNotBlank() && clean.title.length<=200 && clean.position>=0){"Informe um título para a subtarefa."}
        require(d.pathTasks().any{it.id==clean.taskId}){"Tarefa pai não encontrada."}
        d.pathSubtask(clean)
    }
    suspend fun toggleSubtask(subtaskId:String)=change("Subtarefa atualizada"){
        val s=requireNotNull(d.pathSubtasks().find{it.id==subtaskId}){"Subtarefa não encontrada."}
        d.pathSubtask(s.copy(done=!s.done))
    }
    suspend fun reorderSubtask(subtaskId:String,step:Int)=change("Ordem das subtarefas atualizada"){
        val target=requireNotNull(d.pathSubtasks().find{it.id==subtaskId}){"Subtarefa não encontrada."}
        val list=d.pathSubtasks().filter{it.taskId==target.taskId}.sortedBy{it.position}.toMutableList()
        val i=list.indexOfFirst{it.id==subtaskId};val j=(i+step).coerceIn(0,(list.size-1).coerceAtLeast(0))
        if(i>=0&&i!=j){java.util.Collections.swap(list,i,j);list.forEachIndexed{n,v->d.pathSubtask(v.copy(position=n))}}
    }
    suspend fun deleteSubtask(subtaskId:String)=change("Subtarefa removida"){
        d.deletePathSubtask(subtaskId)
    }
    suspend fun linkHabitToPath(pathId:String,habitId:String,enabled:Boolean)=change(if(enabled)"Hábito vinculado ao caminho" else "Vínculo removido"){
        require(d.paths().any{it.id==pathId}){"Caminho não encontrado."}
        val h=requireNotNull(d.habits().find{it.id==habitId}){"Hábito não encontrado."}
        val linkId="$pathId@$habitId"
        if(enabled){
            require(!h.deleted){"Escolha um hábito existente."}
            d.pathHabitLink(PathHabitLink(linkId,pathId,habitId,today()))
        }else{
            d.deletePathHabitLink(linkId)
        }
    }
    suspend fun saveTimeBlock(b:TimeBlock)=change("Bloco de tempo salvo"){
        val clean=b.copy(title=b.title.trim(),category=b.category.trim().ifBlank{"Foco"})
        val tasks = d.pathTasks()
        val habits = d.habits()
        validateTimeBlockInput(clean,d.paths(),tasks,habits)
        require(clean.habitId.isBlank() || clean.taskId.isBlank() && clean.pathId.isBlank()) {
            "Vincule o bloco a um hábito ou a um caminho/tarefa."
        }
        require(clean.taskId.isBlank() || tasks.any {
            it.id == clean.taskId && !it.archived && (clean.pathId.isBlank() || it.pathId == clean.pathId)
        }) { "A tarefa está arquivada ou não pertence ao caminho do bloco." }
        require(clean.habitId.isBlank() || habits.any { it.id == clean.habitId && !it.deleted }) {
            "O hábito vinculado foi excluído."
        }
        d.timeBlock(clean)
    }
    suspend fun deleteTimeBlock(id:String)=change("Bloco de tempo removido"){
        d.deleteTimeBlock(id)
    }
    suspend fun saveEvent(e:AgendaEvent)=change("Compromisso salvo"){
        val clean=e.copy(title=e.title.trim(),kind=e.kind.trim().ifBlank{"Compromisso"})
        validateAgendaEventInput(clean)
        d.agendaEvent(clean)
    }
    suspend fun toggleEvent(id:String)=change("Compromisso atualizado"){
        val e=requireNotNull(d.agendaEvents().find{it.id==id}){"Compromisso não encontrado."}
        d.agendaEvent(e.copy(completed=!e.completed))
    }
    suspend fun deleteEvent(id:String)=change("Compromisso excluído"){
        d.deleteAgendaEvent(id)
    }
    suspend fun saveInboxItem(item:InboxItem)=change("Item guardado na Caixa de Entrada"){
        val clean=item.copy(title=item.title.trim())
        require(clean.id.isNotBlank() && clean.title.isNotBlank() && clean.title.length<=200){"Informe um título para capturar."}
        LocalDate.parse(clean.created)
        d.inboxItem(clean)
    }
    suspend fun deleteInboxItem(id:String)=change("Item removido da Caixa de Entrada"){
        d.deleteInboxItem(id)
    }
    suspend fun triageInboxToTask(inboxId:String,task:PathTask,subtaskTitles:List<String> = emptyList())=change("Item organizado em tarefa"){
        require(d.inboxItems().any { it.id == inboxId && !it.archived }) { "Item ativo da Caixa de Entrada não encontrado." }
        val clean=task.copy(title=task.title.trim())
        require(d.pathTasks().none { it.id == clean.id }) { "Esta tarefa já existe." }
        validateTaskInput(clean,d.paths(),d.pathStages())
        d.pathTask(clean)
        subtaskTitles.map{it.trim()}.filter{it.isNotBlank()}.forEachIndexed{i,title->
            require(title.length<=200){"Subtarefa muito longa."}
            d.pathSubtask(PathSubtask(taskId=clean.id,title=title,position=i))
        }
        d.deleteInboxItem(inboxId)
    }
    suspend fun triageInboxToEvent(inboxId:String,event:AgendaEvent)=change("Item agendado como compromisso"){
        require(d.inboxItems().any { it.id == inboxId && !it.archived }) { "Item ativo da Caixa de Entrada não encontrado." }
        val clean=event.copy(title=event.title.trim(),kind=event.kind.trim().ifBlank{"Compromisso"})
        require(d.agendaEvents().none { it.id == clean.id }) { "Este compromisso já existe." }
        validateAgendaEventInput(clean)
        d.agendaEvent(clean)
        d.deleteInboxItem(inboxId)
    }
    suspend fun saveNotebook(notebook:KnowledgeNotebook)=change("Caderno guardado na Biblioteca"){
        val existing=d.notebooks().find{it.id==notebook.id}
        val clean=notebook.copy(
            name=notebook.name.trim(),
            description=notebook.description.trim(),
            category=notebook.category.trim().ifBlank{"Estudo"},
            created=existing?.created ?: notebook.created.ifBlank{today()},
            updated=today()
        )
        validateNotebookInput(clean,d.paths())
        d.notebook(clean)
    }
    suspend fun archiveNotebook(notebookId:String,archived:Boolean)=change(if(archived)"Caderno arquivado" else "Caderno reativado"){
        val nb=requireNotNull(d.notebooks().find{it.id==notebookId}){"Caderno não encontrado."}
        d.notebook(nb.copy(archived=archived,updated=today()))
    }
    suspend fun deleteNotebook(notebookId:String)=change("Caderno excluído"){
        requireNotNull(d.notebooks().find{it.id==notebookId}){"Caderno não encontrado."}
        val now=today()
        val pagesToDelete=d.knowledgePages().filter{it.notebookId==notebookId}
        val deletedPageIds=pagesToDelete.mapTo(HashSet()){it.id}
        d.flashcards().filter{it.notebookId==notebookId}.forEach{card->
            d.deleteFlashcardReviewsForCard(card.id)
            d.deleteFlashcard(card.id)
        }
        // Defensively unlink any surviving flashcard in another notebook whose pageId belonged to this notebook.
        if(deletedPageIds.isNotEmpty()){
            d.flashcards().filter{it.notebookId!=notebookId && it.pageId in deletedPageIds}.forEach{card->
                d.flashcard(card.copy(pageId="",updated=now))
            }
        }
        pagesToDelete.forEach{pg->
            d.deleteKnowledgeReferencesForPage(pg.id)
            d.deleteKnowledgeLinksForPage(pg.id)
            d.deleteKnowledgePage(pg.id)
        }
        d.deleteNotebook(notebookId)
    }
    suspend fun savePage(page:KnowledgePage)=change("Página guardada"){
        val existing=d.knowledgePages().find{it.id==page.id}
        val clean=page.copy(
            title=page.title.trim(),
            tags=normalizeTags(page.tags),
            created=existing?.created ?: page.created.ifBlank{today()},
            updated=today()
        )
        validatePageInput(clean,d.notebooks(),d.paths(),d.pathTasks())
        d.knowledgePage(clean)
        d.notebooks().find{it.id==clean.notebookId}?.let{nb->d.notebook(nb.copy(updated=today()))}
    }
    suspend fun duplicatePage(pageId:String)=change("Página duplicada"){
        val orig=requireNotNull(d.knowledgePages().find{it.id==pageId}){"Página não encontrada."}
        val copyId=uid()
        val now=today()
        val newTitle="${orig.title} (cópia)".take(200)
        d.knowledgePage(orig.copy(id=copyId,title=newTitle,favorite=false,archived=false,created=now,updated=now))
        d.knowledgeReferences().filter{it.pageId==pageId}.forEach{ref->
            d.knowledgeReference(ref.copy(id=uid(),pageId=copyId,created=now))
        }
        d.notebooks().find{it.id==orig.notebookId}?.let{nb->d.notebook(nb.copy(updated=now))}
    }
    suspend fun movePage(pageId:String,targetNotebookId:String)=change("Página movida"){
        val pg=requireNotNull(d.knowledgePages().find{it.id==pageId}){"Página não encontrada."}
        val dest=requireNotNull(d.notebooks().find{it.id==targetNotebookId}){"Caderno de destino não encontrado."}
        val now=today()
        if(pg.notebookId==targetNotebookId){
            d.knowledgePage(pg.copy(updated=now))
            d.notebook(dest.copy(updated=now))
            return@change
        }
        val sourceNotebookId=pg.notebookId
        // Explicit policy: flashcards stay in their notebook with full review history,
        // and any stale cross-notebook page link is unlinked cleanly.
        d.flashcards().filter{it.pageId==pageId && it.notebookId!=targetNotebookId}.forEach{card->
            d.flashcard(card.copy(pageId="",updated=now))
        }
        d.knowledgePage(pg.copy(notebookId=targetNotebookId,updated=now))
        d.notebooks().find{it.id==sourceNotebookId}?.let{src->d.notebook(src.copy(updated=now))}
        d.notebook(dest.copy(updated=now))
    }
    suspend fun togglePageFavorite(pageId:String)=change("Favorito atualizado"){
        val pg=requireNotNull(d.knowledgePages().find{it.id==pageId}){"Página não encontrada."}
        d.knowledgePage(pg.copy(favorite=!pg.favorite,updated=today()))
    }
    suspend fun archivePage(pageId:String,archived:Boolean)=change(if(archived)"Página arquivada" else "Página reativada"){
        val pg=requireNotNull(d.knowledgePages().find{it.id==pageId}){"Página não encontrada."}
        d.knowledgePage(pg.copy(archived=archived,updated=today()))
    }
    suspend fun deletePage(pageId:String)=change("Página excluída"){
        requireNotNull(d.knowledgePages().find{it.id==pageId}){"Página não encontrada."}
        d.flashcards().filter{it.pageId==pageId}.forEach{card->
            d.flashcard(card.copy(pageId="",updated=today()))
        }
        d.deleteKnowledgeReferencesForPage(pageId)
        d.deleteKnowledgeLinksForPage(pageId)
        d.deleteKnowledgePage(pageId)
    }
    suspend fun linkPages(sourcePageId:String,targetPageId:String)=change("Ligação entre páginas criada"){
        require(sourcePageId!=targetPageId){"Uma página não pode referenciar a si mesma."}
        val pages=d.knowledgePages()
        require(pages.any{it.id==sourcePageId} && pages.any{it.id==targetPageId}){"Páginas para ligação não encontradas."}
        val linkId="$sourcePageId->$targetPageId"
        d.knowledgeLink(KnowledgePageLink(id=linkId,sourcePageId=sourcePageId,targetPageId=targetPageId,created=today()))
    }
    suspend fun unlinkPages(sourcePageId:String,targetPageId:String)=change("Ligação removida"){
        d.deleteKnowledgeLink("$sourcePageId->$targetPageId")
    }
    suspend fun saveReference(reference:KnowledgeReference)=change("Referência bibliográfica salva"){
        val existing=d.knowledgeReferences().find{it.id==reference.id}
        val clean=reference.copy(
            author=reference.author.trim(),
            title=reference.title.trim(),
            year=reference.year.trim(),
            source=reference.source.trim(),
            url=reference.url.trim(),
            location=reference.location.trim(),
            notes=reference.notes.trim(),
            created=existing?.created ?: reference.created.ifBlank{today()}
        )
        validateReferenceInput(clean,d.knowledgePages())
        d.knowledgeReference(clean)
    }
    suspend fun deleteReference(referenceId:String)=change("Referência removida"){
        d.deleteKnowledgeReference(referenceId)
    }
    suspend fun saveFlashcard(card:Flashcard)=change("Runa da Memória salva"){
        val existing=d.flashcards().find{it.id==card.id}
        val clean=card.copy(
            question=card.question.trim(),
            answer=card.answer.trim(),
            tags=normalizeTags(card.tags),
            easeFactor=sanitizeEaseFactor(card.easeFactor),
            created=existing?.created ?: card.created.ifBlank{today()},
            updated=today()
        )
        validateFlashcardInput(clean,d.notebooks(),d.knowledgePages())
        d.flashcard(clean)
    }
    suspend fun archiveFlashcard(flashcardId:String,archived:Boolean)=change(if(archived)"Flashcard arquivado" else "Flashcard reativado"){
        val card=requireNotNull(d.flashcards().find{it.id==flashcardId}){"Flashcard não encontrado."}
        d.flashcard(card.copy(archived=archived,updated=today()))
    }
    suspend fun deleteFlashcard(flashcardId:String)=change("Flashcard excluído"){
        requireNotNull(d.flashcards().find{it.id==flashcardId}){"Flashcard não encontrado."}
        d.deleteFlashcardReviewsForCard(flashcardId)
        d.deleteFlashcard(flashcardId)
    }
    suspend fun reviewFlashcard(flashcardId:String,rating:Int,reviewDate:String=today())=change("Revisão registrada"){
        require(rating in 0..3){"Avaliação inválida."}
        LocalDate.parse(reviewDate)
        val card=requireNotNull(d.flashcards().find{it.id==flashcardId}){"Flashcard não encontrado."}
        require(!card.archived){"Flashcards arquivados não podem ser revisados."}
        val nb=requireNotNull(d.notebooks().find{it.id==card.notebookId}){"Caderno do flashcard não encontrado."}
        require(!nb.archived){"Reative o caderno antes de revisar seus flashcards."}
        require(card.nextReviewDate<=reviewDate && card.lastReviewDate!=reviewDate){"Este flashcard já foi revisado hoje ou ainda não está pendente."}
        val outcome=computeNextReview(card,rating,reviewDate)
        d.flashcard(card.copy(
            repetitions=outcome.repetitions,
            intervalDays=outcome.intervalDays,
            easeFactor=outcome.easeFactor,
            nextReviewDate=outcome.nextReviewDate,
            lastReviewDate=reviewDate,
            updated=reviewDate
        ))
        d.flashcardReview(FlashcardReview(
            flashcardId=card.id,
            reviewDate=reviewDate,
            rating=rating,
            intervalBefore=card.intervalDays,
            intervalAfter=outcome.intervalDays,
            easeAfter=outcome.easeFactor,
            reviewedAt=System.currentTimeMillis()
        ))
    }
    suspend fun importState(s:GameState)=change("Backup restaurado"){
        validateState(s)
        d.clearHeraldry()
        d.clearLoadouts()
        d.clearFlashcardReviews();d.clearFlashcards();d.clearKnowledgeReferences();d.clearKnowledgeLinks();d.clearKnowledgePages();d.clearNotebooks()
        d.clearPathHabitLinks();d.clearPathSubtasks();d.clearPathTasks();d.clearPathStages();d.clearPaths();d.clearTimeBlocks();d.clearAgendaEvents();d.clearInboxItems()
        d.clearPlans();d.clearFocusSessions();d.clearRestDays();d.clearFocus();d.clearRecords();d.clearHabits();d.clearLedger();d.clearQuests();d.clearClaims();d.clearAssets();d.clearAchievements();d.clearJournal();d.clearRoutines();d.clearPlayer()
        s.restDays.forEach{d.restDay(it)}
        s.player?.let{d.player(it)};s.habits.forEach{d.habit(it)};s.plans.forEach{d.plan(it)};s.records.forEach{d.record(it)};s.ledger.forEach{d.reward(it)};s.quests.forEach{d.quest(it)};s.claims.forEach{d.claim(it)};s.assets.forEach{d.asset(it)};s.achievements.forEach{d.unlock(it)};s.journal.forEach{d.journal(it)};s.routines.forEach{d.routine(it)}
        s.paths.forEach{d.path(it)};s.pathStages.forEach{d.pathStage(it)};s.pathTasks.forEach{d.pathTask(it)};s.pathSubtasks.forEach{d.pathSubtask(it)};s.pathHabitLinks.forEach{d.pathHabitLink(it)};s.timeBlocks.forEach{d.timeBlock(it)};s.agendaEvents.forEach{d.agendaEvent(it)};s.inboxItems.forEach{d.inboxItem(it)}
        s.notebooks.forEach{d.notebook(it)};s.knowledgePages.forEach{d.knowledgePage(it)};s.knowledgeLinks.forEach{d.knowledgeLink(it)};s.knowledgeReferences.forEach{d.knowledgeReference(it)};s.flashcards.forEach{d.flashcard(it)};s.flashcardReviews.forEach{d.flashcardReview(it)}
        s.loadouts.forEach{d.loadout(it)}
        s.heraldry?.let{d.saveHeraldry(it)}
        s.focusSessions.forEach{d.focusSession(it)};s.focus?.let{d.focus(it.copy(elapsed=it.seconds(),startedAt=0))}
    }
}
fun validatePathInput(p:PersonalPath){
    require(p.id.isNotBlank() && p.name.isNotBlank() && p.name.length<=120){"Informe um nome para o caminho (até 120 caracteres)."}
    LocalDate.parse(p.startDate)
    if(p.deadline.isNotBlank()){
        LocalDate.parse(p.deadline)
        require(p.deadline>=p.startDate){"O prazo do caminho deve ser igual ou posterior ao início."}
    }
    if(p.completedDate.isNotBlank()) LocalDate.parse(p.completedDate)
    require(p.priority in priorityLabels.indices && p.state in pathStates.indices && p.attribute in -1..4 && p.icon in glyphs.indices && p.color in palette.indices){"Revise prioridade, estado, atributo ou emblema."}
}
fun validateTaskInput(t:PathTask,paths:List<PersonalPath>,stages:List<PathStage>, legacy:Boolean=false){
    require(t.id.isNotBlank() && t.title.isNotBlank() && t.title.length<=200){"Informe um título para a tarefa."}
    require(t.priority in priorityLabels.indices && t.estimateMinutes in 0..1440 && t.position>=0){"Revise prioridade ou estimativa."}
    LocalDate.parse(t.created)
    if(t.deadline.isNotBlank()) LocalDate.parse(t.deadline)
    if(t.scheduledDate.isNotBlank()) LocalDate.parse(t.scheduledDate)
    if(t.scheduledTime.isNotBlank()) {
        require(legacy || t.scheduledDate.isNotBlank()) { "Informe a data antes do horário da tarefa." }
        LocalTime.parse(t.scheduledTime)
    }
    if(t.completedDate.isNotBlank()) LocalDate.parse(t.completedDate)
    if(t.pathId.isNotBlank()) require(paths.any{it.id==t.pathId}){"Caminho associado não encontrado."}
    if(t.stageId.isNotBlank()) require(t.pathId.isNotBlank() && stages.any{it.id==t.stageId && it.pathId==t.pathId}){"Etapa associada não pertence ao caminho."}
}
fun validateTimeBlockInput(b:TimeBlock,paths:List<PersonalPath>,tasks:List<PathTask>,habits:List<Habit>){
    require(b.id.isNotBlank() && b.title.isNotBlank() && b.title.length<=160){"Informe um título para o bloco de tempo."}
    LocalDate.parse(b.date)
    val start=LocalTime.parse(b.startTime);val end=LocalTime.parse(b.endTime)
    require(end.isAfter(start)){"O horário final deve ser posterior ao inicial."}
    require(b.color in palette.indices){"Cor inválida."}
    if(b.pathId.isNotBlank()) require(paths.any{it.id==b.pathId}){"Caminho vinculado não encontrado."}
    if(b.taskId.isNotBlank()) require(tasks.any{it.id==b.taskId}){"Tarefa vinculada não encontrada."}
    if(b.habitId.isNotBlank()) require(habits.any{it.id==b.habitId}){"Hábito vinculado não encontrado."}
}
fun validateAgendaEventInput(e:AgendaEvent){
    require(e.id.isNotBlank() && e.title.isNotBlank() && e.title.length<=160){"Informe um título para o compromisso."}
    LocalDate.parse(e.date)
    if(e.startTime.isNotBlank()) LocalTime.parse(e.startTime)
    if(e.endTime.isNotBlank()){
        require(e.startTime.isNotBlank()){"Informe o horário de início antes do término."}
        require(!LocalTime.parse(e.endTime).isBefore(LocalTime.parse(e.startTime))){"O horário de término não pode preceder o início."}
    }
}
fun validateHabitInput(h:Habit){
        LocalDate.parse(h.created)
        require(h.name.isNotBlank() && h.target in 1..1000000 && h.xp in 0..1000000 && h.coins in 0..1000000 && h.points in 0..1000000){"Revise o nome, a meta e as recompensas (0 a 1.000.000)."}
        require(h.attribute in 0..4 && h.type in 0..3 && h.days.split(',').all{it.toIntOrNull() in 1..7}){"Selecione ao menos um dia."}
        require(h.time.isBlank()&&!h.reminder||Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(h.time)){"Use um horário como 08:00."}
    require(h.difficulty in 0..2 && h.icon in glyphs.indices && h.color in palette.indices){"Revise a dificuldade, o ícone e a cor."}
    require(h.templateId.length<=100 && h.narrativeName.length<=200){"Nome narrativo muito longo."}
}
data class BackupEnvelope(val format:String="ASCEND",val version:Int=12,val sha256:String,val payload:String)
object Backup {
    private val gson=Gson()
    private fun digest(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
    fun encode(s:GameState):String {val data=gson.toJson(s);return gson.toJson(BackupEnvelope(sha256=digest(data),payload=data))}
    fun decode(text:String):GameState {
        require(text.length<=20_000_000){"Backup muito grande (máximo 20 MB)."}
        val b=gson.fromJson(text,BackupEnvelope::class.java)
        require(b.format=="ASCEND" && b.version in 1..12 && b.sha256==digest(b.payload)){"Backup inválido, corrompido ou de versão incompatível."}
        val json=com.google.gson.JsonParser.parseString(b.payload).asJsonObject
        json["player"]?.takeIf{it.isJsonObject}?.asJsonObject?.let{p->if(!p.has("specialization"))p.addProperty("specialization","")}
        listOf("quests","claims").forEach{table->json.getAsJsonArray(table)?.forEach{row->val obj=row.asJsonObject;listOf("sagaStart","sagaBaseline").forEach{key->if(!obj.has(key))obj.addProperty(key,"")};if(!obj.has("focusRuleVersion"))obj.addProperty("focusRuleVersion",0)}}
        json.getAsJsonArray("quests")?.forEach{row->val obj=row.asJsonObject;if(!obj.has("sagaState"))obj.addProperty("sagaState",0);if(!obj.has("sagaEnd"))obj.addProperty("sagaEnd","");if(!obj.has("sagaStopped"))obj.addProperty("sagaStopped",0)}
        json.getAsJsonArray("assets")?.forEach{row->listOf("memorySpeaker","memoryText").forEach{key->if(!row.asJsonObject.has(key))row.asJsonObject.addProperty(key,"")}}
        listOf("plans","focusSessions","paths","pathStages","pathTasks","pathSubtasks","pathHabitLinks","timeBlocks","agendaEvents","inboxItems","notebooks","knowledgePages","knowledgeLinks","knowledgeReferences","flashcards","flashcardReviews","loadouts").forEach{if(!json.has(it))json.add(it,com.google.gson.JsonArray())}
        json["focus"]?.takeIf{it.isJsonObject}?.asJsonObject?.let{f->if(!f.has("sessionId"))f.addProperty("sessionId","");if(!f.has("beganAt"))f.addProperty("beganAt",0);if(!f.has("pathId"))f.addProperty("pathId","");if(!f.has("taskId"))f.addProperty("taskId","");if(!f.has("targetLabel"))f.addProperty("targetLabel","")}
        json.getAsJsonArray("focusSessions")?.forEach{row->val obj=row.asJsonObject;listOf("pathId","taskId","targetLabel").forEach{k->if(!obj.has(k))obj.addProperty(k,"")}}
        val s=gson.fromJson(json,GameState::class.java);validateState(s);return s
    }
}
fun validateState(s:GameState){
    s.habits.forEach{require(it.templateId.length<=100 && it.narrativeName.length<=200)}
    val p=requireNotNull(s.player){"Backup sem personagem."}; require(p.id==1 && p.name.isNotBlank() && p.heroClass in 0..4 && p.color in 0..4 && p.face in 0..2 && p.outfit in 0..2 && p.frame in 0..2)
    require(p.specialization.isBlank() || specializationById(p.specialization)?.heroClass==p.heroClass){"Especialização incompatível com a classe do personagem."}
    LocalDate.parse(p.created)
    require(s.habits.size<=10000 && s.records.size<=200000 && s.ledger.size<=300000)
    fun distinct(ids:List<String>)=require(ids.all{it.isNotBlank()} && ids.distinct().size==ids.size)
    distinct(s.habits.map{it.id});distinct(s.records.map{it.id});distinct(s.ledger.map{it.id});distinct(s.quests.map{it.id});distinct(s.claims.map{it.id});distinct(s.assets.map{it.id});distinct(s.achievements.map{it.id});distinct(s.journal.map{it.id});distinct(s.routines.map{it.id})
    require(s.loadouts.size<=3)
    distinct(s.loadouts.map{it.id})
    s.loadouts.forEach{validateLoadoutInput(it)}
    s.heraldry?.let{validateHeraldryProfile(it)}
    require(s.restDays.size<=36600)
    distinct(s.restDays.map{it.date});s.restDays.forEach{LocalDate.parse(it.date)}
    s.habits.forEach{h->require(h.name.isNotBlank() && h.target in 1..1000000 && h.attribute in 0..4 && h.color in 0..4 && h.icon in glyphs.indices && h.type in 0..3 && h.xp in 0..1000000 && h.coins in 0..1000000 && h.points in 0..1000000 && h.days.split(',').all{it.toIntOrNull() in 1..7});LocalDate.parse(h.created);if(h.time.isNotBlank())LocalTime.parse(h.time)
        if(h.scheduleHistory.isNotBlank())h.scheduleHistory.split(';').forEach{entry->val parts=entry.split('|');require(parts.size==3 && parts[1].split(',').all{it.toIntOrNull() in 1..7} && parts[2].toBooleanStrictOrNull()!=null);LocalDate.parse(parts[0])}
    }
    s.records.forEach{r->require(s.habits.any{it.id==r.habitId} && r.id=="${r.habitId}@${r.date}" && r.target in 1..1000000 && r.progress in 0..r.target && r.attribute in 0..4 && r.type in 0..3 && r.xp in 0..1000000 && r.coins in 0..1000000 && r.points in 0..1000000 && r.date<=today());LocalDate.parse(r.date)}
    s.ledger.forEach{require(kotlin.math.abs(it.xp)<=1000000000L && kotlin.math.abs(it.coins)<=1000000000L && kotlin.math.abs(it.points)<=1000000000L && it.attribute in -1..4);LocalDate.parse(it.date)}
    s.quests.filter{it.sagaStart.isNotBlank()}.forEach{validateSaga(it,s);LocalDate.parse(it.sagaStart);parseBaseline(it.sagaBaseline)}
    s.quests.forEach{val maxMetric=if(isExpedition(it)) 6 else 3;require(it.sagaState in 0..3 && it.sagaStopped>=0 && it.focusRuleVersion in 0..1);if(it.sagaEnd.isNotBlank())LocalDate.parse(it.sagaEnd);require(it.attribute in -1..4 && it.name.isNotBlank() && it.target in 1..1000000 && it.xp in 0..1000000 && it.coins in 0..1000000 && it.period in 0..3 && it.metric in 0..maxMetric);if(it.deadline.isNotEmpty())LocalDate.parse(it.deadline)}
    s.assets.forEach{a->
        require(a.memorySpeaker.length<=200 && a.memoryText.length<=10000)
        require(a.kind in setOf("building","outfit","frame","background","companion","decoration","trophy","head","accessory","weapon","skill","memory"))
        if(a.kind=="building") require(a.id.removePrefix("building:").toIntOrNull() in buildings.indices && a.stage in 1..2)
        else if(a.kind=="memory") require(a.id.startsWith("memory:") && a.id.length<=250)
        else if(a.kind=="skill") require(skillDefs.any{"skill:${it.id}"==a.id})
        else if(a.kind!="trophy") {
            require(shopItems.any{it.id==a.id && it.kind==a.kind})
            if(a.id in forgeItemIds) require(a.stage in FORGE_STAGE_FORGED..FORGE_STAGE_CONSECRATED) else require(a.stage==1)
        }
        LocalDate.parse(a.date)
    }
    require(s.assets.filter{it.equipped}.groupBy{it.kind}.all{it.value.size<=1})
    s.achievements.forEach{a->require(achievementDefs.any{it.id==a.id});LocalDate.parse(a.date)}
    s.journal.forEach{require(it.title.length<=100000 && it.text.length<=1000000);LocalDate.parse(it.date)}
    s.routines.forEach{r->require(r.name.isNotBlank() && r.habitIds.split(',').all{id->s.habits.any{it.id==id}});LocalTime.parse(r.time)}
    s.claims.forEach{c->val maxMetric=if(expeditionRef(c.questId)!=null) 6 else 3;if(c.sagaStart.isNotBlank()){LocalDate.parse(c.sagaStart);parseBaseline(c.sagaBaseline)};require(s.quests.any{it.id==c.questId} && c.id=="${c.questId}@${c.occurrence}" && c.xp in 0..1000000 && c.coins in 0..1000000 && c.target in 1..1000000 && c.metric in 0..maxMetric && c.period in 0..3 && c.attribute in -1..4 && c.focusRuleVersion in 0..1);LocalDate.parse(c.date);if(c.period!=2)LocalDate.parse(c.occurrence)}
    require(s.plans.size<=200000 && s.focusSessions.size<=200000)
    distinct(s.plans.map{it.id});distinct(s.focusSessions.map{it.id})
    s.plans.forEach{p->LocalDate.parse(p.date);require(p.id=="${p.habitId}@${p.date}"&&s.habits.any{it.id==p.habitId&&p.date>=it.created})}
    require(s.paths.size<=10000 && s.pathStages.size<=50000 && s.pathTasks.size<=200000 && s.pathSubtasks.size<=400000 && s.pathHabitLinks.size<=50000 && s.timeBlocks.size<=200000 && s.agendaEvents.size<=200000 && s.inboxItems.size<=100000)
    distinct(s.paths.map{it.id});distinct(s.pathStages.map{it.id});distinct(s.pathTasks.map{it.id});distinct(s.pathSubtasks.map{it.id});distinct(s.pathHabitLinks.map{it.id});distinct(s.timeBlocks.map{it.id});distinct(s.agendaEvents.map{it.id});distinct(s.inboxItems.map{it.id})
    s.paths.forEach{validatePathInput(it)}
    s.pathStages.forEach{st->require(st.name.isNotBlank() && st.name.length<=120 && st.position>=0 && s.paths.any{it.id==st.pathId})}
    s.pathTasks.forEach{validateTaskInput(it,s.paths,s.pathStages,legacy=true)}
    s.pathSubtasks.forEach{sub->require(sub.title.isNotBlank() && sub.title.length<=200 && sub.position>=0 && s.pathTasks.any{it.id==sub.taskId})}
    s.pathHabitLinks.forEach{l->LocalDate.parse(l.created);require(l.id=="${l.pathId}@${l.habitId}" && s.paths.any{it.id==l.pathId} && s.habits.any{it.id==l.habitId})}
    s.timeBlocks.forEach{validateTimeBlockInput(it,s.paths,s.pathTasks,s.habits)}
    s.agendaEvents.forEach{validateAgendaEventInput(it)}
    s.inboxItems.forEach{item->require(item.title.isNotBlank() && item.title.length<=200);LocalDate.parse(item.created)}
    require(s.notebooks.size<=5000 && s.knowledgePages.size<=50000 && s.knowledgeLinks.size<=200000 && s.knowledgeReferences.size<=100000 && s.flashcards.size<=100000 && s.flashcardReviews.size<=500000)
    distinct(s.notebooks.map{it.id});distinct(s.knowledgePages.map{it.id});distinct(s.knowledgeLinks.map{it.id});distinct(s.knowledgeReferences.map{it.id});distinct(s.flashcards.map{it.id});distinct(s.flashcardReviews.map{it.id})
    s.notebooks.forEach{validateNotebookInput(it,s.paths)}
    s.knowledgePages.forEach{validatePageInput(it,s.notebooks,s.paths,s.pathTasks)}
    s.knowledgeLinks.forEach{l->LocalDate.parse(l.created);require(l.sourcePageId!=l.targetPageId && l.id=="${l.sourcePageId}->${l.targetPageId}" && s.knowledgePages.any{it.id==l.sourcePageId} && s.knowledgePages.any{it.id==l.targetPageId})}
    s.knowledgeReferences.forEach{validateReferenceInput(it,s.knowledgePages)}
    s.flashcards.forEach{validateFlashcardInput(it,s.notebooks,s.knowledgePages)}
    s.flashcardReviews.forEach{r->LocalDate.parse(r.reviewDate);require(r.rating in 0..3 && r.intervalBefore in 0..3650 && r.intervalAfter in 1..3650 && !r.easeAfter.isNaN() && !r.easeAfter.isInfinite() && r.easeAfter in 1.3..3.5 && r.reviewedAt>=0 && s.flashcards.any{it.id==r.flashcardId})}
    s.focusSessions.forEach{f->LocalDate.parse(f.date);require((f.habitId.isNotBlank()||f.pathId.isNotBlank()||f.taskId.isNotBlank()||f.targetLabel.isNotBlank()) && (f.habitId.isBlank()||s.habits.any{it.id==f.habitId}) && (f.pathId.isBlank()||s.paths.any{it.id==f.pathId}) && (f.taskId.isBlank()||s.pathTasks.any{it.id==f.taskId}) && f.date<=today() && f.startedAt>0 && f.endedAt>=f.startedAt && f.plannedMinutes in 1..1440 && f.elapsedSeconds in 0..f.plannedMinutes*60L && f.status in listOf("completed","interrupted","cancelled") && f.registered==(f.status!="cancelled"))}
    s.focus?.let{f->
        LocalDate.parse(f.date)
        require(f.sessionId.isBlank() || s.focusSessions.none { it.id == f.sessionId }) { "Sessão ativa já existe no histórico." }
        require(f.id==1 && (f.habitId.isNotBlank()||f.pathId.isNotBlank()||f.taskId.isNotBlank()||f.targetLabel.isNotBlank()) && (f.habitId.isBlank()||s.habits.any{it.id==f.habitId && f.date>=it.created}) && (f.pathId.isBlank()||s.paths.any{it.id==f.pathId}) && (f.taskId.isBlank()||s.pathTasks.any{it.id==f.taskId}) && f.date<=today() && f.duration in 1..1440 && f.elapsed in 0..f.duration*60L && f.startedAt>=0 && f.baseline in 0..1000000){"Sessão de foco inválida no backup."}
    }
}
class GameViewModel(app:Application):AndroidViewModel(app){
    val repo=(app as AscendApp).repository
    val state=repo.state.asStateFlow()
    val error=MutableStateFlow<String?>(null)
    private val drafts=LinkedHashMap<String,String>()
    fun getDraft(key:String,fallback:String):String=drafts[key]?:fallback
    fun setDraft(key:String,value:String){drafts[key]=value}
    fun hasDraft(key:String):Boolean=key in drafts
    fun clearDraftsWithPrefix(prefix:String){drafts.keys.removeAll{it.startsWith(prefix)}}
    val relaxed=app.preferences.data.map{it[booleanPreferencesKey("relaxed")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val bosses=app.preferences.data.map{it[booleanPreferencesKey("bosses")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val appearance=app.preferences.data.map{it[stringPreferencesKey("theme")]?:"dark"}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),"dark")
    val reducedMotion=app.preferences.data.map{it[booleanPreferencesKey("reducedMotion")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val minimal=app.preferences.data.map{it[booleanPreferencesKey("minimal")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val soundEnabled=app.preferences.data.map{it[booleanPreferencesKey("sound_enabled")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val soundVolume=app.preferences.data.map{it[floatPreferencesKey("sound_volume")]?:.35f}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),.35f)
    fun volume(value:Float)=run{getApplication<Application>().preferences.edit{it[floatPreferencesKey("sound_volume")]=value.coerceIn(0f,1f)}}
    val narrativeNames=app.preferences.data.map{it[booleanPreferencesKey("narrative_names")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val catalogInterests=app.preferences.data.map{it[stringSetPreferencesKey("catalog_interests")]?:emptySet()}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptySet())
    val dismissedSuggestions=app.preferences.data.map{it[stringSetPreferencesKey("catalog_dismissed")]?:emptySet()}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptySet())
    fun interest(category:String)=run{getApplication<Application>().preferences.edit{p->val key=stringSetPreferencesKey("catalog_interests");val old=p[key]?:emptySet();p[key]=if(category in old)old-category else old+category}}
    fun dismissSuggestion(id:String)=run{getApplication<Application>().preferences.edit{p->val key=stringSetPreferencesKey("catalog_dismissed");p[key]=(p[key]?:emptySet())+id}}
    fun resetSuggestions()=run{getApplication<Application>().preferences.edit{it.remove(stringSetPreferencesKey("catalog_dismissed"))}}
    init{run{repo.refresh()}}
    fun run(block:suspend ()->Unit){viewModelScope.launch{try{block()}catch(e:CancellationException){throw e}catch(e:Exception){error.value=when(e){
            is java.time.format.DateTimeParseException -> "Informe uma data no formato AAAA-MM-DD ou um horário no formato HH:mm."
            is com.google.gson.JsonParseException -> "O arquivo de backup não pôde ser lido. Escolha um backup válido do ASCEND."
            is java.io.IOException -> "Não foi possível acessar o arquivo. Verifique a pasta escolhida e tente novamente."
            is NullPointerException -> "Os dados estão incompletos. Escolha um backup válido do ASCEND."
            is IllegalArgumentException -> e.message?.takeUnless{it=="Failed requirement."}?:"Os dados informados são inválidos. Revise os campos ou escolha outro backup."
            else -> "Não foi possível salvar. Tente novamente."
        }}}}
    fun setting(key:String,value:Boolean)=run{getApplication<Application>().preferences.edit{it[booleanPreferencesKey(key)]=value}}
    fun theme(value:String)=run{getApplication<Application>().preferences.edit{it[stringPreferencesKey("theme")]=value}}
}
