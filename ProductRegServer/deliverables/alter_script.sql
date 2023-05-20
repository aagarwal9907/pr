
/*changes to brand_dtl table*/

alter table brand_dtl add column `email` varchar(100) default NULL after password ;
alter table brand_dtl add column `day` int(2) NOT NULL default '0' after email;
alter table brand_dtl add column `buser_name` varchar(100) default NULL after day;
alter table brand_dtl add column `buser_pwd` varchar(20) default NULL after buser_name;


/**Changes to generic _master */
INSERT INTO `generic_master` (`name`, `value`, `remarks`) VALUES
('BRANDMASTER', '\\\\192.168.1.108\\common\\DATA\\BRANDMST', NULL),
('XSLPARSER', 'xlsreaderconf.xml,xlsreaderconf1.xml,xlsbrandmaster.xml,xlsquestion.xml,xlsunregmob.xml', NULL),
('QUESTION', '\\\\192.168.1.108\\common\\DATA\\QUESTIONS', NULL),
('UNREGMOB', '\\\\192.168.1.108\\common\\DATA\\UNREGMOB', NULL);

/*new columns in unregister_auth  table*/
alter table `unregister_auth` add column `rreg_yn` char(1) default NULL after ac_name;
alter table `unregister_auth` add column  `reg_sr` char(1) default NULL after rreg_yn;