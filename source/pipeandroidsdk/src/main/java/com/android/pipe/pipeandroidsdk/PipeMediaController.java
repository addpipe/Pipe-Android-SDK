package com.android.pipe.pipeandroidsdk;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.MediaController;


public class PipeMediaController extends MediaController {

    private Context context;

    public PipeMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public PipeMediaController(Context context, boolean useFastForward) {
        super(context, useFastForward);
        this.context = context;
    }

    public PipeMediaController(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void hide() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            super.hide();
            ((Activity)(context)).finish();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

}
