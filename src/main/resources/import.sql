insert into application_user (version, id, username,name,hashed_password,profile_picture) values (1, '1','user','John Normal','$2a$10$xdbKoM48VySZqVSU/cSlVeJn0Z04XCZ7KZBjUBC00eKo5uLswyOpe','');
insert into application_user (version, id, username,name,hashed_password,profile_picture) values (1, '2','admin','Emma Powerful','$2a$10$jpLNVNeA7Ar/ZQ2DKbKCm.MuT2ESe.Qop96jipKMq7RaUgCoQedV.','');

insert into user_roles (user_id, roles) values
('1', 'USER'),
('2', 'USER'),
('2', 'ADMIN');

insert into type_of_printer (id, name) values
('1', 'Laser jet'),
('2', 'Ink jet'),
('3', 'Duplicator'),
('4', 'Foil printer');

insert into variables_for_main_works(id,clazz,description,name) values
('1','DigitalPrinting','Тираж изделий','quantityOfProduct'),
('2','DigitalPrinting','Количество печатных листов','quantityOfPrintSheets'),
('3','DigitalPrinting','Длина изделия','productSizeX'),
('4','DigitalPrinting','Ширина изделия','productSizeY'),
('5','DigitalPrinting','Длина изделия с полями','fullProductSizeX'),
('6','DigitalPrinting','Ширина изделия с полями','fullProductSizeY'),
('7','DigitalPrinting','Длина печатного листа','printSheetSizeX'),
('8','DigitalPrinting','Ширина печатного листа','printSheetSizeY'),
('9','DigitalPrinting','Длина печатной области','printAreaSizeX'),
('10','DigitalPrinting','Ширина печатной области','printAreaSizeY'),
('11','DigitalPrinting','Колонок на печатном листе','columnsOnSheet'),
('12','DigitalPrinting','Строк на печатном листе','rowsOnSheet'),
('13','DigitalPrinting','Изделий на печатном листе','quantityProductionsOnSheet'),
('14','DigitalPrinting','Стоимость одного листа печатного материала','OSDP_MaterialPrice'),
('15','DigitalPrinting','Стоимость одного отпечатка на лицевой стороне','OSDP_FrontPrice'),
('16','DigitalPrinting','Стоимость одного отпечатка на оборотной стороне','OSDP_BackPrice'),
('17','DigitalPrinting','Стоимость работы по печати одного отпечатка','OSDP_EmployerPrice');
