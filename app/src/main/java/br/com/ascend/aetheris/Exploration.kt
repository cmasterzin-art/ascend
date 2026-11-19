package br.com.ascend.aetheris

/**
 * 1.7.2 — Generalized Regional Exploration Engine (Além das Fronteiras).
 *
 * Preserves 100% of Floresta da Persistência historical expedition IDs (exp:roots:0, exp:sanctuary:0, exp:vigil:0),
 * rules, targets, rewards, and snapshots while supporting Biblioteca das Eras (level 5) and
 * Montanhas da Disciplina (level 8) with integrated Focus, Mímir, and Caminhos metrics plus habit fallbacks.
 */

const val EXPEDITION_METRIC_HABITS = 0
const val EXPEDITION_METRIC_FOCUS_SESSIONS = 4
const val EXPEDITION_METRIC_FLASHCARD_DAYS = 5
const val EXPEDITION_METRIC_PATH_TASKS = 6
const val EXPEDITION_MIN_FOCUS_SECONDS = 300L // 1.7.3 (16.1): duração mínima efetiva de 5 minutos por sessão de Foco

data class ExpeditionPhase(
 val name: String,
 val target: Int,
 val text: String,
 val altTarget: Int = target
)

data class ExpeditionDef(
 val id: String,
 val name: String,
 val location: String,
 val premise: String,
 val consequence: String,
 val phases: List<ExpeditionPhase>,
 val xp: Int,
 val coins: Int,
 val item: String = "",
 val regionIndex: Int = 1,
 val locationTile: Int = 6,
 val integratedMetric: Int = EXPEDITION_METRIC_HABITS,
 val integratedLabel: String = "",
 val guardianName: String = "",
 val guardianTile: Int = -1
)

data class RegionNpcDef(
 val id: String,
 val name: String,
 val role: String,
 val regionIndex: Int,
 val portraitTile: Int,
 val firstMeeting: String,
 val availableLine: String,
 val partialLine: String,
 val restoredLine: String,
 val specializationFlavor: Map<String, String> = emptyMap()
)

data class ExplorableRegionDef(
 val regionIndex: Int,
 val unlockLevel: Int,
 val atlasRes: Int,
 val ornamentCell: Int,
 val seal: AetherisSealKind,
 val titleReward: String,
 val achievementId: String,
 val expeditions: List<ExpeditionDef>,
 val npc: RegionNpcDef? = null,
 val distinctionNote: String = ""
)

enum class RegionAtlasState(val label: String, val symbol: String) {
 Locked("Bloqueada", "✕"),
 Revealed("Revelada", "◇"),
 Available("Disponível", "◈"),
 InProgress("Em exploração", "⟐"),
 Completed("Concluída", "✓"),
 NoInteractiveYet("Sem conteúdo interativo ainda", "○")
}

val expeditionRelicIds = setOf(
 "mist-clasp",
 "eternal-memory-medallion",
 "firmament-compass",
 "scribes-ring",
 "boreal-vigil-brooch"
)

