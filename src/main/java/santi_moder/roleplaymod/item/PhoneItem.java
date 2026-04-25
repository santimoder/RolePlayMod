package santi_moder.roleplaymod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import santi_moder.roleplaymod.client.phone.ClientPhoneOpener;
import santi_moder.roleplaymod.common.phone.PhoneData;

public class PhoneItem extends Item {

    public PhoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        PhoneData.initializeIfMissing(stack);

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPhoneOpener.open(hand));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}