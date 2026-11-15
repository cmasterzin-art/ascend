package br.com.ascend.aetheris

import androidx.room.*
import java.time.*
import kotlin.math.*

val attributes = listOf("Intelecto", "Vigor", "Disciplina", "Equilíbrio", "Criatividade")
val classes = listOf("Guerreiro", "Mago", "Guardião", "Explorador", "Artífice")
val classDescriptions = listOf("A coragem de continuar, um passo de cada vez.", "Cada descoberta acende uma janela em Aetheris.", "O equilíbrio sustenta aquilo que vale preservar.", "Há caminhos que só existem depois do primeiro passo.", "Das mãos pacientes nascem mundos inteiros.")
val glyphs = listOf("✦", "◆", "☽", "❖", "✧", "⚑", "◈", "✺")
val palette = listOf(0xFFD9AD61, 0xFF7299F4, 0xFFA084E8, 0xFF67BE92, 0xFFE39483)
val weekdays = listOf("S", "T", "Q", "Q", "S", "S", "D")
fun today(): String = LocalDate.now().toString()
fun weekOf(date: String): String = LocalDate.parse(date).with(java.time.DayOfWeek.MONDAY).toString()
fun uid(): String = java.util.UUID.randomUUID().toString()

@Entity(tableName="player")
data class Player(@PrimaryKey val id: Int = 1, val name: String = "", val heroClass: Int = 1, val color: Int = 1, val face: Int = 0, val outfit: Int = 0, val frame: Int = 0, val title: String = "O Desperto", val goals: String = "", val created: String = today())
@Entity(tableName="habits")
data class Habit(@PrimaryKey val id: String = uid(), val name: String = "", val description: String = "", val icon: Int = 0, val color: Int = 1, val category: String = "Estudo", val attribute: Int = 0, val difficulty: Int = 1, val type: Int = 0, val target: Int = 1, val unit: String = "vez", val days: String = "1,2,3,4,5,6,7", val time: String = "08:00", val xp: Int = 50, val coins: Int = 5, val points: Int = 5, val proportional: Boolean = false, val reminder: Boolean = false, val paused: Boolean = false, val archived: Boolean = false, val deleted: Boolean = false, val position: Int = 0, val created: String = today(), val scheduleHistory: String = "")
@Entity(tableName="records", foreignKeys=[ForeignKey(entity=Habit::class,parentColumns=["id"], childColumns=["habitId"], onDelete=ForeignKey.RESTRICT)], indices=[Index("habitId")])
data class HabitRecord(@PrimaryKey val id: String, val habitId: String, val date: String, val progress: Int, val target: Int, val xp: Int, val coins: Int, val points: Int, val attribute: Int, val proportional: Boolean, val type: Int, val category: String, val unit: String, val name: String, val scheduled: Boolean = true) { val complete get() = progress >= target }
@Entity(tableName="ledger")
data class Reward(@PrimaryKey val id: String, val xp: Long = 0, val coins: Long = 0, val points: Long = 0, val attribute: Int = -1, val date: String = today(), val label: String = "")
@Entity(tableName="quests")
data class Quest(@PrimaryKey val id: String = uid(), val name: String, val description: String = "", val period: Int = 2, val metric: Int = 0, val target: Int = 3, val habitIds: String = "", val xp: Int = 50, val coins: Int = 10, val deadline: String = "", val special: String = "", val attribute: Int = -1)
@Entity(tableName="claims")
data class Claim(@PrimaryKey val id: String, val questId: String, val occurrence: String, val date: String = today(), val xp:Int=0, val coins:Int=0, val target:Int=1, val metric:Int=0, val habitIds:String="", val period:Int=2, val attribute:Int = -1, val deadline:String="")
@Entity(tableName="assets")
data class Asset(@PrimaryKey val id: String, val kind: String, val stage: Int = 1, val equipped: Boolean = false, val date: String = today())
@Entity(tableName="achievements")
data class Unlocked(@PrimaryKey val id: String, val date: String = today())
@Entity(tableName="diary")
data class Journal(@PrimaryKey val id: String = uid(), val title: String = "", val text: String = "", val date: String = today())
@Entity(tableName="routines")
data class Routine(@PrimaryKey val id: String = uid(), val name: String, val habitIds: String, val time: String = "08:00")
@Entity(tableName="focus", foreignKeys=[ForeignKey(entity=Habit::class,parentColumns=["id"],childColumns=["habitId"],onDelete=ForeignKey.RESTRICT)],indices=[Index("habitId")])
data class Focus(@PrimaryKey val id: Int = 1, val habitId: String, val date: String = today(), val duration: Int = 25, val elapsed: Long = 0, val startedAt: Long = 0, val baseline: Int = 0) {
    fun seconds(now: Long = System.currentTimeMillis()) = (elapsed + if(startedAt>0) max(0L,(now-startedAt)/1000) else 0).coerceAtMost(duration*60L)
}

