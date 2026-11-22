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

## 1.7.1 — O Acervo de Mímir (Biblioteca de Mímir & Ascensão dos Arquétipos)

Três novos recursos pictóricos originais gerados via ferramenta `generate_image` (orçamento estrito de 3 imagens) e otimizados em WebP em `app/src/main/res/drawable-nodpi/`, somando **359.502 bytes (351,1 KB)** e preservando integralmente todos os recursos anteriores. Relatório visual e capturas reais em [VISUAL-1.7.1.md](../VISUAL-1.7.1.md).

| Arquivo WebP (1.7.1) | Origem / Geração | Dimensões e Formato | Bytes | SHA-256 | Finalidade Artística | Componentes Onde é Aplicado |
|---|---|---|---:|---|---|---|
| `mimir_library.webp` | `generate_image` (`mimir_library_1795050287377.jpg`) | `1280 × 704` · WebP RGB | 116,684 | `86bbbb5f0f18bb2b85cecdb436fd566f38eaed9a22bba72996c264f2d985b8b5` | Ilustração atmosférica da Biblioteca de Mímir em pedra escura, estantes talhadas e luz fria de velas | `MimirLibraryBanner` (`MimirScreens.kt`) |
| `archetype_seals_atlas.webp` | `generate_image` (`archetype_seals_atlas_1795050288588.jpg`) | `1280 × 512` · WebP RGBA (`5 × 2`, células `256 × 256`) | 198,934 | `11e04851579cd756abd565f7846f91b3e5d93dfe5b97ef3616b7c58c1df69a7a` | 10 selos de bronze e ferro para as 10 especializações (Machado, Escudo de Ferro, Tomo, Círculo Rúnico, Escudo Ornado, Árvore, Bússola, Arco, Bigorna, Nó de Urnes) | `ArchetypeSealArt` em `HeroStage`, `ArchetypeAscensionPanel` (`CharacterScreen.kt`) e `SkillTree` (`EquipmentScreen.kt`) |
| `mimir_codex_atlas.webp` | `generate_image` (`mimir_codex_atlas_1795050289877.jpg`) | `960 × 640` · WebP RGB (`3 × 2`, células `320 × 320`) | 43,884 | `4692aed01ff119a71c4b77ce72bcca62e43918e0fdab07f68a212384fdcb3a2a` | 6 capas e ícones pictóricos de tomos, pergaminhos, manuscritos, runa de ardósia e placa de bronze | `MimirCodexArt` em `MimirScreens.kt` (cadernos, editor, Runas da Memória) e `WorldScreen.kt` |

## 1.7.2 — Renascimento Rúnico (World Expansion & Design System 3.0)

Dez novos recursos pictóricos originais gerados via `generate_image` (orçamento estrito de `10/10` imagens): **1 prancha conceitual** mantida fora do APK (`docs/art/concepts/nordic-viking-ui-172.webp`) e **9 atlas WebP de produção** em `app/src/main/res/drawable-nodpi/`, somando **845.042 bytes (825,2 KB / 0,81 MB)** dentro do APK (limite máximo de `3 MB`) e preservando integralmente todos os recursos das versões `1.3` a `1.7.1`. Relatório visual e capturas reais em [VISUAL-1.7.2.md](../VISUAL-1.7.2.md) e [RENASCIMENTO-RUNICO.md](../RENASCIMENTO-RUNICO.md).

