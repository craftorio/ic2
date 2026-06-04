// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import ic2.core.block.state.IIdProvider;
import ic2.core.util.StackUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.ref.ItemName;

public class ItemBooze extends ItemIC2
{
    public String[] solidRatio;
    public String[] hopsRatio;
    public String[] timeRatioNames;
    public int[] baseDuration;
    public float[] baseIntensity;
    public static float rumStackability;
    public static int rumDuration;
    
    public ItemBooze() {
        super(ItemName.booze_mug);
        this.solidRatio = new String[] { "Watery ", "Clear ", "Lite ", "", "Strong ", "Thick ", "Stodge ", "X" };
        this.hopsRatio = new String[] { "Soup ", "Alcfree ", "White ", "", "Dark ", "Full ", "Black ", "X" };
        this.timeRatioNames = new String[] { "Brew", "Youngster", "Beer", "Ale", "Dragonblood", "Black Stuff" };
        this.baseDuration = new int[] { 300, 600, 900, 1200, 1600, 2000, 2400 };
        this.baseIntensity = new float[] { 0.4f, 0.75f, 1.0f, 1.5f, 2.0f };
        this.setMaxStackSize(1);
        this.setCreativeTab((CreativeTabs)null);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final ItemName name) {
        ModelLoader.setCustomMeshDefinition((Item)this, (ItemMeshDefinition)new ItemMeshDefinition() {
            public ModelResourceLocation getModelLocation(final ItemStack stack) {
                final int meta = stack.getMetadata();
                final int type = ItemBooze.getTypeOfValue(meta);
                BoozeMugType mugType;
                if (type == 1) {
                    final int timeRatio = Math.min(ItemBooze.getTimeRatioOfBeerValue(meta), ItemBooze.this.timeRatioNames.length - 1);
                    mugType = BoozeMugType.values[timeRatio];
                }
                else {
                    if (type != 2) {
                        return null;
                    }
                    mugType = BoozeMugType.rum;
                }
                return ItemIC2.getModelLocation(name, mugType.getName());
            }
        });
        for (final BoozeMugType type : BoozeMugType.values) {
            ModelBakery.registerItemVariants((Item)this, new ResourceLocation[] { (ResourceLocation)ItemIC2.getModelLocation(name, type.getName()) });
        }
    }
    
    @Override
    public String getItemStackDisplayName(final ItemStack itemstack) {
        final int meta = itemstack.getItemDamage();
        final int type = getTypeOfValue(meta);
        if (type == 1) {
            final int timeRatio = Math.min(getTimeRatioOfBeerValue(meta), this.timeRatioNames.length - 1);
            if (timeRatio == this.timeRatioNames.length - 1) {
                return this.timeRatioNames[timeRatio];
            }
            return this.solidRatio[getSolidRatioOfBeerValue(meta)] + this.hopsRatio[getHopsRatioOfBeerValue(meta)] + this.timeRatioNames[timeRatio];
        }
        else {
            if (type == 2) {
                return "Rum";
            }
            return "Zero";
        }
    }
    
