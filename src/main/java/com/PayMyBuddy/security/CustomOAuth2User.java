package com.PayMyBuddy.security;

import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Setter
public class CustomOAuth2User implements OAuth2User {

    private OAuth2User oauth2User;

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    private String email;

    public CustomOAuth2User(OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oauth2User.getAuthorities();
    }

    @Override
    public String getName() {
        if (oauth2User.getAttribute("email") == null) {
            return getEmail();
        } else {
            return oauth2User.getAttribute("email");
        }
    }

    public String getDisplayName() {
        return oauth2User.getAttribute("name");
    }
}
