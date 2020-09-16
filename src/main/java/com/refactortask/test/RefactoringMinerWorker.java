package com.refactortask.test;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RefactoringMinerWorker {
    GitService gitService = new GitServiceImpl();
    GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
    Repository repo;

    public RefactoringMinerWorker(String pathToRepo) throws Exception {
        repo = gitService.openRepository(pathToRepo);
    }

    public RefactoringMinerWorker(String pathToDirectory, String repoUrl) throws Exception {
        repo = gitService.cloneIfNotExists(pathToDirectory, repoUrl);
    }

    public ConcurrentMap<String, AtomicInteger> getAllRefactoringNames() throws Exception {
        final ConcurrentMap<String, AtomicInteger> map = new ConcurrentHashMap<>();
        miner.detectAll(repo, "master", new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                String key;
                for (Refactoring ref : refactorings) {
                    key = ref.getName();
                    map.putIfAbsent(key, new AtomicInteger(0));
                    map.get(key).incrementAndGet();
                }
            }
        });
        map.forEach((k, v) -> System.out.println(k + ", " + v));
        return map;
    }
}
