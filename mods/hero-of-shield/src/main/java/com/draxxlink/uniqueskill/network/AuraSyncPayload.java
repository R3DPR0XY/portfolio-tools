package com.draxxlink.uniqueskill.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AuraSyncPayload(boolean enabled, int colorRgb, int density) implements CustomPayload {
    public static final CustomPayload.Id<AuraSyncPayload> ID = new CustomPayload.Id<>(Identifier.of("unique_skill", "aura_sync"));
    public static final PacketCodec<RegistryByteBuf, AuraSyncPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        AuraSyncPayload::enabled,
        PacketCodecs.INTEGER,
        AuraSyncPayload::colorRgb,
        PacketCodecs.INTEGER,
        AuraSyncPayload::density,
        AuraSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
