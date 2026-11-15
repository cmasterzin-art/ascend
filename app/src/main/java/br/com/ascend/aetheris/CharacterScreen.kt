package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import java.time.*

@Composable fun CharacterScreen(s:GameState,vm:GameViewModel,onSettings:()->Unit,onJournal:()->Unit){
    var tab by remember{mutableIntStateOf(0)};var playerEdit by remember{mutableStateOf(false)}
    Page{
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Title("Seu aventureiro","O que você faz encontra forma aqui.");IconButton(onSettings){Icon(Icons.Default.Settings,"Configurações",tint=Gold)}}
        ChipRow(listOf("Perfil","Atributos","Inventário","Loja","Conquistas","Estatísticas"),tab,{tab=it})
        when(tab){
            0->{Panel{Box(Modifier.fillMaxWidth().background(if(s.assets.any{it.id=="night"&&it.equipped})Violet.copy(alpha=.12f) else Color.Transparent),contentAlignment=Alignment.Center){Avatar(s.player!!,s,160.dp)};Text(s.player!!.name,style=MaterialTheme.typography.headlineMedium);Eyebrow("${classes[s.player.heroClass]} · ${s.player.title}");Text(s.player.goals,color=Muted);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Stat("${s.level}","NÍVEL");Stat("${s.xp}","XP TOTAL",Blue);Stat("${s.coins}","FRAGMENTOS")};Action("Personalizar personagem",{playerEdit=true})};s.assets.find{it.kind=="companion"&&it.equipped}?.let{a->Panel(accent=Violet){val item=shopItems.find{it.id==a.id};Eyebrow("Companheiro",Violet);Text(item?.name?:"Companheiro",style=MaterialTheme.typography.titleLarge);Text(when(a.id){"lyra"->"Lyra se acomoda ao lado do seu caderno. Parece aprovar a página de hoje.";"corvus"->"Corvus observa o horizonte. Há sempre outra trilha entre as árvores.";else->"Ignis protege uma pequena brasa sob as asas. Por enquanto, isso basta."},color=Muted)}};Action("Abrir diário do aventureiro",onJournal,Modifier.fillMaxWidth());Panel{Eyebrow("Títulos conquistados");val titles=listOf(1 to "O Desperto",5 to "Aprendiz Errante",10 to "Aventureiro",15 to "Guardião do Conhecimento",20 to "Mestre da Vontade",30 to "Herói de Aetheris",50 to "Lenda Ascendente");titles.filter{it.first<=s.level}.forEach{(_,title)->TextButton({vm.run{vm.repo.player(s.player!!.copy(title=title))}}){Text(if(s.player!!.title==title)"✦ $title" else title)}}}}
            1->{attributes.forEachIndexed{i,a->val pts=s.points(i);Panel(accent=Color(palette[i])){Eyebrow("Atributo · nível ${1+pts/50}",Color(palette[i]));Text(a,style=MaterialTheme.typography.titleLarge);Meter((pts%50)/50f,Color(palette[i]));Text("${pts%50}/50 pontos neste nível · $pts acumulados",color=Muted,fontSize=12.sp);s.ledger.filter{it.attribute==i && it.points!=0L}.sortedByDescending{it.date}.take(8).forEach{Text("${it.date} · ${it.label.removePrefix("Hábito · ")} · +${it.points}",color=Muted,fontSize=11.sp)}}}}
            2->{val assets=s.assets.filter{it.kind!="building"};if(assets.isEmpty())Empty("Seu inventário aguarda as primeiras descobertas. Fragmentos de hábitos e missões podem ser usados na loja.");assets.forEach{a->val item=shopItems.find{it.id==a.id};Panel(accent=if(a.equipped)Green else Gold){Text("${item?.symbol?:"✦"} ${item?.name?:"Troféu de missão"}",style=MaterialTheme.typography.titleLarge);Text(item?.description?:s.claims.find{"quest:${it.id}"==a.id}?.let{c->s.quests.find{it.id==c.questId}?.special}?:"Uma lembrança da sua jornada.",color=Muted,fontSize=13.sp);if(a.kind!="trophy")Action(if(a.equipped)"Remover" else "Equipar",{vm.run{vm.repo.equip(a)}})}}}
            3->{Eyebrow("◈ ${s.coins} Fragmentos de Aether");Text("Conquistados na vida real. Gastos somente em Aetheris.",color=Muted,fontSize=13.sp);shopItems.forEach{item->val owned=s.assets.any{it.id==item.id};Panel{Text("${item.symbol} ${item.name}",style=MaterialTheme.typography.titleLarge);Text(item.description,color=Muted,fontSize=13.sp);Action(if(owned)"No inventário" else "Adquirir · ◈ ${item.cost}",{vm.run{vm.repo.buy(item)}},enabled=!owned&&s.coins>=item.cost)}}}
            4->{achievementDefs.forEach{a->val unlocked=s.achievements.find{it.id==a.id};val value=achievementProgress(a,s);val color=when(a.rarity){"Lendária"->Gold;"Épica"->Violet;"Rara"->Blue;else->Muted};Panel(accent=color){Eyebrow("${a.rarity} · ${if(unlocked!=null)"Conquistada" else "Em busca"}",color);Text("${a.icon} ${a.name}",style=MaterialTheme.typography.titleLarge);Text(a.description,color=Muted,fontSize=13.sp);Meter(value.toFloat()/a.target,color);Text(unlocked?.let{"Registrada em ${it.date} · ◈ 5"}?:"${value.coerceAtMost(a.target)}/${a.target}",color=color,fontSize=12.sp)}}}
            5->Statistics(s)
        }
    }
    if(playerEdit)PlayerEditor(s.player!!,{playerEdit=false},{p->vm.run{vm.repo.player(p);playerEdit=false}})
}
@Composable fun PlayerEditor(p:Player,onDismiss:()->Unit,onSave:(Player)->Unit){
    var name by remember{mutableStateOf(p.name)};var cls by remember{mutableIntStateOf(p.heroClass)};var face by remember{mutableIntStateOf(p.face)};var color by remember{mutableIntStateOf(p.color)};var outfit by remember{mutableIntStateOf(p.outfit)};var frame by remember{mutableIntStateOf(p.frame)};var goals by remember{mutableStateOf(p.goals)}
    val next=p.copy(name=name,heroClass=cls,face=face,color=color,outfit=outfit,frame=frame,goals=goals)
    FullForm("Personalizar",onDismiss,{onSave(next)}){Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Avatar(next,size=130.dp)};FormField(name,{name=it.take(40)},"Nome");ChipRow(classes,cls,{cls=it});ChipRow(listOf("Pele clara","Pele escura","Pele média"),face,{face=it});ChipRow(listOf("Ouro","Azul","Violeta","Verde","Coral"),color,{color=it});ChipRow(listOf("Túnica","Veste de viagem","Cerimonial"),outfit,{outfit=it});ChipRow(listOf("Simples","Solar","Estrelada"),frame,{frame=it});FormField(goals,{goals=it},"Seus propósitos",multi=true)}
}
@Composable fun Statistics(s:GameState){
    var range by remember{mutableIntStateOf(0)};ChipRow(listOf("7 dias","30 dias","90 dias","Tudo"),range,{range=it})
    val days=when(range){0->7;1->30;2->90;else->(java.time.temporal.ChronoUnit.DAYS.between(LocalDate.parse(s.player!!.created),LocalDate.now()).toInt()+1).coerceAtLeast(1)}
    val start=LocalDate.now().minusDays(days-1L).toString();val rs=s.records.filter{it.date>=start && it.progress>0};val earned=s.ledger.filter{it.date>=start}.sumOf{it.xp};val completed=rs.count{it.complete}
    var planned=0;for(n in 0 until days.coerceAtMost(36600)){planned+=s.habits.count{scheduled(it,LocalDate.now().minusDays(n.toLong()).toString())}}
    Panel(accent=Blue){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Stat("$earned","XP NO PERÍODO",Blue);Stat("$completed","CONCLUSÕES",Green);Stat(if(planned==0)"—" else "${(100*completed/planned).coerceAtMost(100)}%","TAXA",Gold)};Text("Taxa considera os dias programados e as pausas registradas. Dias de descanso não entram no total de metas.",color=Muted,fontSize=11.sp);Text("Foco de Intelecto: ${rs.filter{it.type==2&&it.attribute==0}.sumOf{it.progress}/60}h ${rs.filter{it.type==2&&it.attribute==0}.sumOf{it.progress}%60}min",color=Muted);Text("Maior sequência atual: ${s.habits.maxOfOrNull{streak(it,s.records)}?:0} dias programados",color=Muted)}
    val buckets=if(days<=7)7 else if(days<=30)Math.min(days,30) else 12
    val width=if(days<=30)1 else kotlin.math.ceil(days.toDouble()/buckets).toInt()
    val values=(0 until buckets).map{i->val from=LocalDate.parse(start).plusDays(i.toLong()*width);val to=from.plusDays(width-1L);s.ledger.filter{it.date>=from.toString()&&it.date<=to.toString()}.sumOf{it.xp}.toFloat()}
    Panel(accent=Blue){Eyebrow(if(days<=30)"XP por dia" else "XP por intervalo de $width dias",Blue);Canvas(Modifier.fillMaxWidth().height(130.dp)){val max=(values.maxOrNull()?:1f).coerceAtLeast(1f);val step=size.width/values.size;values.forEachIndexed{i,v->drawRoundRect(Blue.copy(alpha=.8f),Offset(i*step+2,size.height-(v/max*size.height).coerceAtLeast(1f)),Size((step-4).coerceAtLeast(2f),(v/max*size.height).coerceAtLeast(1f)),CornerRadius(3f))}};Text("$start → ${today()}",color=Muted,fontSize=11.sp)}
    Panel(accent=Green){Eyebrow("Calendário de consistência",Green);val count=days.coerceAtMost(91);for(row in 0 until (count+12)/13){Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){for(col in 0..12){val n=row*13+col;val d=LocalDate.now().minusDays((count-1-n).toLong()).toString();val c=rs.count{it.date==d&&it.complete};Box(Modifier.weight(1f).aspectRatio(1f).background(if(n>=count)Color.Transparent else if(c==0)CardColor else Green.copy(alpha=(.2f+c*.15f).coerceAtMost(1f)),androidx.compose.foundation.shape.RoundedCornerShape(3.dp)))}}};Text("Últimos ${count} dias · mais claro = mais conclusões",color=Muted,fontSize=11.sp)}
    Panel(accent=Violet){Eyebrow("XP por semana e mês",Violet);val weeks=s.ledger.filter{it.date>=start}.groupBy{weekOf(it.date)};weeks.toSortedMap().forEach{(w,rows)->Text("Semana $w · ${rows.sumOf{it.xp}} XP",color=Muted,fontSize=12.sp)};val months=s.ledger.filter{it.date>=start}.groupBy{it.date.take(7)};months.toSortedMap().forEach{(m,rows)->Text("Mês $m · ${rows.sumOf{it.xp}} XP",color=Gold,fontSize=12.sp)}}
    Panel{Eyebrow("Atividade por categoria");rs.groupBy{it.category}.forEach{(name,rows)->Text("$name · ${rows.count{it.complete}} conclusões · ${rows.sumOf{Progression.reward(it.xp,it.progress,it.target,it.proportional)}} XP",color=Muted,fontSize=13.sp)}}
}
@Composable fun JournalScreen(s:GameState,vm:GameViewModel){
    var search by remember{mutableStateOf("")};var editor by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<Journal?>(null)};var deleting by remember{mutableStateOf<Journal?>(null)}
    Page{Title("Diário do aventureiro","Nem tudo o que importa cabe em uma barra de XP.");Action("+ Escrever uma página",{editor=true});FormField(search,{search=it},"Pesquisar suas páginas");val entries=s.journal.filter{it.title.contains(search,true)||it.text.contains(search,true)};if(entries.isEmpty())Empty("As páginas estão em silêncio. Escreva quando quiser.");entries.forEach{j->Panel{Eyebrow(j.date);Text(j.title,style=MaterialTheme.typography.titleLarge);Text(j.text,fontFamily=FontFamily.Serif,fontSize=17.sp,lineHeight=26.sp);Row{TextButton({editing=j;editor=true}){Text("Editar")};TextButton({deleting=j}){Text("Excluir",color=Muted)}}}}}
    if(editor)JournalEditor(editing,{editor=false;editing=null},{j->vm.run{vm.repo.journal(j);editor=false;editing=null}})
    deleting?.let{j->AlertDialog(onDismissRequest={deleting=null},title={Text("Excluir esta página?")},text={Text("A página será removida do diário local.")},confirmButton={TextButton({vm.run{vm.repo.deleteJournal(j.id)};deleting=null}){Text("Excluir")}},dismissButton={TextButton({deleting=null}){Text("Cancelar")}})}
}
