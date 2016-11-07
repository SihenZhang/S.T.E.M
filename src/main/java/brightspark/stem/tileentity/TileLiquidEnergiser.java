package brightspark.stem.tileentity;

import brightspark.stem.Config;
import brightspark.stem.energy.StemEnergyStorage;
import brightspark.stem.init.StemFluids;
import brightspark.stem.util.CommonUtils;
import cofh.api.energy.IEnergyContainerItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class TileLiquidEnergiser extends TileMachineWithFluid
{
    public int[] pastEnergyInput = new int[40];
    public int lastEnergyAmount = 0;

    public TileLiquidEnergiser()
    {
        super(new StemEnergyStorage(Config.energyPerMb, Config.maxEnergyInput), new FluidStack(StemFluids.fluidStem, 8000), 3);
        Arrays.fill(pastEnergyInput, 0);
    }

    @Override
    public void update()
    {
        super.update();

        //Liquid progress
        if(active && tank.hasSpace())
        {
            if(!worldObj.isRemote)
            {
                //Check energy
                if(isEnergyFull())
                {
                    //Create STEM
                    tank.fillInternal(1);
                    //tank.fillInternal(4000);
                    energy.setEnergyStored(0);
                }
            }
            markDirty();
            worldObj.scheduleUpdate(getPos(), getBlockType(), 2);
        }

        //Handle slots
        for(int i = 0; i < slots.length; i++)
        {
            ItemStack stack = slots[i];
            if(stack == null)
                continue;
            switch(i)
            {
                case 0: //Energy input
                    if(stack.getItem() instanceof IEnergyContainerItem)
                        ((IEnergyContainerItem) stack.getItem()).extractEnergy(stack, getMaxReceieve(null), false);
                    break;
                case 1: //Bucket input
                    if(stack.getItem().equals(Items.BUCKET) && getFluidAmount() >= Fluid.BUCKET_VOLUME && slots[2] == null)
                    {
                        tank.drainInternal(Fluid.BUCKET_VOLUME);
                        stack.stackSize--;
                        if(stack.stackSize <= 0)
                            setInventorySlotContents(1, null);
                        setInventorySlotContents(2, CommonUtils.createFilledBucket(StemFluids.fluidStem));
                    }
                    break;
            }
        }

        //Average input
        for(int i = 0; i < pastEnergyInput.length - 1; i++)
            pastEnergyInput[i] = pastEnergyInput[i + 1];
        int lastDiff = energy.getEnergyStored() - lastEnergyAmount;
        pastEnergyInput[pastEnergyInput.length - 1] = lastDiff < 0 ? 0 : lastDiff;
        lastEnergyAmount = energy.getEnergyStored();
    }

    public int getAverageInput()
    {
        return CommonUtils.average(pastEnergyInput);
    }

    public String getAverageInputString()
    {
        return getAverageInput() + " RF/t";
    }
}
