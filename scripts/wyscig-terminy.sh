#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"
DAY="${DAY:-$(date -d "+$((RANDOM % 300 + 30)) days" +%Y-%m-%d)}"
REQUESTS="${REQUESTS:-15}"
STAGGER="${STAGGER:-0}"

ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/auth/token" -H 'Content-Type: application/json' \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -z "$ADMIN_TOKEN" ]; then
  echo "login failed for $ADMIN_EMAIL at $BASE_URL" >&2
  exit 1
fi
DOCTOR_ID="${DOCTOR_ID:-$(curl -s "$BASE_URL/doctors" -H "Authorization: Bearer $ADMIN_TOKEN" \
  | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)}"
if [ -z "$DOCTOR_ID" ]; then
  echo "no doctor found, set DOCTOR_ID" >&2
  exit 1
fi

if [ "$STAGGER" = "1" ]; then
  echo "$REQUESTS parallel POST /visits, doctor $DOCTOR_ID, $DAY 10:00-10:30 and 10:15-10:45 alternately"
else
  echo "$REQUESTS parallel POST /visits, doctor $DOCTOR_ID, $DAY 10:00-10:30"
fi

args=(--parallel --parallel-immediate --parallel-max "$REQUESTS")
for i in $(seq 1 "$REQUESTS"); do
  if [ "$STAGGER" = "1" ] && [ $((i % 2)) -eq 0 ]; then
    body="{\"doctorId\":$DOCTOR_ID,\"startsAt\":\"${DAY}T10:15:00\",\"endsAt\":\"${DAY}T10:45:00\"}"
  else
    body="{\"doctorId\":$DOCTOR_ID,\"startsAt\":\"${DAY}T10:00:00\",\"endsAt\":\"${DAY}T10:30:00\"}"
  fi
  if [ "$i" -gt 1 ]; then
    args+=(--next)
  fi
  args+=(-s -o /dev/null -w '%{http_code}\n' -X POST "$BASE_URL/visits" -H 'Content-Type: application/json'
    -H "Authorization: Bearer $ADMIN_TOKEN" -d "$body")
done
curl "${args[@]}" | sort | uniq -c
