# Recursos visuais da 1.3 e 1.3.1

Pinturas originais geradas para ASCEND nesta atualização: fiorde/vila, atlas, retrato adulto em três tons de pele e presença mítica dos chefes. As solicitações de arte especificaram mundo e personagens novos, sem copiar franquias. Não foram usados assets oficiais de jogos. As imagens foram convertidas para WebP para distribuição, sem recorte ou alteração do conteúdo por scripts.

Na 1.3.1, quatro novas pinturas em atlas acrescentam itens em nichos (dois atlas de 3 × 3) e cenas de reconstrução (3 × 3 e 3 × 2). Cada região do atlas é amostrada durante a renderização; arquivos de arte não são recortados por scripts. Os 17 itens têm correspondência própria; uma célula do primeiro atlas ficou sem uso. Equipamentos não são mais sobrepostos ao corpo do personagem. Selos, entalhes, pedras de região e materiais continuam desenhados em Kotlin/Compose. Não há animação 3D ou download de arte.

A fonte **Cinzel** veio do repositório Google Fonts (`ofl/cinzel/Cinzel[wght].ttf`), sob SIL Open Font License 1.1. [Licença completa](Cinzel-OFL.txt), também incluída no APK em `assets/licenses/Cinzel-OFL.txt`. Corpo e textos funcionais usam a fonte de sistema.

Todos os arquivos são distribuídos com o APK; o app não consulta servidores para carregar arte, fontes ou dados.

| Arquivo WebP | Bytes | SHA-256 |
|---|---:|---|
| `nordic_atlas.webp` | 568,580 | `3e61a0cb7cb78b0f89dc70c5bd36dca417e3a6a6473e40cb14e136df7e37d7e9` |
| `nordic_fjord.webp` | 313,992 | `37be4cac4105619d0780649b584cc0134e16ffe1912e9b94cd3f004fe900152d` |
| `nordic_wraith.webp` | 290,568 | `57f7b4ba02fa46bff0a8e2d901d30495d202a166f2e7bf1cb409c5db5527f7de` |
| `wanderer_dark.webp` | 241,050 | `5992d98568a40ce954af658140942a0f3f28f27a8ee2e4df90c1ec67539dd4a8` |
| `wanderer_light.webp` | 258,142 | `4d92b84d048f529ac4a007af81a9b6e36cf3bf9143269d5f0725bb6472932c58` |
| `wanderer_medium.webp` | 239,406 | `d85efb7bc8e12b526255ec06778f27293dcdc115e4375d3cce7bc95372b1a74d` |
| `relic_atlas.webp` | 261,000 | `2fc93f7a2cf9cf1b312e05818d8ab4de89332828936df4429e1cc3db46026f43` |
| `relic_variants.webp` | 260,298 | `3d065608557a3fb63e1daf28a40b9c13809920b8e0795bdf584b5200f7e07ab7` |
| `village_crafts.webp` | 512,498 | `d7aebf37b5635ba03a7adef1964bf21b29b254b8a4767bdb0f20e77bee867f6a` |
| `village_civic.webp` | 440,130 | `0b9f91a9df32f6c552767c9b0568540a8c4144dcd1e2f177669c06939f72687b` |

## 1.4.5 — áudio

As pinturas foram preservadas. Cinco efeitos breves originais foram sintetizados localmente, sem amostras externas. [Origem, reprodução e limites](SOUNDS.md).

## 1.5 — Floresta da Persistência

`forest_atlas.webp` é uma pintura original nova em grade 3 × 3: três estados da floresta, três estados do santuário, Marco dos Retornos, Guardião da Névoa e Broche da Vigília. A direção pediu universo e criatura originais, coerentes com a pintura local do fiorde; nenhum recurso oficial de outra franquia foi utilizado. A imagem original foi convertida para WebP sem edição do conteúdo por scripts. Células são amostradas na renderização por ArtTile, preservando o arquivo completo. Não são simples filtros de cor ou vetores apresentados como pintura.

Bytes: 449,560. SHA-256: `7882d1a03a3687695364d32b941e1060e79bc17dfcba83ccc2a221dbfbe95343`.

## Recursos originais 1.6.1

`ForgeArt.kt`: biblioteca vetorial procedural original de ferro riscado, couro costurado, papel granulado, molduras, entrelaçados pareados, rebites, chapas de canto, nichos e medalhão de nível. Sem recursos externos ou cópia de jogos comerciais; reutiliza as pinturas já registradas acima. Renderização com cache e semente fixa; sem textura animada. Aplicação e capturas em [VISUAL-1.6.1.md](../VISUAL-1.6.1.md).

