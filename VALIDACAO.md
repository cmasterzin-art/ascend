# Validação — ASCEND 1.5.0

16/11/2026. Base 1.4.5: `6653cfece61581cd486cc0dd4d40cd3fbb157ccd`.

- `testDebugUnitTest assembleDebug`: **BUILD SUCCESSFUL**, **90 testes aprovados**, zero falhas, erros ou ignorados.
- APK: `downloads/ASCEND-1.5.0.apk`, 23,121,655 bytes.
- SHA-256: `fdd78bf2934772e7e27c5a6f66ec5abecd3ab12c9b52aa9cced6da7019f8265c`.
- Assinatura v2 válida; certificado preservado desde a 1.1: `e1f003efe40d53754db66325c40177178aaa81d4ad02ab910c85642c681caae4`.
- `zipalign -c 4` e `git diff --check`: aprovados.
- Manifesto: `br.com.ascend.aetheris`, versionName 1.5.0, versionCode 8, minSdk 26/targetSdk 35. **Sem INTERNET, VIBRATE, serviço contínuo ou permissão de alarme exato**.
- JDK Temurin 17, SDK Android 35, Gradle 8.9; APK pessoal de depuração assinado. Chave privada fora do Git.

## Cobertura nova

Os 78 testes da 1.4.5 continuam aprovados, sem remoção de verificações.

| Suíte | Testes | Resultado verificado |
|---|---:|---|
| ExplorationTest | 6 | Seis regiões/níveis preservados e gate 249/250 XP; baseline inicial; oito resgates concorrentes com um sucesso; correções/zero e revalidação sem novo Claim; três aventuras/sete etapas, snapshots independentes, transformação, três fases de chefe, broche/conquista/título; persistência/reload/backup; Saga e Expedição usando o mesmo hábito sem duplicar XP; edição/arquivo de hábito; abertura real do esquema Room v5 e backup v6; oito conclusões concorrentes pelo widget com um registro; data local/descanso e rejeição de hábito complexo |
| ExplorationUiTest | 2 | Atlas → região → local → expedição pela interface; seleção de cinco hábitos e início; registros reais e resgate; três estados do santuário, três fases do Guardião e final 100%; Memórias; retorno Android; 320 dp/fonte 130%/claro/minimalista/redução de movimento e lugares velados |
| WidgetTest | 4 | RemoteViews compacto/expandido/sem personagem; PendingIntent de hábito complexo abre o app sem completar; PendingIntent real de simples executa receiver/repositório e não duplica XP; host simulado recebe alteração/correção e agenda atualização; estado noturno, ações reduzidas conforme altura e intenção do cabeçalho para Jornada |

A regressão cobre Grimório/rotinas, XP, snapshots, moedas, missões, Sagas, chefes antigos, inventário, árvore, reconstrução, Crônicas, memórias, sons, descanso, lembretes e migrações/backups anteriores.

## Persistência e economia

Room **permanece v5**. Não houve alteração de tabelas, novo esquema ou migração destrutiva. As Expedições reutilizam Quest/Claim/ledger/assets e o cálculo por fotografia das Sagas. O teste abre um banco construído do esquema v5 exportado, compara o GameState e confirma exploração em zero. Migrações explícitas 1 → 2 → 3 → 4 → 5 permanecem testadas.

Backup **v7** guarda os novos conteúdos e lê v1–v7. O teste restaura um envelope v6 sem inventar expedições. A suíte completa também verifica backups antigos e as outras migrações.

Cada fase tem Quest/Claim determinísticos. As três etapas do santuário e as três do Guardião exigem novas atividades; somente o último marco de cada aventura paga o prêmio definido. O teste de integração realiza cinco hábitos de 10 XP, cada registro repetido, e confere apenas 50 XP de hábitos; os resgates independentes da Saga e da Expedição acrescentam seus 20/40 XP uma vez. Correções invalidam a cadeia e o ledger econômico; restaurar progresso revalida os mesmos Claims. Cosméticos e conquistas conquistados seguem a política de preservação existente.

## Evidências e limites

[Capturas e comparação](docs/VISUAL-1.5.md) · [Arquitetura/regras/widget](docs/EXPLORACAO-1.5.md) · [JUnit e certificado](docs/validation/).

Renderização nativa Robolectric API 28 em 411 × 891 dp e 320 × 740 dp, tema claro/escuro, fonte 130%, redução de movimento e modo minimalista. As capturas de widgets são RemoteViews Android reais desenhadas no ambiente de testes, não uma instalação em launcher físico. O fluxo de conclusão executou o PendingIntent/receiver real e observou o repositório; o host é simulado.

Não houve instalação em telefone/emulador completo, benchmark físico, Android Lint, audição em aparelho ou teste físico de reboot, bateria, meia-noite/fuso e insets do fabricante. Atualizações de widget usam recursos aproximados e podem atrasar sob restrições do Android; a conclusão sempre revalida data/programação. Permissão de notificações e alarmes de lembrete continuam sujeitas ao fabricante.

Apenas a Floresta recebeu expedições. Não há novos retratos alternativos, combate ou edição/abandono de uma Expedição após iniciar. As Sagas mantêm suas próprias funções de abandono/arquivo/reinício. Nenhum recurso depende de conta, servidor ou serviço pago.

Package/certificado iguais e versionCode maior permitem atualizar os APKs 1.1–1.4.5 fornecidos sem desinstalar; atualização sobre uma instalação física não foi executada. A 1.0 usa outra assinatura. Exporte um backup antes, conforme [INSTALACAO.md](INSTALACAO.md).