| Arquivo WebP (1.7.2) | Tipo / Grade | Dimensões e Formato | Bytes | SHA-256 | Finalidade Artística | Componentes Onde é Aplicado |
|---|---|---|---:|---|---|---|
| `concepts/nordic-viking-ui-172.webp` | Prancha conceitual (fora do APK) | `1376 × 768` · WebP RGB | 125,910 | `2e17f8a69d77acc0fde70f905b374f5c5d2ae96877da4ed6e417c6525a216b4b` | Prancha de direção visual para botões, runas, controles e ornamentação viking | Referência conceitual (fora do APK) |
| `realm_library_atlas.webp` | Atlas regional `3 × 3` | `1080 × 1080` · WebP RGB | 107,650 | `683cc24b7cf8b5f74d13b912afab5c72376af7d509e1bfe25fc1093b72236261` | 3 estados de restauração da Biblioteca das Eras, 3 locais narrativos, retrato de Eydis e placas | `ExplorationScreens.kt` (`RealmAtlasPlate`) |
| `realm_mountains_atlas.webp` | Atlas regional `3 × 3` | `1080 × 1080` · WebP RGB | 120,198 | `7e69e3d714cf6025b1658a1f8ae901e3d204f5254c13d955b55d27d0f18942ae` | 3 estados de restauração das Montanhas da Disciplina, 3 locais narrativos, retrato de Haldor e placas | `ExplorationScreens.kt` (`RealmAtlasPlate`) |
| `realm_relics_atlas.webp` | Atlas de relíquias `2 × 2` | `560 × 560` · WebP RGBA | 93,976 | `c214ca3335784bfc29547e002a942a8f9524b3496e6a435340c2678dc06f9a95` | 4 relíquias regionais colecionáveis (Anel do Escriba, Medalhão da Memória Eterna, Broche da Vigília Boreal, Bússola do Firmamento) | `RelicPainting` / `RelicArt` (`VikingDesignSystem.kt`, `RpgDesign.kt`, `EquipmentScreen.kt`) |
| `realm_ornaments_atlas.webp` | Atlas de ornamentos `3 × 2` | `720 × 480` · WebP RGBA | 86,748 | `9444544e294230460bff3f7470aaea05ceae97312df704490ad51fa2e208f37f` | Marcadores de estado e bússola cartográfica do Atlas de Aetheris 2.0 | `RealmOrnament` (`RpgDesign.kt`, `WorldScreen.kt`) |
| `viking_button_atlas.webp` | Atlas de botões `2 × 3` (9-slice por célula) | `960 × 396` · WebP RGBA | 38,452 | `2a63bd8520fcd5e85acf6c4634b89ec5513ba29a6044219c24844c220c16925e` | 6 famílias de placas de botão viking (`FerroRunico`, `BronzeForjado`, `CerimonialViking`, `Confirmacao`, `Destrutivo`, `CompactoRunico`) | `SagaButton` (`NorthComponents.kt`, `VikingDesignSystem.kt`) |
| `aetheris_runes_atlas.webp` | Atlas de selos fictícios `4 × 3` | `864 × 576` · WebP RGBA | 122,482 | `8b0bf7803670b4f1d31dbf8b70936465d56864a17744b3edac481b9e96a7d505` | 12 selos originais do mundo de Aetheris (distintos das runas históricas vetoriais) | `AetherisSeal` (`VikingDesignSystem.kt`, `ExplorationScreens.kt`, `WorldScreen.kt`) |
| `viking_functional_icons_atlas.webp` | Atlas de ícones `4 × 4` | `640 × 640` · WebP RGBA | 107,338 | `15027ca78ae77f2716179306046007af2029eadbdc26ba7eec22fae64c916587` | 16 ícones funcionais talhados em metal oxidado para navegação e atalhos | `VikingFunctionalIcon` (`AetherisNavigation.kt`, `JourneyHome.kt`, `VikingDesignSystem.kt`) |
| `viking_knotwork_atlas.webp` | Atlas ornamental `2 × 4` | `960 × 640` · WebP RGBA | 101,108 | `3b8dba23cbaa5ff8d586e8eba114dae629c35a83005cfd22e6092f1156ed00ba` | Divisores Urnes/Borre, cantoneiras metálicas e moldura vazada 9-slice | `VikingKnotDivider`, `vikingCornerBrackets`, `vikingHollowFrame` |
| `viking_controls_atlas.webp` | Atlas de controles `2 × 4` | `960 × 640` · WebP RGBA | 67,090 | `2ea4921583c849aac3adf935c1cd149607e02998bf2d663707ec8e2fe5037f9f` | Sulco/barra de progresso, tabuletas de chip, trilho de switch, campo rebaixado, selo de check e amuleto de navegação | `Meter`, `ChipRow`, `FormField`, `Toggle`, `VikingCheckSeal`, `VikingNavAmulet` |

## 1.7.3 — A Forja das Lendas (Living Hero, Runic Crafting & Nordic Viking Arsenal)

