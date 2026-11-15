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
    val database by lazy { Room.databaseBuilder(this,GameDatabase::class.java,"ascend.db").build() }
    val repository by lazy { GameRepository(database) }
}
data class GameEvent(val message:String,val level:Int=0,val xp:Long=0)
class GameRepository(private val db:GameDatabase){
    private val d=db.dao(); private val lock=Mutex()
    val state=MutableStateFlow<GameState?>(null)
    val events=MutableSharedFlow<GameEvent>(extraBufferCapacity=10)
    private suspend fun read()=GameState(d.player(),d.habits(),d.records(),d.ledger(),d.quests(),d.claims(),d.assets(),d.achievements(),d.journal(),d.routines(),d.focus())
    suspend fun refresh()=lock.withLock { state.value=read() }
    private suspend fun change(message:String="Salvo",block:suspend ()->Unit){
        lock.withLock {
            val old=state.value?:read()
            db.withTransaction { block(); reconcile() }
            val next=read(); state.value=next
            events.emit(GameEvent(message,if(next.level>old.level) next.level else 0,(next.xp-old.xp).coerceAtLeast(0)))
        }
    }
    private suspend fun reconcile(){
        var s=read()
        // Claims are re-evaluated against habit records, never against their own rewards.
        s.claims.forEach { claim ->
            val q=s.quests.find{it.id==claim.questId}?:return@forEach
            val historical=q.copy(xp=claim.xp,coins=claim.coins,target=claim.target,metric=claim.metric,habitIds=claim.habitIds,period=claim.period,attribute=claim.attribute,deadline=claim.deadline)
            val valid=questProgress(historical,s,claim.occurrence)>=historical.target
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
        require(h.name.isNotBlank() && h.target in 1..1000000 && h.xp in 0..1000000 && h.coins in 0..1000000 && h.points in 0..1000000){"Revise o nome, a meta e as recompensas (0 a 1.000.000)."}
        require(h.attribute in 0..4 && h.type in 0..3 && h.days.split(',').all{it.toIntOrNull() in 1..7}){"Selecione ao menos um dia."}
        require(Regex("([01]\\d|2[0-3]):[0-5]\\d").matches(h.time)){"Use um horário como 08:00."}
        val old=d.habits().find{it.id==h.id}
        val inactive=h.paused||h.archived||h.deleted
        val history=if(old==null) "${h.created}|${h.days}|$inactive" else if(old.days!=h.days || old.paused!=h.paused || old.archived!=h.archived || old.deleted!=h.deleted){
            val previous=old.scheduleHistory.ifBlank{"${old.created}|${old.days}|${old.paused||old.archived||old.deleted}"}
            (previous.split(';').filter{!it.startsWith(today()+"|")}+"${today()}|${h.days}|$inactive").joinToString(";")
        }else old.scheduleHistory
        d.habit(h.copy(scheduleHistory=history))
    }
    suspend fun reorder(h:Habit,step:Int)=change("Ordem atualizada"){
        val hs=d.habits().filter{!it.deleted && !it.archived}.sortedBy{it.position}.toMutableList();val i=hs.indexOfFirst{it.id==h.id};val j=(i+step).coerceIn(0,(hs.size-1).coerceAtLeast(0))
        if(i>=0){ java.util.Collections.swap(hs,i,j); hs.forEachIndexed{n,v->d.habit(v.copy(position=n))} }
    }
    suspend fun progress(h:Habit,date:String,amount:Int,correction:HabitRecord?=null)=change(if(amount>=h.target) "A chama ganhou força" else "Progresso registrado"){
        require(date<=today() && date>=h.created){"Escolha uma data entre a criação do hábito e hoje."}
        val existing=d.records().find{it.habitId==h.id && it.date==date}
        var r=existing?:HabitRecord("${h.id}@$date",h.id,date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name,scheduled(h,date))
        if(correction!=null) {
            require(correction.id==r.id && correction.target>0 && correction.xp in 0..1000000 && correction.coins in 0..1000000 && correction.points in 0..1000000){"Correção inválida."}
            r=r.copy(xp=correction.xp,coins=correction.coins,points=correction.points)
        }
        r=r.copy(progress=amount.coerceIn(0,r.target)); d.record(r)
        d.reward(Reward("habit:${r.id}",Progression.reward(r.xp,r.progress,r.target,r.proportional),Progression.reward(r.coins,r.progress,r.target,r.proportional),Progression.reward(r.points,r.progress,r.target,r.proportional),r.attribute,date,"Hábito · ${r.name}"))
    }
    suspend fun claim(q:Quest)=change("Recompensa resgatada"){
        val s=read();val occ=occurrence(q);val id="${q.id}@$occ"
        require(s.claims.none{it.id==id}){"Esta recompensa já foi resgatada."}
        require(questProgress(q,s)>=q.target){"A missão ainda está em andamento."}
        d.claim(Claim(id,q.id,occ,xp=q.xp,coins=q.coins,target=q.target,metric=q.metric,habitIds=q.habitIds,period=q.period,attribute=q.attribute,deadline=q.deadline))
        if(q.special.isNotBlank()) d.asset(Asset("quest:$id","trophy"))
    }
    suspend fun quest(q:Quest)=change("Missão salva"){
        require(q.name.isNotBlank() && q.target in 1..1000000 && q.xp in 0..1000000 && q.coins in 0..1000000){"Revise os valores da missão."}
        if(q.deadline.isNotEmpty()) LocalDate.parse(q.deadline)
        d.quest(q)
    }
    suspend fun buy(item:ShopItem)=change("${item.name} chegou ao inventário"){
        val s=read();require(s.assets.none{it.id==item.id}){"Você já possui este item."};require(s.rawCoins>=item.cost){"Fragmentos insuficientes."}
        d.reward(Reward("buy:${item.id}",coins=-item.cost.toLong(),label="Compra · ${item.name}"));d.asset(Asset(item.id,item.kind))
    }
    suspend fun equip(a:Asset)=change(if(a.equipped) "Item removido" else "Item equipado"){
        val s=read();require(s.assets.any{it.id==a.id})
        s.assets.filter{it.kind==a.kind}.forEach{d.asset(it.copy(equipped=false))};d.asset(a.copy(equipped=!a.equipped))
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
    suspend fun startFocus(h:Habit,minutes:Int)=change("Sessão iniciada"){
        require(h.type==2 && minutes in 1..1440){"O foco precisa de um hábito cronometrado e duração de 1 a 1440 minutos."}
        require(d.focus()==null){"Encerre a sessão atual antes de iniciar outra."}
        val r=d.records().find{it.habitId==h.id && it.date==today()}; d.focus(Focus(habitId=h.id,duration=minutes,startedAt=System.currentTimeMillis(),baseline=r?.progress?:0))
    }
    suspend fun pauseFocus()=change("Foco atualizado"){
        val f=d.focus()?:return@change;d.focus(f.copy(elapsed=f.seconds(),startedAt=if(f.startedAt==0L) System.currentTimeMillis() else 0))
    }
    suspend fun endFocus(register:Boolean)=change(if(register) "Tempo efetivo registrado" else "Sessão descartada"){
        val f=d.focus()?:return@change
        if(register){
            val h=d.habits().find{it.id==f.habitId}?:error("Hábito não encontrado")
            val existing=d.records().find{it.habitId==f.habitId && it.date==f.date}
            val r=existing?:HabitRecord("${h.id}@${f.date}",h.id,f.date,0,h.target,h.xp,h.coins,h.points,h.attribute,h.proportional,h.type,h.category,h.unit,h.name,scheduled(h,f.date))
            // Absolute baseline makes overlapping manual progress idempotent.
            val next=maxOf(r.progress,f.baseline+(f.seconds()/60).toInt()).coerceAtMost(r.target)
            d.record(r.copy(progress=next))
            d.reward(Reward("habit:${r.id}",Progression.reward(r.xp,next,r.target,r.proportional),Progression.reward(r.coins,next,r.target,r.proportional),Progression.reward(r.points,next,r.target,r.proportional),r.attribute,r.date,"Hábito · ${r.name}"))
        };d.clearFocus()
    }
    suspend fun importState(s:GameState)=change("Backup restaurado"){
        validateState(s)
        d.clearFocus();d.clearRecords();d.clearHabits();d.clearLedger();d.clearQuests();d.clearClaims();d.clearAssets();d.clearAchievements();d.clearJournal();d.clearRoutines();d.clearPlayer()
        s.player?.let{d.player(it)};s.habits.forEach{d.habit(it)};s.records.forEach{d.record(it)};s.ledger.forEach{d.reward(it)};s.quests.forEach{d.quest(it)};s.claims.forEach{d.claim(it)};s.assets.forEach{d.asset(it)};s.achievements.forEach{d.unlock(it)};s.journal.forEach{d.journal(it)};s.routines.forEach{d.routine(it)};s.focus?.let{d.focus(it.copy(elapsed=it.seconds(),startedAt=0))}
    }
}
data class BackupEnvelope(val format:String="ASCEND",val version:Int=1,val sha256:String,val payload:String)
object Backup {
    private val gson=Gson()
    private fun digest(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8)).joinToString(""){"%02x".format(it)}
    fun encode(s:GameState):String {val data=gson.toJson(s);return gson.toJson(BackupEnvelope(sha256=digest(data),payload=data))}
    fun decode(text:String):GameState {
        require(text.length<=20_000_000){"Backup muito grande (máximo 20 MB)."}
        val b=gson.fromJson(text,BackupEnvelope::class.java)
        require(b.format=="ASCEND" && b.version==1 && b.sha256==digest(b.payload)){"Backup inválido, corrompido ou de versão incompatível."}
        val s=gson.fromJson(b.payload,GameState::class.java);validateState(s);return s
    }
}
fun validateState(s:GameState){
    val p=requireNotNull(s.player){"Backup sem personagem."}; require(p.id==1 && p.name.isNotBlank() && p.heroClass in 0..4 && p.color in 0..4 && p.face in 0..2 && p.outfit in 0..2 && p.frame in 0..2)
    require(s.habits.size<=10000 && s.records.size<=200000 && s.ledger.size<=300000)
    fun distinct(ids:List<String>)=require(ids.all{it.isNotBlank()} && ids.distinct().size==ids.size)
    distinct(s.habits.map{it.id});distinct(s.records.map{it.id});distinct(s.ledger.map{it.id});distinct(s.quests.map{it.id});distinct(s.claims.map{it.id});distinct(s.assets.map{it.id});distinct(s.achievements.map{it.id});distinct(s.journal.map{it.id});distinct(s.routines.map{it.id})
    s.habits.forEach{h->require(h.name.isNotBlank() && h.target in 1..1000000 && h.attribute in 0..4 && h.color in 0..4 && h.icon in glyphs.indices && h.type in 0..3 && h.xp in 0..1000000 && h.coins in 0..1000000 && h.points in 0..1000000 && h.days.split(',').all{it.toIntOrNull() in 1..7});LocalDate.parse(h.created);LocalTime.parse(h.time)
        if(h.scheduleHistory.isNotBlank())h.scheduleHistory.split(';').forEach{entry->val parts=entry.split('|');require(parts.size==3 && parts[1].split(',').all{it.toIntOrNull() in 1..7} && parts[2].toBooleanStrictOrNull()!=null);LocalDate.parse(parts[0])}
    }
    s.records.forEach{r->require(s.habits.any{it.id==r.habitId} && r.id=="${r.habitId}@${r.date}" && r.target in 1..1000000 && r.progress in 0..r.target && r.attribute in 0..4 && r.type in 0..3 && r.xp in 0..1000000 && r.coins in 0..1000000 && r.points in 0..1000000 && r.date<=today());LocalDate.parse(r.date)}
    s.ledger.forEach{require(kotlin.math.abs(it.xp)<=1000000000L && kotlin.math.abs(it.coins)<=1000000000L && kotlin.math.abs(it.points)<=1000000000L && it.attribute in -1..4);LocalDate.parse(it.date)}
    s.quests.forEach{require(it.attribute in -1..4 && it.name.isNotBlank() && it.target in 1..1000000 && it.xp in 0..1000000 && it.coins in 0..1000000 && it.period in 0..3 && it.metric in 0..3);if(it.deadline.isNotEmpty())LocalDate.parse(it.deadline)}
    s.assets.forEach{a->
        require(a.kind in setOf("building","outfit","frame","background","companion","decoration","trophy"))
        if(a.kind=="building") require(a.id.removePrefix("building:").toIntOrNull() in buildings.indices && a.stage in 1..2)
        else if(a.kind!="trophy") require(shopItems.any{it.id==a.id && it.kind==a.kind})
        LocalDate.parse(a.date)
    }
    require(s.assets.filter{it.equipped}.groupBy{it.kind}.all{it.value.size<=1})
    s.achievements.forEach{a->require(achievementDefs.any{it.id==a.id});LocalDate.parse(a.date)}
    s.journal.forEach{require(it.title.length<=100000 && it.text.length<=1000000);LocalDate.parse(it.date)}
    s.routines.forEach{r->require(r.name.isNotBlank() && r.habitIds.split(',').all{id->s.habits.any{it.id==id}});LocalTime.parse(r.time)}
    s.claims.forEach{c->require(s.quests.any{it.id==c.questId} && c.id=="${c.questId}@${c.occurrence}" && c.xp in 0..1000000 && c.coins in 0..1000000 && c.target in 1..1000000 && c.metric in 0..3 && c.period in 0..3 && c.attribute in -1..4);LocalDate.parse(c.date);if(c.period!=2)LocalDate.parse(c.occurrence)}
    s.focus?.let{f->require(s.habits.any{it.id==f.habitId} && f.duration in 1..1440 && f.elapsed>=0 && f.baseline>=0)}
}
class GameViewModel(app:Application):AndroidViewModel(app){
    val repo=(app as AscendApp).repository
    val state=repo.state.asStateFlow()
    val error=MutableStateFlow<String?>(null)
    val relaxed=app.preferences.data.map{it[booleanPreferencesKey("relaxed")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
    val bosses=app.preferences.data.map{it[booleanPreferencesKey("bosses")]?:false}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),false)
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
}
