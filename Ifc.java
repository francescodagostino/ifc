package it.adinfo.testdesign.ifc;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by fraob on 16/06/2017.
 */

public class Ifc{

    protected Activity activity;
    protected OnFragmentInteractionListener listener;
    private PauseHandler pauseHandler;
    private boolean useEvents = false;
    private static Ifc instance = null;

    /* Ifc interface. Must be implemented on activity */
    public interface OnFragmentInteractionListener{
        void sendMsg(Message msg);
        void readMsg(Message msg);
        void onIfcEvent(Ifc.IfcEvent event);
    }

    public Ifc(OnFragmentInteractionListener listener,Activity activity){
        this.activity = activity;
        this.listener = listener;
        EventBus.getDefault().register(activity);
    }

    private Ifc(OnFragmentInteractionListener listener,Activity activity,boolean events){
        this.activity = activity;
        this.listener = listener;
        if(events) EventBus.getDefault().register(activity);
    }

    public static synchronized Ifc getDefault(OnFragmentInteractionListener listener, Activity activity){
        if (null == instance) instance = new Ifc(listener,activity);
        return instance;
    }

    public static synchronized Ifc getDefault(OnFragmentInteractionListener listener, Activity activity, boolean events){
        if (null == instance) instance = new Ifc(listener,activity,events);
        return instance;
    }



    public static synchronized Ifc getDefault()    {
        if (null == instance)  throw new IllegalStateException(Ifc.class.getSimpleName() + " is not initialized, call getInstance(...) first");
        return instance;
    }


//    public void getDefaultWithNoCache(){
//    }

    //builder

    //distruttore?

    public void sendMsg(Message msg){
        this.pauseHandler.sendMessage(msg);
//VALUTARE se assegnare direttamente il pauseHandler al messaggio
    }

    public void register(Activity activity){
        this.pauseHandler.resume(activity);
        EventBus.getDefault().register(activity);
    }

    public void unregister(){
        this.pauseHandler.pause();
        EventBus.getDefault().unregister(activity);
    }

    /**
     * Message Handler class that supports buffering up of messages when the activity is paused i.e. in the background.
     */
    public class PauseHandler extends Handler{

        /** Message Queue Buffer **/
        private final List<Message> messageQueueBuffer = Collections.synchronizedList(new ArrayList<Message>());

        /** Resume the handler.**/
        public final synchronized void resume(Activity activ) {
            activity = activ;          /* Flag indicating the pause state */

            while (messageQueueBuffer.size() > 0) {
                final Message msg = messageQueueBuffer.get(0);
                messageQueueBuffer.remove(0);
                sendMessage(msg);
            }
        }

        /** Pause the handler.**/
        public final synchronized void pause() {
            activity = null;
        }

        /** Store the message if we have been paused, otherwise handle it now.
         * @param msg   Message to handle.
         */
        @Override
        public final synchronized void handleMessage(Message msg) {
            if (activity == null) {
                final Message msgCopy = Message.obtain();
                msgCopy.copyFrom(msg);
                messageQueueBuffer.add(msgCopy);
            } else {
                listener.readMsg(msg);
            }
        }

    }

    public static class IfcEvent{
        public Message msg;
        public IfcEvent(Message msg){
            this.msg = msg;
        }
    }


    //FACTORY
    public static Message Message(int what, int action, int extra, Object obj){
        return Message.obtain(null,what,action,extra,obj);
    }

    public static void IfcEvent(IfcEvent eventMsg){
        EventBus.getDefault().post(eventMsg);
    }

}
