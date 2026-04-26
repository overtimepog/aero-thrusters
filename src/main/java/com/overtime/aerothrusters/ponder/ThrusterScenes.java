package com.overtime.aerothrusters.ponder;

import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class ThrusterScenes {

    public static void thrusterOverview(SceneBuilder scene, SceneBuildingUtil util) {
        CreateSceneBuilder cScene = new CreateSceneBuilder(scene);
        scene.title("thruster_overview", "Thruster");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(10);

        BlockPos tankBot = util.grid().at(1, 1, 1);
        BlockPos tankTop = util.grid().at(1, 2, 1);
        BlockPos pumpPos = util.grid().at(1, 1, 2);
        BlockPos thrusterPos = util.grid().at(1, 1, 3);
        BlockPos motorPos = util.grid().at(2, 1, 1);
        BlockPos cogPos = util.grid().at(2, 1, 2);
        BlockPos leverPos = util.grid().at(1, 2, 3);
        Selection leverSelection = util.select().position(leverPos);

        scene.world().showSection(util.select().position(thrusterPos), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(80)
                .colored(PonderPalette.WHITE)
                .text("Thrusters burn liquid fuel to produce directional thrust")
                .placeNearTarget()
                .pointAt(util.vector().topOf(thrusterPos));
        scene.idle(90);

        // bring in tank + pump
        scene.world().showSection(util.select().fromTo(tankBot, tankTop), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(pumpPos), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .colored(PonderPalette.BLUE)
                .text("Any '#c:fuel' tagged fluid works. A pump moves it from the tank into the thruster")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(pumpPos, Direction.UP));
        scene.idle(100);

        // drive the pump with a motor + cogwheel
        scene.world().showSection(util.select().fromTo(motorPos, cogPos), Direction.DOWN);
        scene.idle(15);
        scene.overlay().showText(90)
                .colored(PonderPalette.FAST)
                .text("Any rotational source works. A Cogwheel linking the pump's axle is one clean option")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(cogPos, Direction.UP));
        scene.idle(100);

        // lever + redstone ramp
        scene.world().showSection(leverSelection, Direction.DOWN);
        scene.idle(15);

        for (int i = 0; i < 15; i++) {
            scene.idle(2);
            final int state = i + 1;
            scene.world().modifyBlockEntityNBT(leverSelection, AnalogLeverBlockEntity.class,
                    nbt -> nbt.putInt("State", state));
            cScene.effects().indicateRedstone(leverPos);
        }
        scene.idle(10);
        scene.overlay().showText(100)
                .colored(PonderPalette.RED)
                .text("An Analog Lever (or any analog redstone signal) acts as the throttle")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(leverPos, Direction.UP));
        scene.idle(110);

        scene.overlay().showText(90)
                .colored(PonderPalette.OUTPUT)
                .text("Stronger signals burn more fuel per tick and push harder")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(thrusterPos, Direction.SOUTH));
        scene.idle(100);

        // ramp down to demonstrate the shutdown
        for (int i = 14; i >= 0; i--) {
            scene.idle(2);
            final int state = i;
            scene.world().modifyBlockEntityNBT(leverSelection, AnalogLeverBlockEntity.class,
                    nbt -> nbt.putInt("State", state));
        }
        scene.idle(10);
        scene.overlay().showText(90)
                .colored(PonderPalette.SLOW)
                .text("Drop the signal to zero and the engine shuts off. No burn, no thrust")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(leverPos, Direction.UP));
        scene.idle(100);

        // binary mode explanation
        scene.overlay().showText(90)
                .colored(PonderPalette.GREEN)
                .text("Shift-right-click with a Wrench to toggle Binary mode")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(thrusterPos, Direction.SOUTH));
        scene.idle(100);

        scene.overlay().showText(90)
                .colored(PonderPalette.OUTPUT)
                .text("In Binary mode, the thruster runs at full power whenever it receives any redstone signal")
                .placeNearTarget()
                .pointAt(util.vector().blockSurface(thrusterPos, Direction.SOUTH));
        scene.idle(100);

        scene.markAsFinished();
        scene.idle(20);
    }
}
