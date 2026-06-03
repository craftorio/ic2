package ic2.core.audio;

import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AudioConfigHandler {
  @SubscribeEvent
  public static void guiCreate(GuiScreenEvent.InitGuiEvent.Post event) {
    if (event.getGui() instanceof net.minecraft.client.gui.GuiScreenOptionsSounds) {
      final GuiScreen gui = event.getGui();
      int slot = 11;
      event.getButtonList().add(new GuiButton(50, gui.field_146294_l / 2 - 155 + slot % 2 * 160, gui.field_146295_m / 6 - 12 + 24 * (slot >> 1), 150, 20, "") {
            private final String categoryName = "IC2";
            
            private float volume = IC2.audioManager.getMasterVolume();
            
            private boolean pressed;
            
            protected int func_146114_a(boolean mouseOver) {
              return 0;
            }
            
            private String getVolumeDisplay() {
              return (this.volume == 0.0F) ? Localization.translate("options.off") : ((int)(this.volume * 100.0F) + "%");
            }
            
            private void updateVolume(int mouseX, int mouseY) {
              ((AudioManagerClient)IC2.audioManager).masterVolume = this.volume = Util.limit((mouseX - this.field_146128_h + 4) / (this.field_146120_f - 8), 0.0F, 1.0F);
              this.field_146126_j = "IC2: " + getVolumeDisplay();
              MainConfig.get().set("audio/volume", String.format("%.2f", new Object[] { Float.valueOf(this.volume) }));
              MainConfig.save();
            }
            
            protected void func_146119_b(Minecraft mc, int mouseX, int mouseY) {
              if (this.field_146125_m) {
                if (this.pressed)
                  updateVolume(mouseX, mouseY); 
                GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
                int sliderPos = (int)(this.volume * (this.field_146120_f - 8));
                func_73729_b(this.field_146128_h + sliderPos, this.field_146129_i, 0, 66, 4, 20);
                func_73729_b(this.field_146128_h + sliderPos + 4, this.field_146129_i, 196, 66, 4, 20);
              } 
            }
            
            public boolean func_146116_c(Minecraft mc, int mouseX, int mouseY) {
              if (super.func_146116_c(mc, mouseX, mouseY)) {
                updateVolume(mouseX, mouseY);
                this.pressed = true;
                return true;
              } 
              return false;
            }
            
            public void func_146113_a(SoundHandler handler) {}
            
            public void func_146118_a(int mouseX, int mouseY) {
              if (this.pressed)
                gui.field_146297_k.func_147118_V().func_147682_a((ISound)PositionedSoundRecord.func_184371_a(SoundEvents.field_187909_gi, 1.0F)); 
              this.pressed = false;
            }
          });
    } 
  }
}
