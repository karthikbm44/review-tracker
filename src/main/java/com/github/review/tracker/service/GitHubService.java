package com.github.review.tracker.service;

public interface GitHubService {
    String listCommentsForPullRequest(String owner,String repo,String pullNumber);
}
