# Projeto Reserva de Salas para sistemas distribuidos

## Descrição

O objetivo deste projeto é desenvolver um sistema distribuído para gerenciar e facilitar as reservas de salas de estudo em uma instituição acadêmica. O sistema permitirá que os estudantes reservem salas de estudo de forma conveniente e eficiente, evitando conflitos de agendamento.

## Características

* Transparência de Localização: A transparência de localização é uma característica
que permite que os usuários do sistema interajam com recursos distribuídos sem
precisar se preocupar com sua localização física. No contexto do sistema de
reservas de salas de estudo, isso significa que os estudantes podem fazer suas
reservas sem precisar saber em qual servidor ou localidade a sala de estudo está
hospedada. O sistema deve garantir que os usuários tenham acesso transparente às
salas disponíveis, independentemente de sua localização física.
* Tolerância a Falhas: Um sistema distribuído deve ser capaz de lidar com falhas
individuais nos seus componentes, como servidores ou conexões de rede. No
contexto do sistema de reservas de salas de estudo, isso significa que o sistema
deve ser projetado para lidar com possíveis falhas nos servidores ou interrupções na
conectividade de rede, sem interromper ou comprometer as funcionalidades
oferecidas aos usuários.
* Consistência de Dados: Em um sistema distribuído, a consistência de dados
refere-se à garantia de que todos os nós (servidores) tenham acesso às mesmas
informações atualizadas e consistentes.

## Funcionalidades

- [x] Reserva de salas de estudo: Os estudantes poderão visualizar a disponibilidade das salas de estudo e fazer reservas para horários específicos.
- [x] Cancelamento de reservas: Os estudantes devem ter a opção de cancelar suas reservas, liberando a sala de estudo para outros usuários.

### OPCIONAIS:
- [ ] Gerenciamento de conflitos: O sistema deve verificar se existem conflitos de agendamento ao fazer uma reserva e notificar o estudante caso haja algum problema.
- [x] Registro de usuários: Os estudantes poderão se registrar no sistema, fornecendo informações básicas.
