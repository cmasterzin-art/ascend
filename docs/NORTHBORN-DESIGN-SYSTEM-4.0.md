# NORTHBORN — Design System 4.0 (ASCEND 1.7.5.9)

> **Versão:** `1.7.5.9` (`versionCode 29`) · **Nome narrativo:** *A Reforma de Aetheris*  
> **Escopo:** Reformulação visual global em Jetpack Compose nativo (sem alteração de regra de negócio, economia do RPG, Room v11 ou Backup v13).

---

## 1. Princípios Diretores

1. **Artesanato escandinavo contemporâneo, nunca "template genérico de IA":** superfícies limpas em carvão mineral (escuro) e linho arquivístico (claro), bordas de 1 dp discretas, espaço negativo generoso e eliminação de caixas dentro de caixas.
2. **Hierarquia antes de ornamento:** runas Futhark, nós geométricos e selos metálicos aparecem apenas onde cumprem função semântica ou cerimonial, nunca como ruído repetido em todo cartão.
3. **Diferenciação arquitetônica por domínio:** Jornada (ritmo aberto), Mesa de Odin (estratégia tabular), Agenda (linha do tempo legível), Forja do Tempo (quietude cerimonial), Biblioteca de Mímir (manuscrito editorial) e Arsenal/Forja (oficina metálica) têm identidades visuais distintas sob a mesma gramática.
4. **Tema claro documental autêntico:** `NorthbornLightPalette` é papel arquivístico e tinta ferro-gálica, não uma inversão mecânica do tema escuro.
5. **Acessibilidade real:** alvos táteis mínimos de `48.dp`, contraste AA/AAA nos três modos (Escuro, Claro, Minimalista), suporte a telas estreitas (`320dp`) e fonte ampliada (`130%`).

---

## 2. Paleta Semântica (`NorthbornPalette` / `LocalNorthborn`)

Definida em [`NorthbornDesignSystem.kt`](../app/src/main/java/br/com/ascend/aetheris/NorthbornDesignSystem.kt):

| Token | Tema Escuro (Mineral) | Tema Claro (Documental) | Papel |
|---|---|---|---|
| `canvas` | `#121417` | `#F2EDE3` | Fundo principal da tela |
| `surfaceBase` | `#181B1F` | `#FAF6EE` | Superfície operacional / folha |
| `surfaceElevated` | `#20242A` | `#EAE3D5` | Cabeçalhos, trilhos, controles ativos |
| `surfaceDocument` | `#1C1A16` | `#F6EFE0` | Manuscritos (Mímir, Crônicas, Objetivos) |
| `surfaceTinted` | `#161A20` | `#E8ECEF` | Faixas informativas e alertas |
| `inkPrimary` | `#EFEAE0` | `#1E2228` | Texto principal |
| `inkSecondary` | `#A7B0BA` | `#515A65` | Subtítulos e metadados |
| `inkTertiary` / `inkMuted` | `#7B838E` | `#77808A` | Legendas discretas |
| `accentBronze` | `#BC9B6A` | `#856232` | Ação primária, destaque nórdico |
| `accentPaleGold` | `#D8BE8F` | `#6B4D21` | Ação cerimonial e selos |
| `accentFjord` | `#84A4C4` | `#2E587A` | Planejamento, foco, agenda |
| `accentPatina` | `#83A88E` | `#296648` | Conclusão, confirmação, estabilidade |
| `accentEmber` | `#CC7E72` | `#9C4236` | Atraso, conflito, ação destrutiva |
| `hairline` | `#282D34` | `#D9D1C2` | Divisórias arquitetônicas silenciosas |
| `hairlineStrong` | `#3A414C` | `#B8AEA0` | Bordas de controles e painéis |

---

## 3. Tipografia, Espaçamento e Geometria

- **3 famílias funcionais** (`Design.kt`):
  - **Display / Narrativa (Serif):** títulos de tela, citações do Cronista, cabeçalhos editoriais.
  - **Interface / Leitura (Sans):** corpo de texto, rótulos de ação, formulários, navegação.
  - **Dados / Numérica (Sans tabular):** contadores de KPI, horários, cronômetro da Forja do Tempo.
