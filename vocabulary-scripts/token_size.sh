#!/bin/bash
tr 'A-Z' 'a-z' < $1 | tr -sc 'A-Za-z' '\n' | sort | wc -l
