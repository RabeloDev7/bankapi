# BankAPI - Claude Instructions

Este é um projeto Spring Boot chamado BankAPI.

## Arquitetura
- Padrão MVC
- Controller → Service → Repository
- DTO para entrada e saída
- Entities apenas para persistência

## Regras de Código
- Seguir Clean Code
- Evitar lógica no Controller
- Services contêm regras de negócio
- Repository apenas acesso a dados

## Segurança
- Usar Spring Security
- Preparado para autenticação JWT

## Objetivo
Sistema bancário com:
- usuários
- contas
- transferências
- pix