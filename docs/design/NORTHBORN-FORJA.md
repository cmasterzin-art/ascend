# ASCEND 1.7.5.9.2 · Controles da Forja

## Direção artística

A Jornada apresenta os atalhos de Odin, Forja do Tempo e Planejador em placas de ferro com gravuras. Os três dividem a mesma faixa, com rótulos em até duas linhas. A ação de criar um hábito tem uma placa de bronze. A hierarquia deixa os atalhos visíveis perto do retrato e mantém uma superfície mais silenciosa na lista de hábitos.

| Ação | Material |
| --- | --- |
| Registrar, criar e avançar | Carvalho escurecido, com borda de bronze e fixadores moldados |
| Editar, abrir e usar ferramentas | Ferro oleado no escuro e estanho acetinado no claro |
| Resgatar recompensa | Ferro escuro e incrustações inspiradas em anéis torcidos escandinavos |
| Confirmar | Cobre com pátina verde |
| Remover | Óxido de ferro avermelhado |
| Voltar ou seguir um link | Texto com o menor destaque do conjunto |

As seis placas foram modeladas e iluminadas em 3D. A moldura elevada, o rebaixo central, a rugosidade e os encaixes fazem parte da geometria ou do material renderizado. O desenho mantém o miolo livre para os rótulos do Android. As bordas são dimensionadas em nove partes, preservando os encaixes quando o botão muda de largura ou recebe duas linhas de texto. O widget usa a mesma geometria em recursos Android 9-patch.

A biblioteca de componentes do **UI UX Designer** orientou a distinção entre ações principais, secundárias e destrutivas, os estados e os alvos de toque. A referência do **Canva** serviu para separar as peças pelo relevo e pelo peso visual. A busca pelo **Designly** não encontrou ferramentas acessíveis nesta sessão. [Open generated image](https://www.canva.com/M/MAHbwP8px-g?utm_source=OC-AaBlKrqBJJNF&utm_campaign=agent_connector_create_image_asset_opened)

## Uso no app

Os componentes compartilhados levam a família de controles aos Hábitos, Planejador, Caminhos, Mesa de Odin, Mímir, Missões, Inventário, diálogos e outros fluxos. Abas e filtros usam uma placa de menor intensidade na seleção, acompanhada de um marcador e do estado selecionado nativo. Os widgets exibem a mesma família de placas em Concluir, Registrar e Jornada.

As ações compactas têm alvo mínimo de 48 dp e rótulos de até duas linhas. O foco por teclado recebe contorno de 2 dp nos botões principais, compactos, nos atalhos da Jornada e nas ações do códice. A pressão escurece a superfície e a opção de reduzir movimento torna esse feedback instantâneo. O modo minimalista usa cores sólidas. O estado de carregamento é anunciado como “Em andamento” e usa reticências estáticas com movimento reduzido.

Ao comparar a cor do texto com cada pixel da região central das seis imagens, o menor contraste medido foi de **5,40:1**, na placa de ação destrutiva. A amostragem cobre o miolo da arte em repouso, onde ficam os rótulos.

## Reprodução da arte

Os arquivos abaixo geram seis WebPs e três imagens 9-patch incluídas no APK. O processo usa Blender 4.3 e Pillow localmente.

```sh
blender -b -P docs/art/tools/forged_controls.py -- /tmp/northborn-plates
python3 docs/art/tools/export_forged_controls.py /tmp/northborn-plates app/src/main/res
```

## Antes e depois

| Tela | Código 35 | Código 36 |
| --- | --- | --- |
| Jornada | [Antes](../screenshots/1.7.5.9.2/01-jornada.png) | [Placas dos atalhos e criação de hábito](../screenshots/1.7.5.9.2-forja/01-jornada.png) |
| Hábitos | [Antes](../screenshots/1.7.5.9.2/04-lista-habitos.png) | [Criar, explorar, abas e filtros](../screenshots/1.7.5.9.2-forja/04-lista-habitos.png) |
| Mesa de Odin | [Antes](../screenshots/1.7.5.9.2/06-mesa-de-odin.png) | [Placa principal e ferramentas](../screenshots/1.7.5.9.2-forja/06-mesa-de-odin.png) |
| Forja do Tempo | [Antes](../screenshots/1.7.5.9.2/13-forja-do-tempo.png) | [Pausar, registrar e modo imersivo](../screenshots/1.7.5.9.2-forja/13-forja-do-tempo.png) |
| Biblioteca de Mímir | [Antes](../screenshots/1.7.5.9.2/16-biblioteca-mimir.png) | [Ações do códice](../screenshots/1.7.5.9.2-forja/16-biblioteca-mimir.png) |
| Arsenal | [Antes](../screenshots/1.7.5.9.2/18-arsenal.png) | [Estúdio, Forja e seleção de aba](../screenshots/1.7.5.9.2-forja/18-arsenal.png) |
| Formulários | [Antes](../screenshots/1.7.5.9.2/30-dialogos-formularios.png) | [Salvar, cancelar e confirmar](../screenshots/1.7.5.9.2-forja/30-dialogos-formularios.png) |
| Missões | [Antes](../screenshots/1.7.5.9.2/45-missoes.png) | [Categorias e criação de missão](../screenshots/1.7.5.9.2-forja/45-missoes.png) |
| Planejador claro (320 dp) | [Antes](../screenshots/1.7.5.9.2/50-planejador-claro-320.png) | [Estanho e carvalho](../screenshots/1.7.5.9.2-forja/50-planejador-claro-320.png) |
| Widget escuro (fonte 130%) | [Antes](../screenshots/1.7.5.9.2/widget-estreito-130-151.png) | [Concluir e Registrar](../screenshots/1.7.5.9.2-forja/widget-estreito-130-151.png) |

[Estados dos controles no escuro](../screenshots/1.7.5.9.2-forja/57-forja-controles-escuro.png) · [Claro a 320 dp com fonte 130%](../screenshots/1.7.5.9.2-forja/58-forja-controles-claro-320-130.png) · [Minimalista](../screenshots/1.7.5.9.2-forja/59-forja-controles-minimalista.png) · [Odin em 320 dp com fonte 130%](../screenshots/1.7.5.9.2-forja/32-odin-320-fonte-130.png) · [Widget claro em dia sem rituais](../screenshots/1.7.5.9.2-forja/widget-sem-rituais-151.png)

As capturas foram renderizadas pelo próprio app com dados de teste, usando Compose e RemoteViews em Robolectric. Os testes em aparelho físico, launcher real e TalkBack ficam pendentes. [Validação](../../VALIDACAO.md)
