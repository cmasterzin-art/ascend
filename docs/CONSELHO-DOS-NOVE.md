# Conselho dos Nove — Áreas da Vida e Objetivos Pessoais (ASCEND 1.7.5)

O **Conselho dos Nove** (`CouncilOfNineScreen.kt`, `StrategicData.kt`) oferece organização opcional de alto nível para as áreas da vida e metas pessoais trimestrais/anuais, preservando 100% do funcionamento para itens sem área vinculada e sem introduzir economia paralela no RPG.

## 1. Nove Áreas Opcionais da Vida (`LifeArea`)

Ativáveis sob demanda com um toque (*Ativar as 9 áreas padrão*) ou criadas livremente:

1. **Estudos** · 2. **Saúde** · 3. **Disciplina** · 4. **Carreira** · 5. **Criatividade** · 6. **Finanças** · 7. **Social** · 8. **Equilíbrio** · 9. **Propósito**.

- Cada área utiliza um selo metálico exclusivo do atlas `nine_council_seals_atlas.webp` (grade `3 × 3` RGBA).
- Permite criar, editar nome/descrição/meta semanal/selo, reordenar (`↑`/`↓`), arquivar ou excluir (a exclusão apenas remove o vínculo `areaId`, nunca apaga hábitos, caminhos, cadernos ou blocos do usuário).
- Calcula o tempo semanal realmente alocado em blocos (`areaAllocatedMinutes`) comparando-o à meta semanal configurada.

## 2. Objetivos Pessoais (`StrategicGoal`)

- Suporte a visão **Trimestral** (`quarterly`) e **Anual** (`annual`), com filtro por horizonte.
- Vinculação direta a **Caminhos** (`linkedPathIds`) e **Hábitos** (`linkedHabitIds`).
- **Progresso verificável (`strategicGoalProgress`):** calculado a partir do percentual real das tarefas concluídas nos Caminhos vinculados e da taxa de cumprimento nos últimos 30 dias dos Hábitos vinculados. Não gera XP, moedas ou multiplicadores extras.
