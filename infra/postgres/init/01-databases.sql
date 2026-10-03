CREATE ROLE catalog_svc LOGIN PASSWORD 'catalog';
CREATE DATABASE catalog OWNER catalog_svc;
-- one role + database per new service