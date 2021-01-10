package com.brandon3055.brandonscore.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by brandon3055 on 17/4/20.
 */
public class MultiCapabilityProvider implements ICapabilitySerializable<CompoundNBT> {

    private Map<Capability<?>, Object> capabilityMap = new HashMap<>();
    private Map<String, INBTSerializable<CompoundNBT>> nameMap = new HashMap<>();

    public MultiCapabilityProvider() {}

    public MultiCapabilityProvider(INBTSerializable<CompoundNBT> capInstance, String name, Capability<?>... capabilities) {
        addCapability(capInstance, name, capabilities);
    }

    public void addCapability(INBTSerializable<CompoundNBT> capInstance, String name, Capability<?>... capabilities) {
        if (CapabilityOP.OP == null) return;
        this.nameMap.put(name, capInstance);
        for (Capability<?> cap : capabilities) {
            Objects.requireNonNull(cap);
            this.capabilityMap.put(cap, capInstance);
        }
    }

    public <T> void addUnsavedCap(Capability<T> capability, T capInstance) {
        if (CapabilityOP.OP == null) return;
        this.capabilityMap.put(capability, capInstance);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (capabilityMap.containsKey(cap)) {
            return LazyOptional.of(() -> capabilityMap.get(cap)).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        nameMap.forEach((s, t) -> tag.put(s, t.serializeNBT()));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        nameMap.forEach((s, t) -> t.deserializeNBT(nbt.getCompound(s)));
    }
}
