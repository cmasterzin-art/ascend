# ASCEND 1.7.2 — Atlas de Aetheris 2.0

O **Atlas de Aetheris 2.0** ([`RpgDesign.kt`](../app/src/main/java/br/com/ascend/aetheris/RpgDesign.kt), [`WorldScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/WorldScreen.kt)) amplia o mapa geográfico original (`nordic_atlas.webp`, preservado integralmente) com navegação interativa acessível, seis estados explícitos (`RegionAtlasState`) e distinção honesta entre regiões já exploráveis e territórios futuros.

## 1. Navegação, Zoom e Acessibilidade

- **Pinça e arrasto com limites (`pointerInput`):** zoom contínuo entre `100%` e `240%`, com deslocamento automaticamente limitado (`clampOffset`) às bordas do mapa.
- **Controles acessíveis dedicados (`atlas_controls`):** botões `−`, `Centralizar` e `+` (alvos mínimos de `48 dp`, com `contentDescription` explícitos) dispostos em `FlowRow` para nunca quebrar `"Zoom 100%"` em telas estreitas (`320 dp`) com fonte `130%`.
- **Lista acessível de regiões (`atlas_accessible_list`):** índice estruturado logo abaixo do mapa listando todas as regiões com estado, nível de desbloqueio e progresso, permitindo navegação completa sem depender de gestos de pinça ou precisão tátil nos marcadores.
- **Preservação do movimento reduzido e modo minimalista:** animações de marcador respeitam `reducedMotion` e ornamentos pictóricos degradam para selos vetoriais limpos no modo minimalista.

## 2. Estados das Regiões (`RegionAtlasState`)

Cada região do Atlas exibe seu estado real derivado do progresso do personagem:

| Estado (`RegionAtlasState`) | Rótulo na UI | Significado |
|---|---|---|
| `Locked` | `Bloqueada` | Nível do personagem abaixo do requisito (`regionLevels`) |
| `Revealed` | `Revelada` | Região revelada (ex.: Vila do Despertar ou região recém-aberta) |
| `Available` | `Disponível` | Região explorável liberada por nível, pronta para iniciar expedição |
| `InProgress` | `Em exploração` | Pelo menos uma expedição iniciada ou fase em curso (`1%`–`99%`) |
| `Completed` | `Concluída` | Todas as expedições regionais concluídas (`100%`) |
| `NoInteractiveYet` | `Sem conteúdo interativo ainda` | Território revelado por nível, mas reservado para versões futuras |

## 3. Regiões do Continente (`regions` / `regionLevels`)

1. **Vila do Despertar** (Nível 1) — Assentamento central com 5 construções reconstruíveis.
2. **Floresta da Persistência** (Nível 3) — Explorável (`1.5`, integralmente preservada).
3. **Biblioteca das Eras** (Nível 5) — **Nova região explorável (`1.7.2`)**, guardiã Eydis, 3 expedições / 8 fases.
4. **Montanhas da Disciplina** (Nível 8) — **Nova região explorável (`1.7.2`)**, guardião Haldor, 3 expedições / 8 fases.
5. **Santuário do Equilíbrio** (Nível 12) — Marco revelado no Atlas com aviso explícito *"Sem conteúdo interativo ainda"*.
6. **Fortaleza da Vontade** (Nível 20) — Marco revelado no Atlas com aviso explícito *"Sem conteúdo interativo ainda"*.
