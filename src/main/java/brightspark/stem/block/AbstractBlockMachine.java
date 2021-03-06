package brightspark.stem.block;

import brightspark.stem.tileentity.TileMachine;
import brightspark.stem.tileentity.TileMachineWithFluid;
import brightspark.stem.util.CommonUtils;
import brightspark.stem.util.LogHelper;
import brightspark.stem.util.WrenchHelper;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractBlockMachine<T extends TileMachine> extends AbstractBlockContainerDirectional<T>
{
    //@SideOnly(Side.CLIENT)
    //private final int chatIdMachineSide = ClientUtils.getNewChatMessageId();

    public AbstractBlockMachine(String name)
    {
        super(name, Material.ROCK);
        setHasGui();
    }

    public boolean canPickupWithWrench()
    {
        return true;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack tool)
    {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

    /**
     * This returns a complete list of items dropped from this block.
     *
     * @param world The current world
     * @param pos Block position in world
     * @param state Current state
     * @param fortune Breakers fortune level
     * @return A ArrayList containing all items this block drops
     */
    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        TileMachine machine = getTileEntity(world, pos);
        //LogHelper.info("Getting Machine Drops - Used Wrench: " + machine.usedWrenchToBreak);
        if(machine.usedWrenchToBreak)
        {
            //Write data to ItemStack
            ItemStack drop = new ItemStack(this, 1, damageDropped(state));
            writeNbtToDroppedStack(world, pos, state, machine, drop);
            return Lists.newArrayList(drop);
        }
        return super.getDrops(world, pos, state, fortune);
    }

    /**
     * Write any data about the block to the NBT of the dropped ItemStack
     */
    protected void writeNbtToDroppedStack(IBlockAccess world, BlockPos pos, IBlockState state, TileMachine machine, ItemStack drop)
    {
        if(machine != null)
            machine.writeDataToStack(drop);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, state, player, stack);
        if(world == null || pos == null) return;
        TileMachine machine = getTileEntity(world, pos);
        if(machine == null) return;
        machine.readDataFromStack(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        tooltip.add("Energy: " + TileMachine.readEnergyFromStack(stack));
        FluidStack fluid = TileMachineWithFluid.readFluidFromStack(stack);
        if(fluid != null)
            tooltip.add(fluid.getLocalizedName() + ": " + CommonUtils.addDigitGrouping(fluid.amount) + "mb");
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    {
        getTileEntity(world, pos).active = !world.isBlockPowered(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        //Actions for wrench.json
        WrenchHelper.EnumWrenchMode mode = WrenchHelper.getWrenchMode(player.getHeldItem(hand));
        if(mode != null && !player.isSneaking())
        {
            TileMachine te = getTileEntity(world, pos);
            switch(mode)
            {
                /*
                case CONFIG_SIDE:
                    //Change side permissions
                    te.nextSidePerm(state, side);
                    if(world.isRemote)
                        ClientUtils.addClientChatMessage(new TextComponentString(te.getPermForSide(state, side).getChatDisplay(side)), chatIdMachineSide);
                    return true;
                */
                case TURN:
                    //Set block facing
                    if(!world.isRemote && side != EnumFacing.UP && side != EnumFacing.DOWN)
                    {
                        if(side == state.getValue(FACING))
                            world.setBlockState(pos, state.withProperty(FACING, side.getOpposite()));
                        else
                            world.setBlockState(pos, state.withProperty(FACING, side));
                        TileMachine newTE = getTileEntity(world, pos);
                        if(newTE != null)
                            newTE.copyDataFrom(te);
                        else
                            LogHelper.error("Block machine at " + pos.toString() + " was unable to copy tile entity data from previous state!");
                    }
                    return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
