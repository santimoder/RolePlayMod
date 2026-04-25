package santi_moder.roleplaymod.common.inventory.logic;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GroundItemLogic {

    public static List<ItemEntity> scanGroundItems(Player player) {
        List<ItemEntity> result = new ArrayList<>();

        if (player == null || player.level() == null) return result;

        List<ItemEntity> items = player.level().getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(2.0),
                entity -> entity != null && entity.isAlive() && !entity.getItem().isEmpty()
        );

        items.sort((a, b) -> Double.compare(
                a.distanceToSqr(player),
                b.distanceToSqr(player)
        ));

        result.addAll(items);
        return result;
    }
}