// Region 1 — Floresta da Persistência (preserved verbatim for 100% backwards compatibility)
val forestExpeditions = listOf(
 ExpeditionDef(
  id = "roots",
  name = "Raízes sob a Névoa",
  location = "O Marco dos Retornos",
  premise = "Sob as raízes de um pinheiro, uma pedra conserva a marca de mãos que voltavam pela mesma trilha. O Cronista encontra um sulco coberto de terra: alguém tentou proteger uma palavra da chuva.",
  consequence = "A inscrição diz apenas: voltamos. Atrás da pedra, cinco marcas indicam uma trilha que não aparece nos mapas.",
  phases = listOf(
   ExpeditionPhase("Os sinais do caminho", 5, "Uma marca reaparece em cada retorno. As pedras não indicam uma chegada; indicam o caminho de volta.")
  ),
  xp = 40,
  coins = 10,
  regionIndex = 1,
  locationTile = 6
 ),
 ExpeditionDef(
  id = "sanctuary",
  name = "O Santuário Esquecido",
  location = "O Santuário das Raízes",
  premise = "A trilha termina numa porta sem dobradiças. Dentro, as raízes sustentam o que restou do teto. Seren pede que nenhuma seja cortada antes de sabermos o que ainda mantém de pé.",
  consequence = "A água agora corre por fora das paredes. Seren deixa uma semente numa fissura preservada. O santuário volta a receber quem atravessa a floresta.",
  phases = listOf(
   ExpeditionPhase("A soleira", 2, "Darian encontra pedra firme sob a madeira podre. Levará só as peças que já não sustentam nada."),
   ExpeditionPhase("O abrigo", 2, "A primeira cobertura desvia a chuva. No chão seco, aparecem pegadas pequenas que ninguém vira antes."),
   ExpeditionPhase("As raízes guardadas", 2, "Seren recoloca a terra ao redor das raízes. A porta ficará aberta; nunca houve fechadura neste lugar.")
  ),
  xp = 60,
  coins = 15,
  regionIndex = 1,
  locationTile = 3
 ),
 ExpeditionDef(
  id = "vigil",
  name = "A Vigília da Floresta",
  location = "O Coração da Névoa",
  premise = "Uma sombra de galhos ocupa a passagem além do santuário. Não avança quando você se aproxima. O som que parecia respiração vem das árvores ao redor.",
  consequence = "O Guardião não cai. Inclina a cabeça e abre passagem. A névoa continua na floresta; agora você reconhece o que ela protege. O Cronista guarda o relato sem lhe dar um fim.",
  phases = listOf(
   ExpeditionPhase("O Chamado", 2, "A presença ergue a cabeça. Entre os galhos, uma pedra marcada repete o sinal do primeiro caminho."),
   ExpeditionPhase("A Resistência", 3, "Você percebe que a sombra tem raízes. Darian baixa a ferramenta. Não existe aqui uma peça que possa ser arrancada sem levar o resto."),
   ExpeditionPhase("O Juramento", 3, "Você deixa uma marca junto às antigas. O Guardião espera até que sua mão se afaste da pedra; só então move um dos braços.")
  ),
  xp = 100,
  coins = 25,
  item = "mist-clasp",
  regionIndex = 1,
  locationTile = 7,
  guardianName = "O Guardião da Névoa",
  guardianTile = 7
 )
)

