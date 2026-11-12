# Plano de auditoria e correções — SpamBlocker Brasil

Auditoria de 12/11/2026. **Modalidade somente documental. Nenhuma correção implementada.** Todos os itens abaixo estão abertos; os passos de implementação dependem de uma solicitação posterior. Este documento é um diagnóstico e um plano, não um aceite de produção.

## 1. Estado auditado e integridade

- Repositório existente: `/home/usuario/workspace/altec/Bloqueador_de_chamadas`.
- Base Git: `1b2bf9ec90fd9c53a0b1d973e45d3675c77198b0`, tag upstream `v5.18`; remoto `https://github.com/aj3423/SpamBlocker.git`.
- Branch de entrada: `feature/interface-brasil`. Branch documental: `docs/auditoria-geral-2026-11-12`, criada antes de escrever no projeto.
- Entrada: índice vazio, **19 arquivos rastreados modificados e 54 arquivos não rastreados**, sem contar ignorados. Nenhum foi incluído no commit documental, descartado, escondido em stash ou sobrescrito.
- Produto observado: `spam.blocker.brasil`, `5.18.1-rc.2`, código `51807`, `minSdk 29`, `targetSdk 36`, `compileSdk 37`; Gradle `9.7.1`, AGP `9.4.1`, Kotlin `2.4.20`, daemon JVM JetBrains 21 e alvo Java 17, conforme arquivos locais.
- O estado auditado inclui código Brasil ainda **não commitado**. Fazer checkout apenas deste commit documental não recupera a RC2. Os 35 hashes de fontes em `artifacts/SpamBlocker-Brasil-5.18.1-rc.2.metadata.json` conferem com a árvore atual. O snapshot indicado ali também confere: SHA-256 `6c1388bed927d2706d810071b5905ae6e42086a665e1244ce71bd97a88729104`.
- APK em `artifacts/SpamBlocker-Brasil-5.18.1-rc.2-debug.apk` e saída `app/build/outputs/apk/brasil/debug/app-brasil-debug.apk`: mesmo SHA-256 `9a1c738fff5a06ae0e4695e541bea6af093a7a8de1580b585296272ad022516b`. Manifesto mesclado confirma `debuggable=true`; assinatura debug permanece uma decisão anterior, não foi alterada.
- Um manifesto de hashes de 1.053 arquivos não ignorados foi capturado antes do trabalho. Digest do mapa ordenado caminho → SHA-256: `91c23994ac1f95682cd294280512f1e3b5fc1e137c00904b4a7c8ec5469f0abe`. Conferência ao fechar a documentação exige zero diferenças nesses arquivos.

Normas aplicadas: `regras-comutacao.md`, registro central `spamblocker-brasil`, `dev-rules/AGENTS.md`, módulos de governança, desenvolvimento geral, auditoria, Git, testes, revisão, Android, Codex, versionamento e integrações. O `AGENTS.md` local acrescenta preferência por traduções curtas. Nenhuma regra global foi promovida ou modificada.

## 2. Escopo, método e limites

Inventário de `app/src`: 185 Kotlin, 31 Java, 662 XML, além de imagens, fontes e um `.bak`; 42.705 linhas Kotlin, 10.108 Java e 53.613 XML. A auditoria abrangeu a árvore, não apenas o diff: manifestos e permissões; Gradle e workflows; motor de regras; chamadas/SMS/MMS; contatos; integrações HTTP/OAuth; automações; SQLite, migrações, backup e importações; UI Compose, recursos e testes.

Foi feita inspeção manual dos fluxos críticos, buscas transversais por fronteiras de entrada/saída, análise do compilador/lint sobre a variante Brasil, parsing dos 662 XML e provas isoladas com dados sintéticos. Isso **não representa leitura linha a linha de todas as traduções e do código PDU de terceiros**, prova de ausência de vulnerabilidades, teste de carga ou certificação de acessibilidade. O alcance e a confiança de cada achado estão indicados abaixo.

| Área | Evidência e cobertura | Limite restante |
|---|---|---|
| Build/configuração | Gradle, catálogo, manifests mesclados, três flavors, CI e artefatos | Release assinado/R8, Play e execução do CI remoto não exercitados |
| Motor de chamadas | Ordem/prioridades, contatos, preprocessadores, resposta ao Telecom, efeitos posteriores | Chamada real, fabricante, dual SIM, doze e latência não medidos |
| SMS/MMS e IPC | Receiver, serviço público, parsing do slot, consultas, efeitos de processamento | Binder adversarial e PDU reais não enviados |
| Dados/integrações | Adaptadores, migrações, restore, CSV/XML, HTTP, OAuth, logs | Sem acesso a contas, tokens, agenda ou bancos reais |
| Interface | Código Brasil e componentes compartilhados; quatro capturas anteriores abertas nesta auditoria | Sem nova sessão interativa, TalkBack, teclado, fonte 2,0 ou multitarefa |
| Testes | Quatro unitários executados novamente; 30 casos existentes de Formula em JVM; provas de borda | Suíte instrumentada completa não executada; `RuleTest` apaga dados no setup |

As capturas inspecionadas, de **11/11**, são `retrato/01-protecao-escuro.png`, `retrato/11-ajustes-claro.png`, `paisagem/03-dialogo-teste.png` e `paisagem/08-historico-expandido.png`, em `artifacts/ui-review-5.18.1-rc.2/`. Mostram composição coerente nos estados capturados, diálogo e nome longo visíveis. Não provam o funcionamento atual nem a semântica para leitor de tela. Os registros anteriores de 4 testes de provider e 4 de UI em cada orientação/escala permanecem evidência histórica, não foram contados como execuções desta auditoria.

## 3. Verificações realizadas

Comandos Gradle executados na raiz absoluta do projeto, com dependências locais e `--offline`:

| ID de evidência | Comando/verificação | Resultado |
|---|---|---|
| E01 | `git status --short`, `git diff --cached --name-only`, branch/log/remotos e hashes iniciais | Índice vazio; alterações anteriores identificadas e preservadas |
| E02 | `./gradlew --offline :app:testBrasilDebugUnitTest --rerun-tasks` | **PASSOU**, 4 testes, zero falhas/erros/ignorados; 28 tarefas executadas, 1m57s |
| E03 | `./gradlew --offline :app:assembleBrasilDebug :app:assembleBrasilDebugAndroidTest :app:lintBrasilDebug` | Os dois builds concluíram; comando terminou com código 1 no lint, em 2m42s; 24 erros, 107 avisos, 2 hints |
| E04 | Build `:app:assembleFdroidDebug :app:assembleGoogleplayDebug`, offline | **PASSOU**, ambas as variantes; 80 tarefas executadas, 1m26s |
| E05 | Provas JVM/SQLite isoladas, descritas na seção 7 | SMS sem slot, CSV, cabeçalhos e aspas em consulta reproduzidos; Formula 30/30 e formatação 18/18 passaram |
| E06 | Parsing XML e comparação com upstream | 662 XML bem formados; todos os 13 arquivos de localização principal dos erros de lint idênticos à base |
| E07 | `git diff --check`; conferência SHA-256 de entradas e APK | Sem erro de whitespace preexistente; APK e 35 fontes conferem com metadados RC2 |
| E08 | Inspeção visual das quatro capturas e código Compose | Evidência histórica parcial; pendências de acessibilidade registradas |

