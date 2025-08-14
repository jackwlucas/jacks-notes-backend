create extension if not exists pgcrypto;

create table if not exists notes (
    id         uuid primary key default gen_random_uuid(),
    user_id   text not null,
    title      varchar(255) not null,
    content    text,
    archived   boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
    );

create table if not exists tags (
    id         uuid primary key default gen_random_uuid(),
    user_id   text not null,
    name       varchar(255) not null,
    created_at timestamptz not null default now(),
    constraint uq_tag_user_name unique (user_id, lower(name))
    );

create table if not exists note_tags (
    note_id uuid not null references notes(id) on delete cascade,
    tag_id  uuid not null references tags(id) on delete cascade,
    primary key (note_id, tag_id)
    );

-- Helpful indexes
create index if not exists idx_notes_user_created_at on notes(user_id, created_at desc);
create index if not exists idx_tags_user_name on tags(user_id, lower(name));
