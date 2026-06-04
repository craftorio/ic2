// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.audio;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import ic2.core.init.MainConfig;
import ic2.core.util.Util;
import ic2.core.init.Localization;
import ic2.core.IC2;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraftforge.client.event.GuiScreenEvent;

public class AudioConfigHandler
{
    @SubscribeEvent
    public static void guiCreate(final GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiScreenOptionsSounds) {
            final GuiScreen gui = event.getGui();
            final int slot = 11;
            event.getButtonList().add(new GuiButton(50, gui.width / 2 - 155 + slot % 2 * 160, gui.height / 6 - 12 + 24 * (slot >> 1), 150, 20, "") {
                private final String categoryName = "IC2";
                private float volume = IC2.audioManager.getMasterVolume();
                private boolean pressed;
                
                {
                    this.displayString = "IC2: " + this.getVolumeDisplay();
                }
                
                protected int getHoverState(final boolean mouseOver) {
                    return 0;
                }
                
                private String getVolumeDisplay() {
                    return (this.volume == 0.0f) ? Localization.translate("options.off") : ((int)(this.volume * 100.0f) + "%");
                }
                
                private void updateVolume(final int mouseX, final int mouseY) {
                    final AudioManagerClient audioManagerClient = (AudioManagerClient)IC2.audioManager;
                    final float limit = Util.limit((mouseX - (this.x + 4)) / (float)(this.width - 8), 0.0f, 1.0f);
                    this.volume = limit;
                    audioManagerClient.masterVolume = limit;
                    this.displayString = "IC2: " + this.getVolumeDisplay();
                    MainConfig.get().set("audio/volume", String.format("%.2f", this.volume));
                    MainConfig.save();
                }
                
                protected void mouseDragged(final Minecraft mc, final int mouseX, final int mouseY) {
                    if (this.visible) {
                        if (this.pressed) {
                            this.updateVolume(mouseX, mouseY);
                        }
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        final int sliderPos = (int)(this.volume * (this.width - 8));
                        this.drawTexturedModalRect(this.x + sliderPos, this.y, 0, 66, 4, 20);
                        this.drawTexturedModalRect(this.x + sliderPos + 4, this.y, 196, 66, 4, 20);
                    }
                }
                
                public boolean mousePressed(final Minecraft mc, final int mouseX, final int mouseY) {
                    if (super.mousePressed(mc, mouseX, mouseY)) {
                        this.updateVolume(mouseX, mouseY);
                        return this.pressed = true;
                    }
                    return false;
                }
                
                public void playPressSound(final SoundHandler handler) {
                }
                
                public void mouseReleased(final int mouseX, final int mouseY) {
                    if (this.pressed) {
                        gui.mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    }
                    this.pressed = false;
                }
            });
        }
    }
}
