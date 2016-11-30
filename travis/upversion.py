#!/usr/bin/python
import os

##
# Bump the smallest digit in a period-separated version string and print it.
##

# Pull the version string to up-version from the LAST_TAG environment variable, splitting it into version "digits"
split_tag = (os.environ['LAST_TAG'].split('-')[0]).split(".")

# Bump the last version digit
split_tag[-1] = str(int(split_tag[-1]) + 1)

# Re-combine the version digits, separated by a period, and print it
print ".".join(split_tag)
