package mrriegel.storagenetwork.network;

import io.netty.buffer.ByteBuf;
import mrriegel.storagenetwork.cable.ContainerCable;
import mrriegel.storagenetwork.cable.TileCable;
import mrriegel.storagenetwork.data.StackWrapper;
import mrriegel.storagenetwork.helper.UtilTileEntity;
import mrriegel.storagenetwork.tile.AbstractFilterTile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.IItemHandler;

public class CableDataMessage implements IMessage, IMessageHandler<CableDataMessage, IMessage> {

  public static final int TOGGLE_WAY = 6;
  public static final int IMPORT_FILTER = 5;
  public static final int TOGGLE_WHITELIST = 3;
  public static final int PRIORITY_UP = 1;
  public static final int PRIORITY_DOWN = 0;
  public static final int TOGGLE_MODE = 4;
  int id;
  BlockPos pos;

  public CableDataMessage() {}

  public CableDataMessage(int id, BlockPos pos) {
    this.id = id;
    this.pos = pos;
  }

  @Override
  public IMessage onMessage(final CableDataMessage message, final MessageContext ctx) {
    IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
    mainThread.addScheduledTask(new Runnable() {

      @Override
      public void run() {
        TileEntity t = ctx.getServerHandler().player.world.getTileEntity(message.pos);
        if (t instanceof AbstractFilterTile) {
          AbstractFilterTile tile = (AbstractFilterTile) t;
          switch (message.id) {
            case PRIORITY_DOWN:
              tile.setPriority(tile.getPriority() - 1);
            break;
            case PRIORITY_UP:
              tile.setPriority(tile.getPriority() + 1);
            break;
            case TOGGLE_WHITELIST:
              tile.setWhite(!tile.isWhitelist());
            break;
            case TOGGLE_MODE://4
              if (tile instanceof TileCable) {
                ((TileCable) tile).setMode(!((TileCable) tile).isMode());
              }
            break;
            case IMPORT_FILTER:
              if (tile.getInventory() != null) {
                IItemHandler inv = tile.getInventory();
                int index = 0;
                tile.setWhite(true);
                int size = 9 * 2;
                for (int i = 0; i < size; i++) {
                  tile.getFilter().put(i, null);
                }
                for (int i = 0; i < inv.getSlots() && index < size; i++) {
                  ItemStack s = inv.getStackInSlot(i);
                  if (s == null || s.isEmpty()) {
                    continue;
                  }
                  else {
                    if (!new ContainerCable(tile, ctx.getServerHandler().player.inventory).isInFilter(new StackWrapper(s, 1))) {
                      tile.getFilter().put(index, new StackWrapper(s, 1));
                      index++;
                    }
                  }
                }
              }
            break;
            case TOGGLE_WAY:
              tile.setWay(tile.getWay().next());
            break;
          }
          tile.markDirty();
        }
        UtilTileEntity.updateTile(t.getWorld(), t.getPos());
      }
    });
    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.pos = BlockPos.fromLong(buf.readLong());
    this.id = buf.readInt();
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeLong(this.pos.toLong());
    buf.writeInt(this.id);
  }
}
