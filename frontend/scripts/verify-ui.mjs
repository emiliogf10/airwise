// Opt-in jsdom/HTTP integration; the normal unit suite needs no running server.
import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('../', import.meta.url))
const result = spawnSync(process.execPath, ['node_modules/vitest/vitest.mjs', 'run', 'src/test/local.integration.test.tsx', '--mode', 'local-integration'], {
  cwd: root, stdio: 'inherit',
})
if (result.error) throw result.error
process.exitCode = result.status ?? 1
