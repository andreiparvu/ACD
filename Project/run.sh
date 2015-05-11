#!/bin/bash

./main $1
dot -Tpng $1.escape.dot -o $1.escape.png
