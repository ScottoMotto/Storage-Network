package mrriegel.storagenetwork.proxy;

import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.gui.GuiHandler;
import mrriegel.storagenetwork.helper.UtilTileEntity;
import mrriegel.storagenetwork.registry.PacketRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

  public void preInit(FMLPreInitializationEvent event) {
    ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
    PacketRegistry.init();
  }

  public void init(FMLInitializationEvent event) {
    NetworkRegistry.INSTANCE.registerGuiHandler(StorageNetwork.instance, new GuiHandler());
  }

  public void postInit(FMLPostInitializationEvent event) {
    UtilTileEntity.init();
  }
}
