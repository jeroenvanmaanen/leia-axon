package org.leialearns.axon.lag;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface LagRepository extends PagingAndSortingRepository<LagDocument,String> {
    Iterable<LagDocument> findTop10ByOrderByMinuteDescKeyAsc();
}
