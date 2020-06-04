import sys

assert sys.version_info >= (3,0)

try:
    import numpy
except ImportError:
    print("numpy is not installed")
exit()