// Region 2 — Biblioteca das Eras (unlock level 5)
val libraryExpeditions = listOf(
 ExpeditionDef(
  id = "lib_portico",
  name = "O Primeiro Manuscrito",
  location = "O Pórtico dos Escribas",
  premise = "Sob arcos de basalto tomados pelo musgo, os selos de bronze que guardavam o pórtico de entrada permanecem travados. Eydis segura uma lanterna fria diante dos entalhes quase apagados.",
  consequence = "As folhas de pedra cedem sem ruído. Sobre o púlpito central repousa o primeiro manuscrito intacto: uma lista de perguntas deixadas por antigos viajantes do Norte.",
  phases = listOf(
   ExpeditionPhase("Os selos esquecidos", 2, "Poeira de séculos se solta dos encaixes. Cada passo disciplinado devolve nitidez aos entalhes do pórtico.", altTarget = 2),
   ExpeditionPhase("A abertura do pórtico", 3, "O vento da montanha atravessa a galeria sem apagar a lanterna. A entrada da Biblioteca das Eras volta a acolher leitores.", altTarget = 2)
  ),
  xp = 55,
  coins = 15,
  regionIndex = 2,
  locationTile = 3,
  integratedMetric = EXPEDITION_METRIC_PATH_TASKS,
  integratedLabel = "Tarefas de Caminhos"
 ),
 ExpeditionDef(
  id = "lib_acervo",
  name = "As Páginas do Silêncio",
  location = "O Acervo Submerso",
  premise = "Nas câmaras inferiores, a água fria do degelo encobriu as primeiras prateleiras. Os pergaminhos preservados em estojos de chumbo aguardam mãos pacientes para serem catalogados novamente.",
  consequence = "As prateleiras baixas voltam a secar sob o calor discreto dos braseiros. Entre os estojos, Eydis encontra um anel prateado que pertencia aos antigos escribas do salão.",
  phases = listOf(
   ExpeditionPhase("A descida", 2, "Degraus úmidos levam até o nível das águas calmas. Você ergue os primeiros estojos intactos acima da maré.", altTarget = 1),
   ExpeditionPhase("Os registros perdidos", 3, "Sob a tinta esmaecida surgem anotações de quem voltou ao mesmo texto várias vezes até compreendê-lo.", altTarget = 1),
   ExpeditionPhase("A recuperação do acervo", 3, "Os volumes retornam às estantes secas. A memória do salão deixa de ser ruína e volta a ser referência viva.", altTarget = 2)
  ),
  xp = 80,
  coins = 20,
  item = "scribes-ring",
  regionIndex = 2,
  locationTile = 4,
  integratedMetric = EXPEDITION_METRIC_FLASHCARD_DAYS,
  integratedLabel = "Dias com revisões em Mímir"
 ),
 ExpeditionDef(
  id = "lib_camara",
  name = "O Juramento da Sabedoria",
  location = "A Câmara de Mímir",
  premise = "No salão mais profundo, diante de uma fonte escura que reflete apenas lanternas, ergue-se a presença imóvel do Vigia das Eras. Ele não empunha armas: apenas observa se a busca pelo saber é sincera.",
  consequence = "O Vigia das Eras inclina a fronte em silêncio. As lâmpadas ao redor da fonte acendem-se com brilho âmbar sereno. O Medalhão da Memória Eterna repousa sobre a borda de pedra, reservado a quem retorna para lembrar.",
  phases = listOf(
   ExpeditionPhase("A pergunta", 2, "O Vigia das Eras não exige respostas perfeitas; escuta a pergunta que você sustentou ao longo dos dias.", altTarget = 1),
   ExpeditionPhase("A lembrança", 3, "O que foi revisado com constância não se perde na neblina. A superfície da fonte reflete runas antigas com clareza.", altTarget = 2),
   ExpeditionPhase("O juramento", 3, "Você inscreve sua marca junto aos antigos escribas. O Vigia abre a câmara e confia o Medalhão da Memória Eterna às suas mãos.", altTarget = 2)
  ),
  xp = 120,
  coins = 30,
  item = "eternal-memory-medallion",
  regionIndex = 2,
  locationTile = 5,
  integratedMetric = EXPEDITION_METRIC_FLASHCARD_DAYS,
  integratedLabel = "Dias com revisões em Mímir",
  guardianName = "O Vigia das Eras",
  guardianTile = 7
 )
)

