# Mudanças — 1.4.5 · As Vozes de Aetheris

- Navegação inferior própria em ferro com cinco ícones originais Canvas, seleção esculpida, resposta de 160 ms, insets do sistema e rótulos sem truncamento.
- AetherisTabs nos menus de Herói, Missões, Mundo, Hábitos e Grimório: opções completas em duas/três colunas, sem categorias de navegação parcialmente cortadas. Abas são salvas; o Herói preserva rolagem por seção.
- Crônica da Semana nas Crônicas: estatísticas derivadas, histórico civil de segunda a domingo, narrativa condicional local e atualização após correções.
- Salão das Memórias pelo Perfil e Crônicas, filtros e inscrições com datas reais/disponíveis; conversas reconhecidas guardam agora uma cópia do texto.
- Sagas: abandono confirmado com progresso congelado, arquivo e nova tentativa com UUID/linha de base próprios; histórico, seletor Android de prazo e aviso de alterações não salvas no editor.
- Retrato ampliado, moldura base de classe, artefatos maiores nos detalhes e estado equipado explícito.
- Novas falas locais de Cronista/Elyra/Darian/Seren, painéis com símbolos e histórico das conversas reconhecidas.
- Cinco efeitos originais locais, opção de ativação e volume, respeitando modo silencioso e volume de mídia; sem música contínua ou vibração.
- Room v5, migração aditiva 4 → 5 e backup v6 aceitando v1–v6; versionCode 7, assinatura preservada.

# Mudanças — 1.4.0 · O Juramento do Norte

- Sagas Pessoais na área de Missões, seis modelos e criação personalizada; seleção de hábitos, métrica de conclusões/minutos/páginas, meta, prazo opcional, XP/moedas editáveis, marcos narrativos de 25/50/75/100% e resgate único.
- Fotografia dos registros no início, incluindo o dia atual; progresso anterior não conta. Correções recalculam progresso e recompensa resgatada com o ledger existente. Hábitos/métrica ficam fixos após iniciar; outros termos podem ser editados até o resgate.
- Novo SagaButton primário/secundário/destrutivo, estados pressionado/desativado/carregando/concluído, bronze com relevo e resposta curta. Ações compartilhadas, formulários, loja e resgates usam o componente.
- Jornada com região, emblema da classe, percentual do dia, pendências e próxima atividade. Medidores refinados e ações de hábitos em ferro recortado.
- Recompensas consolidadas por transação: valores reais de XP, saldo disponível e atributos; nível, conquistas raras, artefatos e restauração no mesmo painel quando ocorrerem juntos.
- Diálogos contextuais locais com prioridade e confirmação persistida; Cronista, restauração de moradores, saga, nível, conquista e retorno reconhecidos sem IA externa.
- Revisão pontual de oito capítulos, preservando os nove títulos, acontecimentos e desbloqueios; códice com parágrafos, diálogos e progresso de capítulos.
- Cinco emblemas originais, descrições de classe, pré-requisitos/estados mais explícitos na árvore, transição breve de reconstrução respeitando redução de movimento.
- Room v4 aditiva e backup v5 aceitando v1–v5. Mesmo certificado desde a 1.1, applicationId preservado, versionCode 6.

# Mudanças — 1.3.1 · Grimório e coerência artística

