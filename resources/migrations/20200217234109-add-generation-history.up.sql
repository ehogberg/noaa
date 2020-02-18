create table if not exists noaa_generation_history(
    id uuid not null primary key,
    noaa_id uuid not null,
    noaa_generated_at timestamp not null,
    noaa_text text not null,
    noaa_template_type varchar(255) not null,
    noaa_generation_data json not null,
    created_at timestamp default now()
);
--;;
create index if not exists idx_noaa_generation_history_noaa_id
on noaa_generation_history(noaa_id);