// Region 3 — Montanhas da Disciplina (unlock level 8)
val mountainsExpeditions = listOf(
 ExpeditionDef(
  id = "mnt_trilha",
  name = "O Caminho dos Antigos",
  location = "A Trilha de Ferro",
  premise = "Estacas de ferro cravadas no basalto marcam a antiga subida entre nevascas. Metade dos marcos sumiu sob a neve; Haldor observa a encosta sem pressa, apontando onde o terreno ainda firma os pés.",
  consequence = "As estacas da encosta voltam a ligar o vale ao passo superior. Quem sobe agora não precisa adivinhar a curva sob o vento frio.",
  phases = listOf(
   ExpeditionPhase("O primeiro passo", 2, "A subida começa quando você para de medir o cume inteiro e escolhe apenas o apoio firme diante dos pés.", altTarget = 2),
   ExpeditionPhase("Além da nevasca", 3, "Entre uma rajada e outra, o traçado de ferro reaparece. O caminho nunca deixou de existir sob a neve.", altTarget = 2)
  ),
  xp = 65,
  coins = 18,
  regionIndex = 3,
  locationTile = 3,
  integratedMetric = EXPEDITION_METRIC_PATH_TASKS,
  integratedLabel = "Tarefas de Caminhos"
 ),
 ExpeditionDef(
  id = "mnt_torre",
  name = "A Chama que Não Cessa",
  location = "A Torre da Vigília",
  premise = "No esporão de pedra que domina o desfiladeiro, o braseiro da antiga Torre da Vigília permanece frio há invernos. Haldor explica que a chama não pede heroísmo súbito, mas atenção reunida sem dispersão.",
  consequence = "O braseiro volta a arder com luz firme visível desde o vale. Entre as pedras da lareira, Haldor entrega o Broche da Vigília Boreal a quem soube sustentar o foco.",
  phases = listOf(
   ExpeditionPhase("A subida", 2, "Os degraus talhados na rocha pedem ritmo constante. Nenhuma pressa encurta uma montanha real.", altTarget = 1),
   ExpeditionPhase("A chama esquecida", 3, "As primeiras faíscas resistem ao vento gélido. Cada período de atenção silenciosa protege a brasa nascente.", altTarget = 2),
   ExpeditionPhase("O retorno da vigília", 3, "A luz da torre corta a neblina noturna. Quem observa do sopé sabe que a vigília voltou ao Norte.", altTarget = 2)
  ),
  xp = 90,
  coins = 24,
  item = "boreal-vigil-brooch",
  regionIndex = 3,
  locationTile = 4,
  integratedMetric = EXPEDITION_METRIC_FOCUS_SESSIONS,
  integratedLabel = "Sessões de Foco registradas"
 ),
 ExpeditionDef(
  id = "mnt_observatorio",
  name = "O Silêncio dos Cumes",
  location = "O Observatório do Norte",
  premise = "No ponto mais alto da cordilheira, arcos de pedra e bronze mediam as constelações boreais antes do abandono. Ali permanece o Guardião de Granito: uma presença serena de rocha e silêncio que não combate, apenas testemunha.",
  consequence = "Sob a aurora fria, os arcos de bronze alinham-se novamente ao Norte. O Guardião de Granito recua um passo e revela a Bússola do Firmamento sobre o altar de pedra. O silêncio dos cumes agora guarda também o seu nome.",
  phases = listOf(
   ExpeditionPhase("As estrelas ocultas", 2, "Acima das nuvens baixas, o céu revela pontos que nunca eram vistos do vale. A disciplina abre espaço para enxergar.", altTarget = 1),
   ExpeditionPhase("O alinhamento", 3, "Os anéis de bronze giram devagar, sem atrito. Paciência e descanso bem guardado sustentam a precisão do ajuste.", altTarget = 2),
   ExpeditionPhase("O último horizonte", 3, "O Guardião de Granito inclina-se sob a luz boreal. A Bússola do Firmamento passa às suas mãos como testemunho da travessia.", altTarget = 2)
  ),
  xp = 140,
  coins = 35,
  item = "firmament-compass",
  regionIndex = 3,
  locationTile = 5,
  integratedMetric = EXPEDITION_METRIC_FOCUS_SESSIONS,
  integratedLabel = "Sessões de Foco registradas",
  guardianName = "O Guardião de Granito",
  guardianTile = 7
 )
)

val eydisNpc = RegionNpcDef(
 id = "eydis",
 name = "Eydis",
 role = "Arquivista das Eras",
 regionIndex = 2,
 portraitTile = 6,
 firstMeeting = "As estantes desta casa sobreviveram ao gelo, mas não ao esquecimento. Se você trouxe perguntas pacientes, já trouxe metade do que precisamos para reabrir o pórtico.",
 availableLine = "Há câmaras ainda seladas sob o pó. Escolha uma expedição quando estiver pronto — seja pelos seus hábitos ou pelas suas revisões na Biblioteca de Mímir.",
 partialLine = "As primeiras lanternas voltaram a iluminar as galerias. Cada volume recuperado lembra que conhecimento verdadeiro nasce de retornar ao que importa.",
 restoredLine = "A Biblioteca das Eras respira novamente. O que você reconstruiu aqui não são apenas paredes de pedra: é a certeza de que a memória pode atravessar qualquer inverno.",
 specializationFlavor = mapOf(
  "erudito" to "Um Erudito reconhece o peso de uma margem bem anotada. Eydis aponta para os manuscritos mais raros do salão.",
  "arquimago_runico" to "Seus olhos de Arquimago Rúnico leem os vínculos entre selos antigos como se fossem páginas de um mesmo tratado.",
  "cartografo" to "Como Cartógrafo, você percebe que o acervo também é um mapa — cada livro aponta uma travessia.",
  "tecelao_de_runas" to "Eydis nota a precisão de Tecelão de Runas na maneira como você organiza cada fragmento recuperado.",
  "mestre_da_forja" to "Mesmo entre pergaminhos, mãos de Mestre da Forja sabem restaurar fechos de bronze e dobradiças antigas.",
  "druida" to "Um Druida entende por que o musgo não é inimigo da pedra — apenas sinal de quanto tempo o salão esperou.",
  "sentinela" to "A constância de Sentinela mantém acesas as lanternas que outros deixariam apagar no primeiro vento.",
  "berserker" to "Sua determinação de Berserker rompe a inércia dos portões mais antigos sem perder o respeito pelo acervo.",
  "protetor" to "Um Protetor sabe que guardar um livro é proteger a voz de quem o escreveu antes de nós.",
  "patrulheiro" to "O olhar atento de Patrulheiro encontra estojos escondidos sob as sombras das colunas."
 )
)

