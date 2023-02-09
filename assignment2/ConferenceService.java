package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TickFinish;
import bgu.spl.mics.application.objects.ConfrenceInformation;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link //PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConfrenceInformation confrence;
    private long currTime = 1;

    public ConferenceService(String name, ConfrenceInformation otherConfrence) {
        super(name);
        confrence = otherConfrence;
    }

    @Override
    protected void initialize() {
        Callback<TickBroadcast> tickTime = new Callback<TickBroadcast>() {
            @Override
            public void call(TickBroadcast eve) {
                currTime = eve.getTimeCounter();
                if(currTime > confrence.getDate()) {
                    sendBroadcast(new PublishConferenceBroadcast(confrence));
                    terminate();
                }
            }
        };
        this.subscribeBroadcast(TickBroadcast.class, tickTime);

        Callback<PublishResultEvent> publishResult = new Callback<PublishResultEvent>() {
            @Override
            public void call(PublishResultEvent eve) {
                confrence.getMyModels().add(eve.getModel());
                if(confrence.getDate() <= currTime) {
                    PublishConferenceBroadcast b = new PublishConferenceBroadcast(confrence);
                    sendBroadcast(b);
                    complete(eve, b);
                }
            }
        };
        this.subscribeEvent(PublishResultEvent.class, publishResult);

        Callback<TickFinish> finishTime = new Callback<TickFinish>() {
            @Override
            public void call(TickFinish eve) {
                terminate();
            }
        };
        this.subscribeBroadcast(TickFinish.class, finishTime);

    }
}
