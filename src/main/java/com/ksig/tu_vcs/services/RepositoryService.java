package com.ksig.tu_vcs.services;


import com.ksig.tu_vcs.repos.RepositoryMemberRepository;
import com.ksig.tu_vcs.repos.RepositoryRepository;
import com.ksig.tu_vcs.repos.entities.Repository;
import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import com.ksig.tu_vcs.repos.entities.enums.Role;
import com.ksig.tu_vcs.services.views.RepositoryView;
import com.ksig.tu_vcs.utils.UserContextUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;




@Service
public class RepositoryService {
    private final RepositoryRepository repositoryRepository;
    private final RepositoryMemberRepository repositoryMemberRepository;
    private final UserContextUtil userContextUtil;

    public RepositoryService(RepositoryRepository repositoryRepository,
                             RepositoryMemberRepository repositoryMemberRepository,
                             UserContextUtil userContextUtil) {
        this.repositoryRepository = repositoryRepository;
        this.repositoryMemberRepository = repositoryMemberRepository;
        this.userContextUtil = userContextUtil;
    }


    @Transactional
    public Repository createRepository(RepositoryView view) {
        Repository repository = new Repository();
        repository.setName(view.getRepositoryName());
        repository.setDescription(view.getDescription());
        repository.setOwner(userContextUtil.getCurrentUser());
        repository.setRequiresApprovalByDefault(true);

        repositoryRepository.save(repository);

        RepositoryMember member = new RepositoryMember();
        member.setRepository(repository);
        member.setUser(userContextUtil.getCurrentUser());
        member.setRole(Role.MASTER);

        repositoryMemberRepository.save(member);

        return repository;


    }
}
