package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable fun SagaBook(s:GameState,vm:GameViewModel){
 var editor by remember{mutableStateOf<Quest?>(null)};var modelPicker by remember{mutableStateOf(false)};var history by rememberSaveable{mutableStateOf(false)};var abandon by remember{mutableStateOf<Quest?>(null)}
 Column(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  ChapterHeading("Sagas Pessoais", "Escolha os hábitos que darão forma ao seu juramento. Apenas o progresso posterior ao início será contado.", NorthbornChapter.Archive, Engraving.Banner)
  Action("Iniciar saga",{modelPicker=true})
 }
 Hairline()
 AetherisTabs(listOf("Em curso","Histórico"),if(history)1 else 0,{history=it==1})
 val shown=s.quests.filter{isPersonalSaga(it)&&if(history)it.sagaState!=0||s.claims.any{c->c.questId==it.id} else it.sagaState==0&&s.claims.none{c->c.questId==it.id}}
 if(shown.isEmpty())Empty(if(history)"Nenhum juramento encerrado. As inscrições aparecerão aqui." else "A pedra está livre para um novo juramento. Escolha uma saga quando fizer sentido.")
 shown.forEach{q->val value=questProgress(q,s);val milestone=sagaMilestone(q,s);val claimed=s.claims.any{it.questId==q.id}
 AetherSurface(if(claimed)SurfaceKind.Ceremonial else SurfaceKind.Heroic){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){CeremonialSeal(if(claimed)SealKind.Reward else SealKind.Oath,milestone>0||claimed,Modifier.size(44.dp));Column(Modifier.weight(1f)){Eyebrow(if(q.sagaState in listOf(1,3))"Abandonada${if(q.sagaState==3)" · arquivada" else ""}" else if(q.sagaState==2)"Concluída · arquivada" else if(claimed)"Memória resgatada" else if(value>=q.target)"Juramento cumprido" else "Saga em curso",Blue);Text(q.name,style=MaterialTheme.typography.titleLarge)}}
 if(claimed&&value<q.target)Text("Progresso corrigido: a recompensa foi compensada no histórico. O resgate permanece único.",color=Muted,fontSize=12.sp)
 Text(q.description,color=Muted);Text("${value.coerceAtMost(q.target.toLong())} / ${q.target} ${sagaUnit(q.metric)}",color=Blue);Meter(value.toFloat()/q.target,Blue,slim=true)
 // 1.6.9.15: connected runic waystones along one solemn path.
 Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically){(1..4).forEach{i->
  if(i>1)Box(Modifier.weight(1f).height(1.dp).padding(horizontal=3.dp).background(if(milestone>=i)Blue.copy(alpha=.7f) else ColdIron.copy(alpha=.3f)))
  val mShape=androidx.compose.foundation.shape.CutCornerShape(3.dp)
  Box(Modifier.background(if(milestone>=i)Blue.copy(alpha=.15f) else androidx.compose.ui.graphics.Color.Transparent,mShape).border(1.dp,if(milestone>=i)Blue.copy(alpha=.55f) else ColdIron.copy(alpha=.25f),mShape).padding(horizontal=5.dp,vertical=2.dp)){
   Text("${i*25}% ${if(milestone>=i)"◆" else "◇"}",color=if(milestone>=i)Blue else Muted,fontSize=11.sp)
  }
 }}
 Text(sagaMilestoneTexts[milestone],fontFamily=SagaFont,fontSize=16.sp)
 Hairline()
 Text("Desde ${displayDate(q.sagaStart)}${if(q.deadline.isNotBlank())" · até ${displayDate(q.deadline)}" else " · sem prazo"}",color=Muted,fontSize=12.sp)
 Text("Recompensa final · ${q.xp} XP · ◈ ${q.coins}",color=Gold)
 Text("Hábitos: "+s.habits.filter{it.id in q.habitIds.split(',')}.joinToString{it.name},color=Muted,fontSize=12.sp)
 Action(if(claimed)"Recompensa resgatada" else "Resgatar juramento",{vm.run{vm.repo.claim(q)}},enabled=!claimed&&q.sagaState==0&&value>=q.target,completed=claimed,ceremonial=!claimed&&q.sagaState==0&&value>=q.target)
 @OptIn(ExperimentalLayoutApi::class) if(!claimed&&q.sagaState==0)FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){SagaButton("Editar saga",{editor=q},secondary=true);TextButton({abandon=q}){Text("Abandonar saga",color=MaterialTheme.colorScheme.error)}}
 @OptIn(ExperimentalLayoutApi::class) if(history)FlowRow(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){if(q.sagaState in 0..1)SagaButton("Arquivar saga",{vm.run{vm.repo.archiveSaga(q.id)}},secondary=true);SagaButton("Reiniciar desafio",{vm.run{vm.repo.restartSaga(q.id);history=false}},secondary=true)}
 }}
 abandon?.let{q->AetherDialog(onDismissRequest={abandon=null},title="Abandonar ${q.name}?",confirmButton={SagaButton("Abandonar",{vm.run{vm.repo.abandonSaga(q.id);abandon=null;history=true}},destructive=true)},dismissButton={TextButton({abandon=null}){Text("Continuar saga")}}){Text("O progresso desta tentativa será guardado. Hábitos, registros e XP dos hábitos permanecem. Não há recompensa pelo abandono.")}}
 if(modelPicker)FullForm("Escolher um juramento",{modelPicker=false},{modelPicker=false},"Voltar"){
 Action("Criar saga personalizada",{editor=Quest(name="");modelPicker=false})
 sagaModels.forEach{m->Panel(accent=Blue){Text(m.name,style=MaterialTheme.typography.titleLarge);Text(m.description,color=Muted);Text("${m.target} ${sagaUnit(m.metric)}",color=Blue);Action("Escolher ${m.name}",{editor=Quest(name=m.name,description=m.description,metric=m.metric,target=m.target);modelPicker=false})}}
 }
 editor?.let{q->SagaEditor(q,s,{editor=null},{updated->vm.run{vm.repo.saga(updated);editor=null}})}
}
fun sagaUnit(metric:Int)=when(metric){1->"minutos";3->"páginas";else->"conclusões"}
@Composable fun SagaEditor(q:Quest,s:GameState,onDismiss:()->Unit,onSave:(Quest)->Unit){
 var name by rememberSaveable{mutableStateOf(q.name)};var desc by rememberSaveable{mutableStateOf(q.description)};var target by rememberSaveable{mutableStateOf(q.target.toString())};var metric by rememberSaveable{mutableIntStateOf(q.metric)};var xp by rememberSaveable{mutableStateOf(q.xp.toString())};var coins by rememberSaveable{mutableStateOf(q.coins.toString())};var deadline by rememberSaveable{mutableStateOf(q.deadline)};var ids by remember{mutableStateOf(q.habitIds.split(',').filter{it.isNotBlank()}.toSet())}
 var discard by remember{mutableStateOf(false)}
 val dirty=name!=q.name||desc!=q.description||target!=q.target.toString()||metric!=q.metric||xp!=q.xp.toString()||coins!=q.coins.toString()||deadline!=q.deadline||ids!=q.habitIds.split(',').filter{it.isNotBlank()}.toSet()
 val close={if(dirty)discard=true else onDismiss()}
 FullForm(if(q.sagaStart.isBlank())"Novo juramento" else "Editar saga",close,{onSave(q.copy(name=name.trim(),description=desc,target=target.toIntOrNull()?:0,metric=metric,xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,deadline=deadline,habitIds=ids.sorted().joinToString(",")))},if(q.sagaStart.isBlank())"Iniciar jornada" else "Salvar alterações"){
 FormField(name,{name=it},"Nome da saga");FormField(desc,{desc=it},"Descrição",multi=true)
 if(q.sagaStart.isBlank())ChipRow(listOf("Conclusões","Minutos","Páginas"),listOf(0,1,3).indexOf(metric),{metric=listOf(0,1,3)[it]}) else Text("Métrica e hábitos preservados desde o início.",color=Muted)
 FormField(target,{target=it},"Meta · ${sagaUnit(metric)}",true);DeadlinePicker(deadline,{deadline=it})
 Eyebrow("Hábitos relacionados")
 s.habits.filter{!it.deleted||it.id in ids}.forEach{h->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(h.id in ids,{ids=if(it)ids+h.id else ids-h.id},enabled=q.sagaStart.isBlank(),colors=CheckboxDefaults.colors(checkedColor=Gold,uncheckedColor=ColdIron,checkmarkColor=androidx.compose.ui.graphics.Color(0xFF17140E)));Text(h.name,Modifier.weight(1f))}}
 if(s.habits.isEmpty())Text("Crie um hábito manual ou escolha um no Grimório antes de iniciar.",color=Muted)
 FormField(xp,{xp=it},"XP final",true);FormField(coins,{coins=it},"Fragmentos finais",true)
 Text("Marcos de 25%, 50% e 75% revelam narrativa. Só o resgate final concede a recompensa configurada, uma vez. Registros anteriores, inclusive os já feitos hoje, são excluídos por uma fotografia do progresso no início.",color=Muted,fontSize=13.sp)
 }
 if(discard)AetherDialog(onDismissRequest={discard=false},title="Descartar alterações?",confirmButton={SagaButton("Descartar",onDismiss,destructive=true)},dismissButton={TextButton({discard=false}){Text("Continuar editando")}}){Text("As mudanças deste formulário ainda não foram salvas.")}
}
@Composable fun DeadlinePicker(value:String,onChange:(String)->Unit){
 val context=LocalContext.current
 Text("Prazo opcional",color=Muted,fontSize=12.sp)
 SagaButton(if(value.isBlank())"Escolher data" else "Até ${displayDate(value)}",{
  val date=runCatching{LocalDate.parse(value)}.getOrDefault(LocalDate.now())
  android.app.DatePickerDialog(context,{_,year,month,day->onChange(LocalDate.of(year,month+1,day).toString())},date.year,date.monthValue-1,date.dayOfMonth).show()
 },secondary=true)
 if(value.isNotBlank())TextButton({onChange("")}){Text("Sem prazo")}
}