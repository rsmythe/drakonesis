package io.heavymeta.targaryen.DragonCommand;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.Queue;

import io.heavymeta.targaryen.DragonConnect.DragonConnect;

/**
 * Created by Ryan on 5/6/2017.
 */

public class DragonHead {

    private DragonAction _currentAction;
    private int _actionDurationRemaining = 0;
    private Handler _sequenceHandler = new Handler(Looper.getMainLooper());
    private Queue<DragonAction> _commandSequence = new LinkedList<DragonAction>();
    private DragonMouthState _mouthState = DragonMouthState.Closed;

    private DragonConnect _connection;

    private final int LOOP_RATE = 100; //loop interval in milliseconds

    private boolean _extendFire = false;

    public DragonHead(DragonConnect connection)
    {
        this._connection = connection;
        this.Run();
    }

    public void OpenMouth()
    {
        // OpenMouth should only ever be the first item in the queue
        if(this._mouthState == DragonMouthState.Closed && this._commandSequence.isEmpty()) {
            this._commandSequence.add(DragonAction.OpenMouth);
        }
    }

    public void CloseMouth()
    {
        if(this._mouthState == DragonMouthState.Open &&
                !this._commandSequence.contains(DragonAction.CloseMouth)) {
            this._commandSequence.add(DragonAction.CloseMouth);
        }
    }

    public void BreathFire()
    {
        if(this._currentAction != null && this._currentAction == DragonAction.BreatheFire) {
            this._extendFire = true;
        }
        else if(this._mouthState == DragonMouthState.Open)
        {
            this._commandSequence.add(DragonAction.BreatheFire);
        }
        else if (this._commandSequence.isEmpty() || this._currentAction == DragonAction.CloseMouth) {
            this._commandSequence.add(DragonAction.OpenMouth);
            this._commandSequence.add(DragonAction.BreatheFire);
            this._commandSequence.add(DragonAction.CloseMouth);
        }
    }

    private Runnable Run()
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                //Update mouthState when OpenMouth and CloseMouth operations complete
                if(_actionDurationRemaining <= 0 && _currentAction != null) {
                    switch (_currentAction) {
                        case OpenMouth:
                            _mouthState = DragonMouthState.Open;
                            break;
                        case CloseMouth:
                            _mouthState = DragonMouthState.Closed;
                            break;
                    }
                }

                //If already breathing fire, extend duration by LOOP_RATE
                if (_extendFire && _currentAction == DragonAction.BreatheFire) {
                    _actionDurationRemaining += LOOP_RATE;
                    _extendFire = false;
                }
                else if(_actionDurationRemaining <= 0) //If current operation is complete
                {
                    //Pop next command off of queue if there is one available
                    if (!_commandSequence.isEmpty()) {
                        _currentAction = _commandSequence.remove();
                        _actionDurationRemaining = _currentAction.Duration();
                    } else {
                        _currentAction = null;
                    }
                }
                //Send the command and loop
                if(_currentAction != null) {
                    _connection.SendAction(_currentAction);
                    _actionDurationRemaining = _actionDurationRemaining - LOOP_RATE;
                }

                _sequenceHandler.postDelayed(this, LOOP_RATE); // repeat the task.
            }
        };

        this._sequenceHandler.post(runnable);

        return runnable;
    }
}