Relatórios gerados em `app/build/reports/lint-results-brasilDebug.{xml,html}` e relatório textual indicado por Gradle em `app/build/intermediates/lint_intermediate_text_report/brasilDebug/lintReportBrasilDebug/lint-results-brasilDebug.txt`. São saídas de build, não arquivos adicionados ao commit.

Logs desta execução e harness temporário estão em `/home/usuario/Documents/Codex/2026-11-12/spamblocker-brasil-auditoria/work/`: `unit-tests.log`, `build-lint.log`, `other-flavors-build.log`, `probes.py`, `probes.log`, `baseline.json` e `inventory.json`. As evidências essenciais e entradas de reprodução estão transcritas aqui para que o plano seja utilizável sem o chat.

## 4. Classificação e ordem de trabalho

- **D — defeito confirmado:** efeito determinístico demonstrado por execução isolada ou fluxo inequívoco de código. Quando não houve execução no Android, isso está explícito.
- **R — risco:** condição perigosa existe no código, mas ocorrência/impacto final depende de ambiente ou configuração ainda não exercitada.
- **S — sugestão:** melhoria com benefício identificado, sem falha funcional demonstrada.
- **L — limitação:** validação necessária ainda ausente.
- **P1/alta:** tratar primeiro, antes de ampliar uso/distribuição. **P2/média:** corrigir em manutenção próxima. **P3/baixa:** evolução planejada. Não foi atribuído P0 sem evidência de incidente crítico.

| ID | Prioridade / severidade | Classe | Tema |
|---|---|---|---|
| AUD-001 | P1 / alta | D | Credenciais e conteúdo sensível em logs persistentes |
| AUD-002 | P1 / alta | R | Serviço SMS exportado sem autorização e com efeitos reais |
| AUD-003 | P1 / alta | R | Callback OAuth com estado fixo e sem sessão pendente |
| AUD-004 | P1 / alta | D | Simulador alcança denúncias e automações reais |
| AUD-005 | P1 / alta | R | Prazo global de triagem de chamada não garantido |
| AUD-006 | P1 / alta | R | Restore destrutivo sem atomicidade e sem propagar falhas |
| AUD-007 | P2 / média | D | SMS sem chave de SIM encerra o processamento |
| AUD-008 | P2 / média | D | CSV com BOM, aspas ou campo final vazio perde conteúdo |
| AUD-009 | P2 / média | D | Parser de cabeçalhos HTTP trunca valores |
| AUD-010 | P2 / média | D | Cancelar exportação pode prender diálogo de progresso |
| AUD-011 | P2 / média | D | Gravação SAF nula é reportada como sucesso |
| AUD-012 | P2 / média | R | Permissões/papel em cache podem divergir do Android |
| AUD-013 | P2 / média | D | Painel apresenta regra de contatos como resultado absoluto |
| AUD-014 | P2 / média | D | Controles compartilhados sem semântica completa de acessibilidade |
| AUD-015 | P2 / média | R | Importações deixam regex/agenda inválidas alcançar o motor |
| AUD-016 | P2 / média | D | Valores de MMS interpolados em expressão SQL |
| AUD-017 | P2 / média | R | Rede sem timeout, limite de leitura ou cancelamento efetivo |
| AUD-018 | P2 / média | R | Backup/exportação podem transportar segredos e históricos |
| AUD-019 | P2 / média | R | Consultas repetidas à agenda no caminho crítico |
| AUD-020 | P2 / média | D | Histórico pesquisado recebe itens fora do filtro; cache fica antigo |
| AUD-021 | P2 / média | D | Gate de lint falha; alertas exigem triagem individual |
| AUD-022 | P2 / média | D | Workflow antigo usa caminhos incompatíveis com flavors |
| AUD-023 | P2 / média | D | Denúncia por domínio faz cast de Boolean para HttpRequest |
| AUD-024 | P3 / baixa | S | Cobertura, isolamento de testes e manutenção do fork |

Ordem sugerida para uma **futura** frente: (1) AUD-001 a 004; (2) AUD-005/017/019 e AUD-007/015; (3) AUD-006/008/009/010/011/016/018/023; (4) AUD-012/013/014/020; (5) AUD-021/022/024. AUD-021 deve rodar como gate a cada lote; não esperar o último lote para detectar regressões. Documentar decisões de política antes de alterar comportamento de bloqueio ou integrações.

## 5. Achados e instruções de correção futura

Os caminhos abreviados `service/`, `util/`, `ui/`, `db/` e `config/` nesta seção são relativos a `app/src/main/java/spam/blocker/`. Linhas referem-se ao estado auditado; localizar também pelos símbolos, pois números mudam após correções.

### AUD-001 — Credenciais e SMS copiados para logs

**P1 / alta · D · segurança/privacidade · confiança alta.** `service/bot/Actions1.kt:226–250` (`HttpRequest.execute`) registra URL, cada valor de header depois de resolver autenticação e corpo POST. `util/Logger.kt` (`SaveableLogger`), `Checker.checkCall/checkSms`, `CallScreeningService.logToHistoryDb` e `AutoReportSpam.updateRecordReportLog` persistem esses logs. O preset PhoneBlock usa `bearer_auth(shared_pref(...))`. A configuração de não salvar o corpo SMS no campo `extraInfo` não impede que o corpo HTTP contendo SMS apareça em `fullScreeningLog`.

**Problema/consequência:** a trajetória até a persistência é explícita; nenhum segredo real foi lido ou exibido nesta auditoria. Ao executar uma API configurada, um token pode aparecer no histórico, exportação e diagnóstico. Vazamento para terceiros não foi observado.

**Alternativas/recomendação:** desligar todos os logs perde diagnóstico; mascarar somente `Authorization` deixa cookies, query strings e corpos expostos. Recomenda-se redigir valores sensíveis antes de qualquer sink, com logs estruturados de status/duração e inclusão de payload apenas mediante política explícita.

**Passos:** 1. Inventariar headers e tags de autenticação usados pelos presets. 2. Criar sanitização central de URL/header/corpo/erro; não depender de `DEBUG`. 3. Usar a mesma sanitização em logger de UI, persistência e exportação. 4. Oferecer limpeza deliberada dos logs antigos após informar o usuário; não apagá-los automaticamente nesta frente. **Regressão/aceite:** enviar somente um token sentinela e SMS fictício para servidor local; nenhum sink, backup ou exceção deve conter esses valores, mantendo status e duração úteis.

### AUD-002 — Serviço público SMS sem autorização

