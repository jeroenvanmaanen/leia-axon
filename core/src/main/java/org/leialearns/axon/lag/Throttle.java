package org.leialearns.axon.lag;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Throttle {

    private final LagService lagService;
    private final long block;
    private final double threshold;

    private long count;

    public Throttle(LagService lagService, long block, double threshold) {
        this.lagService = lagService;
        this.block = block;
        count = block;
        this.threshold = threshold;
    }

    public void check() {
        if (count-- >= 0) {
            return;
        }
        count = block;
        while (lagService.getLag() >= threshold) {
            try {
                log.debug("Sleep");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.trace("Interrupted", e);
            }
        }
    }

}
