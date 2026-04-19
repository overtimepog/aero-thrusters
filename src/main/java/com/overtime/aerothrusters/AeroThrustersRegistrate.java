package com.overtime.aerothrusters;

import com.simibubi.create.foundation.data.CreateRegistrate;

public class AeroThrustersRegistrate extends CreateRegistrate {

    protected AeroThrustersRegistrate(String modid) {
        super(modid);
    }

    public static AeroThrustersRegistrate create(String modid) {
        return new AeroThrustersRegistrate(modid);
    }
}
