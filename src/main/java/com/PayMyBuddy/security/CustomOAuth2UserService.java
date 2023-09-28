package com.PayMyBuddy.security;

import com.PayMyBuddy.dto.GithubApiEmailDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user =  super.loadUser(userRequest);
        CustomOAuth2User customUser = new CustomOAuth2User(user);
        if (customUser.getName() == null) {
            try {
                String email = getGithubEmail(userRequest);
                customUser.setEmail(email);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return customUser;
    }

    private static String getGithubEmail(OAuth2UserRequest userRequest) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/user/emails"))
                .header("Authorization", "token " + userRequest.getAccessToken().getTokenValue())
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        ObjectMapper mapper = new ObjectMapper();
        List<GithubApiEmailDTO> githubApiEmailDTOList = Arrays.asList(mapper.readValue(body, GithubApiEmailDTO[].class));
        return githubApiEmailDTOList.stream().filter(g -> g.primary().equals("true"))
                .map(GithubApiEmailDTO::email)
                .findAny()
                .orElse(null);
    }

}
