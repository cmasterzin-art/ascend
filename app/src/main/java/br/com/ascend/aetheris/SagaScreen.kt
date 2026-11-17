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
 AetherSurface(SurfaceKind.Ceremonial){Eyebrow("Juramentos de longo curso",Blue);Text("Sagas Pessoais",style=MaterialTheme.typography.headlineMedium);Text("Escolha os hábitos que darão forma ao seu juramento. Apenas o progresso posterior ao início será contado.",color=Muted);Action("Iniciar saga",{modelPicker=true})}
 AetherisTabs(listOf("Em curso","Histórico"),if(history)1 else 0,{history=it==1})
 val shown=s.quests.filter{isPersonalSaga(it)&&if(history)it.sagaState!=0||s.claims.any{c->c.questId==it.id} else it.sagaState==0&&s.claims.none{c->c.questId==it.id}}
 if(shown.isEmpty())Empty(if(history)"Nenhum juramento encerrado. As inscrições aparecerão aqui." else "A pedra está livre para um novo juramento. Escolha uma saga quando fizer sentido.")
 shown.forEach{q->val value=questProgress(q,s);val milestone=sagaMilestone(q,s);val claimed=s.claims.any{it.questId==q.id}
 AetherSurface(if(claimed)SurfaceKind.Ceremonial else SurfaceKind.Heroic){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){CeremonialSeal(if(claimed)SealKind.Reward else SealKind.Oath,milestone>0||claimed,Modifier.size(44.dp));Column(Modifier.weight(1f)){Eyebrow(if(q.sagaState in listOf(1,3))"Abandonada${if(q.sagaState==3)" · arquivada" else ""}" else if(q.sagaState==2)"Concluída · arquivada" else if(claimed)"Memória resgatada" else if(value>=q.target)"Juramento cumprido" else "Saga em curso",Blue);Text(q.name,style=MaterialTheme.typography.titleLarge)}}
 if(claimed&&value<q.target)Text("Progresso corrigido: a recompensa foi compensada no histórico. O resgate permanece único.",color=Muted,fontSize=12.sp)
 Text(q.description,color=Muted);Text("${value.coerceAtMost(q.target.toLong())} / ${q.target} ${sagaUnit(q.metric)}",color=Blue);Meter(value.toFloat()/q.target,Blue)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){(1..4).forEach{i->Text("${i*25}% ${if(milestone>=i)"◆" else "◇"}",color=if(milestone>=i)Blue else Muted,fontSize=12.sp)}}
 Text(sagaMilestoneTexts[milestone],fontFamily=SagaFont,fontSize=16.sp)
 Text("Desde ${displayDate(q.sagaStart)}${if(q.deadline.isNotBlank())" · até ${displayDate(q.deadline)}" else " · sem prazo"}",color=Muted,fontSize=12.sp)
 Text("Recompensa final · ${q.xp} XP · ◈ ${q.coins}",color=Gold)
 Text("Hábitos: "+s.habits.filter{it.id in q.habitIds.split(',')}.joinToString{it.name},color=Muted,fontSize=12.sp)
 Action(if(claimed)"Recompensa resgatada" else "Resgatar juramento",{vm.run{vm.repo.claim(q)}},enabled=!claimed&&q.sagaState==0&&value>=q.target,completed=claimed)
 if(!claimed&&q.sagaState==0){SagaButton("Editar saga",{editor=q},secondary=true);SagaButton("Abandonar saga",{abandon=q},destructive=true)}
 if(history){if(q.sagaState in 0..1)SagaButton("Arquivar saga",{vm.run{vm.repo.archiveSaga(q.id)}},secondary=true);SagaButton("Reiniciar desafio",{vm.run{vm.repo.restartSaga(q.id);history=false}},secondary=true)}
 }}
 abandon?.let{q->AlertDialog(onDismissRequest={abandon=null},title={Text("Abandonar ${q.name}?")},text={Text("O progresso desta tentativa será guardado. Hábitos, registros e XP dos hábitos permanecem. Não há recompensa pelo abandono.")},confirmButton={SagaButton("Abandonar",{vm.run{vm.repo.abandonSaga(q.id);abandon=null;history=true}},destructive=true)},dismissButton={TextButton({abandon=null}){Text("Continuar saga")}})}
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
 s.habits.filter{!it.deleted||it.id in ids}.forEach{h->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(h.id in ids,{ids=if(it)ids+h.id else ids-h.id},enabled=q.sagaStart.isBlank());Text(h.name,Modifier.weight(1f))}}
 if(s.habits.isEmpty())Text("Crie um hábito manual ou escolha um no Grimório antes de iniciar.",color=Muted)
 FormField(xp,{xp=it},"XP final",true);FormField(coins,{coins=it},"Fragmentos finais",true)
 Text("Marcos de 25%, 50% e 75% revelam narrativa. Só o resgate final concede a recompensa configurada, uma vez. Registros anteriores, inclusive os já feitos hoje, são excluídos por uma fotografia do progresso no início.",color=Muted,fontSize=13.sp)
 }
 if(discard)AlertDialog(onDismissRequest={discard=false},title={Text("Descartar alterações?")},text={Text("As mudanças deste formulário ainda não foram salvas.")},confirmButton={SagaButton("Descartar",onDismiss,destructive=true)},dismissButton={TextButton({discard=false}){Text("Continuar editando")}})
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