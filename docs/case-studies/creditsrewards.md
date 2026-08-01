# Estudo de Caso - CreditsRewards

## Resumo

CreditsRewards e um plugin Paper comercial para servidores Minecraft. O projeto combina economia propria, missoes configuraveis, recompensas, lojas via NPC, estoque, historico de transacoes e persistencia em SQLite ou MySQL.

## Problema

Servidores de RPG e roleplay frequentemente precisam de uma economia controlada que nao dependa apenas de dinheiro generico. Tambem precisam de missoes, lojas, recompensas e progresso persistente sem depender de varios plugins desconectados.

## Solucao

O plugin centraliza o fluxo de progresso:

- Jogadores completam missoes.
- Missoes geram creditos e recompensas.
- Creditos sao usados em lojas de NPC.
- Compras e saldos sao persistidos.
- Administradores controlam economia, estoque, missoes e validacao.

## Pontos tecnicos

- Plugin Paper com Java 21.
- Persistencia em SQLite ou MySQL.
- Configuracao por YAML.
- Integracao com Citizens para lojas via NPC.
- Suporte opcional a Nexo para itens customizados.
- Suporte opcional a PlaceholderAPI.
- Sistema administrativo de comandos.
- Historico de transacoes e logs.
- Estrutura preparada para servidor real.

## Decisao de publicacao

CreditsRewards foi tratado como projeto comercial. Por isso, o portfolio publica apenas a documentacao de produto e o estudo de caso. O codigo-fonte, builds e configuracoes completas devem permanecer privados.

## O que demonstra

- Desenvolvimento backend para servidor.
- Modelagem de economia e progresso.
- Persistencia de dados.
- Integracao entre plugins.
- Design de comandos administrativos.
- Pensamento de produto comercial.
- Separacao entre portfolio publico e propriedade intelectual privada.

## Caminho

[plugins/creditsrewards](../../plugins/creditsrewards)

