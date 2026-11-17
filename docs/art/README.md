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
