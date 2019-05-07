package com.holler.app.utils;

import com.holler.app.mvp.main.UserModel;

import java.util.List;
import java.util.Map;

import androidx.annotation.CallSuper;

public interface FiniteStateMachine {

    abstract class StateOwner <Status> implements State{
        public Map<Status, State> states;
        public State currentState;

        public StateOwner(Map<Status, State> states){
            this.states = states;
        }

        public void processStatus(Status status){
            State state = states.get(status);
            if(state == null) return;
            if(!state.equals(currentState)){
                if(state instanceof StateOwner){
                    ((StateOwner) state).onPrepare();
                }
                currentState = state;
                state.onEnter();
                return;
            }
            if(state instanceof StateOwner){
                state.onEnter();
            }
        }

        public abstract void onPrepare();
        // -> process state by status
        // -> set current state
        // -> check if state not already set
        // -> invoke State.onEnter

        // -> define states map


        @Override
        public abstract void onEnter();
    }

    interface State{
        //display data
        void onEnter();
    }
}
