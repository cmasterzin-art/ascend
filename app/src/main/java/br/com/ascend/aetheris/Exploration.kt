package br.com.ascend.aetheris

data class ExpeditionPhase(val name:String,val target:Int,val text:String)
data class ExpeditionDef(val id:String,val name:String,val location:String,val premise:String,val consequence:String,val phases:List<ExpeditionPhase>,val xp:Int,val coins:Int,val item:String="")
val forestExpeditions=listOf(
 ExpeditionDef("roots","Raízes sob a Névoa","O Marco dos Retornos","Sob as raízes de um pinheiro, uma pedra conserva a marca de mãos que voltavam pela mesma trilha. O Cronista encontra um sulco coberto de terra: alguém tentou proteger uma palavra da chuva.","A inscrição diz apenas: voltamos. Atrás da pedra, cinco marcas indicam uma trilha que não aparece nos mapas.",listOf(ExpeditionPhase("Os sinais do caminho",5,"Uma marca reaparece em cada retorno. As pedras não indicam uma chegada; indicam o caminho de volta.")),40,10),
 ExpeditionDef("sanctuary","O Santuário Esquecido","O Santuário das Raízes","A trilha termina numa porta sem dobradiças. Dentro, as raízes sustentam o que restou do teto. Seren pede que nenhuma seja cortada antes de sabermos o que ainda mantém de pé.","A água agora corre por fora das paredes. Seren deixa uma semente numa fissura preservada. O santuário volta a receber quem atravessa a floresta.",listOf(ExpeditionPhase("A soleira",2,"Darian encontra pedra firme sob a madeira podre. Levará só as peças que já não sustentam nada."),ExpeditionPhase("O abrigo",2,"A primeira cobertura desvia a chuva. No chão seco, aparecem pegadas pequenas que ninguém vira antes."),ExpeditionPhase("As raízes guardadas",2,"Seren recoloca a terra ao redor das raízes. A porta ficará aberta; nunca houve fechadura neste lugar.")),60,15),
 ExpeditionDef("vigil","A Vigília da Floresta","O Coração da Névoa","Uma sombra de galhos ocupa a passagem além do santuário. Não avança quando você se aproxima. O som que parecia respiração vem das árvores ao redor.","O Guardião não cai. Inclina a cabeça e abre passagem. A névoa continua na floresta; agora você reconhece o que ela protege. O Cronista guarda o relato sem lhe dar um fim.",listOf(ExpeditionPhase("O Chamado",2,"A presença ergue a cabeça. Entre os galhos, uma pedra marcada repete o sinal do primeiro caminho."),ExpeditionPhase("A Resistência",3,"Você percebe que a sombra tem raízes. Darian baixa a ferramenta. Não existe aqui uma peça que possa ser arrancada sem levar o resto."),ExpeditionPhase("O Juramento",3,"Você deixa uma marca junto às antigas. O Guardião espera até que sua mão se afaste da pedra; só então move um dos braços.")),100,25,"mist-clasp")
)
fun expeditionQuestId(e:ExpeditionDef,phase:Int)="exp:${e.id}:$phase"
fun expeditionRef(id:String):Pair<ExpeditionDef,Int>?{val p=id.split(':');if(p.size!=3||p[0]!="exp")return null;val e=forestExpeditions.find{it.id==p[1]}?:return null;val n=p[2].toIntOrNull()?:return null;return if(n in e.phases.indices)e to n else null}
fun isExpedition(q:Quest)=expeditionRef(q.id)!=null
fun isPersonalSaga(q:Quest)=q.sagaStart.isNotBlank()&&!isExpedition(q)
fun expeditionClaimValid(s:GameState,e:ExpeditionDef,phase:Int):Boolean{
 val index=forestExpeditions.indexOf(e)
 if(index>0&&!expeditionComplete(s,forestExpeditions[index-1]))return false
 if(phase>0&&!expeditionClaimValid(s,e,phase-1))return false
 val c=s.claims.find{it.questId==expeditionQuestId(e,phase)}?:return false
 return rawClaimValid(c,s)
}
fun expeditionComplete(s:GameState,e:ExpeditionDef)=expeditionClaimValid(s,e,e.phases.lastIndex)
fun expeditionAvailable(s:GameState,e:ExpeditionDef):Boolean{val index=forestExpeditions.indexOf(e);return s.level>=3&&(index==0||expeditionComplete(s,forestExpeditions[index-1]))}
fun expeditionPhaseIndex(s:GameState,e:ExpeditionDef)=e.phases.indices.firstOrNull{!expeditionClaimValid(s,e,it)}?:e.phases.lastIndex
fun expeditionState(s:GameState,e:ExpeditionDef):String{
 if(expeditionComplete(s,e))return "Recompensa resgatada"
 if(!expeditionAvailable(s,e))return "Bloqueada"
 val phase=expeditionPhaseIndex(s,e);val q=s.quests.find{it.id==expeditionQuestId(e,phase)}?:return "Disponível"
 if(questProgress(q,s)>=q.target)return if(phase==e.phases.lastIndex)"Concluída · resgate disponível" else "Marco alcançado"
 return "Em andamento"
}
fun forestProgress(s:GameState):Int{val phases=forestExpeditions.sumOf{it.phases.size};return forestExpeditions.sumOf{e->e.phases.indices.count{expeditionClaimValid(s,e,it)}}*100/phases}
fun forestStage(s:GameState)=when{expeditionComplete(s,forestExpeditions.last())->2;expeditionComplete(s,forestExpeditions.first())->1;else->0}
fun sanctuaryStage(s:GameState)=when{expeditionComplete(s,forestExpeditions[1])->2;expeditionClaimValid(s,forestExpeditions[1],0)->1;else->0}
fun validClaim(c:Claim,s:GameState):Boolean=expeditionRef(c.questId)?.let{(e,n)->expeditionClaimValid(s,e,n)}?:rawClaimValid(c,s)

fun expeditionPhaseQuest(e:ExpeditionDef,phase:Int,ids:List<String>,s:GameState):Quest{
 val final=phase==e.phases.lastIndex;val def=e.phases[phase]
 return Quest(id=expeditionQuestId(e,phase),name="${e.name} · ${def.name}",description=def.text,target=def.target,habitIds=ids.sorted().joinToString(","),xp=if(final)e.xp else 0,coins=if(final)e.coins else 0,sagaStart=today(),sagaBaseline=s.records.filter{it.habitId in ids}.joinToString(";"){"${it.id}=${it.progress}"})
}
