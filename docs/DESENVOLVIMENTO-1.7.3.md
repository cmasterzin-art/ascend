# Desenvolvimento — ASCEND 1.7.3 (A Forja das Lendas)

Registro incremental da implementação da versão **1.7.3** (*Living Hero, Runic Crafting & Nordic Viking Arsenal Update*), derivada da branch `feat/ascend-1.7.2` (`dafcb03`) na nova branch `feat/ascend-1.7.3`.

## Base Confirmada

- Branch de origem: `feat/ascend-1.7.2` (preservada intacta; `main` preservada intacta)
- Branch ativa: `feat/ascend-1.7.3`
- Versão alvo: `versionName = "1.7.3"`, `versionCode = 24`
- Baseline de testes herdada: 181 testes automatizados verdes, Lint 0 erros, 0 permissão `INTERNET`
- Retratos originais verificados: `wanderer_light.webp`, `wanderer_medium.webp`, `wanderer_dark.webp` — todos `1024 × 1536` (2:3), geometria facial e de ombros idêntica entre os 3 tons de pele.

## Plano de Execução por Etapas

- [x] **Auditoria focalizada e preparação da branch** (`feat/ascend-1.7.3`, geometria dos retratos, catálogo de equipamentos, A Forja `building:1`, bugs 16.1 e 16.2 confirmados em `Exploration.kt`)
- [x] **Etapa 1:** Geração pictórica (8/10 chamadas usadas, 0 regenerações necessárias), inspeção visual e processamento/alinhamento dos 7 atlases WebP no APK (`606,5 KB` / `0,59 MB`, limite `<= 3 MB`) + prancha conceitual `docs/art/concepts/hero-living-173.webp` fora do APK. Alinhamento verificado visualmente nos 3 retratos originais (`light`, `medium`, `dark`).
- [x] **Etapa 2:** Módulo I — Retrato Vivo (`LivingPortrait.kt`, camadas pictóricas calibradas sobre os 3 retratos originais, 10 especializações com luz/runa/selo, unificação de `Avatar`, `ForgePortrait`, `RelicArt` e `PortraitScreen`)
- [x] **Etapa 3:** Persistência Room v9 (`MIGRATION_8_9` + `schemas/9.json`) / Backup v11 (`v1..v11`), Arsenal dos Nove Reinos (`EquipmentScreen.kt`), Estúdio de Personalização (preview bloqueado sem conceder posse, desfazer, salvar, restaurar) e 3 Conjuntos de Equipamento (`Jornada`, `Conselho`, `Expedição`)
- [x] **Etapa 4:** Módulo II — A Forja de Darian (`ForgeCrafting.kt`, `ForgeScreen.kt`, 8 receitas cosméticas em Fragmentos, integração na Vila `building:1` preservando 2 etapas, diálogo e selo exclusivos para `mestre_da_forja` sem vantagem econômica)
- [x] **Etapa 5:** Refinamento pictórico nórdico/viking (`AtlasCellCropped` sem distorção, 12 runas históricas Elder Futhark verificadas, contraste adaptativo tema claro/escuro nas câmaras do Arsenal e Forja, suporte a modo minimalista e redução de movimento)
- [x] **Etapa 6:** Correções 1.7.2 (seção 16), versionamento 1.7.3 (`code 24`), 191 testes unitários/migração/backup/UI aprovados (`181` preservados + `10` novos), 14 capturas reais em `docs/screenshots/1.7.3/`, Lint (`0` erros), Build, CI assinatura oficial e documentação final

## Orçamento de Geração de Imagens (`generate_image`)

- Limite da versão: até **10 chamadas** (8 planejadas + até 2 de reserva de qualidade)
- Chamadas executadas: **8/10** (`hero_living_concept_173`, `living_armor_atlas`, `living_helms_atlas`, `living_artifacts_atlas`, `darian_forge_atlas`, `hero_background_atlas`, `hero_frames_atlas`, `viking_arsenal_ui_atlas`). Reserva (2/10) preservada sem necessidade de uso.
