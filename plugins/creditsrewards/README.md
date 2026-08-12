# CreditsRewards

[English version](README_EN.md)

Plugin Paper comercial para servidores Minecraft com sistema de créditos, missões, recompensas, lojas via NPC e persistência em banco de dados.

## Status

Projeto fechado/comercial. Este diretório é uma vitrine pública do produto; o código-fonte, builds e configurações completas não estão incluídos neste repositório.

## Visão Geral

CreditsRewards foi criado para servidores que precisam de uma economia própria baseada em progresso, missões e recompensas controladas. O sistema centraliza créditos, lojas, estoque, missões, feedback visual e integrações opcionais em um plugin único.

## Recursos Principais

- Sistema de créditos próprio.
- Missões configuráveis por YAML.
- Recompensas por progresso.
- Lojas abertas por NPC Citizens.
- Estoque global e limite por jogador.
- Histórico de transações.
- Persistência em SQLite ou MySQL.
- Feedback por sons, ActionBar e BossBar.
- Suporte opcional a Nexo.
- Suporte opcional a PlaceholderAPI.

## Tipos De Missões

- `KILL`
- `BUILD`
- `MINE`
- `CRAFT`
- `FISH`
- `DELIVER`

## Exemplos De Comandos

Jogador:

```text
/creditos
/creditos top
/missoes
```

Administração:

```text
/creditos admin give <jogador> <quantia>
/creditos admin take <jogador> <quantia>
/creditos admin set <jogador> <quantia>
/recompensas reload
/recompensas lojas
/recompensas abrir <loja>
/soulsociety status
/soulsociety validate
```

## Requisitos

- Paper 1.21.11
- Java 21
- Citizens
- SQLite ou MySQL

Opcionais:

- Nexo
- PlaceholderAPI
- TAB

## Licenciamento

Este plugin não é distribuído como software livre neste repositório.

O uso, edição, redistribuição, revenda, cópia do código-fonte, engenharia reversa e criação de versões derivadas dependem de autorização/licença comercial separada.

## Por Que O Código Não Está Público

CreditsRewards é tratado como produto comercial. Por isso, este repositório mostra a capacidade técnica, o escopo e a documentação do projeto, mas preserva o código-fonte e os builds para distribuição privada/licenciada.

## Distribuição

Para venda ou entrega privada, o formato recomendado é:

- `.jar` compilado.
- Termos de licença comercial.
- Changelog por versão.
- Guia de instalação.
- Guia de configuração.
- Canal de suporte definido.

## Observação

Ofuscação pode dificultar cópia direta, mas não substitui licença, contrato, controle de distribuição e suporte profissional.
