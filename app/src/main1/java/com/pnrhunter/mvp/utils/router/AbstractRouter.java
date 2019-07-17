package com.pnrhunter.mvp.utils.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import java.util.EmptyStackException;
import java.util.Stack;

public abstract class AbstractRouter {
    private Context context;
    private Stack<Intent> routingHistory;

    public AbstractRouter(Context context) {
        this.context = context;
        routingHistory = new Stack<>();
    }

    public void goTo(Class<? extends Activity> activity){
        if(!checkTransitionAllowedTo(activity)) throw new IllegalArgumentException("Transition to " + activity.getName() + " not allowed");
        Intent i = new Intent(context, activity);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        addExtra(i, activity);
        saveToHistory(i);
        context.startActivity(i);
    }

    public abstract boolean checkTransitionAllowedTo(Class<? extends Activity> activity);
    public abstract void addExtra(Intent i, Class<? extends Activity> activity);

    public void goBack(){
        try {
            Intent last = routingHistory.pop();
            if(last.equals(currentIntent))
                last = routingHistory.pop();
            currentIntent=last;
            saveToHistory(last);
            context.startActivity(last);
        }catch (EmptyStackException e){
            Logger.e("Can't go back, history is empty");
        }
    }

    private Intent currentIntent;

    private void saveToHistory(Intent i){
        currentIntent = i;
        routingHistory.push(i);
    }

}
