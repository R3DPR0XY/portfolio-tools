# Void Sentinel Design

## Fantasia

Um guardiao cosmico que luta com quatro escudos orbitais. Ele vence controlando distancia, juntando inimigos e convertendo defesa em explosao.

Paleta principal:
- Preto: vacuo, fumaca, portal, reverse portal
- Vermelho: julgamento, dano, colapso, crimson particles
- Branco: escudos, cura, sentinela, flashes de protecao

## Rotacao principal

1. `Singularity Chain` para agrupar.
2. `Warden's Verdict` para stun curto.
3. `Shield Throw` no alvo prioritario.
4. `Void Guard` quando for focado.
5. `Event Horizon` quando tiver varios inimigos juntos.

## Upgrade futuro: sistema real de escudos

O MVP nao usa recurso real de quatro escudos para evitar quebrar em versoes diferentes dos plugins.

Versao premium sugerida:
- Criar uma variavel `vs_shields` de 0 a 4.
- Cada skill consome escudos.
- Um metaskill regenera 1 escudo a cada X segundos.
- Se `vs_shields` for 0, skills defensivas falham ou ficam mais fracas.

## VFX premium sugerido

- Quatro escudos orbitais com ModelEngine.
- `Shield Throw`: escudo fisico voando e voltando.
- `Singularity Chain`: esfera preta/roxa com aneis.
- `Void Guard`: escudos fechando em dome.
- `Event Horizon`: colapso visual com onda circular.

## Versao "melhor que Watcher"

O Watcher original e mais unico por ter shield management. O Void Sentinel ganha por:
- Mais stun real.
- Mais sustain direto.
- Mais dano de finalizacao.
- Rotacao clara de pull -> stun -> burst.
- Tema visual mais agressivo: preto/vermelho/branco.
- Facilidade de adaptar para PvP ou PvE.
