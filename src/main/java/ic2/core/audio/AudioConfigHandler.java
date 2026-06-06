package ic2.core.audio;

import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AudioConfigHandler
{
	@SubscribeEvent
	public static void guiCreate(GuiScreenEvent.InitGuiEvent.Post event)
	{
		if (event.getGui() instanceof GuiScreenOptionsSounds)
		{
			final GuiScreen gui = event.getGui();
			int slot = 11;
			event.getButtonList()
				.add(
					new GuiButton(50, gui.width / 2 - 155 + slot % 2 * 160, gui.height / 6 - 12 + 24 * (slot >> 1), 150, 20, "")
					{
						private final String categoryName = "IC2";
						private float volume = IC2.audioManager.getMasterVolume();
						private boolean pressed;

						{
							this.displayString = "IC2: " + this.getVolumeDisplay();
						}

						protected int getHoverState(boolean mouseOver)
						{
							return 0;
						}

						private String getVolumeDisplay()
						{
							return this.volume == 0.0F ? Localization.translate("options.off") : (int) (this.volume * 100.0F) + "%";
						}

						private void updateVolume(int mouseX, int mouseY)
						{
							((AudioManagerClient) IC2.audioManager).masterVolume = this.volume = Util.limit(
								(float) (mouseX - (this.x + 4)) / (this.width - 8), 0.0F, 1.0F
							);
							this.displayString = "IC2: " + this.getVolumeDisplay();
							MainConfig.get().set("audio/volume", String.format("%.2f", this.volume));
							MainConfig.save();
						}

						protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
						{
							if (this.visible)
							{
								if (this.pressed)
								{
									this.updateVolume(mouseX, mouseY);
								}

								GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
								int sliderPos = (int) (this.volume * (this.width - 8));
								this.drawTexturedModalRect(this.x + sliderPos, this.y, 0, 66, 4, 20);
								this.drawTexturedModalRect(this.x + sliderPos + 4, this.y, 196, 66, 4, 20);
							}
						}

						public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
						{
							if (super.mousePressed(mc, mouseX, mouseY))
							{
								this.updateVolume(mouseX, mouseY);
								this.pressed = true;
								return true;
							} else
							{
								return false;
							}
						}

						public void mouseReleased(int mouseX, int mouseY)
						{
							if (this.pressed)
							{
								gui.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
							}

							this.pressed = false;
						}
					}
				);
		}
	}
}
