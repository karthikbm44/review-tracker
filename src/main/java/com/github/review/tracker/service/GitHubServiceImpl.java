package com.github.review.tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GitHubServiceImpl implements GitHubService{
    @Value("${github.token}")
    private String githubToken;

    public final RestTemplate restTemplate= new RestTemplate();

    public String listCommentsForPullRequest(String owner,String repo,String pullNumber){
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/pulls/" + pullNumber + "/comments";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(List.of (MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,entity,String.class);
        System.out.println(response.getBody());
        return response.getBody();
    }
}
