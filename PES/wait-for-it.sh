#!/usr/bin/env bash
# Use this script from: https://github.com/vishnubob/wait-for-it
# Basic version for demonstration only

hostport="$1"
shift
host="${hostport%%:*}"
port="${hostport##*:}"

timeout=15
strict=0

while [[ "$1" ]]; do
  case "$1" in
    --timeout=*) timeout="${1#*=}" ;;
    --strict) strict=1 ;;
    --) shift; break ;;
  esac
  shift
done

start_ts=$(date +%s)
while :
do
  (echo > /dev/tcp/$host/$port) >/dev/null 2>&1
  result=$?
  if [[ $result -eq 0 ]]; then
    end_ts=$(date +%s)
    echo "wait-for-it.sh: $host:$port is available after $((end_ts - start_ts)) seconds"
    break
  fi
  sleep 1
done

exec "$@"
