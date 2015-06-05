#!/bin/bash

CODE=javali_tests/escape_analysis/merge_sort.javali

OPTS=(
    ""
    "-no-stackalloc"
    "-no-lockremoval"
    "-no-stackalloc -no-lockremoval"
)

ulimit -s 256000

for SIZE in 500000 1000000 1500000 2000000; do
    sed -r 's/BENCH_SIZE = [0-9]+/BENCH_SIZE = '$SIZE'/' \
         javali_tests/escape_analysis/merge_sort.javali > bench.javali
    for opt in "${OPTS[@]}" ; do
        echo -n "$SIZE,$opt,"
        ./main $opt bench.javali 2>/dev/null
        for i in $(seq 1 10); do
            T=$(./bench.javali.bin | awk '{ print $2 }')
            echo -n "$T,"
        done
        echo
    done
done
