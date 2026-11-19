# Relatório Visual — ASCEND 1.7.3.1 (O Legado das Runas)

Este documento consolida a direção artística, o acervo pictórico original gerado e as **15 capturas reais** renderizadas via Robolectric + Compose UI na versão **1.7.3.1**.

## 1. Direção Artística e Prancha Conceitual

- **Prancha conceitual (fora do APK):** [`docs/art/concepts/runic-heritage-1731.webp`](art/concepts/runic-heritage-1731.webp) (`1376 × 768`, RGB, `237.796 bytes`).
- **Paleta e materialidade:** basalto vulcânico, ferro enegrecido, bronze antigo, prata oxidada, couro curtido, lã escura e velino envelhecido com iluminação fria escandinava, seguindo a estética austera do Norte.
- **Orçamento de recursos no APK:** **7 novos atlas WebP** em `app/src/main/res/drawable-nodpi/` somando **663.010 bytes (`647,5 KB / 0,63 MB`)**, muito abaixo do teto de `3 MB` (detalhes e SHA-256 em [`docs/art/README.md`](art/README.md)).
- **Geração de imagens:** `8/10` solicitações utilizadas (`2/10` preservadas em reserva; nenhuma regeneração necessária).

## 2. Inspeção Visual Real e Ajustes Aplicados

Todas as **15 capturas reais** em `docs/screenshots/1.7.3.1/` foram inspecionadas diretamente antes da entrega:

1. **Estágio III Consagrado na Forja (`09-aprimoramento-consagrado.png`):** na primeira renderização, `oath-helm` (primeiro card da lista) ainda permanecia em `II · GRAVADO`; ajustado o cenário de captura para consagrar `oath-helm` e exibir claramente o selo dourado `III · CONSAGRADO` lado a lado com a progressão `07 → 08 → 09`.
2. **Filtros combináveis no Arsenal 2.1 (`12-arsenal-filtros.png`):** na primeira captura, o painel expandido de filtros ficava abaixo da dobra; ajustada a rolagem para enquadrar busca, ordenação, raridade, posse, origem, coleção lendária e estágio da Forja na mesma tela.
3. **Retrato Vivo 1.1 (`13-retrato-vivo-11-acabamentos.png`):** confirmada preservação integral da fisionomia dos retratos originais (`wanderer_light/medium/dark.webp`), sobreposição limpa de colar gravado (`Gravado`) e broche cerimonial (`Consagrado`), `constellation-brooch`, `vigil-axe` no medalhão inferior esquerdo e medalhão heráldico no canto superior esquerdo.
4. **Tema Claro (`14-tema-claro-1731.png`) e `320 dp` / Fonte `130%` (`15-estreito-320dp-fonte-130-1731.png`):** confirmado alto contraste no modo claro e quebra limpa sem cortes das 4 abas do Arsenal (`Equipamentos`, `Coleções`, `Configurações`, `Estúdio`), 7 nichos e botões em telas estreitas com fonte ampliada.
5. **Navegação inferior (`AetherisNavigation.kt`):** reforçado o contraste dos ícones inativos e o brilho rúnico da aba ativa preservando integralmente os 5 destinos principais.

## 3. Catálogo das 15 Capturas Reais (`docs/screenshots/1.7.3.1/`)

| # | Arquivo | Escopo validado |
|---|---|---|
| 01 | [`01-editor-brasao.png`](screenshots/1.7.3.1/01-editor-brasao.png) | Editor de Heráldica Rúnica (brasão, estandarte, ornamento, inscrição `ᚨ ᚱ ᛟ`) |
| 02 | [`02-brasao-heroi.png`](screenshots/1.7.3.1/02-brasao-heroi.png) | Perfil do Herói (`HeroStage`) com brasão pessoal e Retrato Vivo 1.1 |
| 03 | [`03-estandarte-pessoal.png`](screenshots/1.7.3.1/03-estandarte-pessoal.png) | Retrato Ampliado (`PortraitScreen`) com Estandarte Pessoal e camadas |
| 04 | [`04-codex-24-runas.png`](screenshots/1.7.3.1/04-codex-24-runas.png) | Codex Rúnico com 24 runas vetoriais do Elder Futhark, 6 superfícies e aviso histórico |
| 05 | [`05-codex-runa-detalhe.png`](screenshots/1.7.3.1/05-codex-runa-detalhe.png) | Detalhe de *Ansuz* com separação explícita entre contexto histórico e ficção de Aetheris |
| 06 | [`06-forja-12-receitas.png`](screenshots/1.7.3.1/06-forja-12-receitas.png) | Forja de Darian expandida (12 receitas, abas Fabricar / Aprimorar / Coleções) |
| 07 | [`07-aprimoramento-forjado.png`](screenshots/1.7.3.1/07-aprimoramento-forjado.png) | Aba Aprimorar — Estágio I (*Forjado*) |
| 08 | [`08-aprimoramento-gravado.png`](screenshots/1.7.3.1/08-aprimoramento-gravado.png) | Aba Aprimorar — Estágio II (*Gravado*) |
| 09 | [`09-aprimoramento-consagrado.png`](screenshots/1.7.3.1/09-aprimoramento-consagrado.png) | Aba Aprimorar — Estágio III (*Consagrado*) |
| 10 | [`10-colecoes-lendarias.png`](screenshots/1.7.3.1/10-colecoes-lendarias.png) | Coleções Lendárias (*Juramento de Ferro* em progresso com selos de estágio) |
| 11 | [`11-colecao-concluida.png`](screenshots/1.7.3.1/11-colecao-concluida.png) | Coleção *Sabedoria de Mímir* concluída (`4/4`, título *Herdeiro de Mímir*, brasão) |
| 12 | [`12-arsenal-filtros.png`](screenshots/1.7.3.1/12-arsenal-filtros.png) | Arsenal 2.1 com busca, ordenação e filtros combináveis expandidos |
| 13 | [`13-retrato-vivo-11-acabamentos.png`](screenshots/1.7.3.1/13-retrato-vivo-11-acabamentos.png) | Comparativo Retrato Vivo 1.1: Forjado (1) → Gravado (2) → Consagrado (3) + brasão |
| 14 | [`14-tema-claro-1731.png`](screenshots/1.7.3.1/14-tema-claro-1731.png) | Tema claro na Heráldica Rúnica (alto contraste sobre velino) |
| 15 | [`15-estreito-320dp-fonte-130-1731.png`](screenshots/1.7.3.1/15-estreito-320dp-fonte-130-1731.png) | Tela estreita `320 dp` com fonte `130%` no Arsenal 2.1 |

## 4. Limites Declarados

As capturas foram produzidas em ambiente Robolectric (renderização nativa Skia em JVM). Não houve validação em GPU de aparelho físico Android, densidade física de tela real, TalkBack nem IME físico.
