create view leads_noaas as
select l.id as lead_id,
       l.response_code as response_code,
       l.created_date as lead_created_date,
       n.id as noaa_id,
       n.noaa_identified_at
from leads l left outer join noaas n
on l.id = n.lead_id;
