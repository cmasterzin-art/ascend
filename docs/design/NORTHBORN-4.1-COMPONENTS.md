# NORTHBORN 4.1 · Componentes do protótipo

O protótipo herda os papéis de cor, fontes e semântica do NORTHBORN 4.0. `NorthbornRebirth.kt` concentra os componentes novos para o recorte inicial.

| Componente | Comportamento |
|---|---|
| RebirthMasthead | Arte 3:1 recortada com Crop e gradiente de leitura. Coluna de texto define a altura. O modo minimalista usa a mesma estrutura sem imagem. |
| RebirthAction | Chanfro assimétrico e pátina para o comando principal. Secundário em superfície elevada neutra. Material3 mantém estado desabilitado, ripple e semântica. |
| RebirthIndex | Abas agrupadas com seleção semântica e marcador de linha. Quebra controlada antes de criar rolagem horizontal. |
| RebirthRule | Linha de separação para índices e agrupamentos de dados. |
| RebirthReadingLeaf | Margem tipográfica, indicador lateral e superfície lisa para Markdown e flashcards. |
| Jornada editorial | Paisagem dentro da largura medida, retrato existente, métrica de XP, ação de 48 dp e hábitos por linhas. |
| Navegação | Cinco destinos e rótulos anteriores. Nos módulos piloto, barra e campo ativo identificam seleção além da cor do ícone. Respeita redução de movimento. |
| Busca | Campo de linha com superfície documental em Mímir. Usa estado, pesquisa e rótulo acessível existentes. Os formulários dos outros módulos conservam o componente 4.0. |

A escala de 8, 12, 16, 18 e 24 dp diferencia linhas operacionais, margens e blocos. Títulos usam a tipografia editorial do tema. Rótulos, controles e metadados usam a fonte de interface. Nenhum componente despacha persistência. A lógica de comandos permanece nos callbacks existentes.
