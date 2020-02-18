create table if not exists noaa_delivery_history(
  id uuid not null primary key,
  noaa_id uuid not null,
  noaa_transmitted_at timestamp not null,
  noaa_destination_email varchar(254) not null,
  created_at timestamp default now()
 )
 --;;
 create index if not exists idx_noaa_delivery_history_noaa_id
 on noaa_delivery_history(noaa_id);
