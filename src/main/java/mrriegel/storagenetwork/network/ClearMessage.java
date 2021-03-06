package mrriegel.storagenetwork.network;

import java.util.List;
import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.gui.ContainerNetworkBase;
import mrriegel.storagenetwork.master.TileMaster;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.ItemHandlerHelper;

public class ClearMessage implements IMessage, IMessageHandler<ClearMessage, IMessage> {

  @Override
  public IMessage onMessage(final ClearMessage message, final MessageContext ctx) {
    IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        Container c = ctx.getServerHandler().player.openContainer;
        if (c instanceof ContainerNetworkBase) {
          ContainerNetworkBase ctr = (ContainerNetworkBase) c;
          TileMaster m = ctr.getTileMaster();
          InventoryCrafting craftMatrix = ctr.getCraftMatrix();
          for (int i = 0; i < 9; i++) {
            if (m == null) {
              break;
            }
            ItemStack s = craftMatrix.getStackInSlot(i);
            if (s == null || s.isEmpty()) {
              continue;
            }
            int num = s.getCount();
            int rest = m.insertStack(s.copy(), null, false);
            if (num == rest) {
              continue;
            }
            if (rest == 0)
              craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
            else
              craftMatrix.setInventorySlotContents(i, ItemHandlerHelper.copyStackWithSize(s, rest));
          }
          List<StackWrapper> list = m.getStacks();
          PacketRegistry.INSTANCE.sendTo(new StacksMessage(list, m.getCraftableStacks(list)), ctx.getServerHandler().player);
          ctr.detectAndSendChanges();
        }
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {}

  @Override
  public void toBytes(ByteBuf buf) {}
}
