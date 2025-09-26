insert into application_user (version, id, username,name,hashed_password,profile_picture) values (1, '1','user','John Normal','$2a$10$xdbKoM48VySZqVSU/cSlVeJn0Z04XCZ7KZBjUBC00eKo5uLswyOpe','');
insert into application_user (version, id, username,name,hashed_password,profile_picture) values (1, '2','admin','Emma Powerful','$2a$10$jpLNVNeA7Ar/ZQ2DKbKCm.MuT2ESe.Qop96jipKMq7RaUgCoQedV.','');

insert into user_roles (user_id, roles) values ('1', 'USER');
insert into user_roles (user_id, roles) values ('2', 'USER');
insert into user_roles (user_id, roles) values ('2', 'ADMIN');

insert into type_of_printer (id, name) values ('1', 'Laser jet');
insert into type_of_printer (id, name) values ('2', 'Ink jet');
insert into type_of_printer (id, name) values ('3', 'Duplicator');
insert into type_of_printer (id, name) values ('4', 'Foil printer');

insert into variables_for_main_works(id,clazz,description,name) values ('1','DigitalPrinting','Тираж изделий','quantityOfProduct');
insert into variables_for_main_works(id,clazz,description,name) values ('2','DigitalPrinting','Количество печатных листов','quantityOfPrintSheets');
insert into variables_for_main_works(id,clazz,description,name) values ('3','DigitalPrinting','Длина изделия','productSizeX');
insert into variables_for_main_works(id,clazz,description,name) values ('4','DigitalPrinting','Ширина изделия','productSizeY');
insert into variables_for_main_works(id,clazz,description,name) values ('5','DigitalPrinting','Длина изделия с полями','fullProductSizeX');
insert into variables_for_main_works(id,clazz,description,name) values ('6','DigitalPrinting','Ширина изделия с полями','fullProductSizeY');
insert into variables_for_main_works(id,clazz,description,name) values ('7','DigitalPrinting','Длина печатного листа','printSheetSizeX');
insert into variables_for_main_works(id,clazz,description,name) values ('8','DigitalPrinting','Ширина печатного листа','printSheetSizeY');
insert into variables_for_main_works(id,clazz,description,name) values ('9','DigitalPrinting','Длина печатной области','printAreaSizeX');
insert into variables_for_main_works(id,clazz,description,name) values ('10','DigitalPrinting','Ширина печатной области','printAreaSizeY');
insert into variables_for_main_works(id,clazz,description,name) values ('11','DigitalPrinting','Колонок на печатном листе','columnsOnSheet');
insert into variables_for_main_works(id,clazz,description,name) values ('12','DigitalPrinting','Строк на печатном листе','rowsOnSheet');
insert into variables_for_main_works(id,clazz,description,name) values ('13','DigitalPrinting','Изделий на печатном листе','quantityProductionsOnSheet');
insert into variables_for_main_works(id,clazz,description,name) values ('14','DigitalPrinting','Стоимость одного листа печатного материала','OSDP_MaterialPrice');
insert into variables_for_main_works(id,clazz,description,name) values ('15','DigitalPrinting','Стоимость одного отпечатка на лицевой стороне','OSDP_FrontPrice');
insert into variables_for_main_works(id,clazz,description,name) values ('16','DigitalPrinting','Стоимость одного отпечатка на оборотной стороне','OSDP_BackPrice');
insert into variables_for_main_works(id,clazz,description,name) values ('17','DigitalPrinting','Стоимость работы по печати одного отпечатка','OSDP_EmployerPrice');
