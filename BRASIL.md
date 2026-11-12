# SpamBlocker Brasil 5.18.1-rc.2

Base upstream v5.18, commit 1b2bf9ec90fd9c53a0b1d973e45d3675c77198b0.
Pacote spam.blocker.brasil, variante brasilDebug, código 51807.

A variante compara telefones brasileiros recebidos com DDD com contatos salvos
com 8/9 dígitos, depois da busca completa. Aceita fixos/móveis válidos e recusa
correspondências ambíguas entre contatos distintos. A comparação é compartilhada
pela identificação de chamadas e mensagens.

## Interface RC2

Nova tela Proteção com estado real da filtragem, permissão de contatos e acesso
ao teste de números. Navegação por Proteção, Chamadas, Mensagens e Ajustes, com
barra lateral em paisagem baixa. Temas claro e escuro em azul petróleo e verde,
Source Sans 3 e Space Grotesk, controles maiores, textos com contraste e seções
avançadas recolhidas. Histórico com estados escritos e nomes extensos expansíveis.

## Compilação e evidências

`./gradlew :app:testBrasilDebugUnitTest :app:assembleBrasilDebug :app:assembleBrasilDebugAndroidTest`

- Build concluído e quatro testes unitários passaram.
- Quatro testes de contatos no provider do emulador passaram.
- Quatro testes de interface passaram em retrato, com fonte 1,35 e em paisagem
  real 2280×1080. Cobrem navegação, diálogo, recriação e rolagem longa.
- Capturas de 12 estados em cada orientação estão em artifacts/ui-review-5.18.1-rc.2.
  Os históricos capturados contêm dados fictícios dos próprios testes.
- Contrastes medidos das combinações principais de texto ficaram acima de 4,5.
- Lint encontrou 24 erros, 107 avisos e duas sugestões. Todos os arquivos com
  erros são idênticos à tag v5.18. Há avisos também na interface nova, incluindo
  uso de Configuration.screenWidthDp/screenHeightDp, edit e catálogo de dependências.
- RC2 instalada no Samsung e papel CALL_SCREENING confirmado. A política antes
  e depois da atualização foi comparada e permaneceu igual. Tela Ajustes conferida
  visualmente. A revisão final foi concluída no emulador a pedido do usuário.
- O original foi removido a pedido do usuário na sessão anterior. Confirmação
  de ligação real do contato antes bloqueado continua pendente.

O emulador usa a definição EmuDroid de ~/.zshrc, com AllGateAPI31, GPU host,
Zink/RADV, Vulkan desabilitado e sem snapshot. Rotação feita com `adb emu rotate`.

O APK versionado e os metadados estão em artifacts/. Assinatura Android Debug
do ambiente. Atualizações desta variante precisam usar o mesmo certificado.
Código e auditoria publicados no fork público https://github.com/DaviSilvaTec/SpamBlocker-Brasil, branch `brasil`. Os snapshots e metadados preservam o estado histórico da entrega RC2.

O plano em [docs/PLANO_AUDITORIA_CORRECOES.md](docs/PLANO_AUDITORIA_CORRECOES.md) registra 24 achados abertos e as limitações de validação.
