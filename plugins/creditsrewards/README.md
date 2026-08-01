# CreditsRewards

Plugin Paper comercial para servidores Minecraft com sistema de creditos, missoes, recompensas, lojas via NPC e persistencia em banco de dados.

## Status

Projeto fechado/comercial. Este diretorio e uma vitrine publica do produto; o codigo-fonte, builds e configuracoes completas nao estao incluidos neste repositorio.

## Visao geral

CreditsRewards foi criado para servidores que precisam de uma economia propria baseada em progresso, missoes e recompensas controladas. O sistema centraliza creditos, lojas, estoque, missoes, feedback visual e integracoes opcionais em um plugin unico.

## Recursos principais

- Sistema de creditos proprio.
- Missoes configuraveis por YAML.
- Recompensas por progresso.
- Lojas abertas por NPC Citizens.
- Estoque global e limite por jogador.
- Historico de transacoes.
- Persistencia em SQLite ou MySQL.
- Feedback por sons, ActionBar e BossBar.
- Suporte opcional a Nexo.
- Suporte opcional a PlaceholderAPI.

## Tipos de missoes

- `KILL`
- `BUILD`
- `MINE`
- `CRAFT`
- `FISH`
- `DELIVER`

## Exemplos de comandos

Jogador:

```text
/creditos
/creditos top
/missoes
```

Administracao:

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

Este plugin nao e distribuido como software livre neste repositorio.

O uso, edicao, redistribuicao, revenda, copia do codigo-fonte, engenharia reversa e criacao de versoes derivadas dependem de autorizacao/licenca comercial separada.

## Por que o codigo nao esta publico

CreditsRewards e tratado como produto comercial. Por isso, este repositorio mostra a capacidade tecnica, o escopo e a documentacao do projeto, mas preserva o codigo-fonte e os builds para distribuicao privada/licenciada.

## Distribuicao

Para venda ou entrega privada, o formato recomendado e:

- `.jar` compilado.
- Termos de licenca comercial.
- Changelog por versao.
- Guia de instalacao.
- Guia de configuracao.
- Canal de suporte definido.

## Observacao

Ofuscacao pode dificultar copia direta, mas nao substitui licenca, contrato, controle de distribuicao e suporte profissional.
