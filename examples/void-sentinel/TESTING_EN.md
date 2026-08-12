# Testing Checklist

[Versao em portugues](TESTING.md)

Run reloads:

```text
/mm reload
/ml reload
/mmocore reload
```

Test MythicMobs:

```text
/mm debug cast VoidSentinel_ShieldThrow
/mm debug cast VoidSentinel_VoidGuard
/mm debug cast VoidSentinel_SingularityChain
/mm debug cast VoidSentinel_WardensVerdict
/mm debug cast VoidSentinel_EventHorizon
```

Test MythicLib:

```text
/ml debug cast SHIELD_THROW
/ml debug cast VOID_GUARD
/ml debug cast SINGULARITY_CHAIN
/ml debug cast WARDENS_VERDICT
/ml debug cast EVENT_HORIZON
```

What to verify:

- Reloads do not show red errors in console.
- Shield Throw applies damage and slow.
- Singularity Chain pulls mobs.
- Warden's Verdict stuns for a short time.
- Event Horizon is not too strong for PvP.
- Void Guard does not allow permanent invincibility.

If there is an error:

1. Copy the exact console error.
2. Report MythicMobs, MythicLib, and MMOCore versions.
3. Adjust mechanic/targeter names first, then balance values.
