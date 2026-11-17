# ASCEND 1.6 — O Renascimento de Aetheris

A entrega combina nova composição visual e produtividade persistida. As comparações abaixo são renderizações nativas de Compose/Views/RemoteViews, não mockups. `ProductVisualTourTest` foi executado com a mesma fixture no código 1.5.2 (`67c3bd1`) em worktree isolado e na 1.6. Os dados de teste são gravados pelo repositório real, sem conteúdo fictício incluído no APK.

## Comparações com a 1.5.2

| Tela | 1.5.2 | 1.6 |
|---|---|---|
| Jornada | <img src="screenshots/v1.5.2/tour-jornada.png" width="230" alt="Jornada 1.5.2"> | <img src="screenshots/tour-jornada.png" width="230" alt="Jornada 1.6 com retrato compacto e painel do dia"> |
| Hábitos | <img src="screenshots/v1.5.2/tour-habitos.png" width="230" alt="Livro anterior"> | <img src="screenshots/tour-habitos.png" width="230" alt="Livro com acessos, controles e cartões operacionais"> |
| Missões | <img src="screenshots/v1.5.2/tour-missoes.png" width="230" alt="Contratos anteriores"> | <img src="screenshots/tour-missoes.png" width="230" alt="Contratos separados por objetivo, estado e recompensa"> |
| Sagas | <img src="screenshots/v1.5.2/tour-sagas.png" width="230" alt="Sagas anteriores"> | <img src="screenshots/tour-sagas.png" width="230" alt="Superfície heroica e cerimonial das Sagas"> |
| Herói | <img src="screenshots/v1.5.2/tour-heroi.png" width="230" alt="Perfil anterior"> | <img src="screenshots/tour-heroi.png" width="230" alt="Ficha com retrato lateral e métricas"> |
| Inventário | <img src="screenshots/v1.5.2/tour-inventario.png" width="230" alt="Inventário anterior"> | <img src="screenshots/tour-inventario.png" width="230" alt="Coleção em superfície de artefato com nichos preservados"> |
| Crônica | <img src="screenshots/v1.5.2/tour-cronica.png" width="230" alt="Crônica anterior"> | <img src="screenshots/tour-cronica.png" width="230" alt="Crônica no pergaminho do DS2"> |
| Memórias | <img src="screenshots/v1.5.2/tour-memorias.png" width="230" alt="Memórias anteriores"> | <img src="screenshots/tour-memorias.png" width="230" alt="Inscrições em páginas com tinta sépia"> |
| Widgets | <img src="screenshots/v1.5.2/tour-widget.png" width="250" alt="Widget anterior"> | <img src="screenshots/tour-widget.png" width="250" alt="Widget atual com maior leitura e controles"> |
| Atlas | <img src="screenshots/v1.5.2/tour-atlas.png" width="230" alt="Atlas anterior"> | <img src="screenshots/tour-atlas.png" width="230" alt="Atlas preservado dentro dos componentes refinados"> |

## O que mudou de fato

A Jornada deixa o grande painel ilustrado para o Perfil e usa retrato de 64 dp, identidade/XP, progresso diário, acessos e lista operacional virtualizada. Pendências precedem concluídos sem mudar a ordem permanente dos hábitos. O ganho de espaço é de composição, não apenas de margem.

Os contratos deixam de usar o mesmo documento bege para tudo: diários/semanais têm apresentação operacional, épicos/chefes e Sagas recebem contexto cerimonial. Objetivo, estado, narrativa e valores ficam em blocos legíveis. O Perfil passa a ser uma ficha lateral com números em pares e identidade em seção separada. O inventário conserva os nichos e ganha superfície própria de coleção, contagens e inspeção.

As Memórias passam da linha externa a inscrições paginadas com data/categoria/selo integrados. Crônica e narrativa usam o pergaminho com tinta sépia e corpo funcional. Abas, botões, cabeçalhos, métricas e medidores recebem o sistema compartilhado. As pinturas, preços, classes e itens continuam os anteriores.

O Atlas tem refinamento dos componentes e mantém a pintura/geografia; não foi redesenhado do zero. Vila, Floresta e Guardião preservam cenas/transformações e usam os controles compartilhados. Não se afirma ter criado novas pinturas ou novas regiões.

## Novos fluxos reais

| Planejador | Dia selecionado | Ajuste pontual |
|---|---|---|
| <img src="screenshots/planejador-160.png" width="230" alt="Semana real"> | <img src="screenshots/planejador-dia-160.png" width="230" alt="Dia após dispensa pontual"> | <img src="screenshots/planejador-ajuste-160.png" width="230" alt="Escolha entre normal, incluir e dispensar"> |

| Central de Análises | Análise individual | Foco registrado |
|---|---|---|
| <img src="screenshots/analises-160.png" width="230" alt="Indicadores e curva reais"> | <img src="screenshots/analise-habito-160.png" width="230" alt="Detalhes estatísticos de um hábito"> | <img src="screenshots/foco-historico-160.png" width="230" alt="Sessão concluída e totais"> |

[Detalhes da sessão](screenshots/foco-sessao-160.png) · [Jornada com registros](screenshots/jornada-160.png).

## Tela estreita e acesso

| Planejador 320 dp / 130% / claro | Análises na mesma configuração | Foco sem sessões |
|---|---|---|
| <img src="screenshots/planejador-320-160.png" width="230" alt="Planejador estreito"> | <img src="screenshots/analises-320-160.png" width="230" alt="Análises estreitas"> | <img src="screenshots/foco-vazio-320-160.png" width="230" alt="Histórico vazio intencional"> |

Também foram preservados os cenários anteriores de 411 dp, claro/escuro, minimalista, redução de movimento, nomes longos e widgets reduzidos. A revisão das capturas guiou a redução do cabeçalho heroico e a separação de métricas/ações. Foco é configurado em formulário completo para evitar o campo numérico apertado no alerta.

## Limites honestos

Não foi reescrito todo componente legado. Calendário, rotinas, histórico geral e algumas coleções ainda usam as estruturas anteriores de rolagem. O ranking destaca dez hábitos; detalhes individuais e histórico continuam acessíveis. Não houve teste físico de teclado, leitor de tela, gestos, instalação ou launcher.

As imagens de sessões usam tempo controlado no banco de teste para exercitar o encerramento e o histórico; não demonstram uma sessão física de um minuto em aparelho. As comparações têm a mesma fixture no tour; outras capturas de regressão podem usar personagens/estados diferentes. Nenhuma delas é imagem ilustrativa fingindo UI implementada.

[Design System](DESIGN-SYSTEM-2.0.md) · [Produtividade e persistência](PRODUTIVIDADE-1.6.md) · [Validação](../VALIDACAO.md).
