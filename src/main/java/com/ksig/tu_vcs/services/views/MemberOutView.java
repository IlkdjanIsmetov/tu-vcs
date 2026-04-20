package com.ksig.tu_vcs.services.views;

import com.ksig.tu_vcs.repos.entities.RepositoryMember;
import lombok.Data;

import java.util.UUID;

@Data
public class MemberOutView {

    private UUID id;
    private String user;
    private String role;

    public static MemberOutView fromEntity(RepositoryMember entity){
        MemberOutView memberOutView = new MemberOutView();
        memberOutView.setId(entity.getId());
        memberOutView.setUser(entity.getUser().getUsername());
        memberOutView.setRole(entity.getRole().toString());

        return memberOutView;
    }



//    CREATE TABLE IF NOT EXISTS vcs.repository_member
//            (
//                    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
//    repository_id UUID        NOT NULL REFERENCES vcs.repository (id) ON DELETE CASCADE,
//    user_id       UUID        NOT NULL REFERENCES vcs.app_user (id) ON DELETE CASCADE,
//    role          VARCHAR(20) NOT NULL CHECK (role IN ('MASTER', 'CONTRIBUTOR', 'VIEWER')),
//    UNIQUE (repository_id, user_id)
//);
}
