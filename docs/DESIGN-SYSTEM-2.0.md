# Design System 2.0 — ASCEND 1.6

A interface separa o trabalho cotidiano da apresentação do RPG. A Jornada oferece atividades e planejamento antes das narrativas; o retrato amplo permanece no Herói. Pinturas, fontes e ícones originais continuam locais. Não foram geradas novas ilustrações.

## Tokens e tipografia

`DesignSystem2.kt` concentra `AetherTokens`: página 20 dp, intervalo 16 dp, interior 18 dp, intervalo pequeno 8 dp, toque mínimo 48 dp, forma assimétrica 4/16 dp e movimentos de 140/240 ms. MaterialTheme fornece os papéis semânticos de cor e tipografia; `Gold`, `Blue`, `Ink` e `Muted` continuam respeitando claro/escuro.

- Escuro: fundo `#090E14`, superfície `#151E27`, ardósia `#24303A`, texto `#E7DECB`, secundário `#AEB5BF`, bronze `#C4A36A`, gelo `#9EB4D3`.
- Claro: fundo `#E5E1D8`, superfície `#F1EDE3`, texto `#20252D`, secundário `#515B66`. Contrapartidas de bronze/gelo têm mais contraste.
- Cinzel local para títulos e grandes números; fonte funcional para nomes operacionais, formulários e narrativas longas. Leitura de 15 sp / 25 sp nas áreas narrativas refinadas.
- Os tokens não substituem toda dimensão legada. Componentes secundários ainda conservam medidas próprias; não houve reescrita integral da UI.

## Cinco superfícies em uso

| Tipo | Componente e aplicação | Linguagem |
|---|---|---|
| Operacional | `AetherSurface(Operational)`, painéis, hábitos, Planejador, Análises e Foco | Ardósia, contorno discreto, números escaneáveis e ações claras |
| Heroica | `CompactHero`, ficha do Herói, Sagas em curso | Reflexo frio, retrato, composição assimétrica e dados de evolução |
| Cerimonial | Capa de contratos, missões épicas, Sagas e marcos | Identificação em bronze, selo, hierarquia entre progresso e recompensa |
| Pergaminho | `Manuscript`, Crônica, Memórias e narrativa do Cronista | Gradiente de papel envelhecido e tinta sépia; corpo funcional confortável |
| Artefato | Inventário, nichos, coleção e mercado | Fundo mineral/violeta discreto, pinturas de itens, raridade e estado textual |

```kotlin
AetherSurface(SurfaceKind.Operational) {
    SectionHeader("Agenda do dia", "Ajustes válidos só nesta data")
    MetricPair("2/3", "concluídas", "66%", "cumprimento")
}
```

## Composição e componentes

- `CompactHero`: retrato de 64 dp com nome, classe, nível, título e XP ao lado. A primeira atividade não depende de atravessar uma pintura grande.
- `MetricPair` e `SectionHeader`: indicadores em duas colunas, rótulos funcionais e agrupamento por contexto.
- `HabitCard`: categoria/estado, nome clicável, progresso, recompensa e alvo de registro de 48 dp. Tempo tem acesso próprio ao foco. Conclusão usa texto e símbolo, além da cor.
- `MissionCard2`: contratos operacionais e épicos distintos; estado, objetivo, narrativa e recompensa separados. Mantém resgate e edição existentes.
- `SagaButton`: bronze mais definido, cantos moderados, estados primário/secundário/destrutivo/concluído e pressão breve por deslocamento em layout.
- `AetherisTabs`: seleção explícita e opções completas em grade; navegação principal continua com cinco áreas e ícones Canvas originais.
- Gráficos: eixo numérico, datas de início/fim, escala explícita, descrição acessível e tabela de valores por data. Não se usa ornamentação como dado.
- Formulários reutilizam campos, validação e proteção de rascunhos da 1.5.2. Configurar foco agora usa formulário completo, com rolagem/teclado, em vez de um campo apertado no alerta.

## Movimento e acesso

Navegação usa 140 ms; barras, retrato de abas e cenas da floresta usam 240 ms. Pressão dos botões mantém resposta breve. Redução de movimento continua com duração zero. Não foram adicionadas partículas, animações ambientais permanentes ou música contínua.

Hierarquia, contraste, alvos confortáveis e estados textuais têm prioridade sobre ornamentos. A UI foi exercitada em 411/320 dp e fonte 130%, claro/escuro, minimalista e redução de movimento. Testes de leitor de tela, teclado físico e fabricante continuam pendentes. Widgets usam o tema do sistema, como antes.

[Comparações reais](VISUAL-1.6.md) · [Regras funcionais](PRODUTIVIDADE-1.6.md).

## Evolução 2.1 — Nordic Cinematic UI

As cinco famílias recebem materialidade e composição em `ForgeArt.kt`, compartilhadas por `AetherSurface` e `SagaButton`. Retrato/medalhão e navegação usam a mesma biblioteca. Tokens, tipografia funcional, Cinzel, temas e comportamentos são preservados. [Implementação e comparações](VISUAL-1.6.1.md).
