# Documentação Técnica do Backend da Solução Rota Estudantil

React Native | Expo | TypeScript

## Visão Geral

Aplicação mobile para estudantes e motoristas. O app consome a API do backend para exibir a prontidão da rota e permitir que o estudante gerencie sua decisão de embarque.

### Estrutura de Componentes (Destaque: Home.tsx)

 * **Estado de Presença**: Gerenciado via useState e AsyncStorage, permitindo persistência de sessão.
 * **Atualização Otimista**: A interface responde instantaneamente ao clique do usuário, utilizando um atraso de segurança (setTimeout) antes da sincronização total com o servidor para evitar conflitos de estado.
 * **Gestão de Decisão**:
   * **Estudantes**: Possuem 4 opções de status (IDA, VOLTA, AMBOS, NAO_VOU).
   * **Motoristas**: Recebem feedback visual em tempo real sobre a totalidade de respostas da turma.
   
### Fluxo de Comunicação

 1. O app consulta GET /usuarios/passageiros na montagem do componente.
 2. Ao confirmar presença, o app faz um POST para /rota/confirmar.
 3. O status é mantido no banco para garantir que o motorista tenha visibilidade exata da intenção do aluno.
 

### Pré-requisitos
 * JDK 17+, Maven 3+, Banco de Dados PostgreSQL/MySQL.
 
### Instalação
* Configure o application.properties e sua .env com a credenciais do seu banco e rode mvn spring-boot:run.

### Notas de Manutenção

 * **Status LIMPAR**: Sempre que precisar travar o motorista (indecisão), o frontend deve enviar o status LIMPAR para que o registro seja deletado do banco, disparando a regra de "Aguardando Resposta" na tela do motorista.
 * **Concorrência**: Caso o status visual pareça "oscilar", ajuste o setTimeout na função registrarPresenca do arquivo Home.tsx para compensar latências de rede.
 
### Arquitetura
 *    * RotaController.java (lógica @Transactional)
 *    * Presenca.java (configuração do relacionamento @ManyToOne)
 *    * Lógica de deleção/trava implementada no RotaController.java e Home.tsx
 *    * Estratégia de sincronização otimista no Home.tsx

 ## Visualizar o Frontend da Aplicação

 https://github.com/pethersonzada/van-app/
