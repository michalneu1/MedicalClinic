#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin}"
PATIENT_A_EMAIL="${PATIENT_A_EMAIL:-jan.kowalski@example.com}"
PATIENT_A_PASSWORD="${PATIENT_A_PASSWORD:-haslo123}"
PATIENT_B_EMAIL="${PATIENT_B_EMAIL:-anna.nowak@example.com}"
PATIENT_B_PASSWORD="${PATIENT_B_PASSWORD:-silne123}"
START_DATE="${START_DATE:-$(date -d "+$((RANDOM % 300 + 30)) days" +%Y-%m-%d)}"
SLOTS="${SLOTS:-20}"

login() {
  local token
  token=$(curl -s -X POST "$BASE_URL/auth/token" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
  if [ -z "$token" ]; then
    echo "login failed for $1 at $BASE_URL" >&2
    exit 1
  fi
  echo "$token"
}

first_id() {
  grep -o '"id":[0-9]*' | head -1 | cut -d: -f2
}

patient_id() {
  local id
  id=$(curl -s -G "$BASE_URL/patients" --data-urlencode "email=$1" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | first_id)
  if [ -z "$id" ]; then
    echo "patient $1 not found" >&2
    exit 1
  fi
  echo "$id"
}

ADMIN_TOKEN=$(login "$ADMIN_EMAIL" "$ADMIN_PASSWORD")
TOKEN_A=$(login "$PATIENT_A_EMAIL" "$PATIENT_A_PASSWORD")
TOKEN_B=$(login "$PATIENT_B_EMAIL" "$PATIENT_B_PASSWORD")
PATIENT_A_ID=$(patient_id "$PATIENT_A_EMAIL")
PATIENT_B_ID=$(patient_id "$PATIENT_B_EMAIL")
DOCTOR_ID="${DOCTOR_ID:-$(curl -s "$BASE_URL/doctors" -H "Authorization: Bearer $ADMIN_TOKEN" | first_id)}"
if [ -z "$DOCTOR_ID" ]; then
  echo "no doctor found, set DOCTOR_ID" >&2
  exit 1
fi

visit_ids=()
for i in $(seq 0 $((SLOTS - 1))); do
  day=$(date -d "$START_DATE +$i days" +%Y-%m-%d)
  id=$(curl -s -X POST "$BASE_URL/visits" -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -d "{\"doctorId\":$DOCTOR_ID,\"startsAt\":\"${day}T10:00:00\",\"endsAt\":\"${day}T10:30:00\"}" | first_id)
  if [ -z "$id" ]; then
    echo "could not create visit of doctor $DOCTOR_ID on $day, set another START_DATE" >&2
    exit 1
  fi
  visit_ids+=("$id")
done
echo "created ${#visit_ids[@]} visits of doctor $DOCTOR_ID from $START_DATE"

for id in "${visit_ids[@]}"; do
  curl -s -o /dev/null -w '%{http_code}\n' -X PATCH "$BASE_URL/visits/$id/book/$PATIENT_A_ID" \
    -H "Authorization: Bearer $TOKEN_A" &
  curl -s -o /dev/null -w '%{http_code}\n' -X PATCH "$BASE_URL/visits/$id/book/$PATIENT_B_ID" \
    -H "Authorization: Bearer $TOKEN_B" &
  wait
done | sort | uniq -c