**P1 / alta · R · IPC/permissões · confiança alta na exposição, média no impacto.** `app/src/main/AndroidManifest.xml:137–145` exporta `PublicSMSScreeningService` sem permissão; `service/PublicSMSScreeningService.kt:35–91` não valida UID/pacote autorizado, usa `isTest=false` e chama `SmsReceiver.processSms`. O callback roda em `Handler` principal e `Runnable.run()` não cria outra thread. O fluxo pode escrever histórico, atualizar SMS Alert, executar preprocessadores e denunciar mensagens conforme configuração, mesmo sem passar pelas verificações globais de `SmsReceiver.onReceive`.

**Condição/impacto:** outro aplicativo que consiga bindar pode fornecer número/corpo artificiais. As consequências dependem de regras, permissões e APIs habilitadas; não foi feito ataque Binder, envio SMS ou chamada externa. Há também risco de bloqueio da UI pelo processamento síncrono.

**Alternativas/recomendação:** `exported=false` é apropriado se integração não for necessária; permissão de assinatura quebra consumidores de terceiros. Para manter integração, adotar habilitação explícita, autorização persistida por identidade de pacote/assinatura e classificação sem efeitos por padrão.

**Passos:** 1. Definir consumidores suportados. 2. Verificar a identidade do emissor da mensagem, não só UID do thread do Handler. 3. Validar tamanho/tipos e limitar requisições. 4. Separar consulta pura de ingestão autorizada. 5. Processar fora da main com prazo. **Testes/aceite:** app auxiliar em emulador descartável, autorizado e não autorizado; o segundo não obtém decisão nem produz histórico/alerta/denúncia; modo global desligado respeitado conforme contrato; UI permanece responsiva.

### AUD-003 — Retorno OAuth sem correlação de sessão

**P1 / alta · R · autenticação · confiança alta no código.** `ui/AuthCallbackActivity.kt:34–40` aceita host `auth` e grava `loginToken` quando `state` corresponde a uma constante pública; `ui/setting/api/ApiPresets.kt:63–68` define `PhoneBlock_OAuth_State`. Não há nonce aleatório, prazo, consumo único ou verificação de login pendente. A Activity é exportada.

**Condição/impacto:** um Intent direcionado à Activity com esse host/estado e token fictício alcança a atribuição, mesmo sem iniciar login. Substituição de credencial, interrupção do acesso ou confusão de conta são riscos; não se demonstrou roubo de conta no provedor.

**Alternativas/recomendação:** validar apenas scheme/host não corrige a correlação. Recomenda-se nonce criptográfico por tentativa, validade curta e uso único; avaliar App Links/PKCE com o contrato efetivo do provedor, sem presumir suporte.

**Passos:** 1. Criar estado pendente antes de abrir o navegador. 2. Validar URI completa, nonce, prazo e tentativa ativa. 3. Consumir o estado atomicamente antes de salvar a credencial. 4. Tratar repetição, cancelamento e morte do processo; não registrar token. **Testes/aceite:** callbacks ausente/errado/expirado/repetido, scheme diferente e chamada sem sessão não mudam preferências; um retorno válido funciona uma única vez. A execução no Android permanece pendente.

### AUD-004 — Simulação com efeitos de produção

**P1 / alta · D por fluxo de código · integridade/UX · confiança alta.** `ui/setting/Testing.kt:155–170` chama `processCall/processSms` com `isTest=true`. Esse sinal não existe em `ActionContext` e não chega aos preprocessadores de `Checker.kt:1777/1844` nem aos bots posteriores de chamada (`CallScreeningService.kt:307–319`). `AutoReportSpam.kt:139` pode denunciar imediatamente sem consultar `isTest`; a via agendada permite teste quando `BuildConfig.DEBUG` (`:233`). `SmsReceiver.kt` também permite denúncia em debug. A RC2 distribuída é debug.

**Condição/impacto:** com bot/denúncia habilitados e permissões concedidas, “Testar um número” pode escrever configurações, arquivos, enviar HTTP ou SMS e agendar denúncias. Não foi disparado nenhum desses efeitos; não se afirma que estejam habilitados no telefone do usuário.

**Alternativas/recomendação:** ocultar o simulador elimina recurso útil; aviso isolado é insuficiente. Recomenda-se modo de execução explícito `simulation/live` que atravesse toda a cadeia, com adaptadores de efeitos simulados e relatório do que seria feito.

**Passos:** 1. Propagar modo de execução em contextos de checker/ações/denúncia. 2. Bloquear efeitos externos e mudanças persistentes no modo simulado, independentemente do build. 3. Separar qualquer teste de integração real em comando claramente distinto. 4. Preservar histórico de simulação somente se essa for a política declarada. **Testes/aceite:** sentinelas para SMS, HTTP, arquivos, preferências e WorkManager recebem zero efeitos reais em ambas as variantes debug/release; decisões de classificação permanecem equivalentes.

### AUD-005 — Ausência de prazo global de chamada

**P1 / alta · R · disponibilidade/ciclo de vida · confiança alta na lacuna.** `CallScreeningService.kt:128–186` executa toda a triagem antes de responder; não há watchdog global nem resposta de contingência para exceções. `Checker.InstantQuery` limita somente a corrida de APIs a aproximadamente 4,5 s (`Checker.kt:979–1004`). Pré-processadores, agenda, regex, banco e bots de ringtone estão fora dessa garantia; `Actions3.kt:1737–1742` permite `Wait` com `Thread.sleep`.

