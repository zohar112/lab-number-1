package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;

public class PublishConferenceBroadcast implements Broadcast {
      private ConfrenceInformation conference;

        public PublishConferenceBroadcast(ConfrenceInformation conference) {
        this.conference= conference;
    }

    public ConfrenceInformation getConference() {
        return conference;
    }
}
