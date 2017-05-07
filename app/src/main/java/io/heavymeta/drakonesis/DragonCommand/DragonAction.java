package io.heavymeta.drakonesis.DragonCommand;

/**
 * Created by Ryan on 5/6/2017.
 */

public enum DragonAction {
    OpenMouth((byte)0, 3000),
    CloseMouth((byte)1, 3000),
    BreatheFire((byte)2, 1000),
    ExtendWings((byte)3, 5000),
    RetractWings((byte)4, 5000);

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
