-- ============= CREATE USERS

-- shs_state_data holds indicators actual state
-- in a real DB it would be better to create a dedicate tablespace
CREATE USER shs_state_data IDENTIFIED BY shs_state_data ACCOUNT LOCK;

GRANT "CONNECT" TO shs_state_data;
GRANT "RESOURCE" TO shs_state_data;
GRANT "SELECT_CATALOG_ROLE" TO shs_state_data;
ALTER USER shs_state_data DEFAULT ROLE "CONNECT", "RESOURCE", "SELECT_CATALOG_ROLE";
GRANT UNLIMITED TABLESPACE TO shs_state_data;

-- shs_state_warehouse holds indicators historical data
-- in a real DB it would be better to create a dedicate tablespace
/*
CREATE USER shs_state_warehouse IDENTIFIED BY shs_state_data ACCOUNT LOCK;

GRANT "CONNECT" TO shs_state_warehouse;
GRANT "RESOURCE" TO shs_state_warehouse;
GRANT "SELECT_CATALOG_ROLE" TO shs_state_warehouse;
ALTER USER shs_state_warehouse DEFAULT ROLE "CONNECT", "RESOURCE", "SELECT_CATALOG_ROLE";
GRANT UNLIMITED TABLESPACE TO shs_state_data;
*/

-- shs_api holds no data but views and packages to insert or access data

-- ************************************************************************
-- In a real DB it's better to separate access rights domain from technical
-- aspects. To do so one should make users connect to their private schemes
-- and provide each user schema with synonyms for API objects it needs.
-- But here we'll use a simpler scheme.
-- ************************************************************************
 
CREATE USER shs_api IDENTIFIED BY shs_api;

GRANT "CONNECT" TO shs_api;
GRANT "RESOURCE" TO shs_api;
GRANT "SELECT_CATALOG_ROLE" TO shs_api;
ALTER USER shs_api DEFAULT ROLE "CONNECT", "RESOURCE", "SELECT_CATALOG_ROLE";

-- ============= state data objects

CREATE TABLE SHS_STATE_DATA.shs_device_state
(
  device_state_id  INTEGER GENERATED ALWAYS AS IDENTITY NOT NULL,
  address          VARCHAR2(1024) NOT NULL,
  is_on            VARCHAR2(1) NOT NULL,
  val              NUMBER NOT NULL,
  last_save_date   TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE SHS_STATE_DATA.shs_device_state ADD (
  CONSTRAINT shs_device_state_pk PRIMARY KEY (device_state_id) ENABLE VALIDATE,
  CHECK (is_on in ('Y', 'N')) ENABLE VALIDATE,
  CHECK (val >= 0) ENABLE VALIDATE);

ALTER TABLE SHS_STATE_DATA.SHS_DEVICE_STATE
 ADD UNIQUE (ADDRESS);

-- ============= api objects

GRANT SELECT ON shs_state_data.shs_device_state TO shs_api;
GRANT INSERT ON shs_state_data.shs_device_state TO shs_api;
GRANT UPDATE ON shs_state_data.shs_device_state TO shs_api;
GRANT DELETE ON shs_state_data.shs_device_state TO shs_api;

CREATE OR REPLACE VIEW shs_api.shs_device_state AS
select address, is_on, val, last_save_date AS last_save_date_utc
from shs_state_data.shs_device_state;

CREATE OR REPLACE PACKAGE SHS_API.shs_api_pkg AS
/******************************************************************************
   Creates a devise state record with a given address, val and is_on='Y'
   if it did not exist, 
   or updates existing one with a given volume, is_on='Y' and
   last_save_date=current_timestamp
******************************************************************************/
  PROCEDURE set_device_value(p_address in varchar2, p_val IN NUMBER);
/******************************************************************************
   Creates a devise state record with a given address, volume=0 and is_on='Y'
   if it did not exist,
   or updates existing one with is_on='N' and last_save_date=current_timestamp
******************************************************************************/
  PROCEDURE set_device_is_off(p_address in varchar2);
/******************************************************************************
   Removes a devise state record.
******************************************************************************/
  PROCEDURE remove_device(p_address in varchar2);
END shs_api_pkg;
/

CREATE OR REPLACE PACKAGE BODY SHS_API.shs_api_pkg is
  PROCEDURE set_device_value(p_address in varchar2, p_val IN NUMBER) is
  BEGIN
    merge into shs_state_data.shs_device_state
    using dual
    ON (address  = p_address)
    when matched then
        update set is_on = 'Y', val=p_val, last_save_date = current_timestamp(6)
    when not matched then
        insert (address, is_on, val)
        values (p_address, 'Y', p_val);
    commit;
  END;
  
  PROCEDURE set_device_is_off(p_address in varchar2) is
  BEGIN
    merge into shs_state_data.shs_device_state
    using dual
    on (address = p_address)
    when matched then
        update set is_on = 'N', last_save_date = current_timestamp(6)
    when not matched then
        insert (address, is_on, val)
        values (p_address, 'N', 0);
    commit;
  END;
  
  PROCEDURE remove_device(p_address in varchar2) is
  BEGIN
    delete from shs_state_data.shs_device_state
    where address = p_address;
    commit;
  END;
END shs_api_pkg;
/