- Grimório dos Hábitos com 102 modelos, nove categorias, busca por palavras/nomes narrativos, seis filtros e quatro ordenações; formulário completo antes de adicionar.
- Seis modelos de rotina com prévia, seleção parcial, edição de cada novo hábito, reutilização de existentes e gravação transacional sem bônus de XP.
- Sugestões por interesses, calculadas localmente, dispensáveis. Nomes narrativos opcionais preservam o nome funcional. Contagem de sequência opcional por hábito, desativada inicialmente nos modelos de descanso/autocuidado.
- Equipamentos e companheiros pintados em nichos fora do retrato; 17 itens com imagens próprias, mantendo ações de equipar/retirar, raridades e procedência.
- Marcadores do atlas em pedra esculpida, símbolos regionais, fratura para bloqueio e contorno de seleção.
- Quinze cenas de reconstrução: cinco construções × três estágios. Ruínas, andaimes e edifícios iluminados diferenciam os estados visualmente.
- Botões em couro/metal, medidores entalhados e cartões de hábitos com dificuldade e nome narrativo; cabeçalho do Grimório acompanha a rolagem em telas pequenas.
- Room 3 com migração explícita 2 → 3 de três colunas; mantém 1 → 2. Backup 4 aceita 1–4; applicationId, certificado e cálculo de recompensas preservados.
- versionName 1.3.1, versionCode 5. Evidências em [VALIDACAO.md](VALIDACAO.md), [guia do Grimório](docs/GRIMORIO.md) e [revisão visual](docs/VISUAL-1.3.1.md).

---

# Mudanças — 1.3.0 · Redesign nórdico sombrio

## Telas

- Jornada recomposta em torno do painel heroico, com fiorde, retrato, nível, XP entalhado e atributos; progresso diário e hábitos continuam com ações diretas.
- Perfil com retrato ilustrado adulto, três tons de pele e ornamentos/equipamentos sobrepostos. Trajes, elmo, arma, amuleto, moldura, companheiro e fundo têm leitura visual própria.
- Inventário como arsenal de couro e ferro: retrato central, sete espaços, ícones detalhados, selos de raridade, detalhes de procedência e coleção filtrável. Abas do Herói permanecem fixas durante a rolagem.
- Atlas ilustrado com geografia, fiordes, montanhas, florestas, ruínas e névoa; marcadores selecionáveis preservam desbloqueios reais.
- Livro de missões com capa de couro, páginas gastas e costuradas, selos, objetivos e espólios separados. Chefes recebem uma ilustração original de presença mítica.
- Vila com cenário pintado e sinais luminosos de restauração integrados à paisagem. Crônicas passam a ser lidas em páginas de códice.

## Componentes e atmosfera

- Paleta de carvão, ardósia, bronze, marfim e gelo; tons de texto ajustados para contraste nos temas claro e escuro.
- Fonte Cinzel incorporada ao APK para títulos; corpo em fonte de sistema legível. Licença OFL incluída.
- Novos componentes HeroSaga, SagaCover, RuneSeal, SagaDivider e materialSurface; refinamento de Avatar, Landscape, Manuscript, RelicArt, AetherisMap, Meter, botões, chips, slots, notificações e celebração de nível.
- Grão discreto e determinístico, costuras, rebites, cantos cortados e iluminação fria/âmbar. As imagens são WebP locais, sem dependências remotas.
- Transição suave entre abas do Herói, brilho breve ao mudar equipamento e barras animadas. Redução de movimento desativa esses efeitos; modo minimalista simplifica a Jornada e remove grão dos painéis.
- Rótulos acessíveis para equipamentos e interruptores; alvos de toque de pelo menos 48 dp nas ações redesenhadas. Ajustes para 320 dp e texto em 130%.

## Compatibilidade

- VersionCode 4, versionName 1.3.0. Mesmo applicationId e certificado dos APKs 1.1/1.2 fornecidos.
- Sem mudanças no repositório de dados, regras de recompensa, esquema Room v2 ou formato de backup v3.
- 44 testes aprovados. Revisão visual documentada em [docs/VISUAL-1.3.md](docs/VISUAL-1.3.md).

---

# Mudanças — 1.2.0 · O Despertar de Aetheris

## Referências visuais e interação

- Livro de missões com papel texturizado, bordas ornamentais, objetivos, recompensas e resgate funcional. As abas e a edição dos contratos foram preservadas.
- Mapa vetorial em pergaminho, com rio, florestas, montanhas, trilhas, rosa dos ventos e seis regiões selecionáveis. Os bloqueios mostram o nível necessário.
- Inventário com retrato central, espaços de cabeça, corpo/capa, acessório e arma. Coleção em duas colunas, categorias, cinco raridades, origem e data de obtenção.
- Equipar e remover altera o retrato; fundos, molduras, companheiros e decoração continuam disponíveis. A lanterna ilumina a cena da vila.
- Atributos na Jornada e silhueta original para os chefes opcionais.

