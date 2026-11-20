# A Forja do Tempo — Sistema Avançado de Foco 3.0 (ASCEND 1.7.5)

A **Forja do Tempo** (`TimeForgeScreen.kt`, `AmbientAudio.kt`, `Reminders.kt`, `Repository.kt`) aprofunda o cronômetro offline do ASCEND mantendo a atribuição histórica exata de minutos.

## 1. Cinco Modos de Foco

1. **Foco livre** (`free`) — contagem contínua flexível.
2. **Pomodoro** (`pomodoro`) — ciclos clássicos 25 min foco / 5 min pausa curta / 15 min pausa longa a cada 4 ciclos.
3. **Concentração profunda** (`deep`) — blocos estendidos 50/10/20 min.
4. **Contagem regressiva** (`countdown`) — meta fechada para entregas específicas.
5. **Ciclos personalizados** (`custom`) — duração de trabalho, pausas curta/longa, ciclos e avanço automático configuráveis.

## 2. Integridade da Medição de Tempo

- **Pausas nunca contam como foco:** `phase in {"short_break", "long_break"}` jamais grava `FocusSession` nem incrementa minutos de hábitos cronometrados.
- **Relógio monotônico + detecção de anomalia:** usa `SystemClock.elapsedRealtime()` (`startedRealtimeMs`) quando disponível e sinaliza saltos do relógio do sistema (`clockAnomaly`), registrando `pauseCount` e `intervalsJson`.
- **Vínculo direto com bloco (`blockId`):** iniciar foco a partir de um bloco da Agenda 3.0 registra o `blockId` sem duplicar contagem com o hábito ou tarefa vinculada.

## 3. Modo Imersivo Nórdico e 3 Sons Ambientes Offline

- **Modo Imersivo opcional:** exibe o aro cronométrico forjado (`time_forge_instruments_atlas.webp`) sobre atmosferas serenas (`nordic_focus_scenes_atlas.webp`), respeitando a preferência de redução de movimento e modo minimalista.
- **Síntese procedural offline (`AmbientAudioEngine`):** gera áudio PCM 16-bit localmente via `AudioTrack` para três paisagens sonoras — **Chuva sobre o salão**, **Lareira da forja** e **Vento boreal** — além de **Silêncio do Norte** e controle de volume, sem arquivos externos nem acesso à rede.
- **Notificações locais opcionais (`Reminders.notifyTimeForge`):** avisos para fim de foco, fim de pausa, próximo bloco e evento próximo, respeitando `POST_NOTIFICATIONS` no Android 13+.
