package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.ui.semantics.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FullForm(title:String,onDismiss:()->Unit,onSave:()->Unit,saveLabel:String="Salvar",content:@Composable ColumnScope.()->Unit){
    Dialog(onDismiss,properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)){
        Scaffold(topBar={Column{TopAppBar(title={Text(title,fontFamily=SagaFont)},navigationIcon={IconButton(onDismiss){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Voltar")}},actions={SagaButton(saveLabel,onSave)});Box(Modifier.fillMaxWidth().height(1.dp).background(Gold.copy(alpha=.28f)))}}){padding->Box(Modifier.padding(padding).imePadding()){Page(content)}}
    }
}
@Composable fun HabitEditor(h:Habit,onDismiss:()->Unit,onSave:(Habit)->Unit){
    var narrative by rememberSaveable(h.id){mutableStateOf(h.narrativeName)};var trackStreak by rememberSaveable(h.id){mutableStateOf(h.trackStreak)}
    var name by rememberSaveable(h.id){mutableStateOf(h.name)};var desc by rememberSaveable(h.id){mutableStateOf(h.description)};var category by rememberSaveable(h.id){mutableStateOf(h.category)};var attr by rememberSaveable(h.id){mutableIntStateOf(h.attribute)};var type by rememberSaveable(h.id){mutableIntStateOf(h.type)};var target by rememberSaveable(h.id){mutableStateOf(h.target.toString())};var unit by rememberSaveable(h.id){mutableStateOf(h.unit)}
    var xp by rememberSaveable(h.id){mutableStateOf(h.xp.toString())};var coins by rememberSaveable(h.id){mutableStateOf(h.coins.toString())};var points by rememberSaveable(h.id){mutableStateOf(h.points.toString())};var days by rememberSaveable(h.id){mutableStateOf(h.days.split(',').mapNotNull{it.toIntOrNull()}.toSet())};var time by rememberSaveable(h.id){mutableStateOf(h.time)};var proportional by rememberSaveable(h.id){mutableStateOf(h.proportional)};var reminder by rememberSaveable(h.id){mutableStateOf(h.reminder)};var difficulty by rememberSaveable(h.id){mutableIntStateOf(h.difficulty)};var icon by rememberSaveable(h.id){mutableIntStateOf(h.icon)};var color by rememberSaveable(h.id){mutableIntStateOf(h.color)}
    var discard by remember{mutableStateOf(false)}
    fun draft() = h.copy(name=name.trim(),narrativeName=narrative.trim(),trackStreak=trackStreak,description=desc,category=category,attribute=attr,type=type,target=if(type==0)1 else target.toIntOrNull()?:0,unit=if(type==2)"minutos" else unit,xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,points=points.toIntOrNull()?:-1,days=days.sorted().joinToString(","),time=time,proportional=proportional && type in 1..2,reminder=reminder,difficulty=difficulty,icon=icon,color=color)
    val dirty=draft()!=h
    FullForm(if(h.name.isBlank())"Novo hábito" else if(h.templateId.isNotBlank()&&h.scheduleHistory.isBlank())"Personalizar ritual" else "Editar hábito",{if(dirty)discard=true else onDismiss()},{onSave(draft())}){
        var advanced by rememberSaveable(h.id){mutableStateOf(h.templateId.isNotBlank())}
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
                row.forEach{day->val sel=day in days;FilterChip(sel,{days=if(sel)days-day else days+day},label={Text(listOf("Seg","Ter","Qua","Qui","Sex","Sáb","Dom")[day-1],color=if(sel)Gold else Muted)},modifier=Modifier.weight(1f).heightIn(min=48.dp).semantics{contentDescription=listOf("Segunda-feira","Terça-feira","Quarta-feira","Quinta-feira","Sexta-feira","Sábado","Domingo")[day-1]},shape=androidx.compose.foundation.shape.CutCornerShape(4.dp),colors=FilterChipDefaults.filterChipColors(containerColor=Surface.copy(alpha=.6f),selectedContainerColor=Gold.copy(alpha=.18f),labelColor=Muted,selectedLabelColor=Gold),border=FilterChipDefaults.filterChipBorder(enabled=true,selected=sel,borderColor=ColdIron.copy(alpha=.32f),selectedBorderColor=Gold.copy(alpha=.78f)))};repeat(4-row.size){Spacer(Modifier.weight(1f))}
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
            FormField(time,{time=it},"Horário · HH:mm");Text("Opcional quando não há lembrete. Deixe vazio para atividades sem horário.",color=Muted,fontSize=12.sp);Toggle("Lembrete local opcional",reminder,{reminder=it})
            FormField(coins,{coins=it},"Fragmentos de Aether",number=true);FormField(points,{points=it},"Pontos de ${attributes[attr]}",number=true)
        }
        Panel(accent=Blue){Text("Alterar recompensas afeta apenas novos registros. O histórico preserva os valores originais. Use a correção de registro no histórico quando necessário.",color=Muted,fontSize=13.sp)}
    }
    if(discard)DiscardEdits(onDismiss,{discard=false})
}
@Composable fun ProgressEditor(h:Habit,r:HabitRecord?,onDismiss:()->Unit,onSave:(String,Int,HabitRecord?)->Unit){
    var date by rememberSaveable(h.id,r?.id){mutableStateOf(r?.date?:today())};var amount by rememberSaveable(h.id,r?.id){mutableStateOf((r?.progress?:0).toString())};var correct by rememberSaveable(h.id,r?.id){mutableStateOf(false)};var xp by rememberSaveable(h.id,r?.id){mutableStateOf((r?.xp?:h.xp).toString())};var coins by rememberSaveable(h.id,r?.id){mutableStateOf((r?.coins?:h.coins).toString())};var points by rememberSaveable(h.id,r?.id){mutableStateOf((r?.points?:h.points).toString())}
    var inputError by rememberSaveable(h.id,r?.id){mutableStateOf<String?>(null)}
    var discard by remember{mutableStateOf(false)}
    val dirty=date!=(r?.date?:today())||amount!=(r?.progress?:0).toString()||correct&&(xp!=(r?.xp?:h.xp).toString()||coins!=(r?.coins?:h.coins).toString()||points!=(r?.points?:h.points).toString())
    FullForm("Registrar · ${h.name}",{if(dirty)discard=true else onDismiss()},{
        val correction=if(correct&&r!=null) r.copy(xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,points=points.toIntOrNull()?:-1) else null
        val value=amount.toIntOrNull()
        inputError=when{
            value==null||value<0 -> "Informe um número inteiro igual ou maior que zero."
            runCatching{java.time.LocalDate.parse(date)}.isFailure -> "Informe uma data válida no formato AAAA-MM-DD."
            correction!=null&&(correction.xp !in 0..1000000||correction.coins !in 0..1000000||correction.points !in 0..1000000) -> "Informe recompensas entre 0 e 1.000.000."
            else -> null
        }
        if(inputError==null)onSave(date,requireNotNull(value),correction)
    }){
        inputError?.let{Text(it,color=MaterialTheme.colorScheme.error)}
        FormField(date,{date=it},"Data · AAAA-MM-DD")
        Text("Meta deste registro: ${r?.target?:h.target} ${r?.unit?:h.unit}",color=Gold)
        FormField(amount,{amount=it;inputError=null},"Progresso total nesta data",number=true)
        if(h.type!=0)Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
            (if(h.type==2)listOf(5,15,25) else listOf(1,5,10)).forEach{step->SagaButton("+$step",{amount=((amount.toIntOrNull()?:0).coerceAtLeast(0).toLong()+step).coerceAtMost((r?.target?:h.target).toLong()).toString()},secondary=true)}
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SagaButton("Desfazer",{amount="0"},secondary=true);Action("Atingir meta",{amount=(r?.target?:h.target).toString()})}
        Text("Informe o total realizado no dia. O aplicativo concede apenas a diferença de recompensa.",color=Muted,fontSize=13.sp)
        if(r!=null){Toggle("Corrigir recompensas deste registro",correct,{correct=it});if(correct){FormField(xp,{xp=it},"XP da meta",true);FormField(coins,{coins=it},"Fragmentos da meta",true);FormField(points,{points=it},"Pontos da meta",true)}}
        Text("Se fragmentos de um registro desfeito já tiverem sido gastos, a diferença será compensada pelos próximos ganhos. O saldo disponível permanece em zero até a compensação.",color=Muted,fontSize=12.sp)
    }
    if(discard)DiscardEdits(onDismiss,{discard=false})
}
@Composable fun DiscardEdits(onDiscard:()->Unit,onKeep:()->Unit){
    AetherDialog(onDismissRequest=onKeep,title="Descartar alterações?",confirmButton={SagaButton("Descartar",onDiscard,destructive=true)},dismissButton={TextButton(onKeep){Text("Continuar editando")}}){Text("As mudanças deste formulário ainda não foram salvas.")}
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
            },Modifier.fillMaxWidth(),ceremonial=true)}
        }
        if(step==0&&onRestore!=null)SagaButton("Já tenho uma jornada · Restaurar backup",onRestore,Modifier.fillMaxWidth(),secondary=true)
        if(step>0)TextButton({step--}){Text("Voltar")}
    }
}
@Composable fun QuestEditor(initial:Quest?,habits:List<Habit>,onDismiss:()->Unit,onSave:(Quest)->Unit){
    val q=remember(initial?.id){initial?:Quest(name="",period=2)}
    var name by rememberSaveable(q.id){mutableStateOf(q.name)}
    var desc by rememberSaveable(q.id){mutableStateOf(q.description)}
    var period by rememberSaveable(q.id){mutableIntStateOf(q.period)}
    var metric by rememberSaveable(q.id){mutableIntStateOf(q.metric)}
    var target by rememberSaveable(q.id){mutableStateOf(q.target.toString())}
    var xp by rememberSaveable(q.id){mutableStateOf(q.xp.toString())}
    var coins by rememberSaveable(q.id){mutableStateOf(q.coins.toString())}
    var deadline by rememberSaveable(q.id){mutableStateOf(q.deadline)}
    var special by rememberSaveable(q.id){mutableStateOf(q.special)}
    var selectedCsv by rememberSaveable(q.id){mutableStateOf(q.habitIds)}
    val selected=remember(selectedCsv){selectedCsv.split(',').filter{it.isNotBlank()}.toSet()}
    var discard by remember{mutableStateOf(false)}
    val dirty=name!=q.name||desc!=q.description||period!=q.period||metric!=q.metric||target!=q.target.toString()||xp!=q.xp.toString()||coins!=q.coins.toString()||deadline!=q.deadline||special!=q.special||selected!=q.habitIds.split(',').filter{it.isNotBlank()}.toSet()
    FullForm("Forjar uma missão",{if(dirty)discard=true else onDismiss()},{onSave(q.copy(name=name,description=desc,period=period,metric=metric,target=target.toIntOrNull()?:0,xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,deadline=deadline,special=special,habitIds=selected.joinToString(",")))}){
        FormField(name,{name=it},"Nome");FormField(desc,{desc=it},"Descrição",multi=true);ChipRow(listOf("Diária","Semanal","Épica","Chefe semanal"),period,{period=it});ChipRow(listOf("Conclusões","Minutos","XP de hábitos","Páginas"),metric,{metric=it});FormField(target,{target=it},"Meta",true);FormField(xp,{xp=it},"Recompensa XP",true);FormField(coins,{coins=it},"Fragmentos",true);FormField(deadline,{deadline=it},"Prazo opcional · AAAA-MM-DD");FormField(special,{special=it},"Nome do troféu opcional");Text("Hábitos relacionados (nenhum = todos)",color=Muted);habits.filter{!it.deleted}.forEach{h->Toggle(h.name,h.id in selected,{val next=if(it)selected+h.id else selected-h.id;selectedCsv=next.joinToString(",")})}
    }
    if(discard)DiscardEdits(onDismiss,{discard=false})
}
@Composable fun RoutineEditor(r:Routine?,habits:List<Habit>,onDismiss:()->Unit,onSave:(Routine)->Unit){
    val key=r?.id?:"new-routine"
    var name by rememberSaveable(key){mutableStateOf(r?.name?:"")}
    var time by rememberSaveable(key){mutableStateOf(r?.time?:"08:00")}
    var idsCsv by rememberSaveable(key){mutableStateOf(r?.habitIds?:"")}
    val ids=remember(idsCsv){idsCsv.split(',').filter{it.isNotBlank()}}
    var discard by remember{mutableStateOf(false)}
    val dirty=name!=(r?.name?:"")||time!=(r?.time?:"08:00")||idsCsv!=(r?.habitIds?:"")
    FullForm("Ritual pessoal",{if(dirty)discard=true else onDismiss()},{onSave(Routine(r?.id?:uid(),name,ids.joinToString(","),time))}){
        FormField(name,{name=it},"Nome da rotina");FormField(time,{time=it},"Horário · HH:mm")
        Text("A ordem de seleção define a ordem da rotina. Desmarque e selecione novamente para reorganizar.",color=Muted,fontSize=13.sp)
        habits.filter{!it.deleted && !it.archived}.forEach{h->Toggle("${if(h.id in ids) "${ids.indexOf(h.id)+1}. " else ""}${h.name}",h.id in ids,{val next=if(it)ids+h.id else ids-h.id;idsCsv=next.joinToString(",")})}
        Text("Conclusões usam o mesmo registro diário em todas as rotinas.",color=Muted,fontSize=12.sp)
    }
    if(discard)DiscardEdits(onDismiss,{discard=false})
}
@Composable fun JournalEditor(j:Journal?,onDismiss:()->Unit,onSave:(Journal)->Unit){
    val key=j?.id?:"new-journal"
    var title by rememberSaveable(key){mutableStateOf(j?.title?:"")}
    var text by remember(key){mutableStateOf(j?.text?:"")}
    var discard by remember{mutableStateOf(false)}
    val dirty=title!=(j?.title?:"")||text!=(j?.text?:"")
    FullForm("Diário do aventureiro",{if(dirty)discard=true else onDismiss()},{onSave(Journal(j?.id?:uid(),title,text,j?.date?:today()))}){Eyebrow(displayDate(j?.date?:today()));FormField(title,{title=it},"Título");FormField(text,{text=it},"O que este dia deixou em você?",multi=true,modifier=Modifier.heightIn(min=320.dp));Text("Esta página fica somente neste aparelho e no backup local que você exportar.",color=Muted,fontSize=12.sp)}
    if(discard)DiscardEdits(onDismiss,{discard=false})
}
