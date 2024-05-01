package net.pl3x.map.fabric.client;

import net.pl3x.map.core.scheduler.Scheduler;

public class ClientScheduler extends Scheduler {
    @Override
    protected void error(String message, Throwable t) {
        Pl3xMapFabricClient.LOGGER.error(message, t);
    }
}
