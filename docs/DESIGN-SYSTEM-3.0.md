# ASCEND 1.7.2 — Design System 3.0 (Nordic Viking Interface)

O **Design System 3.0** ([`VikingDesignSystem.kt`](../app/src/main/java/br/com/ascend/aetheris/VikingDesignSystem.kt), [`NorthComponents.kt`](../app/src/main/java/br/com/ascend/aetheris/NorthComponents.kt), [`Design.kt`](../app/src/main/java/br/com/ascend/aetheris/Design.kt), [`AetherisNavigation.kt`](../app/src/main/java/br/com/ascend/aetheris/AetherisNavigation.kt)) aprofunda a materialidade nórdica e viking da `1.6.9.19` sem comprometer legibilidade, contraste, tema claro, modo minimalista ou redução de movimento.

## 1. Seis Famílias de Botões Viking (`VikingButtonStyle`)

Todas as variantes são renderizadas a partir de células individuais do atlas `viking_button_atlas.webp` (`2 × 3` RGBA) via recorte **9-slice por célula (`drawAtlasCellNineSlice`)**, garantindo que rebites, chanfros e extremidades nunca estiquem ou vazem de células vizinhas:

1. **`FerroRunico`** — Ação principal operacional em ferro escurecido batido a martelo.
2. **`BronzeForjado`** — Ação secundária em bronze nórdico envelhecido.
3. **`CerimonialViking`** — Ações solenes (resgates, início/avanço de expedições, restauração, ascensão).
4. **`Confirmacao`** — Estados concluídos ou confirmação positiva em verde musgo oxidado.
5. **`Destrutivo`** — Ações irreversíveis (excluir, abandonar, cancelar) em ferro avermelhado.
6. **`CompactoRunico`** — Controles densos (zoom do Atlas, filtros compactos, atalhos), preservando área de toque acessível (`>= 48 dp` quando interativo).

## 2. Separação Explícita: Runas Históricas vs. Selos Fictícios de Aetheris

- **Runas Históricas (`HistoricalFutharkRune` + `FutharkRuneGlyph`):** 12 glifos do **Elder Futhark** desenhados exclusivamente em geometria vetorial limpa (`Canvas`), usados apenas como ornamentação linguística e divisores sem texto ilegível gerado por IA e sem qualquer simbologia extremista ou ideológica.
- **Selos Fictícios de Aetheris (`AetherisSealKind` + `AetherisSeal`):** 12 emblemas originais do universo do jogo extraídos célula a célula de `aetheris_runes_atlas.webp` (`4 × 3` RGBA) para regiões, expedições, relíquias e domínios de Aetheris.
- **Ícones Funcionais Viking (`VikingIcon` + `VikingFunctionalIcon`):** 16 ícones funcionais talhados (`viking_functional_icons_atlas.webp`, `4 × 4` RGBA) aplicados à navegação inferior, atalhos e cartões, com degradação automática para `CarvedGlyph` vetorial no modo minimalista.

## 3. Controles Forjados e Ornamentação (`viking_controls_atlas`, `viking_knotwork_atlas`)

- **`Meter`:** sulco em basalto com preenchimento contínuo em bronze e brilho sutil de metal batido (refinado após inspeção visual para eliminar qualquer emenda vertical).
- **`ChipRow`:** tabuletas chanfradas com scrim escuro central e texto em marfim claro no modo escuro para alto contraste sobre a filigrana de bronze.
- **`FormField` e `Toggle`:** tábua rebaixada de ardósia com filete de bronze e trilho metálico forjado.
- **`VikingCheckSeal`:** selo circular de confirmação extraído com recorte quadrado centralizado (`min(cw, ch)`) para manter proporção perfeita em `HabitCard` e prancha de componentes.
- **`VikingKnotDivider` e `vikingCornerBrackets`:** divisores entrelaçados Urnes/Borre e cantoneiras metálicas usados seletivamente com espaço negativo preservado.
- **`AetherisNavigation`:** barra inferior com amuleto rúnico oval (`VikingNavAmulet`) destacando a aba ativa sem sobrecarregar o fundo.
