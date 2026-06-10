package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);

    void deleteByExpireTimeBefore(LocalDateTime dateTime);
}
