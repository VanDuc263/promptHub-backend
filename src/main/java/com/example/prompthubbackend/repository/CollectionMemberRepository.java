package com.example.prompthubbackend.repository;

import com.example.prompthubbackend.model.Collection;
import com.example.prompthubbackend.model.CollectionMember;
import com.example.prompthubbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionMemberRepository extends JpaRepository<CollectionMember, CollectionMember.CollectionMemberId> {
    boolean existsByCollectionAndUser(Collection collection, User user);
    List<CollectionMember> findByUserOrderByJoinedAtDesc(User user);
    List<CollectionMember> findByCollectionOrderByJoinedAtAsc(Collection collection);
}