**Condição/impacto:** um bot de entrada com espera acima de cinco segundos, provider lento ou regex cara pode atrasar a resposta. O Android ignora respostas após o prazo de cinco segundos, segundo a [documentação de CallScreeningService](https://developer.android.com/reference/android/telecom/CallScreeningService). Não se mediu atraso de chamada física.

**Alternativas/recomendação:** simplesmente aumentar o timeout local é inviável. Recomenda-se orçamento monotônico de ponta a ponta desde o callback, resposta única protegida por estado e contingência explícita aprovada; mover persistência/efeitos pós-decisão para fora do caminho crítico. Cancelamento de coroutine sozinho não interrompe I/O bloqueante.

**Passos:** 1. Instrumentar etapas sem PII. 2. Definir margem abaixo do limite da plataforma. 3. Aplicar limites a cada etapa e impedir `Wait`/ações incompatíveis na triagem. 4. Garantir resposta única em sucesso, exceção e expiração. 5. Associar escopos ao ciclo de vida. **Testes/aceite:** atrasos sintéticos por etapa, exceções e chamadas concorrentes; todas recebem uma resposta dentro do orçamento; confirmar em aparelho/doze depois, sem transformar teste de emulador em aceite físico.

### AUD-006 — Restore pode deixar estado parcial e indicar sucesso

**P1 / alta · R · integridade de dados · confiança alta.** `config/Config.kt:743–875,1067–1120` limpa tabelas antes de inserir e aplica categorias/preferências sequencialmente. `db/BasicTable.kt:31–79` retorna texto de erro em `addAll` e não verifica o retorno de `insert` em `addWithId`; chamadores de restore ignoram esses resultados. `ui/setting/misc/BackupRestore.kt:385–399` marca sucesso após `apply`, sem tratar a cadeia como operação atômica. `NaiveBayes.apply` mantém o arquivo de modelo anterior quando o backup não contém modelo.

**Condição/impacto:** disco cheio, registro inválido, IDs duplicados ou falha de canal podem produzir restauração parcial; modelo antigo pode sobreviver a amostras novas. Perda de dados não foi provocada nesta auditoria.

**Alternativas/recomendação:** transação apenas por lote não protege a limpeza anterior nem preferências/arquivos/canais Android. Recomenda-se validar tudo antes de alterar, transação única para o conjunto SQLite e plano de recuperação para os demais recursos.

**Passos:** 1. Validar versão, limites, IDs, regex e referências. 2. Capturar estado recuperável e pausar ingestão durante a troca. 3. Propagar todas as falhas e transacionar o banco. 4. Aplicar preferências/modelo/canais com rollback ou journal; definir ausência de modelo explicitamente. 5. Disparar `configImported` apenas após consistência verificada. **Testes/aceite:** falha injetada em cada etapa conserva estado anterior ou restaura snapshot; nenhum sucesso falso; backup válido mantém regras, canais e comportamento de classificação após reinício.

### AUD-007 — SMS sem slot lança exceção

**P2 / média · D executado em helper isolado · robustez · confiança alta.** `service/SmsReceiver.kt:36–52`, `getSimSlotFromSmsIntent`, usa `first { slot != -1 }` e captura apenas `SecurityException`. Bundle existente sem nenhuma das nove chaves produz `NoSuchElementException` (E05). A função é usada também em MMS.

**Problema/consequência:** informação opcional ausente encerra processamento em vez de retornar slot desconhecido. **Alternativas/recomendação:** capturar a exceção é possível, mas busca explícita com `firstOrNull/mapNotNull` e validação do intervalo evita usar exceção como fluxo.

**Passos:** 1. Tratar Intent/extras ausentes e tipo inválido. 2. Retornar `null` sem correspondência. 3. Diferenciar subscription ID de índice de slot; definir política para fabricante e slot desconhecido. **Testes/aceite:** Bundle vazio, chave não reconhecida, slot 0/1, -1, valor fora da faixa, múltiplas chaves e null; nenhuma exceção; regra por SIM não deve se tornar universal sem política explícita. A prova isolada não substitui teste de broadcast Android.

### AUD-008 — CSV corrompe três entradas válidas

**P2 / média · D executado com a classe original · importação · confiança alta.** `util/Csv.kt:37–42,96–99,118–125`; `ui/setting/regex/ImportCsv.kt:48–62` e `Actions1.ParseCSV` consomem o resultado. `Reader` já decodificou BOM em um caractere, mas `skipBOM` descarta outros dois. Aspas escapadas são removidas; coluna final vazia desaparece no EOF.

**Evidência:** `\uFEFFpattern,description\n123,ok` → cabeçalho `ttern`; `pattern,description\n123,` → linha com um campo; `pattern,description\n123,"a""b"` → `ab`, esperado `a"b`. Isso rejeita planilhas válidas e altera regras/textos importados.

**Alternativas/recomendação:** biblioteca CSV consolidada reduz casos de borda, mas acrescenta dependência; corrigir a máquina de estados existente é viável se o contrato de delimitadores for mantido e testado.

**Passos:** 1. Consumir apenas `\uFEFF`. 2. Distinguir fechamento e escape de aspas. 3. Preservar campos vazios no final e validar aspas não fechadas. 4. Definir cabeçalho entre aspas, CRLF e delimitadores suportados. **Testes/aceite:** três casos acima, vazio, somente BOM, multiline, CRLF, `;`/`|`, aspas malformadas e mapeamento; importação mantém exatamente os campos e não grava lote parcialmente inválido.

### AUD-009 — Cabeçalho HTTP trunca valores que contêm dois-pontos

**P2 / média · D executado na expressão original · integração · confiança alta.** `service/bot/Actions1.kt:184–198`, `splitHeader`: `val (key,value)=resolved.split(":").map { it.trim() }`. E05: `Referer: https://example.invalid:8443/a` vira `Referer → https`; `X-Label: a:b` vira `a`; linha sem separador lança `IndexOutOfBoundsException`.

**Consequência:** requisição diferente da configurada, falhas de autenticação/roteamento e diagnóstico enganoso. **Alternativas/recomendação:** representação estruturada seria mais segura, mas divisão no primeiro `:` preserva o formato atual com menor migração.

**Passos:** 1. Separar com limite 2. 2. Validar nome não vazio, CR/LF e linha inválida com erro compreensível. 3. Tratar User-Agent sem depender de caixa exata. 4. Aplicar AUD-001 aos logs. **Testes/aceite:** valores com URL, porta, hora, dois-pontos, espaços e vazios; bytes enviados a endpoint local correspondem ao configurado; erro não inclui credenciais.

### AUD-010 — Cancelamento da exportação deixa modal aberto

**P2 / média · D por fluxo de código · UI/erro · confiança alta.** `ui/setting/misc/BackupRestore.kt:185–241`: ao incluir banco de spam, `progressTrigger=true`; diálogo não pode ser dispensado manualmente; flag é limpa somente dentro de `if (uri != null)`. Cancelar o seletor retorna null e não fecha o progresso. Exceção na preparação também não tem finalização garantida.

**Alternativas/recomendação:** permitir fechar modal mascara operação ainda ativa; usar estado explícito de preparação/seleção/gravação/cancelamento e `finally`.

**Passos:** 1. Separar preparação IO do lançamento do seletor na main. 2. Limpar progresso no cancelamento e em qualquer falha. 3. Não marcar cancelamento como sucesso. 4. Restaurar estado após recriação. **Testes/aceite:** exportar com SPAM_NUMBERS, cancelar no seletor, repetir, provocar falha de preparação e girar tela; usuário volta a navegar sem reiniciar o processo. Reprodução interativa ainda pendente.

### AUD-011 — SAF sem stream é sucesso falso

**P2 / média · D por retorno de código · arquivos · confiança alta.** `util/FileUtils.kt:49–58` retorna null (sucesso) mesmo quando `openOutputStream` retorna null; `:99–135` ignora `createDocument`/stream nulos. `Actions1.WriteFile` retorna true ao terminar essa função.

**Consequência:** exportação/automação pode anunciar gravação sem arquivo completo. **Alternativas/recomendação:** retorno Boolean perde contexto; usar resultado explícito com erro por URI/stream ausente e verificar conclusão.

**Passos:** 1. Tornar falhas nulas explícitas. 2. Propagar falha até UI e bot. 3. Preferir escrita temporária e troca quando provider suportar; documentar quando não houver atomicidade. 4. Não apagar destino anterior antes de ter recuperação. **Testes/aceite:** provider falso com null, IOException, quota e URI revogada; sucesso somente com tamanho/conteúdo relido esperado; cancelamento não escreve.

### AUD-012 — Estado de permissões pode ficar obsoleto

**P2 / média · R · lifecycle/UX · confiança média-alta.** `util/Permission.kt:58,359–367` mantém `isGranted` em memória e inicializa em `App.onCreate`; `PermissionChain.checkNext` confia no cache. Callbacks atualizam permissões pedidas pelo próprio fluxo, mas não há refresh geral no retorno da Activity. `ProtectionScreen.kt:34–35` usa esse cache para “PROTEÇÃO ATIVA”. `SimUtils.kt:29–36` consulta assinatura após checagem do cache, sem captura local de `SecurityException`.

**Condição/impacto:** alteração de papel/permissão fora do app, sem morte do processo, pode manter estado visual ou decisão desatualizados. Alguns revogamentos matam o processo; não assumir esse comportamento para todos os papéis/OEMs.

**Alternativas/recomendação:** polling constante custa energia; refresh no resume e checagem real em fronteiras sensíveis são mais adequados.

**Passos:** 1. Atualizar papéis/permissões no resume. 2. Revalidar ao iniciar operação sensível, tratando corrida de revogação. 3. Não religar proteção automaticamente. 4. Reconciliar `G`/SharedPreferences/estado do Android. **Testes/aceite:** trocar CALL_SCREENING em configurações, negar e conceder contatos, retornar ao app, recriar processo; estado correto e ausência de crash. Reteste Android pendente.

### AUD-013 — Resumo de contatos ignora precedência

**P2 / média · D por comparação de condições · UI/clareza · confiança alta.** `ui/brasil/ProtectionScreen.kt:61–67` mostra “Permitidos”/“Bloqueados” apenas por preferência de contatos e permissão. O motor (`Checker.kt:1780–1789`) ordena todos os checkers por prioridade e aceita a primeira decisão.

**Reprodução lógica:** contatos ligados, prioridade permitir 10; regra explícita de bloqueio para um contato com prioridade 100. Painel continua “Contatos da agenda: Permitidos”, mas o motor bloqueia. Uma exceção permitir desconhecido acima de `strictPriority` produz discrepância inversa. O texto introdutório “fazem parte” não explica a exceção nas linhas categóricas.

**Alternativas/recomendação:** calcular todos os resultados possíveis é caro e impreciso. Preferir “Permitidos pela regra de contatos; regras prioritárias podem prevalecer” e link para prioridades/conflitos.

**Passos:** 1. Definir texto de política, separando regra configurada de decisão final. 2. Indicar exceções/prioridades quando relevantes. 3. Manter teste de número acessível após AUD-004. 4. Mover textos para recursos. **Testes/aceite:** contato com bloqueio prioritário, desconhecido com permissão prioritária, agenda negada e proteção pausada; nenhuma promessa absoluta incompatível com motor.

### AUD-014 — Acessibilidade incompleta nos controles compartilhados

**P2 / média · D na semântica; impacto TalkBack pendente · acessibilidade · confiança alta/média.** `ui/widgets/Balloon.kt:126–139` cria ajuda apenas por imagem/click; `Resource.kt` fornece `contentDescription=""`; `CheckBox.kt:27–43` usa `clickable` no pai e Checkbox sem ação, sem `toggleable` no grupo; `StrokeButton.kt:54–68` não declara papel de botão. `BrasilSection` exibe `+`/`−` sem estado expandido explícito. A variante Brasil já melhorou Switch e navegação com semântica, portanto não se trata de ausência geral de acessibilidade.

**Consequência:** controles podem ser anunciados sem propósito/estado, sobretudo ajuda e expansores. Dimensões pequenas no código não bastam para provar tamanho efetivo de toque, pois Compose pode expandi-lo.

**Alternativas/recomendação:** rótulos genéricos como “Opção” não distinguem controles. Preferir componentes Material ou semântica equivalente com nome específico, papel, estado e ações.

**Passos:** 1. Rotular ajuda pelo assunto. 2. Usar `toggleable`/`selectable` e papéis adequados. 3. Expor expandido/recolhido e evitar foco duplicado decorativo. 4. Medir áreas reais de toque, ordem/foco e contraste de estados desabilitados. **Testes/aceite:** semântica automatizada e TalkBack por Proteção/Ajustes/backup/regras/histórico, fonte 2,0, 320dp, paisagem e teclado; cada controle identificável e acionável, sem sobreposição de alvos.

### AUD-015 — Importações sem validação suficiente para o motor

**P2 / média · R · robustez/segurança de entrada · confiança alta.** `ui/setting/regex/ImportCsv.kt:60–103` monta `RegexRule.fromMap` e grava; `Configs.apply` não valida regras. Editor manual verifica erros, mas essa proteção não cobre restore/CSV. `util/Util.kt:149–151` compila regex textual sem catch apesar do comentário; `db/RegexTable.kt:138–145` e `Checker.checkQuickCopy` usam compilação direta. `TimeSchedule.parseFromStr:105–120` não limita dia/hora e chama `digitToInt` sem tratar caractere inválido.

**Condição/impacto:** importar regex `[` ou agenda `x,00:00-01:00,true` pode levar exceção ao processamento/exibição; regex válida mas custosa pode atrasar triagem. Crash Android ou ataque de complexidade não foram executados.

**Alternativas/recomendação:** catch global evita parte do crash mas oculta regra defeituosa. Recomenda-se validação única reutilizada por UI, importações e restore, mais proteção no motor para dados antigos inválidos.

**Passos:** 1. Validar regex, flags, agenda, SIM e limites antes de gravar. 2. Rejeitar lote com localização do erro, sem ecoar dados sensíveis. 3. Definir política explícita para regra antiga inválida. 4. Limitar tamanho/complexidade e medir casos patológicos sem bloquear thread principal. **Testes/aceite:** entradas acima, dia 0/8, hora 99, flags desconhecidas e lote misto não comprometem regras anteriores; nenhuma exceção escapa do screening.

### AUD-016 — Aspas em identificador MMS quebram seleção

**P2 / média · D em SQLite equivalente; impacto no provider pendente · dados · confiança alta.** `service/WapPushReceiver.kt:142–149` monta `"$colName='$colValue'"` com transactionId/contentLocation oriundos do PDU e `selectionArgs=null`. E05: valor sintético `abc'def` produz erro de sintaxe; seleção parametrizada retorna a linha correta.

**Consequência:** falha ou seleção alterada no contexto das permissões já concedidas. Não há prova de acesso além das permissões Android, nem foi explorado provider real.

**Alternativas/recomendação:** escapar aspas manualmente é frágil; usar coluna de uma lista fixa e `selectionArgs`.

**Passos:** 1. Manter nomes de coluna fechados no código. 2. Passar valor via `?`. 3. Tratar PDU/extras/from ausentes e exceção do provider sem derrubar processo. 4. Rever `CoroutineScope(IO)` do receiver sem `goAsync`, pois trabalho continua até 20 s após `onReceive` retornar. **Testes/aceite:** apóstrofo, Unicode, valor longo e campos ausentes em provider/fixtures isolados; consulta exata, tarefa finalizada dentro do contrato do receiver, nenhuma leitura adicional indevida.

### AUD-017 — Transporte HTTP sem limites de recursos

**P2 / média · R · disponibilidade/rede · confiança alta.** `util/Http.kt:41–84` não configura `connectTimeout/readTimeout`, lê resposta integral com `readBytes` e só desconecta ao sair do bloco. `util/race.kt` cancela jobs, mas I/O bloqueante pode continuar; os corpos de erro também são ilimitados. `HttpRequest` limita somente texto de log depois de carregar a resposta. Manifesto permite tráfego HTTP claro globalmente.

**Condição/impacto:** endpoint lento, resposta muito grande ou rede interrompida pode reter threads/memória. No Android, timeout zero significa espera sem limite: [URLConnection](https://developer.android.com/reference/java/net/URLConnection). A corrida de APIs já possui prazo de retorno; o problema de recursos remanescentes não deve ser confundido com ausência total de timeout nessa corrida.

**Alternativas/recomendação:** manter HttpURLConnection com limites e cancelamento explícito, ou adotar cliente já compatível com cancelamento; não trocar biblioteca apenas por preferência.

**Passos:** 1. Definir limites por consulta e download de base. 2. Aplicar deadline e tamanho máximo também em erros. 3. Desconectar em cancelamento, fechar writer e tratar charset. 4. Exigir HTTPS por padrão; exceções locais/HTTP precisam ser explícitas e nunca receber credenciais inadvertidamente. **Testes/aceite:** servidor local lento, sem término, resposta excessiva, redirecionamento e cancelamento repetido; threads/conexões retornam ao patamar inicial, resultado diagnosticável e sem segredo.

### AUD-018 — Backup inclui dados sensíveis sem política clara

**P2 / média · R · privacidade/configuração · confiança alta.** Manifesto `allowBackup=true`; `res/xml/backup_rules.xml` e `data_extraction_rules.xml` não excluem dados. `config/Config.kt:880–934` inclui API_TOKENS na seleção padrão e `toByteArray` apenas comprime JSON com GZIP. APIs/bots também podem conter credenciais em headers, mesmo quando API_TOKENS é desmarcado. `Docs/PRIVACY POLICY.md` afirma armazenamento apenas no dispositivo e omite o conteúdo SMS que ações HTTP podem enviar.

**Condição/impacto:** backup do sistema pode incluir preferências/banco e exportação compartilhada pode conter segredos. A documentação oficial confirma a [inclusão padrão de preferências e bancos no Auto Backup](https://developer.android.com/identity/data/autobackup). Não foi observado upload nem vazamento; backup Android tem proteções próprias e não deve ser descrito como arquivo público sem criptografia.

**Alternativas/recomendação:** excluir tudo prejudica recuperação; preferir categorias claras, tokens fora da exportação padrão, redigir segredos embutidos e exclusões específicas nos dois formatos de backup. Exportação com segredos exige opção explícita e proteção definida.

**Passos:** 1. Mapear todos os locais de segredo/PII, inclusive logs. 2. Separar referências de credencial de definições compartilháveis. 3. Configurar cloud backup/D2D conforme política. 4. Atualizar política do fork com comportamento real, sem afirmar certificações legais não verificadas. **Testes/aceite:** backup/exportação com sentinelas, API_TOKENS desmarcado, API/bot com header secreto; nenhuma sentinela proibida no resultado; restauração solicita reautenticação quando previsto.

### AUD-019 — Agenda completa percorrida repetidamente

**P2 / média · R · desempenho · confiança alta na operação, média no impacto.** `util/Contact.kt:111–146` lê todas as linhas de `Phone.CONTENT_URI` quando lookup completo falha. `Checker.Contact` e `NonContact` fazem consultas separadas, e notificação/relatório podem repetir. `HistoryCard.kt:94` também faz lookup durante composição. A regra de fallback Brasil é intencional e os quatro testes puros passaram; não se encontrou evidência que justifique remover DDD de contatos completos.

**Consequência possível:** agenda grande/provider lento aumenta latência de chamada e rolagem; desempenho real ainda não medido. **Alternativas/recomendação:** cache global sem invalidação introduz nomes/decisões antigos. Preferir cache por evento de triagem e índice local com invalidação somente se a medição justificar.

**Passos:** 1. Medir 0/1.000/10.000 entradas sintéticas e duplicatas. 2. Reutilizar resultado por chamada/SMS. 3. Tirar leitura da composição para estado assíncrono. 4. Preservar lookup completo primeiro, validação BR, ausência de DDD no número salvo e rejeição de contatos ambíguos. **Testes/aceite:** mesmos resultados funcionais, número de consultas reduzido, atualização/revogação refletidas; orçamento compatível com AUD-005, sem alegar limiar medido antes da medição.

### AUD-020 — Busca do histórico e cache não acompanham eventos

**P2 / média · D por fluxo determinístico · UI/estado · confiança alta.** `ui/history/HistoryViewModel.kt:55–59` retorna true quando busca ativa recebe `filter=null`. `App.kt` (`listenToNewCallSMS`) chama `isVisible` sem filtro e insere o novo registro, permitindo item que não corresponde à pesquisa. `ContactsCache` (`util/Contact.kt:39–49`) guarda inclusive null, sem invalidação; `HistoryCard` lembra o contato apenas por número.

**Reprodução:** buscar “Alice”; chegada de registro “Bob” é inserida porque o filtro não foi passado. Renomear/adicionar contato após lookup pode manter nome/ausência antigos até reinício ou recriação apropriada. Não se trata de cache de autorização no motor de chamadas, que usa lookup direto.

**Alternativas/recomendação:** recarregar tudo a cada evento resolve parte do problema, mas é caro. Centralizar predicado atual e invalidar cache com mudanças da agenda/permissão, evitando dependência de parâmetro opcional ambíguo.

**Passos:** 1. Fazer `isVisible` sempre considerar a consulta ativa. 2. Aplicar o mesmo predicado em reload e inserção incremental. 3. Invalidar/recarregar nomes quando agenda muda. 4. Evitar índices capturados que fiquem obsoletos com novas linhas. **Testes/aceite:** chegada compatível/incompatível durante busca, exclusão/undo, renomeação de contato e grant posterior atualizam resultados sem reiniciar.

### AUD-021 — Lint não passa e precisa de triagem sem correção cega

**P2 / média · D na verificação; riscos por regra · qualidade/compatibilidade · confiança alta.** E03 reproduziu exatamente **24 erros / 107 avisos / 2 hints**. Os 13 arquivos de localização principal dos erros são idênticos à base. Incluindo a localização secundária de `StringFormatInvalid`, são 14 arquivos; isso não contradiz a contagem histórica de 13.

| Grupo de erros | Quantidade | Tratamento recomendado |
|---|---:|---|
| `LocalContextGetResourceValueCall` | 18 | Diferenciar leitura composable de callback; usar recursos sensíveis à configuração e chave correta de remember |
| `MissingPermission` | 3 | SimUtils tem guarda em cache, requer rechecagem/erro; duas ocorrências em PduPersister precisam confirmar alcançabilidade antes de alegar crash |
| `MissingQuantity` | 2 | Completar plural árabe `zero`, preservando tradução/contexto |
| `StringFormatInvalid` | 1 | Candidato a falso positivo: `answered_warning` é argumento de `help_answered`, não o format principal; 18 formatos passaram em JVM |

Avisos incluem 21 leituras de recursos via Context, 28 quantidades não usadas, 11 typos, quatro usos de dimensões Configuration no código Brasil/MainActivity e dependências. `ExportedService` está coberto em AUD-002. **Versão mais nova disponível não comprova bug, vulnerabilidade ou necessidade de atualização imediata.** O parser `formatAnnotated` reconhece apenas `%s`; não converter mecanicamente todas as traduções para `%1$s` sem ajustar/testar esse contrato.

**Alternativas/recomendação:** não desativar lint inteiro nem adicionar baseline amplo para esconder falhas. Classificar cada ocorrência, corrigir defeitos reais e usar supressão estrita somente com evidência e justificativa do falso positivo.

**Passos:** 1. Manter inventário XML por ID/local. 2. Corrigir grupos pequenos com teste de idioma/recriação/API. 3. Reproduzir `StringFormatInvalid` com recurso Android antes de decidir supressão. 4. Avaliar dimensões por janela e insets em multitarefa. **Testes/aceite:** lint retorna zero erros não justificados, warnings novos são revisados, mudanças de idioma/rotação não mostram recurso antigo; catálogo não atualizado em bloco sem validação.

### AUD-022 — Pipeline antigo e fonte da RC2 não são reproduzíveis só por Git

**P2 / média · D na configuração · build/entrega · confiança alta.** `.github/workflows/latest.yml:33–41` usa `assembleRelease` e depois procura `app/build/outputs/apk/release/app-release.apk` e mapping sem flavor; a configuração atual cria diretórios por flavor. `new_release.yml` publica F-Droid/Google Play, tag fixa v5.18; `test.yml` é manual, faz checkout da branch `test` e build, sem executar a suíte/lint Brasil. A fonte Brasil ainda está fora dos commits; `local.properties` continua rastreado apesar do `.gitignore`.

**Consequência:** workflow `latest` não encontra a saída esperada; não existe gate demonstrado para RC2 Brasil. Não se executou workflow remoto nem se presumiu acesso ao repositório upstream.

**Alternativas/recomendação:** manter workflows upstream arquivados pode ser suficiente para fork local; se houver publicação própria, criar pipeline explícito do fork com caminhos/flavors corretos. Não publicar no remoto upstream por inferência.

**Passos:** 1. Decidir repositório/canal de manutenção do fork. 2. Em frente autorizada, versionar mudanças Brasil preservadas e retirar configuração de máquina do índice, sem apagar arquivo local. 3. Definir teste/lint/build por variante e artefato esperado. 4. Definir assinatura de continuidade; não trocar certificado sem plano de migração. 5. Preparar release sem `DEBUG` e validar efeitos alterados por esse flag (AUD-004). **Testes/aceite:** checkout limpo reproduz variante, pacote, versão e testes; workflow não usa caminhos inexistentes e não expõe segredos; upgrade preserva dados. Hash binário idêntico só deve ser exigido se build reprodutível for formalmente adotado.

### AUD-023 — Filtro de denúncia assume ação HTTP presente

**P2 / média · D por cast explícito · automação · confiança alta.** `service/reporting/AutoReportSpam.kt:322–327`: `firstOrNull { it is HttpRequest } ?: false`, seguido de `(http as HttpRequest).url`. APIs de denúncia podem usar SMS, conforme presets; uma API sem HttpRequest que chega a `domainFilter != null` provoca `ClassCastException` em vez de ser excluída do filtro.

**Condição/impacto:** precisa API de chamada habilitada sem ação HTTP, filtros anteriores satisfeitos e denúncia filtrada por domínio; esse caminho não foi executado contra APIs reais. **Alternativas/recomendação:** rejeitar configuração sem HTTP restringiria transportes válidos. Recomenda-se tratar domínio como opcional e excluir API sem domínio apenas quando esse filtro está presente.

**Passos:** 1. Usar seleção tipada `filterIsInstance<HttpRequest>().firstOrNull()`. 2. Retornar false para ausência de domínio no filtro. 3. Validar URL antes de extrair domínio. 4. Manter denúncia SMS sem filtro funcionando. **Testes/aceite:** mistura de APIs HTTP/SMS, ações vazias, domínio diferente/igual e URL inválida; nenhuma exceção e apenas destinatários pretendidos selecionados.

### AUD-024 — Melhorias de cobertura e manutenção do fork

**P3 / baixa · S · manutenibilidade/testabilidade · confiança alta.** Arquivos extensos (`Checker.kt` 1.917 linhas, `Actions3.kt` 1.786, `Config.kt` 1.148), estado global `G` e componentes com diversos checks de flavor aumentam o custo de testes. Existe somente uma classe em `src/test` com quatro testes. `RuleTest.setup` limpa SharedPreferences e tabelas sem restauração; `BrasilInterfaceTest` altera filtros sem restaurá-los. Formula tem 30 casos puros, mas está na suíte instrumentada. Parte da UI Brasil tem strings fixas em português, apesar da configuração de idioma existente.

**Alternativas/recomendação:** reescrita arquitetural total não é justificada. Preferir extrações pequenas em limites já identificados, fixtures isoladas e testes de comportamento.

**Passos:** 1. Criar variante/ambiente de teste descartável, sem contas nem dados do usuário; restaurar preferências que testes alterem. 2. Mover testes realmente puros para JVM. 3. Extrair execução de efeitos, validação e estado de permissão conforme os itens anteriores. 4. Consolidar recursos de texto/tokens de UI e formatar arquivos Brasil numa mudança separada. 5. Executar análise de dependências com fonte atual antes de registrar CVE; nenhuma CVE foi demonstrada aqui. **Testes/aceite:** suíte repetível duas vezes, sem dependência da ordem, sem tocar dados persistentes do usuário, cobrindo os defeitos corrigidos; cada refatoração mantém comportamento e passa variantes pertinentes.

## 6. Itens adiados e limitações explícitas

| ID | Item | Motivo / evidência parcial | Próxima validação e aceite |
|---|---|---|---|
| L01 | Chamada física do contato antes bloqueado | Pendente desde RC1/RC2; testes de lookup e simulação não a comprovam | Chamada real com logs redigidos, contato esperado toca; rejeições legítimas preservadas |
| L02 | Instrumentados, UI e hardware nesta rodada | `RuleTest` limpa preferências/tabelas; testes UI alteram estado. Uma consulta ADB listou emulador, a consulta seguinte não encontrou nenhum; não houve instalação nem execução instrumentada | Ambiente descartável usando configuração EmuDroid já preferida; confirmar alvo estável e executar suíte por classe |
| L03 | TalkBack, fonte 2,0, janela estreita, RTL e multitarefa | Quatro capturas antigas e inspeção estática não cobrem essas condições | Roteiro AUD-014/021 com gravação de estados e medidas de alvos/contraste |
| L04 | Parser XML/PDU e arquivos hostis | `Xml.parseXml:44–49` usa factory sem política explícita de entidades; código PDU de terceiros tem 31 Java no conjunto, sem fuzzing nesta rodada | Corpus limitado no Android suportado; verificar entidades/DTD e limites antes de declarar XXE ou exploração de PDU |
| L05 | Migrações de todos os bancos antigos | `DbSchemaTest` cobre caminho histórico específico, não todas as versões; não executado | Fixtures sintéticas por versão relevante, comparar dados além de colunas; nenhuma migração em banco real |
| L06 | Release/R8, assinatura e distribuição | Somente builds debug; RC2 usa certificado debug anterior, não há plano de produção aprovado | Build release isolado, validação de pacote/certificado/upgrade; publicação exige autorização própria |
| L07 | APIs/OAuth reais, SMS de saída e denúncias | Evitados efeitos em contas/telefones reais; provas e inspeção usam dados sintéticos | Primeiro dublês e endpoint local; teste contratado com conta de teste somente após correções e autorização pertinente |
| L08 | Limites de agenda, regex, histórico, rede e memória | Riscos identificados por operações sem limite, sem benchmark | Orçamento medido por etapa/tamanho; não aceitar metas de performance inventadas |
| L09 | Traduções completas e licença de toda dependência | XML bem formado e triagem lint; fontes incluem arquivos OFL existentes | Revisão linguística/RTL e inventário de licenças/SBOM antes de distribuição ampla |

Não se recomenda corrigir automaticamente: warnings de atualização de bibliotecas; suposto XXE sem teste do parser Android; `StringFormatInvalid` sem confirmar o recurso efetivamente formatado; `MissingPermission` em classe PDU sem confirmar caminho alcançável; cores/layout apenas por gosto. A ausência de escrita na agenda pelo fallback Brasil foi confirmada no código examinado e deve ser preservada.

## 7. Provas reproduzíveis e evidências transcritas

### E05 — Execuções isoladas sem produto modificado

O harness temporário compilou **os arquivos originais** `Formula.kt` e `Csv.kt` com o compilador Kotlin 2.4.20 já no cache. Extraiu literalmente `getSimSlotFromSmsIntent` e forneceu somente dublês mínimos de Context/Intent/Bundle; não simulou um broadcast real. Para cabeçalhos executou a expressão exata `line.split(":").map { it.trim() }` com destructuring. SQLite usou banco `:memory:` com tabela fictícia. Não houve rede, credencial, agenda ou dado real.

```text
SMS_MISSING_SLOT=NoSuchElementException
SMS_VALID_SLOT=1
HEADER=Referer -> https
HEADER=X-Label -> a
HEADER=IndexOutOfBoundsException
FORMULA_EXISTING_CASES=30 passed
CSV_HEADERS=[ttern, description]; ROWS=[[123, ok]]
CSV_HEADERS=[pattern, description]; ROWS=[[123]]
CSV_HEADERS=[pattern, description]; ROWS=[[123, ab]]
ANSWERED_HELP_FORMATS=18 passed
MMS_APOSTROPHE=near "def": syntax error
MMS_PARAMETERIZED=[(1,)]
```

Para reproduzir sem o harness, executar em scratch JVM os trechos a seguir, junto da classe `CSVParser` original:

```kotlin
val (key, value) = "Referer: https://example.invalid:8443/a".split(":").map { it.trim() }
check(value == "https") // demonstra o truncamento atual; valor esperado é URL inteira
val inputs = listOf("\uFEFFpattern,description\n123,ok", "pattern,description\n123,", "pattern,description\n123,\"a\"\"b\"")
inputs.forEach {
    val csv = CSVParser(PushbackReader(StringReader(it))).parse()
    println(csv.headers to csv.rows)
}
// No helper SMS original, fornecer extras existentes com getInt(chave,-1) == -1 para todas as chaves.
// first{...} lança NoSuchElementException; com slot=1 retorna 1.
```

```python
import sqlite3
db = sqlite3.connect(':memory:')
db.execute('CREATE TABLE mms (_id INT, tr_id TEXT)')
value = "abc'def"
db.execute('INSERT INTO mms VALUES (1, ?)', (value,))
# A consulta atual falha com OperationalError:
# db.execute(f"SELECT _id FROM mms WHERE tr_id='{value}'").fetchall()
assert db.execute('SELECT _id FROM mms WHERE tr_id=?', (value,)).fetchall() == [(1,)]
```

O primeiro rascunho da extensão do harness CSV teve erro de aspas no próprio código de teste; foi corrigido antes da execução válida acima. Não foi contabilizado como falha do aplicativo. As provas Formula executam os mesmos 30 pares entrada/esperado de `app/src/androidTest/java/spam/blocker/util/FormulaTest.kt`; não são 30 novos testes do projeto. A formatação usou os 18 recursos `help_answered` presentes, com argumento textual sintético e dois inteiros; prova Java parcial, não resolução de recursos Android.

### Fontes técnicas externas consultadas

Consultadas em 12/11/2026, apenas para contratos da plataforma; os achados do projeto vêm do código local:

- [CallScreeningService — prazo e resposta](https://developer.android.com/reference/android/telecom/CallScreeningService): sustenta o limite usado em AUD-005.
- [URLConnection — timeouts](https://developer.android.com/reference/java/net/URLConnection): sustenta o comportamento de espera em AUD-017.
- [Auto Backup — inclusão e exclusão de dados](https://developer.android.com/identity/data/autobackup): sustenta AUD-018; não prova que backup ocorreu no aparelho.
- [Uso inseguro de deep links](https://developer.android.com/privacy-and-security/risks/unsafe-use-of-deeplinks): orienta validação/correlação de AUD-003; App Links só se aplicam após definir domínio/contrato compatível.

## 8. Critérios de encerramento desta auditoria

1. Plano revisado, com distinção D/R/S/L, evidência, alternativas, implementação futura e aceite por item.
2. Estado consolidado e sessão no registro central atualizados pelo fluxo oficial; `registry.py validate` retorna `Registro valido`.
3. Comparação final dos 1.053 hashes iniciais sem alteração de conteúdo preexistente; somente este plano documental preparado para commit.
4. `git diff --check` e diff staged revisados; commit contém exclusivamente `docs/PLANO_AUDITORIA_CORRECOES.md`.
5. Sem correções de produto, branch de implementação, merge, push, publicação ou nova tarefa de correção. Próxima ação depende de pedido posterior do usuário.

O hash do commit é apresentado no relatório de encerramento e na sessão central; não é inserido recursivamente no próprio documento commitado.

Conclusão documental em 12/11/2026. A análise e o plano foram produzidos pelo GPT-6 Astra. Após interrupção da execução Astra, o GPT-6.1 Sol conferiu os 1.053 hashes iniciais, revisou o plano e concluiu sua gravação, o registro central e o commit autorizado. As verificações da seção 3 são da execução Astra.
