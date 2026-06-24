package uz.java.kpisystem.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uz.java.kpisystem.event.*;
import uz.java.kpisystem.service.CacheManagerService;
@Component
@RequiredArgsConstructor
public class CacheEvictEventListener {
    private final CacheManagerService cacheManagerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheEvict(ProjectCacheEvictEvent event) {
        // Faqat DB commit bo'lgandan KEYIN ishlaydi!
        cacheManagerService.delete(event.cachePrefix());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheEvict(GroupCacheEvictEvent event) {
        cacheManagerService.delete(event.cachePrefix());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheEvict(OrganizationCacheEvictEvent event) {
        cacheManagerService.delete(event.cachePrefix());
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheEvict(CheckListCacheEvictEvent event) {
        cacheManagerService.delete(event.cachePrefix());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheEvict(CheckListItemsCacheEvictEvent event) {
        cacheManagerService.delete(event.cachePrefix());
    }
}
