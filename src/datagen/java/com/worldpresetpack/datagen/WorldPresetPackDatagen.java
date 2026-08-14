package com.worldpresetpack.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class WorldPresetPackDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        // TODO: Enregistrer les providers de génération de données ici
        // ex: pack.addProvider(WorldPresetProvider::new);
    }
}
