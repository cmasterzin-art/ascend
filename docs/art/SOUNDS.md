# Efeitos originais — ASCEND 1.4.5

Cinco efeitos mono PCM, 22.050 Hz, sintetizados originalmente para ASCEND. O script [synthesize-effects.py](synthesize-effects.py) reproduz os arquivos a partir da raiz do projeto. Não há gravações, amostras, músicas ou recursos de jogos de terceiros.

| Arquivo | Caráter | Duração |
|---|---|---:|
| aether_mark.wav | Marca ritual, conclusão de hábito | 0,24 s |
| aether_bell.wav | Sino distante, nível/conquista/saga | 0,65 s |
| aether_iron.wav | Metal suave, equipamento | 0,26 s |
| aether_wood.wav | Madeira, reconstrução | 0,25 s |
| aether_page.wav | Folha, abertura de capítulo/crônica semanal | 0,16 s |

Os efeitos começam desligados. Ative-os em Herói → Configurações → Efeitos sonoros. Volume relativo de 0–100%, além do volume de mídia do Android. O SoundPool usa assistência/sonificação e verifica modo de toque normal e volume de mídia positivo antes de tocar. Modo silencioso e vibração do Android impedem os efeitos. Um evento consolidado prioriza o sino de nível/conquista/saga em vez de sobrepor vários sons. Não há reprodução contínua ou serviço em segundo plano; o SoundPool é liberado com a tela.

Vibração do aplicativo não foi implementada e não é solicitada. A política de áudio e a persistência de preferências foram testadas; audição e comportamento físico do fabricante/DND dependem de verificação no telefone. Preferências sonoras são locais em DataStore e não acompanham o backup JSON de progresso.
