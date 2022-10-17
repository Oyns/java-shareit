package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PageableDefault;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestor(@Param("requestor_id") Long userId);

    @Query("SELECT i FROM ItemRequest i WHERE i.requestor NOT IN (:requestor_id)")
    List<ItemRequest> findAllByRequestorWithoutSelfRequest(@Param("requestor_id") Long userId,
                                                           @PageableDefault(size = 0) Pageable pageable);
}
