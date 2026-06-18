-- Cleanup after ClientBarCodeRepositoryIT test methods
DELETE FROM client_bar_code WHERE id IN ('cbc-1','cbc-2','cbc-3','cbc-4','cbc-5');
DELETE FROM location WHERE id IN ('loc-cbc-1','loc-cbc-2');
DELETE FROM device WHERE id IN ('dev-cbc-1','dev-cbc-2');
