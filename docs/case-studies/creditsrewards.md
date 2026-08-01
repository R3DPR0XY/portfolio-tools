# Estudo De Caso - CreditsRewards

## Resumo

CreditsRewards é um plugin Paper comercial para servidores Minecraft. O projeto combina economia própria, missões configuráveis, recompensas, lojas via NPC, estoque, histórico de transações e persistência em SQLite ou MySQL.

## Problema

Servidores de RPG e roleplay frequentemente precisam de uma economia controlada que não dependa apenas de dinheiro genérico. Também precisam de missões, lojas, recompensas e progresso persistente sem depender de vários plugins desconectados.

## Solução

O plugin centraliza o fluxo de progresso:

- Jogadores completam missões.
- Missões geram créditos e recompensas.
- Créditos são usados em lojas de NPC.
- Compras e saldos são persistidos.
- Administradores controlam economia, estoque, missões e validação.

## Pontos Técnicos

- Plugin Paper com Java 21.
- Persistência em SQLite ou MySQL.
- Configuração por YAML.
- Integração com Citizens para lojas via NPC.
- Suporte opcional a Nexo para itens customizados.
- Suporte opcional a PlaceholderAPI.
- Sistema administrativo de comandos.
- Histórico de transações e logs.
- Estrutura preparada para servidor real.

## Decisão De Publicação

CreditsRewards foi tratado como projeto comercial. Por isso, o portfólio publica apenas a documentação de produto e o estudo de caso. O código-fonte, builds e configurações completas devem permanecer privados.

## O Que Demonstra

- Desenvolvimento backend para servidor.
- Modelagem de economia e progresso.
- Persistência de dados.
- Integração entre plugins.
- Design de comandos administrativos.
- Pensamento de produto comercial.
- Separação entre portfólio público e propriedade intelectual privada.

## Caminho

[plugins/creditsrewards](../../plugins/creditsrewards)
