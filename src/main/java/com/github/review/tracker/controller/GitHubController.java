package com.github.review.tracker.controller;


import com.github.review.tracker.service.GitHubService;
import com.github.review.tracker.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/review/tracker")
public class GitHubController {

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private GitHubService gitHubService;

    @PostMapping("/suggestion")
    public String getCodeSuggestions(@RequestBody String prompt){
        return openAiService.getCodeSuggestions(prompt);
    }

    @GetMapping("/{owner}/{repo}/{pullNumber}/suggestion")
    public ResponseEntity<String> getGithubComments(@PathVariable String owner,
                                                    @PathVariable String repo,
                                                    @PathVariable String pullNumber){
        String response= null;
        String reviewComments = gitHubService.listCommentsForPullRequest(owner,repo,pullNumber);
        response= openAiService.getCodeSuggestions(reviewComments);
        return ResponseEntity.ok(response);
    }

}
