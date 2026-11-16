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