Oito novos recursos pictóricos originais gerados via `generate_image` (orçamento estrito de `8/10` chamadas, sem necessidade de usar as `2` de reserva): **1 prancha conceitual** mantida fora do APK (`docs/art/concepts/hero-living-173.webp`) e **7 atlas WebP de produção** em `app/src/main/res/drawable-nodpi/`, somando **621.042 bytes (606,5 KB / 0,59 MB)** dentro do APK (limite máximo de `3 MB`) e preservando integralmente os três retratos base (`wanderer_light/medium/dark.webp`) e todos os recursos das versões `1.3` a `1.7.2`. Documentação visual em [VISUAL-1.7.3.md](../VISUAL-1.7.3.md) e [RETRATO-VIVO-1.7.3.md](../RETRATO-VIVO-1.7.3.md).

| Arquivo WebP (1.7.3) | Tipo / Grade | Dimensões e Formato | Bytes | SHA-256 | Finalidade Artística | Componentes Onde é Aplicado |
|---|---|---|---:|---|---|---|
| `concepts/hero-living-173.webp` | Prancha conceitual (fora do APK) | `1376 × 768` · WebP RGB | 134,652 | `f2030e512ae68a6c18971d7311ce1acb88589089411c00cfe2789179b52c8ec1` | Prancha de direção visual para Retrato Vivo, camadas sobre retratos originais, Forja de Darian e Arsenal Viking | Referência conceitual (fora do APK) |
| `living_armor_atlas.webp` | Atlas de trajes `2 × 2` | `1024 × 1536` · WebP RGBA | 90,468 | `b06be0652ab1cf1eb444d7617369f77570cf82eccd1de9d5dc35ba575ca5343f` | 4 trajes nórdicos calibrados ao torso/ombros dos retratos base (`north-mail`, `scribe-robes`, `rune-mantle`, `vigil-armor`) | `LivingHeroPortrait` (`LivingPortrait.kt`), `RelicArt` (`RpgDesign.kt`) |
| `living_helms_atlas.webp` | Atlas de elmos/coroas `2 × 2` | `1024 × 1536` · WebP RGBA | 32,010 | `bb8db995e2b3624984549baf38680fd7291012823d8332fa62d9bd6165f49a72` | 4 elmos/coroas abertos calibrados acima dos olhos (`oath-helm`, `mimir-runic-crown`, `traveler-helm`, `will-helm`) | `LivingHeroPortrait` (`LivingPortrait.kt`), `RelicArt` (`RpgDesign.kt`) |
| `living_artifacts_atlas.webp` | Atlas de artefatos `4 × 2` | `1024 × 512` · WebP RGBA | 77,474 | `b80db748d5156a433efb4f7766f6ab10241515d2aaf10a41b0fa280be3fe87a6` | 8 fíbulas, broches, relíquias e armas/aliados contextuais para o manto e Arsenal | `LivingHeroPortrait` (`LivingPortrait.kt`), `RelicArt` (`RpgDesign.kt`) |
| `darian_forge_atlas.webp` | Atlas da Forja `2 × 2` | `1280 × 720` · WebP RGB | 148,708 | `85c7fc0976c00caaadac4cee47588c5c28691fdee94574d985f612280b1267ae` | 3 estados da oficina de Darian (Etapa 0 ruínas, Etapa 1 parcial, Etapa 2 restaurada) + retrato de Darian | `ForgeScreen.kt` (`AtlasCellCropped`) |
| `hero_background_atlas.webp` | Atlas de fundos `2 × 2` | `960 × 1440` · WebP RGB | 101,160 | `597c0e0d6160a1c813400e55446f8024be75f75947af52873dcbd19c5464a33d` | 4 ambientes nórdicos (Salão Viking, Biblioteca Rúnica, Torre dos Cumes, Oficina dos Artífices) | `LivingHeroPortrait`, `EquipmentScreen.kt`, `ForgeScreen.kt` |
| `hero_frames_atlas.webp` | Atlas de molduras `2 × 2` | `960 × 1440` · WebP RGBA | 94,150 | `38b31c2ec7947623158fd7ce67e79d43212968ba827ad6855890615fec1a3d08` | 4 molduras pictóricas do Retrato Vivo (Ferro, Bronze Borre, Prata Rúnica, Lendária Nove Reinos Yggdrasil) | `LivingHeroPortrait` (`LivingPortrait.kt`), `RelicArt` (`RpgDesign.kt`) |
| `viking_arsenal_ui_atlas.webp` | Atlas de UI do Arsenal `4 × 2` | `1024 × 512` · WebP RGBA | 77,072 | `ac7486c76f12d676e9e1b6142e74b8b2b1ad86fcfb5527dd83a438ff1718324e` | Encaixes vazio/equipado, placas e 4 brasões de raridade (Comum/Incomum, Raro, Épico, Lendário) | `EquipmentScreen.kt` (`EquipmentSlot`, `RarityCrestBadge`) |

