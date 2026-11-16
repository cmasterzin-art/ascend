package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.ui.semantics.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FullForm(title:String,onDismiss:()->Unit,onSave:()->Unit,saveLabel:String="Salvar",content:@Composable ColumnScope.()->Unit){
    Dialog(onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)){
        Scaffold(topBar={TopAppBar(title={Text(title)},navigationIcon={IconButton(onDismiss){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Voltar")}},actions={SagaButton(saveLabel,onSave)})}){padding->Box(Modifier.padding(padding).imePadding()){Page(content)}}
    }
}
@Composable fun HabitEditor(h:Habit,onDismiss:()->Unit,onSave:(Habit)->Unit){
    var narrative by remember{mutableStateOf(h.narrativeName)};var trackStreak by remember{mutableStateOf(h.trackStreak)}
    var name by remember{mutableStateOf(h.name)};var desc by remember{mutableStateOf(h.description)};var category by remember{mutableStateOf(h.category)};var attr by remember{mutableIntStateOf(h.attribute)};var type by remember{mutableIntStateOf(h.type)};var target by remember{mutableStateOf(h.target.toString())};var unit by remember{mutableStateOf(h.unit)}
    var xp by remember{mutableStateOf(h.xp.toString())};var coins by remember{mutableStateOf(h.coins.toString())};var points by remember{mutableStateOf(h.points.toString())};var days by remember{mutableStateOf(h.days.split(',').mapNotNull{it.toIntOrNull()}.toSet())};var time by remember{mutableStateOf(h.time)};var proportional by remember{mutableStateOf(h.proportional)};var reminder by remember{mutableStateOf(h.reminder)};var difficulty by remember{mutableIntStateOf(h.difficulty)};var icon by remember{mutableIntStateOf(h.icon)};var color by remember{mutableIntStateOf(h.color)}
    FullForm(if(h.name.isBlank())"Novo hábito" else if(h.templateId.isNotBlank()&&h.scheduleHistory.isBlank())"Personalizar ritual" else "Editar hábito",onDismiss,{
        onSave(h.copy(name=name.trim(),narrativeName=narrative.trim(),trackStreak=trackStreak,description=desc,category=category,attribute=attr,type=type,target=if(type==0)1 else target.toIntOrNull()?:0,unit=if(type==2)"minutos" else unit,xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,points=points.toIntOrNull()?:-1,days=days.sorted().joinToString(","),time=time,proportional=proportional && type in 1..2,reminder=reminder,difficulty=difficulty,icon=icon,color=color))
    }){
        var advanced by remember{mutableStateOf(h.templateId.isNotBlank())}
        Panel(accent=habitColor(color)){
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
                Text(glyphs[icon],color=habitColor(color),fontSize=36.sp)
                Column{Text(name.ifBlank{"Seu próximo passo"},style=MaterialTheme.typography.titleLarge);Text("+$xp XP · ${attributes[attr]}",color=habitColor(color),fontSize=13.sp)}
            }
        }
        FormField(name,{name=it},"Nome do hábito")
        Text("Como registrar?",color=Muted);ChipRow(listOf("Simples","Quantidade","Tempo","Repetições"),type,{type=it;if(it==2){unit="minutos";if(target=="1")target="60"}})
        if(type!=0){FormField(target,{target=it},if(type==2)"Meta em minutos" else "Meta",number=true);if(type!=2)FormField(unit,{unit=it},"Unidade (páginas, copos, sessões…)")}
        FormField(xp,{xp=it},"XP ao atingir a meta",number=true)
        Text("A recompensa é sua escolha. Sugestões: 15, 30 ou 50 XP.",color=Muted,fontSize=12.sp)
        Text("Atributo associado",color=Muted);ChipRow(attributes,attr,{attr=it})
        Eyebrow("Dias programados")
        Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
            (1..7).toList().chunked(4).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                row.forEach{day->FilterChip(day in days,{days=if(day in days)days-day else days+day},label={Text(listOf("Seg","Ter","Qua","Qui","Sex","Sáb","Dom")[day-1])},modifier=Modifier.weight(1f).heightIn(min=48.dp).semantics{contentDescription=listOf("Segunda-feira","Terça-feira","Quarta-feira","Quinta-feira","Sexta-feira","Sábado","Domingo")[day-1]},shape=androidx.compose.foundation.shape.CutCornerShape(3.dp))};repeat(4-row.size){Spacer(Modifier.weight(1f))}
            }}
        }
        Text("Dias sem seleção são descanso. Não quebram sua sequência.",color=Muted,fontSize=12.sp)
        if(type in 1..2)Toggle("Recompensa proporcional ao progresso",proportional,{proportional=it})
        Toggle("Mais opções de personalização",advanced,{advanced=it})
        if(advanced){
            FormField(desc,{desc=it},"Descrição",multi=true)
            FormField(narrative,{narrative=it.take(200)},"Nome narrativo opcional")
            if(type==0)FormField(unit,{unit=it},"Unidade")
            Toggle("Contar sequências deste hábito",trackStreak,{trackStreak=it})
            Eyebrow("Identidade visual");ChipRow(glyphs,icon,{icon=it});ChipRow(listOf("Ouro","Azul","Violeta","Verde","Coral"),color,{color=it})
            FormField(category,{category=it},"Categoria")
            Text("Dificuldade narrativa",color=Muted);ChipRow(listOf("Leve","Moderada","Desafiadora"),difficulty,{difficulty=it})
            FormField(time,{time=it},"Horário · HH:mm");Toggle("Lembrete local opcional",reminder,{reminder=it})
            FormField(coins,{coins=it},"Fragmentos de Aether",number=true);FormField(points,{points=it},"Pontos de ${attributes[attr]}",number=true)
        }
        Panel(accent=Blue){Text("Alterar recompensas afeta apenas novos registros. O histórico preserva os valores originais. Use a correção de registro no histórico quando necessário.",color=Muted,fontSize=13.sp)}
    }
}
@Composable fun ProgressEditor(h:Habit,r:HabitRecord?,onDismiss:()->Unit,onSave:(String,Int,HabitRecord?)->Unit){
    var date by remember{mutableStateOf(r?.date?:today())};var amount by remember{mutableStateOf((r?.progress?:0).toString())};var correct by remember{mutableStateOf(false)};var xp by remember{mutableStateOf((r?.xp?:h.xp).toString())};var coins by remember{mutableStateOf((r?.coins?:h.coins).toString())};var points by remember{mutableStateOf((r?.points?:h.points).toString())}
    FullForm("Registrar · ${h.name}",onDismiss,{
        val correction=if(correct&&r!=null) r.copy(xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,points=points.toIntOrNull()?:-1) else null
        onSave(date,amount.toIntOrNull()?:0,correction)
    }){
        FormField(date,{date=it},"Data · AAAA-MM-DD")
        Text("Meta deste registro: ${r?.target?:h.target} ${r?.unit?:h.unit}",color=Gold)
        FormField(amount,{amount=it},"Progresso total nesta data",number=true)
        if(h.type!=0)Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            (if(h.type==2)listOf(5,15,25) else listOf(1,5,10)).forEach{step->OutlinedButton({amount=((amount.toIntOrNull()?:0).coerceAtLeast(0).toLong()+step).coerceAtMost((r?.target?:h.target).toLong()).toString()}){Text("+$step")}}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton({amount="0"}){Text("Desfazer")};Action("Atingir meta",{amount=(r?.target?:h.target).toString()})}
        Text("Informe o total realizado no dia. O aplicativo concede apenas a diferença de recompensa.",color=Muted,fontSize=13.sp)
        if(r!=null){Toggle("Corrigir recompensas deste registro",correct,{correct=it});if(correct){FormField(xp,{xp=it},"XP da meta",true);FormField(coins,{coins=it},"Fragmentos da meta",true);FormField(points,{points=it},"Pontos da meta",true)}}
        Text("Se fragmentos de um registro desfeito já tiverem sido gastos, a diferença será compensada pelos próximos ganhos. O saldo disponível permanece em zero até a compensação.",color=Muted,fontSize=12.sp)
    }
}
@Composable fun Onboarding(onRestore:(()->Unit)?=null,onCreate:(Player,List<Habit>)->Unit){
    var step by remember{mutableIntStateOf(0)};var name by remember{mutableStateOf("")};var cls by remember{mutableIntStateOf(1)};var color by remember{mutableIntStateOf(1)};var face by remember{mutableIntStateOf(0)};var outfit by remember{mutableIntStateOf(0)};var frame by remember{mutableIntStateOf(0)};var title by remember{mutableIntStateOf(0)};var goals by remember{mutableStateOf(setOf("Aprender"))};var starters by remember{mutableStateOf(setOf(0))}
    val titles=listOf("O Desperto","Viajante da Névoa","Portador da Brasa")
    val p=Player(name=name,heroClass=cls,color=color,face=face,outfit=outfit,frame=frame,title=titles[title],goals=goals.joinToString(", "))
    Page{
        Spacer(Modifier.height(20.dp));Eyebrow("Crônicas da Ascensão");Text("ASCEND",style=MaterialTheme.typography.headlineLarge,letterSpacing=6.sp,color=Gold)
        Meter((step+1)/3f,Gold)
        when(step){
            0->{Landscape(GameState(),large=true);Title("Aetheris espera por você.");Text("A Estagnação apagou as luzes da vila. Mas uma brasa permaneceu. Cada ação realizada na sua vida pode devolver um pouco de luz a este mundo.",color=Muted);Panel{Eyebrow("O Cronista");Text("“Não procure uma espada. Procure algo que você consiga fazer hoje.”",fontFamily=androidx.compose.ui.text.font.FontFamily.Serif,fontSize=20.sp)};Action("Acender a primeira chama",{step=1},Modifier.fillMaxWidth())}
            1->{Title("Quem atravessa a névoa?");Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){Avatar(p,size=144.dp)};FormField(name,{name=it.take(40)},"Nome do aventureiro");ChipRow(classes,cls,{cls=it});ClassEmblem(cls);Text(classDescriptions[cls],color=Muted);Text("Todas as classes podem desenvolver qualquer hábito.",color=Muted,fontSize=12.sp);Eyebrow("Seu retrato");ChipRow(listOf("Pele clara","Pele escura","Pele média"),face,{face=it});ChipRow(listOf("Ouro","Azul","Violeta","Verde","Coral"),color,{color=it});ChipRow(listOf("Túnica","Veste de viagem","Traje cerimonial"),outfit,{outfit=it});ChipRow(listOf("Moldura simples","Moldura solar","Moldura estrelada"),frame,{frame=it});ChipRow(titles,title,{title=it});Action("Continuar",{step=2},Modifier.fillMaxWidth(),name.isNotBlank())}
            2->{Title("Sua vida, sua jornada.","Selecione propósitos e os primeiros hábitos. Tudo poderá ser editado depois.");listOf("Aprender","Cuidar do corpo","Organizar a rotina","Encontrar equilíbrio","Criar projetos").forEach{g->Toggle(g,g in goals,{goals=if(it)goals+g else goals-g})};Eyebrow("Primeiras brasas");listOf("Estudar Direito · 60 minutos · 60 XP","Ler · 20 páginas · 25 XP","Mover o corpo · 30 minutos · 70 XP").forEachIndexed{i,label->Toggle(label,i in starters,{starters=if(it)starters+i else starters-i})};Text("Nenhum registro fictício será criado. Sua história começa com suas ações.",color=Muted,fontSize=12.sp);Action("Entrar em Aetheris",{
                val hs=listOf(Habit(name="Estudar Direito",type=2,target=60,unit="minutos",xp=60,points=5,coins=5,proportional=true),Habit(name="Ler",type=1,target=20,unit="páginas",xp=25,points=3,category="Leitura",proportional=true,icon=4),Habit(name="Mover o corpo",type=2,target=30,unit="minutos",xp=70,attribute=1,color=3,category="Movimento",icon=3))
                onCreate(p,hs.filterIndexed{i,_->i in starters}.mapIndexed{i,h->h.copy(position=i)})
            },Modifier.fillMaxWidth())}
        }
        if(step==0&&onRestore!=null)OutlinedButton(onRestore,Modifier.fillMaxWidth()){Text("Já tenho uma jornada · Restaurar backup")}
        if(step>0)TextButton({step--}){Text("Voltar")}
    }
}
@Composable fun QuestEditor(initial:Quest?,habits:List<Habit>,onDismiss:()->Unit,onSave:(Quest)->Unit){
    val q=remember{initial?:Quest(name="",period=2)};var name by remember{mutableStateOf(q.name)};var desc by remember{mutableStateOf(q.description)};var period by remember{mutableIntStateOf(q.period)};var metric by remember{mutableIntStateOf(q.metric)};var target by remember{mutableStateOf(q.target.toString())};var xp by remember{mutableStateOf(q.xp.toString())};var coins by remember{mutableStateOf(q.coins.toString())};var deadline by remember{mutableStateOf(q.deadline)};var special by remember{mutableStateOf(q.special)};var selected by remember{mutableStateOf(q.habitIds.split(',').filter{it.isNotBlank()}.toSet())}
    FullForm("Forjar uma missão",onDismiss,{onSave(q.copy(name=name,description=desc,period=period,metric=metric,target=target.toIntOrNull()?:0,xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,deadline=deadline,special=special,habitIds=selected.joinToString(",")))}){
        FormField(name,{name=it},"Nome");FormField(desc,{desc=it},"Descrição",multi=true);ChipRow(listOf("Diária","Semanal","Épica","Chefe semanal"),period,{period=it});ChipRow(listOf("Conclusões","Minutos","XP de hábitos","Páginas"),metric,{metric=it});FormField(target,{target=it},"Meta",true);FormField(xp,{xp=it},"Recompensa XP",true);FormField(coins,{coins=it},"Fragmentos",true);FormField(deadline,{deadline=it},"Prazo opcional · AAAA-MM-DD");FormField(special,{special=it},"Nome do troféu opcional");Text("Hábitos relacionados (nenhum = todos)",color=Muted);habits.filter{!it.deleted}.forEach{h->Toggle(h.name,h.id in selected,{selected=if(it)selected+h.id else selected-h.id})}
    }
}
@Composable fun RoutineEditor(r:Routine?,habits:List<Habit>,onDismiss:()->Unit,onSave:(Routine)->Unit){
    var name by remember{mutableStateOf(r?.name?:"")};var time by remember{mutableStateOf(r?.time?:"08:00")};var ids by remember{mutableStateOf(r?.habitIds?.split(',')?.filter{it.isNotBlank()}?:emptyList())}
    FullForm("Ritual pessoal",onDismiss,{onSave(Routine(r?.id?:uid(),name,ids.joinToString(","),time))}){
        FormField(name,{name=it},"Nome da rotina");FormField(time,{time=it},"Horário · HH:mm")
        Text("A ordem de seleção define a ordem da rotina. Desmarque e selecione novamente para reorganizar.",color=Muted,fontSize=13.sp)
        habits.filter{!it.deleted && !it.archived}.forEach{h->Toggle("${if(h.id in ids) "${ids.indexOf(h.id)+1}. " else ""}${h.name}",h.id in ids,{ids=if(it)ids+h.id else ids-h.id})}
        Text("Conclusões usam o mesmo registro diário em todas as rotinas.",color=Muted,fontSize=12.sp)
    }
}
@Composable fun JournalEditor(j:Journal?,onDismiss:()->Unit,onSave:(Journal)->Unit){
    var title by remember{mutableStateOf(j?.title?:"")};var text by remember{mutableStateOf(j?.text?:"")}
    FullForm("Diário do aventureiro",onDismiss,{onSave(Journal(j?.id?:uid(),title,text,j?.date?:today()))}){Eyebrow(j?.date?:today());FormField(title,{title=it},"Título");OutlinedTextField(text,{text=it},Modifier.fillMaxWidth().heightIn(min=320.dp),label={Text("O que este dia deixou em você?")});Text("Esta página fica somente neste aparelho e no backup local que você exportar.",color=Muted,fontSize=12.sp)}
}
