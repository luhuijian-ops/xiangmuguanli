package com.xiangmuguanli.repository;

import com.xiangmuguanli.entity.UserBinding;
import com.xiangmuguanli.enums.OAuthPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBindingRepository extends JpaRepository<UserBinding, Long> {

    Optional<UserBinding> findByPlatformAndOpenId(OAuthPlatform platform, String openId);

    Optional<UserBinding> findByUserIdAndPlatform(Long userId, OAuthPlatform platform);
}
