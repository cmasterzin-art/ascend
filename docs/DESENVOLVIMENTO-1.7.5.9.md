# Diário de Desenvolvimento — ASCEND 1.7.5.9 (A Reforma de Aetheris)

- **Branch de origem:** `feat/ascend-1.7.5.1` (`56f55e4`, 227 testes aprovados, `versionCode 28`)
- **Branch de trabalho:** `feat/ascend-1.7.5.9` (`versionName = "1.7.5.9"`, `versionCode = 29`)
- **Invariantes preservados:** `br.com.ascend.aetheris`, Room v11, Backup v13, zero `INTERNET`, assinatura oficial `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`, todas as correções técnicas da `1.7.5.1` e economia do RPG intactas.

---

## Checkpoints por Fase

- [x] **Fase 1 — Auditoria Global:** Branch `feat/ascend-1.7.5.9` criada, `versionCode = 29 / versionName = "1.7.5.9"`, inventário completo de todas as 32 rotas e 17 famílias de tela registrado em `docs/AUDITORIA-VISUAL-1.7.5.9.md`.
- [ ] **Fase 2 — Identidade Northborn (Gerações 1–13):** Pranchas conceituais (`docs/art/concepts/northborn-master-board-1759.webp`, `northborn-screen-direction.webp`) + 11 atlas WebP otimizados em `drawable-nodpi/` (orçamento: `13` planejadas + `3` reservas).
- [ ] **Fase 3 — Design System 4.0 (`NorthbornDesignSystem.kt`, `Design.kt`, `DesignSystem2.kt`, `NorthComponents.kt`, `AetherisNavigation.kt`, `NordicCouncilDesign.kt`, `Forms.kt`):** Paleta semântica Northborn (Escuro / Claro documental / Minimalista), tipografia em 3 famílias, escala modular `4/8/12/16/24/32 dp`, 5 superfícies semânticas, Botões 4.0 (7 níveis hierárquicos), controles transversais sem adesivos rasterizados, navegação inferior e cabeçalho secundário reconstruídos.
- [ ] **Fase 4 — Núcleo da Experiência:** Jornada (`JourneyHome.kt`, `DesignSystem2.kt`), Hábitos/Rotinas (`JourneyScreens.kt`), Perfil Heroico (`CharacterScreen.kt`, `HeroSheet.kt`).
- [ ] **Fase 5 — Produtividade Estratégica:** Mesa de Odin (`OdinCouncilScreens.kt`), Agenda 3.0 (`AgendaScreens.kt`), Caminhos/Gantt/Kanban (`CaminhosScreens.kt`, `Paths30Screens.kt`), Forja do Tempo (`TimeForgeScreen.kt`), Conselho dos Nove (`CouncilOfNineScreen.kt`).
- [ ] **Fase 6 — RPG e Conhecimento:** Biblioteca de Mímir (`MimirScreens.kt`), Arsenal & Forja (`EquipmentScreen.kt`, `ForgeScreen.kt`), Atlas & Exploração (`WorldScreen.kt`, `ExplorationScreens.kt`), Missões/Crônicas/Codex/Heráldica (`SagaScreen.kt`, `ChronicleScreens.kt`, `HeraldryAndCodex.kt`).
- [ ] **Fase 7 — Interfaces Secundárias:** Configurações, Backup, Onboarding e Diálogos (`MainActivity.kt`, `Forms.kt`, `ProductivityScreens.kt`).
- [ ] **Fase 8 — Polimento, 30 Capturas Reais & Autoauditoria Estética:** Suíte `NorthbornUiReforgeTest.kt`, inspeção visual de todas as 30 capturas em `docs/screenshots/1.7.5.9/`, comparações diretas `1.7.5.1 × 1.7.5.9` e refinamento iterativo.
- [ ] **Fase 9 — Validação, CI Oficial, APK Assinado e Documentação Final:** `./gradlew testDebugUnitTest`, `lintDebug`, `assembleDebug`, GitHub Actions, verificação de `downloads/ASCEND-1.7.5.9.apk`, SHA-256, certificado `e1f003ef...` e relatórios finais.
