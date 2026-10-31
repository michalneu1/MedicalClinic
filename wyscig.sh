for i in $(seq 1 20); do
  d=$(printf '2030-06-%02d' "$i")
  curl -s -o /dev/null -X POST localhost:8080/visits -H 'Content-Type: application/json' \
       -d "{\"doctorId\":1,\"startsAt\":\"${d}T10:00:00\",\"endsAt\":\"${d}T10:30:00\"}"
done
for id in $(seq 1 20); do
  curl -s -o /dev/null -w '%{http_code}\n' -X PATCH localhost:8080/visits/$id/book/1 &
  curl -s -o /dev/null -w '%{http_code}\n' -X PATCH localhost:8080/visits/$id/book/2 &
  wait
done | sort | uniq -c
