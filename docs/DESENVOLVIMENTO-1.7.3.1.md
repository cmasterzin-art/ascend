# Desenvolvimento — ASCEND 1.7.3.1 (O Legado das Runas)

Registro incremental da implementação da versão **1.7.3.1** (*Runic Heritage & Legendary Collections Update*), derivada da branch `feat/ascend-1.7.3` (`cd2d1e2`) na nova branch `feat/ascend-1.7.3.1`.

## Base Confirmada

- Branch de origem: `feat/ascend-1.7.3` (preservada intacta; `main` preservada intacta)
- Branch ativa: `feat/ascend-1.7.3.1`
- Versão alvo: `versionName = "1.7.3.1"`, `versionCode = 25`
- Baseline herdada: **191 testes aprovados**, Lint `0` erros, Room `v9`, Backup `v11`, `0` permissão `INTERNET`, 8 receitas na Forja de Darian, 12 runas históricas *Elder Futhark*.

## Plano de Execução por Etapas

- [ ] **Etapa 0 — Estabilidade & Correções da 1.7.3:**
  - `6.1`: Comandos explícitos e idempotentes (`equipItem`, `unequipItem`, `setEquipment`) preservando `equip(Asset)` para compatibilidade.
  - `6.2`: Retrocompatibilidade explícita de fases de expedição de Foco (`focusRuleVersion` aditivo em `quests`: `0` legado `>0s` para fases pré-1.7.3.1 sem reuso indevido em claims novos, `1` `>= 300s` para novas fases).
  - `6.3`: Integridade de `applyLoadout()` — prévia explícita de disponíveis/ausentes/alterações antes de aplicar e preservação de slots válidos quando há itens ausentes.
  - `6.4`: Preservação de rascunho do Estúdio (`rememberSaveable`), confirmação antes de descartar alterações não salvas, manutenção do Estúdio aberto em caso de erro de persistência.
  - `6.5`: Alinhamento documental da chave real do ledger (`forge:<recipeId>`).
- [ ] **Etapa 1 — Direção Artística & Acervo dos Ancestrais (até 10 gerações: 8 planejadas + até 2 reserva):**
  - `docs/art/concepts/runic-heritage-1731.webp` (fora do APK)
  - `ancestral_heraldry_atlas.webp` (`4 × 2` RGBA — 8 símbolos heráldicos)
  - `viking_heraldic_bases_atlas.webp` (`4 × 2` RGBA — 4 escudos/placas + 3 estandartes + suporte de inscrição)
  - `forge_legacy_items_atlas.webp` (`2 × 2` RGBA — `vigil-axe`, `runic-staff`, `northern-blade`, `constellation-brooch`)
  - `legendary_collections_atlas.webp` (`3 × 2` — 3 pinturas temáticas + 3 medalhões exclusivos)
  - `runic_codex_surfaces_atlas.webp` (`3 × 2` — 6 superfícies materiais do Codex Rúnico)
  - `forge_consacration_atlas.webp` (`4 × 2` RGBA — 3 selos Forjado/Gravado/Consagrado + moldura aprimorada + 4 ornamentos materiais)
  - `living_hero_heritage_atlas.webp` (`3 × 2` RGBA — camadas calibradas de acabamento e Broche das Constelações no Retrato Vivo)
- [ ] **Etapa 2 — Forja Expandida (12 receitas), Aprimoramentos (3 estágios) e 3 Coleções Lendárias:**
  - 4 novas receitas (`vigil-axe`, `runic-staff`, `northern-blade`, `constellation-brooch`), total 12.
  - Aprimoramentos cosméticos `Forjado (0) → Gravado (1) → Consagrado (2)` em `Asset.stage` (`forge-upgrade:<itemId>:<stage>`, `0 XP`).
  - 3 Coleções Lendárias (*Juramento de Ferro*, *Sabedoria de Mímir*, *Vigília Boreal*) independentes das 3 configurações (`Jornada`, `Conselho`, `Expedição`), com recompensas idempotentes e diálogos/memórias de Darian.
- [ ] **Etapa 3 — Heráldica Rúnica & Codex Rúnico (24 Elder Futhark):**
  - 8 símbolos heráldicos, 4 formatos, 4 metais, cor secundária, inscrição rúnica, ornamentação, 3 estandartes, persistência em **Room v10 (`MIGRATION_9_10`)** e **Backup v12** (`v1`–`v12`).
  - Elder Futhark completo com 24 runas históricas vetoriais verificadas (preservando mapeamentos explícitos das 12 anteriores), contextualização histórica rigorosa (diferenciando Elder vs. Younger Futhark e associações ficcionais de Aetheris) e Codex Rúnico acessível via Biblioteca de Mímir / Crônicas.
- [ ] **Etapa 4 — Retrato Vivo 1.1, Arsenal 2.1 & Design System 3.1:**
  - Integração das 4 novas peças, acabamentos de aprimoramento e brasão pessoal no Retrato Vivo e nas telas do app (Jornada compacta, Perfil, Zoom, Estúdio, Arsenal, Crônicas, Coleções).
  - Arsenal 2.1 com abas compactas (*Equipamentos*, *Coleções*, *Configurações*, *Estúdio*), busca textual, filtros combináveis (incluindo fundos/molduras/estágio/coleção/origem/posse/equipado) e ordenação.
  - Refinamento material e legibilidade dos ícones da navegação inferior.
- [ ] **Etapa 5 — Validação & Entrega:**
  - Testes completos (`191` preservados + nova suíte `RunicHeritageTest` e `RunicHeritageUiTest`), Android Lint (`0` erros), 15 capturas reais em `docs/screenshots/1.7.3.1/`, inspeção visual, documentação completa, CI assinatura oficial (`e1f003ef...`) e publicação em `downloads/ASCEND-1.7.3.1.apk`.

## Orçamento de Geração de Imagens (`generate_image`)

- Limite autorizado: até **10 chamadas** (`8` planejadas + até `2` reserva).
- Chamadas executadas até o momento: `0/10`.
