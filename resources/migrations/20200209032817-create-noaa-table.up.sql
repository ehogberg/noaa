create table if not exists noaas(
  id uuid not null primary key,
  lead_id uuid not null,
  noaa_identified_at timestamp not null,
  noaa_generated_at timestamp,
  noaa_text text,
  noaa_destination_email varchar(254),
  noaa_transmitted_at timestamp,  
  created_at timestamp default now(),
  updated_at timestamp default now()
  );
--;;
create index if not exists idx_noaas_lead_id on noaas(lead_id);