## 1.7.3.1 — O Legado das Runas (O Acervo dos Ancestrais)

Oito novos recursos pictóricos originais gerados via `generate_image` (orçamento de `8/10` chamadas, preservando as `2` de reserva sem necessidade de regeneração): **1 prancha conceitual** mantida fora do APK (`docs/art/concepts/runic-heritage-1731.webp`) e **7 atlas WebP de produção** em `app/src/main/res/drawable-nodpi/`, somando **663.010 bytes (647,5 KB / 0,63 MB)** dentro do APK (limite máximo de `3 MB`) e preservando integralmente toda a biblioteca pictórica das versões `1.3` a `1.7.3`. Documentação visual em [VISUAL-1.7.3.1.md](../VISUAL-1.7.3.1.md).

| Arquivo WebP (1.7.3.1) | Tipo / Grade | Dimensões e Formato | Bytes | SHA-256 | Finalidade Artística | Componentes Onde é Aplicado |
|---|---|---|---:|---|---|---|
| `concepts/runic-heritage-1731.webp` | Prancha conceitual (fora do APK) | `1376 × 768` · WebP RGB | 237,796 | `6edd3dd3102d6ead605aaa21048b57bc5b74249bddc16cc2fd5b0a11f4847346` | Prancha conceitual de direção visual para Heráldica Rúnica, Codex, Coleções Lendárias e Consagração | Referência conceitual (fora do APK) |
| `ancestral_heraldry_atlas.webp` | Atlas heráldico `4 × 2` | `1024 × 512` · WebP RGBA | 137,802 | `b9c31e7294f6bacdd650282c5350e382ebb41668859af5c90369f5eba85c15d5` | 8 símbolos heráldicos vikings (Corvo, Lobo, Carvalho, Machado, Escudo, Tomo, Montanha, Yggdrasil) | `HeraldicCrest`, `HeraldryEditorScreen` (`HeraldryAndCodex.kt`) |
| `viking_heraldic_bases_atlas.webp` | Atlas de escudos e estandartes `4 × 2` | `1024 × 512` · WebP RGBA | 55,274 | `ccb090094275e8916cf40b70f4b95afd44d17eaeb4a10967ecb75ddc5066e6cf` | 4 bases vazias (Escudo Circular, Oval, Placa Rúnica, Medalhão), 3 estandartes (Tecido, Couro, Cerimonial) e placa de inscrição | `HeraldicCrest`, `HeraldicBanner`, `HeraldryEditorScreen` |
| `forge_legacy_items_atlas.webp` | Atlas de peças da Forja `2 × 2` | `768 × 768` · WebP RGBA | 55,136 | `c84fa68772ae047577e226ce54269210b4a4f003c9a01ec93f8e05860e6ac360` | 4 novos equipamentos fabricáveis (`vigil-axe`, `runic-staff`, `northern-blade`, `constellation-brooch`) | `RelicArt` (`RpgDesign.kt`), `ForgeScreen.kt`, `EquipmentScreen.kt`, `LivingPortrait.kt` |
| `legendary_collections_atlas.webp` | Atlas de coleções `3 × 2` | `1080 × 720` · WebP RGB | 106,112 | `b3a4fb726d665c02961fbfb05dc54557f5dbce23e18423a39699efc97cfa01a3` | 3 pinturas temáticas + 3 medalhões exclusivos (*Juramento de Ferro*, *Sabedoria de Mímir*, *Vigília Boreal*) | `LegendaryCollectionsSection` (`EquipmentScreen.kt`, `ForgeScreen.kt`) |
| `runic_codex_surfaces_atlas.webp` | Atlas de superfícies do Codex `3 × 2` | `960 × 640` · WebP RGB | 93,222 | `e05cea3c632b1f1b49780ae6356243e6e2419404e07314e8fe29970e2e1449be` | 6 superfícies materiais limpas (Pedra escura, Pedra clara, Ferro, Velino, Madeira escandinava, Bronze) para sobreposição das 24 runas vetoriais | `RunicCodexScreen` (`HeraldryAndCodex.kt`) |
| `forge_consacration_atlas.webp` | Atlas de consagração `4 × 2` | `1024 × 512` · WebP RGBA | 90,790 | `10d1573e2f46ef70a62b62f2ff079edb77e06009af8ee6b13fe767504af477d4` | 3 selos de aprimoramento (Forjado, Gravado, Consagrado), moldura viking aprimorada e 4 ornamentos metálicos | `UpgradeStageBadge`, `ForgeScreen.kt`, `EquipmentScreen.kt` |
| `living_hero_heritage_atlas.webp` | Atlas de acabamentos do Retrato Vivo `3 × 2` | `960 × 640` · WebP RGBA | 124,674 | `80c396aa73e271670b43a2a9124b65dc8e29c222bbb0502e652e8e0984cf2a06` | Broche das Constelações, gola Borre gravada, faixa cerimonial, aro heráldico, coroa de moldura e aplique consagrado | `LivingHeroPortrait` (`LivingPortrait.kt`) |

