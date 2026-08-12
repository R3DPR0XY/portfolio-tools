# MPersonagem

[English version](README_EN.md)

Plugin Paper para sistema de personagens de roleplay com nome, sobrenome, idade, altura, genero, descricao, menus, atributos por altura e suporte a PlaceholderAPI/TAB.

## Requisitos

- Paper 1.21.x
- Java 21
- PlaceholderAPI opcional
- TAB opcional

## Comandos de jogador

- `/personagem` abre o menu de selecao e inicia a criacao guiada dos personagens.
- `/id` mostra sua identidade: nome, idade, altura e dados de roleplay.
- `/id <jogador>` mostra a identidade de outro jogador, se o staff tiver permissao.
- `/pularano` soma +1 ano em todos os personagens salvos.

## Comando administrativo recomendado

Use `/mpersonagem ajuda` para ver as opcoes de staff.

- `/mpersonagem reload` recarrega `config.yml` e `messages.yml`.
- `/mpersonagem ver <player>` abre a identidade de outro jogador.
- `/mpersonagem editar <player>` abre a edicao do personagem ativo de outro jogador.
- `/mpersonagem reset <player>` apaga todos os personagens de um jogador.
- `/mpersonagem setaltura <player> <altura>` define a altura do personagem ativo.

Nao existe `/mpersonagem setidade`. A idade deve ser editada pelo menu de personagem ou avancada em massa com `/pularano`.

## Fluxo de criacao

A criacao de personagem segue uma sequencia de menus:

1. Menu de talentos.
2. Menu de altura com previa dos atributos por scaling.
3. Menu de nome e idade.
4. Menu de confirmacao final.

O nome e sobrenome ainda usam entrada pelo chat, porque inventarios do Minecraft nao possuem campo de texto nativo. Depois que o jogador digita, ele volta automaticamente para o menu correto.

## Texturas

O projeto inclui texturas de referencia em pixel art para resource pack/GUI:

- `criacao_personagem_zombie_pixel_256.png`: versao recomendada para textura base.
- `criacao_personagem_zombie_pixel_1024.png`: mesma textura ampliada com nearest-neighbor, sem suavizacao.

As texturas ficam em `src/main/resources/assets/mpersonagem/textures/gui/`.

## Menu admin por clique

Ao usar `/mpersonagem editar <player>`, o staff pode abrir o menu admin do personagem ativo. Esse menu permite alterar por clique:

- idade: `-1` e `+1`
- altura: `-5 cm` e `+5 cm`
- talento: rotaciona entre os talentos configurados

## Permissoes

- `mpersonagem.use`
- `mpersonagem.view`
- `mpersonagem.edit`
- `mpersonagem.reload`
- `mpersonagem.reset`
- `mpersonagem.setaltura`
- `mpersonagem.pularano`
- `mpersonagem.admin`

## Placeholders

Com PlaceholderAPI instalado:

- `%mpersonagem_nome%`
- `%mpersonagem_sobrenome%`
- `%mpersonagem_nome_completo%`
- `%mpersonagem_idade%`
- `%mpersonagem_altura%`
- `%mpersonagem_altura_formatada%`
- `%mpersonagem_altura_metros%`
- `%mpersonagem_genero%`
- `%mpersonagem_descricao%`
- `%mpersonagem_tem_personagem%`

Para TAB, use principalmente `%mpersonagem_nome_completo%` ou `%mpersonagem_nome%` no formato do tablist. O plugin tambem atualiza `playerListName` diretamente quando `settings.update-tab-list-name` esta ativo.

## Configuracao

Arquivos principais:

- `config.yml`: limites, atributos, validacoes, menu e formatacao de nick/TAB.
- `messages.yml`: todas as mensagens editaveis.
- `players/<uuid>.yml`: dados salvos de cada jogador.

Os personagens podem ser criados, editados e apagados sem resetar o player por completo. A exclusao pelo menu tem confirmacao para evitar clique acidental.
