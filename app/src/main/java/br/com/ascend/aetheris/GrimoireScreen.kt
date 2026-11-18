package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import androidx.compose.ui.semantics.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable fun FilterPicker(label:String,options:List<String>,selected:Int,onSelect:(Int)->Unit,modifier:Modifier=Modifier){
    var open by remember{mutableStateOf(false)}
    val shape=CutCornerShape(topStart=6.dp,topEnd=2.dp,bottomEnd=6.dp,bottomStart=2.dp)
    Box(modifier){
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).background(Surface.copy(alpha=.7f),shape).border(1.dp,ColdIron.copy(alpha=.4f),shape).clickable{open=true}.padding(horizontal=12.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
            Column(Modifier.weight(1f)){Text(label,color=Muted,fontSize=10.sp);Text(options[selected.coerceIn(options.indices)],color=Ink,fontSize=12.sp)}
            Text("▾",color=Gold,fontSize=12.sp)
        }
        DropdownMenu(open,{open=false}){options.forEachIndexed{i,v->DropdownMenuItem(text={Text(v)},onClick={onSelect(i);open=false})}}
    }
}
@Composable fun GrimoireScreen(s:GameState,vm:GameViewModel,onAdd:(Habit)->Unit,onRemindersNeeded:()->Unit={}){
    var filter by rememberSaveable{mutableStateOf(CatalogFilter())};var tab by rememberSaveable{mutableIntStateOf(0)};var expanded by remember{mutableStateOf(false)};var interestsOpen by remember{mutableStateOf(false)};var routine by remember{mutableStateOf<RoutineTemplate?>(null)}
    val interests by vm.catalogInterests.collectAsStateWithLifecycle();val dismissed by vm.dismissedSuggestions.collectAsStateWithLifecycle();val narrative=LocalNarrativeNames.current
    val found=remember(filter){filterTemplates(filter)}
    LazyColumn(Modifier.fillMaxSize().testTag("catalog_list"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item(key="heading"){Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            Title("Grimório dos Hábitos","Todo grande feito começa com um pequeno ritual.")
            AetherisTabs(listOf("Hábitos","Modelos de rotina"),tab,{tab=it})
            if(tab==0)FormField(filter.query,{filter=filter.copy(query=it)},"Buscar nome ou palavra-chave")
        }}
            if(tab==0){
                item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                    SagaButton(if(expanded)"Recolher filtros" else "Filtros e ordem",{expanded=!expanded},Modifier.weight(1f),secondary=true)
                    TextButton({filter=CatalogFilter()}){Text("Limpar")}
                }}
                item{ChipRow(listOf("Todas")+catalogCategories,if(filter.category==null)0 else catalogCategories.indexOf(filter.category)+1,{filter=filter.copy(category=if(it==0)null else catalogCategories[it-1])})}
                if(expanded)item{
                    Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            FilterPicker("Dificuldade",listOf("Todas")+difficultyNames,(filter.difficulty?:-1)+1,{filter=filter.copy(difficulty=if(it==0)null else it-1)},Modifier.weight(1f))
                            FilterPicker("Tipo",listOf("Todos")+habitTypeNames,(filter.type?:-1)+1,{filter=filter.copy(type=if(it==0)null else it-1)},Modifier.weight(1f))
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            FilterPicker("Atributo",listOf("Todos")+attributes,(filter.attribute?:-1)+1,{filter=filter.copy(attribute=if(it==0)null else it-1)},Modifier.weight(1f))
                            FilterPicker("Frequência",listOf("Todas","Diária","2 a 6 dias/semana","1 dia/semana"),filter.frequency,{filter=filter.copy(frequency=it)},Modifier.weight(1f))
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                            FilterPicker("Tempo estimado",listOf("Todos","Até 15 min","16 a 30 min","Mais de 30 min"),filter.duration,{filter=filter.copy(duration=it)},Modifier.weight(1f))
                            FilterPicker("Ordenar por",listOf("Nome","Categoria","Duração","Dificuldade"),filter.order,{filter=filter.copy(order=it)},Modifier.weight(1f))
                        }
                    }
                }
                item{Toggle("Exibir nomes narrativos",narrative,{vm.setting("narrative_names",it)})}
                if(filter.query.isBlank()&&filter.category==null){
                    item{Panel{
                        Eyebrow("Sugestões para sua Jornada")
                        Text("Escolha interesses. Mostramos até quatro modelos dessas categorias que você ainda não adicionou nem dispensou. Tudo é calculado neste aparelho.",color=Muted,fontSize=12.sp)
                        TextButton({interestsOpen=!interestsOpen}){Text("Escolher interesses")}
                        if(interestsOpen)catalogCategories.forEach{category->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(category in interests,{vm.interest(category)},modifier=Modifier.semantics{contentDescription="Interesse: $category"},colors=CheckboxDefaults.colors(checkedColor=Gold,uncheckedColor=ColdIron,checkmarkColor=androidx.compose.ui.graphics.Color(0xFF17140E)));Text(category,Modifier.weight(1f),fontSize=13.sp)}}
                        if(dismissed.isNotEmpty())TextButton({vm.resetSuggestions()}){Text("Reexibir sugestões dispensadas")}
                    }}
                    val suggestions=recommendations(interests,dismissed,s.habits)
                    items(suggestions,key={"suggest:${it.id}"}){t->CatalogCard(t,s,narrative,{onAdd(t.instantiate(s.habits.size))},{vm.dismissSuggestion(t.id)})}
                }
                item{Eyebrow("${found.size} ${if(found.size==1)"sugestão no catálogo" else "sugestões no catálogo"}");if(filter.category=="Vigor")Text("Duração e intensidade são sugestões editáveis. Adapte-as às suas condições; não são recomendações médicas universais.",color=Muted,fontSize=12.sp);if(filter.category=="Equilíbrio")Text("Descanso é um convite. Estes modelos começam sem contagem de sequências; você pode ajustar essa opção.",color=Muted,fontSize=12.sp)}
                if(found.isEmpty())item{Empty("Nenhum ritual encontrado. Experimente outro termo ou limpe os filtros.")}
                items(found,key={"catalog:${it.id}"}){t->CatalogCard(t,s,narrative,{onAdd(t.instantiate(s.habits.size))})}
            }else{
                item{Text("Escolha os hábitos antes de adicionar. Itens já existentes são reutilizados, sem alterar suas configurações. A rotina não concede XP adicional.",color=Muted,fontSize=13.sp)}
                items(routineTemplates,key={it.id}){r->Panel{Eyebrow("${r.choices.size} rituais · ${r.time}");Text(r.name,style=MaterialTheme.typography.titleLarge);Text(r.description,color=Muted);Action("Examinar ${r.name}",{routine=r})}}
            }
    }
    routine?.let{r->RoutinePreview(r,s,vm,{routine=null},onRemindersNeeded)}
}
@Composable private fun CatalogCard(t:HabitTemplate,s:GameState,narrative:Boolean,onAdd:()->Unit,onDismiss:(()->Unit)?=null){
    val existing=matchingHabit(s.habits,t.instantiate());val color=habitColor(t.attribute)
    Column(Modifier.fillMaxWidth().materialSurface(accent=color,leather=true).padding(16.dp).testTag(if(onDismiss==null)"template:${t.id}" else "suggest:${t.id}"),verticalArrangement=Arrangement.spacedBy(9.dp)){
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
            AttributeEmblem(t.attribute,Modifier.size(36.dp))
            Column(Modifier.weight(1f)){Eyebrow("${t.category} · ${attributes[t.attribute]}",color);Text(if(narrative)t.narrativeName else t.name,style=MaterialTheme.typography.titleLarge);if(narrative)Text(t.name,color=Muted,fontSize=12.sp)}
        }
        Text(t.description,color=Muted,fontSize=13.sp)
        Text("${t.target} ${t.unit} · ${difficultyNames[t.difficulty]} · ${frequencyLabel(t.days)} · ~${t.estimatedMinutes} min",color=color,fontSize=12.sp)
        Hairline()
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){Text("${t.xp} XP  ·  ◈ ${t.coins}",Modifier.weight(1f),color=Gold,fontFamily=SagaFont,fontSize=15.sp);Action(if(existing!=null)"Já na jornada" else "Adicionar",onAdd,enabled=existing==null,completed=existing!=null)}
        if(existing?.archived==true||existing?.paused==true)Text("O hábito existente está arquivado ou pausado. Reative-o em Hábitos.",color=Muted,fontSize=11.sp)
        if(onDismiss!=null)TextButton(onDismiss){Text("Dispensar sugestão",fontSize=12.sp)}
    }
}
@Composable private fun RoutinePreview(r:RoutineTemplate,s:GameState,vm:GameViewModel,onDismiss:()->Unit,onRemindersNeeded:()->Unit){
    var candidates by remember(r.id){mutableStateOf(r.choices.mapIndexed{i,c->c.instantiate(s.habits.size+i)})}
    var selected by remember(r.id){mutableStateOf(candidates.map{it.templateId}.toSet())};var editing by remember{mutableStateOf<Habit?>(null)}
    var busy by remember{mutableStateOf(false)};var selectionError by remember{mutableStateOf(false)}
    FullForm(r.name,onDismiss,{
        if(selected.isEmpty())selectionError=true
        else if(!busy){busy=true;vm.run{try{vm.repo.addRoutineTemplate(r.id,candidates.filter{it.templateId in selected});if(candidates.any{it.templateId in selected&&it.reminder})onRemindersNeeded();onDismiss()}finally{busy=false}}}
    },if(busy)"Adicionando…" else "Adicionar seleção"){
        if(selectionError&&selected.isEmpty())Text("Selecione pelo menos um hábito para adicionar.",color=MaterialTheme.colorScheme.error)
        Text(r.description,color=Muted);Text("Selecione e personalize antes de salvar. Hábitos existentes serão reutilizados sem alterações e podem ser editados depois em Hábitos.",fontSize=13.sp,color=Muted)
        candidates.forEach{h->val existing=matchingHabit(s.habits,h)
            Panel{Row(verticalAlignment=Alignment.CenterVertically){Checkbox(h.templateId in selected,{selected=if(it)selected+h.templateId else selected-h.templateId},modifier=Modifier.semantics{contentDescription="Incluir ${h.name}"},colors=CheckboxDefaults.colors(checkedColor=Gold,uncheckedColor=ColdIron,checkmarkColor=androidx.compose.ui.graphics.Color(0xFF17140E)));Text(h.name,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge)}
                val shown=existing?:h
                Text("${shown.target} ${shown.unit} · ${shown.xp} XP · ${frequencyLabel(shown.days)}",color=Muted,fontSize=12.sp)
                if(existing!=null)Text("Reutilizar existente${if(existing.archived||existing.paused)" · arquivado/pausado" else ""}",color=Gold,fontSize=12.sp)
                else TextButton({editing=h}){Text("Personalizar ${h.name}")}
            }
        }
        Text("${selected.size} ${if(selected.size==1)"selecionado" else "selecionados"} · nenhum XP por adicionar ou agrupar",color=Muted,fontSize=12.sp)
    }
    editing?.let{h->HabitEditor(h,{editing=null},{updated->vm.run{validateHabitInput(updated);candidates=candidates.map{if(it.id==updated.id)updated else it};editing=null}})}
}
