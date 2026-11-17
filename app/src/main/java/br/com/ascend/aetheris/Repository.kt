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
    val database by lazy { Room.databaseBuilder(this,GameDatabase::class.java,"ascend.db").addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5).build() }
    val repository by lazy { GameRepository(database){s->HabitWidget.refresh(this,s)} }
}
data class GameEvent(val message:String,val level:Int=0,val xp:Long=0,val coins:Long=0,val points:List<Long> = emptyList(),val highlights:List<String> = emptyList())
class GameRepository(private val db:GameDatabase,private val onChanged:(GameState)->Unit={}){
    private val d=db.dao(); private val lock=Mutex()
    val state=MutableStateFlow<GameState?>(null)
    val events=MutableSharedFlow<GameEvent>(extraBufferCapacity=10)
    private suspend fun read()=GameState(d.player(),d.habits(),d.records(),d.ledger(),d.quests(),d.claims(),d.assets(),d.achievements(),d.journal(),d.routines(),d.focus(),d.restDays())
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
                d.unlock(Unlocked(a.id)); d.reward(Reward("achievement:${a.id}",coins=5,label="Conquista · ${a.name}"))
            }
        }
    }
    suspend fun create(p:Player,starters:List<Habit>)=change("Sua jornada começou"){
        require(p.name.trim().isNotEmpty()){ "Escolha um nome." }; d.player(p.copy(name=p.name.trim().take(40)))
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
    suspend fun player(p:Player)=change("Personagem atualizado"){require(p.name.isNotBlank());d.player(p)}
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
        var r=existing?:HabitRecord("${savedHabit.id}@$date",savedHabit.id,date,0,savedHabit.target,savedHabit.xp,savedHabit.coins,savedHabit.points,savedHabit.attribute,savedHabit.proportional,savedHabit.type,savedHabit.category,savedHabit.unit,savedHabit.name,scheduled(savedHabit,date,d.restDays().map{it.date}.toSet()))
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
    suspend fun startExpedition(id:String,habitIds:List<String>)=change("Expedição iniciada"){
        val s=read();val e=forestExpeditions.firstOrNull{it.id==id}?:error("Expedição desconhecida")
        require(expeditionAvailable(s,e)){"Revele a região e conclua a aventura anterior."}
        require(s.quests.none{it.id==expeditionQuestId(e,0)}){"Esta expedição já foi iniciada."}
        require(habitIds.isNotEmpty()&&habitIds.distinct().size==habitIds.size&&habitIds.all{id->s.habits.any{it.id==id&&!it.deleted&&!it.archived&&!it.paused}}){"Selecione hábitos ativos."}
        d.quest(expeditionPhaseQuest(e,0,habitIds,s))
    }
    suspend fun advanceExpedition(id:String)=change("Marco da floresta reconhecido"){
        val s=read();val e=forestExpeditions.firstOrNull{it.id==id}?:error("Expedição desconhecida")
        require(expeditionAvailable(s,e)){"Uma etapa anterior precisa ser confirmada novamente pelo progresso real."}
        require(!expeditionComplete(s,e)){"A recompensa já foi resgatada."}
        val phase=expeditionPhaseIndex(s,e);val q=requireNotNull(s.quests.find{it.id==expeditionQuestId(e,phase)})
        require(questProgress(q,s)>=q.target){"A etapa ainda está em andamento."}
        val claimId="${q.id}@once"
        if(s.claims.none{it.id==claimId})d.claim(Claim(claimId,q.id,"once",xp=q.xp,coins=q.coins,target=q.target,metric=q.metric,habitIds=q.habitIds,period=2,sagaStart=q.sagaStart,sagaBaseline=q.sagaBaseline))
        if(phase<e.phases.lastIndex){if(s.quests.none{it.id==expeditionQuestId(e,phase+1)})d.quest(expeditionPhaseQuest(e,phase+1,q.habitIds.split(','),s))}
        else if(e.item.isNotBlank()){val item=shopItems.first{it.id==e.item};if(s.assets.none{it.id==item.id})d.asset(Asset(item.id,item.kind))}
    }
    suspend fun widgetComplete(habitId:String,date:String)=change("A chama ganhou força"){
        val s=read();require(date==today()){"O dia mudou. Atualize o widget."}
        val h=requireNotNull(s.habits.find{it.id==habitId}){"Este hábito não está mais disponível. Abra a Jornada."};require(h.type==0&&!h.paused&&!h.archived&&!h.deleted&&scheduled(h,date,s.restDates)){"Registre este hábito no aplicativo."}
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
        require(s.level>=skill.level && skillPoints(s)>0 && (skill.parent==null || s.assets.any{it.id=="skill:${skill.parent}"})){"Faltam pontos, nível ou habilidade anterior."}
        val item=shopItems.first{it.id==skill.item}
        d.asset(Asset("skill:$id","skill"));d.asset(Asset(item.id,item.kind))
    }
    suspend fun buy(item:ShopItem)=change("${item.name} chegou ao inventário"){
        require(shopItems.any{it==item} && item.id!="mist-clasp" && skillDefs.none{it.item==item.id}){"Este item é obtido por habilidade."};val s=read();require(s.assets.none{it.id==item.id}){"Você já possui este item."};require(s.rawCoins>=item.cost){"Fragmentos insuficientes."}
        d.reward(Reward("buy:${item.id}",coins=-item.cost.toLong(),label="Compra · ${item.name}"));d.asset(Asset(item.id,item.kind))
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
    suspend fun startFocus(h:Habit,minutes:Int)=change("Sessão iniciada"){
        val savedHabit=requireNotNull(d.habits().find{it.id==h.id}){"Este hábito não está mais disponível."}
        require(savedHabit.type==2 && !savedHabit.deleted && !savedHabit.archived && !savedHabit.paused && minutes in 1..1440){"Escolha um hábito cronometrado ativo e duração de 1 a 1440 minutos."}
        require(d.focus()==null){"Encerre a sessão atual antes de iniciar outra."}
        val date=today();val r=d.records().find{it.habitId==savedHabit.id && it.date==date}
        d.focus(Focus(habitId=savedHabit.id,date=date,duration=minutes,startedAt=System.currentTimeMillis(),baseline=r?.progress?:0))
    }
    suspend fun pauseFocus()=change("Foco atualizado"){
        val f=d.focus()?:return@change;d.focus(f.copy(elapsed=f.seconds(),startedAt=if(f.startedAt==0L) System.currentTimeMillis() else 0))
    }
    suspend fun endFocus(register:Boolean)=change(if(register) "Tempo efetivo registrado" else "Sessão descartada"){
        val f=d.focus()?:return@change
        if(register){
            val h=d.habits().find{it.id==f.habitId}?:error("Hábito não encontrado")
            val existing=d.records().find{it.habitId==f.habitId && it.date==f.date}
            val r=existing?:HabitRecord("${h.id}@${f.date}",h.id,f.date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name,scheduled(h,f.date,d.restDays().map{it.date}.toSet()))
            // Absolute baseline makes overlapping manual progress idempotent.
            val next=maxOf(r.progress,f.baseline+(f.seconds()/60).toInt()).coerceAtMost(r.target)
            d.record(r.copy(progress=next))
            d.reward(Reward("habit:${r.id}",Progression.reward(r.xp,next,r.target,r.proportional),Progression.reward(r.coins,next,r.target,r.proportional),Progression.reward(r.points,next,r.target,r.proportional),r.attribute,r.date,"Hábito · ${r.name}"))
        };d.clearFocus()
    }
    suspend fun importState(s:GameState)=change("Backup restaurado"){
        validateState(s)
        d.clearRestDays();d.clearFocus();d.clearRecords();d.clearHabits();d.clearLedger();d.clearQuests();d.clearClaims();d.clearAssets();d.clearAchievements();d.clearJournal();d.clearRoutines();d.clearPlayer()
        s.restDays.forEach{d.restDay(it)}
        s.player?.let{d.player(it)};s.habits.forEach{d.habit(it)};s.records.forEach{d.record(it)};s.ledger.forEach{d.reward(it)};s.quests.forEach{d.quest(it)};s.claims.forEach{d.claim(it)};s.assets.forEach{d.asset(it)};s.achievements.forEach{d.unlock(it)};s.journal.forEach{d.journal(it)};s.routines.forEach{d.routine(it)};s.focus?.let{d.focus(it.copy(elapsed=it.seconds(),startedAt=0))}
    }
}
fun validateHabitInput(h:Habit){
        LocalDate.parse(h.created)
        require(h.name.isNotBlank() && h.target in 1..1000000 && h.xp in 0..1000000 && h.coins in 0..1000000 && h.points in 0..1000000){"Revise o nome, a meta e as recompensas (0 a 1.000.000)."}
        require(h.attribute in 0..4 && h.type in 0..3 && h.days.split(',').all{it.toIntOrNull() in 1..7}){"Selecione ao menos um dia."}
        require(Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(h.time)){"Use um horário como 08:00."}
    require(h.difficulty in 0..2 && h.icon in glyphs.indices && h.color in palette.indices){"Revise a dificuldade, o ícone e a cor."}
    require(h.templateId.length<=100 && h.narrativeName.length<=200){"Nome narrativo muito longo."}
}
data class BackupEnvelope(val format:String="ASCEND",val version:Int=7,val sha256:String,val payload:String)
object Backup {
    private val gson=Gson()
    private fun digest(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
    fun encode(s:GameState):String {val data=gson.toJson(s);return gson.toJson(BackupEnvelope(sha256=digest(data),payload=data))}
    fun decode(text:String):GameState {
        require(text.length<=20_000_000){"Backup muito grande (máximo 20 MB)."}
        val b=gson.fromJson(text,BackupEnvelope::class.java)
        require(b.format=="ASCEND" && b.version in 1..7 && b.sha256==digest(b.payload)){"Backup inválido, corrompido ou de versão incompatível."}
        val json=com.google.gson.JsonParser.parseString(b.payload).asJsonObject
        listOf("quests","claims").forEach{table->json.getAsJsonArray(table)?.forEach{row->listOf("sagaStart","sagaBaseline").forEach{key->if(!row.asJsonObject.has(key))row.asJsonObject.addProperty(key,"")}}}
        json.getAsJsonArray("quests")?.forEach{row->val obj=row.asJsonObject;if(!obj.has("sagaState"))obj.addProperty("sagaState",0);if(!obj.has("sagaEnd"))obj.addProperty("sagaEnd","");if(!obj.has("sagaStopped"))obj.addProperty("sagaStopped",0)}
        json.getAsJsonArray("assets")?.forEach{row->listOf("memorySpeaker","memoryText").forEach{key->if(!row.asJsonObject.has(key))row.asJsonObject.addProperty(key,"")}}
        val s=gson.fromJson(json,GameState::class.java);validateState(s);return s
    }
}
fun validateState(s:GameState){
    s.habits.forEach{require(it.templateId.length<=100 && it.narrativeName.length<=200)}
    val p=requireNotNull(s.player){"Backup sem personagem."}; require(p.id==1 && p.name.isNotBlank() && p.heroClass in 0..4 && p.color in 0..4 && p.face in 0..2 && p.outfit in 0..2 && p.frame in 0..2)
    LocalDate.parse(p.created)
    require(s.habits.size<=10000 && s.records.size<=200000 && s.ledger.size<=300000)
    fun distinct(ids:List<String>)=require(ids.all{it.isNotBlank()} && ids.distinct().size==ids.size)
    distinct(s.habits.map{it.id});distinct(s.records.map{it.id});distinct(s.ledger.map{it.id});distinct(s.quests.map{it.id});distinct(s.claims.map{it.id});distinct(s.assets.map{it.id});distinct(s.achievements.map{it.id});distinct(s.journal.map{it.id});distinct(s.routines.map{it.id})
    require(s.restDays.size<=36600)
    distinct(s.restDays.map{it.date});s.restDays.forEach{LocalDate.parse(it.date)}
    s.habits.forEach{h->require(h.name.isNotBlank() && h.target in 1..1000000 && h.attribute in 0..4 && h.color in 0..4 && h.icon in glyphs.indices && h.type in 0..3 && h.xp in 0..1000000 && h.coins in 0..1000000 && h.points in 0..1000000 && h.days.split(',').all{it.toIntOrNull() in 1..7});LocalDate.parse(h.created);LocalTime.parse(h.time)
        if(h.scheduleHistory.isNotBlank())h.scheduleHistory.split(';').forEach{entry->val parts=entry.split('|');require(parts.size==3 && parts[1].split(',').all{it.toIntOrNull() in 1..7} && parts[2].toBooleanStrictOrNull()!=null);LocalDate.parse(parts[0])}
    }
    s.records.forEach{r->require(s.habits.any{it.id==r.habitId} && r.id=="${r.habitId}@${r.date}" && r.target in 1..1000000 && r.progress in 0..r.target && r.attribute in 0..4 && r.type in 0..3 && r.xp in 0..1000000 && r.coins in 0..1000000 && r.points in 0..1000000 && r.date<=today());LocalDate.parse(r.date)}
    s.ledger.forEach{require(kotlin.math.abs(it.xp)<=1000000000L && kotlin.math.abs(it.coins)<=1000000000L && kotlin.math.abs(it.points)<=1000000000L && it.attribute in -1..4);LocalDate.parse(it.date)}
    s.quests.filter{it.sagaStart.isNotBlank()}.forEach{validateSaga(it,s);LocalDate.parse(it.sagaStart);parseBaseline(it.sagaBaseline)}
    s.quests.forEach{require(it.sagaState in 0..3 && it.sagaStopped>=0);if(it.sagaEnd.isNotBlank())LocalDate.parse(it.sagaEnd);require(it.attribute in -1..4 && it.name.isNotBlank() && it.target in 1..1000000 && it.xp in 0..1000000 && it.coins in 0..1000000 && it.period in 0..3 && it.metric in 0..3);if(it.deadline.isNotEmpty())LocalDate.parse(it.deadline)}
    s.assets.forEach{a->
        require(a.memorySpeaker.length<=200 && a.memoryText.length<=10000)
        require(a.kind in setOf("building","outfit","frame","background","companion","decoration","trophy","head","accessory","weapon","skill","memory"))
        if(a.kind=="building") require(a.id.removePrefix("building:").toIntOrNull() in buildings.indices && a.stage in 1..2)
        else if(a.kind=="memory") require(a.id.startsWith("memory:") && a.id.length<=250)
        else if(a.kind=="skill") require(skillDefs.any{"skill:${it.id}"==a.id})
        else if(a.kind!="trophy") require(shopItems.any{it.id==a.id && it.kind==a.kind})
        LocalDate.parse(a.date)
    }
    require(s.assets.filter{it.equipped}.groupBy{it.kind}.all{it.value.size<=1})
    s.achievements.forEach{a->require(achievementDefs.any{it.id==a.id});LocalDate.parse(a.date)}
    s.journal.forEach{require(it.title.length<=100000 && it.text.length<=1000000);LocalDate.parse(it.date)}
    s.routines.forEach{r->require(r.name.isNotBlank() && r.habitIds.split(',').all{id->s.habits.any{it.id==id}});LocalTime.parse(r.time)}
    s.claims.forEach{c->if(c.sagaStart.isNotBlank()){LocalDate.parse(c.sagaStart);parseBaseline(c.sagaBaseline)};require(s.quests.any{it.id==c.questId} && c.id=="${c.questId}@${c.occurrence}" && c.xp in 0..1000000 && c.coins in 0..1000000 && c.target in 1..1000000 && c.metric in 0..3 && c.period in 0..3 && c.attribute in -1..4);LocalDate.parse(c.date);if(c.period!=2)LocalDate.parse(c.occurrence)}
    s.focus?.let{f->
        LocalDate.parse(f.date)
        require(f.id==1 && s.habits.any{it.id==f.habitId && f.date>=it.created} && f.date<=today() && f.duration in 1..1440 && f.elapsed in 0..f.duration*60L && f.startedAt>=0 && f.baseline in 0..1000000){"Sessão de foco inválida no backup."}
    }
}
class GameViewModel(app:Application):AndroidViewModel(app){
    val repo=(app as AscendApp).repository
    val state=repo.state.asStateFlow()
    val error=MutableStateFlow<String?>(null)
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
