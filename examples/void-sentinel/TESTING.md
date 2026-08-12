# Testing Checklist

[English version](TESTING_EN.md)

Rode os reloads:

```text
/mm reload
/ml reload
/mmocore reload
```

Teste MythicMobs:

```text
/mm debug cast VoidSentinel_ShieldThrow
/mm debug cast VoidSentinel_VoidGuard
/mm debug cast VoidSentinel_SingularityChain
/mm debug cast VoidSentinel_WardensVerdict
/mm debug cast VoidSentinel_EventHorizon
```

Teste MythicLib:

```text
/ml debug cast SHIELD_THROW
/ml debug cast VOID_GUARD
/ml debug cast SINGULARITY_CHAIN
/ml debug cast WARDENS_VERDICT
/ml debug cast EVENT_HORIZON
```

O que verificar:

- Reload sem erro vermelho no console.
- Shield Throw aplica dano e slow.
- Singularity Chain puxa mobs.
- Warden's Verdict stuna por pouco tempo.
- Event Horizon nao fica forte demais para PvP.
- Void Guard nao permite invencibilidade permanente.

Se der erro:

1. Copie o erro exato do console.
2. Informe versoes de MythicMobs, MythicLib e MMOCore.
3. Ajuste primeiro nomes de mecanicas/targeters, depois balanceamento.
