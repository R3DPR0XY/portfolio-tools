# Estudo de Caso - Kagerov

## Resumo

Kagerov e um mod Fabric para Minecraft focado em ferramentas client-side de texto, leitura, edicao e interface. O projeto mostra criacao de telas customizadas, integracao com recursos do jogo e organizacao de funcionalidades em modulos.

## Problema

O fluxo padrao de edicao e leitura de textos/livros no Minecraft e limitado para usos mais criativos e de roleplay. Faltam recursos de biblioteca, edicao avancada, paleta de texto e ferramentas que tornem a escrita mais pratica.

## Solucao

O mod adiciona telas e utilitarios para melhorar o uso de livros, placas e textos. A estrutura separa responsabilidades entre clipboard, biblioteca, telas de editor/leitor e seletores visuais.

## Pontos tecnicos

- Mod Fabric com entrypoints client e main.
- Uso de mixins para integrar funcionalidades em telas existentes.
- Telas customizadas para editor, leitor, biblioteca e paleta.
- Recursos em JSON para idiomas e configuracao visual.
- Organizacao em pacotes por dominio: `book`, `screen`, `mixin`.

## O que demonstra

- Capacidade de criar UX dentro de um ambiente limitado.
- Organizacao de codigo em um projeto client-side.
- Conhecimento de Fabric, mixins e recursos do Minecraft.
- Pensamento voltado a ferramentas de produtividade para usuarios.

## Caminho

[mods/kagerov](../../mods/kagerov)

