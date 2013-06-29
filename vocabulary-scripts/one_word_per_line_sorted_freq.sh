#!/bin/bash

tr -sc 'A-Za-z' '\n' < $1 | sort | uniq -c | sort -n -r | less
