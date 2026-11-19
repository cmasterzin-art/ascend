package br.com.ascend.aetheris

// Content definitions are independent of owned items; all unlocks use the existing transactional store.
data class SkillDef(val id:String,val name:String,val path:String,val level:Int,val parent:String?,val item:String,val description:String,val heroClass:Int=-1,val atlasIndex:Int=-1,val effectSummary:String="",val titleGrant:String="")
val skillDefs=listOf(
    SkillDef("scholar","Olhar do Erudito","Sabedoria",3,null,"scholar-bg","Revela um fundo de biblioteca para seu retrato."),
    SkillDef("runes","Guardião dos Tomos","Sabedoria",8,"scholar","rune-frame","Concede a moldura das runas recuperadas."),
    SkillDef("ember","Chama Constante","Perseverança",3,null,"ember-amulet","Concede o amuleto da chama, exposto no nicho de relíquias."),
    SkillDef("will","Vontade Inabalável","Perseverança",8,"ember","will-helm","Concede a coroa da vontade."),
    SkillDef("craft","Mãos do Artífice","Criação",3,null,"craft-blade","Forja uma lâmina cerimonial, sem bônus de XP."),
    SkillDef("dawn","Arquiteto da Aurora","Criação",8,"craft","dawn-outfit","Concede o traje da aurora para seu personagem."),
    // Ascensão dos Arquétipos (1.7.1) — 2 habilidades por classe, efeitos cosméticos, narrativos e de conveniência
    SkillDef("war_oath","Juramento de Ferro","Guerreiro",3,null,"","Sela o compromisso diário sob o símbolo do machado e da vigília.",heroClass=0,atlasIndex=0,effectSummary="Emblema de juramento e título 'Portador do Juramento' no perfil.",titleGrant="Portador do Juramento"),
    SkillDef("war_resolve","Vontade de Ferro","Guerreiro",6,"war_oath","","Ergue a muralha interior para atravessar semanas exigentes sem perder o eixo.",heroClass=0,atlasIndex=1,effectSummary="Selo cerimonial da muralha e título 'Vontade da Muralha'.",titleGrant="Vontade da Muralha"),
    SkillDef("mage_mimir","Olhar de Mímir","Mago",3,null,"","Conecta a jornada pessoal diretamente aos volumes da Biblioteca de Mímir.",heroClass=1,atlasIndex=2,effectSummary="Atalho direto à Biblioteca de Mímir no perfil e título 'Discípulo de Mímir'.",titleGrant="Discípulo de Mímir"),
    SkillDef("mage_runes","Memória das Runas","Mago",6,"mage_mimir","","Reúne conhecimento e repetição espaçada sob o círculo rúnico de Aetheris.",heroClass=1,atlasIndex=3,effectSummary="Atalho rápido às Runas da Memória no perfil e título 'Escriba das Runas'.",titleGrant="Escriba das Runas"),
    SkillDef("guard_vigil","Vigília Constante","Guardião",3,null,"","Honra a constância serena e protege o ritmo dos compromissos essenciais.",heroClass=2,atlasIndex=4,effectSummary="Selo do santuário no perfil e título 'Vigia do Santuário'.",titleGrant="Vigia do Santuário"),
    SkillDef("guard_roots","Raízes do Santuário","Guardião",6,"guard_vigil","","Enraíza equilíbrio entre esforço, descanso planejado e renovação contínua.",heroClass=2,atlasIndex=5,effectSummary="Ornamento das raízes antigas e título 'Guardião das Raízes'.",titleGrant="Guardião das Raízes"),
    SkillDef("expl_compass","Bússola de Aetheris","Explorador",3,null,"","Orienta projetos pessoais e leituras como rotas de uma mesma travessia.",heroClass=3,atlasIndex=6,effectSummary="Atalho rápido aos Caminhos no perfil e título 'Guia da Bússola'.",titleGrant="Guia da Bússola"),
    SkillDef("expl_horizon","Passos do Horizonte","Explorador",6,"expl_compass","","Celebra o avanço sustentado em longas expedições e metas de longo prazo.",heroClass=3,atlasIndex=7,effectSummary="Selo do horizonte no perfil e título 'Caminhante do Horizonte'.",titleGrant="Caminhante do Horizonte"),
    SkillDef("art_forge","Forja do Propósito","Artífice",3,null,"","Transforma intenções em cadernos, tarefas e estruturas tangíveis.",heroClass=4,atlasIndex=8,effectSummary="Selo da forja no perfil e título 'Aprendiz da Forja'.",titleGrant="Aprendiz da Forja"),
    SkillDef("art_runes","Entalhe Rúnico","Artífice",6,"art_forge","","Refina o ofício pessoal com precisão estética e memória organizada.",heroClass=4,atlasIndex=9,effectSummary="Ornamento entalhado no perfil e título 'Mestre do Entalhe'.",titleGrant="Mestre do Entalhe")
)
fun skillPoints(s:GameState)=(s.level/3-s.assets.count{it.kind=="skill"}).coerceAtLeast(0)
fun rarity(item:ShopItem)=forgeRecipeById(item.id)?.rarity ?: when(item.id){
 "eternal-memory-medallion","firmament-compass","will-helm","dawn-outfit"->4
 "mist-clasp","scribes-ring","boreal-vigil-brooch","ignis","rune-frame","craft-blade"->3
 "frame","corvus","armor","scholar-bg"->2
 "cloak","lyra","ember-amulet"->1
 else->0
}
val rarityNames=listOf("Comum","Incomum","Raro","Épico","Lendário")
fun itemOrigin(item:ShopItem)=forgeRecipeById(item.id)?.let{"A Forja de Darian · ${it.cost} Fragmentos (${it.requirementLabel})"} ?: when(item.id){
 "mist-clasp"->"Expedição: A Vigília da Floresta · Floresta da Persistência"
 "scribes-ring"->"Expedição: As Páginas do Silêncio · Biblioteca das Eras"
 "eternal-memory-medallion"->"Expedição: O Juramento da Sabedoria · Biblioteca das Eras"
 "boreal-vigil-brooch"->"Expedição: A Chama que Não Cessa · Montanhas da Disciplina"
 "firmament-compass"->"Expedição: O Silêncio dos Cumes · Montanhas da Disciplina"
 else->skillDefs.find{it.item==item.id}?.let{"Habilidade: ${it.name}"}?:"Mercador · ${item.cost} Fragmentos"
}
val equipmentSlots=listOf("head" to "Cabeça","outfit" to "Corpo / capa","accessory" to "Acessório","weapon" to "Arma cosmética","companion" to "Companheiro","frame" to "Moldura","background" to "Fundo")
fun specializationTitles(s:GameState):List<String> = buildList {
    activeSpecialization(s)?.let { add(it.titleGrant) }
    skillDefs.filter { it.titleGrant.isNotBlank() && s.assets.any { a -> a.id == "skill:${it.id}" } }.forEach { add(it.titleGrant) }
    if(s.achievements.any{it.id=="mimir_five_days"}) add("Guardião do Conhecimento")
    if(s.achievements.any{it.id=="mimir_sage"}) add("Sábio dos Reinos")
    if(expeditionComplete(s,forestExpeditions.last()))add("Testemunha das Raízes")
    if(expeditionComplete(s,libraryExpeditions.last()))add("Guardião das Eras")
    if(expeditionComplete(s,mountainsExpeditions.last()))add("Testemunha dos Cumes")
    val names=listOf("Aprendiz das Runas","Caminhante de Ferro","Guardião dos Rituais","Voz do Santuário","Mãos da Aurora")
    names.forEachIndexed{i,n->if(1+s.points(i)/50>=10)add(n)}
    if(s.level>=50)add("Lenda do Primeiro Sol")
}.distinct()
fun villagerDialogue(index:Int,stage:Int):Pair<String,String> = when(index){
    0->"Elyra · Bibliotecária" to when(stage){0->"Ainda reconheço as estantes sob a poeira. Se recuperarmos o teto, os livros poderão voltar.";1->"A chuva já não alcança as páginas. Deixei uma mesa para a próxima pessoa que trouxer uma pergunta.";else->"Encontrei três notas diferentes nas margens do mesmo livro. Não vou apagá-las. Uma biblioteca também é feita de quem discorda dela."}
    1->"Darian · Ferreiro" to when(stage){0->"O fole está partido. A primeira peça que vamos forjar é a ferramenta para consertá-lo.";1->"O fole aguenta. A bigorna ainda oscila: falta uma cunha de carvalho. Não vou pôr um aprendiz diante dela assim.";else->"A primeira lâmina ficou na parede. As ferramentas, essas estão nas mãos da vila inteira."}
    2->"Seren · Guardiã do Jardim" to when(stage){0->"As raízes sobreviveram. Precisam de espaço, água e alguém disposto a esperar.";1->"Encontrei brotos junto ao muro norte. O chão ainda guarda frio; deixei a palha onde estava.";else->"Há sombra para descansar e sementes para levar. Você não precisa fazer mais nada hoje."}
    else->"O Cronista" to if(stage==0)"Guardei o nome deste lugar. Agora precisamos devolver-lhe uma história." else if(stage==1)"As pedras estão de pé. Ainda falta a vida que irá preencher os espaços." else "Este lugar já aparece nos mapas novos. Deixei seu nome nas primeiras páginas."
}

val narrativeQuests=listOf(
    Quest(id="narrative:elyra",name="A Chama do Conhecimento",description="Elyra separou cinco mesas vazias. Complete cinco hábitos de Intelecto para trazer novos leitores à biblioteca.",target=5,xp=100,coins=35,special="Selo de Elyra",attribute=0),
    Quest(id="narrative:darian",name="As Mãos da Reconstrução",description="Darian precisa de mãos pacientes. Conclua cinco hábitos de Disciplina para sustentar a reconstrução.",target=5,xp=100,coins=35,special="Martelo de Darian",attribute=2),
    Quest(id="narrative:seren",name="Um Jardim para Respirar",description="Seren plantou as primeiras sementes. Complete cinco hábitos de Equilíbrio para celebrar o retorno do jardim.",target=5,xp=100,coins=35,special="Semente de Seren",attribute=3)
)