## 1.7.5 — A Mesa de Odin (Nordic Council Design System 3.2)

Dez novos recursos pictóricos originais gerados via `generate_image` (orçamento estrito de `10/12` chamadas, preservando as `2` de reserva sem necessidade de regeneração): **1 prancha conceitual** mantida fora do APK (`docs/art/concepts/nordic-council-175.webp`) e **9 recursos WebP de produção** em `app/src/main/res/drawable-nodpi/`, somando **664.174 bytes (648,6 KB / 0,63 MB)** dentro do APK (limite máximo de `4 MB`) e preservando integralmente toda a biblioteca pictórica das versões `1.3` a `1.7.3.1`. Documentação visual em [VISUAL-1.7.5.md](../VISUAL-1.7.5.md) e [DESIGN-SYSTEM-3.2.md](../DESIGN-SYSTEM-3.2.md).

| Arquivo WebP (1.7.5) | Tipo / Grade | Dimensões e Formato | Bytes | SHA-256 | Finalidade Artística | Componentes Onde é Aplicado |
|---|---|---|---:|---|---|---|
| `concepts/nordic-council-175.webp` | Prancha conceitual (fora do APK) | `1376 × 768` · WebP RGB | 164,370 | `11258693807e2b695f91ebb7d94e5bc91e98878b0b297581d47fd573ab32f1e8` | Direção visual para Mesa de Odin, Caminhos 3.0, Forja do Tempo, Conselho dos Nove e materiais DS 3.2 | Referência conceitual (fora do APK) |
| `odin_council_hall.webp` | Ilustração panorâmica `1 × 1` | `1152 × 640` · WebP RGB | 95,454 | `27a22573f27ffd564de60aa810aff669dd9cb57ae82ee300fe22648fd3ce5772` | Salão estratégico do Conselho de Odin com mesa de madeira esculpida, mapas e luz fria nórdica | `OdinHallHeader` (`OdinCouncilScreens.kt`) |
| `odin_strategies_atlas.webp` | Atlas de estratégias `3 × 2` | `960 × 640` · WebP RGB | 52,028 | `25b03f850c5bdbd8d2352f40318791fedc67e98d041596deaa6f69d2fb5ffc6c` | 6 medalhões estratégicos (Guardião, Erudito, Berserker, Estrategista, Explorador, Artífice) | `OdinStrategyTile` (`OdinCouncilScreens.kt`, `NordicCouncilDesign.kt`) |
| `nine_council_seals_atlas.webp` | Atlas de áreas da vida `3 × 3` | `768 × 768` · WebP RGBA | 128,246 | `1ad45f72c71442515bc994933ab401b8d9863b25ab3173774e2ac35a688a8c75` | 9 selos metálicos para as áreas da vida (Estudos, Saúde, Disciplina, Carreira, Criatividade, Finanças, Social, Equilíbrio, Propósito) | `LifeAreaSeal` (`CouncilOfNineScreen.kt`, `NordicCouncilDesign.kt`) |
| `time_forge_instruments_atlas.webp` | Atlas da Forja do Tempo `2 × 2` | `768 × 768` · WebP RGBA | 87,184 | `40cea7f5b807c05be7556fa905991111809afe896bcea48a852351e489c02dde` | Anel cronométrico bronze, ampulheta rúnica, anel de pausa em prata oxidada e mostrador de bronze martelado | `TimeForgeDial` (`TimeForgeScreen.kt`) |
| `nordic_focus_scenes_atlas.webp` | Atlas de atmosferas `2 × 2` | `960 × 640` · WebP RGB | 51,906 | `7b098475703e9e2b1190b0bf7151c22a18c5e74a7bcaadc7814f950086e2d925` | 4 cenários calmos para o Modo Imersivo (Chuva no salão, Brasa da forja, Vento boreal, Biblioteca sob vela) | `ImmersiveFocusBackdrop` (`TimeForgeScreen.kt`) |
| `runic_calendar_atlas.webp` | Atlas de Agenda 3.0 `3 × 2` | `768 × 512` · WebP RGBA | 56,018 | `5e67f6d95e3c33c1192c3cc2553ae17f87f8caa0c4adcc4261313eef2bc020a0` | Faixa temporal, moldura do dia atual, cartão de bloco, marcador de evento, selo de recorrência e lua de descanso | `AgendaScreens.kt`, `NordicCouncilDesign.kt` |
| `nordic_strategy_controls_atlas.webp` | Atlas de controles `4 × 2` | `1024 × 512` · WebP RGBA | 55,016 | `53db7344b0dcc16ce1f2483bab9577571265fb2a7095437488acbf8d8c764d54` | Trilho Gantt, ponta de dependência, marco de bronze, estrela de prioridade, placa de tarefa, cartão Kanban, broche de fixação e brasão de risco | `Paths30Screens.kt`, `OdinCouncilScreens.kt`, `AgendaScreens.kt` |
| `nordic_council_materials_atlas.webp` | Atlas de materiais DS 3.2 `3 × 2` | `960 × 640` · WebP RGB | 46,780 | `ba93432d504aa10e9c32fb5e93124a98a568fdeed6a15b0b418b3d3157361262` | 6 superfícies limpas (Madeira nórdica, Velino, Couro de projeto, Bronze martelado, Prata oxidada, Ferro escuro) | `CouncilMaterialSurface` (`NordicCouncilDesign.kt`) |
| `odin_council_ornaments_atlas.webp` | Atlas ornamental `4 × 2` | `1024 × 512` · WebP RGBA | 91,542 | `cdac45fe7fe389fb9c73b765865f3ec928cacab3ea6f9afedd2a83f65a651992` | Cantoneira Urnes, 3 divisores estratégicos, rosa-dos-ventos rúnica, selo hexagonal de meta, corvos gêmeos e moldura vazada | `NordicCouncilDesign.kt`, `OdinCouncilScreens.kt`, `CouncilOfNineScreen.kt` |

