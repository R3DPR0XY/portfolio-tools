# R3DPR0XY Portfolio Tools

[English version](README_EN.md)

Portfólio técnico com mods, plugins, scripts, automações e experimentos criados para resolver problemas reais, acelerar fluxos de trabalho e demonstrar capacidade prática de desenvolvimento.

## Sobre

Sou o **R3DPR0XY**, criador focado em ferramentas práticas, automações, mods, plugins e soluções técnicas que transformam ideias em projetos funcionais. Este portfólio reúne trabalhos organizados para mostrar raciocínio técnico, cuidado com documentação e atenção à utilidade real de cada entrega.

## Visão geral

Este repositório foi organizado para funcionar como vitrine pública: cada item deve explicar claramente o problema que resolve, como usar, quais tecnologias utiliza e qual é o estado atual do projeto.

## Projetos publicados

| Projeto | Tipo | Descrição |
| --- | --- | --- |
| [Kagerov](mods/kagerov) | Mod Fabric | Mod Fabric com sistemas client-side, interface, recursos visuais e estrutura própria para Minecraft. |
| [LootPanel](mods/lootpanel) | Mod Fabric | Painel visual para containers, somando itens iguais e exibindo loot de forma mais clara. |
| [Betterchat](mods/betterchat) | Mod Fabric | Mod de chat para Minecraft com interface e customizações client-side. |
| [Saturation Mod](mods/saturationmod) | Mod Fabric | Mod experimental voltado a ajustes de saturação e feedback visual. |
| [MPersonagem](plugins/mpersonagem) | Plugin Paper | Sistema de personagens para roleplay com menus, atributos, dados persistidos e integrações opcionais. |
| [CreditsRewards](plugins/creditsrewards) | Plugin Paper comercial | Sistema fechado de creditos, missoes, recompensas, lojas via NPC e persistencia SQLite/MySQL. |
| [Void Sentinel](examples/void-sentinel) | Configuração de servidor | Pacote de design, balanceamento e configurações para conteúdo customizado de servidor. |

## Estrutura

| Pasta | Conteúdo |
| --- | --- |
| `mods/` | Mods, customizações e ajustes para ferramentas, jogos ou ambientes. |
| `plugins/` | Plugins, extensões e integrações reutilizáveis. |
| `scripts/` | Scripts utilitários para automação, manutenção e produtividade. |
| `automations/` | Fluxos automatizados, rotinas recorrentes e processos documentados. |
| `docs/` | Guias, notas técnicas, decisões e documentação complementar. |
| `examples/` | Exemplos pequenos, dados fictícios e demonstrações de uso. |

## Padrão de cada projeto

Cada item publicado deve ter um `README.md` próprio com:

- **Resumo:** o que o projeto faz em poucas linhas.
- **Problema resolvido:** por que ele existe.
- **Tecnologias:** linguagens, frameworks, APIs ou ferramentas usadas.
- **Como executar:** comandos e requisitos mínimos.
- **Exemplos:** entrada, saída ou caso de uso.
- **Status:** experimental, em desenvolvimento, estável ou legado.
- **Segurança:** observações sobre dados, permissões e variáveis de ambiente.

## Qualidade antes de publicar

Antes de mover qualquer arquivo para este repositório:

- Remover tokens, senhas, cookies, chaves de API e arquivos `.env`.
- Substituir caminhos locais por exemplos genéricos.
- Trocar dados pessoais ou privados por dados fictícios.
- Remover arquivos gerados, temporários ou pesados.
- Testar o comando principal do projeto.
- Atualizar o `README.md` do projeto com instruções reais.

## Destaques

- Mods Fabric para Minecraft.
- Plugins Paper para servidores.
- Configurações e exemplos de sistemas customizados.
- Documentação focada em instalação, uso e manutenção.
- Organização pensada para portfólio público.
- **R3DPR0XY Forge Maps:** formato autoral para transformar projetos técnicos em vitrines visuais.

## R3DPR0XY Forge Maps

Forge Maps é um formato próprio para explicar projetos técnicos com identidade visual R3DPR0XY. Em vez de diagramas genéricos, os mapas usam quatro estruturas:

| Mapa | Uso |
| --- | --- |
| Release Rail | Pipelines, builds, empacotamento e releases. |
| Runtime Core | Plugins Paper e sistemas de servidor em execução. |
| Mod Circuit | Mods Fabric, entrypoints, mixins, telas, assets e JAR final. |
| Showcase Board | Visão pública de projeto para README, portfólio e release notes. |

Veja os exemplos em [docs/forge-maps](docs/forge-maps).

## Curriculo e estudos de caso

- [Curriculo tecnico](docs/resume.md)
- [Estudo de caso: Kagerov](docs/case-studies/kagerov.md)
- [Estudo de caso: LootPanel](docs/case-studies/lootpanel.md)
- [Estudo de caso: Betterchat](docs/case-studies/betterchat.md)
- [Estudo de caso: MPersonagem](docs/case-studies/mpersonagem.md)
- [Estudo de caso: CreditsRewards](docs/case-studies/creditsrewards.md)
- [Estudo de caso: Void Sentinel](docs/case-studies/void-sentinel.md)
- [Postagem para LinkedIn/GitHub](docs/social-post.md)

## Perfil técnico

- Desenvolvimento de scripts e automações.
- Organização de projetos para uso público.
- Criação de ferramentas práticas e reutilizáveis.
- Documentação clara para instalação, execução e manutenção.
- Cuidados com segurança antes da publicação.

## Licença

Distribuído sob a licença MIT, exceto projetos ou pastas com licença própria indicada. Veja [LICENSE](LICENSE).
