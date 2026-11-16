package br.com.ascend.aetheris

// Content definitions are independent of owned items; all unlocks use the existing transactional store.
data class SkillDef(val id:String,val name:String,val path:String,val level:Int,val parent:String?,val item:String,val description:String)
val skillDefs=listOf(
    SkillDef("scholar","Olhar do Erudito","Sabedoria",3,null,"scholar-bg","Revela um fundo de biblioteca para seu retrato."),
    SkillDef("runes","Guardião dos Tomos","Sabedoria",8,"scholar","rune-frame","Concede a moldura das runas recuperadas."),
    SkillDef("ember","Chama Constante","Perseverança",3,null,"ember-amulet","Concede o amuleto da chama, exposto no nicho de relíquias."),
    SkillDef("will","Vontade Inabalável","Perseverança",8,"ember","will-helm","Concede a coroa da vontade."),
    SkillDef("craft","Mãos do Artífice","Criação",3,null,"craft-blade","Forja uma lâmina cerimonial, sem bônus de XP."),
    SkillDef("dawn","Arquiteto da Aurora","Criação",8,"craft","dawn-outfit","Concede o traje da aurora para seu personagem.")
)
fun skillPoints(s:GameState)=(s.level/3-s.assets.count{it.kind=="skill"}).coerceAtLeast(0)
fun rarity(item:ShopItem)=when(item.id){"will-helm","dawn-outfit"->4;"ignis","rune-frame","craft-blade"->3;"frame","corvus","armor","scholar-bg"->2;"cloak","lyra","ember-amulet"->1;else->0}
val rarityNames=listOf("Comum","Incomum","Raro","Épico","Lendário")
fun itemOrigin(item:ShopItem)=skillDefs.find{it.item==item.id}?.let{"Habilidade: ${it.name}"}?:"Mercador · ${item.cost} Fragmentos"
val equipmentSlots=listOf("head" to "Cabeça","outfit" to "Corpo / capa","accessory" to "Acessório","weapon" to "Arma cosmética","companion" to "Companheiro","frame" to "Moldura","background" to "Fundo")
fun specializationTitles(s:GameState):List<String> = buildList {
    val names=listOf("Aprendiz das Runas","Caminhante de Ferro","Guardião dos Rituais","Voz do Santuário","Mãos da Aurora")
    names.forEachIndexed{i,n->if(1+s.points(i)/50>=10)add(n)}
    if(s.level>=50)add("Lenda do Primeiro Sol")
}
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
