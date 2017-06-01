package io.heavymeta.drakonesis.DragonCommand;

/**
 * Created by Ryan on 5/6/2017.
 */

public enum DragonAction {
    OpenMouth((byte)0x02, 3000),
    CloseMouth((byte)0x03, 3000),
    BreatheFire((byte)0x04, 1000),
    ExtendWings((byte)0x05, 5000),
    RetractWings((byte)0x06, 5000);

    private final byte _id;
    private final int _duration;
    DragonAction(byte id, int duration)
    {
        this._id = id;
        this._duration = duration;
    }


    public byte getValue()
    {
        return this._id;
    }

    public int Duration() { return this._duration; }
}
