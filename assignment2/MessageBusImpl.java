package bgu.spl.mics;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private HashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> EventManeger; //each event has a queue of its subscribed microservices
	private HashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> BroadcastManeger; //each broadcast has a queue of its subscribed microservices
	private HashMap<MicroService, LinkedBlockingQueue<Message>> microsEvents; //each microservice has a queue of its own messages
	private HashMap<Event<?>, Future> futureMap;

	private static class MessageBusHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() { //TODO understand where to use
		return MessageBusHolder.instance;
	}

	private MessageBusImpl() {
		EventManeger = new HashMap<>();
		BroadcastManeger = new HashMap<>();
		microsEvents = new HashMap<>();
		futureMap = new HashMap<>();
	}


	@Override
	public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
			if(EventManeger.get(type) == null)
				EventManeger.put(type, new ConcurrentLinkedQueue<>());
			Queue<MicroService> q = EventManeger.get(type);
			q.add(m);
	}

	@Override
	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
			if(BroadcastManeger.get(type) == null)
				BroadcastManeger.put(type, new ConcurrentLinkedQueue<>());
			Queue<MicroService> q = BroadcastManeger.get(type);
			q.add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future= futureMap.get(e);
		synchronized (future){
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		Queue<MicroService> q = BroadcastManeger.get(b.getClass());
		if(!q.isEmpty()){
			for(MicroService m : q){
				synchronized (m){
					microsEvents.get(m).add(b);
					m.notify();
				}
			}
		}
	}

	@Override
	public synchronized  <T> Future<T> sendEvent(Event<T> e) {
		ConcurrentLinkedQueue<MicroService> queueOfEvents = EventManeger.get(e.getClass());
		if (queueOfEvents == null)
			return null;
		Future<T> future = new Future<>();
		MicroService m = queueOfEvents.poll();
		LinkedBlockingQueue<Message> thisQueue = microsEvents.get(m);
		futureMap.put(e, future);
		try {
			thisQueue.put(e);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		queueOfEvents.add(m);
		notifyAll();
		return future;
	}

	@Override
	public void register(MicroService m) {
		synchronized (microsEvents) {
			LinkedBlockingQueue<Message> q = new LinkedBlockingQueue<Message>();
			microsEvents.put(m, q);
		}
	}

	@Override
	public synchronized void unregister(MicroService m) {
		for(ConcurrentLinkedQueue queue1: BroadcastManeger.values())
			queue1.remove(m);
		for(ConcurrentLinkedQueue queue2: EventManeger.values())
			queue2.remove(m);
		microsEvents.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		LinkedBlockingQueue<Message> q = microsEvents.get(m);
		if(q==null)
			throw new IllegalArgumentException();
		return q.take();
	}
}