## Materiais originais 1.6.2

Quatro recursos gerados nesta execução com a ferramenta de geração de imagens: `forge_iron.webp` (ferro escurecido), `forge_leather.webp` (couro), `forge_vellum.webp` (velino) e `forge_brooch.webp` (medalhão com relevo entrelaçado original). Estudos próprios sem texto, marcas ou recursos extraídos de obras comerciais. Otimizados em WebP, com materiais em 1024 px e medalhão em 256 px com transparência. Os originais foram preservados no ambiente de geração; os arquivos distribuídos são locais no APK. Aplicação real, comparação e limitações: [VISUAL-1.6.2.md](../VISUAL-1.6.2.md).

## 1.6.5

Conceito original de três telas em `concepts/north-165.webp`; apenas direção artística. `north_frame.webp` e `north_stone.webp` foram gerados nesta execução e otimizados em WebP para uso local real. Recursos anteriores preservados. Sem cópia de assets comerciais.

## 1.6.8

`north_divider.webp`: divisor original gerado com transparência, bronze oxidado e relevo orgânico, otimizado em WebP. Uso seletivo em composição Compose; sem texto incorporado. Novo símbolo vetorial de montanhas para Jornada.

## 1.6.9 — O Último Entalhe

Consolidação artística definitiva (*Nordic Cinematic UI*). Todas as pinturas anteriores (`nordic_*`, `wanderer_*`, `relic_*`, `village_*`, `forest_atlas`, `forge_*`, `north_*`) foram integralmente preservadas. Novos recursos originais gerados via `image-generator` sem texto dinâmico e otimizados em WebP, além da família completa de 25 ícones vetoriais entalhados em `CarvedArt.kt` (`CarvedGlyph`):

- `concepts/entalhe-169.webp`: conceito visual de direção de arte para Jornada, Herói e Missões.
- Prancha de aplicação e comparativos em [VISUAL-1.6.9.md](../VISUAL-1.6.9.md).

| Arquivo WebP (1.6.9) | Bytes | SHA-256 |
|---|---:|---|
| `carved_emblems.webp` | 20,496 | `6718dbfad9411ad33c43ed10b1c8c9415d4479e488f2750f825543cfa00e476d` |
| `carved_seals.webp` | 15,060 | `2d5eea8417cda4f96b6dbb304e01aec657b69c2193db012dcd6d06e01cf999ae` |
| `plate_iron.webp` | 194 | `9f2f46d38f033dcc4f74ee0be8e0e4158700dcf1cff9738b41d645fcc4a33f79` |
| `plate_bronze.webp` | 212 | `0a6fc49a945013f910477fc7eaec298e7b3b48b86f9b794c557e940c6a0a02f3` |
| `artifact_frame.webp` | 3,400 | `f4bdcba57d9988e9e66cba116bb16404f45ab11d683bb8a1f92e029fd7a697b` |
| `widget_slate.webp` | 1,320 | `93a0c75e431e21310ab5a71db26e9f87a821a2173f1fc5457102015cfeb15080` |
| `concepts/entalhe-169.webp` | 12,750 | `588eef4706674952d295dcd03f8ffae769e2e764b88cb4792ee27ef89112bbdb` |

## 1.6.9.19 — A Biblioteca da Forja (Nordic Pictorial Library)

Biblioteca pictórica original gerada via ferramenta de geração de imagens (`image-generator` / Nano Banana) antes da integração em código, sem texto embutido, sem cópia de franquias comerciais e com preservação integral de todos os recursos anteriores (`nordic_*`, `wanderer_*`, `relic_*`, `village_*`, `forest_atlas`, `forge_*`, `north_*`, `carved_*`, `plate_*`, `artifact_frame`, `widget_slate`).

Os **20 novos arquivos WebP** (1 prancha conceitual + 19 recursos de produção em `app/src/main/res/drawable-nodpi/` somando `855.218` bytes para os 19 recursos do APK) organizam-se em quatro famílias (**Materiais**, **Componentes de Interface**, **Ornamentação** e **Widgets**) e são aplicados de forma controlada com recorte 9-slice (`drawNineSlice` / `pictorialFrame`) e amostragem de atlas sem deformação de cantos ou entalhes. Documentação completa, prancha renderizada e comparativos `1.6.9.15 × 1.6.9.19` em [VISUAL-1.6.9.19.md](../VISUAL-1.6.9.19.md).

