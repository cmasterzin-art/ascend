package br.com.ascend.aetheris

import java.time.LocalDate

data class WeekChronicle(val start:String,val end:String,val summary:PeriodSummary,val coins:Long,val points:List<Long>,val missions:List<String>,val sagasAdvanced:List<String>,val sagasCompleted:List<String>,val achievements:List<String>,val restored:List<String>,val narrative:String)
fun rawClaimValid(c:Claim,s:GameState):Boolean{
 val q=s.quests.find{it.id==c.questId}?:return false
 return questProgress(q.copy(target=c.target,metric=c.metric,habitIds=c.habitIds,period=c.period,attribute=c.attribute,deadline=c.deadline,sagaStart=c.sagaStart,sagaBaseline=c.sagaBaseline),s,c.occurrence)>=c.target
}
fun weeklyChronicle(s:GameState,date:String=today(),now:String=today()):WeekChronicle{
 val start=weekOf(date);val end=minOf(LocalDate.parse(start).plusDays(6).toString(),now)
 require(start<=end)
 val summary=periodSummary(s,start,end);val ledger=s.ledger.filter{it.date in start..end}
 val claims=s.claims.filter{it.date in start..end&&validClaim(it,s)}
 val missions=claims.filter{it.sagaStart.isBlank()||expeditionRef(it.questId)!=null}.mapNotNull{c->s.quests.find{it.id==c.questId}?.name}
 val done=claims.filter{it.sagaStart.isNotBlank()&&expeditionRef(it.questId)==null}.mapNotNull{c->s.quests.find{it.id==c.questId}?.name}
 val advanced=s.quests.filter{q->isPersonalSaga(q)&&sagaWeekDelta(q,s,start,end)>0}.map{it.name}
 val achievements=s.achievements.filter{it.date in start..end}.mapNotNull{a->achievementDefs.find{it.id==a.id}?.name}
 val restored=ledger.filter{it.id.startsWith("restore:")&&it.coins<0}.map{it.label.removePrefix("Restauração · ")+" · etapa "+it.id.substringAfterLast(':')}
 val intro=when{done.isNotEmpty()->"O Cronista separou uma folha para os juramentos que encontraram seu fim. As marcas desta semana já não dependem de uma promessa.";restored.isNotEmpty()->"Houve som de ferramentas entre as casas. Sob o pó, apareceram encaixes que o inverno não conseguiu destruir.";achievements.isNotEmpty()->"Uma inscrição nova ficou junto à porta do salão. A luz é pouca, mas basta para ler os nomes.";summary.completed==0&&summary.xp==0L->"A neve cobriu a trilha sem apagar o caminho. Esta folha guarda uma semana de silêncio; não há dívida escrita nela.";summary.rate!=null&&summary.rate!!>=75->"O caderno voltou com as margens gastas. Há sinais de muitos retornos, e o Cronista deixou a janela aberta enquanto os registrava.";else->listOf("Poucas brasas bastaram para manter o salão visível. O Cronista recolheu os sinais desta semana, sem exigir que fossem iguais aos da anterior.","Entre uma travessia e outra, houve tempo para deixar marcas na pedra. Nem todas formam uma linha; todas pertencem ao caminho.")[LocalDate.parse(start).toEpochDay().mod(2)]}
 return WeekChronicle(start,end,summary,ledger.sumOf{it.coins.coerceAtLeast(0)},(0..4).map{a->ledger.filter{it.attribute==a}.sumOf{it.points}.coerceAtLeast(0)},missions,advanced,done,achievements,restored,intro)
}
fun sagaWeekDelta(q:Quest,s:GameState,start:String,end:String):Long{
 // For abandoned sagas, derive the week's recorded activity only up to abandonment, capped at the frozen total.
 val cutoff=if(q.sagaEnd.isBlank())end else minOf(end,q.sagaEnd)
 if(cutoff<start)return 0
 val active=q.copy(sagaState=0)
 val before=sagaProgress(active,s.copy(records=s.records.filter{it.date<start}))
 val after=sagaProgress(active,s.copy(records=s.records.filter{it.date<=cutoff}))
 return (minOf(after,q.target.toLong(),if(q.sagaState in listOf(1,3))q.sagaStopped else Long.MAX_VALUE)-minOf(before,q.target.toLong())).coerceAtLeast(0)
}
data class HallMemory(val id:String,val title:String,val category:String,val date:String?,val detail:String,val symbol:Int=0)
fun hallMemories(s:GameState):List<HallMemory> = buildList{
 s.records.filter{it.complete}.minByOrNull{it.date}?.let{add(HallMemory("first","A primeira marca","Personagem",it.date,"Primeira conclusão atualmente registrada: ${it.name}."))}
 s.claims.sortedBy{it.date}.forEachIndexed{i,c->val q=s.quests.find{it.id==c.questId}?:return@forEachIndexed;if(c.sagaStart.isNotBlank()||c.id==s.claims.filter{it.sagaStart.isBlank()}.minByOrNull{it.date}?.id)add(HallMemory("claim:${c.id}",q.name,if(expeditionRef(c.questId)!=null)"Mundo" else if(c.sagaStart.isNotBlank())"Sagas" else "História",c.date,if(validClaim(c,s))"${expeditionRef(c.questId)?.let{(e,n)->"Floresta da Persistência · ${e.phases[n].name}. ${if(n==e.phases.lastIndex)e.consequence else e.phases[n].text} "}?:""}Juramento resgatado por ${s.player?.name}. ${c.xp} XP · ${c.coins} Fragmentos configurados." else "Resgate histórico. O progresso foi corrigido; a recompensa acompanha essa correção.",3))}
 s.achievements.forEach{a->val def=achievementDefs.find{it.id==a.id}?:return@forEach;add(HallMemory("achievement:${a.id}",def.name,"Conquistas",a.date,"${def.rarity} · ${def.description}",4))}
 s.ledger.filter{it.id.startsWith("level:")&&it.coins>0&&it.id.substringAfter(':').toIntOrNull() in listOf(5,10,15,20,30,50)}.forEach{add(HallMemory(it.id,it.label,"Personagem",it.date,"Marco alcançado por ${s.player?.name}.",2))}
 s.ledger.filter{it.id.startsWith("restore:")&&it.coins<0}.forEach{add(HallMemory(it.id,it.label,"Mundo",it.date,"Etapa ${it.id.substringAfterLast(':')} · ${-it.coins} Fragmentos utilizados.",2))}
 regions.forEachIndexed{i,name->if(s.level>=regionLevels[i])add(HallMemory("region:$i",name,"Mundo",if(i==0)s.player?.created else s.ledger.find{it.id=="level:${regionLevels[i]}"}?.date,"Região revelada · nível ${regionLevels[i]}.",3))}
 listOf(1 to "O Desperto",5 to "Aprendiz Errante",10 to "Aventureiro",15 to "Guardião do Conhecimento",20 to "Mestre da Vontade",30 to "Herói de Aetheris",50 to "Lenda Ascendente").filter{it.first<=s.level}.forEach{(level,title)->add(HallMemory("title:$level",title,"Personagem",if(level==1)s.player?.created else s.ledger.find{it.id=="level:$level"}?.date,"Título disponível para ${s.player?.name} · nível $level.",2))}
 specializationTitles(s).forEach{add(HallMemory("title:$it",it,"Personagem",null,"Título disponível pelos atributos atuais.",2))}
 s.assets.filter{it.kind=="memory"}.forEach{a->val fallback=narrativeCandidates(s).find{"memory:${it.id}"==a.id};add(HallMemory(a.id,a.memorySpeaker.ifBlank{fallback?.speaker?:"Voz de Aetheris"},"História",a.date,a.memoryText.ifBlank{fallback?.text?:"Conversa reconhecida em uma versão anterior. O texto original não foi guardado."},1))}
 s.paths.filter{it.state==3}.forEach{p->val prog=pathProgress(s,p.id);val taskInfo=if(prog.total>0)" · ${prog.done}/${prog.total} tarefas" else "";add(HallMemory("path:${p.id}",p.name,"Caminhos",p.completedDate.ifBlank{p.startDate},"Caminho concluído · ${p.category}$taskInfo.",2))}
}.sortedWith(compareByDescending<HallMemory>{it.date?:""}.thenBy{it.id})
