-- Demo seed: 7 online + 3 offline devices, 10 laptops (all online).
-- Scope: docker_name=right_wing, docker_vdms_id=VDMS760. monitor=1; status 1=online, 0=offline.
INSERT INTO public.device
 (id, docker_name, docker_vdms_id, type, monitor, status, user_data_name, display_name,
  user_data_vendor, user_data_model, serial_number, ip_address, mac_address,
  category, asset_group, virtual_device_type, onboard_status,
  created_email, created_timestamp, updated_timestamp, source_type)
VALUES
-- 7 ONLINE devices (monitor=1, status=1)
('demo-SN-PR-101','right_wing','VDMS760','Printer',1,1,'Reception Printer','Reception Printer','HP','LaserJet M428','SN-PR-101','10.0.12.10','00:1C:2B:3C:4D:11','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-DS-102','right_wing','VDMS760','Display',1,1,'Conference Room Display','Conference Room Display','Samsung','QM55R','SN-DS-102','10.0.12.11','00:1C:2B:3C:4D:12','generic','AV',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-KS-103','right_wing','VDMS760','Kiosk',1,1,'Lobby Kiosk','Lobby Kiosk','Elo','I-Series 4','SN-KS-103','10.0.12.13','00:1C:2B:3C:4D:13','generic','AV',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-NVR-104','right_wing','VDMS760','NVR',1,1,'NVR Recorder','NVR Recorder','Hikvision','DS-7716','SN-NVR-104','10.0.12.14','00:1C:2B:3C:4D:14','generic','Security',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-RT-105','right_wing','VDMS760','Network',1,1,'Network Router','Network Router','Cisco','ISR 4331','SN-RT-105','10.0.0.1','00:1C:2B:3C:4D:15','generic','Network',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-TS-106','right_wing','VDMS760','Sensor',1,1,'Temperature Sensor','Temperature Sensor','Monnit','ALTA-TMP','SN-TS-106','10.0.13.16','00:1C:2B:3C:4D:16','generic','Facilities',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-PH-107','right_wing','VDMS760','Phone',1,1,'VoIP Phone','VoIP Phone','Cisco','8845','SN-PH-107','10.0.14.17','00:1C:2B:3C:4D:17','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
-- 3 OFFLINE devices (monitor=1, status=0)
('demo-SN-NAS-108','right_wing','VDMS760','Storage',1,0,'Backup NAS','Backup NAS','Synology','DS1821+','SN-NAS-108','10.0.15.18','00:1C:2B:3C:4D:18','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-SW-109','right_wing','VDMS760','Network',1,0,'Spare Switch','Spare Switch','Netgear','GS748T','SN-SW-109','10.0.0.9','00:1C:2B:3C:4D:19','generic','Network',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-PJ-110','right_wing','VDMS760','Projector',1,0,'Old Projector','Old Projector','Epson','EB-2250U','SN-PJ-110','10.0.16.20','00:1C:2B:3C:4D:1A','generic','AV',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
-- 10 LAPTOPS (monitor=1, status=1 online)
('demo-SN-LT-201','right_wing','VDMS760','Laptop',1,1,'Dell Latitude 7440','Dell Latitude 7440','Dell','Latitude 7440','SN-LT-201','10.0.20.21','00:1C:2B:3C:4D:21','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-202','right_wing','VDMS760','Laptop',1,1,'HP EliteBook 840','HP EliteBook 840','HP','EliteBook 840 G10','SN-LT-202','10.0.20.22','00:1C:2B:3C:4D:22','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-203','right_wing','VDMS760','Laptop',1,1,'Lenovo ThinkPad X1','Lenovo ThinkPad X1','Lenovo','ThinkPad X1 Carbon','SN-LT-203','10.0.20.23','00:1C:2B:3C:4D:23','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-204','right_wing','VDMS760','Laptop',1,1,'MacBook Pro 14','MacBook Pro 14','Apple','MacBook Pro 14','SN-LT-204','10.0.20.24','00:1C:2B:3C:4D:24','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-205','right_wing','VDMS760','Laptop',1,1,'Dell XPS 15','Dell XPS 15','Dell','XPS 15','SN-LT-205','10.0.20.25','00:1C:2B:3C:4D:25','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-206','right_wing','VDMS760','Laptop',1,1,'HP ProBook 450','HP ProBook 450','HP','ProBook 450 G10','SN-LT-206','10.0.20.26','00:1C:2B:3C:4D:26','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-207','right_wing','VDMS760','Laptop',1,1,'Lenovo ThinkPad T14','Lenovo ThinkPad T14','Lenovo','ThinkPad T14','SN-LT-207','10.0.20.27','00:1C:2B:3C:4D:27','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-208','right_wing','VDMS760','Laptop',1,1,'MacBook Air M2','MacBook Air M2','Apple','MacBook Air M2','SN-LT-208','10.0.20.28','00:1C:2B:3C:4D:28','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-209','right_wing','VDMS760','Laptop',1,1,'Dell Latitude 5540','Dell Latitude 5540','Dell','Latitude 5540','SN-LT-209','10.0.20.29','00:1C:2B:3C:4D:29','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms'),
('demo-SN-LT-210','right_wing','VDMS760','Laptop',1,1,'Asus ZenBook 14','Asus ZenBook 14','Asus','ZenBook 14','SN-LT-210','10.0.20.30','00:1C:2B:3C:4D:30','generic','IT',2,0,'admin',(EXTRACT(EPOCH FROM now())*1000)::bigint,(EXTRACT(EPOCH FROM now())*1000)::bigint,'vdms');
