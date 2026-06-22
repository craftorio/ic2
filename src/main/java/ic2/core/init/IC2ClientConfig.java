package ic2.core.init;

import net.minecraftforge.common.ForgeConfigSpec;

public class IC2ClientConfig
{
	public static final ForgeConfigSpec SPEC;

	public static final Audio audio;
	public static final Misc misc;

	static
	{
		ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

		audio = new Audio(b);
		misc = new Misc(b);

		SPEC = b.build();
	}

	public static class Audio
	{
		public final ForgeConfigSpec.BooleanValue enabled;
		public final ForgeConfigSpec.DoubleValue volume;
		public final ForgeConfigSpec.IntValue fadeDistance;
		public final ForgeConfigSpec.IntValue maxSourceCount;

		Audio(ForgeConfigSpec.Builder b)
		{
			b.push("audio");
			b.comment("Enable IC2's custom sound system.");
			enabled = b.define("enabled", true);
			b.comment("Volume of IC2's sounds, range from 0 (silent) ... 1 (100%).");
			volume = b.defineInRange("volume", 1.0, 0.0, 1.0);
			b.comment("The number of blocks the sounds attenuate over.");
			fadeDistance = b.defineInRange("fadeDistance", 16, 0, Integer.MAX_VALUE);
			b.comment("Maximum number of active audio sources, only change it if you know what you're doing.");
			maxSourceCount = b.defineInRange("maxSourceCount", 32, 0, Integer.MAX_VALUE);
			b.pop();
		}
	}

	public static class Misc
	{
		public final ForgeConfigSpec.BooleanValue hideSecretRecipes;
		public final ForgeConfigSpec.BooleanValue quantumSpeedOnSprint;

		Misc(ForgeConfigSpec.Builder b)
		{
			b.push("misc");
			b.comment("Enable hiding of secret recipes in CraftGuide/NEI.");
			hideSecretRecipes = b.define("hideSecretRecipes", true);
			b.comment("Enable activation of the quantum leggings' speed boost when sprinting instead of holding the boost key.");
			quantumSpeedOnSprint = b.define("quantumSpeedOnSprint", true);
			b.pop();
		}
	}
}
