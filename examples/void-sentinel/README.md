# Void Sentinel Pack

[English version](README_EN.md)

Classe tank/control inspirada no estilo Watcher's Shield, mas mais agressiva: escudos orbitais, puxao, stun, mitigacao, cura, dano em area e ultimate de colapso.

Tema visual: preto, vermelho e branco.

## Dependencias alvo

- MythicMobs 5.x
- MMOCore
- MythicLib

Opcional:
- MMOItems ou MythicCrucible para item/arma propria
- ItemsAdder, Nexo ou Oraxen para icones/modelos/VFX melhores

## Identidade da classe

Nome: Void Sentinel
Funcao: frontline control tank
Paleta:
- Preto: vazio, singularidade, defesa
- Vermelho: julgamento, dano, colapso
- Branco: escudo, cura, sentinela

## Instalacao

1. Copie `MythicMobs/Skills/void_sentinel_skills.yml` para:
   `plugins/MythicMobs/Skills/void_sentinel_skills.yml`

2. Copie `MMOCore/classes/void-sentinel.yml` para:
   `plugins/MMOCore/classes/void-sentinel.yml`

3. Copie `MythicLib/skill/void-sentinel.yml` para:
   `plugins/MythicLib/skill/void-sentinel.yml`

4. Opcional: copie `MMOItems/void-sentinel-items.yml` para sua pasta de itens do MMOItems/MythicCrucible, ajustando o caminho conforme seu setup.

5. Reinicie o servidor ou rode os reloads dos plugins:
   - `/mm reload`
   - `/ml reload`
   - `/mmocore reload`

6. Teste primeiro pelo MythicMobs/MythicLib:
   - `/mm debug cast VoidSentinel_ShieldThrow`
   - `/ml debug cast SHIELD_THROW`

O arquivo `MMOCore/skills/void-sentinel.yml` fica incluido como rascunho/compatibilidade para setups antigos, mas o fluxo recomendado pela documentacao atual e registrar as skills pelo MythicLib.

## Kit

- `Orbital Resonance`: passiva simples de sustain. Cura e da absorcao leve.
- `Shield Throw`: arremessa energia de escudo, causa dano, slow, puxa e marca alvo.
- `Void Guard`: skill defensiva. Da resistencia/absorcao e explode depois com knockback.
- `Singularity Chain`: mini buraco negro. Puxa, da slow e dano em ticks.
- `Warden's Verdict`: slam frontal/em area curta com stun.
- `Event Horizon`: ultimate. Puxa geral, stuna e causa burst.

## Binds sugeridos

- LMB: Shield Throw
- RMB: Void Guard
- Shift + LMB: Singularity Chain
- Shift + RMB: Warden's Verdict
- Shift duas vezes ou tecla ultimate: Event Horizon

## Balanceamento rapido

Para PvE:
- Aumente dano em `damage{a=...}`.
- Use stun entre 20 e 35 ticks.
- Use cooldowns menores no MMOCore.

Para PvP:
- Reduza `stun{d=...}` para 10-20 ticks.
- Reduza o raio de `@EIR{r=...}`.
- Aumente cooldown de `EVENT_HORIZON`.

## Observacoes

Este pack usa particulas vanilla para funcionar sem assets pagos. A parte visual deve ser trocada depois por modelos/VFX se voce quiser um visual premium.

Alguns nomes de opcoes podem variar por versao de MythicMobs/MMOCore. Se algum reload acusar erro, mande o log que eu ajusto em cima da sua versao exata.
