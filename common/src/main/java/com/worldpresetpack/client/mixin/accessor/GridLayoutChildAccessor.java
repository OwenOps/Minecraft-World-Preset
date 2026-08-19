package com.worldpresetpack.client.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.gui.layouts.GridLayout$ChildContainer")
public interface GridLayoutChildAccessor {

    @Accessor("row")
    int worldpresetpack$getRow();

    @Mutable
    @Accessor("row")
    void worldpresetpack$setRow(int row);
}
