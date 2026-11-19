# ASCEND 1.7.2 — Expansão da Biblioteca da Forja: Renascimento Rúnico

A expansão **Renascimento Rúnico** acrescenta **10 novos recursos pictóricos originais** gerados via ferramenta `generate_image` (orçamento estrito de `10/10` imagens): **1 prancha conceitual** mantida fora do APK e **9 atlas WebP de produção** em `app/src/main/res/drawable-nodpi/`, somando **845.042 bytes (825,2 KB / 0,81 MB)** — bem abaixo do teto de `3 MB` — e preservando integralmente todos os recursos anteriores (`1.3` a `1.7.1`).

> **Conceito vs. Aplicativo Real:** a prancha `docs/art/concepts/nordic-viking-ui-172.webp` é exclusivamente referência artística de direção visual (fora do APK) e não representa captura de tela do aplicativo. As capturas reais renderizadas em Compose encontram-se em [`docs/screenshots/1.7.2/`](screenshots/1.7.2/) e em [`VISUAL-1.7.2.md`](VISUAL-1.7.2.md).

## Inventário dos 10 Recursos da 1.7.2

| Arquivo WebP | Tipo / Grade | Dimensões e Formato | Bytes | SHA-256 | Onde é aplicado |
|---|---|---|---:|---|---|
| `docs/art/concepts/nordic-viking-ui-172.webp` | Prancha conceitual (fora do APK) | `1376 × 768` · WebP RGB | 125.910 | `2e17f8a69d77acc0fde70f905b374f5c5d2ae96877da4ed6e417c6525a216b4b` | Direção de arte do Design System 3.0 (referência, fora do APK) |
| `realm_library_atlas.webp` | Atlas regional `3 × 3` | `1080 × 1080` · WebP RGB | 107.650 | `683cc24b7cf8b5f74d13b912afab5c72376af7d509e1bfe25fc1093b72236261` | Biblioteca das Eras (3 estágios, 3 locais, retrato de Eydis, placas narrativas) |
| `realm_mountains_atlas.webp` | Atlas regional `3 × 3` | `1080 × 1080` · WebP RGB | 120.198 | `7e69e3d714cf6025b1658a1f8ae901e3d204f5254c13d955b55d27d0f18942ae` | Montanhas da Disciplina (3 estágios, 3 locais, retrato de Haldor, placas narrativas) |
| `realm_relics_atlas.webp` | Atlas de relíquias `2 × 2` | `560 × 560` · WebP RGBA | 93.976 | `c214ca3335784bfc29547e002a942a8f9524b3496e6a435340c2678dc06f9a95` | 4 relíquias regionais (`RelicPainting` / `RelicArt` no Inventário e exploração) |
| `realm_ornaments_atlas.webp` | Atlas de ornamentos `3 × 2` | `720 × 480` · WebP RGBA | 86.748 | `9444544e294230460bff3f7470aaea05ceae97312df704490ad51fa2e208f37f` | Marcadores de estado e bússola do Atlas 2.0 (`RealmOrnament`) |
| `viking_button_atlas.webp` | Atlas de botões `2 × 3` (9-slice por célula) | `960 × 396` · WebP RGBA | 38.452 | `2a63bd8520fcd5e85acf6c4634b89ec5513ba29a6044219c24844c220c16925e` | 6 famílias de botões em `SagaButton` (`VikingButtonStyle`) |
| `aetheris_runes_atlas.webp` | Atlas de selos fictícios `4 × 3` | `864 × 576` · WebP RGBA | 122.482 | `8b0bf7803670b4f1d31dbf8b70936465d56864a17744b3edac481b9e96a7d505` | 12 selos originais de Aetheris (`AetherisSeal`) |
| `viking_functional_icons_atlas.webp` | Atlas de ícones `4 × 4` | `640 × 640` · WebP RGBA | 107.338 | `15027ca78ae77f2716179306046007af2029eadbdc26ba7eec22fae64c916587` | 16 ícones funcionais (`VikingFunctionalIcon` em navegação e cartões) |
| `viking_knotwork_atlas.webp` | Atlas ornamental `2 × 4` | `960 × 640` · WebP RGBA | 101.108 | `3b8dba23cbaa5ff8d586e8eba114dae629c35a83005cfd22e6092f1156ed00ba` | Divisores entrelaçados (`VikingKnotDivider`), cantoneiras e moldura vazada |
| `viking_controls_atlas.webp` | Atlas de controles `2 × 4` | `960 × 640` · WebP RGBA | 67.090 | `2ea4921583c849aac3adf935c1cd149607e02998bf2d663707ec8e2fe5037f9f` | `Meter`, `ChipRow`, `Toggle`, `FormField`, `VikingCheckSeal` e `VikingNavAmulet` |

## Regras Técnicas de Renderização

- **Zero 9-slice sobre atlas inteiro:** todo atlas com bordas escaláveis passa por `drawAtlasCellNineSlice`, que isola exatamente o retângulo de origem de uma célula antes de aplicar os 9 quadrantes.
- **Proporção preservada:** `VikingCheckSeal` usa recorte quadrado centralizado da célula `6` de `viking_controls_atlas.webp`, e os atlas regionais `3 × 3` respeitam a razão de aspecto da célula sem distorção.
- **Compatibilidade total:** no modo minimalista, todos os novos atlas cedem lugar a vetores discretos (`FutharkRuneGlyph` / `CarvedGlyph`); no tema claro, as alfas são suavizadas para manter contraste sobre pergaminho e ardósia clara.
