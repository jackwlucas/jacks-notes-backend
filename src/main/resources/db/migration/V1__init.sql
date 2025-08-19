create extension if not exists pgcrypto;

-- NOTES
create table if not exists notes (
    id         uuid primary key default gen_random_uuid(), -- or uuid_generate_v4()
    user_id   text      not null,
    title      text      not null,
    content    text,
    archived   boolean   not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
    );

-- TAGS
create table if not exists tags (
    id         uuid primary key default gen_random_uuid(),
    user_id   text      not null,
    name       text      not null,
    created_at timestamptz not null default now()
    );

-- NOTE_TAGS (many-to-many)
create table if not exists note_tags (
    note_id uuid not null references notes(id) on delete cascade,
    tag_id  uuid not null references tags(id)  on delete cascade,
    primary key (note_id, tag_id)
    );

create index if not exists idx_notes_owner on notes(user_id);
create index if not exists idx_tags_owner on tags(user_id);
create index if not exists idx_note_tags_note on note_tags(note_id);
create index if not exists idx_note_tags_tag  on note_tags(tag_id);
