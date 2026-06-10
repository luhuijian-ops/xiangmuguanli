package com.xiangmuguanli.entity;

import com.xiangmuguanli.enums.OAuthPlatform;

import jakarta.persistence.*;

@Entity
@Table(name = "user_bindings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"platform", "open_id"})
})
public class UserBinding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuthPlatform platform;

    @Column(name = "open_id", nullable = false)
    private String openId;

    @Column(name = "union_id")
    private String unionId;

    private String nickname;

    private String avatar;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OAuthPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(OAuthPlatform platform) {
        this.platform = platform;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
