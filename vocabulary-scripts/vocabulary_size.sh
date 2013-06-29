#!/bin/bash

tr 'A-Z' 'a-z' < $1 | tr -sc 'A-Za-z' '\n' | sort | uniq -c | sort -n -r | wc -l