## 1.7.5.1 — O Conselho Restaurado (Etapa II · Design System 3.2.1)

Seis gerações pictóricas originais realizadas via `generate_image` (orçamento `6 / 8`, com `2` gerações de reserva preservadas sem uso), otimizadas em WebP RGBA com recortes limpos e zero texto dinâmico incorporado:

- `concepts/nordic-council-refined-1751.webp`: prancha conceitual de direção artística (fora do APK).
- `5` atlas WebP de produção em `app/src/main/res/drawable-nodpi/`, somando **374,380 bytes (365.6 KB / 0.36 MB)** adicionais no APK (teto: `2 MB`).

| Arquivo WebP (1.7.5.1) | Dimensões | Bytes | SHA-256 | Finalidade |
|---|---|---:|---|---|
| `nordic-council-refined-1751.webp` | `1280×720` | 139,048 | `59c32cb02b385a83dd4244fc9f1be7741ada7a8e9920c10d7da16071345d4bae` | Prancha conceitual de refinamento 1.7.5.1 (fora do APK) |
| `restored_council_buttons_atlas.webp` | `960×450` | 51,922 | `7c29c38f419a8056aff506f534b5dd4c9c5c29c10d55d53bbbc8000a10845482` | 6 placas metálicas de botões vikings nine-slice (2×3) |
| `time_forge_refinement_atlas.webp` | `960×640` | 114,968 | `a67247a93d0bac8b74338bd38a65ed7bada269d25bb80b054778d41a7e71859e` | 6 instrumentos da Forja do Tempo (3×2: trabalho, pausa, ciclo, selos) |
| `nordic_planning_marks_atlas.webp` | `768×384` | 60,656 | `4565b50b8ffe2bc2c076cefa448e4053608c6e0a346b09c1a8921b6efa4a86fa` | 8 marcadores nórdicos de planejamento (4×2) |
| `nordic_strategy_frames_atlas.webp` | `1024×512` | 45,336 | `a10be7e1ca7d778c5fd0bd0e0499cb308b0503f228857be23b64adce9343bcee` | 8 molduras, cantoneiras, divisores e placa rúnica (4×2) |
| `odin_council_seals_refined_atlas.webp` | `768×512` | 101,498 | `0d836aaea6763df86409d24cfb3dfd1898f832ab119f13892fc3d0047a4add3e` | 6 selos refinados das estratégias de Odin (3×2) |

