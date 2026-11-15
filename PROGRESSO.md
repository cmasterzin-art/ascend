# ASCEND — progresso da versão 1.0

Implementação nativa em Kotlin, Compose, Material 3, Navigation, Room, DataStore, ViewModel, Coroutines e StateFlow. Sem permissão INTERNET, contas, anúncios ou backend.

## Implementado

- Criação de personagem, cinco classes, nome, objetivos, títulos e avatar vetorial personalizável.
- Cinco áreas de navegação com paleta escura, ilustrações vetoriais, barras animadas e celebração de nível.
- Hábitos simples, quantitativos, cronometrados e repetitivos. Nome, descrição, categoria, ícone, cor, atributo, dificuldade, dias, hora, meta e recompensas editáveis.
- XP, moedas e pontos livres; recompensa proporcional por valores absolutos, sem duplicação. Snapshot de recompensas por hábito/dia, edição explícita de registros e reversão.
- Pausa, arquivo, exclusão com confirmação preservando histórico, duplicação e ordenação. Sequências respeitam dias previstos e mudanças/pausas registradas.
- Calendário, histórico, rotinas e cronômetro persistente com pausa/retomada e registro de minutos efetivos.
- Missões diárias, semanais, épicas, personalizadas e chefes opcionais configuráveis. Resgates únicos por ocorrência e recálculo de recompensas após correções.
- Conquistas, galeria, títulos por marcos, loja, inventário, compra/equipamento/remoção, companheiros cosméticos com falas locais.
- Mapa vetorial de seis regiões, revelação por níveis, cinco capítulos escritos e acessíveis mediante marcos.
- Vila com cinco construções e duas etapas de recuperação após as ruínas, cenas que refletem a restauração e compras transacionais.
- Diário editável/pesquisável, estatísticas reais com períodos, XP diário/semanal/mensal, barras, calendário de consistência e histórico de atributos.
- Lembretes locais opcionais com AlarmManager e reagendamento após reinício; não dependem de internet.
- Backup JSON versionado, checksum SHA-256, validação estrutural e importação transacional, via seletor de arquivos Android.

## Limites desta versão

- Regiões além da vila são revelações de mapa e narrativa; a reconstrução interativa está concentrada na Vila do Despertar.
- Recuperação manual de sequências por exceções ainda não existe. Pausas e dias de descanso são suportados; a ausência não retira XP ou conquistas.
- Avatar é um retrato vetorial, com poucas opções de roupa/pele/moldura. Não há animação de corpo inteiro ou combate.
- Gráficos disponíveis: barras e calendário. Não há gráfico de linhas dedicado.
- Lembretes são aproximados e sujeitos às restrições de bateria do Android. O cronômetro usa horário persistido, não requer serviço contínuo e não toca alarme de foco em segundo plano.
- Calendário mostra a seleção atual de hábitos, enquanto os cálculos de programação e sequências guardam mudanças registradas a partir desta versão. Alterações feitas no mesmo dia substituem a programação desse dia.
- Conquistas representam marcos atingidos e são preservadas após correções; XP, pontos, moedas e recompensas de missões são recalculados. Missões resgatadas preservam snapshots das regras e recompensas por ocorrência; edições afetam as próximas ocorrências.
- Ao desfazer moedas já gastas, o saldo disponível fica em zero e ganhos futuros compensam a diferença. Nenhuma compra pode exceder o saldo.
- Preferências de interface e permissões de notificações pertencem ao aparelho e não são transferidas no backup. O diário do backup é texto legível.
- Sem migração destrutiva. Esquema Room v1 exportado; futuras alterações exigem migrations explícitas e testes antes de incrementar a versão.

A validação executada e os resultados de compilação constam em VALIDACAO.md.
