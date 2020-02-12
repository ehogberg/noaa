CREATE TABLE if not exists "leads" (
  "id" uuid NOT NULL primary key,
  "created_date" timestamp(6) DEFAULT now(),
  "updated_date" timestamp(6) DEFAULT now(),
  "request" json,
  "status" int4,
  "version" varchar COLLATE "pg_catalog"."default",
  "response_code" int4,
  "response" json,
  "all_ctx" json,
  "prepop_data" json,
  "ssn" varchar COLLATE "pg_catalog"."default"
)




