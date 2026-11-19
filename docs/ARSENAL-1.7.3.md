# ASCEND 1.7.3 — Módulo III: Arsenal dos Nove Reinos, Estúdio & 3 Conjuntos

O **Arsenal dos Nove Reinos** ([`EquipmentScreen.kt`](../app/src/main/java/br/com/ascend/aetheris/EquipmentScreen.kt)) evolui o inventário cosmético sem romper a aba da **Loja** existente, a navegação ou a economia de Fragmentos.

## 1. Câmara do Arsenal e 5 Níveis de Raridade

- **Câmara Central:** combina o `LivingHeroPortrait` com os **7 nichos pictóricos nórdicos** (*Cabeça*, *Corpo / capa*, *Acessório*, *Arma cosmética*, *Companheiro*, *Moldura* e *Fundo*) usando os encaixes forjados de `viking_arsenal_ui_atlas.webp` (células `0` vazio e `1` equipado) e scrim adaptativo para os temas escuro e claro.
- **5 Níveis de Raridade (`RarityCrestBadge`):** *Comum* (Ferro Simples), *Incomum* (Ferro Temperado), *Raro* (Bronze Viking), *Épico* (Prata Rúnica) e *Lendário* (Ouro dos Reinos), diferenciados por cor, espessura de borda, runa histórica *Elder Futhark* e brasão pictórico (`viking_arsenal_ui_atlas` células `4..7`).
- **Origem e Representação:** cada item exibe sua origem exata (`Mercador`, `A Forja de Darian`, `Expedição` ou `Habilidade`) e sua forma de representação no Retrato Vivo (`portraitRepresentationFor`).

## 2. Estúdio de Personalização (`HeroCustomizationStudio`)

- Permite compor todos os 7 espaços ao vivo no `LivingHeroPortrait` antes de confirmar.
- **Prévia segura de itens bloqueados:** itens ainda não possuídos podem ser provados na prévia ao vivo, mas exibem explicitamente o selo **`Bloqueado`** (`studio_locked_banner`) e impedem o salvamento até serem removidos da seleção; `Repository.applyStudioSelection` valida a posse real no banco e rejeita qualquer item não possuído.
- **Controles de segurança:** *Desfazer* (retorna ao estado atual equipado), *Salvar visual* (aplica atomicamente em Room) e *Restaurar anterior* (recupera o visual imediatamente anterior ao último salvamento na sessão).

## 3. Três Conjuntos de Equipamento (`EquipmentLoadout`, Room v9 & Backup v11)

- Três espaços configuráveis e renomeáveis (`loadout:1` *Jornada*, `loadout:2` *Conselho*, `loadout:3` *Expedição*) persistidos na nova tabela `equipment_loadouts` criada aditivamente por **`MIGRATION_8_9`** (`app/schemas/br.com.ascend.aetheris.GameDatabase/9.json`).
- **Ações disponíveis:** *Salvar atual*, *Aplicar*, *Renomear* (`1..40` caracteres) e *Limpar*.
- **Integridade de espaços e itens ausentes:** `validateLoadoutInput` rejeita incompatibilidade entre tipo de item e espaço; se algum item salvo em um conjunto importado não estiver possuído no inventário, `loadoutMissingItems` sinaliza explicitamente a ausência e `applyLoadout` aplica apenas as peças válidas sem substituição silenciosa.
- **Backup v11:** inclui `loadouts`, mantém compatibilidade total de leitura com backups `v1`–`v10` (preenchendo `loadouts = emptyList()` em arquivos antigos) e executa rollback atômico em caso de validação inválida.
