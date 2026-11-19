package br.com.ascend.aetheris

import java.time.LocalDate

fun parseBaseline(value:String):Map<String,Int> = if(value.isBlank())emptyMap() else value.split(';').associate { val parts=it.split('=');require(parts.size==2);parts[0] to parts[1].toInt().also{v->require(v in 0..1000000)} }
fun validateSaga(q:Quest,s:GameState){
    val exp=isExpedition(q)
    val allowedMetrics=if(exp) listOf(0,1,3,EXPEDITION_METRIC_FOCUS_SESSIONS,EXPEDITION_METRIC_FLASHCARD_DAYS,EXPEDITION_METRIC_PATH_TASKS) else listOf(0,1,3)
    require(q.name.isNotBlank()&&q.target in 1..1000000&&q.xp in 0..1000000&&q.coins in 0..1000000&&q.metric in allowedMetrics){"Revise nome, meta, métrica e recompensas."}
    if(q.metric in listOf(EXPEDITION_METRIC_FOCUS_SESSIONS,EXPEDITION_METRIC_FLASHCARD_DAYS,EXPEDITION_METRIC_PATH_TASKS)){
        require(exp){"Métrica exclusiva de expedições regionais."}
    }else{
        val ids=q.habitIds.split(',').filter{it.isNotBlank()}
        require(ids.isNotEmpty()&&ids.distinct().size==ids.size&&ids.all{id->s.habits.any{it.id==id}}){"Selecione hábitos relacionados."}
    }
    if(q.deadline.isNotBlank()){LocalDate.parse(q.deadline);require(q.deadline>=(q.sagaStart.ifBlank{today()})){"O prazo deve ser posterior ao início."}}
}
fun sagaProgress(q:Quest,s:GameState):Long{
    if(q.sagaState in listOf(1,3))return q.sagaStopped
    if(q.metric in listOf(EXPEDITION_METRIC_FOCUS_SESSIONS,EXPEDITION_METRIC_FLASHCARD_DAYS,EXPEDITION_METRIC_PATH_TASKS)){
        return expeditionMetricProgress(q,s)
    }
    val baseline=parseBaseline(q.sagaBaseline);val ids=q.habitIds.split(',').toSet()
    return s.records.filter{it.habitId in ids&&it.date>=q.sagaStart&&(q.deadline.isBlank()||it.date<=q.deadline)}.sumOf{r->
        val prior=baseline[r.id]?:0;val delta=(r.progress-prior).coerceAtLeast(0).toLong()
        when(q.metric){1->if(r.type==2)delta else 0L;3->if(r.unit.contains("página",true))delta else 0L;else->if(r.complete&&prior<r.target)1L else 0L}
    }
}
fun sagaMilestone(q:Quest,s:GameState)=(questProgress(q,s)*4/q.target).coerceIn(0,4).toInt()
val sagaMilestoneTexts=listOf("A pedra ainda espera o primeiro sinal.","Um sulco rompeu a superfície. O juramento já tem testemunhas.","Sob a geada, as marcas se encontram. Metade do caminho ficou gravada.","A última curva aparece. Há espaço para uma linha que ainda não foi escrita.","A inscrição está inteira. O que você fez já pertence à memória do Norte.")
data class SagaModel(val name:String,val description:String,val metric:Int,val target:Int,val attribute:Int)
val sagaModels=listOf(
 SagaModel("A Saga do Conhecimento","Reunir trinta horas de estudo registrado.",1,1800,0),
 SagaModel("Os Caminhos do Norte","Completar vinte caminhadas escolhidas por você.",0,20,1),
 SagaModel("O Livro das Eras","Ler quinhentas páginas nos hábitos selecionados.",3,500,0),
 SagaModel("A Forja da Disciplina","Concluir sessenta atividades de organização.",0,60,2),
 SagaModel("O Juramento do Artífice","Dedicar vinte e cinco horas a projetos pessoais.",1,1500,4),
 SagaModel("A Vigília do Guardião","Concluir vinte atividades de autocuidado.",0,20,3))