data class GameState(val player: Player? = null, val habits: List<Habit> = emptyList(), val records: List<HabitRecord> = emptyList(), val ledger: List<Reward> = emptyList(), val quests: List<Quest> = emptyList(), val claims: List<Claim> = emptyList(), val assets: List<Asset> = emptyList(), val achievements: List<Unlocked> = emptyList(), val journal: List<Journal> = emptyList(), val routines: List<Routine> = emptyList(), val focus: Focus? = null) {
    val xp get() = ledger.sumOf{it.xp}.coerceAtLeast(0)
    val level get() = Progression.level(xp)
    val rawCoins get() = ledger.sumOf{it.coins}
    val coins get() = rawCoins.coerceAtLeast(0)
    val debt get() = (-rawCoins).coerceAtLeast(0)
    fun points(attr: Int) = ledger.filter{it.attribute==attr}.sumOf{it.points}.coerceAtLeast(0)
    fun record(habitId: String, date: String = today()) = records.find{it.habitId==habitId && it.date==date}
    fun due(date: String = today()) = habits.filter{!it.archived && !it.deleted && scheduled(it,date)}.sortedBy{it.position}
}
object Progression {
    fun threshold(level: Int): Long = 25L*(level-1)*(level+2)
    fun requirement(level: Int): Long = 100L + 50L*(level-1)
    fun level(xp: Long): Int { val root = ((sqrt(9.0+4.0*(xp.coerceAtLeast(0)/25.0))-1)/2).toInt().coerceAtLeast(1); var n=root; while(threshold(n+1)<=xp) n++; while(threshold(n)>xp && n>1) n--; return n }
    fun current(xp: Long) = xp-threshold(level(xp))
    fun reward(max: Int, progress: Int, target: Int, proportional: Boolean): Long = if(proportional) max.toLong()*progress.coerceIn(0,target)/target.coerceAtLeast(1) else if(progress>=target) max.toLong() else 0L
}
fun scheduled(h: Habit, date: String): Boolean {
    if(date<h.created) return false
    val entry=h.scheduleHistory.split(';').map{it.split('|')}.filter{it.size==3 && it[0]<=date}.maxByOrNull{it[0]}
    val days=entry?.get(1)?:h.days
    val paused=entry?.get(2)?.toBooleanStrictOrNull()?:h.paused
    return !paused && LocalDate.parse(date).dayOfWeek.value.toString() in days.split(',')
}
fun streak(h: Habit, records: List<HabitRecord>, end: String=today()): Int {
    val dates=records.filter{it.habitId==h.id && it.complete}.map{it.date}.toSet()
    var day=LocalDate.parse(end); var count=0; var skippedToday=false
    repeat(36600) {
        if(day.toString()<h.created) return count
        if(scheduled(h,day.toString())) {
            if(day.toString() in dates) count++ else if(day.toString()==end && !skippedToday) skippedToday=true else return count
        }
        day=day.minusDays(1)
    }; return count
}
fun occurrence(q: Quest,date: String=today()) = when(q.period){0->date; 1,3->weekOf(date); else->"once"}
fun questProgress(q: Quest,s: GameState, occ: String = occurrence(q)): Long {
    val selected=q.habitIds.split(',').filter{it.isNotBlank()}.toSet()
    val rs=s.records.filter{r -> (selected.isEmpty() || r.habitId in selected) && (q.attribute<0 || r.attribute==q.attribute) && when(q.period){0->r.date==occ;1,3->r.date>=occ && r.date<=LocalDate.parse(occ).plusDays(6).toString();else->true} && (q.deadline.isEmpty() || r.date<=q.deadline)}
    return when(q.metric){1->rs.filter{it.type==2}.sumOf{it.progress.toLong()};2->rs.sumOf{Progression.reward(it.xp,it.progress,it.target,it.proportional)};3->rs.filter{it.unit.contains("página",true)}.sumOf{it.progress.toLong()};else->rs.count{it.complete}.toLong()}
}
data class AchievementDef(val id: String, val name: String, val description: String, val rarity: String, val icon: String, val target: Long)
val achievementDefs=listOf(
    AchievementDef("first","O Primeiro Passo","Concluir um hábito.","Comum","✦",1),
    AchievementDef("five","Despertar","Alcançar o nível 5.","Rara","❖",5),
    AchievementDef("study","Mente Incansável","Acumular 20 horas de foco registrado em Intelecto.","Épica","◈",1200),
    AchievementDef("hundred","Guardião da Rotina","Concluir 100 hábitos.","Épica","◆",100),
    AchievementDef("streak","A Chama Persistente","Manter uma sequência de 7 dias programados.","Rara","✺",7),
    AchievementDef("return","O Retorno do Herói","Concluir um hábito após 7 dias sem registros completos.","Rara","☽",1),
    AchievementDef("fifty","Lenda de Aetheris","Alcançar o nível 50.","Lendária","⚑",50),
    AchievementDef("restore","Pedras que Recordam","Restaurar uma construção da vila.","Rara","◇",1))
