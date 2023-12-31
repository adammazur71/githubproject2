package com.example.demo.repository;

import com.example.demo.repository.client.GitHubBranchInfoResponseDto;
import com.example.demo.repository.client.GitHubProxy;
import com.example.demo.repository.client.UserProjectsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service

public class RepositoryService {

    private final GitHubProxy gitHubProxy;
    public RepositoryService(GitHubProxy gitHubProxy) {
        this.gitHubProxy = gitHubProxy;
    }

   public List<ProjectInfoDto> projectInfoDtos(String username){
       List<UserProjectsData> response = makeGitHubRequestForUserProjects(username);
       return generateProjectInfoDtos(response, username);
   }

    private List<UserProjectsData> makeGitHubRequestForUserProjects(String userName) {
        return gitHubProxy.downloadUsersRepos(userName);
    }

    private BranchWithRepoNameDto retrieveBranchesForProject(String userName, String projectName) {
        List<GitHubBranchInfoResponseDto> branchInfoResponse = gitHubProxy.downloadUsersReposBranchesInfo(userName, projectName);
        List<BranchDto> branchDtos = branchInfoResponse.stream()
                .map(branchInfo -> new BranchDto(branchInfo.name(), branchInfo.commit().sha()))
                .toList();

        return new BranchWithRepoNameDto(branchDtos, projectName);
    }

    private List<ProjectInfoDto> generateProjectInfoDtos(List<UserProjectsData> userProjectsData, String username) {
        return userProjectsData.stream()
                .filter(projectsData -> !projectsData.fork())
                .map(projectsData -> retrieveBranchesForProject(username, projectsData.name()))
                .map(branchWithRepoNameDto -> new ProjectInfoDto(branchWithRepoNameDto.repoName(), username, branchWithRepoNameDto.branchesDto()))
                .collect(Collectors.toList());
    }


}
