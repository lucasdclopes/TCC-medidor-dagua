# TCC medidor d'água

## Sobre

Este projeto é do TCC do curso de Engenheria de Computação da Univesp.

O Sistema trabalha com um sensor de distância ultrassônico HC-SR04 ligado em um ESP32, que envia estes dados via HTTP POST para o back-end deste repositório. Também há um módulo Relé ligado no ESP32 para o acionamento de uma bomba d'água

Este repositório contém o backend. 
O frontend pode ser encontrado no outro repositório: https://github.com/lucasdclopes/TCC-medidor-dagua-Frontend/tree/main

O backend é feito em Java, utiliza o Framework Jakarta EE10 e o servidor WildFly.

## Requisitos

Para executar este projeto você precisa do OpenJDK 17. O sistema foi testado com a build da Azul x86 64 bits: https://www.azul.com/downloads/?version=java-17-lts&architecture=x86-64-bit&package=jdk#zulu

O banco de dados utilizado é o Microsoft SQL Server 2022 (16.0.1050.5).

O servidor de aplicação e Web é o WildFly 30.0.0.Final (https://www.wildfly.org/downloads/)

A IDE utilizada é o Eclipse, mas pode-se trabalhar com a IDE de sua preferência

## Hardware ESP32

O código C do dispositivo ESP pode ser encontrado neste repositório: https://github.com/lucasdclopes/TCC-medidor-dagua-ESP
Lembre-se de ajustar a variável `serverAddr`, pois esta indica para onde o ESP32 enviará as requisições 

## Banco de dados

Os arquivos para criar o banco de dados estão no root do repositório.

`criar_estrutura.sql` cria a estrutura(schema) do banco de dados: campos, tabelas, chaves e índices 

Note que o banco de dados não é criado automáticamente. É necessário utilizar os scripts acima. Para gerenciar o SQL Server, recomanda-se utilizar o SQL Server Managment Studio: https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms?view=sql-server-ver16

## Acesso ao banco de dados

Os dados de acesso ao banco de dados ficam configurados nos DataSources do WildFly, dentro do arquivo `standalone.xml` deste servidor.

## Executando

É necessário realizar o deploy em um servidor WildFly 30. Recomendo consultar a documentação deste servidor. https://docs.wildfly.org/30/

