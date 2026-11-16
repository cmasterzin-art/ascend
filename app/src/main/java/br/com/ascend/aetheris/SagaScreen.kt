package br.com.ascend.aetheris

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable fun SagaBook(s:GameState,vm:GameViewModel){
 var editor by remember{mutableStateOf<Quest?>(null)};var modelPicker by remember{mutableStateOf(false)}
 Panel(accent=Blue){Eyebrow("Juramentos de longo curso",Blue);Text("Sagas Pessoais",style=MaterialTheme.typography.headlineMedium);Text("Escolha os hábitos que darão forma ao seu juramento. Apenas o progresso posterior ao início será contado.",color=Muted);Action("Iniciar saga",{modelPicker=true})}
 s.quests.filter{it.sagaStart.isNotBlank()}.forEach{q->val value=questProgress(q,s);val milestone=sagaMilestone(q,s);val claimed=s.claims.any{it.questId==q.id}
 Panel(accent=if(claimed)Green else Blue){Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){RuneSeal(milestone,Blue,Modifier.size(46.dp));Column(Modifier.weight(1f)){Eyebrow(if(claimed)"Memória resgatada" else if(value>=q.target)"Juramento cumprido" else "Saga em curso",Blue);Text(q.name,style=MaterialTheme.typography.titleLarge)}}
 if(claimed&&value<q.target)Text("Progresso corrigido: a recompensa foi compensada no histórico. O resgate permanece único.",color=Muted,fontSize=12.sp)
 Text(q.description,color=Muted);Text("${value.coerceAtMost(q.target.toLong())} / ${q.target} ${sagaUnit(q.metric)}",color=Blue);Meter(value.toFloat()/q.target,Blue)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){(1..4).forEach{i->Text("${i*25}% ${if(milestone>=i)"◆" else "◇"}",color=if(milestone>=i)Blue else Muted,fontSize=12.sp)}}
 Text(sagaMilestoneTexts[milestone],fontFamily=SagaFont,fontSize=16.sp)
 Text("Desde ${q.sagaStart}${if(q.deadline.isNotBlank())" · até ${q.deadline}" else " · sem prazo"}",color=Muted,fontSize=12.sp)
 Text("Recompensa final · ${q.xp} XP · ◈ ${q.coins}",color=Gold)
 Text("Hábitos: "+s.habits.filter{it.id in q.habitIds.split(',')}.joinToString{it.name},color=Muted,fontSize=12.sp)
 Action(if(claimed)"Recompensa resgatada" else "Resgatar juramento",{vm.run{vm.repo.claim(q)}},enabled=!claimed&&value>=q.target,completed=claimed)
 if(!claimed)SagaButton("Editar saga",{editor=q},secondary=true)
 }}
 if(modelPicker)FullForm("Escolher um juramento",{modelPicker=false},{modelPicker=false},"Voltar"){
 Action("Criar saga personalizada",{editor=Quest(name="");modelPicker=false})
 sagaModels.forEach{m->Panel(accent=Blue){Text(m.name,style=MaterialTheme.typography.titleLarge);Text(m.description,color=Muted);Text("${m.target} ${sagaUnit(m.metric)}",color=Blue);Action("Escolher ${m.name}",{editor=Quest(name=m.name,description=m.description,metric=m.metric,target=m.target);modelPicker=false})}}
 }
 editor?.let{q->SagaEditor(q,s,{editor=null},{updated->vm.run{vm.repo.saga(updated);editor=null}})}
}
fun sagaUnit(metric:Int)=when(metric){1->"minutos";3->"páginas";else->"conclusões"}
@Composable fun SagaEditor(q:Quest,s:GameState,onDismiss:()->Unit,onSave:(Quest)->Unit){
 var name by remember{mutableStateOf(q.name)};var desc by remember{mutableStateOf(q.description)};var target by remember{mutableStateOf(q.target.toString())};var metric by remember{mutableIntStateOf(q.metric)};var xp by remember{mutableStateOf(q.xp.toString())};var coins by remember{mutableStateOf(q.coins.toString())};var deadline by remember{mutableStateOf(q.deadline)};var ids by remember{mutableStateOf(q.habitIds.split(',').filter{it.isNotBlank()}.toSet())}
 FullForm(if(q.sagaStart.isBlank())"Novo juramento" else "Editar saga",onDismiss,{onSave(q.copy(name=name.trim(),description=desc,target=target.toIntOrNull()?:0,metric=metric,xp=xp.toIntOrNull()?:-1,coins=coins.toIntOrNull()?:-1,deadline=deadline,habitIds=ids.sorted().joinToString(",")))},"Iniciar jornada"){
 FormField(name,{name=it},"Nome da saga");FormField(desc,{desc=it},"Descrição",multi=true)
 if(q.sagaStart.isBlank())ChipRow(listOf("Conclusões","Minutos","Páginas"),listOf(0,1,3).indexOf(metric),{metric=listOf(0,1,3)[it]}) else Text("Métrica e hábitos preservados desde o início.",color=Muted)
 FormField(target,{target=it},"Meta · ${sagaUnit(metric)}",true);FormField(deadline,{deadline=it},"Prazo opcional · AAAA-MM-DD")
 Eyebrow("Hábitos relacionados")
 s.habits.filter{!it.deleted||it.id in ids}.forEach{h->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(h.id in ids,{ids=if(it)ids+h.id else ids-h.id},enabled=q.sagaStart.isBlank());Text(h.name,Modifier.weight(1f))}}
 if(s.habits.isEmpty())Text("Crie um hábito manual ou escolha um no Grimório antes de iniciar.",color=Muted)
 FormField(xp,{xp=it},"XP final",true);FormField(coins,{coins=it},"Fragmentos finais",true)
 Text("Marcos de 25%, 50% e 75% revelam narrativa. Só o resgate final concede a recompensa configurada, uma vez. Registros anteriores, inclusive os já feitos hoje, são excluídos por uma fotografia do progresso no início.",color=Muted,fontSize=13.sp)
 }
}