fun achievementProgress(a: AchievementDef,s: GameState): Long = when(a.id){
    "first","hundred"->s.records.count{it.complete}.toLong()
    "five","fifty"->s.level.toLong()
    "study"->s.records.filter{it.type==2 && it.attribute==0}.sumOf{it.progress.toLong()}
    "streak"->s.habits.maxOfOrNull{streak(it,s.records)}?.toLong()?:0
    "return"->{val ds=s.records.filter{it.complete}.map{LocalDate.parse(it.date)}.distinct().sorted(); if(ds.zipWithNext().any{(x,y)->java.time.temporal.ChronoUnit.DAYS.between(x,y)>=7}) 1 else 0}
    "restore"->s.assets.count{it.kind=="building"}.toLong()
    else->0
}
data class ShopItem(val id:String,val name:String,val kind:String,val cost:Int,val description:String,val symbol:String)
val shopItems=listOf(
    ShopItem("cloak","Manto das Eras","outfit",60,"Um manto violeta tecido com histórias recuperadas.","✧"),
    ShopItem("armor","Armadura da Aurora","outfit",100,"O cobre da aurora acompanha cada novo começo.","◆"),
    ShopItem("frame","Círculo Astral","frame",45,"Uma moldura marcada pelas constelações de Aetheris.","◈"),
    ShopItem("night","Céu de Aetheris","background",70,"Um céu de estrelas para o retrato do aventureiro.","☽"),
    ShopItem("lyra","Lyra · Raposa Espiritual","companion",80,"Conhece o cheiro dos livros que ainda não foram escritos.","♧"),
    ShopItem("corvus","Corvus · Corvo Arcano","companion",100,"Observa os caminhos que a névoa tenta esconder.","✦"),
    ShopItem("ignis","Ignis · Dragão Jovem","companion",150,"Guarda uma brasa para os dias frios.","✺"),
    ShopItem("lantern","Lanterna do Regresso","decoration",30,"Uma luz na praça, à espera dos viajantes.","◇"))
