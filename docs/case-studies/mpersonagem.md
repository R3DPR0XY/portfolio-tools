# Estudo de Caso - MPersonagem

## Resumo

MPersonagem e um plugin Paper para servidores de roleplay. Ele permite criar, editar e visualizar personagens com nome, sobrenome, idade, altura, genero, descricao, menus e permissoes.

## Problema

Servidores de roleplay precisam de identidade persistente para jogadores, mas muitas solucoes dependem de processos manuais, comandos soltos ou configuracoes dificeis de manter.

## Solucao

O plugin centraliza criacao e edicao de personagens em comandos e menus. Os dados sao persistidos por jogador, as mensagens ficam configuraveis e a equipe do servidor tem comandos administrativos para consulta e manutencao.

## Pontos tecnicos

- Plugin Paper com Java 21.
- Comandos de jogador e administracao.
- Menus interativos com inventarios.
- Persistencia em arquivos por UUID.
- Sistema de permissoes.
- Configuracao por `config.yml` e `messages.yml`.
- Hooks opcionais para PlaceholderAPI/TAB.

## O que demonstra

- Desenvolvimento backend para servidor.
- Organizacao de regras de negocio.
- Persistencia e configuracao editavel.
- Cuidado com fluxo de usuario e administracao.
- Projeto mais completo, com varias camadas de funcionalidade.

## Caminho

[plugins/mpersonagem](../../plugins/mpersonagem)