val haldorNpc = RegionNpcDef(
 id = "haldor",
 name = "Haldor",
 role = "Vigia das Montanhas",
 regionIndex = 3,
 portraitTile = 6,
 firstMeeting = "A montanha não se importa com pressa nem com promessas grandiosas. Ela respeita apenas quem dá um passo firme, descansa quando precisa e retorna no dia seguinte.",
 availableLine = "Os cumes aguardam sem cobrança. Dias de descanso também fazem parte da subida — escolha a trilha que melhor acompanha seu ritmo real.",
 partialLine = "O fogo da encosta já pode ser visto do vale. Não foi força brusca que nos trouxe até aqui; foi paciência repetida em silêncio.",
 restoredLine = "O Observatório do Norte voltou a mirar o firmamento. Você atravessou o frio sem transformar disciplina em castigo. Os cumes guardam o seu passo.",
 specializationFlavor = mapOf(
  "sentinela" to "Haldor acena em reconhecimento: um Sentinela sabe sustentar a vigília longa quando a neblina fecha a vista.",
  "berserker" to "Haldor sorri sob a barba grisalha: a força de Berserker abre trilha na neve, mas aqui ela aprende o compasso do vento.",
  "cartografo" to "Com olhos de Cartógrafo, você lê as curvas de nível e os marcos de ferro antes mesmo de pisar na encosta.",
  "patrulheiro" to "Um Patrulheiro conhece o valor de medir o fôlego e observar o horizonte antes de avançar.",
  "protetor" to "A serenidade de Protetor mantém o abrigo seguro para quem atravessa dias difíceis.",
  "druida" to "Um Druida escuta o ritmo das estações — sabe quando subir e quando deixar a neve assentar.",
  "erudito" to "Haldor aponta os astrolábios de pedra: um Erudito saberá ler as marcas deixadas pelos antigos astrônomos.",
  "arquimago_runico" to "As runas gravadas no observatório respondem à clareza paciente de um Arquimago Rúnico.",
  "mestre_da_forja" to "As estacas da Trilha de Ferro lembram o ofício de Mestre da Forja: temperadas para suportar muitos invernos.",
  "tecelao_de_runas" to "A harmonia dos anéis de bronze no observatório revela a precisão que todo Tecelão de Runas admira."
 )
)

val explorableRegions = listOf(
 ExplorableRegionDef(
  regionIndex = 1,
  unlockLevel = 3,
  atlasRes = R.drawable.forest_atlas,
  ornamentCell = 2,
  seal = AetherisSealKind.PineBough,
  titleReward = "Testemunha das Raízes",
  achievementId = "forest",
  expeditions = forestExpeditions
 ),
 ExplorableRegionDef(
  regionIndex = 2,
  unlockLevel = 5,
  atlasRes = R.drawable.realm_library_atlas,
  ornamentCell = 0,
  seal = AetherisSealKind.OpenBook,
  titleReward = "Guardião das Eras",
  achievementId = "library_ages",
  expeditions = libraryExpeditions,
  npc = eydisNpc,
  distinctionNote = "Região narrativa de exploração em Aetheris — distinta da Biblioteca de Mímir, seu caderno funcional de notas e flashcards."
 ),
 ExplorableRegionDef(
  regionIndex = 3,
  unlockLevel = 8,
  atlasRes = R.drawable.realm_mountains_atlas,
  ornamentCell = 1,
  seal = AetherisSealKind.MountainPeaks,
  titleReward = "Testemunha dos Cumes",
  achievementId = "mountains_discipline",
  expeditions = mountainsExpeditions,
  npc = haldorNpc
 )
)

val allExpeditions: List<ExpeditionDef> = explorableRegions.flatMap { it.expeditions }

fun explorableRegion(regionIndex: Int): ExplorableRegionDef? =
 explorableRegions.find { it.regionIndex == regionIndex }

