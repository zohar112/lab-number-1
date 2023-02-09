package bgu.spl.mics.application.services;

import bgu.spl.mics.Callback;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TickFinish;

import java.util.Timer;
import java.util.TimerTask;


/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService {
	long speed;
	long duration;
	long timeCount;

	public TimeService(long otherSpeed, long otherDuration) {
		super("TimeService");
		speed = otherSpeed;
		duration = otherDuration;
		timeCount = 1;
	}

	@Override
	protected void initialize() {
		Callback<TickFinish> finishTime = new Callback<TickFinish>() {
			@Override
			public void call(TickFinish eve) {
				terminate();
			}
		};
		this.subscribeBroadcast(TickFinish.class, finishTime);

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (timeCount*speed < duration) {
					timeCount++;
					sendBroadcast(new TickBroadcast(timeCount, duration));
				}
				else {
					sendBroadcast(new TickFinish());
					timer.cancel();
				}
			}
		}, 1, speed);
	}
}