## Progressão e narrativa

- Seis habilidades em três caminhos: Sabedoria, Perseverança e Criação. Um ponto a cada três níveis; cada habilidade custa um ponto, exige nível e respeita o pré-requisito.
- Habilidades concedem seis cosméticos exclusivos, persistidos em transação; a loja não permite comprá-los diretamente. Não alteram recompensas de hábitos.
- Elmo do Viajante, Lâmina de Prata e Amuleto de Aether disponíveis no mercador.
- Elyra, Darian e Seren oferecem três missões com troféus; os diálogos dos moradores acompanham as três etapas da construção.
- Cinco títulos por atributos e um título de prestígio no nível 50.
- Habilidades conquistadas permanecem após correções de XP; novos pontos só aparecem após compensar o nível, evitando duplicação.

## Compatibilidade

- Mesmo applicationId e certificado do APK 1.1; versionCode 3, versionName 1.2.0.
- Room continua em v2, sem alteração de tabelas ou migração destrutiva. Itens e habilidades usam a coleção de assets existente.
- Backup v3, com leitura das versões 1, 2 e 3. Backups novos devem ser restaurados na 1.2.
- Equipamento utiliza os dados atuais do banco, preservando tipo e exclusividade de espaço.
- Sem permissão INTERNET, serviços pagos, imagens remotas ou fontes baixadas durante o uso.

Este é um incremento funcional do documento de expansão. A lista de recursos entregues e pendentes está em [PROGRESSO.md](PROGRESSO.md).

---

# Mudanças — 1.1.0

## Jornada e apresentação

- Cena original de Aetheris em destaque, com iluminação e restauração ligadas às construções existentes.
- Personagem, progresso de nível, resumo do dia, semana de consistência e pendências acessíveis na Jornada.
- Paletas escura e clara, opção de seguir o sistema e contraste ajustado para textos e atributos.
- Modo minimalista e redução de animações em barras, celebração e transições.
- Avisos mostram o XP efetivamente recebido. Missões prontas podem ser resgatadas na Jornada.

## Hábitos

- Busca por nome/categoria, filtro de estado e filtro de atributo.
- Detalhes com sequência atual, melhor sequência histórica, programação, calendário recente e edição de registros.
- Criação com campos essenciais primeiro e personalizações avançadas recolhidas.
- Atalhos para acrescentar quantidades ou minutos no registro, sempre preservando o teto e os snapshots.
- A visão de datas passadas respeita a programação histórica mesmo após arquivar um hábito.

## Descanso e estatísticas

- Descanso global por data, reversível pelo calendário. Nenhuma recompensa ou conclusão é apagada.
- Lembretes locais também ignoram descansos e retomam após intervalos prolongados, inclusive depois de reiniciar o aparelho.
- Sequências ignoram descansos planejados; registros extras continuam rendendo XP.
- A taxa considera apenas metas programadas e respectivas conclusões. Descansos e atividades não previstas ficam fora da razão.
- Gráfico de linha de XP, comparação com o período anterior, recorde histórico e distribuição por atributo.
- A Jornada atualiza a data local mesmo quando permanece aberta na virada do dia.

## Mundo e dados

- Quatro capítulos originais novos: As Promessas do Bosque, A Cartógrafa do Silêncio, A Fortaleza da Vontade e A Aurora de Aetheris.
- Banco Room v2 com migração 1 → 2 sem apagar as tabelas existentes.
- Restauração de backup disponível na tela de boas-vindas, sem criar um personagem provisório.
- Backups v2 incluem descansos. Backups v1 continuam sendo aceitos e validados.
- applicationId preservado; versionCode 2 e versionName 1.1.0.

Os limites ainda existentes estão em `PROGRESSO.md`. A assinatura deste APK é diferente da 1.0; siga `INSTALACAO.md` antes de trocar de versão.
