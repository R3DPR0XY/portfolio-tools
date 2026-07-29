# Estudo de Caso - LootPanel

## Resumo

LootPanel e um mod Fabric client-side que exibe um painel de itens ao abrir containers. Ele soma itens iguais, ignora o inventario do jogador e apresenta o conteudo de forma mais clara.

## Problema

Containers grandes podem ser lentos de analisar visualmente, principalmente quando existem muitos itens repetidos. O jogador precisa contar manualmente e comparar slots.

## Solucao

O mod injeta um painel nas telas de container e renderiza uma lista consolidada de itens. O painel mostra icone, nome e quantidade total, reduzindo o esforco de leitura.

## Pontos tecnicos

- Uso de mixins em telas de container.
- Leitura e agregacao de stacks de itens.
- Renderizacao customizada de painel.
- Configuracao separada para comportamento e layout.
- Projeto Gradle/Fabric organizado para build.

## O que demonstra

- Capacidade de transformar uma dor de usuario em ferramenta pequena e util.
- Conhecimento de renderizacao client-side.
- Manipulacao de dados do inventario sem alterar regras do jogo.
- Organizacao de um mod simples, focado e publicavel.

## Caminho

[mods/lootpanel](../../mods/lootpanel)

