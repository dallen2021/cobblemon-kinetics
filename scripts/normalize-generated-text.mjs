#!/usr/bin/env node

let input = "";
process.stdin.setEncoding("utf8");
for await (const chunk of process.stdin) input += chunk;
process.stdout.write(`${input.trimEnd()}\n`);
