create table medicao_sensor (
	idMedicao int not null identity(1,1),
	vlDistancia decimal(7,2),
	dtMedicao datetime2(2) not null,
	constraint PK__medicao_sensor primary key(idMedicao desc),
	constraint UN_dtMedicao unique (dtMedicao desc)
)

drop table alerta_enviado
drop table alerta
go

create table alerta (
	idAlerta int not null identity(1,1),
	isHabilitado bit not null,
	intervaloEsperaSegundos smallint not null,
	vlMax decimal(6,2) null, 
	vlMin decimal(6,2) null,
	dtCriado datetime2(2) not null,
	dtUltimoEnvio datetime2(2) null,
	destinatarios varchar(1000) not null,
	habilitarDispositivo bit not null,
	constraint PK__alerta primary key (idAlerta desc)
) 
go

create table alerta_enviado (
	idEnviado int not null identity(1,1),
	idAlerta int not null,
	dtEnvio datetime2(2) not null,
	constraint PK__alerta_enviado primary key (idEnviado desc),
	constraint FK__alerta_enviado__idAlerta__alerta foreign key (idAlerta)
		references alerta(idAlerta)
)

create table log_erros_sistema (
	idLogErros int not null identity(1,1),
	msgErro varchar(max) not null,
	dtLog datetime2(2) not null,
	stacktrace varchar(max) null,
	constraint PK__log_erros_sistema primary key (idLogErros desc)
)


create table user_config (
	configNome varchar(100),
	configValor varchar(500),
	constraint PK__user_config primary key (configNome desc)
)

insert into user_config 
(configNome,configValor)
values
('EMAIL_ENDERECO_REMETENTE','lucas.dev.noreply@gmail.com'),
('EMAIL_SMTP_HOSTNAME','smtp.googlemail.com'),
('EMAIL_SMTP_PORTA','587'),
('EMAIL_NOME_REMETENTE','Mensagem Alerta'),
('EMAIL_ALERTA_TITULO','Alerta de nível d`água!'),
('EMAIL_SMTP_SENHA','??????'),
('EMAIL_SMTP_USER','lucas.dev.noreply@gmail.com'),
('MONITORAMENTO_INTERVALO_MS','3000'),
('SENSOR_ALTURA_RESERVATORIO_CM','100')