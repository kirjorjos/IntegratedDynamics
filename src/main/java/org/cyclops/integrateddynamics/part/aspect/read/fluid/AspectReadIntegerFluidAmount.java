package org.cyclops.integrateddynamics.part.aspect.read.fluid;

import net.minecraftforge.fluids.FluidTankInfo;
import org.cyclops.integrateddynamics.core.part.aspect.property.AspectProperties;

/**
 * Aspect that checks the target tank total amount.
 * @author rubensworks
 */
public class AspectReadIntegerFluidAmount extends AspectReadIntegerFluidBase {

    @Override
    protected String getUnlocalizedIntegerFluidType() {
        return "amount";
    }

    @Override
    protected int getValue(FluidTankInfo[] tankInfo, AspectProperties properties) {
        int amount = 0;
        for(FluidTankInfo tank : tankInfo) {
            if(tank.fluid != null) {
                amount += tank.fluid.amount;
            }
        }
        return amount;
    }

}
