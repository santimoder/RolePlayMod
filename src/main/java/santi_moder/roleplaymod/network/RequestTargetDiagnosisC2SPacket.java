package santi_moder.roleplaymod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import santi_moder.roleplaymod.server.data.PlayerDataProvider;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

public final class RequestTargetDiagnosisC2SPacket {

    private static final double RANGE = 5.0D;

    public static void encode(RequestTargetDiagnosisC2SPacket packet, FriendlyByteBuf buf) {
    }

    public static RequestTargetDiagnosisC2SPacket decode(FriendlyByteBuf buf) {
        return new RequestTargetDiagnosisC2SPacket();
    }

    public static void handle(RequestTargetDiagnosisC2SPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer healer = ctx.getSender();
            if (healer == null || healer.isDeadOrDying()) return;

            Optional<ServerPlayer> target = findLookedAtPlayer(healer);

            if (target.isPresent()) {
                ServerPlayer patient = target.get();

                patient.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                    ModNetwork.STATS_CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> healer),
                            new OpenTargetDiagnosisS2CPacket(
                                    patient.getUUID(),
                                    patient.getName().getString(),
                                    data
                            )
                    );
                });

                return;
            }

            healer.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                ModNetwork.STATS_CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> healer),
                        new OpenSelfDiagnosisS2CPacket(data)
                );
            });
        });

        ctx.setPacketHandled(true);
    }

    private static Optional<ServerPlayer> findLookedAtPlayer(ServerPlayer healer) {
        Vec3 eye = healer.getEyePosition();
        Vec3 look = healer.getLookAngle().normalize();

        AABB searchBox = healer.getBoundingBox().inflate(RANGE);

        return healer.level().getEntitiesOfClass(ServerPlayer.class, searchBox, target ->
                        target != healer
                                && !target.isDeadOrDying()
                                && isValidMedicalTarget(target)
                                && isSelectableTarget(healer, target, eye, look)
                )
                .stream()
                .min(Comparator.comparingDouble(target -> scoreTarget(healer, target, eye, look)));
    }

    private static boolean isValidMedicalTarget(ServerPlayer target) {
        return target.getCapability(PlayerDataProvider.PLAYER_DATA)
                .map(data -> data.isInconsciente())
                .orElse(false);
    }

    private static boolean isSelectableTarget(ServerPlayer healer, ServerPlayer target, Vec3 eye, Vec3 look) {
        double distanceSqr = healer.distanceToSqr(target);

        if (distanceSqr > RANGE * RANGE) return false;

        // Si está muy cerca, permitir aunque no estés apuntando perfecto.
        if (distanceSqr <= 4.0D) return true;

        AABB box = target.getBoundingBox().inflate(1.8D);

        Vec3 center = box.getCenter();
        Vec3 toTarget = center.subtract(eye);

        if (toTarget.lengthSqr() <= 0.001D) return false;

        Vec3 direction = toTarget.normalize();
        double dot = look.dot(direction);

        // Debe estar más o menos delante.
        if (dot < 0.45D) return false;

        Vec3 end = eye.add(look.scale(RANGE));

        // Hitbox expandida para jugadores tirados.
        return box.clip(eye, end).isPresent() || dot >= 0.70D;
    }

    private static double scoreTarget(ServerPlayer healer, ServerPlayer target, Vec3 eye, Vec3 look) {
        Vec3 center = target.getBoundingBox().getCenter();
        Vec3 direction = center.subtract(eye).normalize();

        double dot = look.dot(direction);
        double distance = healer.distanceToSqr(target);

        return distance - (dot * 10.0D);
    }
}