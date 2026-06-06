package ic2.core.block;

import ic2.api.tile.IRotorProvider;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class KineticGeneratorRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T>
{
	private static final Map<Integer, ModelBase> rotorModels = new HashMap<>();

	protected void renderBlockRotor(IRotorProvider windGen, World world, BlockPos pos)
	{
		int diameter = windGen.getRotorDiameter();
		if (diameter != 0)
		{
			float angle = windGen.getAngle();
			ResourceLocation rotorRL = windGen.getRotorRenderTexture();
			ModelBase model = rotorModels.get(diameter);
			if (model == null)
			{
				model = new KineticGeneratorRotor(diameter);
				rotorModels.put(diameter, model);
			}

			EnumFacing facing = windGen.getFacing();
			pos = pos.offset(facing);
			int light = world.getCombinedLight(pos, 0);
			int blockLight = light % 65536;
			int skyLight = light / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
			switch (facing)
			{
				case NORTH:
					GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
					break;
				case EAST:
					GL11.glRotatef(-180.0F, 0.0F, 1.0F, 0.0F);
					break;
				case SOUTH:
					GL11.glRotatef(-270.0F, 0.0F, 1.0F, 0.0F);
					break;
				case UP:
					GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
			}

			GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
			GlStateManager.translate(-0.2F, 0.0F, 0.0F);
			this.bindTexture(rotorRL);
			model.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
			GlStateManager.popMatrix();
		}
	}

	public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		if (te instanceof IRotorProvider)
		{
			this.renderBlockRotor((IRotorProvider) te, te.getWorld(), te.getPos());
		}

		GL11.glPopMatrix();
	}
}