    public ItemStack onItemUseFinish(final ItemStack stack, final World world, final EntityLivingBase living) {
        final int meta = stack.getItemDamage();
        final int type = getTypeOfValue(meta);
        if (type == 0) {
            return ItemName.mug.getItemStack(ItemMug.MugType.empty);
        }
        if (type == 1) {
            if (getTimeRatioOfBeerValue(meta) == 5) {
                return this.drinkBlackStuff(living);
            }
            final int solidRatio = getSolidRatioOfBeerValue(meta);
            final int alc = getHopsRatioOfBeerValue(meta);
            final int duration = this.baseDuration[solidRatio];
            final float intensity = this.baseIntensity[getTimeRatioOfBeerValue(meta)];
            if (living instanceof EntityPlayer) {
                ((EntityPlayer)living).getFoodStats().addStats(6 - alc, solidRatio * 0.15f);
            }
            final int max = (int)(intensity * (alc * 0.5f));
            final PotionEffect slow = living.getActivePotionEffect(MobEffects.MINING_FATIGUE);
            int level = -1;
            if (slow != null) {
                level = slow.getAmplifier();
            }
            this.amplifyEffect(living, MobEffects.MINING_FATIGUE, max, intensity, duration);
            if (level > -1) {
                this.amplifyEffect(living, MobEffects.STRENGTH, max, intensity, duration);
                if (level > 0) {
                    this.amplifyEffect(living, MobEffects.SLOWNESS, max / 2, intensity, duration);
                    if (level > 1) {
                        this.amplifyEffect(living, MobEffects.RESISTANCE, max - 1, intensity, duration);
                        if (level > 2) {
                            this.amplifyEffect(living, MobEffects.NAUSEA, 0, intensity, duration);
                            if (level > 3) {
                                living.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, living.getEntityWorld().rand.nextInt(3)));
                            }
                        }
                    }
                }
            }
        }
        if (type == 2) {
            if (getProgressOfRumValue(meta) < 100) {
                this.drinkBlackStuff(living);
            }
            else {
                this.amplifyEffect(living, MobEffects.FIRE_RESISTANCE, 0, ItemBooze.rumStackability, ItemBooze.rumDuration);
                final PotionEffect def = living.getActivePotionEffect(MobEffects.RESISTANCE);
                int level2 = -1;
                if (def != null) {
                    level2 = def.getAmplifier();
                }
                this.amplifyEffect(living, MobEffects.RESISTANCE, 2, ItemBooze.rumStackability, ItemBooze.rumDuration);
                if (level2 >= 0) {
                    this.amplifyEffect(living, MobEffects.BLINDNESS, 0, ItemBooze.rumStackability, ItemBooze.rumDuration);
                }
                if (level2 >= 1) {
                    this.amplifyEffect(living, MobEffects.NAUSEA, 0, ItemBooze.rumStackability, ItemBooze.rumDuration);
                }
            }
        }
        return ItemName.mug.getItemStack(ItemMug.MugType.empty);
    }
    
    public void amplifyEffect(final EntityLivingBase living, final Potion potion, final int max, final float intensity, int duration) {
        final PotionEffect eff = living.getActivePotionEffect(potion);
        if (eff == null) {
            living.addPotionEffect(new PotionEffect(potion, duration, 0));
        }
        else {
            int currentDuration = eff.getDuration();
            int maxnewdur = (int)(duration * (1.0f + intensity * 2.0f) - currentDuration) / 2;
            if (maxnewdur < 0) {
                maxnewdur = 0;
            }
            if (maxnewdur < duration) {
                duration = maxnewdur;
            }
            currentDuration += duration;
            int newamp = eff.getAmplifier();
            if (newamp < max) {
                ++newamp;
            }
            living.addPotionEffect(new PotionEffect(potion, currentDuration, newamp));
        }
    }
    
    public ItemStack drinkBlackStuff(final EntityLivingBase living) {
        switch (living.getEntityWorld().rand.nextInt(6)) {
            case 1: {
                living.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 1200, 0));
                break;
            }
            case 2: {
                living.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 2400, 0));
                break;
            }
            case 3: {
                living.addPotionEffect(new PotionEffect(MobEffects.POISON, 2400, 0));
                break;
            }
            case 4: {
                living.addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 2));
                break;
            }
            case 5: {
                living.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, living.getEntityWorld().rand.nextInt(4)));
                break;
            }
        }
        return ItemName.mug.getItemStack(ItemMug.MugType.empty);
    }
    
    public int getMaxItemUseDuration(final ItemStack itemstack) {
        return 32;
    }
    
    public EnumAction getItemUseAction(final ItemStack itemstack) {
        return EnumAction.DRINK;
    }
    
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        player.setActiveHand(hand);
        return (ActionResult<ItemStack>)new ActionResult(EnumActionResult.SUCCESS, (Object)StackUtil.get(player, hand));
    }
    
    public static int getTypeOfValue(final int value) {
        return unpackValue(value, 0, 2);
    }
    
    public static int getAmountOfValue(final int value) {
        if (getTypeOfValue(value) == 0) {
            return 0;
        }
        return unpackValue(value, 2, 5) + 1;
    }
    
    public static int getSolidRatioOfBeerValue(final int value) {
        return unpackValue(value, 7, 3);
    }
    
    public static int getHopsRatioOfBeerValue(final int value) {
        return unpackValue(value, 10, 3);
    }
    
    public static int getTimeRatioOfBeerValue(final int value) {
        return unpackValue(value, 13, 3);
    }
    
    public static int getProgressOfRumValue(final int value) {
        return unpackValue(value, 7, 7);
    }
    
    private static int unpackValue(int value, final int bitshift, final int take) {
        value >>>= bitshift;
        final int mask = (1 << take) - 1;
        return value & mask;
    }
    
    static {
        ItemBooze.rumStackability = 2.0f;
        ItemBooze.rumDuration = 600;
    }
    
    private enum BoozeMugType implements IIdProvider
    {
        beer_brew, 
        beer_youngster, 
        beer_beer, 
        beer_ale, 
        beer_dragon_blood, 
        beer_black_stuff, 
        rum;
        
        public static final BoozeMugType[] values;
        
        @Override
        public String getName() {
            return this.name();
        }
        
        @Override
        public int getId() {
            throw new UnsupportedOperationException();
        }
        
        static {
            values = values();
        }
    }
}
