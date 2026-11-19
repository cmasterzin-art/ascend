# Codex Rúnico (24 Runas do Elder Futhark) — ASCEND 1.7.3.1

O **Codex Rúnico** completa a biblioteca histórica vetorial do ASCEND, expandindo as 12 runas originais da 1.7.2 para o alfabeto completo de **24 caracteres do Elder Futhark**, em uma enciclopédia offline integrada à **Biblioteca de Mímir**, às **Crônicas** e ao **Perfil do Herói**.

## 1. Rigor Histórico e Separação Editorial

- O Codex exibe um aviso editorial permanente distinguindo:
  - **Elder Futhark (24 runas, séc. II–VIII d.C.):** alfabeto rúnico germânico mais antigo, base dos 24 glifos vetoriais do Codex.
  - **Younger Futhark (16 runas, Era Viking, séc. VIII–XII d.C.):** sistema efetivamente utilizado nas inscrições escandinavas da Era Viking.
- Cada verbete separa explicitamente:
  1. **Nome reconstruído e transliteração fonética aproximada**
  2. **Significado documentado/reconstruído e contexto histórico**
  3. **Associação narrativa em Aetheris (ficção)** — rotulada como criação literária do aplicativo, nunca como fato histórico ou promessa mística.

## 2. Preservação de Compatibilidade Vetorial

Em [`VikingDesignSystem.kt`](../app/src/main/java/br/com/ascend/aetheris/VikingDesignSystem.kt):
- As **12 runas originais** preservam exatamente seus nomes estáveis e ordinais `0..11` (`Fehu`, `Ansuz`, `Raido`, `Kenaz`, `Algiz`, `Sowilo`, `Tiwaz`, `Berkano`, `Mannaz`, `Laguz`, `Othala`, `Dagaz`).
- As **12 runas adicionais** completam os três Ættir na ordem canônica (`futharkOrder = 1..24`): `Uruz`, `Thurisaz`, `Gebo`, `Wunjo`, `Hagalaz`, `Nauthiz`, `Isa`, `Jera`, `Eihwaz`, `Perthro`, `Ehwaz`, `Ingwaz`.
- O fallback de `AetherisSeal` deixou de usar aritmética por `ordinal %` e passou a utilizar o mapeamento explícito `sealFallbackRune(seal: AetherisSealKind)`.

## 3. Superfícies Materiais e Recursos da Tela

Implementado em [`RunicHeritageData.kt`](../app/src/main/java/br/com/ascend/aetheris/RunicHeritageData.kt) e [`HeraldryAndCodex.kt`](../app/src/main/java/br/com/ascend/aetheris/HeraldryAndCodex.kt):
- **6 superfícies limpas de estudo** (`runic_codex_surfaces_atlas.webp`, 3×2): Pedra Basáltica, Madeira Escura, Pergaminho Envelhecido, Ferro Escovado, Bronze Escuro e Couro Escuro.
- **Busca offline** por nome, transliteração, significado ou associação narrativa.
- **Filtros por Ætt** (*Todas (24)*, *1º Ætt*, *2º Ætt*, *3º Ætt*, *Favoritas*).
- **Favoritos locais** na sessão de estudo e atalho direto *"Usar na Heráldica Rúnica"*.