- **Escala modular (`NorthbornSpace`):** `4 / 8 / 12 / 16 / 24 / 32 dp`, margem lateral de página `20.dp`, toque mínimo `48.dp`.
- **Chanfros arquitetônicos simétricos (`NorthbornShape`):** `xs (3.dp)`, `sm (4.dp)`, `md (6.dp)`, `lg (8.dp)`, `button (6.dp)`, `pill (5.dp)` — substituindo cantos assimétricos inconsistentes.

---

## 4. Materiais e Papéis de Superfície

### 4.1 Atlas de Materiais (`northborn_materials_atlas.webp`)
Grade `4×2` (`1024×512`, `47,402 B` no APK) aplicada com baixa opacidade (`0.04–0.14`) e suprimida integralmente no modo minimalista:
1. Freixo Nórdico (`NordicAsh`)
2. Carvalho Defumado (`SmokedOak`)
3. Ferro Forjado (`ForgedIron`)
4. Bronze Envelhecido (`AgedBronze`)
5. Prata Oxidada (`OxidizedSilver`)
6. Couro Escandinavo (`SaddleLeather`)
7. Velino Arquivístico (`ArchivalVellum`)
8. Ardósia Vulcânica (`VolcanicSlate`)

### 4.2 Cinco Papéis de Superfície (`NorthbornSurfaceRole`)
1. **`Operational`:** hábitos, blocos, tarefas Kanban — leitura rápida, 1 dp de borda silenciosa.
2. **`Document`:** Biblioteca de Mímir, flashcards SM-2, objetivos estratégicos, citações — grão de velino quente.
3. **`Strategic`:** Mesa de Odin, seletor de estratégias, Gantt, região do Atlas — ardósia/bronze estruturado.
4. **`Ceremonial`:** conquistas desbloqueadas, diálogos de nível, confirmações de marco — filete bronze sutil.
5. **`Informative`:** Rumos do Dia, resumos de capacidade, notas de transparência — fundo mineral discreto com faixa lateral de acento.

> **Remoção de ruído automático:** `AetherSurface` foi migrado para `NorthbornSurface` e deixou de injetar cabeçalhos automáticos repetitivos (*"SAGA PESSOAL"*, *"MARCA DE AETHERIS"*, *"COLEÇÃO DE ARTEFATOS"*) em cada cartão.

---

## 5. Sistema de Botões 4.0 (`NorthbornButton`)

Substitui adesivos rasterizados sobre botões Material por placas chanfradas arquitetônicas com resposta tátil suave (desativada em redução de movimento) e 7 papéis semânticos:

- `Primary` — ação principal de cada módulo (bronze fosco)
- `Secondary` — ação complementar (placa mineral clara/escura com hairline)
- `Tertiary` — ação discreta sem preenchimento pesado
- `Contextual` — controles compactos de navegação (`‹` / `›` / filtros)
- `Ceremonial` — ações solenes (Acender a primeira chama, Gerar plano, Aplicar plano completo, Revelar resposta)
- `Confirm` — confirmação positiva (verde pátina nórdica)
- `Destructive` — remoção ou descarte (brasa escurecida)

---

## 6. Divisores, Navegação e Iconografia

- **`NorthbornDivider`:** `Hairline`, `DiamondKnot` e `DoubleKnot` desenhados em vetor limpo com degradê nas extremidades (suprimidos para linha simples no modo minimalista).
- **`NorthbornTopBar`:** cabeçalho secundário limpo com ícone `Back` vetorial e botão de ação primária alinhado.
- **`AetherisNavigation`:** barra inferior de 5 destinos (*Jornada*, *Hábitos*, *Missões*, *Mundo*, *Herói*) com indicador superior fino em bronze e **`NorthbornIcon`** (15 ícones escandinavos de traço uniforme).
- **Controles sem caixa pesada:** `ChipRow`, `FormField`, `Meter` e `Toggle` reconstruídos em `Design.kt` com traços limpos e contraste consistente em claro/escuro.