fun expeditionById(id: String): ExpeditionDef? = allExpeditions.find { it.id == id }

fun expeditionRegion(e: ExpeditionDef): ExplorableRegionDef =
 explorableRegions.first { it.regionIndex == e.regionIndex }

fun expeditionQuestId(e: ExpeditionDef, phase: Int) = "exp:${e.id}:$phase"

fun expeditionRef(id: String): Pair<ExpeditionDef, Int>? {
 val p = id.split(':')
 if (p.size != 3 || p[0] != "exp") return null
 val e = allExpeditions.find { it.id == p[1] } ?: return null
 val n = p[2].toIntOrNull() ?: return null
 return if (n in e.phases.indices) e to n else null
}

fun isExpedition(q: Quest) = expeditionRef(q.id) != null
fun isPersonalSaga(q: Quest) = q.sagaStart.isNotBlank() && !isExpedition(q)

fun expeditionClaimValid(s: GameState, e: ExpeditionDef, phase: Int): Boolean {
 val regionExps = expeditionRegion(e).expeditions
 val index = regionExps.indexOf(e)
 if (index > 0 && !expeditionComplete(s, regionExps[index - 1])) return false
 if (phase > 0 && !expeditionClaimValid(s, e, phase - 1)) return false
 val c = s.claims.find { it.questId == expeditionQuestId(e, phase) } ?: return false
 return rawClaimValid(c, s)
}

fun expeditionComplete(s: GameState, e: ExpeditionDef) =
 expeditionClaimValid(s, e, e.phases.lastIndex)

fun expeditionAvailable(s: GameState, e: ExpeditionDef): Boolean {
 val reg = expeditionRegion(e)
 val index = reg.expeditions.indexOf(e)
 return s.level >= reg.unlockLevel && (index == 0 || expeditionComplete(s, reg.expeditions[index - 1]))
}

fun expeditionPhaseIndex(s: GameState, e: ExpeditionDef) =
 e.phases.indices.firstOrNull { !expeditionClaimValid(s, e, it) } ?: e.phases.lastIndex

fun expeditionState(s: GameState, e: ExpeditionDef): String {
 if (expeditionComplete(s, e)) return "Recompensa resgatada"
 if (!expeditionAvailable(s, e)) return "Bloqueada"
 val phase = expeditionPhaseIndex(s, e)
 val q = s.quests.find { it.id == expeditionQuestId(e, phase) } ?: return "Disponível"
 if (questProgress(q, s) >= q.target) return if (phase == e.phases.lastIndex) "Concluída · resgate disponível" else "Marco alcançado"
 return "Em andamento"
}

fun regionConfirmedMarks(s: GameState, regionIndex: Int): Int {
 val reg = explorableRegion(regionIndex) ?: return 0
 return reg.expeditions.sumOf { e -> e.phases.indices.count { expeditionClaimValid(s, e, it) } }
}

fun regionTotalMarks(regionIndex: Int): Int =
 explorableRegion(regionIndex)?.expeditions?.sumOf { it.phases.size } ?: 0

fun regionProgress(s: GameState, regionIndex: Int): Int {
 val total = regionTotalMarks(regionIndex)
 if (total <= 0) return 0
 return regionConfirmedMarks(s, regionIndex) * 100 / total
}

/** 0 = abandoned/ruins, 1 = partially restored, 2 = fully restored (strictly tied to real expedition milestones). */
fun regionRestorationStage(s: GameState, regionIndex: Int): Int {
 val reg = explorableRegion(regionIndex) ?: return 0
 return when {
  expeditionComplete(s, reg.expeditions.last()) -> 2
  expeditionComplete(s, reg.expeditions.first()) || regionConfirmedMarks(s, regionIndex) >= 2 -> 1
  else -> 0
 }
}

// Preserved Forest compatibility functions
fun forestProgress(s: GameState): Int = regionProgress(s, 1)
fun forestStage(s: GameState): Int = when {
 expeditionComplete(s, forestExpeditions.last()) -> 2
 expeditionComplete(s, forestExpeditions.first()) -> 1
 else -> 0
}
fun sanctuaryStage(s: GameState): Int = when {
 expeditionComplete(s, forestExpeditions[1]) -> 2
 expeditionClaimValid(s, forestExpeditions[1], 0) -> 1
 else -> 0
}

