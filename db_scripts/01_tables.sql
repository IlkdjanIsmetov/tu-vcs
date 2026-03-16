CREATE SCHEMA IF NOT EXISTS vcs;

CREATE TABLE vcs.app_user
(
    id          UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    username    VARCHAR(255) UNIQUE NOT NULL,
    email       VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE vcs.repository
(
    id                           UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name                         VARCHAR(255) UNIQUE NOT NULL,
    description                  TEXT,
    owner_id                     UUID                REFERENCES vcs.app_user (id) ON DELETE SET NULL,
    requires_approval_by_default BOOLEAN                  DEFAULT FALSE,
    created_at                   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE vcs.repository_member
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_id UUID        NOT NULL REFERENCES vcs.repository (id) ON DELETE CASCADE,
    user_id       UUID        NOT NULL REFERENCES vcs.app_user (id) ON DELETE CASCADE,
    role          VARCHAR(20) NOT NULL CHECK (role IN ('MASTER', 'CONTRIBUTOR', 'VIEWER')),
    UNIQUE (repository_id, user_id)
);



CREATE TABLE vcs.item
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    repository_id UUID        NOT NULL REFERENCES vcs.repository (id) ON DELETE CASCADE,
    path          TEXT        NOT NULL,
    item_type     VARCHAR(20) NOT NULL CHECK (item_type IN ('FILE', 'DIRECTORY')),
    UNIQUE (repository_id, path)
);


CREATE TABLE vcs.revision
(
    id              UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    repository_id   UUID   NOT NULL REFERENCES vcs.repository (id) ON DELETE CASCADE,
    revision_number BIGINT NOT NULL,
    author_id       UUID   REFERENCES vcs.app_user (id) ON DELETE SET NULL,
    message         TEXT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (repository_id, revision_number)
);


CREATE TABLE vcs.item_revision
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id     UUID        NOT NULL REFERENCES vcs.item (id) ON DELETE CASCADE,
    revision_id UUID        NOT NULL REFERENCES vcs.revision (id) ON DELETE CASCADE,
    action      VARCHAR(20) NOT NULL CHECK (action IN ('ADD', 'MODIFY', 'DELETE')) ,
    storage_key VARCHAR(255),
    file_size BIGINT,
    checksum VARCHAR(64)
);



CREATE TABLE vcs.change_request
(
    id                   UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    repository_id        UUID         NOT NULL REFERENCES vcs.repository (id) ON DELETE CASCADE,
    author_id            UUID         NOT NULL REFERENCES vcs.app_user (id) ON DELETE CASCADE,
    base_revision_number BIGINT       NOT NULL,
    status               VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CONFLICTED')),
    title                VARCHAR(255) NOT NULL,
    description          TEXT,
    created_at           TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at          TIMESTAMP WITH TIME ZONE
);


CREATE TABLE vcs.change_request_item
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    change_request_id UUID        NOT NULL REFERENCES vcs.change_request (id) ON DELETE CASCADE,
    path              TEXT        NOT NULL,
    item_type         VARCHAR(20) NOT NULL CHECK (item_type IN ('FILE', 'DIRECTORY')),
    action            VARCHAR(20) NOT NULL CHECK (action IN ('ADD', 'MODIFY', 'DELETE')) ,
    storage_key VARCHAR(255),
    file_size BIGINT,
    checksum VARCHAR(64)
);


insert into vcs.app_user (id, username, email) values ('123e4567-e89b-12d3-a456-426614174000', 'fakeUser', 'fake@fake.com');