| Arquivo WebP (1.6.9.19) | Família / Referência | Dimensões e Formato | Bytes | SHA-256 | Finalidade Artística | Componentes Onde é Aplicado |
|---|---|---|---:|---|---|---|
| `concepts/biblioteca-forja-16919.webp` | Prancha conceitual ([captura](../screenshots/v1.6.9.19/prancha-conceitual-16919.png)) | `1376 × 768` · WebP RGB | 152,656 | `e52b2acda671b4d062b91c617087db0450b2d66906e42c818ae214d357164420` | Direção de arte para metais escandinavos, pedra vulcânica, placas forjadas, ornamentos Urnes/Borre/Ringerike e tábuas de widget | Referência conceitual (fora do APK) |
| `mat_iron_nordic.webp` | Família I — Materiais ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGB | 71,520 | `73f0fdbec78b0e596c5c3576893ef153a23ad171b819b116e205147c7555c0aa` | Ferro nórdico enegrecido batido a martelo, micro-marcas de forja e reflexo frio | `Modifier.forgeMaterial(ForgeMaterial.Iron)`, `AetherSurface(SurfaceKind.Operational)` |
| `mat_bronze_ancient.webp` | Família I — Materiais ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGB | 54,208 | `c7823d431669906cc878b785364cceb4695e36329d35393996f59c85b57e2a5c` | Bronze escandinavo antigo escovado com pátina esverdeada discreta e calor metálico | `Modifier.forgeMaterial(ForgeMaterial.Bronze)`, `AetherSurface(SurfaceKind.Heroic)` |
| `mat_silver_oxidized.webp` | Família I — Materiais ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGB | 92,924 | `77079aa9bb0fcbc86fe4752c83bac1fa028bda5aa23a7f9c0c46191794c46c7c` | Prata nórdica oxidada com pátina de sulfeto nos sulcos e brilho lunar austero | `Modifier.forgeMaterial(ForgeMaterial.Silver)`, `AetherSurface(SurfaceKind.Ceremonial)` |
| `mat_slate_volcanic.webp` | Família I — Materiais ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGB | 85,732 | `5bf2599ec2cb0297e1f493fa13e28b8659739c05627255d2dc8daaec14f4fbeb` | Ardósia vulcânica islandesa clivada em camadas minerais foscas | `Modifier.forgeMaterial(ForgeMaterial.Slate)`, `AetherSurface(SurfaceKind.Artifact)` |
| `mat_leather_carved.webp` | Família I — Materiais ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGB | 83,470 | `46b60d0ef75d420b33e3f36450ad35d40794b86516574465c6d5fb51a3ce0d0a` | Couro cerimonial escuro curtido com relevo cego Urnes discreto | `Modifier.forgeMaterial(ForgeMaterial.Leather)`, `SagaCover`, superfícies de couro |
| `mat_vellum_aged.webp` | Família I — Materiais ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGB | 71,240 | `869cb568ae8c23ec2f2b0d66c52f1935cc83f0f8802adf49e5b729f91f3de17a` | Velino nórdico envelhecido com fibras orgânicas sutis e centro limpo para leitura | `Modifier.forgeMaterial(ForgeMaterial.Vellum)`, `AetherSurface(SurfaceKind.Parchment)` |
| `ui_plate_primary.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `640 × 224` · WebP RGBA | 16,406 | `1f3d4053b6fc93a16d1c949952cc85ef4fb7107c9b3241dad48ba404791ebbed` | Placa chanfrada de bronze forjado e ferro escuro com entalhes de canto e rebites | `SagaButton` primário (`Modifier.plateMaterial`) |
| `ui_plate_secondary.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `640 × 224` · WebP RGBA | 39,052 | `786af9107c32a19312146fb929e6e14544ab17005a2f916d987b5cd1582bb58e` | Placa chanfrada de ferro nórdico enegrecido com bisel de cinzel e rebites discretos | `SagaButton` secundário/concluído (`Modifier.plateMaterial`) |
| `ui_plate_ceremonial.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `640 × 192` · WebP RGBA | 29,510 | `b23e2eff8c8848120bb710b93be66d90723eab4cd7b43f01ae41179d11cbf0d1` | Placa cerimonial de recompensa em ouro nórdico oxidado e prata escura com filigrana Urnes nos cantos | `SagaButton(ceremonial = true)` em resgates de contratos, Sagas, Expedições, Vila, Foco e nível |
| `ui_field_inscription.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `480 × 160` · WebP RGBA | 8,700 | `e94fb5d2b4fcac9d4bf0037c1c88f5c7dfaf3aaff56d9d291605dc3b1d16e821` | Tábua de inscrição rebaixada em ardósia vulcânica com cantoneiras de ferro e filete inferior de bronze | `FormField` (`Design.kt`) |
| `ui_tablet_selector.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 224` · WebP RGBA | 30,794 | `225af9eb9cd837dd885173b4e94f43b933885fb06c7fd0e3e38bd93dcf938d91` | Tabuleta chanfrada para seletores e filtros com cantos de bronze e face de ardósia | `ChipRow` (`Design.kt`), `FilterChip` (`Forms.kt`) |
| `ui_control_surface.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 200` · WebP RGBA | 40,712 | `d2ace1f20dd32aa4022ebeef7f14748b8e7d603fc8f89ac26f2da9b872defb2a` | Trilho escavado em ferro e bronze para interruptores e controles deslizantes | `Toggle` (`Design.kt`) |
| `ui_frame_dialog.webp` | Família II — Interface ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `640 × 480` · WebP RGBA | 17,666 | `10e8f26ec85e5535067fbe1731137159dfd36f6272a7373e94f8cac1c2cbca1d` | Moldura estrutural de diálogo em pedra vulcânica e ferro forjado com braçadeiras de bronze nos cantos | `AetherDialog` (`DesignSystem2.kt`) |
| `orn_divider_forged.webp` | Família III — Ornamentação ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `540 × 120` · WebP RGBA (atlas `1 × 2`) | 16,468 | `d52729cb4f036d290e114b307dedad0f8c5e2418835c6f98dbe2fd8e03de81f0` | Divisores horizontais forjados em bronze oxidado e ferro (faixa superior cerimonial Urnes; inferior operacional) | `NorthDivider` (`ForgeArt.kt`), `SagaDivider` (`NordicArt.kt`) |
| `orn_frames_rarity.webp` | Família III — Ornamentação ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `512 × 512` · WebP RGBA (atlas `2 × 2`) | 34,928 | `62f37172bb6a02cdfa840e3083345a1ce15fa33c999935651a3a54903ce3e136` | Quatro molduras quadradas vazadas de nicho de artefato por raridade (ferro, bronze, prata rúnica, ouro ancestral) | `ArtifactFrame` (`CarvedArt.kt`), `RelicArt` (`RpgDesign.kt`) |
| `orn_medallions_seals.webp` | Família III — Ornamentação ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `576 × 384` · WebP RGBA (atlas `3 × 2`) | 56,218 | `b5d881c6e5a6fe9bdc7d5ad6db633dbe8dd3dfe6e866b021b50453fa66232a0f` | Seis medalhões e selos cerimoniais entalhados (aros de bronze/ferro, astrolábio solar, selos de cera e prata) | `ForgeMedallion`, `DayDial` (`ForgeArt.kt`), `RuneSeal` (`NordicArt.kt`) |
| `orn_track_progress.webp` | Família III — Ornamentação ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `520 × 120` · WebP RGBA (atlas `1 × 2`) | 15,678 | `07bf301849f9777f00aaad1165f81081645f95ffc8ece933b862dc9783e11e37` | Sulco de progresso esculpido em pedra/ferro (metade superior) e barra de bronze fundido (metade inferior) | `Meter` (`Design.kt`) |
| `wid_plate_compact.webp` | Família IV — Widgets ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `628 × 140` · WebP RGBA | 15,950 | `ff2d1c3d402e9a553f10618b786988c8fd6fbe21ebfe66f58b12b33afcaad030` | Placa pictórica horizontal de ardósia e ferro com braçadeiras de bronze para widget compacto | `widget_surface.xml`, `HabitWidget.kt` (modo compacto) |
| `wid_plate_expanded.webp` | Família IV — Widgets ([prancha](../screenshots/v1.6.9.19/prancha-biblioteca-16919.png)) | `628 × 320` · WebP RGBA | 54,036 | `d5fbe0a4b0e25b7d99138820dfd8182ac964819767478a87ef0a5650db0121e7` | Tábua pictórica expandida de ardósia vulcânica com arco superior de bronze e cantoneiras de ferro | `widget_surface_expanded.xml`, `HabitWidget.kt` (modo expandido) |

