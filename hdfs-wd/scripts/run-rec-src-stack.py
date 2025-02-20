#!/usr/bin/python
import sys
import runExp
from policies import crash


if len(sys.argv) < 2:
	print('Correct usage: python run-rec-src-stack.py <No. of failures in an execution>')
	sys.exit(0)

MAX = int(sys.argv[1])	
inclPostFIds = False
useEqvCl = False
rIdOption = -1
clsLast = False
policies = []

#Allow only crashes policy
crashPolicy = crash.Crash()
policies.append(crashPolicy)

#Recovery clustering policy (source location + stack)
useEqvCl = True
rIdOption = 2

runExp.runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)
