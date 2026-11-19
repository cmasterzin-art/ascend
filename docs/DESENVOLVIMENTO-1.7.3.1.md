# Diário de Desenvolvimento — ASCEND 1.7.3.1 (O Legado das Runas)

- **Branch:** `feat/ascend-1.7.3.1` (derivada de `feat/ascend-1.7.3` commit `cd2d1e2`, preservando `main` e `feat/ascend-1.7.3` intactas).
- **Versionamento:** `versionName = "1.7.3.1"`, `versionCode = 25`.
- **Esquema e Backup:** **Room v10 (`MIGRATION_9_10`)** e **Backup v12** (`v1`–`v12`).

## Checkpoints Concluídos

1. **Etapa 1 — Acervo Pictórico (`b25ff40`):**
   - `8/10` solicitações de `generate_image` (`2/10` em reserva): 1 prancha conceitual fora do APK (`docs/art/concepts/runic-heritage-1731.webp`) e 7 atlas WebP de produção em `drawable-nodpi/` somando `663.010 bytes` (`647,5 KB / 0,63 MB`, abaixo do limite de `3 MB`).
2. **Etapa 0 — Correções Estruturais da 1.7.3:**
   - **6.1 Equipamento idempotente:** `Repository.equipItem`, `unequipItem` e `setEquipment` garantindo 1 item por espaço, posse, compatibilidade e zero efeitos econômicos (mantendo `equip(Asset)` para retrocompatibilidade).
   - **6.2 Foco em expedições versionado:** `focusRuleVersion` em `Quest` e `Claim` (`0` = legado `>0s` preservando fases não resgatadas e claims da 1.7.2; `1` = `>= 300s` em novas fases e claims).
   - **6.3 Aplicação parcial segura de configurações:** `previewLoadoutApplication` + diálogo explícito listando disponíveis, ausentes e espaços alterados; `applyLoadout` parcial nunca apaga espaços válidos já equipados fora dos itens disponíveis.
   - **6.4 Integridade do Estúdio de Personalização:** rascunho persistido em `rememberSaveable`, diálogo de confirmação ao descartar alterações, permanência do Estúdio aberto se o salvamento falhar e prévia de itens bloqueados sem conceder posse.
   - **6.5 Alinhamento documental:** corrigido `craft:<recipeId>` → `forge:<recipeId>` em `docs/FORJA-DARIAN-1.7.3.md`, `README.md`, `CHANGELOG.md` e `PROGRESSO.md`.
3. **Pilares 1 a 6:**
   - **Heráldica Rúnica (`RunicHeritageData.kt`, `HeraldryAndCodex.kt`):** 8 símbolos, 4 formatos, 4 metais, 5 cores, 5 ornamentos, 3 estandartes, inscrição de até 6 runas com transliteração aproximada explícita, persistida em `heraldry_profile` (Room v10 / Backup v12) e exibida em Jornada, Perfil, Retrato Ampliado, Retrato Vivo 1.1, Arsenal, Salão das Memórias, Crônicas e Coleções concluídas.
   - **Codex Rúnico (`VikingDesignSystem.kt`, `HeraldryAndCodex.kt`):** 24 runas vetoriais do Elder Futhark preservando ordinais `0..11` das 12 originais, mapeamento explícito `sealFallbackRune`, 6 superfícies limpas, busca/filtros/favoritos e distinção rigorosa entre Elder Futhark (24), Younger Futhark (16) e ficção de Aetheris.
   - **Forja de Darian expandida (`ForgeCrafting.kt`, `ForgeScreen.kt`):** 12 receitas (`+vigil-axe`, `+runic-staff`, `+northern-blade`, `+constellation-brooch`), 3 estágios de aprimoramento (*Forjado → Gravado → Consagrado*) via `Asset.stage` e ledger determinístico `forge-upgrade:<itemId>:<stage>` (`0 XP`), sem venda na Loja.
   - **3 Coleções Lendárias:** *Juramento de Ferro*, *Sabedoria de Mímir* e *Vigília Boreal* (3 × 4 obras da Forja, conclusão por posse legítima, troféu `"collection:<id>"` + título exclusivo com `0 XP`/multiplicadores), coexistindo independentemente das 3 Configurações de Equipamento (*Jornada*, *Conselho*, *Expedição*).
   - **Retrato Vivo 1.1, Arsenal 2.1 & Design System 3.1 (`LivingPortrait.kt`, `EquipmentScreen.kt`, `AetherisNavigation.kt`):** acabamentos *Gravado*/*Consagrado* e medalhão heráldico no Retrato Vivo; Arsenal em 4 abas compactas com busca, filtros combináveis e 5 ordenações; contraste refinado na navegação inferior.
4. **Validação & Entrega:**
   - Suíte completa (**202 testes**: `191` preservados + `11` novos em `RunicHeritageTest` e `RunicHeritageUiTest`), `lintDebug` com `0` erros, 15 capturas reais inspecionadas e APK oficial assinado via GitHub Actions (`ASCEND_KEYSTORE_BASE64`).
