package brightspark.stem.util;

import brightspark.stem.STEM;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.IFluidBlock;

public class ClientUtils
{
    private static Minecraft mc = Minecraft.getMinecraft();

    //Register a model
    public static void regModel(Item item)
    {
        regModel(item, 0);
    }
    public static void regModel(Block block)
    {
        regModel(Item.getItemFromBlock(block), 0);
    }

    //Register a model with meta
    public static void regModel(Item item, int meta)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    //Register a fluid model
    public static void regFluidModel(IFluidBlock fluidBlock)
    {
        Item item = Item.getItemFromBlock((Block) fluidBlock);
        if(item == null)
        {
            LogHelper.fatal("Fluid " + ((Block) fluidBlock).getRegistryName() + " gave a null Item!");
            return;
        }
        ModelBakery.registerItemVariants(item);
        final ModelResourceLocation modelLoc = new ModelResourceLocation(STEM.MOD_ID + ":fluid", fluidBlock.getFluid().getName());
        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
        {
            @Override
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return modelLoc;
            }
        });
        ModelLoader.setCustomStateMapper((Block) fluidBlock, new StateMapperBase()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return modelLoc;
            }
        });
    }
}