fun rewardEvent(message:String,old:GameState,next:GameState):GameEvent{
 val highlights=mutableListOf<String>()
 if(next.level>old.level)highlights+="Nível ${next.level} alcançado"
 next.achievements.filter{a->old.achievements.none{it.id==a.id}}.forEach{a->val def=achievementDefs.first{it.id==a.id};if(def.rarity!="Comum")highlights+="${def.rarity}: ${def.name}"}
 next.assets.filter{a->a.kind!="memory"&&old.assets.none{it.id==a.id&&it.stage==a.stage}}.forEach{a->when(a.kind){"building"->{val i=a.id.substringAfter(':').toInt();highlights+="${buildings[i]} · etapa ${a.stage}/2 · ${a.stage*30} Fragmentos utilizados"};"skill"->highlights+="Habilidade: ${skillDefs.first{a.id=="skill:${it.id}"}.name}";else->shopItems.find{it.id==a.id}?.let{highlights+="Artefato: ${it.name}"}}}
 next.claims.filter{c->old.claims.none{it.id==c.id}}.forEach{c->highlights+="${if(expeditionRef(c.questId)!=null)"Expedição" else if(c.sagaStart.isNotBlank())"Saga" else "Missão"} resgatada: ${next.quests.find{it.id==c.questId}?.name}"}
 return GameEvent(message,if(next.level>old.level)next.level else 0,(next.xp-old.xp).coerceAtLeast(0),(next.coins-old.coins).coerceAtLeast(0),(0..4).map{(next.points(it)-old.points(it)).coerceAtLeast(0)},highlights)
}
data class NarrativeLine(val id:String,val speaker:String,val text:String)
fun narrativeCandidates(s:GameState):List<NarrativeLine> = buildList{
 s.claims.filter{it.sagaStart.isNotBlank()&&expeditionRef(it.questId)==null}.forEach{add(NarrativeLine("saga-done:${it.id}","O Cronista","Guardei uma folha para ${s.quests.find{q->q.id==it.questId}?.name}. Hoje ela deixou de ser uma promessa. Tem marcas de uso; prefiro assim."))}
 (0..2).forEach{i->if(s.assets.any{it.id=="building:$i"&&it.stage==2}){val d=villagerDialogue(i,2);add(NarrativeLine("restored:$i",d.first,d.second))}}
 s.achievements.filter{a->achievementDefs.any{it.id==a.id&&it.rarity in listOf("Épica","Lendária")}}.forEach{a->add(NarrativeLine("achievement:${a.id}","O Cronista","Inscrevi ${achievementDefs.first{it.id==a.id}.name} na pedra que fica junto à entrada. O entalhe vai escurecer com a chuva. O nome ficará."))}
 if(s.claims.any{it.period==2&&it.sagaStart.isBlank()})add(NarrativeLine("epic","O Cronista","O contrato voltou com as margens gastas. Quem o ler depois saberá que esteve fora desta sala."))
 if(s.achievements.any{it.id=="return"})add(NarrativeLine("return","O Cronista","Deixei a cadeira junto à porta. O inverno não me disse quando você voltaria."))
 listOf(30,20,10,5).forEach{if(s.level>=it)add(NarrativeLine("level:$it","O Cronista","No registro do nível $it, a tinta já alcança a margem. Preciso de outro caderno. Este ficará na prateleira de quem atravessou a névoa."))}
 s.quests.filter{isPersonalSaga(it)}.forEach{add(NarrativeLine("saga-start:${it.id}","O Cronista","Você deu um nome ao caminho: ${it.name}. Vou anotá-lo sem preencher o fim. Essa parte ainda não nos pertence."))}
 (0..2).forEach{i->if(s.assets.any{it.id=="building:$i"&&it.stage==1}){val d=villagerDialogue(i,1);add(NarrativeLine("rebuilding:$i",d.first,d.second))}}
 if(s.level>=10)add(NarrativeLine("elyra-level10","Elyra · Bibliotecária","Encontrei sua anotação num volume que ninguém abria. Deixei outra ao lado. Talvez uma terceira pessoa nos responda daqui a um inverno."))
 if(s.quests.any{isPersonalSaga(it)&&it.sagaState==0&&it.metric==1})add(NarrativeLine("darian-saga","Darian · Ferreiro","Marquei as horas na madeira do banco. Não para apressar o trabalho. Para saber quanto dele ficou aqui quando eu for embora."))
 if(chapterUnlocked(6,s))add(NarrativeLine("discovery-ione","O Cronista","Ione voltou com a margem do mapa molhada. Onde o rio mudou, ela desenhou duas linhas: uma para a água, outra para a lembrança."))
 if(s.achievements.any{it.id=="return"})add(NarrativeLine("seren-return","Seren · Guardiã do Jardim","As sementes ficaram sob a terra enquanto você esteve longe. Algumas esperaram. Outras encontraram seu próprio tempo."))
 allExpeditions.forEach{e->if(expeditionComplete(s,e)){val speaker=expeditionRegion(e).npc?.let{"${it.name} · ${it.role}"} ?: "O Cronista";add(NarrativeLine("expedition:${e.id}",speaker,e.consequence))}}
 s.paths.filter{it.state==3}.forEach{p->add(NarrativeLine("path-done:${p.id}","O Cronista","O caminho \"${p.name}\" alcançou seu marco final. Não foi uma promessa vaga; houve passos reais entre a partida e a chegada."))}
 if(s.records.any{it.complete})add(NarrativeLine("first","O Cronista","Encontrei terra na soleira. Até ontem, só havia poeira. Alguém saiu e voltou; é o primeiro fato deste volume."))
}
fun nextNarrative(s:GameState)=narrativeCandidates(s).firstOrNull{line->s.assets.none{it.id=="memory:${line.id}"}}

fun rewardSummary(e:GameEvent)=buildList{if(e.xp>0)add("+${e.xp} XP");if(e.coins>0)add("+${e.coins} Fragmentos");e.points.forEachIndexed{i,v->if(v>0)add("+$v ${attributes[i]}")}}.joinToString(" · ")