fun libraryProgress(s: GameState): Int = regionProgress(s, 2)
fun libraryStage(s: GameState): Int = regionRestorationStage(s, 2)
fun mountainsProgress(s: GameState): Int = regionProgress(s, 3)
fun mountainsStage(s: GameState): Int = regionRestorationStage(s, 3)

fun validClaim(c: Claim, s: GameState): Boolean =
 expeditionRef(c.questId)?.let { (e, n) -> expeditionClaimValid(s, e, n) } ?: rawClaimValid(c, s)

fun regionAtlasState(s: GameState, regionIndex: Int): RegionAtlasState {
 val levelReq = regionLevels.getOrElse(regionIndex) { 99 }
 if (s.level < levelReq) return RegionAtlasState.Locked
 if (regionIndex == 0) {
  val stages = s.assets.filter { it.kind == "building" }.sumOf { it.stage }
  return when {
   stages >= 10 -> RegionAtlasState.Completed
   stages > 0 -> RegionAtlasState.InProgress
   else -> RegionAtlasState.Available
  }
 }
 val reg = explorableRegion(regionIndex) ?: return RegionAtlasState.NoInteractiveYet
 val prog = regionProgress(s, regionIndex)
 val hasStarted = reg.expeditions.any { e -> s.quests.any { it.id == expeditionQuestId(e, 0) } }
 return when {
  prog >= 100 -> RegionAtlasState.Completed
  prog > 0 || hasStarted -> RegionAtlasState.InProgress
  else -> RegionAtlasState.Available
 }
}

fun npcDialogue(npc: RegionNpcDef, s: GameState): Pair<String, String?> {
 val stage = regionRestorationStage(s, npc.regionIndex)
 val startedAny = explorableRegion(npc.regionIndex)?.expeditions?.any { e ->
  s.quests.any { it.id == expeditionQuestId(e, 0) }
 } == true
 val base = when {
  stage >= 2 -> npc.restoredLine
  stage == 1 -> npc.partialLine
  startedAny -> npc.availableLine
  else -> npc.firstMeeting
 }
 val spec = activeSpecialization(s)
 val flavor = spec?.let { npc.specializationFlavor[it.id] }
 return base to flavor
}

fun expeditionMetricUnit(metric: Int, target: Int): String = when (metric) {
 EXPEDITION_METRIC_FOCUS_SESSIONS -> if (target == 1) "sessão de foco registrada" else "sessões de foco registradas"
 EXPEDITION_METRIC_FLASHCARD_DAYS -> if (target == 1) "dia com revisão em Mímir" else "dias distintos com revisão em Mímir"
 EXPEDITION_METRIC_PATH_TASKS -> if (target == 1) "tarefa de Caminho concluída" else "tarefas de Caminhos concluídas"
 else -> if (target == 1) "conclusão" else "conclusões"
}

/**
 * Computes baseline snapshot string for a given expedition metric so pre-phase progress
 * is never counted and consecutive phases never reuse the same activity.
 */
fun expeditionBaselineFor(metric: Int, ids: List<String>, s: GameState): String = when (metric) {
 EXPEDITION_METRIC_FOCUS_SESSIONS -> s.focusSessions
  .filter { it.registered && it.elapsedSeconds >= EXPEDITION_MIN_FOCUS_SECONDS && it.status != "cancelled" }
  .sortedBy { it.id }
  .joinToString(";") { "${it.id}=1" }
 EXPEDITION_METRIC_FLASHCARD_DAYS -> s.flashcardReviews
  .map { it.reviewDate }
  .distinct()
  .sorted()
  .joinToString(";") { "$it=1" }
 EXPEDITION_METRIC_PATH_TASKS -> s.pathTasks
  .filter { it.completed }
  .sortedBy { it.id }
  .joinToString(";") { "${it.id}=1" }
 else -> s.records
  .filter { it.habitId in ids }
  .joinToString(";") { "${it.id}=${it.progress}" }
}

/**
 * Evaluates expanded expedition metrics (4=Focus Sessions >= 5 min, 5=Mímir Flashcard Days, 6=Path Tasks).
 * Archiving a completed PathTask preserves expedition credit (16.2); reopening or deleting removes it.
 */
