import { execSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const gradlePath = path.join(__dirname, '..', 'build.gradle.kts');

const content = fs.readFileSync(gradlePath, 'utf8');
const match = content.match(/^version = "(\d+\.\d+\.\d+)"/m);

if (!match) {
  console.error('ERROR: Could not find version in build.gradle.kts');
  process.exit(1);
}

const current = match[1];
const [major, minor, patch] = current.split('.').map(Number);
const next = `${major}.${minor}.${patch + 1}`;

const updated = content.replace(/^(version = ")\d+\.\d+\.\d+(")/m, `$1${next}$2`);
fs.writeFileSync(gradlePath, updated, 'utf8');

console.log(`Version bumped: ${current} → ${next}`);

execSync(`git add "${gradlePath}"`, { stdio: 'inherit', cwd: path.join(__dirname, '..') });
execSync(`git commit -m "chore(version): bump to ${next}" --no-verify`, { stdio: 'inherit', cwd: path.join(__dirname, '..') });
execSync('git push', { stdio: 'inherit', cwd: path.join(__dirname, '..') });