## 1.7.5.9 — A Reforma de Aetheris (NORTHBORN · Design System 4.0)

Três novos recursos pictóricos originais gerados via `generate_image` e otimizados em WebP (2 pranchas conceituais fora do APK + 1 atlas de materiais 4×2 em `app/src/main/res/drawable-nodpi/`, somando **47.402 bytes / 46,3 KB** adicionados ao APK):

| Arquivo WebP (1.7.5.9) | Dimensões | Bytes | SHA-256 | Finalidade |
|---|---|---:|---|---|
| `concepts/northborn-master-board-1759.webp` | `1280×720` | 88,858 | `d8c4d9d6169477cdda75d29065cbc1d25c39e22e222f75ee15e8ff9fdd4d1b7f` | Prancha mestra NORTHBORN (paleta, materiais, botões arquitetônicos, divisores geométricos, iconografia — fora do APK) |
| `concepts/northborn-screen-direction.webp` | `1280×720` | 142,802 | `528a869272d82c52f24f84ede70f1be4fa09b0676b8177d0d7b320eaaf10402f` | Prancha de direção compositiva de telas (Jornada, Mesa Estratégica, Biblioteca, Arsenal, Forja do Tempo, Atlas — fora do APK) |
| `northborn_materials_atlas.webp` | `1024×512` | 47,402 | `364cd42891099937cd3bc3c14b7fe6682b80cdbc8ea03d777b671096c77826ff` | Atlas 4×2 de 8 materiais escandinavos foscos (freixo nórdico, carvalho defumado, ferro enegrecido, bronze envelhecido, prata oxidada, couro selaria, velino arquivístico, ardósia vulcânica) |

**Limitação registrada (Seção 30):** após a geração destes 3 arquivos, o serviço `generate_image` (`gemini-3.1-flash-image`) retornou esgotamento de cota (`Quota exceeded`, janela de recarga ~69h), impedindo a geração das 10 folhas pictóricas adicionais planejadas (`northborn_buttons_atlas` até `northborn_interface_details_atlas`). Em vez de fingir vetores como imagens geradas ou reciclar placas ruidosas, o Design System 4.0 combina o novo `northborn_materials_atlas.webp` com geometria arquitetônica pura em Jetpack Compose, iconografia vetorial de traço uniforme inspirada diretamente em `northborn-master-board-1759.webp`, divisores geométricos nórdicos e uso seletivo dos retratos, mapas e selos já existentes no projeto.

