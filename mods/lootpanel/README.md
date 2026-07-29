# LootPanel

Base de mod Fabric client-side para Minecraft Java 1.21.8.

## O que ja esta "feito"

- Abre junto com telas de container, como bau.
- Soma itens iguais dentro do container aberto.
- Ignora o inventario do jogador.
- Desenha um painel com:
  - icone do item;
  - nome abaixo do icone;
  - contagem total abaixo do nome.

## Arquivos principais

- `src/main/java/com/bmod/chestpanel/mixin/HandledScreenMixin.java`: injeta o painel no final do render das telas de container.
- `src/main/java/com/bmod/chestpanel/client/ChestPanelRenderer.java`: coleta os itens e desenha o painel.
- `src/main/resources/fabric.mod.json`: manifesto do mod.
- `gradle.properties`: versoes do Minecraft/Fabric.

## Como compilar

Instale um JDK 21 e rode:

```powershell
gradle build
```

O `.jar` sai em:

```text
build/libs/
```

Depois coloque o `.jar` na pasta `mods` junto com Fabric Loader e Fabric API para Minecraft 1.21.8.

## Onde mexer no visual

No arquivo `ChestPanelRenderer.java`, ajuste:

- `PANEL_WIDTH`
- `CELL_WIDTH`
- `CELL_HEIGHT`
- cores `BACKGROUND`, `BORDER`, `TEXT_COLOR`, `COUNT_COLOR`
- posicao `x` e `y` dentro do metodo `render`

## Observacao

Neste ambiente nao havia `JAVA_HOME` nem `java` no PATH, entao a compilacao nao foi validada aqui. A estrutura esta pronta para compilar assim que um JDK 21 estiver instalado.