fun expeditionMetricProgress(
 q: Quest,
 s: GameState,
 minFocusSeconds: Long = EXPEDITION_MIN_FOCUS_SECONDS
): Long {
 val baseline = parseBaseline(q.sagaBaseline)
 return when (q.metric) {
  EXPEDITION_METRIC_FOCUS_SESSIONS -> s.focusSessions.count { f ->
   f.registered && f.elapsedSeconds >= minFocusSeconds && f.status != "cancelled" &&
    f.date >= q.sagaStart && (q.deadline.isBlank() || f.date <= q.deadline) &&
    f.id !in baseline
  }.toLong()
  EXPEDITION_METRIC_FLASHCARD_DAYS -> s.flashcardReviews
   .asSequence()
   .map { it.reviewDate }
   .filter { d -> d >= q.sagaStart && (q.deadline.isBlank() || d <= q.deadline) && d !in baseline }
   .distinct()
   .count()
   .toLong()
  EXPEDITION_METRIC_PATH_TASKS -> s.pathTasks.count { t ->
   t.completed && t.id !in baseline &&
    (t.completedDate.isBlank() || (t.completedDate >= q.sagaStart && (q.deadline.isBlank() || t.completedDate <= q.deadline)))
  }.toLong()
  else -> 0L
 }
}

fun expeditionPhaseQuest(
 e: ExpeditionDef,
 phase: Int,
 ids: List<String>,
 s: GameState,
 metric: Int = EXPEDITION_METRIC_HABITS
): Quest {
 val final = phase == e.phases.lastIndex
 val def = e.phases[phase]
 val effectiveMetric = if (metric == EXPEDITION_METRIC_HABITS || metric == e.integratedMetric) metric else EXPEDITION_METRIC_HABITS
 val phaseTarget = if (effectiveMetric == EXPEDITION_METRIC_HABITS) def.target else def.altTarget
 return Quest(
  id = expeditionQuestId(e, phase),
  name = "${e.name} · ${def.name}",
  description = def.text,
  target = phaseTarget,
  metric = effectiveMetric,
  habitIds = if (effectiveMetric == EXPEDITION_METRIC_HABITS) ids.sorted().joinToString(",") else "",
  xp = if (final) e.xp else 0,
  coins = if (final) e.coins else 0,
  sagaStart = today(),
  sagaBaseline = expeditionBaselineFor(effectiveMetric, ids, s)
 )
}

data class ExplorationJournalEntry(
 val activeExpeditions: List<Triple<ExpeditionDef, Int, Quest>>,
 val nextAvailable: ExpeditionDef?,
 val totalConfirmedMarks: Int,
 val totalAvailableMarks: Int,
 val lastDiscovery: Pair<String, String>?
)

fun explorationJournal(s: GameState): ExplorationJournalEntry {
 val active = allExpeditions.mapNotNull { e ->
  if (expeditionComplete(s, e) || !expeditionAvailable(s, e)) null
  else {
   val p = expeditionPhaseIndex(s, e)
   val q = s.quests.find { it.id == expeditionQuestId(e, p) }
   if (q != null) Triple(e, p, q) else null
  }
 }
 val nextAvail = allExpeditions.firstOrNull { e ->
  expeditionAvailable(s, e) && !expeditionComplete(s, e) && s.quests.none { it.id == expeditionQuestId(e, 0) }
 }
 val totalConfirmed = explorableRegions.sumOf { regionConfirmedMarks(s, it.regionIndex) }
 val totalMarks = explorableRegions.sumOf { regionTotalMarks(it.regionIndex) }
 val lastClaim = s.claims
  .filter { expeditionRef(it.questId) != null && validClaim(it, s) }
  .sortedWith(compareByDescending<Claim> { it.date }.thenByDescending { it.id })
  .firstOrNull()
 val lastDiscovery = lastClaim?.let { c ->
  expeditionRef(c.questId)?.let { (e, n) ->
   "${regions[e.regionIndex]} · ${e.name}" to (if (n == e.phases.lastIndex) e.consequence else e.phases[n].text)
  }
 }
 return ExplorationJournalEntry(active, nextAvail, totalConfirmed, totalMarks, lastDiscovery)
}