val buildings=listOf("A Biblioteca","A Forja","O Jardim","A Torre do Cronista","A Praça Central")
val regions=listOf("Vila do Despertar","Floresta da Persistência","Biblioteca das Eras","Montanhas da Disciplina","Santuário do Equilíbrio","Fortaleza da Vontade")
val regionLevels=listOf(1,3,5,8,12,20)
val regionDescriptions=listOf("As pedras ainda guardam o calor de uma comunidade. A primeira chama aguarda suas mãos.","Sob a névoa, raízes continuam abrindo caminhos. Aqui, cada retorno deixa uma trilha.","Estantes vazias guardam as marcas de livros que um dia voltarão. O conhecimento reconstrói suas paredes.","Os cumes não pedem pressa. Somente o passo seguinte.","Entre fontes silenciosas, o mundo reaprende a respirar.","A última muralha não foi erguida contra invasores, mas contra o esquecimento.")
val chapterNames=listOf("I · O Despertar","II · Ecos da Estagnação","III · A Primeira Chama","IV · O Caminho Esquecido","V · A Biblioteca das Eras")
val chapterTexts=listOf(
    "Você desperta ao som de um sino que não se move. Na praça, uma figura recolhe nomes apagados de uma placa de pedra.\n\n— Ainda não sei o seu — diz o Cronista. — Mas talvez você possa me ajudar a lembrar os outros.\n\nA vila tem portas sem moradores e uma fonte sem água. Em sua palma, uma brasa pequena ilumina uma única palavra: começar.\n\n— Não procure uma espada — ele acrescenta. — Procure algo que você consiga fazer hoje.",
    "Ao voltar de sua primeira tarefa, você encontra uma janela acesa. Não havia ninguém ali antes.\n\nO Cronista toca a moldura. A madeira deixou de apodrecer.\n\n— Chamamos de Estagnação aquilo que cresce quando ninguém retorna. Ela não vence batalhas. Apenas espera.\n\nVocê pergunta se uma única janela pode mudar alguma coisa. Ele abre o caderno. Na página, surge o desenho de uma casa inteira.\n\n— É assim que sabemos onde recomeçar.",
    "Quando a primeira construção recebe suas últimas pedras, o sino finalmente se move. O som não é alto. Ainda assim, pássaros levantam voo além da névoa.\n\nO Cronista deixa o caderno no chão e ajuda a varrer a entrada. Pela primeira vez, parece menos uma sombra.\n\n— Pensei que estivesse registrando uma ruína — admite. — Talvez estivesse esperando uma história nova.\n\nNa praça, a Chama da Ascensão encontra abrigo. Aetheris não está pronta. Mas agora tem um lugar para receber quem voltar.",
    "Uma trilha aparece além dos portões. No chão, marcas antigas de rodas seguem até a floresta. O Cronista entrega um mapa incompleto.\n\n— Não desenhei o que ainda não atravessamos.\n\nEntre as árvores, pequenas lanternas apagadas indicam uma estrada. Você acende a primeira. A próxima está distante, mas já pode ser vista.",
    "A porta da Biblioteca das Eras se abre sem ruído. Há pó sobre as mesas, mas nenhum sobre os livros.\n\nEm cada lombada, um assunto que alguém abandonou antes de entender. O Cronista escolhe um volume e oferece a você.\n\n— Algumas cidades são reconstruídas com pedra. Esta precisa de perguntas.\n\nVocê abre a primeira página. A sala inteira recebe um pouco de luz.")
fun chapterUnlocked(i:Int,s:GameState)=when(i){0->true;1->s.records.any{it.complete};2->s.assets.any{it.kind=="building"};3->s.level>=3;else->s.level>=5}
